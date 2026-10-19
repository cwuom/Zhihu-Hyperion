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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.github.zly2006.zhihu.data.DataHolder
import com.github.zly2006.zhihu.data.Feed
import com.github.zly2006.zhihu.data.MOBILE_NOTIFICATION_MESSAGE_URL
import com.github.zly2006.zhihu.data.MobileNotificationMessageOverview
import com.github.zly2006.zhihu.data.QUALITY_FILTER_MODE_PREFERENCE_KEY
import com.github.zly2006.zhihu.data.QualityFilterMode
import com.github.zly2006.zhihu.data.RecommendationMode
import com.github.zly2006.zhihu.data.target
import com.github.zly2006.zhihu.filter.RemoteHistorySync
import com.github.zly2006.zhihu.icons.AppIcons
import com.github.zly2006.zhihu.icons.Icon
import com.github.zly2006.zhihu.navigation.Account
import com.github.zly2006.zhihu.navigation.Article
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.Notification
import com.github.zly2006.zhihu.navigation.Pin
import com.github.zly2006.zhihu.navigation.Search
import com.github.zly2006.zhihu.navigation.requestLoginNavigation
import com.github.zly2006.zhihu.notification.NotificationSettingsStore
import com.github.zly2006.zhihu.platform.SettingsStore
import com.github.zly2006.zhihu.platform.UserMessageDuration
import com.github.zly2006.zhihu.platform.rememberAppPrivateDirectory
import com.github.zly2006.zhihu.platform.rememberPlainTextClipboard
import com.github.zly2006.zhihu.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.reading.RegisterReadingQueueSource
import com.github.zly2006.zhihu.ui.components.AnnouncementCard
import com.github.zly2006.zhihu.ui.components.AnnouncementCardDefaults
import com.github.zly2006.zhihu.ui.components.AppLoadingIndicator
import com.github.zly2006.zhihu.ui.components.BlockByKeywordsDialog
import com.github.zly2006.zhihu.ui.components.DEFAULT_FAB_OPACITY
import com.github.zly2006.zhihu.ui.components.DraggableRefreshButton
import com.github.zly2006.zhihu.ui.components.EmptyState
import com.github.zly2006.zhihu.ui.components.EmptyStateAction
import com.github.zly2006.zhihu.ui.components.FabMenu
import com.github.zly2006.zhihu.ui.components.FeedAuthorBlockConfirmDialog
import com.github.zly2006.zhihu.ui.components.FeedAuthorBlockRequest
import com.github.zly2006.zhihu.ui.components.FeedAuthorBlockType
import com.github.zly2006.zhihu.ui.components.FeedCard
import com.github.zly2006.zhihu.ui.components.MyModalBottomSheet
import com.github.zly2006.zhihu.ui.components.PREF_FAB_OPACITY
import com.github.zly2006.zhihu.ui.components.PaginatedList
import com.github.zly2006.zhihu.ui.components.ProgressIndicatorFooter
import com.github.zly2006.zhihu.ui.components.feedKeywordExtractionAvailable
import com.github.zly2006.zhihu.ui.components.pageTurnViewportWithGuide
import com.github.zly2006.zhihu.ui.components.rememberPageTurnTarget
import com.github.zly2006.zhihu.ui.subscreens.BOTTOM_BAR_SINGLE_TAP_REFRESH_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.subscreens.CreationEntryPosition
import com.github.zly2006.zhihu.update.UpdateController
import com.github.zly2006.zhihu.update.UpdateState
import com.github.zly2006.zhihu.util.json
import com.github.zly2006.zhihu.viewmodel.MobileClientProvider
import com.github.zly2006.zhihu.viewmodel.feed.AUTO_REFRESH_HOME_ON_STARTUP_PREFERENCE_KEY
import com.github.zly2006.zhihu.viewmodel.feed.BaseFeedViewModel
import com.github.zly2006.zhihu.viewmodel.feed.HomeFeedInteractionViewModel
import com.github.zly2006.zhihu.viewmodel.feed.HomeFeedViewModel
import com.github.zly2006.zhihu.viewmodel.filter.HomeFeedFilter
import com.github.zly2006.zhihu.viewmodel.local.LocalHomeFeedViewModel
import com.github.zly2006.zhihu.viewmodel.rememberZhihuApiEnvironment
import com.github.zly2006.zhihu.viewmodel.za.AndroidHomeFeedViewModel
import com.github.zly2006.zhihu.viewmodel.za.MixedHomeFeedViewModel
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatform

const val HOME_TOP_ACTIONS_TAG = "home_top_actions"
const val HOME_SEARCH_BUTTON_TAG = "home_search_button"
const val HOME_CREATE_FAB_TAG = "home_create_fab"
const val HOME_CREATE_MENU_TAG = "home_create_menu"
const val HOME_WRITE_QUESTION_BUTTON_TAG = "home_write_question_button"
const val HOME_WRITE_ANSWER_BUTTON_TAG = "home_write_answer_button"
const val HOME_WRITE_PIN_BUTTON_TAG = "home_write_pin_button"
const val HOME_NOTIFICATION_BUTTON_TAG = "home_notification_button"
const val HOME_NOTIFICATION_BUTTON_CONTENT_TAG = "home_notification_button_content"
const val HOME_NOTIFICATION_BADGE_TAG = "home_notification_badge"
const val HOME_ACCOUNT_BUTTON_TAG = "home_account_button"
const val HOME_FEED_LIST_TAG = "home_feed_list"
const val HOME_REFRESH_BUTTON_TAG = "home_refresh_button"

// Pager can dispose this page while its feed ViewModels survive. Keep the header rows alive too,
// so restoring a saved list index does not shift the visible card.
private class HomeScreenState : ViewModel() {
    val dismissedUpdateVersionCode = mutableStateOf<Int?>(null)
}

/**
 * 首页信息流页面。
 *
 * 页面顶部承载搜索、通知、账号入口等高频操作，主体是可分页的推荐信息流，底部可按设置显示可拖动刷新 FAB。
 * 设计上首页同时响应推荐算法、Duo3 账号入口迁移、更新公告和未读通知等状态，因此 UI 改动时要同时检查
 * `recommendationMode`、`duo3_home_account`、`showRefreshFab` 和账号面板相关路径。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    scrollToTopTrigger: Int,
    innerPadding: PaddingValues,
    showTopActions: Boolean = true,
    isActive: Boolean = true,
    creationEntryPosition: CreationEntryPosition = CreationEntryPosition.Floating,
) {
    val readingPlayerOverlayPadding = LocalReadingPlayerOverlayPadding.current
    val navigator = LocalNavigator.current
    val paginationEnvironment = rememberZhihuApiEnvironment(allowGuestAccess = true)
    val settings = koinInject<SettingsStore>()
    val homeState: HomeScreenState = viewModel { HomeScreenState() }
    val remoteHistory = koinInject<RemoteHistorySync>()
    val homeFeedFilter = koinInject<HomeFeedFilter>()
    val mobileClient = koinInject<MobileClientProvider>()
    val clipboard = rememberPlainTextClipboard()
    val appPrivateDirectory = rememberAppPrivateDirectory()
    val notificationSettings = koinInject<NotificationSettingsStore>()
    val userMessages = rememberUserMessageSink()
    val lifecycleOwner = LocalLifecycleOwner.current

    val duo3HomeAccount = settings.getBoolean("duo3_home_account", false)
    val showRefreshFab = settings.getBoolean("showRefreshFab", true)
    val autoRefreshOnStartup = settings.getBoolean(AUTO_REFRESH_HOME_ON_STARTUP_PREFERENCE_KEY, true)
    val showUnreadBadge = notificationSettings.getUnreadBadgeEnabled()
    var showAccountBottomSheet by remember { mutableStateOf(false) }
    var showCreateMenu by remember { mutableStateOf(false) }
    // 点底栏切到别的页面时收起创作菜单，回来不会看到残留的展开态。
    LaunchedEffect(isActive, creationEntryPosition) { showCreateMenu = false }
    val createMenuBlurRadius by animateDpAsState(
        targetValue = if (showCreateMenu) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "createMenuBlurRadius",
    )

    // 获取当前推荐算法设置
    val currentRecommendationMode =
        RecommendationMode.entries.find {
            it.key == settings.getString("recommendationMode", RecommendationMode.MIXED.key)
        } ?: RecommendationMode.MIXED
    val startupCacheFile = remember(appPrivateDirectory, currentRecommendationMode) {
        Path(appPrivateDirectory, homeFeedStartupCacheFileName(currentRecommendationMode))
    }

    val account = rememberAccountSettingsAccountState().value
    if (account.login && !account.hasRequiredCookie) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Icon(AppIcons.Cookie, contentDescription = null) },
            title = { Text("Cookie 不完整") },
            text = { Text("当前登录信息缺少必要的 Cookie d_c0，请重新登录。") },
            confirmButton = {
                TextButton(onClick = ::requestLoginNavigation) {
                    Text("重新登录")
                }
            },
        )
    }
    val updateState by koinInject<UpdateController>().state.collectAsState()
    val updateAnnouncement = (updateState as? UpdateState.Available)?.update
    val isDebuggable = rememberHomeIsDebuggable()
    val viewModel: BaseFeedViewModel = when (currentRecommendationMode) {
        RecommendationMode.WEB -> viewModel { HomeFeedViewModel(settings, homeFeedFilter) }
        RecommendationMode.ANDROID -> viewModel { AndroidHomeFeedViewModel(settings, homeFeedFilter, mobileClient) }
        RecommendationMode.LOCAL -> viewModel { LocalHomeFeedViewModel(settings, KoinPlatform.getKoin().inject()) }
        RecommendationMode.MIXED -> viewModel { MixedHomeFeedViewModel(settings, homeFeedFilter, mobileClient) }
    }
    val localHomeViewModel = viewModel as? LocalHomeFeedViewModel
    if (localHomeViewModel?.showDatabaseError == true) {
        AlertDialog(
            onDismissRequest = { localHomeViewModel.showDatabaseError = false },
            icon = { Icon(AppIcons.Database, contentDescription = null) },
            title = { Text("数据库错误") },
            text = { Text("本地推荐系统的数据库未正确初始化。请尝试重启应用或清除应用数据。") },
            confirmButton = {
                TextButton(onClick = { localHomeViewModel.showDatabaseError = false }) {
                    Text("确定")
                }
            },
        )
    }
    val readingQueueSourceId = "home:${currentRecommendationMode.name}"
    RegisterReadingQueueSource(
        sourceId = readingQueueSourceId,
        items = viewModel.displayItems,
    )

    var dismissedUpdateVersionCode by homeState.dismissedUpdateVersionCode

    val listState = rememberLazyListState()
    var cachedScrollToTopTrigger by remember { mutableIntStateOf(scrollToTopTrigger) }
    LaunchedEffect(scrollToTopTrigger, isActive) {
        if (!isActive) {
            cachedScrollToTopTrigger = scrollToTopTrigger
            return@LaunchedEffect
        }
        when (
            topLevelReselectAction(
                triggerDelta = scrollToTopTrigger - cachedScrollToTopTrigger,
                isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0,
                refreshOnSingleTap = settings.getBoolean(BOTTOM_BAR_SINGLE_TAP_REFRESH_PREFERENCE_KEY, false),
            )
        ) {
            TopLevelReselectAction.Refresh -> {
                listState.requestScrollToItem(0)
                viewModel.refresh(paginationEnvironment)
            }

            TopLevelReselectAction.ScrollToTop -> {
                listState.animateScrollToItem(0)
            }

            null -> {}
        }
        cachedScrollToTopTrigger = scrollToTopTrigger
    }

    var unreadCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(account.login) {
        if (!account.login) {
            unreadCount = 0
            return@LaunchedEffect
        }
        try {
            unreadCount = mobileClient
                .withClient { client ->
                    client.get("$MOBILE_NOTIFICATION_MESSAGE_URL?limit=20").json<MobileNotificationMessageOverview>()
                }.totalUnreadCount
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // 忽略错误
        }
    }

    val latestLoadedDisplayItems = viewModel.latestLoadedDisplayItems.value
    LaunchedEffect(latestLoadedDisplayItems) {
        if (latestLoadedDisplayItems.isNotEmpty()) {
            encodeHomeFeedStartupSnapshot(latestLoadedDisplayItems)?.let { serialized ->
                withContext(Dispatchers.Default) {
                    runCatching {
                        SystemFileSystem.sink(startupCacheFile).buffered().use { it.writeString(serialized) }
                    }
                }
            }
        }
    }

    val completedPageCount = viewModel.completedPageCount
    LaunchedEffect(completedPageCount) {
        if (completedPageCount > 0 &&
            currentRecommendationMode == RecommendationMode.WEB &&
            settings.getString(QUALITY_FILTER_MODE_PREFERENCE_KEY, QualityFilterMode.RULES.name) == QualityFilterMode.HIDE.name &&
            viewModel.displayItems.isEmpty() &&
            !viewModel.isEnd
        ) {
            viewModel.loadMore(paginationEnvironment)
        }
    }

    LaunchedEffect(lifecycleOwner, currentRecommendationMode, account.login, account.id, autoRefreshOnStartup, isActive) {
        if (!isActive) return@LaunchedEffect
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (account.login &&
                account.hasRequiredCookie &&
                settings.getBoolean("enableContentFilter", true) &&
                !settings.getBoolean("reverseBlock", false)
            ) {
                remoteHistory.start(homeState.viewModelScope, paginationEnvironment)
            }
            if (!account.login && settings.getBoolean("loginForRecommendation", true)) {
                requestLoginNavigation()
            } else if (viewModel.displayItems.isEmpty()) {
                val cachedItems = if (autoRefreshOnStartup) {
                    emptyList()
                } else {
                    withContext(Dispatchers.Default) {
                        runCatching {
                            if (SystemFileSystem.exists(startupCacheFile)) {
                                SystemFileSystem.source(startupCacheFile).buffered().use { source ->
                                    decodeHomeFeedStartupSnapshot(source.readString())
                                }
                            } else {
                                emptyList()
                            }
                        }.getOrDefault(emptyList())
                    }
                }
                if (cachedItems.isNotEmpty()) {
                    remoteHistory.awaitRecentPage()
                    val unreadCachedItems = remoteHistory.filterRemoteReads(cachedItems)
                    if (unreadCachedItems.isEmpty()) {
                        viewModel.refresh(paginationEnvironment)
                    } else {
                        viewModel.addDisplayItems(unreadCachedItems)
                    }
                } else {
                    viewModel.refresh(paginationEnvironment)
                }
            }
        }
    }

    val historyRevision by remoteHistory.importedRevision.collectAsState()
    LaunchedEffect(remoteHistory, viewModel, historyRevision, isActive) {
        if (!isActive) return@LaunchedEffect
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.isNotEmpty() }.first { it }
        snapshotFlow { viewModel.displayItems.toList() }.collectLatest { items ->
            // Keep cards already reached by the user stable; only prune the unseen tail in place.
            val visibleKeys = listState.layoutInfo.visibleItemsInfo
                .map { it.key }
                .toSet()
            val lastVisible = items.indexOfLast { it.stableKey in visibleKeys }
            val candidates = items.drop(lastVisible + 1)
            val keptKeys = remoteHistory.filterRemoteReads(candidates).map { it.stableKey }.toSet()
            val removedKeys = candidates.filterNot { it.stableKey in keptKeys }.map { it.stableKey }.toSet()
            // The user may have scrolled while the database query was suspended.
            val currentVisibleKeys = listState.layoutInfo.visibleItemsInfo
                .map { it.key }
                .toSet()
            val protectedEnd = viewModel.displayItems.indexOfLast { it.stableKey in currentVisibleKeys }
            for (index in viewModel.displayItems.lastIndex downTo protectedEnd + 1) {
                if (viewModel.displayItems[index].stableKey in removedKeys) viewModel.displayItems.removeAt(index)
            }
            viewModel.latestLoadedDisplayItems.value = remoteHistory.filterRemoteReads(viewModel.latestLoadedDisplayItems.value)
        }
    }

    // 显示错误信息
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            userMessages.showMessage(it, UserMessageDuration.Long)
        }
    }

    var feedAuthorBlockRequest by remember { mutableStateOf<FeedAuthorBlockRequest?>(null) }

    // 按关键词屏蔽对话框
    var showBlockByKeywordsDialog by remember { mutableStateOf(false) }
    var feedToBlockByKeywords by remember { mutableStateOf<Pair<String, String?>?>(null) } // 二元组内容为标题和摘要。
    val pageTurnTarget = rememberPageTurnTarget(
        listState = listState,
        enabled = isActive &&
            !showAccountBottomSheet &&
            !showCreateMenu &&
            feedAuthorBlockRequest == null &&
            !showBlockByKeywordsDialog &&
            (!account.login || account.hasRequiredCookie),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = if (duo3HomeAccount) {
                Modifier
                    .fillMaxSize()
                    .blur(createMenuBlurRadius)
            } else {
                // master旧版需要pad掉状态栏
                Modifier
                    .fillMaxSize()
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .blur(createMenuBlurRadius)
            },
            topBar = {
                if (!showTopActions && creationEntryPosition != CreationEntryPosition.Top) {
                    return@Scaffold
                }
                if (duo3HomeAccount) {
                    Box {
                        Surface(
                            modifier = Modifier
                                .height(
                                    WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp + 32.dp,
                                ).fillMaxWidth(),
                        ) { }
                        Row(
                            modifier = Modifier
                                .testTag(HOME_TOP_ACTIONS_TAG)
                                .fillMaxWidth()
                                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                                .padding(16.dp, 8.dp, 16.dp, 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .testTag(HOME_SEARCH_BUTTON_TAG),
                                shape = RoundedCornerShape(32.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                onClick = {
                                    navigator.onNavigate(
                                        Search(query = ""),
                                    )
                                },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(start = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        AppIcons.Search,
                                        contentDescription = "搜索",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "搜索",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f),
                                    )

                                    IconButton(
                                        onClick = { showAccountBottomSheet = true },
                                        modifier = Modifier
                                            .size(64.dp)
                                            .testTag(HOME_ACCOUNT_BUTTON_TAG),
                                    ) {
                                        Box(Modifier.padding(12.dp)) {
                                            BadgedBox(
                                                badge = {
                                                    if (showUnreadBadge && unreadCount > 0) {
                                                        Badge { }
                                                    }
                                                },
                                            ) {
                                                val avatarUrl = account.avatarUrl
                                                if (avatarUrl != null) {
                                                    AsyncImage(
                                                        model = avatarUrl,
                                                        contentDescription = "账号",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .border(
                                                                0.5.dp,
                                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                                                CircleShape,
                                                            ).clip(CircleShape),
                                                    )
                                                } else {
                                                    Icon(
                                                        AppIcons.AccountCircle,
                                                        contentDescription = "账号",
                                                        tint = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(40.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (creationEntryPosition == CreationEntryPosition.Top) {
                                Spacer(modifier = Modifier.width(8.dp))
                                HomeNotificationButton(showUnreadBadge, unreadCount) { navigator.onNavigate(Notification) }
                                CreationTopBarButton(isActive)
                            }
                        }
                    }
                } else {
                    Surface(shadowElevation = 4.dp) {
                        Row(
                            modifier = Modifier
                                .testTag(HOME_TOP_ACTIONS_TAG)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag(HOME_SEARCH_BUTTON_TAG),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                onClick = {
                                    navigator.onNavigate(
                                        Search(query = ""),
                                    )
                                },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        AppIcons.Search,
                                        contentDescription = "搜索",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "搜索内容",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            HomeNotificationButton(showUnreadBadge, unreadCount) { navigator.onNavigate(Notification) }
                            if (creationEntryPosition == CreationEntryPosition.Top) {
                                CreationTopBarButton(isActive)
                            }
                        }
                    }
                }
            },
        ) { scaffoldPadding ->
            if (duo3HomeAccount && showAccountBottomSheet) {
                MyModalBottomSheet(
                    onDismissRequest = { showAccountBottomSheet = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    AccountSettingScreen(
                        innerPadding = PaddingValues(0.dp),
                        unreadCount = unreadCount,
                        showUnreadBadge = showUnreadBadge,
                        onDismissRequest = { showAccountBottomSheet = false },
                    )
                }
            }

            FeedPullToRefresh(viewModel, PaddingValues(top = scaffoldPadding.calculateTopPadding())) {
                PaginatedList(
                    items = viewModel.displayItems,
                    listState = listState,
                    modifier = Modifier
                        .pageTurnViewportWithGuide(pageTurnTarget)
                        .testTag(HOME_FEED_LIST_TAG),
                    contentPadding = PaddingValues(
                        top = scaffoldPadding.calculateTopPadding() + 8.dp,
                        bottom = innerPadding.calculateBottomPadding() + readingPlayerOverlayPadding,
                    ),
                    onLoadMore = { viewModel.loadMore(paginationEnvironment) },
                    footer = { listState ->
                        val errorMessage = viewModel.errorMessage
                        if (errorMessage != null && !viewModel.isLoading && viewModel.displayItems.isEmpty()) {
                            EmptyState(
                                icon = AppIcons.Error,
                                title = "加载失败",
                                description = errorMessage,
                                modifier = Modifier.fillMaxWidth(),
                                action = EmptyStateAction("重试", AppIcons.Refresh) { viewModel.refresh(paginationEnvironment) },
                            )
                        } else {
                            ProgressIndicatorFooter(listState)
                        }
                    },
                    key = { item -> item.stableKey },
                    topContent = {
                        item {
                            val availableUpdate = updateAnnouncement

                            AnnouncementCard(
                                visible = availableUpdate != null && dismissedUpdateVersionCode != availableUpdate.versionCode,
                                title = "发现新版本：${availableUpdate?.displayVersion}",
                                leadingIcon = { Icon(AppIcons.ArrowCircleUp, contentDescription = null) },
                                accept = { Text("查看更新") },
                                onAccept = {
                                    navigator.onNavigate(Account.SystemAndUpdateSettings())
                                },
                                dismiss = { Text("以后") },
                                onDismiss = {
                                    dismissedUpdateVersionCode = availableUpdate?.versionCode
                                },
                                colors = AnnouncementCardDefaults.colorsImportant(),
                            )
                        }
                    },
                ) { item ->
                    FeedCard(
                        item,
                        readingQueueSourceId = readingQueueSourceId,
                        thumbnailUrl = when (val target = item.feed?.target) {
                            is Feed.AnswerTarget -> target.thumbnail
                            else -> null
                        },
                        menuItems = { dismissMenu ->
                            if (feedKeywordExtractionAvailable) {
                                DropdownMenuItem(
                                    text = { Text("按关键词屏蔽") },
                                    leadingIcon = { Icon(AppIcons.MatchWord, contentDescription = null) },
                                    onClick = {
                                        dismissMenu()
                                        viewModel.handleBlockByKeywords(paginationEnvironment, userMessages, item) { (_, contentInfo) ->
                                            feedToBlockByKeywords = contentInfo.first to contentInfo.second
                                            showBlockByKeywordsDialog = true
                                        }
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("屏蔽用户") },
                                leadingIcon = { Icon(AppIcons.PersonOff, contentDescription = null) },
                                onClick = {
                                    dismissMenu()
                                    viewModel.handleBlockUser(paginationEnvironment, userMessages, item) { authorInfo ->
                                        feedAuthorBlockRequest = FeedAuthorBlockRequest(
                                            type = FeedAuthorBlockType.CONTENT_AUTHOR,
                                            userId = authorInfo.first,
                                            userName = authorInfo.second,
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
                                        viewModel.handleBlockQuestionAuthor(paginationEnvironment, userMessages, item) { authorInfo ->
                                            feedAuthorBlockRequest = FeedAuthorBlockRequest(
                                                type = FeedAuthorBlockType.QUESTION_AUTHOR,
                                                userId = authorInfo.first,
                                                userName = authorInfo.second,
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
                    ) { clickedItem, destination ->
                        val feed = clickedItem.feed
                        if (feed != null) {
//                            DataHolder.putFeed(feed)
                            (viewModel as? HomeFeedInteractionViewModel)
                                ?.onUiContentClick(paginationEnvironment, feed, clickedItem)
                        } else {
                            localHomeViewModel?.onLocalItemOpened(clickedItem)
                        }
                        if (destination != null) {
                            navigator.onNavigate(destination)
                        }
                    }
                }

                // 创作菜单展开时隐藏可拖动按钮；收起时让出 48dp 创作按钮和间距。
                if (showRefreshFab && !showCreateMenu) {
                    val fabAvoidance = readingPlayerOverlayPadding + if (creationEntryPosition == CreationEntryPosition.Floating) 64.dp else 0.dp
                    if (isDebuggable) {
                        DraggableRefreshButton(
                            onClick = {
                                val data = Json.encodeToString(viewModel.debugData)
                                clipboard("data", data)
                                userMessages.showShortMessage("已复制调试数据")
                            },
                            preferenceName = "copyAll",
                            bottomAvoidance = fabAvoidance,
                            initiallyOnLeft = true,
                        ) {
                            Icon(AppIcons.CopyAll, contentDescription = "复制")
                        }
                    }
                    DraggableRefreshButton(
                        modifier = Modifier.testTag(HOME_REFRESH_BUTTON_TAG),
                        bottomAvoidance = fabAvoidance,
                        onClick = { viewModel.refresh(paginationEnvironment) },
                    ) {
                        if (viewModel.isLoading) {
                            AppLoadingIndicator(modifier = Modifier.size(30.dp))
                        } else {
                            Icon(AppIcons.Refresh, contentDescription = "刷新")
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showCreateMenu,
            enter = fadeIn(animationSpec = tween(durationMillis = 120)),
            exit = fadeOut(animationSpec = tween(durationMillis = 120)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.16f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        showCreateMenu = false
                    },
            )
        }

        if (creationEntryPosition == CreationEntryPosition.Floating) {
            FabMenu(
                expanded = showCreateMenu,
                onExpandedChange = { showCreateMenu = it },
                icon = AppIcons.Add,
                contentDescription = "创作",
                actions = creationMenuActions(navigator.onNavigate),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = innerPadding.calculateBottomPadding() + readingPlayerOverlayPadding)
                    .testTag(HOME_CREATE_MENU_TAG),
                buttonModifier = Modifier.testTag(HOME_CREATE_FAB_TAG),
                buttonAlpha = remember(settings) {
                    settings.getInt(PREF_FAB_OPACITY, DEFAULT_FAB_OPACITY).coerceIn(10, 100) / 100f
                },
            )
        }
    }

    FeedAuthorBlockConfirmDialog(
        request = feedAuthorBlockRequest,
        displayItems = viewModel.displayItems,
        onDismiss = { feedAuthorBlockRequest = null },
        onConfirm = {
            viewModel.refresh(paginationEnvironment)
            feedAuthorBlockRequest = null
        },
    )

    // 按关键词屏蔽对话框
    feedToBlockByKeywords?.let { (title, excerpt) ->
        BlockByKeywordsDialog(
            showDialog = showBlockByKeywordsDialog,
            feedTitle = title,
            feedExcerpt = excerpt,
            onDismiss = {
                showBlockByKeywordsDialog = false
                feedToBlockByKeywords = null
            },
            onConfirm = {
                viewModel.refresh(paginationEnvironment)
                showBlockByKeywordsDialog = false
                feedToBlockByKeywords = null
            },
        )
    }
}

@Composable
private fun HomeNotificationButton(showUnreadBadge: Boolean, unreadCount: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .testTag(HOME_NOTIFICATION_BUTTON_TAG)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // 点击盒与内容盒分离，避免 IconButton 的裁剪边界截掉 BadgedBox 的 badge。
        Box(
            modifier = Modifier
                .size(40.dp)
                .testTag(HOME_NOTIFICATION_BUTTON_CONTENT_TAG),
            contentAlignment = Alignment.Center,
        ) {
            BadgedBox(
                badge = {
                    if (showUnreadBadge && unreadCount > 0) {
                        Badge(modifier = Modifier.testTag(HOME_NOTIFICATION_BADGE_TAG)) {
                            Text("$unreadCount")
                        }
                    }
                },
            ) {
                Icon(
                    AppIcons.Notifications,
                    contentDescription = "通知",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
