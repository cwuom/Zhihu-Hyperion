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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.fleeksoft.ksoup.Ksoup
import com.github.zly2006.zhihu.icons.AppIcons
import com.github.zly2006.zhihu.icons.Icon
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.resolveContent
import com.github.zly2006.zhihu.platform.SettingsStore
import com.github.zly2006.zhihu.platform.isLiteVariant
import com.github.zly2006.zhihu.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.ui.components.AppLoadingIndicator
import com.github.zly2006.zhihu.ui.components.AppPullToRefreshBox
import com.github.zly2006.zhihu.ui.components.EmptyState
import com.github.zly2006.zhihu.ui.components.EmptyStateAction
import com.github.zly2006.zhihu.ui.components.GroupedListItem
import com.github.zly2006.zhihu.ui.components.pageTurnViewportWithGuide
import com.github.zly2006.zhihu.ui.components.rememberPageTurnTarget
import com.github.zly2006.zhihu.ui.subscreens.BOTTOM_BAR_SINGLE_TAP_REFRESH_PREFERENCE_KEY
import com.github.zly2006.zhihu.util.formatDailyDate
import com.github.zly2006.zhihu.util.jsonObject
import com.github.zly2006.zhihu.util.twoDigitString
import com.github.zly2006.zhihu.viewmodel.DailyViewModel
import com.github.zly2006.zhihu.viewmodel.rememberZhihuApiEnvironment
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.jsonPrimitive
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 知乎日报页面。
 *
 * 最新一期在顶部用轮播展示当天头条，下面按日期分组列出日报，日期标题吸顶；滚到底部继续加载更早的日期，完整版可以选择日期。
 * 日报条目可能跳转到站内内容或外部链接，因此页面同时依赖 [LocalNavigator] 和系统 URI 打开能力。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun DailyScreen(
    scrollToTopTrigger: Int = 0,
    isActive: Boolean = true,
    innerPadding: PaddingValues = PaddingValues(0.dp),
) {
    val settings = koinInject<SettingsStore>()
    val navigator = LocalNavigator.current
    val httpClient = rememberZhihuApiEnvironment(allowGuestAccess = false).httpClient()
    val uriHandler = LocalUriHandler.current
    val viewModel = viewModel { DailyViewModel() }
    val userMessages = rememberUserMessageSink()
    var showDatePicker by remember { mutableStateOf(false) }
    var openingStoryId by remember { mutableStateOf<Long?>(null) }
    var pendingDateSelection by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val pageTurnTarget = rememberPageTurnTarget(
        listState = listState,
        enabled = isActive && (isLiteVariant || !showDatePicker),
    )
    var cachedScrollToTopTrigger by remember { mutableIntStateOf(scrollToTopTrigger) }

    LaunchedEffect(listState) {
        // 滚动到底部时加载更多
        snapshotFlow {
            val info = listState.layoutInfo
            (info.visibleItemsInfo.lastOrNull()?.index ?: 0) to info.totalItemsCount
        }.collect { (last, total) ->
            if (total > 0 && last >= total - 3) {
                viewModel.loadMore(httpClient)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.sections.isEmpty()) {
            viewModel.loadLatest(httpClient)
        }
    }

    if (!isLiteVariant) {
        LaunchedEffect(showDatePicker, pendingDateSelection) {
            if (showDatePicker) return@LaunchedEffect
            val selectedDate = pendingDateSelection ?: return@LaunchedEffect
            // DatePickerDialog is hosted in a platform window. Delay page replacement until the closing
            // dialog has yielded its focus and measure work back to the main content window.
            withFrameNanos { }
            viewModel.loadDate(httpClient, selectedDate)
            listState.requestScrollToItem(0)
            if (pendingDateSelection == selectedDate) {
                pendingDateSelection = null
            }
        }
    }

    // 失败和空状态里没有列表：scrollToItem 会一直等列表首次布局而永远不返回，requestScrollToItem 不等。
    val doRefresh: () -> Unit = {
        scope.launch {
            viewModel.loadLatest(httpClient)
            listState.requestScrollToItem(0)
        }
    }
    val retry: () -> Unit = {
        scope.launch {
            viewModel.retry(httpClient)
            listState.requestScrollToItem(0)
        }
    }
    // 日报条目只给出日报站内的链接，先取日报正文里的“查看知乎原文”，能解析成站内内容就在应用内打开。
    // 取正文期间忽略重复点击，免得慢网络下连点后打开好几个页面。
    val openStory: (id: Long, url: String) -> Unit = { id, url ->
        if (openingStoryId == null) {
            openingStoryId = id
            scope.launch {
                try {
                    val response = withContext(Dispatchers.Default) {
                        httpClient
                            .get("https://daily.zhihu.com/api/7/story/$id")
                            .jsonObject()
                    }
                    val doc = response["body"]?.jsonPrimitive?.content?.let(Ksoup::parse)
                    val destination = doc
                        ?.selectFirst("a.originUrl")
                        ?.attr("href")
                        ?.let(::resolveContent)
                        ?: doc
                            ?.selectFirst("div.view-more a")
                            ?.attr("href")
                            ?.let(::resolveContent)
                    if (destination != null) {
                        navigator.onNavigate(destination)
                    } else {
                        // “瞎扯”等日报合辑的 originUrl 是 /question/None/answer/None，
                        // 没有唯一原文；打开完整日报，不能任选正文中的一条回答替代合辑。
                        // https://github.com/eltavine/Zhihu-Hyperion/issues/68
                        uriHandler.openUri(url)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    userMessages.showShortMessage("日报正文加载失败：${e.message}")
                } finally {
                    openingStoryId = null
                }
            }
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
                    doRefresh()
                }

                TopLevelReselectAction.ScrollToTop -> {
                    listState.animateScrollToItem(0)
                }

                null -> {}
            }
        }
        cachedScrollToTopTrigger = scrollToTopTrigger
    }

    if (!isLiteVariant && showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = Clock.System.now().toEpochMilliseconds(),
            selectableDates = DailySelectableDates,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        pendingDateSelection = formatDailyDatePickerSelection(millis)
                    }
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 分段列表项是 surface 色，放在 surfaceContainer 底色上才能看出分组，与设置页一致。
    Scaffold(
        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "知乎日报",
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        modifier = Modifier.testTag(DAILY_SCREEN_TITLE_TAG),
                    )
                },
                actions = {
                    if (!isLiteVariant) {
                        IconButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.testTag(DAILY_SCREEN_DATE_PICKER_BUTTON_TAG),
                        ) {
                            Icon(AppIcons.DateRange, contentDescription = "选择日期")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            )
        },
    ) { scaffoldPadding ->
        // 已有内容时刷新保留列表，只显示下拉指示器；没有内容时只显示页面中央的加载指示器。
        AppPullToRefreshBox(
            isRefreshing = viewModel.isLoading && viewModel.sections.isNotEmpty(),
            onRefresh = doRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = scaffoldPadding.calculateTopPadding()),
        ) {
            when {
                viewModel.isLoading && viewModel.sections.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(DAILY_SCREEN_LOADING_TAG),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppLoadingIndicator()
                    }
                }

                viewModel.error != null || viewModel.sections.isEmpty() -> {
                    // 可以滚动才会把下拉手势交给外层的下拉刷新。
                    Box(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (viewModel.error != null) {
                            EmptyState(
                                icon = AppIcons.Error,
                                title = "日报加载失败",
                                modifier = Modifier.testTag(DAILY_SCREEN_ERROR_TAG),
                                description = viewModel.error,
                                action = EmptyStateAction("重试", AppIcons.Refresh, DAILY_SCREEN_RETRY_TAG, retry),
                            )
                        } else {
                            EmptyState(
                                icon = AppIcons.Newspaper,
                                title = "这一天没有日报",
                                modifier = Modifier.testTag(DAILY_SCREEN_EMPTY_TAG),
                                action = EmptyStateAction("回到最新", AppIcons.CalendarToday, DAILY_SCREEN_LATEST_TAG, doRefresh),
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .pageTurnViewportWithGuide(pageTurnTarget)
                            .testTag(DAILY_SCREEN_LIST_TAG),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        val topStories = viewModel.topStories
                        if (topStories.isNotEmpty()) {
                            item(key = "top_stories") {
                                HorizontalMultiBrowseCarousel(
                                    state = rememberCarouselState { topStories.size },
                                    preferredItemWidth = 320.dp,
                                    itemSpacing = 8.dp,
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .height(220.dp)
                                        .testTag(DAILY_SCREEN_TOP_STORIES_TAG),
                                ) { index ->
                                    val story = topStories[index]
                                    val info = carouselItemDrawInfo
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .maskClip(MaterialTheme.shapes.extraLarge)
                                            .clickable { openStory(story.id, story.url) }
                                            .testTag("daily_screen_top_story_${story.id}"),
                                    ) {
                                        AsyncImage(
                                            model = story.image,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        // 轮播两侧的小项被裁窄，标题在项宽不到一半时完全隐去，不在窄条里露出半截字。
                                        Column(
                                            Modifier
                                                .align(Alignment.BottomStart)
                                                .fillMaxWidth()
                                                .graphicsLayer {
                                                    alpha = if (info.maxSize > info.minSize) {
                                                        val expanded = (info.size - info.minSize) / (info.maxSize - info.minSize)
                                                        ((expanded - 0.5f) * 2f).coerceIn(0f, 1f)
                                                    } else {
                                                        1f
                                                    }
                                                }
                                                // 渐变在文字上方的留白里完成，文字背后至少 85% 黑：底图最亮时白字也有 4.5:1 的对比度。
                                                .background(Brush.verticalGradient(0f to Color.Transparent, 0.3f to Color.Black.copy(alpha = 0.85f)))
                                                .padding(start = 16.dp, top = 32.dp, end = 16.dp, bottom = 16.dp),
                                        ) {
                                            Text(
                                                story.title,
                                                style = MaterialTheme.typography.titleMediumEmphasized,
                                                color = Color.White,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                story.hint,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White.copy(alpha = 0.9f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        viewModel.sections.forEach { section ->
                            stickyHeader(key = "header_${section.date}") {
                                Text(
                                    formatDailyDate(section.date),
                                    style = MaterialTheme.typography.titleSmallEmphasized,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                        .padding(horizontal = 24.dp, vertical = 12.dp)
                                        .testTag("daily_screen_section_${section.date}"),
                                )
                            }
                            itemsIndexed(section.stories, key = { _, story -> "story_${story.id}" }) { index, story ->
                                GroupedListItem(
                                    index = index,
                                    count = section.stories.size,
                                    onClick = { openStory(story.id, story.url) },
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .testTag("daily_screen_story_${story.id}"),
                                    supportingContent = {
                                        Text(story.hint, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    },
                                    trailingContent = story.images.firstOrNull()?.let { image ->
                                        {
                                            AsyncImage(
                                                model = image,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .clip(MaterialTheme.shapes.medium),
                                            )
                                        }
                                    },
                                ) {
                                    Text(story.title, maxLines = 4, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }

                        // 底部加载指示器。
                        if (viewModel.isLoadingMore) {
                            item(key = "loading_more") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    AppLoadingIndicator(
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        } else {
                            viewModel.loadMoreError?.let { reason ->
                                item(key = "load_more_failed") {
                                    EmptyState(
                                        icon = AppIcons.Error,
                                        title = "加载失败",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag(DAILY_SCREEN_LOAD_MORE_ERROR_TAG),
                                        description = reason,
                                        action = EmptyStateAction("重试", AppIcons.Refresh, DAILY_SCREEN_LOAD_MORE_RETRY_TAG) {
                                            scope.launch { viewModel.loadMore(httpClient) }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (openingStoryId != null) {
                LinearProgressIndicator(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .testTag(DAILY_SCREEN_OPENING_STORY_TAG),
                )
            }
        }
    }
}

/** 日期选择器给出的是所选日期的 UTC 零点，按本地时区换算会在 UTC 以西的时区变成前一天。 */
private fun datePickerDate(utcMillis: Long): LocalDate = Instant.fromEpochMilliseconds(utcMillis).toLocalDateTime(TimeZone.UTC).date

/** 知乎日报从 2013-05-19 开始；更早的日期接口返回的不是日报数据，未来的日期会静默返回当天的日报。 */
@OptIn(ExperimentalMaterial3Api::class)
private object DailySelectableDates : SelectableDates {
    private val firstDate = LocalDate(2013, 5, 19)

    @OptIn(ExperimentalTime::class)
    private fun today() = Clock.System
        .now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    override fun isSelectableDate(utcTimeMillis: Long) = datePickerDate(utcTimeMillis) in firstDate..today()

    override fun isSelectableYear(year: Int) = year in firstDate.year..today().year
}

private fun formatDailyDatePickerSelection(millis: Long): String {
    val date = datePickerDate(millis)
    return date.year.toString().padStart(4, '0') +
        (date.month.ordinal + 1).twoDigitString() +
        date.day.twoDigitString()
}

private const val DAILY_SCREEN_TITLE_TAG = "daily_screen_title"
private const val DAILY_SCREEN_DATE_PICKER_BUTTON_TAG = "daily_screen_date_picker_button"
private const val DAILY_SCREEN_LOADING_TAG = "daily_screen_loading"
private const val DAILY_SCREEN_ERROR_TAG = "daily_screen_error"
private const val DAILY_SCREEN_RETRY_TAG = "daily_screen_retry"
private const val DAILY_SCREEN_EMPTY_TAG = "daily_screen_empty"
private const val DAILY_SCREEN_LATEST_TAG = "daily_screen_latest"
private const val DAILY_SCREEN_LOAD_MORE_ERROR_TAG = "daily_screen_load_more_error"
private const val DAILY_SCREEN_LOAD_MORE_RETRY_TAG = "daily_screen_load_more_retry"
private const val DAILY_SCREEN_OPENING_STORY_TAG = "daily_screen_opening_story"
private const val DAILY_SCREEN_LIST_TAG = "daily_screen_list"
private const val DAILY_SCREEN_TOP_STORIES_TAG = "daily_screen_top_stories"
