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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.github.zly2006.zhihu.data.DataHolder
import com.github.zly2006.zhihu.data.Feed
import com.github.zly2006.zhihu.data.target
import com.github.zly2006.zhihu.icons.AppIcons
import com.github.zly2006.zhihu.icons.Icon
import com.github.zly2006.zhihu.navigation.FollowUserUpdates
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.requestLoginNavigation
import com.github.zly2006.zhihu.platform.SettingsStore
import com.github.zly2006.zhihu.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.reading.RegisterReadingQueueSource
import com.github.zly2006.zhihu.ui.components.AppLoadingIndicator
import com.github.zly2006.zhihu.ui.components.DraggableRefreshButton
import com.github.zly2006.zhihu.ui.components.EmptyState
import com.github.zly2006.zhihu.ui.components.EmptyStateAction
import com.github.zly2006.zhihu.ui.components.FeedAuthorBlockConfirmDialog
import com.github.zly2006.zhihu.ui.components.FeedAuthorBlockRequest
import com.github.zly2006.zhihu.ui.components.FeedAuthorBlockType
import com.github.zly2006.zhihu.ui.components.FeedCard
import com.github.zly2006.zhihu.ui.components.NoOpPagerNestedScrollConnection
import com.github.zly2006.zhihu.ui.components.PaginatedList
import com.github.zly2006.zhihu.ui.components.ProgressIndicatorFooter
import com.github.zly2006.zhihu.ui.components.pageTurnViewportWithGuide
import com.github.zly2006.zhihu.ui.components.rememberNestedHorizontalPagerConnection
import com.github.zly2006.zhihu.ui.components.rememberPageTurnTarget
import com.github.zly2006.zhihu.ui.subscreens.BOTTOM_BAR_SINGLE_TAP_REFRESH_PREFERENCE_KEY
import com.github.zly2006.zhihu.viewmodel.feed.FollowRecommendViewModel
import com.github.zly2006.zhihu.viewmodel.feed.FollowViewModel
import com.github.zly2006.zhihu.viewmodel.feed.RecentMomentsViewModel
import com.github.zly2006.zhihu.viewmodel.rememberZhihuApiEnvironment
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class FollowScreenData : ViewModel() {
    var selectedTabIndex by mutableIntStateOf(0)
}

const val FOLLOW_SCREEN_TAB_ROW_TAG = "follow_screen_tab_row"
const val FOLLOW_SCREEN_PAGER_TAG = "follow_screen_pager"
const val FOLLOWING_USERS_ROW_TAG = "following_users_row"
const val FOLLOW_RECOMMEND_LIST_TAG = "follow_recommend_list"
const val FOLLOW_RECOMMEND_REFRESH_BUTTON_TAG = "follow_recommend_refresh_button"
const val FOLLOW_DYNAMIC_LIST_TAG = "follow_dynamic_list"
const val FOLLOW_DYNAMIC_REFRESH_BUTTON_TAG = "follow_dynamic_refresh_button"

/** A preloaded inner page may own page-turn input only while the outer Follow page is visible. */
internal fun isFollowPageTurnTargetActive(
    followScreenActive: Boolean,
    selectedPage: Int,
    page: Int,
): Boolean = followScreenActive && selectedPage == page

/**
 * 关注顶层页的生产入口。
 *
 * 底部栏中的“关注”在主 pager 中对应推荐和动态两个子页，这个入口使用真实刷新/加载更多逻辑，并接收主壳传入的
 * [scrollToTopTrigger] 和 [innerPadding] 以配合 tab 重选回到顶部、系统栏和底部栏留白。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FollowScreen(
    scrollToTopTrigger: Int = 0,
    innerPadding: PaddingValues,
    parentPagerState: PagerState,
    isActive: Boolean = true,
) {
    val account by rememberAccountSettingsAccountState()
    if (!account.login) {
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            EmptyState(
                icon = AppIcons.Login,
                title = "登录后查看关注的人和话题的动态",
                action = EmptyStateAction("登录", AppIcons.Login, onClick = ::requestLoginNavigation),
            )
        }
        return
    }
    FollowScreenContent(
        scrollToTopTrigger = scrollToTopTrigger,
        innerPadding = innerPadding,
        parentPagerState = parentPagerState,
        isActive = isActive,
    )
}

/**
 * 关注页的实际布局实现。
 *
 * 页面内部用横向 pager 承载“推荐”和“动态”两个 tab，并把 tab 切换、列表刷新、加载更多和主壳的回到顶部触发集中在这里。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun FollowScreenContent(
    scrollToTopTrigger: Int = 0,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    parentPagerState: PagerState,
    isActive: Boolean,
) {
    val viewModel = viewModel { FollowScreenData() }
    val titles = listOf("推荐", "动态")
    val pagerState = rememberPagerState(
        initialPage = viewModel.selectedTabIndex,
        pageCount = { titles.size },
    )
    val coroutineScope = rememberCoroutineScope()
    val settleMotion = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()

    LaunchedEffect(pagerState.currentPage) {
        viewModel.selectedTabIndex = pagerState.currentPage
    }

    Column(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
        FollowTabRow(
            pagerState = pagerState,
            selectedTabIndex = pagerState.currentPage,
            onTabSelected = { index ->
                viewModel.selectedTabIndex = index
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index, animationSpec = settleMotion)
                }
            },
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                ),
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(
                    rememberNestedHorizontalPagerConnection(
                        parentState = parentPagerState,
                        childState = pagerState,
                    ),
                ).testTag(FOLLOW_SCREEN_PAGER_TAG),
            pageNestedScrollConnection = NoOpPagerNestedScrollConnection,
        ) { page ->
            when (page) {
                0 -> FollowRecommendScreen(
                    scrollToTopTrigger = scrollToTopTrigger,
                    isActive = isFollowPageTurnTargetActive(isActive, pagerState.currentPage, 0),
                )

                1 -> FollowDynamicScreen(
                    scrollToTopTrigger = scrollToTopTrigger,
                    isActive = isFollowPageTurnTargetActive(isActive, pagerState.currentPage, 1),
                )
            }
        }
    }
}

@Composable
private fun FollowTabRow(
    pagerState: PagerState? = null,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val titles = listOf("推荐", "动态")
    val tabs: @Composable () -> Unit = {
        titles.forEachIndexed { index, title ->
            Tab(
                modifier = Modifier.testTag("follow_screen_tab_$index"),
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                },
            )
        }
    }

    if (pagerState != null) {
        BoxWithConstraints(modifier) {
            val tabWidth = maxWidth / titles.size
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.testTag(FOLLOW_SCREEN_TAB_ROW_TAG),
                indicator = {
                    val page = (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                        .coerceIn(0f, (titles.size - 1).toFloat())
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier
                            .offset(x = tabWidth * page)
                            .width(tabWidth),
                    )
                },
            ) {
                tabs()
            }
        }
    } else {
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = modifier.testTag(FOLLOW_SCREEN_TAB_ROW_TAG),
        ) {
            tabs()
        }
    }
}

@Composable
fun FollowingUsersRow() {
    val navigator = LocalNavigator.current
    val viewModel: RecentMomentsViewModel = viewModel { RecentMomentsViewModel() }
    val environment = rememberZhihuApiEnvironment(allowGuestAccess = false)

    LaunchedEffect(Unit) {
        viewModel.load(environment)
    }

    when {
        viewModel.errorMessage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = viewModel.errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        viewModel.users.isNotEmpty() -> {
            LazyRow(
                modifier = Modifier.testTag(FOLLOWING_USERS_ROW_TAG),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(viewModel.users) { user ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .testTag("following_users_item_${user.actor.id}")
                            .clickable {
                                viewModel.markRead(environment, user.actor.id)
                                navigator.onNavigate(
                                    FollowUserUpdates(
                                        id = user.actor.id,
                                        urlToken = user.actor.urlToken,
                                        name = user.actor.name,
                                    ),
                                )
                            }.padding(vertical = 4.dp),
                    ) {
                        BadgedBox(
                            badge = {
                                if (user.unreadCount > 0) {
                                    Badge(modifier = Modifier.testTag("following_users_unread_${user.actor.id}"))
                                }
                            },
                        ) {
                            AsyncImage(
                                model = user.actor.avatarUrl,
                                contentDescription = user.actor.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = user.actor.name,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(64.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FollowRecommendScreen(
    scrollToTopTrigger: Int = 0,
    isActive: Boolean = true,
) {
    val settings = koinInject<SettingsStore>()
    val viewModel: FollowRecommendViewModel = viewModel { FollowRecommendViewModel(settings) }
    val readingQueueSourceId = "follow:recommend"
    if (isActive) {
        RegisterReadingQueueSource(
            sourceId = readingQueueSourceId,
            items = viewModel.displayItems,
        )
    }
    val environment = rememberZhihuApiEnvironment(allowGuestAccess = viewModel.allowGuestAccess)
    val userMessages = rememberUserMessageSink()
    val showRefreshFab = remember { settings.getBoolean("showRefreshFab", true) }
    val listState = rememberLazyListState()
    var cachedScrollToTopTrigger by remember { mutableIntStateOf(scrollToTopTrigger) }

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
                    viewModel.refresh(environment)
                }

                TopLevelReselectAction.ScrollToTop -> {
                    listState.animateScrollToItem(0)
                }

                null -> {}
            }
        }
        cachedScrollToTopTrigger = scrollToTopTrigger
    }

    LaunchedEffect(Unit) {
        if (viewModel.displayItems.isEmpty()) {
            viewModel.refresh(environment)
        }
    }

    var feedAuthorBlockRequest by remember { mutableStateOf<FeedAuthorBlockRequest?>(null) }
    val pageTurnTarget = rememberPageTurnTarget(
        listState = listState,
        enabled = isActive && feedAuthorBlockRequest == null,
    )

    Column {
        FeedPullToRefresh(viewModel, environment) {
            PaginatedList(
                items = viewModel.displayItems,
                listState = listState,
                modifier = Modifier
                    .pageTurnViewportWithGuide(pageTurnTarget)
                    .testTag(FOLLOW_RECOMMEND_LIST_TAG),
                topContent = {
                    item {
                        FollowingUsersRow()
                    }
                },
                onLoadMore = { viewModel.loadMore(environment) },
                isEnd = { viewModel.isEnd },
                footer = ProgressIndicatorFooter,
                loadFailed = viewModel.errorMessage != null,
                onRetry = { viewModel.retry(environment) },
                loadFailureMessage = viewModel.apiError?.message,
                loadFailureDescription = viewModel.errorMessage.takeIf { viewModel.apiError == null },
                onLogin = (::requestLoginNavigation).takeIf { viewModel.apiError?.needLogin == true },
                emptyContent = {
                    EmptyState(AppIcons.Group, "暂时没有推荐内容", Modifier.fillMaxWidth())
                },
                centerStatusWhenEmpty = true,
            ) { item ->
                FeedCard(
                    item = item,
                    readingQueueSourceId = readingQueueSourceId.takeIf { isActive },
                    modifier = Modifier.testTag("follow_recommend_item_${item.stableKey}"),
                    menuItems = { dismissMenu ->
                        DropdownMenuItem(
                            text = { Text("屏蔽用户") },
                            leadingIcon = { Icon(AppIcons.PersonOff, contentDescription = null) },
                            onClick = {
                                dismissMenu()
                                viewModel.handleBlockUser(environment, userMessages, item) { authorInfo ->
                                    feedAuthorBlockRequest = FeedAuthorBlockRequest(
                                        FeedAuthorBlockType.CONTENT_AUTHOR,
                                        authorInfo.first,
                                        authorInfo.second,
                                    )
                                }
                            },
                        )
                        val canBlockQuestionAuthor = when (item.feed?.target) {
                            is Feed.AnswerTarget, is Feed.QuestionTarget -> true
                            else -> item.raw is DataHolder.Answer || item.raw is DataHolder.Question
                        }
                        if (canBlockQuestionAuthor) {
                            DropdownMenuItem(
                                text = { Text("屏蔽提问者") },
                                leadingIcon = { Icon(AppIcons.PersonOff, contentDescription = null) },
                                onClick = {
                                    dismissMenu()
                                    viewModel.handleBlockQuestionAuthor(environment, userMessages, item) { authorInfo ->
                                        feedAuthorBlockRequest = FeedAuthorBlockRequest(
                                            FeedAuthorBlockType.QUESTION_AUTHOR,
                                            authorInfo.first,
                                            authorInfo.second,
                                        )
                                    }
                                },
                            )
                        }
                        val topics = when (val raw = item.raw) {
                            is DataHolder.Answer -> raw.question.topics
                            is DataHolder.Question -> raw.topics
                            is DataHolder.Article -> raw.topics ?: emptyList()
                            is DataHolder.Pin -> raw.topics ?: emptyList()
                            else -> emptyList()
                        }
                        topics.forEach { topic ->
                            DropdownMenuItem(
                                text = { Text("屏蔽「${topic.name}」") },
                                leadingIcon = { Icon(AppIcons.Tag, contentDescription = null) },
                                onClick = {
                                    dismissMenu()
                                    viewModel.handleBlockTopic(userMessages, topic.id, topic.name)
                                },
                            )
                        }
                    },
                )
            }

            if (showRefreshFab) {
                DraggableRefreshButton(
                    modifier = Modifier.testTag(FOLLOW_RECOMMEND_REFRESH_BUTTON_TAG),
                    onClick = {
                        viewModel.refresh(environment)
                    },
                ) {
                    if (viewModel.isLoading) {
                        AppLoadingIndicator(modifier = Modifier.size(36.dp))
                    } else {
                        Icon(AppIcons.Refresh, contentDescription = "刷新")
                    }
                }
            }
        }

        FeedAuthorBlockConfirmDialog(
            request = feedAuthorBlockRequest,
            displayItems = viewModel.displayItems,
            onDismiss = { feedAuthorBlockRequest = null },
            onConfirm = {
                viewModel.refresh(environment)
                feedAuthorBlockRequest = null
            },
        )
    }
}

@Composable
fun FollowDynamicScreen(
    scrollToTopTrigger: Int = 0,
    isActive: Boolean = true,
) {
    val settings = koinInject<SettingsStore>()
    val viewModel: FollowViewModel = viewModel { FollowViewModel(settings) }
    val readingQueueSourceId = "follow:dynamic"
    if (isActive) {
        RegisterReadingQueueSource(
            sourceId = readingQueueSourceId,
            items = viewModel.displayItems,
        )
    }
    val environment = rememberZhihuApiEnvironment(allowGuestAccess = viewModel.allowGuestAccess)
    val userMessages = rememberUserMessageSink()
    val showRefreshFab = remember { settings.getBoolean("showRefreshFab", true) }
    val listState = rememberLazyListState()
    var cachedScrollToTopTrigger by remember { mutableIntStateOf(scrollToTopTrigger) }

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
                    viewModel.refresh(environment)
                }

                TopLevelReselectAction.ScrollToTop -> {
                    listState.animateScrollToItem(0)
                }

                null -> {}
            }
        }
        cachedScrollToTopTrigger = scrollToTopTrigger
    }

    LaunchedEffect(Unit) {
        if (viewModel.displayItems.isEmpty()) {
            viewModel.refresh(environment)
        }
    }

    var feedAuthorBlockRequest by remember { mutableStateOf<FeedAuthorBlockRequest?>(null) }
    val pageTurnTarget = rememberPageTurnTarget(
        listState = listState,
        enabled = isActive && feedAuthorBlockRequest == null,
    )

    Column {
        FeedPullToRefresh(viewModel, environment) {
            PaginatedList(
                items = viewModel.displayItems,
                listState = listState,
                modifier = Modifier
                    .pageTurnViewportWithGuide(pageTurnTarget)
                    .testTag(FOLLOW_DYNAMIC_LIST_TAG),
                onLoadMore = { viewModel.loadMore(environment) },
                topContent = {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                },
                isEnd = { viewModel.isEnd },
                footer = ProgressIndicatorFooter,
                loadFailed = viewModel.errorMessage != null,
                onRetry = { viewModel.retry(environment) },
                loadFailureMessage = viewModel.apiError?.message,
                loadFailureDescription = viewModel.errorMessage.takeIf { viewModel.apiError == null },
                onLogin = (::requestLoginNavigation).takeIf { viewModel.apiError?.needLogin == true },
                emptyContent = {
                    EmptyState(AppIcons.Group, "关注的人还没有新动态", Modifier.fillMaxWidth())
                },
                centerStatusWhenEmpty = true,
            ) { item ->
                FeedCard(
                    item = item,
                    readingQueueSourceId = readingQueueSourceId.takeIf { isActive },
                    modifier = Modifier.testTag("follow_dynamic_item_${item.stableKey}"),
                    showSourceLabel = true,
                    menuItems = { dismissMenu ->
                        DropdownMenuItem(
                            text = { Text("屏蔽用户") },
                            leadingIcon = { Icon(AppIcons.PersonOff, contentDescription = null) },
                            onClick = {
                                dismissMenu()
                                viewModel.handleBlockUser(environment, userMessages, item) { authorInfo ->
                                    feedAuthorBlockRequest = FeedAuthorBlockRequest(
                                        FeedAuthorBlockType.CONTENT_AUTHOR,
                                        authorInfo.first,
                                        authorInfo.second,
                                    )
                                }
                            },
                        )
                        val canBlockQuestionAuthor = when (item.feed?.target) {
                            is Feed.AnswerTarget, is Feed.QuestionTarget -> true
                            else -> item.raw is DataHolder.Answer || item.raw is DataHolder.Question
                        }
                        if (canBlockQuestionAuthor) {
                            DropdownMenuItem(
                                text = { Text("屏蔽提问者") },
                                leadingIcon = { Icon(AppIcons.PersonOff, contentDescription = null) },
                                onClick = {
                                    dismissMenu()
                                    viewModel.handleBlockQuestionAuthor(environment, userMessages, item) { authorInfo ->
                                        feedAuthorBlockRequest = FeedAuthorBlockRequest(
                                            FeedAuthorBlockType.QUESTION_AUTHOR,
                                            authorInfo.first,
                                            authorInfo.second,
                                        )
                                    }
                                },
                            )
                        }
                        val topics = when (val raw = item.raw) {
                            is DataHolder.Answer -> raw.question.topics
                            is DataHolder.Question -> raw.topics
                            is DataHolder.Article -> raw.topics ?: emptyList()
                            is DataHolder.Pin -> raw.topics ?: emptyList()
                            else -> emptyList()
                        }
                        topics.forEach { topic ->
                            DropdownMenuItem(
                                text = { Text("屏蔽「${topic.name}」") },
                                leadingIcon = { Icon(AppIcons.Tag, contentDescription = null) },
                                onClick = {
                                    dismissMenu()
                                    viewModel.handleBlockTopic(userMessages, topic.id, topic.name)
                                },
                            )
                        }
                    },
                )
            }

            if (showRefreshFab) {
                DraggableRefreshButton(
                    modifier = Modifier.testTag(FOLLOW_DYNAMIC_REFRESH_BUTTON_TAG),
                    onClick = {
                        viewModel.refresh(environment)
                    },
                ) {
                    if (viewModel.isLoading) {
                        AppLoadingIndicator(modifier = Modifier.size(36.dp))
                    } else {
                        Icon(AppIcons.Refresh, contentDescription = "刷新")
                    }
                }
            }
        }

        FeedAuthorBlockConfirmDialog(
            request = feedAuthorBlockRequest,
            displayItems = viewModel.displayItems,
            onDismiss = { feedAuthorBlockRequest = null },
            onConfirm = {
                viewModel.refresh(environment)
                feedAuthorBlockRequest = null
            },
        )
    }
}
