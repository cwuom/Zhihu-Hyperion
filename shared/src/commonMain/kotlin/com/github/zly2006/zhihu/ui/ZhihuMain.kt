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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.github.zly2006.zhihu.account.LoginScreen
import com.github.zly2006.zhihu.filter.ContentOpenFrom
import com.github.zly2006.zhihu.glass.LIQUID_GLASS_PREFERENCE_KEY
import com.github.zly2006.zhihu.glass.LiquidGlassTab
import com.github.zly2006.zhihu.glass.LiquidGlassTabBar
import com.github.zly2006.zhihu.glass.glassBackdropSource
import com.github.zly2006.zhihu.glass.isLiquidGlassEnabledByDefault
import com.github.zly2006.zhihu.glass.isLiquidGlassSupported
import com.github.zly2006.zhihu.glass.rememberGlassBackdrop
import com.github.zly2006.zhihu.icons.AppIcon
import com.github.zly2006.zhihu.icons.AppIcons
import com.github.zly2006.zhihu.icons.Icon
import com.github.zly2006.zhihu.markdown.LocalSegmentCommentSheet
import com.github.zly2006.zhihu.navigation.Account
import com.github.zly2006.zhihu.navigation.Article
import com.github.zly2006.zhihu.navigation.ArticleType
import com.github.zly2006.zhihu.navigation.ArticleTypeNavType
import com.github.zly2006.zhihu.navigation.CollectionContent
import com.github.zly2006.zhihu.navigation.Collections
import com.github.zly2006.zhihu.navigation.Daily
import com.github.zly2006.zhihu.navigation.EmptyDetail
import com.github.zly2006.zhihu.navigation.Follow
import com.github.zly2006.zhihu.navigation.History
import com.github.zly2006.zhihu.navigation.Home
import com.github.zly2006.zhihu.navigation.HotList
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.Login
import com.github.zly2006.zhihu.navigation.MainTabs
import com.github.zly2006.zhihu.navigation.MyCollections
import com.github.zly2006.zhihu.navigation.NavDestination
import com.github.zly2006.zhihu.navigation.Navigator
import com.github.zly2006.zhihu.navigation.Notification
import com.github.zly2006.zhihu.navigation.OnlineHistory
import com.github.zly2006.zhihu.navigation.Person
import com.github.zly2006.zhihu.navigation.Pin
import com.github.zly2006.zhihu.navigation.Question
import com.github.zly2006.zhihu.navigation.Search
import com.github.zly2006.zhihu.navigation.SegmentCommentHolder
import com.github.zly2006.zhihu.navigation.SentenceSimilarityTest
import com.github.zly2006.zhihu.navigation.TopLevelDestination
import com.github.zly2006.zhihu.navigation.Topic
import com.github.zly2006.zhihu.navigation.Video
import com.github.zly2006.zhihu.navigation.WriteAnswer
import com.github.zly2006.zhihu.navigation.WritePin
import com.github.zly2006.zhihu.navigation.loginNavigationRequestFlow
import com.github.zly2006.zhihu.platform.PlatformBackHandler
import com.github.zly2006.zhihu.platform.SettingsStore
import com.github.zly2006.zhihu.platform.isTopLevelBackToLauncherSupported
import com.github.zly2006.zhihu.platform.platformName
import com.github.zly2006.zhihu.reading.CompactReadingPlayerButton
import com.github.zly2006.zhihu.reading.ReadingPlayerBar
import com.github.zly2006.zhihu.reading.ReadingQueueSheet
import com.github.zly2006.zhihu.reading.rememberReadingPlayerController
import com.github.zly2006.zhihu.reading.saveReadingPlaybackSpeed
import com.github.zly2006.zhihu.ui.components.AppDialogHost
import com.github.zly2006.zhihu.ui.components.LocalSelectedContentDestination
import com.github.zly2006.zhihu.ui.components.NoOpPagerNestedScrollConnection
import com.github.zly2006.zhihu.ui.components.rememberObservedSetting
import com.github.zly2006.zhihu.ui.subscreens.AppearanceSettingsScreen
import com.github.zly2006.zhihu.ui.subscreens.BlockedFeedHistoryScreen
import com.github.zly2006.zhihu.ui.subscreens.CREATION_ENTRY_POSITION_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.subscreens.ColorSchemeScreen
import com.github.zly2006.zhihu.ui.subscreens.ContentFilterSettingsScreen
import com.github.zly2006.zhihu.ui.subscreens.CreationEntryPosition
import com.github.zly2006.zhihu.ui.subscreens.DeveloperSettingsScreen
import com.github.zly2006.zhihu.ui.subscreens.IdentityManagementScreen
import com.github.zly2006.zhihu.ui.subscreens.OpenSourceLicensesScreen
import com.github.zly2006.zhihu.ui.subscreens.ReadingSettingsScreen
import com.github.zly2006.zhihu.ui.subscreens.SettingsSearchScreen
import com.github.zly2006.zhihu.ui.subscreens.SystemAndUpdateSettingsScreen
import com.github.zly2006.zhihu.ui.subscreens.TOP_LEVEL_BACK_TO_LAUNCHER_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.subscreens.WebDavSyncScreen
import com.github.zly2006.zhihu.update.UpdateOnLaunch
import com.github.zly2006.zhihu.video.VideoScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

private sealed class MainTabPage(
    val bottomDestination: TopLevelDestination,
    val key: String,
) {
    data object HomePage : MainTabPage(Home, "home")

    data object FollowPage : MainTabPage(Follow, "follow")

    data object HotListPage : MainTabPage(HotList, "hotlist")

    data object DailyPage : MainTabPage(Daily, "daily")

    data object OnlineHistoryPage : MainTabPage(OnlineHistory, "online_history")

    data object MyCollectionsPage : MainTabPage(MyCollections, "my_collections")

    data object AccountPage : MainTabPage(Account, "account")
}

internal val LocalReadingPlayerOverlayPadding = staticCompositionLocalOf { 0.dp }
internal val LocalArticleNavController = staticCompositionLocalOf<NavHostController?> { null }

/** 底栏中的一项；选中时显示 [selectedIcon]（填充版本），符合 Material 导航栏的选中态规范。 */
internal data class BottomBarItem(
    val destination: TopLevelDestination,
    val label: String,
    val icon: AppIcon,
    val selectedIcon: AppIcon,
)

/** 所有可以放进底栏的一级页面，按默认顺序排列；底栏本身和外观设置里的底栏选项都从这里取标题与图标。 */
internal val allBottomBarItems = listOf(
    BottomBarItem(Home, "主页", AppIcons.Home, AppIcons.HomeFilled),
    BottomBarItem(Follow, "关注", AppIcons.Group, AppIcons.GroupFilled),
    BottomBarItem(HotList, "热榜", AppIcons.Whatshot, AppIcons.WhatshotFilled),
    BottomBarItem(Daily, "日报", AppIcons.Newspaper, AppIcons.NewspaperFilled),
    BottomBarItem(OnlineHistory, "历史", AppIcons.History, AppIcons.HistoryFilled),
    BottomBarItem(MyCollections, "收藏夹", AppIcons.Bookmarks, AppIcons.BookmarksFilled),
    BottomBarItem(Account, "账号", AppIcons.ManageAccounts, AppIcons.ManageAccountsFilled),
)

/**
 * Zhihu-Hyperion 的共享应用主壳。
 *
 * 这个 composable 是顶层体验的唯一所有者：渲染可配置底部导航栏，承载横向主 tab pager，向子页面提供 [LocalNavigator]，
 * 并注册跨平台共享的 typed [NavDestination] route。设计上把顶层 tab 收在 [MainTabs] 内部，而不是把每个 tab
 * 都作为独立 NavHost 页面 push，这样 tab 重选、回到顶部、顶/底栏自动隐藏和持久化 tab 选择都能使用同一套状态模型。
 *
 * 用户可见的主壳设置通过 [preferenceState] 流入。设置页退出时只 reload 这份状态，不重建 NavHost，从而在应用底栏和主题相关变更时
 * 保留已加载页面、返回栈和滚动位置。
 *
 * [navController] 是主内容 NavHost 的控制器；[navigate] 负责把目标交给平台主导航入口。
 * [navigateContent] 可将目标交给指定的内容控制器，用于横屏列表/详情布局的右侧 NavHost；未提供平台实现时会回退到 [navigate]。
 *
 * @param navController 主内容 NavHost；顶层 tab 和默认内容页使用它维护返回栈。
 * @param navigate 平台主导航入口，负责处理顶层 tab 与外部导航。
 * @param navigateContent 指定内容 NavHost 的导航入口；大屏列表/详情布局通过它打开右侧内容。
 */
@OptIn(ExperimentalFoundationApi::class)
@Suppress("RestrictedApi")
@Composable
fun ZhihuMain(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    mainTabNavigationTarget: TopLevelDestination?,
    navigate: (NavDestination) -> Unit,
    navigateContent: (NavDestination, NavHostController) -> Unit = { destination, _ -> navigate(destination) },
    enableLandscapeListDetail: Boolean = false,
    isListDetailCapable: Boolean = true,
    setCurrentMainTabOpenFrom: (String?) -> Unit,
    consumeMainTabNavigationTarget: (TopLevelDestination) -> Unit,
    preferenceState: ZhihuMainPreferenceState,
    articleContent: @Composable (Article, NavBackStackEntry) -> Unit,
    showMainNavigationBar: Boolean = true,
    showHomeTopActions: Boolean = true,
    predictiveBackEnabled: Boolean = true,
    onCurrentMainTabDestinationChange: (TopLevelDestination) -> Unit = {},
    sentenceSimilarityContent: @Composable () -> Unit = {
        error("$platformName 暂不支持句子相似度测试")
    },
    blocklistSettingsNlpContent: @Composable (onNavigateBack: () -> Unit) -> Unit = {
        error("$platformName 暂不支持 NLP 智能屏蔽设置")
    },
    articleEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    articleExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
) = BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val containerWidth = maxWidth
    val isLargeLandscape = canShowLandscapeListDetail(
        enabled = enableLandscapeListDetail,
        deviceSupportsListDetail = isListDetailCapable,
        preferenceEnabled = preferenceState.landscapeListDetailEnabled,
        width = containerWidth,
        height = maxHeight,
    )
    val motion = MaterialTheme.motionScheme
    val navigationDispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher
    val backGestureInProgress by remember(navigationDispatcher) {
        navigationDispatcher
            ?.transitionState
            ?.map { it is NavigationEventTransitionState.InProgress && it.direction == NavigationEventTransitionState.TRANSITIONING_BACK }
            ?.distinctUntilChanged()
            ?: flowOf(false)
    }.collectAsState(false)
    val isPredictiveBack = predictiveBackEnabled && backGestureInProgress && LocalWindowInfo.current.isWindowFocused
    var predictiveBackEntry by remember { mutableStateOf<NavBackStackEntry?>(null) }
    val useLinearBackProgress = isPredictiveBack || predictiveBackEntry != null
    // NavHost 按总时长 seek：API 36 的 1080px 窗口中，15% 进度直接 seek slowSpatial 会移动 812px。
    // 线性插值保留到当前导航项结束转场，避免提交或取消时切换曲线导致跳帧；普通转场仍使用主题弹簧。
    val backSpatialSpec = if (useLinearBackProgress) tween<IntOffset>(easing = LinearEasing) else motion.slowSpatialSpec<IntOffset>()
    val backExitTransition = slideOutHorizontally(backSpatialSpec) { it } +
        if (useLinearBackProgress) ExitTransition.None else fadeOut(motion.defaultEffectsSpec())
    val bottomPadding = ScaffoldDefaults.contentWindowInsets.asPaddingValues().calculateBottomPadding()
    val duo3HomeAccount = preferenceState.duo3HomeAccount
    val tapToScrollToTopEnabled = preferenceState.tapToScrollToTopEnabled
    val autoHideBottomBar = preferenceState.autoHideBottomBar
    val collectionDirectBrowseEnabled = preferenceState.collectionDirectBrowseEnabled
    val selectedBottomBarItemKeys = preferenceState.selectedBottomBarItemKeys
    val startDestination = preferenceState.startDestination
    val reloadBottomBarPreferences = preferenceState::reload
    val readingPlayer = rememberReadingPlayerController()
    val readingPlayerState by readingPlayer.state
    val settings = koinInject<SettingsStore>()
    val accountState = rememberAccountSettingsAccountState()
    val creationEntryPosition by rememberObservedSetting(settings, CREATION_ENTRY_POSITION_PREFERENCE_KEY) {
        val stored = getString(CREATION_ENTRY_POSITION_PREFERENCE_KEY, CreationEntryPosition.Floating.key)
        CreationEntryPosition.entries.firstOrNull { it.key == stored } ?: CreationEntryPosition.Floating
    }
    val liquidGlassEnabled by rememberObservedSetting(settings, LIQUID_GLASS_PREFERENCE_KEY) {
        getBoolean(LIQUID_GLASS_PREFERENCE_KEY, isLiquidGlassEnabledByDefault)
    }
    val glassBackdrop = if (liquidGlassEnabled && isLiquidGlassSupported) {
        rememberGlassBackdrop(MaterialTheme.colorScheme.background)
    } else {
        null
    }
    UpdateOnLaunch()
    AppDialogHost()

    /** 右侧详情独立持有返回栈；栏内导航保留上级，从左侧重新选项时清空。 */
    val paneStateHolder = rememberSaveableStateHolder()
    val detailNavController = rememberNavController()
    val detailEntry by detailNavController.currentBackStackEntryAsState()
    val selectedContentDestination = detailEntry.readingDestinationOrNull()
    val hasOpenSecondaryDetail = detailEntry?.destination?.hasRoute<EmptyDetail>() == false
    var showReadingQueue by remember { mutableStateOf(false) }
    var isReadingPlayerExpandedByUser by remember { mutableStateOf(false) }
    var readingPlayerHeightPx by remember { mutableIntStateOf(0) }
    var bottomBarHeightPx by remember { mutableIntStateOf(0) }
    val readingPlayerOverlayOffsetState = remember { ReadingPlayerOverlayOffsetState() }
    val density = LocalDensity.current
    val currentOnMainTabDestinationChange by rememberUpdatedState(onCurrentMainTabDestinationChange)

    val navEntry by navController.currentBackStackEntryAsState()
    val showMainNavigation = navEntry?.destination?.hasRoute<MainTabs>() == true
    val isListPaneContext = navEntry.isListPaneDestination()
    // 窄屏从详情进入主栈页面时隐藏详情；返回原列表项后恢复同一详情和页面状态。
    var detailOwnerEntryId by rememberSaveable { mutableStateOf<String?>(null) }
    val showListDetail = isLargeLandscape && isListPaneContext
    val showDetailPane = hasOpenSecondaryDetail &&
        (showListDetail || navEntry?.id == detailOwnerEntryId)
    LaunchedEffect(isPredictiveBack, predictiveBackEntry) {
        if (isPredictiveBack) {
            predictiveBackEntry = if (showDetailPane) detailEntry else navEntry
        } else {
            predictiveBackEntry?.let { entry ->
                entry.lifecycle.currentStateFlow.first { it == Lifecycle.State.RESUMED || it == Lifecycle.State.DESTROYED }
                predictiveBackEntry = null
            }
        }
    }
    val hasOpenDetail = showDetailPane && selectedContentDestination != null
    val useSecondaryContentNavigation = enableLandscapeListDetail && isListPaneContext
    val detailPaneTransition = updateTransition(showListDetail || showDetailPane, label = "detail_pane_visibility")
    val detailPaneProgress by detailPaneTransition.animateFloat(transitionSpec = { motion.slowSpatialSpec() }, label = "detail_pane_progress") {
        if (it) 1f else 0f
    }
    val detailPaneAlpha by detailPaneTransition.animateFloat(transitionSpec = { motion.defaultEffectsSpec() }, label = "detail_pane_alpha") {
        if (it) 1f else 0f
    }
    val detailLifecycleOwner = rememberLifecycleOwner(
        maxLifecycle = if (showListDetail || showDetailPane) Lifecycle.State.RESUMED else Lifecycle.State.CREATED,
    )
    val listLifecycleOwner = rememberLifecycleOwner(
        maxLifecycle = if (showDetailPane && !showListDetail) Lifecycle.State.CREATED else Lifecycle.State.RESUMED,
    )

    LaunchedEffect(isLargeLandscape, navEntry) {
        if (showListDetail) detailOwnerEntryId = navEntry?.id
    }

    fun openListDetail(destination: NavDestination) {
        detailOwnerEntryId = navController.currentBackStackEntry?.id
        detailNavController.popBackStack(detailNavController.graph.startDestinationId, inclusive = false)
        navigateContent(destination, detailNavController)
    }

    fun openReadingDestination(destination: NavDestination, onlyIfReading: Boolean = false) {
        val current = if (hasOpenDetail) selectedContentDestination else navEntry.readingDestinationOrNull()
        if (current == destination || (onlyIfReading && current == null)) return
        val opensInDetail = useSecondaryContentNavigation && destination.isDetailPaneDestination()
        if (hasOpenDetail) {
            if (opensInDetail) {
                detailNavController.popBackStack()
            } else {
                detailNavController.popBackStack(detailNavController.graph.startDestinationId, inclusive = false)
            }
        } else if (current != null && !opensInDetail) {
            navController.popBackStack()
        }
        if (opensInDetail) {
            detailOwnerEntryId = navController.currentBackStackEntry?.id
            navigateContent(destination, detailNavController)
        } else {
            navigate(destination)
        }
    }
    val isOnReadingDetail = hasOpenDetail ||
        navEntry?.destination?.hasRoute<Article>() == true ||
        navEntry?.destination?.hasRoute<Question>() == true ||
        navEntry?.destination?.hasRoute<Pin>() == true
    val isReadingPlayerExpanded = readingPlayerState.hasSession &&
        (isOnReadingDetail || isReadingPlayerExpandedByUser)
    val shouldCompactPlayerOnBackgroundInteraction by rememberUpdatedState(
        isReadingPlayerExpandedByUser && !isOnReadingDetail,
    )
    val readingPlayerOverlayPadding = when {
        !readingPlayerState.hasSession -> 0.dp
        !isReadingPlayerExpanded -> 0.dp
        readingPlayerHeightPx > 0 -> with(density) { readingPlayerHeightPx.toDp() } + 16.dp
        else -> 16.dp
    }

    LaunchedEffect(readingPlayerState.hasSession) {
        if (!readingPlayerState.hasSession) {
            showReadingQueue = false
            isReadingPlayerExpandedByUser = false
            readingPlayerOverlayOffsetState.resetOffset()
        }
    }
    var previousReadingItemKey by remember { mutableStateOf(readingPlayerState.currentItem?.key) }
    LaunchedEffect(readingPlayerState.currentItem?.key) {
        val currentItem = readingPlayerState.currentItem
        val currentItemKey = currentItem?.key
        val itemChanged = previousReadingItemKey != null && previousReadingItemKey != currentItemKey
        previousReadingItemKey = currentItemKey
        if (itemChanged && currentItem != null) {
            openReadingDestination(currentItem.toDestination(readingPlayerState.sourceId), onlyIfReading = true)
        }
    }

    // 离开文章页时恢复系统状态栏（只在实际切换时触发）
    val isOnArticle = (hasOpenDetail && detailEntry?.destination?.hasRoute<Article>() == true) ||
        navEntry?.destination?.hasRoute<Article>() == true
    LaunchedEffect(navEntry) {
        isReadingPlayerExpandedByUser = false
        if (!isOnArticle) readingPlayerOverlayOffsetState.clearRoute()
    }
    var wasOnArticle by remember { mutableStateOf(false) }
    if (!isOnArticle && wasOnArticle) {
        LeaveImmersiveModeCleanup()
    }
    SideEffect {
        wasOnArticle = isOnArticle
    }

    var scrollToTopTrigger by remember { mutableIntStateOf(0) }
    // 滚动时自动隐藏底部导航栏
    var isBottomBarVisible by remember { mutableStateOf(true) }
    val bottomBarScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                when {
                    available.y < -3f -> isBottomBarVisible = false
                    available.y > 3f -> isBottomBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    val bottomBarItems = selectedBottomBarItemKeys.mapNotNull { key ->
        allBottomBarItems.firstOrNull { it.destination.name == key }
    }

    val mainTabPages = remember(bottomBarItems) {
        bottomBarItems.flatMap { item ->
            when (item.destination) {
                Home -> listOf(MainTabPage.HomePage)
                Follow -> listOf(MainTabPage.FollowPage)
                HotList -> listOf(MainTabPage.HotListPage)
                Daily -> listOf(MainTabPage.DailyPage)
                OnlineHistory -> listOf(MainTabPage.OnlineHistoryPage)
                MyCollections -> listOf(MainTabPage.MyCollectionsPage)
                Account -> listOf(MainTabPage.AccountPage)
                else -> emptyList()
            }
        }
    }

    fun pageIndexForDestination(destination: TopLevelDestination): Int = mainTabPages
        .indexOfFirst {
            it.bottomDestination::class == destination::class
        }.takeIf { it >= 0 } ?: mainTabPages
        .indexOfFirst {
            it.bottomDestination::class == startDestination::class
        }.takeIf { it >= 0 } ?: 0

    val mainPagerState = rememberPagerState(
        initialPage = pageIndexForDestination(startDestination),
        pageCount = { mainTabPages.size },
    )
    val coroutineScope = rememberCoroutineScope()

    var currentMainTabDestination by remember { mutableStateOf(startDestination) }
    val isAccountSurface = currentMainTabDestination == Account

    PlatformBackHandler(enabled = navEntry != null && navController.previousBackStackEntry != null && !showMainNavigation && !showDetailPane) {
        navController.popBackStack()
    }
    PlatformBackHandler(enabled = showDetailPane) {
        detailNavController.popBackStack()
    }

    fun navigateTopLevel(destination: TopLevelDestination) {
        if (destination == Account && hasOpenDetail) {
            detailNavController.popBackStack(
                detailNavController.graph.startDestinationId,
                inclusive = false,
            )
        }
        val targetPage = pageIndexForDestination(destination)
        coroutineScope.launch {
            mainPagerState.animateScrollToPage(targetPage, animationSpec = motion.defaultSpatialSpec())
        }
    }

    LaunchedEffect(navController) {
        loginNavigationRequestFlow.collect {
            if (navController.currentBackStackEntry?.destination?.hasRoute<Login>() == true && navController.previousBackStackEntry == null) {
                return@collect
            }
            navController.navigate(Login) {
                launchSingleTop = true
                if (!accountState.value.login && settings.getBoolean("loginForRecommendation", true)) {
                    popUpTo(navController.graph.id) { inclusive = false }
                }
            }
        }
    }

    LaunchedEffect(mainPagerState.currentPage, mainTabPages) {
        mainTabPages.getOrNull(mainPagerState.currentPage)?.bottomDestination?.let { destination ->
            currentMainTabDestination = destination
            setCurrentMainTabOpenFrom(destination.openFrom)
            currentOnMainTabDestinationChange(destination)
        }
    }

    LaunchedEffect(currentMainTabDestination, navEntry) {
        if (isAccountSurface && hasOpenDetail) {
            // A reading detail belongs to the feed that opened it. Do not carry it into
            // the account split pane when the user switches tabs.
            detailNavController.popBackStack(
                detailNavController.graph.startDestinationId,
                inclusive = false,
            )
        }
    }

    val topLevelBackToLauncher by rememberObservedSetting(settings, TOP_LEVEL_BACK_TO_LAUNCHER_PREFERENCE_KEY) {
        isTopLevelBackToLauncherSupported && getBoolean(TOP_LEVEL_BACK_TO_LAUNCHER_PREFERENCE_KEY, false)
    }
    PlatformBackHandler(
        showMainNavigation &&
            mainPagerState.currentPage != 0 &&
            !showDetailPane &&
            !topLevelBackToLauncher,
    ) {
        coroutineScope.launch {
            mainPagerState.animateScrollToPage(0, animationSpec = motion.defaultSpatialSpec())
        }
    }

    LaunchedEffect(mainTabNavigationTarget, mainTabPages) {
        mainTabNavigationTarget?.let { destination ->
            // 平台适配层会把旧的顶层 route 请求映射到 MainTabs。这里消费该请求，
            // 让 deeplink 等调用方仍能选中 Home/Follow 等 tab，而不是把旧 route 压入返回栈。
            mainPagerState.scrollToPage(pageIndexForDestination(destination))
            consumeMainTabNavigationTarget(destination)
        }
    }

    LaunchedEffect(mainTabPages) {
        if (mainTabPages.isNotEmpty()) {
            val currentDestinationStillVisible = mainTabPages.any {
                it.bottomDestination::class == currentMainTabDestination::class
            }
            val targetDestination = if (currentDestinationStillVisible) {
                currentMainTabDestination
            } else {
                startDestination
            }
            val targetPage = pageIndexForDestination(targetDestination)
            if (mainPagerState.currentPage != targetPage || mainPagerState.currentPage !in mainTabPages.indices) {
                mainPagerState.scrollToPage(targetPage)
            }
        }
    }

    var listPaneRatio by remember {
        mutableFloatStateOf(
            settings.getFloat(LANDSCAPE_LIST_PANE_RATIO_KEY, DEFAULT_LIST_PANE_RATIO),
        )
    }

    @Composable
    fun DetailPane(modifier: Modifier) {
        CompositionLocalProvider(
            LocalLifecycleOwner provides detailLifecycleOwner,
            LocalArticleNavController provides detailNavController,
            LocalSegmentCommentSheet provides SegmentCommentSheet,
            LocalNavigator provides Navigator(
                onNavigate = { destination ->
                    if (destination.isDetailPaneDestination() || destination.isAccountDetailDestination()) {
                        navigateContent(destination, detailNavController)
                    } else {
                        navigate(destination)
                    }
                },
                onNavigateBack = detailNavController::popBackStack,
                onNavigateTopLevel = ::navigateTopLevel,
            ),
            LocalSelectedContentDestination provides selectedContentDestination,
            LocalReadingPlayerOverlayPadding provides readingPlayerOverlayPadding,
            LocalReadingPlayerOverlayOffsetState provides readingPlayerOverlayOffsetState,
        ) {
            NavHost(
                navController = detailNavController,
                startDestination = EmptyDetail,
                modifier = modifier.testTag("detail_pane"),
                enterTransition = { slideInHorizontally(motion.slowSpatialSpec()) { it } },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { backExitTransition },
            ) {
                composable<EmptyDetail> {
                    if (showListDetail) EmptyDetailPane()
                }
                composable<Article>(
                    typeMap = mapOf(typeOf<ArticleType>() to ArticleTypeNavType),
                    enterTransition = articleEnterTransition,
                    exitTransition = articleExitTransition,
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = {
                        if (useLinearBackProgress || showListDetail || !targetState.destination.hasRoute<EmptyDetail>()) {
                            backExitTransition
                        } else {
                            fadeOut(motion.defaultEffectsSpec())
                        }
                    },
                ) { entry ->
                    articleContent(entry.toRoute(), entry)
                }
                composable<Pin> { entry ->
                    PinScreen(entry.toRoute())
                }
                accountSettings(reloadBottomBarPreferences, blocklistSettingsNlpContent)
            }
        }
    }

    // Material 自适应导航：窗口宽度达到中等尺寸（600dp）时，一级页面改用左侧导航轨，底栏只用于窄屏。
    // 列表-详情双栏时列表栏本身就是窄屏宽度，仍在列表栏底部用底栏：导航轨再占 96dp，
    // 约 860dp 宽的横屏平板就放不下列表栏（320dp）和详情栏（480dp）的最小宽度。
    val useNavigationRail = showMainNavigationBar && containerWidth >= 600.dp && !isLargeLandscape
    val showNavigationRail = useNavigationRail && showMainNavigation && navEntry != null
    var navigationRailWidth by remember { mutableStateOf(0.dp) }
    val contentWidth = if (showNavigationRail) containerWidth - navigationRailWidth else containerWidth
    val currentBottomDestination = mainTabPages.getOrNull(mainPagerState.targetPage)?.bottomDestination
    var showBottomCreateMenu by remember(creationEntryPosition, navEntry, currentBottomDestination, isBottomBarVisible) { mutableStateOf(false) }
    val showBottomCreateEntry = creationEntryPosition == CreationEntryPosition.Bottom

    fun onTopLevelItemClick(destination: TopLevelDestination, selected: Boolean) {
        isReadingPlayerExpandedByUser = false
        if (!selected) {
            navigateTopLevel(destination)
        } else if (tapToScrollToTopEnabled) {
            scrollToTopTrigger++
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (showNavigationRail) {
            WideNavigationRail(
                modifier = Modifier.onSizeChanged { navigationRailWidth = with(density) { it.width.toDp() } },
                header = if (showBottomCreateEntry) {
                    {
                        Box(Modifier.width(navigationRailWidth), contentAlignment = Alignment.Center) {
                            IconButton(
                                onClick = { showBottomCreateMenu = true },
                                modifier = Modifier.testTag(MAIN_CREATE_NAV_BUTTON_TAG),
                            ) {
                                Icon(AppIcons.Add, contentDescription = "发布")
                            }
                            CreationDropdownMenu(showBottomCreateMenu, { showBottomCreateMenu = false }, navigate)
                        }
                    }
                } else {
                    null
                },
            ) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    bottomBarItems.forEach { item ->
                        val destination = item.destination
                        val selected = currentBottomDestination?.let { it::class == destination::class } == true
                        WideNavigationRailItem(
                            selected = selected,
                            onClick = { onTopLevelItemClick(destination, selected) },
                            icon = {
                                Icon(if (selected) item.selectedIcon else item.icon, contentDescription = item.label)
                            },
                            label = { Text(item.label) },
                            railExpanded = false,
                            modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}"),
                        )
                    }
                }
            }
        }
        Box(Modifier.fillMaxSize().padding(start = if (showNavigationRail) navigationRailWidth else 0.dp)) {
            val listPaneWidth = if (showListDetail) {
                normalizedListPaneWidth(contentWidth, listPaneRatio)
            } else {
                contentWidth
            }
            val listPaneModifier = if (showListDetail) {
                Modifier.width(listPaneWidth).fillMaxHeight().align(Alignment.CenterStart)
            } else {
                Modifier.fillMaxSize()
            }
            LaunchedEffect(navEntry, isLargeLandscape, hasOpenSecondaryDetail, isListPaneContext) {
                val directReadingDestination = navEntry.readingDestinationOrNull()?.takeIf { it.isDetailPaneDestination() }
                if (enableLandscapeListDetail && directReadingDestination != null) {
                    navController.popBackStack()
                    if (navController.currentBackStackEntry == null) navController.navigate(MainTabs)
                    detailOwnerEntryId = navController.currentBackStackEntry?.id
                    detailNavController.popBackStack(detailNavController.graph.startDestinationId, inclusive = false)
                    // 平台入口已经处理历史、评论交接等副作用，这里只转移页面归属。
                    detailNavController.navigate(directReadingDestination)
                }
            }
            if (showListDetail || !showDetailPane || useLinearBackProgress) {
                paneStateHolder.SaveableStateProvider("list") {
                    Scaffold(
                        modifier = listPaneModifier
                            .testTag("list_pane")
                            .nestedScroll(bottomBarScrollConnection),
                        bottomBar = {
                            if (showMainNavigationBar && !useNavigationRail && navEntry != null) {
                                // 页面切换时重置底部导航栏可见状态
                                LaunchedEffect(navEntry) { isBottomBarVisible = true }
                                AnimatedVisibility(
                                    visible = showMainNavigation && (!autoHideBottomBar || isBottomBarVisible),
                                    modifier = Modifier.onSizeChanged { bottomBarHeightPx = it.height },
                                    enter = slideInVertically(motion.slowSpatialSpec()) { it },
                                    exit = slideOutVertically(motion.slowSpatialSpec()) { it },
                                ) {
                                    Box {
                                        if (glassBackdrop != null) {
                                            val selectedIndex = bottomBarItems.indexOfFirst { item ->
                                                currentBottomDestination?.let { it::class == item.destination::class } == true
                                            }
                                            LiquidGlassTabBar(
                                                tabs = bottomBarItems
                                                    .mapIndexed { index, item ->
                                                        LiquidGlassTab(
                                                            label = item.label,
                                                            testTag = "nav_tab_${item.destination.name.lowercase()}",
                                                            icon = { selected ->
                                                                Icon(if (selected) item.selectedIcon else item.icon, contentDescription = null)
                                                            },
                                                            onClick = { onTopLevelItemClick(item.destination, index == selectedIndex) },
                                                        )
                                                    }.toMutableList()
                                                    .apply {
                                                        if (showBottomCreateEntry) {
                                                            add(
                                                                size / 2,
                                                                LiquidGlassTab(
                                                                    label = "发布",
                                                                    testTag = MAIN_CREATE_NAV_BUTTON_TAG,
                                                                    icon = { Icon(AppIcons.Add, contentDescription = null) },
                                                                    onClick = { showBottomCreateMenu = true },
                                                                    isNavigation = false,
                                                                ),
                                                            )
                                                        }
                                                    },
                                                selectedIndex = selectedIndex + if (showBottomCreateEntry && selectedIndex >= bottomBarItems.size / 2) 1 else 0,
                                                backdrop = glassBackdrop,
                                            )
                                        } else {
                                            ShortNavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                                                val itemColors = ShortNavigationBarItemDefaults.colors(
                                                    selectedIndicatorColor = Color.Transparent,
                                                )
                                                bottomBarItems.forEachIndexed { index, item ->
                                                    if (showBottomCreateEntry && index == bottomBarItems.size / 2) {
                                                        ShortNavigationBarItem(
                                                            selected = false,
                                                            onClick = { showBottomCreateMenu = true },
                                                            icon = { Icon(AppIcons.Add, contentDescription = "发布") },
                                                            label = { Text("发布") },
                                                            colors = itemColors,
                                                            modifier = Modifier.testTag(MAIN_CREATE_NAV_BUTTON_TAG),
                                                        )
                                                    }
                                                    val destination = item.destination
                                                    val selected = currentBottomDestination?.let { it::class == destination::class } == true
                                                    ShortNavigationBarItem(
                                                        selected = selected,
                                                        onClick = { onTopLevelItemClick(destination, selected) },
                                                        icon = {
                                                            Icon(if (selected) item.selectedIcon else item.icon, contentDescription = item.label)
                                                        },
                                                        label = { Text(item.label) },
                                                        colors = itemColors,
                                                        modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}"),
                                                    )
                                                }
                                            }
                                        }
                                        CreationDropdownMenu(
                                            expanded = showBottomCreateMenu,
                                            onDismiss = { showBottomCreateMenu = false },
                                            onNavigate = navigate,
                                            offset = DpOffset(
                                                x = listPaneWidth / (bottomBarItems.size + 1) * (bottomBarItems.size / 2),
                                                y = 0.dp,
                                            ),
                                        )
                                    }
                                }
                            }
                        },
                    ) { innerPadding ->
                        CompositionLocalProvider(
                            LocalLifecycleOwner provides listLifecycleOwner,
                            LocalArticleNavController provides navController,
                            LocalSegmentCommentSheet provides SegmentCommentSheet,
                            LocalNavigator provides Navigator(
                                onNavigate = { destination ->
                                    if (
                                        useSecondaryContentNavigation &&
                                        (
                                            destination.isDetailPaneDestination() ||
                                                (showListDetail && currentMainTabDestination == Account && destination.isAccountDetailDestination())
                                        )
                                    ) {
                                        openListDetail(destination)
                                    } else {
                                        navigate(destination)
                                    }
                                },
                                onNavigateBack = navController::popBackStack,
                                onNavigateTopLevel = ::navigateTopLevel,
                            ),
                            LocalReadingPlayerOverlayPadding provides readingPlayerOverlayPadding,
                            LocalReadingPlayerOverlayOffsetState provides readingPlayerOverlayOffsetState,
                            LocalSelectedContentDestination provides selectedContentDestination,
                        ) {
                            NavHost(
                                navController,
                                modifier = Modifier.glassBackdropSource(glassBackdrop).pointerInput(Unit) {
                                    while (true) {
                                        awaitPointerEventScope {
                                            awaitFirstDown(
                                                requireUnconsumed = false,
                                                pass = PointerEventPass.Initial,
                                            )
                                            while (
                                                awaitPointerEvent(PointerEventPass.Final)
                                                    .changes
                                                    .any { it.pressed }
                                            ) {
                                                // 等手势完成后再重组，避免取消同一次背景点击或滚动。
                                            }
                                        }
                                        if (shouldCompactPlayerOnBackgroundInteraction) {
                                            delay(100)
                                            isReadingPlayerExpandedByUser = false
                                        }
                                    }
                                },
                                startDestination = MainTabs,
                                enterTransition = {
                                    slideInHorizontally(motion.slowSpatialSpec()) { it }
                                },
                                exitTransition = {
                                    ExitTransition.None
                                },
                                popEnterTransition = {
                                    EnterTransition.None
                                },
                                popExitTransition = {
                                    backExitTransition
                                },
                            ) {
                                composable<MainTabs> {
                                    MainTabsPager(
                                        pagerState = mainPagerState,
                                        pages = mainTabPages,
                                        scrollToTopTrigger = scrollToTopTrigger,
                                        innerPadding = innerPadding,
                                        collectionDirectBrowseEnabled = collectionDirectBrowseEnabled,
                                        showHomeTopActions = showHomeTopActions,
                                        creationEntryPosition = if (!showMainNavigationBar && showBottomCreateEntry) CreationEntryPosition.Top else creationEntryPosition,
                                    )
                                }
                                composable<Login> {
                                    val canNavigateBack = navController.previousBackStackEntry != null
                                    LoginScreen(
                                        onLoginComplete = {
                                            if (navController.previousBackStackEntry == null) {
                                                navController.navigate(MainTabs) {
                                                    popUpTo<Login> { inclusive = true }
                                                    launchSingleTop = true
                                                }
                                            } else {
                                                navController.popBackStack()
                                            }
                                        },
                                        onNavigateBack = if (canNavigateBack) ({ navController.popBackStack() }) else null,
                                    )
                                }
                                composable<Question> { navEntry ->
                                    val question: Question = navEntry.toRoute()
                                    QuestionScreen(question)
                                }
                                composable<Topic> { navEntry ->
                                    TopicScreen(navEntry.toRoute())
                                }
                                composable<com.github.zly2006.zhihu.navigation.Column> { navEntry ->
                                    ColumnScreen(navEntry.toRoute())
                                }
                                composable<com.github.zly2006.zhihu.navigation.FollowUserUpdates> { navEntry ->
                                    FollowUserUpdatesScreen(navEntry.toRoute())
                                }
                                composable<WriteAnswer> { navEntry ->
                                    val args: WriteAnswer = navEntry.toRoute()
                                    WriteAnswerScreen(args)
                                }
                                composable<WritePin> { navEntry ->
                                    WritePinScreen(navEntry.toRoute())
                                }
                                composable<Article>(
                                    typeMap = mapOf(typeOf<ArticleType>() to ArticleTypeNavType),
                                    enterTransition = articleEnterTransition,
                                    exitTransition = articleExitTransition,
                                    popEnterTransition = { EnterTransition.None },
                                    popExitTransition = { backExitTransition },
                                ) { navEntry ->
                                    val article: Article = navEntry.toRoute()
                                    // 启用自适应导航时由 effect 转交详情栈，不创建会抢先消费一次性交接数据的临时页面。
                                    if (!enableLandscapeListDetail) articleContent(article, navEntry)
                                }
                                composable<HotList> {
                                    HotListScreen(innerPadding)
                                }
                                composable<Follow> {
                                    FollowScreen(
                                        scrollToTopTrigger = scrollToTopTrigger,
                                        innerPadding = innerPadding,
                                        parentPagerState = mainPagerState,
                                    )
                                }
                                composable<Daily> {
                                    DailyScreen(innerPadding = innerPadding)
                                }
                                composable<History> {
                                    LegacyLocalHistoryScreen(innerPadding)
                                }
                                composable<OnlineHistory> {
                                    OnlineHistoryScreen(innerPadding = innerPadding)
                                }
                                composable<Account> {
                                    AccountSettingScreen(innerPadding)
                                }
                                composable<Search>(
                                    enterTransition = {
                                        if (initialState.destination.hasRoute<Search>()) {
                                            EnterTransition.None
                                        } else {
                                            fadeIn(animationSpec = motion.defaultEffectsSpec()) +
                                                slideInVertically(animationSpec = motion.slowSpatialSpec()) { it / 16 } +
                                                scaleIn(
                                                    animationSpec = motion.slowSpatialSpec(),
                                                    initialScale = 0.985f,
                                                )
                                        }
                                    },
                                    popEnterTransition = { EnterTransition.None },
                                    popExitTransition = {
                                        if (targetState.destination.hasRoute<Search>()) {
                                            ExitTransition.None
                                        } else if (useLinearBackProgress) {
                                            backExitTransition
                                        } else {
                                            fadeOut(animationSpec = motion.defaultEffectsSpec()) +
                                                slideOutVertically(animationSpec = motion.slowSpatialSpec()) { it / 20 } +
                                                scaleOut(
                                                    animationSpec = motion.slowSpatialSpec(),
                                                    targetScale = 0.985f,
                                                )
                                        }
                                    },
                                ) { navEntry ->
                                    val search: Search = navEntry.toRoute()
                                    SearchScreen(search)
                                }
                                composable<Collections> { navEntry ->
                                    val data: Collections = navEntry.toRoute()
                                    CollectionScreen(
                                        urlToken = data.userToken,
                                        contentPadding = innerPadding,
                                    )
                                }
                                composable<CollectionContent> { navEntry ->
                                    val content: CollectionContent = navEntry.toRoute()
                                    CollectionContentScreen(content.collectionId)
                                }
                                composable<Person> { navEntry ->
                                    val person: Person = navEntry.toRoute()
                                    PeopleScreen(person)
                                }
                                composable<Pin> { navEntry ->
                                    val pin: Pin = navEntry.toRoute()
                                    if (!enableLandscapeListDetail) PinScreen(pin)
                                }
                                composable<Video> { navEntry ->
                                    VideoScreen(navEntry.toRoute())
                                }
                                accountSettings(reloadBottomBarPreferences, blocklistSettingsNlpContent)
                                composable<Notification> {
                                    NotificationScreen()
                                }
                                composable<Notification.Entry> { navEntry ->
                                    val entry: Notification.Entry = navEntry.toRoute()
                                    NotificationTimelineScreen(entry.entryName, entry.title)
                                }
                                composable<Notification.Invitations> {
                                    NotificationTimelineScreen("invite", "邀请回答")
                                }
                                composable<Notification.Message> { navEntry ->
                                    PrivateMessageScreen(navEntry.toRoute())
                                }
                                composable<Notification.NotificationSettings> { navEntry ->
                                    NotificationSettingsScreen(
                                        setting = navEntry.toRoute<Notification.NotificationSettings>().setting,
                                    )
                                }
                                composable<SentenceSimilarityTest> {
                                    sentenceSimilarityContent()
                                }
                            }
                        }
                    }
                }
            }

            if (detailPaneTransition.currentState || detailPaneTransition.targetState || detailPaneTransition.isRunning || detailEntry == null) {
                paneStateHolder.SaveableStateProvider("detail") {
                    // 尺寸变化只调整同一宿主的布局；隐藏时保存整个导航子树，独立弹窗也随页面离开组合。
                    Row(
                        (if (detailEntry != null) Modifier.fillMaxSize() else Modifier.size(0.dp)).graphicsLayer {
                            translationX = if (showListDetail) 0f else size.width * (1f - detailPaneProgress.coerceIn(0f, 1f))
                            alpha = if (showListDetail) 1f else detailPaneAlpha
                        },
                    ) {
                        if (showListDetail) {
                            Spacer(Modifier.width(listPaneWidth))
                            ListDetailDivider(
                                onDrag = { deltaPx ->
                                    val usableWidthPx = with(density) {
                                        (contentWidth - LIST_DETAIL_DIVIDER_WIDTH).toPx()
                                    }
                                    if (usableWidthPx > 0f) {
                                        listPaneRatio = (listPaneRatio + deltaPx / usableWidthPx).coerceIn(0f, 1f)
                                    }
                                },
                                onAdjustBy = { ratioDelta ->
                                    listPaneRatio = (listPaneRatio + ratioDelta).coerceIn(0f, 1f)
                                },
                                onDragStopped = {
                                    val normalizedWidth = normalizedListPaneWidth(contentWidth, listPaneRatio)
                                    listPaneRatio = normalizedWidth.value /
                                        (contentWidth - LIST_DETAIL_DIVIDER_WIDTH).value
                                    settings.putFloat(LANDSCAPE_LIST_PANE_RATIO_KEY, listPaneRatio)
                                },
                            )
                        }
                        DetailPane(Modifier.weight(1f).fillMaxSize())
                    }
                }
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = if (showListDetail && showDetailPane) listPaneWidth + LIST_DETAIL_DIVIDER_WIDTH else 0.dp,
                        bottom = 16.dp + if (
                            !showDetailPane && showMainNavigation && showMainNavigationBar && !useNavigationRail && (!autoHideBottomBar || isBottomBarVisible)
                        ) {
                            // 底栏的样式（Material 底栏或悬浮的液态玻璃底栏）决定了它的高度，按实测结果避让。
                            maxOf(bottomPadding, with(density) { bottomBarHeightPx.toDp() })
                        } else {
                            bottomPadding
                        },
                    ).fillMaxWidth(),
                visible = isReadingPlayerExpanded,
                enter = fadeIn(motion.defaultEffectsSpec()) + scaleIn(motion.slowSpatialSpec(), initialScale = 0.92f),
                exit = fadeOut(motion.defaultEffectsSpec()) + scaleOut(motion.slowSpatialSpec(), targetScale = 0.92f),
            ) {
                ReadingPlayerBar(
                    state = readingPlayerState,
                    onPrevious = readingPlayer::playPrevious,
                    onTogglePlayPause = readingPlayer::togglePlayPause,
                    onNext = readingPlayer::playNext,
                    onStop = readingPlayer::stop,
                    onOpenQueue = { showReadingQueue = true },
                    onPlaybackSpeedChange = { speed ->
                        saveReadingPlaybackSpeed(settings, speed)
                        readingPlayer.setPlaybackSpeed(speed)
                    },
                    onBackgroundInteraction = {
                        if (!isOnReadingDetail) isReadingPlayerExpandedByUser = false
                    },
                    modifier = Modifier
                        .onSizeChanged { readingPlayerHeightPx = it.height }
                        .graphicsLayer {
                            translationY = readingPlayerOverlayOffsetState.verticalOffsetPx
                        },
                )
            }

            AnimatedVisibility(
                visible = readingPlayerState.hasSession && !isReadingPlayerExpanded,
                enter = fadeIn(motion.defaultEffectsSpec()),
                exit = fadeOut(motion.defaultEffectsSpec()),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CompactReadingPlayerButton(
                        state = readingPlayerState,
                        onExpand = { isReadingPlayerExpandedByUser = true },
                    )
                }
            }
        }
    }

    if (showReadingQueue && readingPlayerState.hasSession) {
        ReadingQueueSheet(
            state = readingPlayerState,
            onDismissRequest = {
                showReadingQueue = false
                if (!isOnReadingDetail) isReadingPlayerExpandedByUser = false
            },
            onItemClick = { index, item ->
                previousReadingItemKey = item.key
                if (index != readingPlayerState.currentIndex) {
                    readingPlayer.playAt(index)
                }
                showReadingQueue = false
                openReadingDestination(item.toDestination(readingPlayerState.sourceId))
            },
            onOpenSettings = {
                showReadingQueue = false
                isReadingPlayerExpandedByUser = false
                if (navEntry?.destination?.hasRoute<Account.ReadingSettings>() != true) {
                    navigate(Account.ReadingSettings)
                }
            },
        )
    }
}

/**
 * 渲染可配置底部导航主壳内的页面。
 *
 * 每个页面都接收主壳给出的 [innerPadding]，保证系统栏、底部栏和子页面之间的留白一致。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainTabsPager(
    pagerState: PagerState,
    pages: List<MainTabPage>,
    scrollToTopTrigger: Int,
    innerPadding: PaddingValues,
    collectionDirectBrowseEnabled: Boolean,
    showHomeTopActions: Boolean,
    creationEntryPosition: CreationEntryPosition,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        pageNestedScrollConnection = NoOpPagerNestedScrollConnection,
    ) { pageIndex ->
        val page = pages.getOrNull(pageIndex) ?: return@HorizontalPager
        when (page) {
            MainTabPage.HomePage -> HomeScreen(
                scrollToTopTrigger = scrollToTopTrigger,
                innerPadding = innerPadding,
                showTopActions = showHomeTopActions,
                isActive = pagerState.currentPage == pageIndex,
                creationEntryPosition = creationEntryPosition,
            )

            MainTabPage.FollowPage -> FollowScreen(
                scrollToTopTrigger = scrollToTopTrigger,
                innerPadding = innerPadding,
                parentPagerState = pagerState,
                isActive = pagerState.currentPage == pageIndex,
            )

            MainTabPage.HotListPage -> HotListScreen(
                innerPadding = innerPadding,
                scrollToTopTrigger = scrollToTopTrigger,
                isActive = pagerState.currentPage == pageIndex,
            )

            MainTabPage.DailyPage -> DailyScreen(
                scrollToTopTrigger = scrollToTopTrigger,
                isActive = pagerState.currentPage == pageIndex,
                innerPadding = innerPadding,
            )

            MainTabPage.OnlineHistoryPage -> OnlineHistoryScreen(
                scrollToTopTrigger = scrollToTopTrigger,
                isActive = pagerState.currentPage == pageIndex,
                innerPadding = innerPadding,
            )

            MainTabPage.MyCollectionsPage -> MyCollectionsTopLevelPage(
                scrollToTopTrigger = scrollToTopTrigger,
                innerPadding = innerPadding,
                collectionDirectBrowseEnabled = collectionDirectBrowseEnabled,
                isActive = pagerState.currentPage == pageIndex,
            )

            MainTabPage.AccountPage -> AccountSettingScreen(
                innerPadding = innerPadding,
                isActive = pagerState.currentPage == pageIndex,
            )
        }
    }
}

@Composable
private fun MyCollectionsTopLevelPage(
    scrollToTopTrigger: Int,
    innerPadding: PaddingValues,
    collectionDirectBrowseEnabled: Boolean,
    isActive: Boolean,
) {
    val account = rememberAccountSettingsAccountState().value
    if (collectionDirectBrowseEnabled) {
        CollectionBrowseScreen(
            urlToken = account.urlToken,
            contentPadding = innerPadding,
            showBackButton = false,
            scrollToTopTrigger = scrollToTopTrigger,
            isActive = isActive,
        )
    } else {
        CollectionScreen(
            urlToken = account.urlToken,
            contentPadding = innerPadding,
            showBackButton = false,
            isActive = isActive,
        )
    }
}

private val TopLevelDestination.openFrom: String?
    get() = when (this) {
        Home -> ContentOpenFrom.HOME_FEED
        OnlineHistory -> ContentOpenFrom.HISTORY
        else -> null
    }

internal fun NavBackStackEntry?.hasRoute(cls: KClass<out NavDestination>): Boolean {
    val dest = this?.destination ?: return false
    return dest.hierarchy.any { it.hasRoute(cls) }
}

private fun NavBackStackEntry?.readingDestinationOrNull(): NavDestination? = when {
    this?.destination?.hasRoute<Article>() == true -> runCatching { toRoute<Article>() }.getOrNull()
    this?.destination?.hasRoute<Pin>() == true -> runCatching { toRoute<Pin>() }.getOrNull()
    this?.destination?.hasRoute<Question>() == true -> runCatching { toRoute<Question>() }.getOrNull()
    else -> null
}

private fun NavDestination.isAccountDetailDestination(): Boolean = when (this) {
    is Account.AppearanceSettings,
    Account.ReadingSettings,
    is Account.RecommendSettings,
    Account.RecommendSettings.Blocklist,
    Account.RecommendSettings.BlockedFeedHistory,
    Account.IdentityManagement,
    is Account.SystemAndUpdateSettings,
    Account.WebDavSync,
    Account.SettingsSearch,
    Account.OpenSourceLicenses,
    Account.DeveloperSettings,
    Account.DeveloperSettings.ColorScheme,
    -> true

    else -> false
}

private fun NavBackStackEntry?.isListPaneDestination(): Boolean = when {
    this == null -> false
    destination.hasRoute<MainTabs>() -> true
    destination.hasRoute<Search>() -> true
    destination.hasRoute<History>() -> true
    destination.hasRoute<Collections>() -> true
    destination.hasRoute<CollectionContent>() -> true
    destination.hasRoute<Question>() -> true
    destination.hasRoute<Person>() -> true
    destination.hasRoute<Topic>() -> true
    destination.hasRoute<com.github.zly2006.zhihu.navigation.Column>() -> true
    destination.hasRoute<Notification>() -> true
    destination.hasRoute<Notification.Entry>() -> true
    destination.hasRoute<Notification.Invitations>() -> true
    destination.hasRoute<Account.RecommendSettings.BlockedFeedHistory>() -> true
    else -> false
}

private fun NavGraphBuilder.accountSettings(
    reloadPreferences: () -> Unit,
    nlpContent: @Composable (onNavigateBack: () -> Unit) -> Unit,
) {
    composable<Account.RecommendSettings.Blocklist> {
        BlocklistSettingsScreen(nlpContent)
    }
    composable<Account.RecommendSettings.BlockedFeedHistory> {
        BlockedFeedHistoryScreen()
    }
    composable<Account.AppearanceSettings> { entry ->
        val args = entry.toRoute<Account.AppearanceSettings>()
        AppearanceSettingsScreen(
            setting = args.setting,
            onExit = reloadPreferences,
        )
    }
    composable<Account.RecommendSettings> { entry ->
        val args = entry.toRoute<Account.RecommendSettings>()
        ContentFilterSettingsScreen(args.setting)
    }
    composable<Account.IdentityManagement> {
        IdentityManagementScreen()
    }
    composable<Account.SystemAndUpdateSettings> { entry ->
        SystemAndUpdateSettingsScreen(
            setting = entry.toRoute<Account.SystemAndUpdateSettings>().setting,
        )
    }
    composable<Account.ReadingSettings> {
        ReadingSettingsScreen()
    }
    composable<Account.WebDavSync> {
        WebDavSyncScreen()
    }
    composable<Account.SettingsSearch> {
        SettingsSearchScreen()
    }
    composable<Account.OpenSourceLicenses> {
        OpenSourceLicensesScreen()
    }
    composable<Account.DeveloperSettings> {
        DeveloperSettingsScreen()
    }
    composable<Account.DeveloperSettings.ColorScheme> {
        ColorSchemeScreen()
    }
}

private val SegmentCommentSheet: @Composable (Boolean, () -> Unit, SegmentCommentHolder) -> Unit = { showComments, onDismiss, content ->
    CommentScreenComponent(showComments = showComments, onDismiss = onDismiss, content = content)
}
