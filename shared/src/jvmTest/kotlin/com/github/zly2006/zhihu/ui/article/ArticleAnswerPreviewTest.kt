package com.github.zly2006.zhihu.ui.article

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.navigation.Article
import com.github.zly2006.zhihu.navigation.ArticleType
import com.github.zly2006.zhihu.viewmodel.ArticleViewModel.CachedAnswerContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ArticleAnswerPreviewTest {
    @Test
    fun cachedPreviewUsesTheCurrentAnswerToolbar() = runComposeUiTest {
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Box(Modifier.size(390.dp, 844.dp)) {
                    CachedAnswerPreview(
                        cachedAnswer,
                        useTiqianMarkdown = false,
                        liquidGlassEnabled = false,
                        useDuo3ArticleActions = false,
                    )
                }
            }
        }

        val pixels = onRoot().captureToImage().toPixelMap()
        val legacyBlue = Color(0xFF40B6F6).toArgb()
        val legacyPixels = (0 until pixels.width).sumOf { x ->
            (pixels.height / 2 until pixels.height).count { y -> pixels[x, y].toArgb() == legacyBlue }
        }
        assertEquals(0, legacyPixels, "侧滑预览仍绘制旧版蓝色赞同按钮")
        onNodeWithContentDescription("赞同").assertExists()
        onNodeWithContentDescription("反对").assertExists()
        onNodeWithContentDescription("评论").assertExists()
        onNodeWithContentDescription("收藏").assertExists()
        onNodeWithContentDescription("更多选项").assertExists()
        onNodeWithText("5695").assertExists()
        onNodeWithText("47").assertExists()
    }

    @Test
    fun cachedPreviewFollowsGlassSettingInBothThemes() {
        for (dark in listOf(false, true)) {
            runComposeUiTest {
                var glassEnabled by mutableStateOf(false)
                setContent {
                    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                        Box(Modifier.size(390.dp, 844.dp)) {
                            CachedAnswerPreview(
                                cachedAnswer,
                                useTiqianMarkdown = false,
                                liquidGlassEnabled = glassEnabled,
                                useDuo3ArticleActions = false,
                            )
                        }
                    }
                }

                val opaque = onRoot().captureToImage().toPixelMap()
                val voteBounds = onNodeWithContentDescription("赞同").fetchSemanticsNode().boundsInRoot
                runOnIdle { glassEnabled = true }
                val glass = onRoot().captureToImage().toPixelMap()
                assertEquals(voteBounds, onNodeWithContentDescription("赞同").fetchSemanticsNode().boundsInRoot)
                val changedPixels = (0 until opaque.width).sumOf { x ->
                    (opaque.height / 2 until opaque.height).count { y -> opaque[x, y] != glass[x, y] }
                }
                assertTrue(changedPixels > 1000, "切换玻璃设置后底栏仅改变 $changedPixels 个像素")
                runOnIdle { glassEnabled = false }
                val restored = onRoot().captureToImage().toPixelMap()
                val remainingDifferences = (0 until opaque.width).sumOf { x ->
                    (opaque.height / 2 until opaque.height).count { y -> opaque[x, y] != restored[x, y] }
                }
                assertEquals(0, remainingDifferences, "关闭玻璃后未恢复原底栏")
            }
        }
    }

    private val cachedAnswer = CachedAnswerContent(
        article = Article(id = 1, type = ArticleType.Answer),
        title = "Preview answer",
        authorName = "Author",
        authorBio = "",
        authorAvatarUrl = "",
        content = "",
        voteUpCount = 5695,
        commentCount = 47,
    )
}
