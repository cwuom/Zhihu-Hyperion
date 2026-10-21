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

package com.github.zly2006.zhihu.ui.subscreens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.glass.LIQUID_GLASS_PREFERENCE_KEY
import com.github.zly2006.zhihu.glass.isLiquidGlassEnabledByDefault
import com.github.zly2006.zhihu.glass.isLiquidGlassSupported
import com.github.zly2006.zhihu.icons.AppIcons
import com.github.zly2006.zhihu.icons.Icon
import com.github.zly2006.zhihu.markdown.TiqianBrandTitle
import com.github.zly2006.zhihu.markdown.isTiqianMarkdownRendererAvailable
import com.github.zly2006.zhihu.navigation.Account
import com.github.zly2006.zhihu.navigation.Daily
import com.github.zly2006.zhihu.navigation.Follow
import com.github.zly2006.zhihu.navigation.Home
import com.github.zly2006.zhihu.navigation.HotList
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.MyCollections
import com.github.zly2006.zhihu.navigation.OnlineHistory
import com.github.zly2006.zhihu.navigation.TopLevelDestination
import com.github.zly2006.zhihu.platform.ARTICLE_SWIPE_BACK_PREFERENCE_KEY
import com.github.zly2006.zhihu.platform.ENABLE_SEGMENT_ACTION_SHEET_PREFERENCE_KEY
import com.github.zly2006.zhihu.platform.SettingsStore
import com.github.zly2006.zhihu.platform.isAnswerSwipeSupported
import com.github.zly2006.zhihu.platform.isInAppBrowserSupported
import com.github.zly2006.zhihu.platform.isLegacyWebViewSupported
import com.github.zly2006.zhihu.platform.isPageTurnSupported
import com.github.zly2006.zhihu.platform.isTopLevelBackToLauncherSupported
import com.github.zly2006.zhihu.platform.platformBottomBarItemLimit
import com.github.zly2006.zhihu.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.theme.DUO3_TIQIAN_MARKDOWN_PREFERENCE_KEY
import com.github.zly2006.zhihu.theme.DUO3_TIQIAN_MATH_FONT_PREFERENCE_KEY
import com.github.zly2006.zhihu.theme.PREF_BLOCK_SPACING
import com.github.zly2006.zhihu.theme.PREF_FONT_SIZE
import com.github.zly2006.zhihu.theme.PREF_LINE_HEIGHT
import com.github.zly2006.zhihu.theme.ThemeManager
import com.github.zly2006.zhihu.theme.ThemeMode
import com.github.zly2006.zhihu.theme.isDynamicColorSupported
import com.github.zly2006.zhihu.ui.allBottomBarItems
import com.github.zly2006.zhihu.ui.article.ANSWER_DOUBLE_TAP_ACTION_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.article.ARTICLE_USE_WEBVIEW_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.article.AnswerDoubleTapAction
import com.github.zly2006.zhihu.ui.components.ANSWER_SWITCH_SENSITIVITY_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.components.ChoiceButtonGroup
import com.github.zly2006.zhihu.ui.components.ChoiceOption
import com.github.zly2006.zhihu.ui.components.ColorPickerDialog
import com.github.zly2006.zhihu.ui.components.DEFAULT_ANSWER_SWITCH_SENSITIVITY
import com.github.zly2006.zhihu.ui.components.DEFAULT_FAB_OPACITY
import com.github.zly2006.zhihu.ui.components.DEFAULT_FEED_CARD_STYLE
import com.github.zly2006.zhihu.ui.components.DEFAULT_PAGE_TURN_PERCENT
import com.github.zly2006.zhihu.ui.components.DEFAULT_PAGE_TURN_SWITCH_ANSWER
import com.github.zly2006.zhihu.ui.components.DEFAULT_SHOW_CONTENT_END_MARKER
import com.github.zly2006.zhihu.ui.components.DEFAULT_SHOW_PAGE_TURN_GUIDE
import com.github.zly2006.zhihu.ui.components.DISABLE_BOTTOM_SHEET_ROUNDED_CORNERS_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.components.FEED_CARD_STYLE_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.components.MAX_ANSWER_SWITCH_SENSITIVITY
import com.github.zly2006.zhihu.ui.components.MIN_ANSWER_SWITCH_SENSITIVITY
import com.github.zly2006.zhihu.ui.components.PREF_FAB_OPACITY
import com.github.zly2006.zhihu.ui.components.PREF_PAGE_TURN_PERCENT
import com.github.zly2006.zhihu.ui.components.PREF_PAGE_TURN_SWITCH_ANSWER
import com.github.zly2006.zhihu.ui.components.PREF_SHOW_CONTENT_END_MARKER
import com.github.zly2006.zhihu.ui.components.PREF_SHOW_PAGE_TURN_FAB
import com.github.zly2006.zhihu.ui.components.PREF_SHOW_PAGE_TURN_GUIDE
import com.github.zly2006.zhihu.ui.components.PREF_VOLUME_KEY_PAGE_TURN
import com.github.zly2006.zhihu.ui.components.SettingItem
import com.github.zly2006.zhihu.ui.components.SettingItemGroup
import com.github.zly2006.zhihu.ui.components.SettingItemOverall
import com.github.zly2006.zhihu.ui.components.SettingItemWithSwitch
import com.github.zly2006.zhihu.ui.components.normalizedAnswerSwitchSensitivity
import com.github.zly2006.zhihu.ui.components.pageTurnViewportWithGuide
import com.github.zly2006.zhihu.ui.components.rememberObservedSetting
import com.github.zly2006.zhihu.ui.components.rememberPageTurnTarget
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

const val ENABLE_PREDICTIVE_BACK_PREFERENCE_KEY = "enable_predictive_back"

const val DUO3_CARD_LARGE_TITLE_PREFERENCE_KEY = "duo3_card_large_title"
const val APPEARANCE_SETTINGS_SCROLL_TAG = "appearanceSettings.scroll"
const val APPEARANCE_SETTINGS_START_DESTINATION_TAG = "appearanceSettings.startDestination"
const val APPEARANCE_SETTINGS_ANSWER_DOUBLE_TAP_TAG = "appearanceSettings.answerDoubleTap"
const val APPEARANCE_SETTINGS_ANSWER_SWITCH_SENSITIVITY_TAG = "appearanceSettings.answerSwitchSensitivity"
const val APPEARANCE_SETTINGS_USE_WEBVIEW_TAG = "appearanceSettings.useWebView"
const val APPEARANCE_SETTINGS_TIQIAN_MARKDOWN_TAG = "appearanceSettings.tiqianMarkdown"
const val APPEARANCE_SETTINGS_WEBVIEW_FONT_TAG = "appearanceSettings.webViewFont"
const val APPEARANCE_SETTINGS_WEBVIEW_OPTIONS_TAG = "appearanceSettings.webViewOptions"
const val APPEARANCE_SETTINGS_BOTTOM_BAR_SECTION_KEY = "appearanceSettings.bottomBarSection"
const val APPEARANCE_SETTINGS_COLLECTION_DIRECT_BROWSE_TAG = "appearanceSettings.collectionDirectBrowse"
const val APPEARANCE_SETTINGS_DISABLE_BOTTOM_SHEET_ROUNDED_CORNERS_TAG = "appearanceSettings.disableBottomSheetRoundedCorners"
const val APPEARANCE_SETTINGS_LANDSCAPE_LIST_DETAIL_TAG = "appearanceSettings.landscapeListDetail"
const val APPEARANCE_SETTINGS_LIQUID_GLASS_TAG = "appearanceSettings.liquidGlass"

const val START_DESTINATION_PREFERENCE_KEY = "startDestination"
const val BOTTOM_BAR_ITEMS_PREFERENCE_KEY = "bottom_bar_items"
const val BOTTOM_BAR_ITEM_ORDER_PREFERENCE_KEY = "bottom_bar_item_order"
const val COLLECTION_DIRECT_BROWSE_PREFERENCE_KEY = "collectionDirectBrowse"
const val BOTTOM_BAR_SINGLE_TAP_REFRESH_PREFERENCE_KEY = "bottomBarSingleTapRefresh"
const val LANDSCAPE_LIST_DETAIL_PREFERENCE_KEY = "landscapeListDetail"
const val TOP_LEVEL_BACK_TO_LAUNCHER_PREFERENCE_KEY = "topLevelBackToLauncher"
const val CREATION_ENTRY_POSITION_PREFERENCE_KEY = "creationEntryPosition"

enum class CreationEntryPosition(
    val key: String,
    val label: String,
) {
    Floating("floating", "悬浮按钮"),
    Top("top", "顶部导航"),
    Bottom("bottom", "底部导航"),
}

private const val BOTTOM_BAR_ITEM_ORDER_SEPARATOR = ","
internal val contentFontSizeLevels = (50..120 step 5).toList() + (130..200 step 10).toList()
private val bottomBarSettingItemHeight = 64.dp
private val bottomBarSettingItemSpacing = 4.dp

private val topLevelDestinationsInOrder: List<Pair<String, TopLevelDestination>> = listOf(
    Home.name to Home,
    Follow.name to Follow,
    HotList.name to HotList,
    Daily.name to Daily,
    OnlineHistory.name to OnlineHistory,
    MyCollections.name to MyCollections,
    Account.name to Account,
)

internal fun navDestinationFromName(name: String): TopLevelDestination = topLevelDestinationsInOrder
    .firstOrNull { it.first == name }
    ?.second
    ?: Home

internal fun resolveValidStartDestinationKey(
    preferredKey: String?,
    availableKeysInOrder: List<String>,
): String = when {
    !preferredKey.isNullOrEmpty() && preferredKey in availableKeysInOrder -> preferredKey
    availableKeysInOrder.isNotEmpty() -> availableKeysInOrder.first()
    else -> Home.name
}

internal fun defaultBottomBarSelectionKeys(
    duo3HomeAccount: Boolean,
    maximumSelection: Int? = 5,
): Set<String> = if (maximumSelection == null) {
    if (duo3HomeAccount) {
        linkedSetOf(Home.name, Follow.name, HotList.name, Daily.name, OnlineHistory.name, MyCollections.name)
    } else {
        linkedSetOf(Home.name, Follow.name, HotList.name, Daily.name, OnlineHistory.name, MyCollections.name, Account.name)
    }
} else if (duo3HomeAccount) {
    linkedSetOf(Home.name, Follow.name, Daily.name)
} else {
    linkedSetOf(Home.name, Follow.name, Daily.name, OnlineHistory.name, Account.name)
}

internal fun normalizeBottomBarSelection(
    selectedKeys: Collection<String>,
    duo3HomeAccount: Boolean,
    enforceMinimumSelection: Boolean = false,
    maximumSelection: Int? = 5,
): Set<String> {
    val allowedKeys = topLevelDestinationsInOrder.map { it.first }.toSet()
    val normalized = selectedKeys
        .filterTo(linkedSetOf()) { it in allowedKeys }
        .ifEmpty { defaultBottomBarSelectionKeys(duo3HomeAccount, maximumSelection).toMutableSet() }

    if (duo3HomeAccount) {
        if (Home.name in normalized) {
            normalized.remove(Account.name)
        } else {
            normalized.add(Account.name)
        }
    } else {
        normalized.add(Account.name)
        while (maximumSelection != null && normalized.size > maximumSelection) {
            val removableKey = listOf(
                HotList.name,
                MyCollections.name,
                OnlineHistory.name,
                Daily.name,
                Follow.name,
                Home.name,
            ).firstOrNull { it in normalized } ?: break
            normalized.remove(removableKey)
        }
    }

    if (enforceMinimumSelection) {
        val fillOrder = if (duo3HomeAccount) {
            if (Home.name in normalized) {
                listOf(Follow.name, Daily.name, HotList.name, OnlineHistory.name)
            } else {
                listOf(Follow.name, Daily.name, HotList.name, OnlineHistory.name, Home.name)
            }
        } else {
            listOf(Home.name, Follow.name, Daily.name, HotList.name, OnlineHistory.name, MyCollections.name, Account.name)
        }
        fillOrder.forEach { key ->
            if (normalized.size < 3) {
                normalized.add(key)
            }
        }
    }

    return normalized
}

internal fun normalizeBottomBarItemOrder(
    preferredOrderKeys: List<String>,
    selectedKeys: Set<String>,
): List<String> {
    val allowedKeys = topLevelDestinationsInOrder.map { it.first }.toSet()
    val orderedKeys = mutableListOf<String>()
    preferredOrderKeys.forEach { key ->
        if (key in allowedKeys && key in selectedKeys && key !in orderedKeys) {
            orderedKeys.add(key)
        }
    }
    topLevelDestinationsInOrder.forEach { (key, _) ->
        if (key in selectedKeys && key !in orderedKeys) {
            orderedKeys.add(key)
        }
    }
    return orderedKeys
}

private fun bottomBarItemOrderPreferenceValue(keys: List<String>): String =
    keys.joinToString(BOTTOM_BAR_ITEM_ORDER_SEPARATOR)

internal fun bottomBarItemOrderFromPreference(
    preferenceValue: String?,
    selectedKeys: Set<String>,
): List<String> = normalizeBottomBarItemOrder(
    preferenceValue
        .orEmpty()
        .split(BOTTOM_BAR_ITEM_ORDER_SEPARATOR)
        .map { it.trim() }
        .filter { it.isNotEmpty() },
    selectedKeys,
)

internal fun shouldShowAccountHistoryShortcut(
    duo3HomeAccount: Boolean,
    selectedKeys: Set<String>,
): Boolean = duo3HomeAccount && OnlineHistory.name !in selectedKeys

/**
 * 外观与阅读体验设置页。
 *
 * 这里集中管理主题、字号/行高、信息流样式、文章页行为、底部导航栏、分享、搜索和技术性导航开关。页面支持通过 [setting]
 * 跳入指定设置项并高亮滚动到位，因此新增设置时应提供稳定的 `settingKey`，必要时也补充 test tag。
 *
 * 底部导航栏相关设置会影响 [com.github.zly2006.zhihu.ui.ZhihuMain] 的主壳状态；页面退出时必须通过 [onExit]
 * 触发上层重新读取设置，而不是直接重建 NavHost。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    setting: String?,
    onExit: () -> Unit,
) {
    val settingKey = setting.orEmpty()
    val settings = koinInject<SettingsStore>()
    val userMessages = rememberUserMessageSink()

    val scrollState = rememberScrollState()
    val pageTurnTarget = rememberPageTurnTarget(scrollState, enabled = true)
    val navigator = LocalNavigator.current

    val bringIntoViewRequesters = remember { mutableStateMapOf<String, BringIntoViewRequester>() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var scrolledSetting by remember { mutableStateOf<String?>(null) }

    fun requesterFor(settingKey: String): BringIntoViewRequester =
        bringIntoViewRequesters.getOrPut(settingKey) { BringIntoViewRequester() }
    val duo3HomeAccount = remember { mutableStateOf(settings.getBoolean("duo3_home_account", false)) }
    val selectedBottomBarItemKeys = remember {
        val normalizedSelection = normalizeBottomBarSelection(
            settings.getStringSet(
                BOTTOM_BAR_ITEMS_PREFERENCE_KEY,
                defaultBottomBarSelectionKeys(duo3HomeAccount.value, platformBottomBarItemLimit),
            ),
            duo3HomeAccount.value,
            enforceMinimumSelection = true,
            maximumSelection = platformBottomBarItemLimit,
        )
        mutableStateOf(
            bottomBarItemOrderFromPreference(
                settings.getStringOrNull(BOTTOM_BAR_ITEM_ORDER_PREFERENCE_KEY),
                normalizedSelection,
            ),
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            onExit()
        }
    }

    LaunchedEffect(settingKey, bringIntoViewRequesters[settingKey]) {
        if (settingKey.isNotEmpty() && scrolledSetting != settingKey) {
            bringIntoViewRequesters[settingKey]?.let { requester ->
                scrolledSetting = settingKey
                delay(200.milliseconds)
                // 收缩 LargeTopAppBar（programmatic scroll 不触发 nestedScroll）
                scrollBehavior.state.heightOffset = scrollBehavior.state.heightOffsetLimit
                requester.bringIntoView()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeTopAppBar(
                title = { Text("外观与阅读体验") },
                navigationIcon = {
                    IconButton(
                        onClick = navigator.onNavigateBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Icon(AppIcons.ArrowBack, contentDescription = "返回")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pageTurnViewportWithGuide(pageTurnTarget)
                .verticalScroll(scrollState)
                .testTag(APPEARANCE_SETTINGS_SCROLL_TAG)
                .padding(innerPadding)
                .padding(vertical = 16.dp),
        ) {
            val useDynamicColor = ThemeManager.getUseDynamicColor()
            val currentThemeMode = ThemeManager.getThemeMode()

            // ── 主题 ────────────────────────────────────────────────────────────

            SettingItemGroup(
                title = "主题",
            ) {
                SettingItem(
                    title = { Text("主题模式") },
                    icon = { Icon(AppIcons.Contrast, contentDescription = null) },
                    description = { Text("设置应用的显示主题。") },
                    settingKey = "nightMode",
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor("nightMode"),
                    bottomAction = {
                        val themeModes = listOf(
                            ChoiceOption(ThemeMode.SYSTEM, "自动", AppIcons.BrightnessAuto),
                            ChoiceOption(ThemeMode.LIGHT, "亮色", AppIcons.LightMode),
                            ChoiceOption(ThemeMode.DARK, "暗色", AppIcons.DarkMode),
                        )
                        ChoiceButtonGroup(
                            options = themeModes,
                            selected = currentThemeMode,
                            onSelect = { mode ->
                                ThemeManager.setThemeMode(mode)
                                settings.putString("themeMode", mode.name)
                                userMessages.showShortMessage("已切换到${themeModes.first { it.value == mode }.label}")
                            },
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    },
                )

                if (isDynamicColorSupported) {
                    SettingItemWithSwitch(
                        title = { Text("使用 Material You 动态取色") },
                        icon = { Icon(AppIcons.Wallpaper, contentDescription = null) },
                        description = { Text("根据系统壁纸自动提取主题色（Android 12+ 可用）。\n关闭后可以自己设定主题颜色。") },
                        checked = useDynamicColor,
                        onCheckedChange = {
                            ThemeManager.setUseDynamicColor(it)
                            settings.putBoolean("useDynamicColor", it)
                            userMessages.showShortMessage("已${if (it) "启用" else "禁用"}动态取色")
                        },
                        settingKey = "dynamicColor",
                        highlightedKey = settingKey,
                        bringIntoViewRequester = requesterFor("dynamicColor"),
                    )
                }

                var showColorPicker by remember { mutableStateOf(false) }
                val customColor = ThemeManager.getCustomColor()

                AnimatedVisibility(visible = !isDynamicColorSupported || !useDynamicColor) {
                    SettingItem(
                        title = { Text("自定义主题色") },
                        icon = { Icon(AppIcons.Palette, contentDescription = null) },
                        description = { Text("点击选择您喜欢的主题颜色") },
                        onClick = { showColorPicker = true },
                        endAction = {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(customColor)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                            )
                        },
                    )
                }
                if (showColorPicker) {
                    ColorPickerDialog(
                        title = "选择主题色",
                        initialColor = customColor,
                        onDismiss = { showColorPicker = false },
                        onColorSelected = { color ->
                            ThemeManager.setCustomColor(color)
                            settings.putInt("customThemeColor", color.toArgb())
                            userMessages.showShortMessage("主题色已保存")
                            showColorPicker = false
                        },
                    )
                }

                if (isInAppBrowserSupported) {
                    var showLuotianYiColorPicker by remember { mutableStateOf(false) }
                    val luotianYiColor = remember {
                        Color(settings.getInt("luotianyi_color", 0xff_66CCFF.toInt()))
                    }

                    SettingItem(
                        title = { Text("唤起浏览器主题色") },
                        icon = { Icon(AppIcons.FormatColorFill, contentDescription = null) },
                        description = { Text("应用内浏览器的工具栏颜色") },
                        onClick = { showLuotianYiColorPicker = true },
                        endAction = {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(luotianYiColor)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                            )
                        },
                    )

                    if (showLuotianYiColorPicker) {
                        ColorPickerDialog(
                            title = "选择浏览器主题色",
                            initialColor = luotianYiColor,
                            presetColors = listOf(
                                Color(0xFF66CCFF),
                                Color(0xFF2196F3),
                                Color(0xFF4CAF50),
                                Color(0xFFF44336),
                                Color(0xFFFF9800),
                                Color(0xFF9C27B0),
                            ),
                            onDismiss = { showLuotianYiColorPicker = false },
                            onColorSelected = { color ->
                                settings.putInt("luotianyi_color", color.toArgb())
                                userMessages.showShortMessage("浏览器主题色已保存")
                                showLuotianYiColorPicker = false
                            },
                        )
                    }
                }

                val currentIsDarkTheme = ThemeManager.isDarkTheme()
                var showBackgroundColorPicker by remember { mutableStateOf(false) }
                val backgroundColor = ThemeManager.getBackgroundColor()

                SettingItem(
                    title = { Text("自定义背景颜色") },
                    icon = { Icon(AppIcons.FormatPaint, contentDescription = null) },
                    description = { Text(if (currentIsDarkTheme) "深色模式背景色" else "浅色模式背景色") },
                    onClick = { showBackgroundColorPicker = true },
                    endAction = {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(backgroundColor)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                        )
                    },
                )

                if (showBackgroundColorPicker) {
                    ColorPickerDialog(
                        title = "选择背景颜色",
                        initialColor = backgroundColor,
                        presetColors = listOfNotNull(
                            Color(if (currentIsDarkTheme) 0xFF121212.toInt() else 0xFFFFFFFF.toInt()),
                            MaterialTheme.colorScheme.surfaceContainer,
                            if (ThemeManager.isDarkTheme()) Color.Black else null,
                        ),
                        onDismiss = { showBackgroundColorPicker = false },
                        onColorSelected = { color ->
                            ThemeManager.setBackgroundColor(color, currentIsDarkTheme)
                            settings.putInt(
                                if (currentIsDarkTheme) "backgroundColorDark" else "backgroundColorLight",
                                color.toArgb(),
                            )
                            userMessages.showShortMessage("背景颜色已保存")
                            showBackgroundColorPicker = false
                        },
                    )
                }

                val disableBottomSheetRoundedCorners = remember {
                    mutableStateOf(settings.getBoolean(DISABLE_BOTTOM_SHEET_ROUNDED_CORNERS_PREFERENCE_KEY, false))
                }
                SettingItemWithSwitch(
                    modifier = Modifier.testTag(APPEARANCE_SETTINGS_DISABLE_BOTTOM_SHEET_ROUNDED_CORNERS_TAG),
                    title = { Text("禁用 popup 圆角") },
                    icon = { Icon(AppIcons.RoundedCorner, contentDescription = null) },
                    description = { Text("开启后，评论等 popup 顶部不再显示圆角。") },
                    checked = disableBottomSheetRoundedCorners.value,
                    onCheckedChange = {
                        disableBottomSheetRoundedCorners.value = it
                        settings.putBoolean(DISABLE_BOTTOM_SHEET_ROUNDED_CORNERS_PREFERENCE_KEY, it)
                    },
                    settingKey = DISABLE_BOTTOM_SHEET_ROUNDED_CORNERS_PREFERENCE_KEY,
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor(DISABLE_BOTTOM_SHEET_ROUNDED_CORNERS_PREFERENCE_KEY),
                )

                if (isLiquidGlassSupported) {
                    var liquidGlass by remember {
                        mutableStateOf(settings.getBoolean(LIQUID_GLASS_PREFERENCE_KEY, isLiquidGlassEnabledByDefault))
                    }
                    SettingItemWithSwitch(
                        modifier = Modifier.testTag(APPEARANCE_SETTINGS_LIQUID_GLASS_TAG),
                        title = { Text("液态玻璃") },
                        icon = { Icon(AppIcons.BlurOn, contentDescription = null) },
                        description = { Text("底部导航栏和回答页底部操作栏改为悬浮的液态玻璃，透出并折射下方内容。") },
                        checked = liquidGlass,
                        onCheckedChange = {
                            liquidGlass = it
                            settings.putBoolean(LIQUID_GLASS_PREFERENCE_KEY, it)
                        },
                        settingKey = LIQUID_GLASS_PREFERENCE_KEY,
                        highlightedKey = settingKey,
                        bringIntoViewRequester = requesterFor(LIQUID_GLASS_PREFERENCE_KEY),
                    )
                }

                var fabOpacity by remember {
                    mutableIntStateOf(settings.getInt(PREF_FAB_OPACITY, DEFAULT_FAB_OPACITY))
                }
                SettingItem(
                    title = { Text("悬浮按钮透明度") },
                    icon = { Icon(AppIcons.Opacity, contentDescription = null) },
                    description = { Text("控制所有悬浮按钮的透明度 ($fabOpacity%)。") },
                    bottomAction = {
                        Slider(
                            value = fabOpacity.toFloat(),
                            onValueChange = {
                                val v = (it / 5).roundToInt() * 5
                                fabOpacity = v
                                settings.putInt(PREF_FAB_OPACITY, v)
                            },
                            valueRange = 10f..100f,
                            steps = 17,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                    },
                )

                val landscapeListDetailEnabled = remember {
                    mutableStateOf(settings.getBoolean(LANDSCAPE_LIST_DETAIL_PREFERENCE_KEY, true))
                }
                SettingItemWithSwitch(
                    modifier = Modifier.testTag(APPEARANCE_SETTINGS_LANDSCAPE_LIST_DETAIL_TAG),
                    title = { Text("横屏双栏布局") },
                    icon = { Icon(AppIcons.VerticalSplit, contentDescription = null) },
                    description = { Text("在平板和电脑横屏时同时显示列表与详情。手机横屏始终使用单栏。") },
                    checked = landscapeListDetailEnabled.value,
                    onCheckedChange = {
                        landscapeListDetailEnabled.value = it
                        settings.putBoolean(LANDSCAPE_LIST_DETAIL_PREFERENCE_KEY, it)
                    },
                    settingKey = LANDSCAPE_LIST_DETAIL_PREFERENCE_KEY,
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor(LANDSCAPE_LIST_DETAIL_PREFERENCE_KEY),
                )
            }
            // ── 阅读 ────────────────────────────────────────────────────────────
            SettingItemGroup(
                title = "阅读",
            ) {
                var fontSize by remember {
                    val storedValue = settings.getInt(PREF_FONT_SIZE, 100)
                    val boundedValue = storedValue.coerceIn(
                        contentFontSizeLevels.first(),
                        contentFontSizeLevels.last(),
                    )
                    val snappedValue = contentFontSizeLevels.minBy { abs(it - boundedValue) }
                    if (storedValue != snappedValue) {
                        settings.putInt(PREF_FONT_SIZE, snappedValue)
                    }
                    mutableIntStateOf(snappedValue)
                }
                SettingItem(
                    title = { Text("字号") },
                    icon = { Icon(AppIcons.FormatSize, contentDescription = null) },
                    description = { Text("调整内容文字大小 ($fontSize%)") },
                    settingKey = "fontScale",
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor("fontScale"),
                    bottomAction = {
                        Slider(
                            value = contentFontSizeLevels.indexOf(fontSize).toFloat(),
                            onValueChange = {
                                val levelIndex = it.roundToInt().coerceIn(0, contentFontSizeLevels.lastIndex)
                                fontSize = contentFontSizeLevels[levelIndex]
                                settings.putInt(PREF_FONT_SIZE, fontSize)
                            },
                            valueRange = 0f..contentFontSizeLevels.lastIndex.toFloat(),
                            steps = contentFontSizeLevels.size - 2,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                    },
                )

                var lineHeight by remember { mutableIntStateOf(settings.getInt(PREF_LINE_HEIGHT, 160)) }
                SettingItem(
                    title = { Text("行高") },
                    icon = { Icon(AppIcons.FormatLineSpacing, contentDescription = null) },
                    description = { Text("调整内容行间距 (${lineHeight / 100f})") },
                    bottomAction = {
                        Slider(
                            value = lineHeight.toFloat(),
                            onValueChange = {
                                lineHeight = it.toInt()
                                settings.putInt(PREF_LINE_HEIGHT, it.toInt())
                            },
                            valueRange = 100f..300f,
                            steps = 19,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                    },
                )

                var blockSpacing by remember { mutableIntStateOf(settings.getInt(PREF_BLOCK_SPACING, 100)) }
                SettingItem(
                    title = { Text("段间距") },
                    icon = { Icon(AppIcons.FormatParagraph, contentDescription = null) },
                    description = { Text("调整正文段落和块级内容间距 ($blockSpacing%)") },
                    settingKey = PREF_BLOCK_SPACING,
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor(PREF_BLOCK_SPACING),
                    bottomAction = {
                        Slider(
                            value = blockSpacing.toFloat(),
                            onValueChange = {
                                val spacing = (it / 10).roundToInt() * 10
                                blockSpacing = spacing
                                settings.putInt(PREF_BLOCK_SPACING, spacing)
                            },
                            valueRange = 0f..300f,
                            steps = 29,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                    },
                )
            }

            // ── 信息流 ──────────────────────────────────────────────────────────
            val showRefreshFab = remember { mutableStateOf(settings.getBoolean("showRefreshFab", true)) }
            SettingItemGroup(
                title = "信息流",
            ) {
                val creationPosition by rememberObservedSetting(settings, CREATION_ENTRY_POSITION_PREFERENCE_KEY) {
                    getString(CREATION_ENTRY_POSITION_PREFERENCE_KEY, CreationEntryPosition.Floating.key)
                }
                SettingItem(
                    title = { Text("发布入口位置") },
                    icon = { Icon(AppIcons.Add, contentDescription = null) },
                    description = { Text("将发布入口放入导航栏，减少对阅读区域的遮挡。切换后立即生效。") },
                    settingKey = CREATION_ENTRY_POSITION_PREFERENCE_KEY,
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor(CREATION_ENTRY_POSITION_PREFERENCE_KEY),
                    modifier = Modifier.testTag("appearanceSettings.creationEntryPosition"),
                    bottomAction = {
                        ChoiceButtonGroup(
                            options = CreationEntryPosition.entries.map { ChoiceOption(it.key, it.label) },
                            selected = creationPosition,
                            onSelect = { settings.putString(CREATION_ENTRY_POSITION_PREFERENCE_KEY, it) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                )
                val showFeedThumbnail = remember { mutableStateOf(settings.getBoolean("showFeedThumbnail", true)) }
                SettingItemWithSwitch(
                    title = { Text("显示 Feed 卡片缩略图") },
                    icon = { Icon(AppIcons.Image, contentDescription = null) },
                    description = { Text("在信息流卡片中显示文章缩略图。") },
                    checked = showFeedThumbnail.value,
                    onCheckedChange = {
                        showFeedThumbnail.value = it
                        settings.putBoolean("showFeedThumbnail", it)
                    },
                    settingKey = "showFeedThumbnail",
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor("showFeedThumbnail"),
                )

                SettingItemWithSwitch(
                    title = { Text("显示刷新悬浮按钮") },
                    icon = { Icon(AppIcons.Refresh, contentDescription = null) },
                    description = { Text("在页面上显示可拖动的刷新按钮。") },
                    checked = showRefreshFab.value,
                    onCheckedChange = {
                        showRefreshFab.value = it
                        settings.putBoolean("showRefreshFab", it)
                    },
                    settingKey = "showRefreshFab",
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor("showRefreshFab"),
                )

                val feedCardStyle = remember {
                    mutableStateOf(settings.getString(FEED_CARD_STYLE_PREFERENCE_KEY, DEFAULT_FEED_CARD_STYLE))
                }
                SettingItem(
                    title = { Text("信息流样式") },
                    icon = { Icon(AppIcons.ViewAgenda, contentDescription = null) },
                    description = { Text("卡片样式使用圆角卡片展示，分割线样式使用细线分隔条目。") },
                    settingKey = FEED_CARD_STYLE_PREFERENCE_KEY,
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor(FEED_CARD_STYLE_PREFERENCE_KEY),
                    bottomAction = {
                        val feedCardStyleOptions = listOf(
                            ChoiceOption("card", "卡片样式", AppIcons.ViewDay),
                            ChoiceOption("divider", "分割线样式", AppIcons.ViewHeadline),
                        )
                        ChoiceButtonGroup(
                            options = feedCardStyleOptions,
                            selected = feedCardStyle.value,
                            onSelect = { mode ->
                                feedCardStyle.value = mode
                                settings.putString(FEED_CARD_STYLE_PREFERENCE_KEY, mode)
                                userMessages.showShortMessage("已设置为：${feedCardStyleOptions.first { it.value == mode }.label}")
                            },
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    },
                )
            }

            // ── 回答页 ──────────────────────────────────────────────────────────
            val buttonSkipAnswer = remember { mutableStateOf(settings.getBoolean("buttonSkipAnswer", true)) }
            SettingItemGroup(
                title = "回答页",
            ) {
                val articleUseWebview = remember {
                    mutableStateOf(settings.getBoolean(ARTICLE_USE_WEBVIEW_PREFERENCE_KEY, false))
                }
                SettingItemWithSwitch(
                    modifier = Modifier.testTag(APPEARANCE_SETTINGS_USE_WEBVIEW_TAG),
                    title = { Text("使用 WebView 显示文章") },
                    icon = { Icon(AppIcons.Web, contentDescription = null) },
                    description = { Text("关闭后使用 Compose 渲染，支持代码高亮等高级功能。警告：这个渲染模式不再推荐，非专业人士请不要开启！") },
                    checked = articleUseWebview.value,
                    enabled = isLegacyWebViewSupported,
                    onCheckedChange = {
                        articleUseWebview.value = it
                        settings.putBoolean(ARTICLE_USE_WEBVIEW_PREFERENCE_KEY, it)
                    },
                    settingKey = ARTICLE_USE_WEBVIEW_PREFERENCE_KEY,
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor(ARTICLE_USE_WEBVIEW_PREFERENCE_KEY),
                )

                if (articleUseWebview.value && isLegacyWebViewSupported) {
                    var customFontName by remember {
                        mutableStateOf(settings.getStringOrNull("webviewCustomFontName"))
                    }
                    Column(
                        modifier = Modifier.testTag(APPEARANCE_SETTINGS_WEBVIEW_OPTIONS_TAG),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (isWebViewCustomFontSupported) {
                            SettingItem(
                                modifier = Modifier.testTag(APPEARANCE_SETTINGS_WEBVIEW_FONT_TAG),
                                title = {
                                    Text(
                                        "WebView 自定义字体",
                                        modifier = Modifier.testTag(APPEARANCE_SETTINGS_WEBVIEW_FONT_TAG),
                                    )
                                },
                                icon = { Icon(AppIcons.FontDownload, contentDescription = null) },
                                description = { Text(customFontName ?: "未设置") },
                                bottomAction = {
                                    WebViewCustomFontSettings(
                                        customFontName = customFontName,
                                        onCustomFontNameChange = { name ->
                                            if (name == null) {
                                                settings.remove("webviewCustomFontName")
                                            } else {
                                                settings.putString("webviewCustomFontName", name)
                                            }
                                            customFontName = name
                                        },
                                    )
                                },
                            )
                        }

                        val useHardwareAcceleration = remember { mutableStateOf(settings.getBoolean("webviewHardwareAcceleration", true)) }
                        SettingItemWithSwitch(
                            title = { Text("WebView 硬件加速") },
                            icon = { Icon(AppIcons.Speed, contentDescription = null) },
                            description = { Text("提高渲染性能，可能导致兼容性问题。") },
                            checked = useHardwareAcceleration.value,
                            onCheckedChange = {
                                useHardwareAcceleration.value = it
                                settings.putBoolean("webviewHardwareAcceleration", it)
                            },
                        )
                    }
                }

                val isTitleAutoHide = remember { mutableStateOf(settings.getBoolean("titleAutoHide", false)) }
                SettingItemWithSwitch(
                    title = { Text("自动隐藏回答标题") },
                    icon = { Icon(AppIcons.Title, contentDescription = null) },
                    description = { Text("滚动时自动隐藏回答标题栏。") },
                    checked = isTitleAutoHide.value,
                    onCheckedChange = {
                        isTitleAutoHide.value = it
                        settings.putBoolean("titleAutoHide", it)
                    },
                    settingKey = "titleAutoHide",
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor("titleAutoHide"),
                )

                var segmentActionsEnabled by remember {
                    mutableStateOf(settings.getBoolean(ENABLE_SEGMENT_ACTION_SHEET_PREFERENCE_KEY, true))
                }
                SettingItemWithSwitch(
                    modifier = Modifier.testTag("appearance_settings_segment_action_sheet"),
                    title = { Text("点击划线显示弹窗") },
                    icon = { Icon(AppIcons.TouchApp, contentDescription = null) },
                    description = { Text("关闭后保留段评划线，点击时不弹出操作面板；长按选择文字不受影响。仅适用于 Compose 正文。") },
                    checked = segmentActionsEnabled,
                    onCheckedChange = {
                        segmentActionsEnabled = it
                        settings.putBoolean(ENABLE_SEGMENT_ACTION_SHEET_PREFERENCE_KEY, it)
                    },
                    settingKey = ENABLE_SEGMENT_ACTION_SHEET_PREFERENCE_KEY,
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor(ENABLE_SEGMENT_ACTION_SHEET_PREFERENCE_KEY),
                )

                var articleSwipeBack by remember {
                    mutableStateOf(settings.getBoolean(ARTICLE_SWIPE_BACK_PREFERENCE_KEY, false))
                }
                SettingItemWithSwitch(
                    modifier = Modifier.testTag("appearance_settings_article_swipe_back"),
                    title = { Text("全屏右滑返回") },
                    icon = { Icon(AppIcons.Swipe, contentDescription = null) },
                    description = { Text("在回答或文章页面从屏幕任意位置向右滑动返回。默认关闭；左右切换回答时优先保留切换手势。仅适用于 Compose 正文。") },
                    checked = articleSwipeBack,
                    onCheckedChange = {
                        articleSwipeBack = it
                        settings.putBoolean(ARTICLE_SWIPE_BACK_PREFERENCE_KEY, it)
                    },
                    settingKey = ARTICLE_SWIPE_BACK_PREFERENCE_KEY,
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor(ARTICLE_SWIPE_BACK_PREFERENCE_KEY),
                )

                val autoHideArticleBottomBar = remember {
                    mutableStateOf(settings.getBoolean("autoHideArticleBottomBar", false))
                }
                SettingItemWithSwitch(
                    title = { Text("自动隐藏回答底部按钮") },
                    icon = { Icon(AppIcons.CallToAction, contentDescription = null) },
                    description = { Text("上划时隐藏回答底部操作按钮，下划时重新显示。") },
                    checked = autoHideArticleBottomBar.value,
                    onCheckedChange = {
                        autoHideArticleBottomBar.value = it
                        settings.putBoolean("autoHideArticleBottomBar", it)
                    },
                    settingKey = "autoHideArticleBottomBar",
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor("autoHideArticleBottomBar"),
                )

                SettingItemWithSwitch(
                    title = { Text("显示跳转下一个回答按钮") },
                    icon = { Icon(AppIcons.KeyboardDoubleArrowDown, contentDescription = null) },
                    description = { Text("在回答页面显示可拖动的快速跳转按钮。") },
                    checked = buttonSkipAnswer.value,
                    onCheckedChange = {
                        buttonSkipAnswer.value = it
                        settings.putBoolean("buttonSkipAnswer", it)
                    },
                    settingKey = "buttonSkipAnswer",
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor("buttonSkipAnswer"),
                )

                val autoHideSkipAnswerButton = remember { mutableStateOf(settings.getBoolean("autoHideSkipAnswerButton", true)) }
                AnimatedVisibility(buttonSkipAnswer.value) {
                    SettingItemWithSwitch(
                        title = { Text("滚动时自动隐藏跳转按钮") },
                        icon = { Icon(AppIcons.SwipeVertical, contentDescription = null) },
                        description = { Text("上划时淡出「下一个回答」按钮，下划时淡入显示。") },
                        checked = autoHideSkipAnswerButton.value,
                        onCheckedChange = {
                            autoHideSkipAnswerButton.value = it
                            settings.putBoolean("autoHideSkipAnswerButton", it)
                        },
                    )
                }

                val pinAnswerDate = remember { mutableStateOf(settings.getBoolean("pinAnswerDate", false)) }
                SettingItemWithSwitch(
                    title = { Text("置顶回答日期") },
                    icon = { Icon(AppIcons.CalendarToday, contentDescription = null) },
                    description = { Text("将回答的发布日期和编辑日期移动到内容最前面显示。") },
                    checked = pinAnswerDate.value,
                    onCheckedChange = {
                        pinAnswerDate.value = it
                        settings.putBoolean("pinAnswerDate", it)
                    },
                    settingKey = "pinAnswerDate",
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor("pinAnswerDate"),
                )

                val answerSwitchMode = remember {
                    mutableStateOf(settings.getString("answerSwitchMode", "vertical"))
                }
                if (isAnswerSwipeSupported) {
                    SettingItem(
                        title = { Text("回答切换手势") },
                        icon = { Icon(AppIcons.Swipe, contentDescription = null) },
                        description = { Text("在回答页面通过手势切换同一问题下的其他回答。") },
                        settingKey = "answerSwitchMode",
                        highlightedKey = settingKey,
                        bringIntoViewRequester = requesterFor("answerSwitchMode"),
                        bottomAction = {
                            val answerSwitchOptions = listOf(
                                ChoiceOption("off", "关闭"),
                                ChoiceOption("vertical", "上下滑动"),
                                ChoiceOption("horizontal", "左右滑动"),
                            )
                            ChoiceButtonGroup(
                                options = answerSwitchOptions,
                                selected = answerSwitchMode.value,
                                onSelect = { mode ->
                                    answerSwitchMode.value = mode
                                    settings.putString("answerSwitchMode", mode)
                                    userMessages.showShortMessage("已设置为：${answerSwitchOptions.first { it.value == mode }.label}")
                                },
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        },
                    )
                }

                var answerSwitchSensitivity by remember {
                    mutableStateOf(
                        normalizedAnswerSwitchSensitivity(
                            settings.getFloat(
                                ANSWER_SWITCH_SENSITIVITY_PREFERENCE_KEY,
                                DEFAULT_ANSWER_SWITCH_SENSITIVITY,
                            ),
                        ),
                    )
                }
                AnimatedVisibility(isAnswerSwipeSupported && answerSwitchMode.value != "off") {
                    SettingItem(
                        title = { Text("回答切换灵敏度") },
                        icon = { Icon(AppIcons.Tune, contentDescription = null) },
                        description = {
                            Text("当前 ${(answerSwitchSensitivity * 10).roundToInt() / 10f}x，数值越高，滑动越短；同时作用于上下和左右切换。")
                        },
                        settingKey = ANSWER_SWITCH_SENSITIVITY_PREFERENCE_KEY,
                        highlightedKey = settingKey,
                        bringIntoViewRequester = requesterFor(ANSWER_SWITCH_SENSITIVITY_PREFERENCE_KEY),
                        bottomAction = {
                            Slider(
                                value = answerSwitchSensitivity,
                                onValueChange = {
                                    val sensitivity = (it * 10).roundToInt() / 10f
                                    answerSwitchSensitivity = sensitivity
                                    settings.putFloat(ANSWER_SWITCH_SENSITIVITY_PREFERENCE_KEY, sensitivity)
                                },
                                valueRange = MIN_ANSWER_SWITCH_SENSITIVITY..MAX_ANSWER_SWITCH_SENSITIVITY,
                                steps = 24,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .testTag(APPEARANCE_SETTINGS_ANSWER_SWITCH_SENSITIVITY_TAG),
                            )
                        },
                    )
                }

                var answerDoubleTapExpanded by remember { mutableStateOf(false) }
                val answerDoubleTapAction = remember {
                    mutableStateOf(
                        AnswerDoubleTapAction.fromPreference(
                            settings.getString(
                                ANSWER_DOUBLE_TAP_ACTION_PREFERENCE_KEY,
                                AnswerDoubleTapAction.Ask.preferenceValue,
                            ),
                        ),
                    )
                }
                SettingItem(
                    title = { Text("双击回答动作") },
                    icon = { Icon(AppIcons.TouchApp, contentDescription = null) },
                    description = { Text("双击回答正文时执行的动作。默认弹窗询问。") },
                    settingKey = ANSWER_DOUBLE_TAP_ACTION_PREFERENCE_KEY,
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor(ANSWER_DOUBLE_TAP_ACTION_PREFERENCE_KEY),
                    bottomAction = {
                        ExposedDropdownMenuBox(
                            expanded = answerDoubleTapExpanded,
                            onExpandedChange = { answerDoubleTapExpanded = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        ) {
                            OutlinedTextField(
                                value = answerDoubleTapAction.value.label,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = answerDoubleTapExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag(APPEARANCE_SETTINGS_ANSWER_DOUBLE_TAP_TAG),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            )
                            ExposedDropdownMenu(
                                expanded = answerDoubleTapExpanded,
                                onDismissRequest = { answerDoubleTapExpanded = false },
                            ) {
                                AnswerDoubleTapAction.entries.forEach { action ->
                                    DropdownMenuItem(
                                        text = { Text(action.label) },
                                        trailingIcon = if (action == answerDoubleTapAction.value) {
                                            { Icon(AppIcons.Check, contentDescription = "当前设置") }
                                        } else {
                                            null
                                        },
                                        leadingIcon = {
                                            Icon(
                                                when (action) {
                                                    AnswerDoubleTapAction.None -> AppIcons.Block
                                                    AnswerDoubleTapAction.Ask -> AppIcons.Help
                                                    AnswerDoubleTapAction.VoteUp -> AppIcons.ThumbUp
                                                    AnswerDoubleTapAction.OpenComments -> AppIcons.Comment
                                                    AnswerDoubleTapAction.ToggleImmersive -> AppIcons.Fullscreen
                                                },
                                                contentDescription = null,
                                            )
                                        },
                                        onClick = {
                                            answerDoubleTapAction.value = action
                                            settings.putString(
                                                ANSWER_DOUBLE_TAP_ACTION_PREFERENCE_KEY,
                                                action.preferenceValue,
                                            )
                                            answerDoubleTapExpanded = false
                                            userMessages.showShortMessage("已设置为：${action.label}")
                                        },
                                    )
                                }
                            }
                        }
                    },
                )
            }

            // ── 底部导航栏 ──────────────────────────────────────────────────────
            val bottomBarItemsByKey = allBottomBarItems.associateBy { it.destination.name }
            var startDestinationExpanded by remember { mutableStateOf(false) }
            var startDestinationKey by remember {
                mutableStateOf(
                    resolveValidStartDestinationKey(
                        settings.getString(START_DESTINATION_PREFERENCE_KEY, Home.name),
                        allBottomBarItems.map { it.destination.name }.filter { it in selectedBottomBarItemKeys.value },
                    ),
                )
            }

            fun persistBottomBarSelection(
                currentOrderKeys: List<String>,
                duo3HomeAccountEnabled: Boolean = duo3HomeAccount.value,
            ) {
                val normalizedSet = normalizeBottomBarSelection(
                    currentOrderKeys,
                    duo3HomeAccountEnabled,
                    enforceMinimumSelection = true,
                    maximumSelection = platformBottomBarItemLimit,
                )
                val normalizedOrderKeys = normalizeBottomBarItemOrder(currentOrderKeys, normalizedSet)
                val availableKeys = normalizedOrderKeys
                val resolvedStartDestination = resolveValidStartDestinationKey(startDestinationKey, availableKeys)
                selectedBottomBarItemKeys.value = normalizedOrderKeys
                startDestinationKey = resolvedStartDestination
                settings.putStringSet(BOTTOM_BAR_ITEMS_PREFERENCE_KEY, normalizedSet)
                settings.putString(
                    BOTTOM_BAR_ITEM_ORDER_PREFERENCE_KEY,
                    bottomBarItemOrderPreferenceValue(normalizedOrderKeys),
                )
                settings.putString(START_DESTINATION_PREFERENCE_KEY, resolvedStartDestination)
            }

            fun moveBottomBarItem(key: String, offset: Int) {
                val currentOrderKeys = selectedBottomBarItemKeys.value
                val fromIndex = currentOrderKeys.indexOf(key)
                val toIndex = fromIndex + offset
                if (fromIndex < 0 || toIndex !in currentOrderKeys.indices) {
                    return
                }
                val reorderedKeys = currentOrderKeys.toMutableList()
                reorderedKeys.removeAt(fromIndex)
                reorderedKeys.add(toIndex, key)
                persistBottomBarSelection(reorderedKeys)
            }

            SettingItemGroup(
                title = "底部导航栏",
                settingKey = APPEARANCE_SETTINGS_BOTTOM_BAR_SECTION_KEY,
                highlightedKey = settingKey,
                bringIntoViewRequester = requesterFor(APPEARANCE_SETTINGS_BOTTOM_BAR_SECTION_KEY),
            ) {
                val selectedBottomBarItemKeySet = selectedBottomBarItemKeys.value.toSet()
                val startDestinationItems = selectedBottomBarItemKeys.value.mapNotNull { bottomBarItemsByKey[it] }
                val orderedSettingItems = startDestinationItems +
                    allBottomBarItems.filter { it.destination.name !in selectedBottomBarItemKeySet }

                SettingItem(
                    title = { Text("应用启动默认页面") },
                    icon = { Icon(AppIcons.RocketLaunch, contentDescription = null) },
                    description = { Text("仅可选择已在底部导航栏中显示的页面。") },
                    bottomAction = {
                        ExposedDropdownMenuBox(
                            expanded = startDestinationExpanded,
                            onExpandedChange = {
                                if (startDestinationItems.isNotEmpty()) {
                                    startDestinationExpanded = it
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        ) {
                            OutlinedTextField(
                                value = startDestinationItems.find { it.destination.name == startDestinationKey }?.label ?: "主页",
                                onValueChange = {},
                                readOnly = true,
                                enabled = startDestinationItems.isNotEmpty(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startDestinationExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag(APPEARANCE_SETTINGS_START_DESTINATION_TAG),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            )
                            ExposedDropdownMenu(
                                expanded = startDestinationExpanded,
                                onDismissRequest = { startDestinationExpanded = false },
                            ) {
                                startDestinationItems.forEach { item ->
                                    val key = item.destination.name
                                    DropdownMenuItem(
                                        modifier = Modifier.testTag("appearanceSettings:startDestination:option:$key"),
                                        text = { Text(item.label) },
                                        trailingIcon = if (key == startDestinationKey) {
                                            { Icon(AppIcons.Check, contentDescription = "当前设置") }
                                        } else {
                                            null
                                        },
                                        leadingIcon = { Icon(item.icon, contentDescription = null) },
                                        onClick = {
                                            startDestinationKey = key
                                            settings.putString(START_DESTINATION_PREFERENCE_KEY, key)
                                            startDestinationExpanded = false
                                            userMessages.showShortMessage("已设置启动页：${item.label}，重启后生效")
                                        },
                                    )
                                }
                            }
                        }
                    },
                )

                SettingItem(
                    title = { Text("选择要在底部栏显示的页面") },
                    icon = { Icon(AppIcons.BottomNavigation, contentDescription = null) },
                    description = {
                        Text("建议选择 3-5 项，可用箭头调整显示和滑动顺序。")
                    },
                    bottomAction = {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    8.dp +
                                        bottomBarSettingItemHeight * orderedSettingItems.size +
                                        bottomBarSettingItemSpacing * (orderedSettingItems.size - 1).coerceAtLeast(0),
                                ).padding(top = 8.dp),
                            userScrollEnabled = false,
                            verticalArrangement = Arrangement.spacedBy(bottomBarSettingItemSpacing),
                        ) {
                            items(
                                items = orderedSettingItems,
                                key = { it.destination.name },
                            ) { item ->
                                val key = item.destination.name
                                val label = item.label
                                val isChecked = selectedBottomBarItemKeys.value.contains(key)
                                val selectedIndex = selectedBottomBarItemKeys.value.indexOf(key)
                                val candidateOrderKeys = if (isChecked) {
                                    selectedBottomBarItemKeys.value.filter { it != key }
                                } else {
                                    selectedBottomBarItemKeys.value + key
                                }
                                val isEnabled = key != Account.name

                                Row(
                                    modifier = Modifier
                                        .animateItem(
                                            fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                            fadeOutSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                            placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                        ).testTag("appearanceSettings:bottomBar:item:$key")
                                        .fillMaxWidth()
                                        .height(bottomBarSettingItemHeight)
                                        .clickable(enabled = isEnabled) {
                                            when {
                                                isChecked && selectedBottomBarItemKeys.value.size <= 3 -> {
                                                    userMessages.showShortMessage("至少保留3项")
                                                }

                                                !isChecked -> {
                                                    platformBottomBarItemLimit
                                                        ?.takeIf { selectedBottomBarItemKeys.value.size >= it }
                                                        ?.let { maximum ->
                                                            userMessages.showShortMessage("最多选择${maximum}项")
                                                        }
                                                        ?: persistBottomBarSelection(candidateOrderKeys)
                                                }

                                                else -> {
                                                    persistBottomBarSelection(candidateOrderKeys)
                                                }
                                            }
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = null,
                                            enabled = isEnabled,
                                        )
                                        Icon(
                                            item.icon,
                                            contentDescription = null,
                                            modifier = Modifier.padding(start = 8.dp, end = 12.dp),
                                            tint = if (isEnabled) {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            },
                                        )
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (isEnabled) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            },
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.width(96.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (isChecked) {
                                            IconButton(
                                                onClick = { moveBottomBarItem(key, -1) },
                                                enabled = selectedIndex > 0,
                                                modifier = Modifier.testTag("appearanceSettings:bottomBar:moveUp:$key"),
                                            ) {
                                                Icon(AppIcons.KeyboardArrowUp, contentDescription = "上移$label")
                                            }
                                            IconButton(
                                                onClick = { moveBottomBarItem(key, 1) },
                                                enabled = selectedIndex in 0 until selectedBottomBarItemKeys.value.lastIndex,
                                                modifier = Modifier.testTag("appearanceSettings:bottomBar:moveDown:$key"),
                                            ) {
                                                Icon(AppIcons.KeyboardArrowDown, contentDescription = "下移$label")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                )

                val collectionDirectBrowse = remember {
                    mutableStateOf(settings.getBoolean(COLLECTION_DIRECT_BROWSE_PREFERENCE_KEY, false))
                }
                SettingItemWithSwitch(
                    modifier = Modifier.testTag(APPEARANCE_SETTINGS_COLLECTION_DIRECT_BROWSE_TAG),
                    title = { Text("收藏直达浏览（测试）") },
                    icon = { Icon(AppIcons.Bookmarks, contentDescription = null) },
                    description = {
                        Text("测试功能，请谨慎开启，可能存在问题。开启后支持收藏夹直览、顺序模式与随机模式，欢迎提交 Issue。")
                    },
                    checked = collectionDirectBrowse.value,
                    onCheckedChange = {
                        collectionDirectBrowse.value = it
                        settings.putBoolean(COLLECTION_DIRECT_BROWSE_PREFERENCE_KEY, it)
                    },
                    settingKey = COLLECTION_DIRECT_BROWSE_PREFERENCE_KEY,
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor(COLLECTION_DIRECT_BROWSE_PREFERENCE_KEY),
                )

                val tapToRefresh = remember { mutableStateOf(settings.getBoolean("bottomBarTapScrollToTop", true)) }
                var singleTapRefresh by remember {
                    mutableStateOf(settings.getBoolean(BOTTOM_BAR_SINGLE_TAP_REFRESH_PREFERENCE_KEY, false))
                }
                SettingItemWithSwitch(
                    title = { Text("点击底部导航栏回到顶部/刷新") },
                    icon = { Icon(AppIcons.VerticalAlignTop, contentDescription = null) },
                    description = {
                        Text(
                            if (singleTapRefresh) {
                                "点击底部导航栏当前页面按钮回到顶部并刷新页面。"
                            } else {
                                "点击底部导航栏当前页面按钮回到顶部，已在顶部时则刷新页面。双击可直接刷新。"
                            },
                        )
                    },
                    checked = tapToRefresh.value,
                    onCheckedChange = {
                        tapToRefresh.value = it
                        settings.putBoolean("bottomBarTapScrollToTop", it)
                    },
                )

                SettingItemWithSwitch(
                    modifier = Modifier.testTag("appearanceSettings.bottomBarSingleTapRefresh"),
                    title = { Text("单击回顶并刷新") },
                    icon = { Icon(AppIcons.Refresh, contentDescription = null) },
                    description = {
                        Text(
                            if (tapToRefresh.value) {
                                "单击底部导航栏当前页面按钮，即使不在顶部也会回顶并刷新。"
                            } else {
                                "需先开启“点击底部导航栏回到顶部/刷新”。"
                            },
                        )
                    },
                    checked = singleTapRefresh,
                    enabled = tapToRefresh.value,
                    onCheckedChange = {
                        singleTapRefresh = it
                        settings.putBoolean(BOTTOM_BAR_SINGLE_TAP_REFRESH_PREFERENCE_KEY, it)
                    },
                    settingKey = BOTTOM_BAR_SINGLE_TAP_REFRESH_PREFERENCE_KEY,
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor(BOTTOM_BAR_SINGLE_TAP_REFRESH_PREFERENCE_KEY),
                )

                val autoHideBottomBar = remember { mutableStateOf(settings.getBoolean("autoHideBottomBar", false)) }
                SettingItemWithSwitch(
                    title = { Text("滚动时自动隐藏底部导航栏") },
                    icon = { Icon(AppIcons.VisibilityOff, contentDescription = null) },
                    description = { Text("上划时隐藏底部导航栏，下划时重新显示。") },
                    checked = autoHideBottomBar.value,
                    onCheckedChange = {
                        autoHideBottomBar.value = it
                        settings.putBoolean("autoHideBottomBar", it)
                    },
                )
            }

            if (isPageTurnSupported) {
                SettingItemGroup(
                    title = "电纸书翻页",
                    header = {
                        Text(
                            "在内容列表、正文、评论区和设置等可滚动页面生效，私聊除外。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    },
                ) {
                    var volumeKeyPageTurn by remember {
                        mutableStateOf(settings.getBoolean(PREF_VOLUME_KEY_PAGE_TURN, false))
                    }
                    SettingItemWithSwitch(
                        title = { Text("音量键翻页") },
                        icon = { Icon(AppIcons.VolumeUp, contentDescription = null) },
                        description = { Text("短按翻页，长按跳到顶部或底部。") },
                        checked = volumeKeyPageTurn,
                        onCheckedChange = {
                            volumeKeyPageTurn = it
                            settings.putBoolean(PREF_VOLUME_KEY_PAGE_TURN, it)
                        },
                        settingKey = PREF_VOLUME_KEY_PAGE_TURN,
                        highlightedKey = settingKey,
                        bringIntoViewRequester = requesterFor(PREF_VOLUME_KEY_PAGE_TURN),
                    )

                    var pageTurnSwitchAnswer by remember {
                        mutableStateOf(settings.getBoolean(PREF_PAGE_TURN_SWITCH_ANSWER, DEFAULT_PAGE_TURN_SWITCH_ANSWER))
                    }
                    SettingItemWithSwitch(
                        title = { Text("翻页切换回答") },
                        icon = { Icon(AppIcons.AutoStories, contentDescription = null) },
                        description = { Text("默认开启。在回答顶部继续上翻进入上一个回答，在底部继续下翻进入下一个回答。") },
                        checked = pageTurnSwitchAnswer,
                        onCheckedChange = {
                            pageTurnSwitchAnswer = it
                            settings.putBoolean(PREF_PAGE_TURN_SWITCH_ANSWER, it)
                        },
                        settingKey = PREF_PAGE_TURN_SWITCH_ANSWER,
                        highlightedKey = settingKey,
                        bringIntoViewRequester = requesterFor(PREF_PAGE_TURN_SWITCH_ANSWER),
                    )

                    var showPageTurnFab by remember {
                        mutableStateOf(settings.getBoolean(PREF_SHOW_PAGE_TURN_FAB, false))
                    }
                    SettingItemWithSwitch(
                        title = { Text("显示翻页悬浮按钮") },
                        icon = { Icon(AppIcons.SwapVert, contentDescription = null) },
                        description = { Text("只在支持翻页的可滚动页面显示。") },
                        checked = showPageTurnFab,
                        onCheckedChange = {
                            showPageTurnFab = it
                            settings.putBoolean(PREF_SHOW_PAGE_TURN_FAB, it)
                        },
                        settingKey = PREF_SHOW_PAGE_TURN_FAB,
                        highlightedKey = settingKey,
                        bringIntoViewRequester = requesterFor(PREF_SHOW_PAGE_TURN_FAB),
                    )

                    var pageTurnPercent by remember {
                        mutableIntStateOf(settings.getInt(PREF_PAGE_TURN_PERCENT, DEFAULT_PAGE_TURN_PERCENT))
                    }
                    SettingItem(
                        title = { Text("翻页距离") },
                        icon = { Icon(AppIcons.Height, contentDescription = null) },
                        description = { Text("每次滚动可见区域的 $pageTurnPercent%。") },
                        bottomAction = {
                            Slider(
                                value = pageTurnPercent.toFloat(),
                                onValueChange = {
                                    pageTurnPercent = (it / 5).roundToInt() * 5
                                    settings.putInt(PREF_PAGE_TURN_PERCENT, pageTurnPercent)
                                },
                                valueRange = 50f..100f,
                                steps = 9,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            )
                        },
                        settingKey = PREF_PAGE_TURN_PERCENT,
                        highlightedKey = settingKey,
                        bringIntoViewRequester = requesterFor(PREF_PAGE_TURN_PERCENT),
                    )

                    var showGuide by remember {
                        mutableStateOf(settings.getBoolean(PREF_SHOW_PAGE_TURN_GUIDE, DEFAULT_SHOW_PAGE_TURN_GUIDE))
                    }
                    SettingItemWithSwitch(
                        title = { Text("显示翻页位置线") },
                        icon = { Icon(AppIcons.HorizontalRule, contentDescription = null) },
                        description = { Text("翻页后标记上一页与下一页的重叠位置。") },
                        checked = showGuide,
                        onCheckedChange = {
                            showGuide = it
                            settings.putBoolean(PREF_SHOW_PAGE_TURN_GUIDE, it)
                        },
                        settingKey = PREF_SHOW_PAGE_TURN_GUIDE,
                        highlightedKey = settingKey,
                        bringIntoViewRequester = requesterFor(PREF_SHOW_PAGE_TURN_GUIDE),
                    )

                    var showContentEndMarker by remember {
                        mutableStateOf(
                            settings.getBoolean(
                                PREF_SHOW_CONTENT_END_MARKER,
                                DEFAULT_SHOW_CONTENT_END_MARKER,
                            ),
                        )
                    }
                    SettingItemWithSwitch(
                        title = { Text("显示内容结束标记") },
                        icon = { Icon(AppIcons.LastPage, contentDescription = null) },
                        description = { Text("方便电纸书用户确定内容结束了，再向下翻页将跳转下一个内容或无效果。") },
                        checked = showContentEndMarker,
                        onCheckedChange = {
                            showContentEndMarker = it
                            settings.putBoolean(PREF_SHOW_CONTENT_END_MARKER, it)
                        },
                        settingKey = PREF_SHOW_CONTENT_END_MARKER,
                        highlightedKey = settingKey,
                        bringIntoViewRequester = requesterFor(PREF_SHOW_CONTENT_END_MARKER),
                    )
                }
            }

            // ── 交互 ────────────────────────────────────────────────────────────
            SettingItemGroup(
                title = "交互",
            ) {
                var shareActionExpanded by remember { mutableStateOf(false) }
                val shareActionMode = remember {
                    mutableStateOf(settings.getString("shareActionMode", "ask"))
                }
                val shareActionOptions = listOf(
                    Triple("ask", "询问", AppIcons.Help),
                    Triple("copy", "复制链接", AppIcons.ContentCopy),
                    Triple("share", "Android分享", AppIcons.Share),
                )
                SettingItem(
                    title = { Text("分享操作") },
                    icon = { Icon(AppIcons.Share, contentDescription = null) },
                    description = { Text("点击分享按钮时的默认行为。") },
                    settingKey = "shareAction",
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor("shareAction"),
                    bottomAction = {
                        ExposedDropdownMenuBox(
                            expanded = shareActionExpanded,
                            onExpandedChange = { shareActionExpanded = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        ) {
                            OutlinedTextField(
                                value = shareActionOptions.find { it.first == shareActionMode.value }?.second ?: "询问",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shareActionExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            )
                            ExposedDropdownMenu(
                                expanded = shareActionExpanded,
                                onDismissRequest = { shareActionExpanded = false },
                            ) {
                                shareActionOptions.forEach { (mode, label, icon) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        trailingIcon = if (mode == shareActionMode.value) {
                                            { Icon(AppIcons.Check, contentDescription = "当前设置") }
                                        } else {
                                            null
                                        },
                                        leadingIcon = { Icon(icon, contentDescription = null) },
                                        onClick = {
                                            shareActionMode.value = mode
                                            settings.putString("shareActionMode", mode)
                                            shareActionExpanded = false
                                            userMessages.showShortMessage("已设置为：$label")
                                        },
                                    )
                                }
                            }
                        }
                    },
                )
            }

            // ── 搜索 ────────────────────────────────────────────────────────────
            SettingItemGroup(
                title = "搜索",
            ) {
                val showSearchHotSearch = remember { mutableStateOf(settings.getBoolean("showSearchHotSearch", true)) }
                SettingItemWithSwitch(
                    title = { Text("搜索界面显示热搜") },
                    icon = { Icon(AppIcons.Whatshot, contentDescription = null) },
                    description = { Text("在搜索界面空白时显示知乎热搜关键词。") },
                    checked = showSearchHotSearch.value,
                    onCheckedChange = {
                        showSearchHotSearch.value = it
                        settings.putBoolean("showSearchHotSearch", it)
                    },
                    settingKey = "showSearchHotSearch",
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor("showSearchHotSearch"),
                )
                val showSearchHistory = remember { mutableStateOf(settings.getBoolean("showSearchHistory", true)) }
                SettingItemWithSwitch(
                    title = { Text("记录并显示搜索历史") },
                    icon = { Icon(AppIcons.History, contentDescription = null) },
                    description = { Text("在搜索界面显示最近搜索过的关键词，关闭后不再记录新的搜索。") },
                    checked = showSearchHistory.value,
                    onCheckedChange = {
                        showSearchHistory.value = it
                        settings.putBoolean("showSearchHistory", it)
                    },
                    settingKey = "showSearchHistory",
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor("showSearchHistory"),
                )
            }

            // ── 导航 ────────────────────────────────────────────────────────────
            SettingItemGroup(
                title = "技术性导航设置",
            ) {
                val useCustomNavHost = remember { mutableStateOf(settings.getBoolean("use_custom_nav_host", true)) }
                SettingItemWithSwitch(
                    title = { Text("使用自定义导航") },
                    icon = { Icon(AppIcons.Navigation, contentDescription = null) },
                    description = { Text("使用自定义导航替代系统默认的导航组件，可能部分提升国产手机上的操作手感，请视情况开启。") },
                    checked = useCustomNavHost.value,
                    onCheckedChange = {
                        useCustomNavHost.value = it
                        settings.putBoolean("use_custom_nav_host", it)
                        userMessages.showShortMessage("需要重启应用生效")
                    },
                    settingKey = "use_custom_nav_host",
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor("use_custom_nav_host"),
                )

                val enablePredictiveBack = remember { mutableStateOf(settings.getBoolean(ENABLE_PREDICTIVE_BACK_PREFERENCE_KEY, true)) }
                SettingItemWithSwitch(
                    title = { Text("启用预测性返回") },
                    icon = { Icon(AppIcons.SwipeLeft, contentDescription = null) },
                    description = { Text("开启 Android 14+ 的预测性返回手势动画。") },
                    checked = enablePredictiveBack.value,
                    onCheckedChange = {
                        enablePredictiveBack.value = it
                        settings.putBoolean(ENABLE_PREDICTIVE_BACK_PREFERENCE_KEY, it)
                    },
                    settingKey = ENABLE_PREDICTIVE_BACK_PREFERENCE_KEY,
                    highlightedKey = settingKey,
                    bringIntoViewRequester = requesterFor(ENABLE_PREDICTIVE_BACK_PREFERENCE_KEY),
                )

                if (isTopLevelBackToLauncherSupported) {
                    val topLevelBackToLauncher = remember {
                        mutableStateOf(settings.getBoolean(TOP_LEVEL_BACK_TO_LAUNCHER_PREFERENCE_KEY, false))
                    }
                    SettingItemWithSwitch(
                        title = { Text("一级页面返回时回到桌面") },
                        icon = { Icon(AppIcons.ArrowBack, contentDescription = null) },
                        description = { Text("在底栏任意页面按返回都直接回到系统桌面，不再先切回第一个页面。") },
                        checked = topLevelBackToLauncher.value,
                        onCheckedChange = {
                            topLevelBackToLauncher.value = it
                            settings.putBoolean(TOP_LEVEL_BACK_TO_LAUNCHER_PREFERENCE_KEY, it)
                        },
                        settingKey = TOP_LEVEL_BACK_TO_LAUNCHER_PREFERENCE_KEY,
                        highlightedKey = settingKey,
                        bringIntoViewRequester = requesterFor(TOP_LEVEL_BACK_TO_LAUNCHER_PREFERENCE_KEY),
                    )
                }
            }
            // ── 123duo3 UI 改进 ─────────────────────────────────────────────────

            // 先声明所有子开关状态，以便主开关可以批量操作
            val duo3All = remember { mutableStateOf(settings.getBoolean("duo3_all", false)) }
            val duo3CardAppearance = remember { mutableStateOf(settings.getBoolean("duo3_card_appearance", false)) }
            val duo3CardLayout = remember { mutableStateOf(settings.getBoolean("duo3_card_layout", false)) }
            val duo3CardLargeTitle = remember {
                mutableStateOf(settings.getBoolean(DUO3_CARD_LARGE_TITLE_PREFERENCE_KEY, true))
            }
            val duo3ArticleBar = remember { mutableStateOf(settings.getBoolean("duo3_article_bar", false)) }
            val duo3ArticleActions = remember { mutableStateOf(settings.getBoolean("duo3_article_actions", false)) }
            val duo3TiqianMarkdown = remember {
                mutableStateOf(settings.getBoolean(DUO3_TIQIAN_MARKDOWN_PREFERENCE_KEY, false))
            }
            val duo3TiqianMathFont = remember {
                mutableStateOf(settings.getString(DUO3_TIQIAN_MATH_FONT_PREFERENCE_KEY, "lete"))
            }

            fun enableAllSubs() {
                if (isTiqianMarkdownRendererAvailable) {
                    settings.putBoolean(DUO3_TIQIAN_MARKDOWN_PREFERENCE_KEY, true)
                    duo3TiqianMarkdown.value = true
                }
                settings.putBoolean("duo3_home_account", true)
                settings.putBoolean("duo3_card_appearance", true)
                settings.putBoolean("duo3_card_layout", true)
                settings.putBoolean("duo3_article_bar", true)
                settings.putBoolean("duo3_article_actions", true)
                settings.putBoolean("showRefreshFab", false)
                settings.putBoolean("buttonSkipAnswer", false)
                duo3HomeAccount.value = true
                duo3CardAppearance.value = true
                duo3CardLayout.value = true
                duo3ArticleBar.value = true
                duo3ArticleActions.value = true
                // 123duo3 改动中会移除 FAB。
                showRefreshFab.value = false
                buttonSkipAnswer.value = false
                val updatedSelection = if (Home.name !in selectedBottomBarItemKeys.value) {
                    selectedBottomBarItemKeys.value + Account.name
                } else {
                    selectedBottomBarItemKeys.value
                }
                persistBottomBarSelection(updatedSelection, duo3HomeAccountEnabled = true)
            }

            fun disableAllSubs() {
                if (isTiqianMarkdownRendererAvailable) {
                    settings.putBoolean(DUO3_TIQIAN_MARKDOWN_PREFERENCE_KEY, false)
                    duo3TiqianMarkdown.value = false
                }
                settings.putBoolean("duo3_home_account", false)
                settings.putBoolean("duo3_card_appearance", false)
                settings.putBoolean("duo3_card_layout", false)
                settings.putBoolean("duo3_article_bar", false)
                settings.putBoolean("duo3_article_actions", false)
                duo3HomeAccount.value = false
                duo3CardAppearance.value = false
                duo3CardLayout.value = false
                duo3ArticleBar.value = false
                duo3ArticleActions.value = false
                persistBottomBarSelection(selectedBottomBarItemKeys.value, duo3HomeAccountEnabled = false)
            }

            SettingItemGroup(
                title = "123Duo3 的 UI/UX 改进（beta）",
                settingKey = "123Duo3",
                highlightedKey = settingKey,
                bringIntoViewRequester = requesterFor("123Duo3"),
                header = {
                    SettingItemOverall(
                        title = { Text("启用所有修改并关闭浮动按钮") },
                        icon = { Icon(AppIcons.AutoAwesome, contentDescription = null) },
                        checked = duo3All.value,
                        onCheckedChange = {
                            duo3All.value = it
                            settings.putBoolean("duo3_all", it)
                            if (it) {
                                enableAllSubs()
                            } else {
                                disableAllSubs()
                            }
                        },
                    )
                },
                footer = {
                    Text(
                        text = buildAnnotatedString {
                            append("以上设置项可能随时更改，或并入主线。\n欢迎")
                            withLink(LinkAnnotation.Url("https://github.com/eltavine/Zhihu-Hyperion/issues")) {
                                withStyle(
                                    MaterialTheme.typography.bodyMedium
                                        .copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            textDecoration = TextDecoration.Underline,
                                            fontWeight = FontWeight.Medium,
                                        ).toSpanStyle(),
                                ) {
                                    append("提交 Issue")
                                }
                            }
                            append(" 讨论本次 UI/UX 修改和反馈问题。")
                        },
                    )
                },
            ) {
                if (isTiqianMarkdownRendererAvailable) {
                    SettingItemWithSwitch(
                        modifier = Modifier.testTag(APPEARANCE_SETTINGS_TIQIAN_MARKDOWN_TAG),
                        title = { TiqianBrandTitle(prefix = "正文：使用", suffix = " Markdown 渲染器") },
                        icon = { Icon(AppIcons.FormatAlignJustify, contentDescription = null) },
                        description = { Text("使用「提椠」段落书写器排版正文：段落两端对齐，改进中西混排间距、代码、表格、公式与脚注等样式，接近纸质书的排版效果。作用于文章、想法与问题详情。实验功能。") },
                        checked = duo3TiqianMarkdown.value,
                        onCheckedChange = {
                            duo3TiqianMarkdown.value = it
                            settings.putBoolean(DUO3_TIQIAN_MARKDOWN_PREFERENCE_KEY, it)
                        },
                        settingKey = DUO3_TIQIAN_MARKDOWN_PREFERENCE_KEY,
                        highlightedKey = settingKey,
                        bringIntoViewRequester = requesterFor(DUO3_TIQIAN_MARKDOWN_PREFERENCE_KEY),
                    )
                    AnimatedVisibility(visible = duo3TiqianMarkdown.value) {
                        SettingItemWithSwitch(
                            title = { Text("正文：使用非衬线数学字体") },
                            icon = { Icon(AppIcons.Function, contentDescription = null) },
                            description = { Text("默认公式使用非衬线的 Lete Sans Math；关闭后改用衬线的 STIX Two Math。") },
                            checked = duo3TiqianMathFont.value == "lete",
                            onCheckedChange = {
                                val fontId = if (it) "lete" else "stix"
                                duo3TiqianMathFont.value = fontId
                                settings.putString(DUO3_TIQIAN_MATH_FONT_PREFERENCE_KEY, fontId)
                            },
                        )
                    }
                }

                SettingItemWithSwitch(
                    title = { Text("主页：账号入口迁移至顶部头像") },
                    icon = { Icon(AppIcons.AccountCircle, contentDescription = null) },
                    description = { Text("搜索栏样式变更；点击头像弹出账号与设置；「历史」入口可挪入账号设置页。") },
                    checked = duo3HomeAccount.value,
                    onCheckedChange = {
                        duo3HomeAccount.value = it
                        settings.putBoolean("duo3_home_account", it)
                        val updatedSelection = if (it && Home.name !in selectedBottomBarItemKeys.value) {
                            selectedBottomBarItemKeys.value + Account.name
                        } else {
                            selectedBottomBarItemKeys.value
                        }
                        persistBottomBarSelection(updatedSelection, it)
                    },
                )

                SettingItemWithSwitch(
                    title = { Text("信息流卡片：外观更改") },
                    icon = { Icon(AppIcons.Style, contentDescription = null) },
                    description = { Text("卡片圆角增大，移除阴影；修改背景与卡片颜色。") },
                    checked = duo3CardAppearance.value,
                    onCheckedChange = {
                        duo3CardAppearance.value = it
                        settings.putBoolean("duo3_card_appearance", it)
                    },
                )

                SettingItemWithSwitch(
                    title = { Text("信息流卡片：更改内容排版") },
                    icon = { Icon(AppIcons.ViewQuilt, contentDescription = null) },
                    description = { Text("作者移至底部；图片不与底部小字并列；摘要最多显示 4 行（原 3 行），规范字体样式。") },
                    checked = duo3CardLayout.value,
                    onCheckedChange = {
                        duo3CardLayout.value = it
                        settings.putBoolean("duo3_card_layout", it)
                    },
                )

                AnimatedVisibility(visible = duo3CardLayout.value) {
                    SettingItemWithSwitch(
                        title = { Text("信息流卡片：使用更大的标题字体") },
                        icon = { Icon(AppIcons.TextIncrease, contentDescription = null) },
                        description = { Text("默认启用；关闭后标题会缩小一档。") },
                        checked = duo3CardLargeTitle.value,
                        onCheckedChange = {
                            duo3CardLargeTitle.value = it
                            settings.putBoolean(DUO3_CARD_LARGE_TITLE_PREFERENCE_KEY, it)
                        },
                    )
                }

                SettingItemWithSwitch(
                    title = { Text("文章阅读页：更改整体顶/底栏框架") },
                    icon = { Icon(AppIcons.WebAsset, contentDescription = null) },
                    description = { Text("更改标题栏样式；优化顶/底栏隐藏逻辑。") },
                    checked = duo3ArticleBar.value,
                    onCheckedChange = {
                        duo3ArticleBar.value = it
                        settings.putBoolean("duo3_article_bar", it)
                    },
                )

                AnimatedVisibility(visible = duo3ArticleBar.value) {
                    SettingItemWithSwitch(
                        title = { Text("文章阅读页：更改操作栏样式") },
                        icon = { Icon(AppIcons.SmartButton, contentDescription = null) },
                        description = { Text("底栏操作按钮用药丸包裹；分隔赞同/反对按钮并添加动画。") },
                        checked = duo3ArticleActions.value,
                        onCheckedChange = {
                            duo3ArticleActions.value = it
                            settings.putBoolean("duo3_article_actions", it)
                        },
                    )
                }
            }
        }
    }
}
