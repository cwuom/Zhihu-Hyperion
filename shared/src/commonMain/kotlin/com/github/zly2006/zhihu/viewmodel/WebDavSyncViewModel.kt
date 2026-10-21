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

package com.github.zly2006.zhihu.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.zly2006.zhihu.data.ANSWER_VOTEUP_THRESHOLD_PREFERENCE_KEY
import com.github.zly2006.zhihu.data.ARTICLE_FOLLOWERS_THRESHOLD_PREFERENCE_KEY
import com.github.zly2006.zhihu.data.ARTICLE_VOTEUP_THRESHOLD_PREFERENCE_KEY
import com.github.zly2006.zhihu.data.QUALITY_FILTER_MODE_PREFERENCE_KEY
import com.github.zly2006.zhihu.data.QUESTION_ANSWER_THRESHOLD_PREFERENCE_KEY
import com.github.zly2006.zhihu.data.QUESTION_FOLLOWERS_THRESHOLD_PREFERENCE_KEY
import com.github.zly2006.zhihu.data.VIDEO_FOLLOWERS_THRESHOLD_PREFERENCE_KEY
import com.github.zly2006.zhihu.data.VIDEO_VOTE_THRESHOLD_PREFERENCE_KEY
import com.github.zly2006.zhihu.platform.ARTICLE_SWIPE_BACK_PREFERENCE_KEY
import com.github.zly2006.zhihu.platform.ENABLE_SEGMENT_ACTION_SHEET_PREFERENCE_KEY
import com.github.zly2006.zhihu.platform.MACOS_QUIT_ON_WINDOW_CLOSE_PREFERENCE_KEY
import com.github.zly2006.zhihu.platform.SettingsStore
import com.github.zly2006.zhihu.theme.DUO3_TIQIAN_MARKDOWN_PREFERENCE_KEY
import com.github.zly2006.zhihu.theme.DUO3_TIQIAN_MATH_FONT_PREFERENCE_KEY
import com.github.zly2006.zhihu.theme.PREF_BLOCK_SPACING
import com.github.zly2006.zhihu.theme.PREF_FONT_SIZE
import com.github.zly2006.zhihu.theme.PREF_LINE_HEIGHT
import com.github.zly2006.zhihu.ui.article.ANSWER_DOUBLE_TAP_ACTION_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.article.ARTICLE_USE_WEBVIEW_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.components.ANSWER_SWITCH_SENSITIVITY_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.components.DISABLE_BOTTOM_SHEET_ROUNDED_CORNERS_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.components.PREF_FAB_OPACITY
import com.github.zly2006.zhihu.ui.components.PREF_PAGE_TURN_PERCENT
import com.github.zly2006.zhihu.ui.components.PREF_PAGE_TURN_SWITCH_ANSWER
import com.github.zly2006.zhihu.ui.components.PREF_SHOW_CONTENT_END_MARKER
import com.github.zly2006.zhihu.ui.components.PREF_SHOW_PAGE_TURN_FAB
import com.github.zly2006.zhihu.ui.components.PREF_SHOW_PAGE_TURN_GUIDE
import com.github.zly2006.zhihu.ui.components.PREF_VOLUME_KEY_PAGE_TURN
import com.github.zly2006.zhihu.ui.subscreens.BOTTOM_BAR_SINGLE_TAP_REFRESH_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.subscreens.COLLECTION_DIRECT_BROWSE_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.subscreens.CONTINUOUS_USAGE_REMINDER_INTERVAL_MINUTES_KEY
import com.github.zly2006.zhihu.ui.subscreens.DUO3_CARD_LARGE_TITLE_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.subscreens.ENABLE_PREDICTIVE_BACK_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.subscreens.LANDSCAPE_LIST_DETAIL_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.subscreens.TOP_LEVEL_BACK_TO_LAUNCHER_PREFERENCE_KEY
import com.github.zly2006.zhihu.update.CHECK_NIGHTLY_UPDATES_PREFERENCE_KEY
import com.github.zly2006.zhihu.util.TextDocumentStore
import com.github.zly2006.zhihu.viewmodel.feed.AUTO_REFRESH_HOME_ON_STARTUP_PREFERENCE_KEY
import com.github.zly2006.zhihu.viewmodel.filter.ContentFilterDatabase
import com.github.zly2006.zhihu.viewmodel.filter.encodeBlocklistBackup
import com.github.zly2006.zhihu.viewmodel.filter.importBlocklistBackupFromJsonText
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import io.ktor.util.encodeBase64
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlin.time.Clock

private const val BACKUP_DIRECTORY = "zhihu-hyperion/"
private const val BLOCKLIST_FILE = "blocklist.json"
private const val SETTINGS_FILE = "settings.json"

private val webDavJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    encodeDefaults = true
}

/** WebDAV 连接配置；与普通设置分开存放，不会随设置上传，也排除在 Android 云备份之外。 */
@Serializable
data class WebDavConfig(
    val url: String = "",
    val username: String = "",
    val password: String = "",
)

class WebDavConfigFile(
    private val document: TextDocumentStore,
) {
    fun load(): WebDavConfig = document
        .readText()
        ?.let { runCatching { webDavJson.decodeFromString(WebDavConfig.serializer(), it) }.getOrNull() }
        ?: WebDavConfig()

    fun save(config: WebDavConfig) = document.writeText(webDavJson.encodeToString(WebDavConfig.serializer(), config))
}

private enum class SyncedSettingType { BOOLEAN, INT, FLOAT, STRING }

/**
 * 参与同步的设置及其类型。桌面和 macOS 的设置文件只保存字符串，恢复到 Android 时必须按原类型写回，
 * 否则 SharedPreferences 按类型读取会抛 ClassCastException。这里只登记设置页里的用户偏好：
 * 开发者选项、需要每台设备单独同意的 AIGC 标记、取决于本机网络的 GitHub 加速、随平台调整默认值的底栏配置和液态玻璃，
 * 以及引用本机字体文件的 WebView 字体都不同步。
 */
private val syncedSettings: Map<String, SyncedSettingType> = buildMap {
    listOf(
        AUTO_REFRESH_HOME_ON_STARTUP_PREFERENCE_KEY,
        CHECK_NIGHTLY_UPDATES_PREFERENCE_KEY,
        COLLECTION_DIRECT_BROWSE_PREFERENCE_KEY,
        DISABLE_BOTTOM_SHEET_ROUNDED_CORNERS_PREFERENCE_KEY,
        DUO3_CARD_LARGE_TITLE_PREFERENCE_KEY,
        DUO3_TIQIAN_MARKDOWN_PREFERENCE_KEY,
        LANDSCAPE_LIST_DETAIL_PREFERENCE_KEY,
        MACOS_QUIT_ON_WINDOW_CLOSE_PREFERENCE_KEY,
        ARTICLE_USE_WEBVIEW_PREFERENCE_KEY,
        ARTICLE_SWIPE_BACK_PREFERENCE_KEY,
        ENABLE_SEGMENT_ACTION_SHEET_PREFERENCE_KEY,
        PREF_PAGE_TURN_SWITCH_ANSWER,
        PREF_SHOW_CONTENT_END_MARKER,
        PREF_SHOW_PAGE_TURN_FAB,
        PREF_SHOW_PAGE_TURN_GUIDE,
        PREF_VOLUME_KEY_PAGE_TURN,
        TOP_LEVEL_BACK_TO_LAUNCHER_PREFERENCE_KEY,
        "autoHideArticleBottomBar",
        "autoHideBottomBar",
        "autoHideSkipAnswerButton",
        "blockPaidContent",
        "blockWeChatOfficialAccount",
        "blockZhihuAdPlatform",
        "blockZhihuSchool",
        "bottomBarTapScrollToTop",
        BOTTOM_BAR_SINGLE_TAP_REFRESH_PREFERENCE_KEY,
        "buttonSkipAnswer",
        "duo3_all",
        "duo3_article_actions",
        "duo3_article_bar",
        "duo3_card_appearance",
        "duo3_card_layout",
        "duo3_home_account",
        "enableContentFilter",
        "enableKeywordBlocking",
        "enableTopicBlocking",
        "enableUserBlocking",
        ENABLE_PREDICTIVE_BACK_PREFERENCE_KEY,
        "filterFollowedUserContent",
        "loginForRecommendation",
        "pinAnswerDate",
        "reverseBlock",
        "showFeedThumbnail",
        "showRefreshFab",
        "showSearchHistory",
        "showSearchHotSearch",
        "titleAutoHide",
        "useDynamicColor",
        "use_custom_nav_host",
        "webviewHardwareAcceleration",
    ).forEach { put(it, SyncedSettingType.BOOLEAN) }
    listOf(
        ANSWER_VOTEUP_THRESHOLD_PREFERENCE_KEY,
        ARTICLE_FOLLOWERS_THRESHOLD_PREFERENCE_KEY,
        ARTICLE_VOTEUP_THRESHOLD_PREFERENCE_KEY,
        CONTINUOUS_USAGE_REMINDER_INTERVAL_MINUTES_KEY,
        PREF_BLOCK_SPACING,
        PREF_FAB_OPACITY,
        PREF_FONT_SIZE,
        PREF_LINE_HEIGHT,
        PREF_PAGE_TURN_PERCENT,
        QUESTION_ANSWER_THRESHOLD_PREFERENCE_KEY,
        QUESTION_FOLLOWERS_THRESHOLD_PREFERENCE_KEY,
        VIDEO_FOLLOWERS_THRESHOLD_PREFERENCE_KEY,
        VIDEO_VOTE_THRESHOLD_PREFERENCE_KEY,
        "backgroundColorDark",
        "backgroundColorLight",
        "customThemeColor",
        "luotianyi_color",
        "topicBlockingThreshold",
    ).forEach { put(it, SyncedSettingType.INT) }
    put(ANSWER_SWITCH_SENSITIVITY_PREFERENCE_KEY, SyncedSettingType.FLOAT)
    listOf(
        ANSWER_DOUBLE_TAP_ACTION_PREFERENCE_KEY,
        DUO3_TIQIAN_MATH_FONT_PREFERENCE_KEY,
        QUALITY_FILTER_MODE_PREFERENCE_KEY,
        "answerSwitchMode",
        "feedCardStyle",
        "recommendationMode",
        "shareActionMode",
        "themeMode",
    ).forEach { put(it, SyncedSettingType.STRING) }
}

@Serializable
private data class SettingsBackup(
    val version: Int = 1,
    val exportTime: Long = Clock.System.now().toEpochMilliseconds(),
    val values: Map<String, JsonPrimitive> = emptyMap(),
)

/**
 * 手动把屏蔽列表和 [syncedSettings] 上传到用户自己的 WebDAV 目录，或从中恢复。恢复沿用本地文件导入的语义：
 * 屏蔽条目只合并不删除，设置按键覆盖。请求用 Koin 的共享引擎新建客户端，不带知乎账号的 Cookie。
 */
class WebDavSyncViewModel(
    private val settings: SettingsStore,
    private val engine: HttpClientEngine,
    private val database: ContentFilterDatabase,
    private val configFile: WebDavConfigFile,
) : ViewModel() {
    var config by mutableStateOf(configFile.load())
    var isRunning by mutableStateOf(false)
        private set
    var status by mutableStateOf<String?>(null)
        private set

    fun saveConfig() = configFile.save(config)

    fun testConnection() = launchRequest { client, directory ->
        client
            .request(directory) {
                method = HttpMethod("PROPFIND")
                header("Depth", "0")
            }.requireSuccess("连接")
        "连接成功"
    }

    fun upload() = launchRequest { client, directory ->
        val backupDirectory = directory + BACKUP_DIRECTORY
        val created = client.request(backupDirectory) { method = HttpMethod("MKCOL") }
        // WebDAV answers 405 when the collection already exists.
        if (created.status != HttpStatusCode.MethodNotAllowed) created.requireSuccess("创建备份目录")
        val blocklist = encodeBlocklistBackup(
            database.blockedKeywordDao(),
            database.blockedUserDao(),
            database.blockedQuestionAuthorDao(),
            database.blockedTopicDao(),
        )
        client
            .put(backupDirectory + BLOCKLIST_FILE) { setBody(TextContent(blocklist, ContentType.Application.Json)) }
            .requireSuccess("上传屏蔽列表")
        val values = syncedSettings
            .filterKeys(settings::contains)
            .mapValues { (key, type) ->
                when (type) {
                    SyncedSettingType.BOOLEAN -> JsonPrimitive(settings.getBoolean(key, false))
                    SyncedSettingType.INT -> JsonPrimitive(settings.getInt(key, 0))
                    SyncedSettingType.FLOAT -> JsonPrimitive(settings.getFloat(key, 0f))
                    SyncedSettingType.STRING -> JsonPrimitive(settings.getString(key, ""))
                }
            }
        val settingsText = webDavJson.encodeToString(SettingsBackup.serializer(), SettingsBackup(values = values))
        client
            .put(backupDirectory + SETTINGS_FILE) { setBody(TextContent(settingsText, ContentType.Application.Json)) }
            .requireSuccess("上传设置")
        "已上传屏蔽列表和 ${values.size} 项设置"
    }

    fun restore() = launchRequest { client, directory ->
        val backupDirectory = directory + BACKUP_DIRECTORY
        val blocklistResponse = client.get(backupDirectory + BLOCKLIST_FILE)
        val settingsResponse = client.get(backupDirectory + SETTINGS_FILE)
        if (blocklistResponse.status == HttpStatusCode.NotFound && settingsResponse.status == HttpStatusCode.NotFound) {
            return@launchRequest "WebDAV 上还没有备份"
        }
        val restored = mutableListOf<String>()
        if (blocklistResponse.status != HttpStatusCode.NotFound) {
            val summary = importBlocklistBackupFromJsonText(
                database.blockedKeywordDao(),
                database.blockedUserDao(),
                database.blockedQuestionAuthorDao(),
                database.blockedTopicDao(),
                blocklistResponse.requireSuccess("下载屏蔽列表").bodyAsText(),
            )
            restored += "屏蔽列表（$summary）"
        }
        if (settingsResponse.status != HttpStatusCode.NotFound) {
            val backup = webDavJson.decodeFromString(
                SettingsBackup.serializer(),
                settingsResponse.requireSuccess("下载设置").bodyAsText(),
            )
            val applied = backup.values.count { (key, value) ->
                when (syncedSettings[key]) {
                    SyncedSettingType.BOOLEAN -> value.booleanOrNull?.also { settings.putBoolean(key, it) }
                    SyncedSettingType.INT -> value.intOrNull?.also { settings.putInt(key, it) }
                    SyncedSettingType.FLOAT -> value.floatOrNull?.also { settings.putFloat(key, it) }
                    SyncedSettingType.STRING -> value.takeIf { it.isString }?.content?.also { settings.putString(key, it) }
                    null -> null
                } != null
            }
            restored += "$applied 项设置（部分设置重启后生效）"
        }
        "已恢复" + restored.joinToString("、")
    }

    private fun launchRequest(block: suspend (client: HttpClient, directory: String) -> String) {
        if (isRunning) return
        saveConfig()
        val current = config
        val directory = current.url.trim().let { if (it.endsWith('/')) it else "$it/" }
        val host = runCatching { Url(directory).host }.getOrNull().orEmpty()
        // Basic auth sends the password with every request, so only HTTPS is accepted; loopback never leaves the
        // device, which keeps a local WebDAV server usable for testing.
        val secure = directory.startsWith("https://", ignoreCase = true) ||
            (directory.startsWith("http://", ignoreCase = true) && host in setOf("127.0.0.1", "localhost", "::1", "[::1]"))
        if (!secure || host.isEmpty()) {
            status = "请填写以 https:// 开头的 WebDAV 目录地址"
            return
        }
        isRunning = true
        status = null
        viewModelScope.launch {
            status = try {
                HttpClient(engine) {
                    expectSuccess = false
                    if (current.username.isNotEmpty()) {
                        defaultRequest {
                            header(HttpHeaders.Authorization, "Basic " + "${current.username}:${current.password}".encodeBase64())
                        }
                    }
                }.use { block(it, directory) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.message ?: "WebDAV 请求失败"
            } finally {
                isRunning = false
            }
        }
    }
}

private fun HttpResponse.requireSuccess(action: String): HttpResponse {
    if (status.isSuccess()) return this
    val reason = when (status) {
        HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> "用户名或应用密码错误"
        HttpStatusCode.NotFound -> "目录不存在"
        else -> status.description
    }
    throw IllegalStateException("${action}失败：$reason（HTTP ${status.value}）")
}
