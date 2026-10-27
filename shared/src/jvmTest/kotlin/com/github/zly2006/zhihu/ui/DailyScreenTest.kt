/*
 * Zhihu-Hyperion - Free & Ad-Free Zhihu client for all platforms.
 * Copyright (C) 2026, eltavine <me@eltavine.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation (version 3 only).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.zly2006.zhihu.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.github.zly2006.zhihu.data.installZhihuCommonClientConfig
import com.github.zly2006.zhihu.navigation.Article
import com.github.zly2006.zhihu.navigation.ArticleType
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.NavDestination
import com.github.zly2006.zhihu.navigation.Navigator
import com.github.zly2006.zhihu.platform.MapSettingsStore
import com.github.zly2006.zhihu.platform.SettingsStore
import com.github.zly2006.zhihu.viewmodel.ZhihuApiEnvironment
import com.github.zly2006.zhihu.viewmodel.ZhihuGuestClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals

/** 夹具为 2026-10-28 从日报列表及 /api/7/story/{id} 获取的原始公开响应。 */
@OptIn(ExperimentalTestApi::class)
class DailyScreenTest {
    /** https://github.com/eltavine/Zhihu-Hyperion/issues/68：合辑原文是 None，点击后仍应能阅读完整日报。 */
    @Test
    fun compilationWithoutValidOriginOpensTheDailyPage() = checkStoryOpening(
        id = 9793548,
        expectedPage = "https://daily.zhihu.com/story/9793548",
    )

    @Test
    fun ordinaryStoryStillOpensItsAnswerInTheApp() = checkStoryOpening(
        id = 9793544,
        expectedDestination = Article(type = ArticleType.Answer, id = 2057428446440076787),
    )

    private fun checkStoryOpening(id: Long, expectedPage: String? = null, expectedDestination: NavDestination? = null) {
        val requestedUrls = CopyOnWriteArrayList<String>()
        val engine = MockEngine { request ->
            requestedUrls += request.url.toString()
            val resource = when (request.url.encodedPath) {
                "/api/4/stories/latest" -> "/daily/latest.json"
                "/api/7/story/$id" -> "/daily/$id.json"
                else -> null
            }
            respond(
                resource?.let { javaClass.getResource(it)!!.readText() }
                    ?: """{"date":"20261026","stories":[]}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(engine) {
            installZhihuCommonClientConfig(mutableMapOf(), "test")
        }
        val guest = ZhihuGuestClient(engine, "test")
        val owner = object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
        val koin = koinApplication {
            modules(
                module {
                    single<SettingsStore> { MapSettingsStore() }
                    single { guest }
                    single<ZhihuApiEnvironment> {
                        object : ZhihuApiEnvironment {
                            override fun httpClient() = client

                            override fun authenticatedCookies() = emptyMap<String, String>()

                            override suspend fun handleFetchFailure(tag: String?, error: Exception) = throw error
                        }
                    }
                },
            )
        }
        val openedPages = CopyOnWriteArrayList<String>()
        val destinations = CopyOnWriteArrayList<NavDestination>()
        try {
            runComposeUiTest {
                setContent {
                    KoinIsolatedContext(koin) {
                        CompositionLocalProvider(
                            LocalViewModelStoreOwner provides owner,
                            LocalNavigator provides Navigator(destinations::add, {}),
                            LocalUriHandler provides object : UriHandler {
                                override fun openUri(uri: String) {
                                    openedPages += uri
                                }
                            },
                        ) {
                            MaterialTheme { DailyScreen() }
                        }
                    }
                }
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodesWithTag("daily_screen_list").fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithTag("daily_screen_list").performScrollToNode(hasTestTag("daily_screen_story_$id"))
                onNodeWithTag("daily_screen_story_$id").performClick()
                waitForIdle()
                waitUntil(timeoutMillis = 10_000) {
                    openedPages.isNotEmpty() || destinations.isNotEmpty() ||
                        onAllNodesWithText("由于知乎的 Bug，无法找到原文").fetchSemanticsNodes().isNotEmpty()
                }
                assertEquals(listOfNotNull(expectedPage), openedPages)
                assertEquals(listOfNotNull(expectedDestination), destinations)
                assertEquals(1, requestedUrls.count { it == "https://daily.zhihu.com/api/7/story/$id" })
            }
        } finally {
            owner.viewModelStore.clear()
            koin.close()
            guest.close()
            client.close()
            engine.close()
        }
    }
}
