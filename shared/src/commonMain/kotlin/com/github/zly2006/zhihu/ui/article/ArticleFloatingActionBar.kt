package com.github.zly2006.zhihu.ui.article

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.zly2006.zhihu.data.VoteUpState
import com.github.zly2006.zhihu.glass.GlassBackdrop
import com.github.zly2006.zhihu.icons.AppIcons
import com.github.zly2006.zhihu.icons.Icon
import com.github.zly2006.zhihu.ui.components.ActionToggleButton
import com.github.zly2006.zhihu.ui.components.BottomFloatingToolbar

/** 正文与侧滑预览共用操作栏布局，预览只展示缓存计数，不提交操作。 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ArticleFloatingActionBar(
    voteUpState: VoteUpState,
    voteUpCount: Int,
    commentCount: Int,
    isFavorited: Boolean,
    onVoteUpStateChange: (VoteUpState) -> Unit,
    onShowComments: () -> Unit,
    onShowCollection: () -> Unit,
    onShowActions: () -> Unit,
    glassBackdrop: GlassBackdrop? = null,
) {
    BottomFloatingToolbar(glassBackdrop = glassBackdrop) {
        val voteColor = voteUpNeutralContent()
        ActionToggleButton(
            checked = voteUpState == VoteUpState.Up,
            onCheckedChange = { checked ->
                onVoteUpStateChange(if (checked) VoteUpState.Up else VoteUpState.Neutral)
            },
            icon = { Icon(if (voteUpState == VoteUpState.Up) AppIcons.ThumbUpFilled else AppIcons.ThumbUp, "赞同") },
            label = voteUpCount.toString(),
            checkedContentColor = voteColor,
        )
        ActionToggleButton(
            checked = voteUpState == VoteUpState.Down,
            onCheckedChange = { checked ->
                onVoteUpStateChange(if (checked) VoteUpState.Down else VoteUpState.Neutral)
            },
            icon = { Icon(if (voteUpState == VoteUpState.Down) AppIcons.ThumbDownFilled else AppIcons.ThumbDown, "反对") },
            checkedContentColor = voteColor,
        )
        TextButton(
            onClick = onShowComments,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
        ) {
            Icon(AppIcons.Comment, contentDescription = "评论")
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text(text = "$commentCount")
        }
        ActionToggleButton(
            checked = isFavorited,
            // 收藏要选收藏夹，点击总是打开收藏夹列表，选中态只反映结果。
            onCheckedChange = { onShowCollection() },
            icon = {
                Icon(if (isFavorited) AppIcons.BookmarkFilled else AppIcons.Bookmark, contentDescription = "收藏")
            },
            checkedContentColor = MaterialTheme.colorScheme.tertiary,
        )
        IconButton(onClick = onShowActions) {
            Icon(AppIcons.MoreVert, contentDescription = "更多选项")
        }
    }
}
