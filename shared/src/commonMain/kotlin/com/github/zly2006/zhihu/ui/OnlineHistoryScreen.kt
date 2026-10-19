/*
 * Zhihu-Hyperion - Free & Ad-Free Zhihu client for all platforms.
 * Copyright (C) 2024-2026, zly2006 <i@zly2006.me>
 * Co-author: eltavine <me@eltavine.com>
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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.zly2006.zhihu.data.HistoryStorage
import com.github.zly2006.zhihu.icons.AppIcons
import com.github.zly2006.zhihu.icons.Icon
import com.github.zly2006.zhihu.navigation.History
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.requestLoginNavigation
import com.github.zly2006.zhihu.platform.PlatformBackHandler
import com.github.zly2006.zhihu.platform.SettingsStore
import com.github.zly2006.zhihu.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.reading.RegisterReadingQueueSource
import com.github.zly2006.zhihu.ui.components.EmptyState
import com.github.zly2006.zhihu.ui.components.EmptyStateAction
import com.github.zly2006.zhihu.ui.components.FeedCard
import com.github.zly2006.zhihu.ui.components.PaginatedList
import com.github.zly2006.zhihu.ui.components.ProgressIndicatorFooter
import com.github.zly2006.zhihu.ui.components.pageTurnViewportWithGuide
import com.github.zly2006.zhihu.ui.components.rememberPageTurnTarget
import com.github.zly2006.zhihu.ui.subscreens.BOTTOM_BAR_SINGLE_TAP_REFRESH_PREFERENCE_KEY
import com.github.zly2006.zhihu.util.suspendRunCatching
import com.github.zly2006.zhihu.viewmodel.deleteOnlineHistory
import com.github.zly2006.zhihu.viewmodel.feed.OnlineHistoryViewModel
import com.github.zly2006.zhihu.viewmodel.rememberZhihuApiEnvironment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

const val ONLINE_HISTORY_OVERFLOW_TAG = "online_history_overflow"

/**
 * 在线浏览历史页面。
 *
 * 页面展示知乎账号侧的浏览历史，提供刷新、分页加载和清空历史入口。它既可能作为底部栏 tab 出现，也可能从账号页快捷入口独立 push。
 * 当前实现本身不绘制返回箭头，底栏显隐由外层 route 决定；如果包装层需要独立返回入口，应由包装层显式传入对应状态。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineHistoryScreen(
    scrollToTopTrigger: Int = 0,
    isActive: Boolean = true,
    innerPadding: PaddingValues = PaddingValues(0.dp),
) {
    val navigator = LocalNavigator.current
    val history = koinInject<HistoryStorage>()
    val settings = koinInject<SettingsStore>()
    val viewModel: OnlineHistoryViewModel = viewModel { OnlineHistoryViewModel(settings, history) }
    val readingQueueSourceId = "history:online"
    if (isActive) {
        RegisterReadingQueueSource(
            sourceId = readingQueueSourceId,
            items = viewModel.displayItems,
        )
    }
    val paginationEnvironment = rememberZhihuApiEnvironment(allowGuestAccess = false)
    val userMessages = rememberUserMessageSink()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var cachedScrollToTopTrigger by remember { mutableIntStateOf(scrollToTopTrigger) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showActionsMenu by remember { mutableStateOf(false) }
    val pageTurnTarget = rememberPageTurnTarget(
        listState = listState,
        enabled = isActive && !showClearHistoryDialog && !showActionsMenu,
    )

    val account by rememberAccountSettingsAccountState()
    LaunchedEffect(account.login) {
        if (account.login && viewModel.displayItems.isEmpty()) {
            viewModel.refresh(paginationEnvironment)
        }
    }

    LaunchedEffect(scrollToTopTrigger, isActive) {
        val action = topLevelReselectAction(
            triggerDelta = scrollToTopTrigger - cachedScrollToTopTrigger,
            isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0,
            refreshOnSingleTap = settings.getBoolean(BOTTOM_BAR_SINGLE_TAP_REFRESH_PREFERENCE_KEY, false),
        )
        if (isActive) {
            when (action) {
                TopLevelReselectAction.Refresh -> {
                    listState.requestScrollToItem(0)
                    viewModel.refresh(paginationEnvironment)
                }

                TopLevelReselectAction.ScrollToTop -> {
                    listState.animateScrollToItem(0)
                }

                null -> {}
            }
        }
        cachedScrollToTopTrigger = scrollToTopTrigger
    }

    PlatformBackHandler(enabled = showClearHistoryDialog) {
        showClearHistoryDialog = false
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = innerPadding.calculateBottomPadding()),
        topBar = {
            TopAppBar(
                title = { Text("历史记录") },
                actions = {
                    PlatformBackHandler(enabled = showActionsMenu) {
                        showActionsMenu = false
                    }
                    IconButton(
                        modifier = Modifier.testTag(ONLINE_HISTORY_OVERFLOW_TAG),
                        onClick = { showActionsMenu = true },
                    ) {
                        Icon(
                            AppIcons.MoreVert,
                            contentDescription = "更多选项",
                        )

                        DropdownMenu(
                            expanded = showActionsMenu,
                            onDismissRequest = { showActionsMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("查看本地历史记录") },
                                leadingIcon = { Icon(AppIcons.History, contentDescription = null) },
                                onClick = {
                                    showActionsMenu = false
                                    navigator.onNavigate(History)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("清除历史记录") },
                                leadingIcon = { Icon(AppIcons.DeleteSweep, contentDescription = null) },
                                onClick = {
                                    showActionsMenu = false
                                    showClearHistoryDialog = true
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (showClearHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showClearHistoryDialog = false },
                icon = { Icon(AppIcons.DeleteSweep, contentDescription = null) },
                title = { Text("确认清除历史记录") },
                text = { Text("此操作会清除当前账号的在线和本地的全部历史记录。") },
                confirmButton = {
                    TextButton(onClick = {
                        showClearHistoryDialog = false
                        coroutineScope.launch {
                            history.clear()
                            suspendRunCatching {
                                if ("d_c0" in paginationEnvironment.authenticatedCookies()) {
                                    paginationEnvironment.deleteOnlineHistory(emptyList(), clear = true)
                                }
                            }.onSuccess {
                                userMessages.showShortMessage("已清除所有历史记录")
                            }.onFailure {
                                userMessages.showShortMessage("在线历史记录清除失败：${it.message}")
                            }
                            // 重新加载而不是只清空本地列表：历史超过一页时只清空列表不会到底，“还没有浏览记录”不会出现。
                            viewModel.refresh(paginationEnvironment)
                        }
                    }) {
                        Text("确认")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryDialog = false }) {
                        Text("我再想想")
                    }
                },
            )
        }
        if (!account.login) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = AppIcons.Login,
                    title = "登录后查看浏览历史",
                    action = EmptyStateAction("登录", AppIcons.Login, onClick = ::requestLoginNavigation),
                )
            }
            return@Scaffold
        }
        FeedPullToRefresh(viewModel, paginationEnvironment, padding = innerPadding) {
            PaginatedList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pageTurnViewportWithGuide(pageTurnTarget)
                    .testTag("online_history_list"),
                items = viewModel.displayItems,
                listState = listState,
                onLoadMore = { viewModel.loadMore(paginationEnvironment) },
                isEnd = { viewModel.isEnd },
                key = { item -> item.stableKey },
                footer = ProgressIndicatorFooter,
                loadFailed = viewModel.errorMessage != null,
                onRetry = { viewModel.retry(paginationEnvironment) },
                loadFailureMessage = viewModel.apiError?.message,
                loadFailureDescription = viewModel.errorMessage.takeIf { viewModel.apiError == null },
                onLogin = (::requestLoginNavigation).takeIf { viewModel.apiError?.needLogin == true },
                emptyContent = {
                    EmptyState(AppIcons.History, "还没有浏览记录", Modifier.fillMaxWidth())
                },
                centerStatusWhenEmpty = true,
            ) { item ->
                FeedCard(
                    item,
                    readingQueueSourceId = readingQueueSourceId.takeIf { isActive },
                    menuItems = { dismissMenu ->
                        DropdownMenuItem(
                            text = { Text("删除该条历史记录") },
                            leadingIcon = { Icon(AppIcons.Delete, contentDescription = null) },
                            onClick = {
                                dismissMenu()
                                coroutineScope.launch {
                                    try {
                                        viewModel.deleteItem(paginationEnvironment, item)
                                        userMessages.showShortMessage("已删除")
                                    } catch (error: CancellationException) {
                                        throw error
                                    } catch (_: Exception) {
                                        userMessages.showShortMessage("操作失败")
                                    }
                                }
                            },
                        )
                    },
                )
            }
        }
    }
}
