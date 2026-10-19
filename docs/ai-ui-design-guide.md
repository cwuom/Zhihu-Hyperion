# Zhihu-Hyperion UI 设计约定

本文说明 UI、导航、按钮和设置项的共同语义、状态来源与影响范围。改动前仍须核对对应源码。

## 先读顺序

1. `shared/src/commonMain/kotlin/com/github/zly2006/zhihu/navigation/NavDestination.kt`
2. `shared/src/commonMain/kotlin/com/github/zly2006/zhihu/ui/ZhihuMain.kt`
3. Android: `app/src/main/java/com/github/zly2006/zhihu/MainActivity.kt` 和 `app/src/main/java/com/github/zly2006/zhihu/ui/ZhihuMainAndroidState.kt`
4. Desktop: `shared/src/jvmMain/kotlin/com/github/zly2006/zhihu/ui/DesktopZhihuMain.kt`
5. 设置页: `shared/src/commonMain/kotlin/com/github/zly2006/zhihu/ui/subscreens/*SettingsScreen.kt`
6. 复用组件: `shared/src/commonMain/kotlin/com/github/zly2006/zhihu/ui/components/SettingItem.kt`、`FeedCard.kt`、`DraggableRefreshButton.kt`

UI 改动不要只看目标页面。至少用 `rg "<preferenceKey>" shared app` 查设置项的读取点，用 `rg "<NavDestinationName>" shared app` 查导航入口和测试。

## 导航模型

`NavDestination` 是所有可导航目标的源头，使用 Kotlin Serialization 作为 typed route。新增页面或参数先改 `NavDestination.kt`，再注册 `NavHost` route。

主壳是 `MainTabs`。`Home`、`Follow`、`HotList`、`Daily`、`OnlineHistory`、`Account` 是 `TopLevelDestination`，用于底栏和主 pager 选页；不要把旧 top-level tab 当作新的独立页面 push，除非源码已经明确注册了独立 route。`Follow` 对应两个相邻 pager page: 推荐和动态，并记住上一次选择。

底栏显示只在 `MainTabs` route 上出现，判断点是 `isTopLevelDest(navEntry)`。内容页、设置页、搜索页、通知页等已注册独立目的地会离开主壳，依赖返回栈回退。

Android 导航由 `MainActivity.navigate()` 承接，负责历史记录、剪贴板/外链跳转、内容打开来源埋点。Desktop 在 `DesktopZhihuMain.navigate()` 中实现同一语义。视频是普通 route：`Video` 只凭视频 id 由 `:feature:video` 的 `VideoScreen` 加载，宿主不再按来源页面特殊处理；它注册在主 `NavHost`，从详情栏打开时也铺满窗口。跨平台 UI 内部应优先用 `LocalNavigator.current.onNavigate(...)`，不要直接持有 Activity 或平台 NavController。

URL 解析集中在 `resolveContent()`。支持知乎问题、回答、文章、用户、视频（含 `video.zhihu.com/video/{id}`）、想法、搜索、`link.zhihu.com?target=` 和 `zhihu://` scheme。新增 deep link 必须同时考虑 Android intent、剪贴板解析和桌面打开路径。

## 主界面结构

`ZhihuMain` 是共同 UI 外壳。它读取 `ZhihuMainPreferenceState`，生成底栏项、主 pager 页、自动隐藏状态和 `NavHost` route。Android 与 Desktop 分别用平台 adapter 注入文章页、NLP 管理页和转场。

底栏项顺序固定为: 主页、关注、热榜、日报、历史、账号。实际显示由 `bottom_bar_items` 决定，并经过 `normalizeBottomBarSelection()` 兜底。`duo3_home_account` 开启后，账号入口迁到主页头像，底栏选择规则会改变；这类改动必须一起检查主页账号入口、账号页历史快捷方式和底栏启动页。

`startDestination` 不是 NavHost 的 start route，而是主 pager 初始页。NavHost start route 始终是 `MainTabs`。如果用户选的启动页不在底栏里，`resolveValidStartDestinationKey()` 会回落到当前可用项。

## 按钮和交互约定

设置列表使用 `SettingItemGroup` 包组，单项用 `SettingItem`，开关用 `SettingItemWithSwitch`，总开关用 `SettingItemOverall`。需要从账号页跳入并高亮某个设置时，给目标项传 `settingKey`，导航参数使用 `Account.AppearanceSettings(setting = "...")` 或 `Account.RecommendSettings(setting = "...")`。

返回入口通常放在当前页面顶栏的 `navigationIcon`：设置页多用 `LargeTopAppBar`，普通列表页多用 `TopAppBar`。返回图标优先用自动镜像箭头，`contentDescription = "返回"`。列表页容器通常是 `Scaffold` 加可滚动内容，具体背景色和滚动容器以现有页面体系为准。

底栏按钮测试 tag 是 `nav_tab_${destination.name.lowercase()}`。设置页和账号页已有显式 test tag，新增可测试入口优先加稳定 tag，而不是让 UI 测试用坐标点。

分享按钮的行为由 `shareActionMode` 决定。`ask` 打开分享弹窗，`copy` 直接复制链接，`share` 直接调系统分享；弹窗的设置入口会跳到 `Account.AppearanceSettings(setting = "shareAction")`。

可拖动刷新 FAB 使用 `DraggableRefreshButton`，位置保存在 `"<preferenceName>-x"` 和 `"<preferenceName>-y"`。不要另写一套拖动定位逻辑；需要多个 FAB 时改 `preferenceName` 防止位置互相覆盖。

## 视觉体系

当前主线 UI 以 Material 3 为默认实现。改共同行为时优先放在 common runtime、状态或 shared component；改视觉时先确认影响的是共享组件、单个页面还是平台 adapter，避免只改入口而漏掉复用路径。

### 设计系统：Material 3 Expressive

- 主题：`ZhihuTheme` 用 `MaterialExpressiveTheme`，动效是 `MotionScheme.expressive()` 的弹簧物理，形状和字体用 Expressive 默认尺度（含 `largeIncreased`、`extraLargeIncreased`、`extraExtraLarge` 形状和 `*Emphasized` 字体样式）。自定义动画优先取 `MaterialTheme.motionScheme` 的 spring token，不再手写 tween 时长。预测性返回按手势进度线性 seek，插值规则保留到当前导航项结束转场，提交和取消交由 NavHost 收尾；普通转场使用主题 spring。预览应显示原页面并保留其上下文。
- 配色：平台壁纸取色可用时用平台配色；否则（关闭动态取色、桌面、macOS、Android 12 以下）用 `customThemeColor` 经 material-kolor 按 2025 版规范生成。
- 组件：优先用 material3 已提供的 Expressive 组件（分段列表、按钮组、切换按钮、FAB 菜单、浮动工具栏、加载指示器等），不要手写外观相似的替代品；只有承载应用语义时才封装（如设置项）。
- 图标：界面只用 `:core:icons` 的 `AppIcons.<名称>`，用 `Icon(icon = ...)` 渲染，不依赖任何图标库或直接引用图标资源。图标是 Material Symbols Rounded 的未填充版本；`*Filled` 只用于选中、已赞、已收藏等状态，底栏选中项也用填充版本。新增图标先在 `core/icons/symbols.txt` 登记一行，再运行 `python3 core/icons/symbols.py`，CI 用 `--check` 校验清单、资源和 `AppIcons.kt` 一致。品牌标志（GitHub、Telegram）这类 Material Symbols 没有的图形才保留独立资源。同一个概念全局只用一个图标（如“收藏夹”一律用 `Bookmarks`，赞数与赞同用 `ThumbUp`、反对用 `ThumbDown`，粉丝数和关注数用 `Group`），一个图标也只表示一种含义；选图标前先搜一遍 `AppIcons.<名称>` 的现有用法。
- 实验 API 的边界：Expressive 组件大多仍标注 `@ExperimentalMaterial3ExpressiveApi`，只在 `:core:ui` 的封装里使用（`SettingItem` 系列、`ChoiceButtonGroup`、`AppLoadingIndicator`、`AppPullToRefreshBox`、`FabMenu`、`EmptyState`、`ShapedIcon`、`MediumActionButton`、`BottomFloatingToolbar`、`ActionToggleButton`、`GroupedListItem`），公开参数只用稳定类型；页面直接调用这些封装，不在页面里 opt-in 实验 API。`ShortNavigationBar` 等已稳定的组件可以直接用。
- 自适应导航：窗口宽度达到 600dp（横屏手机、平板、桌面窗口）且没有进入列表-详情双栏时，主壳用左侧导航轨 `WideNavigationRail` 代替底栏，标题和图标同样只取 `allBottomBarItems`；双栏时列表栏本身是窄屏宽度，仍在列表栏底部用底栏。悬浮在内容上的控件要按所在容器而不是整个窗口计算位置，因为导航轨会占去左侧宽度。
- 设置项：每一行都配一个表意的图标，`SettingItem` 把它放进无独立底色的 40dp 图标区域，用间距区分各行。有整行动作的行由 Expressive 分段列表项绘制，按下时圆角形变；开关行带 `Role.Switch` 语义。行尾只放开关、数值和跳转箭头；选择控件（按钮组、下拉框）、滑杆和输入框放进 `bottomAction`，横跨整行宽度，不和标题抢宽度。
- 单选：两到三个短标签的互斥选项用 `ChoiceButtonGroup`（连接式按钮组）；标签加图标在 360dp 宽度下放不下时只放标签。选项多、标签长或随数据变化时用下拉菜单。
- 菜单和对话框：`DropdownMenuItem` 配 `leadingIcon`，倍速、时长这类纯数值列表除外；下拉选择的菜单里，当前值在尾部打勾；`AlertDialog` 配 `icon` 说明用途（删除用 `Delete`，清空用 `DeleteSweep`，屏蔽用户用 `PersonOff` 等），同一动作在各处用同一个图标。对话框一律用 `AlertDialog`，不用 `Dialog` 加 `Surface` 手写外框；对话框里有一组可选的操作时用 `GroupedListItem` 排成分段列表，只列当前平台支持的操作，不放灰掉的“暂不支持”项。不属于某个页面的对话框（登录过期、请求出错、连续浏览提醒、崩溃报告等）交给 Koin 里的 `AppDialogQueue`，由应用根部的 `AppDialogHost` 一次显示一个，同一个 key 不会叠加；不要用 `android.app.AlertDialog` 这类系统对话框。
- 加载：页面、列表加载更多、弹窗内容等区块的不定时等待用 `AppLoadingIndicator`；按钮或行内的小号转圈、视频缓冲，以及有明确进度的场景继续用 `CircularProgressIndicator` / `LinearProgressIndicator`。下拉刷新一律用 `AppPullToRefreshBox`，不直接用 `PullToRefreshBox`。
- 导航与 FAB：主壳底栏是 `ShortNavigationBar`，各项的标题和图标只在 `allBottomBarItems` 定义。一个按钮下有多个创作类动作时用 `FabMenu`（Expressive FAB 菜单），不要手写弹出卡片。
- 空状态：整页空列表和列表底部的加载失败用 `EmptyState`（Expressive 异形容器里的图标、标题、可选说明和“重试”等按钮）。标题只写状态（如“加载失败”），原因、错误详情和这里会出现什么放进说明；只有一行的轻提示（如“已经到底啦”）保持文字。被其他设置禁用的设置项，要在说明或分组脚注里写明前置条件。分页列表把这两种状态交给 `PaginatedList`：`loadFailed` 加 `onRetry`（`PaginationViewModel.retry` 从失败的那一页接着加载）显示“加载失败”和“重试”，`emptyContent` 在加载完却没有条目时代替“已经到底啦”；同时要传 `isEnd`，否则列表底部的加载指示一直不消失。
- 主视觉与页面级按钮：引导页、登录页这类需要视觉重心的位置用 `ShapedIcon`（`MaterialShapes` 异形容器，形状只从 `IconShape` 里选，主视觉用 `primaryContainer`）。整页只有一两个主要操作、按钮横跨内容宽度时用 `MediumActionButton`（56dp，按下形变，`loading` 时原位显示加载指示）：主操作 `Filled`，离开流程的外链 `Outlined` 并配 `OpenInNew`；列表里、工具栏里的普通按钮仍用默认尺寸。
- 引导与登录：内容宽度不超过 560dp 并水平居中，平板和桌面窗口不把按钮拉满；页面根节点消费 `safeDrawing`，iOS 状态栏区域的触摸归系统，控件压在下面会点不到。声明页的确认按钮写明动作（“我已了解”“同意并继续”），不用笼统的“确认”；多步流程里返回键先回到上一步。表单的输入框配前置图标，手机号用 `prefix` 显示区号，验证码这类输入标注 `ContentType`（iOS 另经 `PlatformImeOptions` 标成一次性验证码），错误放进 `errorContainer` 卡片。二维码始终放在白底卡片里，深色主题下也要能被扫。
- 液态玻璃：效果只来自 `:core:glass`（封装 Kyant backdrop，调用方看不到库的类型），只用在主壳底栏和回答页操作栏这两个代表位置，不要扩散到列表项或对话框。内容宿主用 `rememberGlassBackdrop` + `Modifier.glassBackdropSource` 录制背景，玻璃表面必须是它的兄弟节点；关闭开关或平台不支持时传 null，回到 Material 外观。
- 阅读页操作栏：主视觉用 `BottomFloatingToolbar`（Expressive 浮动工具栏，居中悬浮在正文上方，滚动隐藏沿用 `ArticleBottomBarState`），只放赞同、反对、评论、收藏和更多。开关类动作用 `ActionToggleButton`：赞同、反对选中时用知乎蓝 `voteUpNeutralContent()`，收藏用 `tertiary`；其余按钮用 `onSurface` 色。操作按钮不绘制独立底色，选中态由填充图标和内容色表达，保留原触控区域；duo3 操作栏沿用相同规则。
- 评论：输入框是至少 48dp 高的胶囊（`surfaceContainerHighest`，`bodyLarge`），表情和发送按钮都是 48dp，发送用 `FilledIconButton`；列表项的回复、点赞、更多操作用标准按钮尺寸，点赞用 `ActionToggleButton`，作者标签是 `secondaryContainer` 小药丸；时间与 IP 属地合成一行 `labelMedium`。正文文字一律用主题字体样式，只保留用户设置的评论字号与行高倍数，不写死 `fontSize`。
- 内容分组列表：按日期等维度分组、整行可点的内容条目（如日报）用 `GroupedListItem`（Expressive 分段列表项，组内留分段间隙、首末项大圆角），页面底色用 `surfaceContainer`，与设置页一致；分组标题用 `stickyHeader` 吸顶。少量头条大图用 `HorizontalMultiBrowseCarousel`，项被裁窄时隐去文字。信息流卡片仍用 `FeedCard`。

主题状态集中在 `ThemeManager` 和平台 `ThemeSettingsRuntime`。`themeMode` 控制明暗，`useDynamicColor` 控制 Material You 动态取色，`customThemeColor` 在动态取色关闭后生效，`backgroundColorLight` / `backgroundColorDark` 控制背景色，`luotianyi_color` 控制应用内浏览器工具栏色。

信息流卡片由 `FeedCard` 读取 `showFeedThumbnail`、`feedCardStyle`、`duo3_card_appearance`、`duo3_card_layout`、`duo3_card_large_title`。改这些 key 的语义时要同时查主页、关注、热榜、历史和搜索结果等复用卡片的页面。

卡片左下角的详情行由 `feedDetailParts` 按「 · 」拆段：赞同、评论、收藏、浏览计数和「…推荐」来源使用图标与 `onSurfaceVariant` 次级文字，不加彩色色块；其余片段保持原文。标题、作者、摘要、互动信息依次分层，互动信息与摘要之间保留独立间距。数据源新增计数时沿用「数字 量词」的写法并在 `FeedDetailKind` 登记量词，不要另拼带图标的文字。

文章、问题详情和想法正文会根据 `ARTICLE_USE_WEBVIEW_PREFERENCE_KEY` 在 WebView 与 Compose Markdown 之间切换；该常量当前值是 `webviewRender`。WebView 正文渲染只作为废弃路径保留，不再接受新功能；阅读体验新能力只接入 Compose Markdown 路径。Compose Markdown 路径依赖 `RenderMarkdown` 和 `SegmentedText` 的字号、行高和段间距设置。

详情页标题默认使用 18sp 字号和 26sp 行高，并随 `contentFontSize` 按比例缩放。问题正文标题和展开的回答标题保留加粗、完整换行；收起后的工具栏标题保留原有单行省略显示。

## 设置项影响图

### 外观与阅读体验

| preference key | 入口 | 主要影响 | 注意 |
| --- | --- | --- | --- |
| `themeMode` | 主题模式 | 应用明暗主题 | 通过 `ThemeManager` 立即影响顶层主题 |
| `useDynamicColor` | Material You 动态取色 | Android 12+ 取系统壁纸色 | 关闭后自定义主题色才明显生效 |
| `customThemeColor` | 自定义主题色 | Material 3 主色 | 不要和动态取色同时假设生效 |
| `backgroundColorLight`, `backgroundColorDark` | 自定义背景颜色 | 明暗模式背景 | 按当前明暗模式写入不同 key |
| `disableBottomSheetRoundedCorners` | 禁用 popup 圆角 | `MyModalBottomSheet` 顶部改为直角 | 默认关闭，统一影响评论等复用 popup |
| `contentFontSize`, `contentLineHeight`, `contentBlockSpacing` | 字号、行高、段间距 | 正文与详情标题字号、正文行高、分段文本样式和 Markdown 正文块间距 | 查 `SegmentedText` 和 Markdown 渲染路径 |
| `liquidGlass` | 液态玻璃 | 主壳底栏换成悬浮的 `LiquidGlassTabBar`，回答页 `BottomFloatingToolbar` 换成玻璃底板 | 常量与默认值在 `:core:glass`：只有 iOS 默认开启；只在 `isLiquidGlassSupported`（Android 13+、iOS、macOS、桌面）时显示开关；随平台调整默认值，不进 WebDAV 同步 |
| `showFeedThumbnail` | Feed 卡片缩略图 | 信息流卡片是否显示图 | 由复用 `FeedCard` 的页面读取 |
| `showRefreshFab` | 刷新 FAB | 首页/列表可拖动刷新按钮显示 | 123Duo3 总开关会关闭它 |
| `creationEntryPosition` | 外观与阅读体验 → 信息流 → 发布入口位置 | `floating`（默认）、`top`、`bottom`，实时切换发布入口 | 常量和枚举在 `AppearanceSettingsScreen`；`ZhihuMain` 观察设置并传给首页，悬浮发布按钮为 48dp；顶部模式按通知、发布排列，两者之间不额外留白；底部模式为导航栏中央的动作，宽屏并入导航轨；没有共享导航栏的平台使用首页顶部入口。发布动作不加入 pager，不改变主 tab 选择或排序 |
| `feedCardStyle` | 信息流样式 | `card`（默认，色调圆角卡片，卡片间 8dp）或 `divider` | 常量 `FEED_CARD_STYLE_PREFERENCE_KEY` / `DEFAULT_FEED_CARD_STYLE`，影响所有复用 FeedCard 的列表 |
| `webviewRender` | 使用 WebView 显示文章 | 文章、问题详情、想法正文渲染路径 | 常量名是 `ARTICLE_USE_WEBVIEW_PREFERENCE_KEY` |
| `webviewCustomFontName` | WebView 自定义字体 | WebView 注入字体 | 仅 WebView 路径 |
| `webviewHardwareAcceleration` | WebView 硬件加速 | Android WebView layer type | 兼容性/性能相关 |
| `titleAutoHide` | 自动隐藏回答标题 | 文章页顶部标题栏 | 查 `rememberArticleScreenSettingsState()` |
| `autoHideArticleBottomBar` | 自动隐藏回答底部按钮 | 文章页底部操作栏 | 与滚动方向有关 |
| `buttonSkipAnswer` | 显示跳转下一个回答按钮 | 文章页快速跳转按钮 | 123Duo3 总开关会关闭它 |
| `autoHideSkipAnswerButton` | 自动隐藏跳转按钮 | 跳转按钮滚动隐藏 | 仅 `buttonSkipAnswer` 开启时可见 |
| `pinAnswerDate` | 置顶回答日期 | 回答日期位置 | 影响文章正文布局 |
| `answerSwitchMode` | 回答切换手势 | `off` / `vertical` / `horizontal` | 会影响转场方向和手势冲突 |
| `answerSwitchSensitivity` | 回答切换灵敏度 | 上下/左右回答切换触发阈值 | 默认 1.0，数值越高触发距离越短；关闭手势时设置项隐藏 |
| `answerDoubleTapAction` | 双击回答动作 | 双击正文后的动作 | 可在文章页内动态保存 |
| `bottom_bar_items` | 底栏页面选择 | 主 pager 页集合和底栏按钮 | 始终经 `normalizeBottomBarSelection()` |
| `collectionDirectBrowse` | 收藏直达浏览（测试） | 底栏收藏夹进入内容瀑布流，支持切换、删除、刷新及随机排序 | 默认关闭，关闭时保留原收藏夹列表入口 |

### 收藏直达浏览测试功能边界

- 页面从非活动状态重新进入活动状态时，会同时刷新收藏夹列表和当前收藏夹内容；用户也可以在内容区下拉刷新。
- 每次进入随机模式都会生成新种子并重新洗牌，同一轮内 Compose 重组不会让卡片反复跳位；随机模式下显示可拖动的重新加载 FAB，点击时重新拉取内容并生成新顺序；切回顺序模式会恢复接口返回顺序并隐藏该 FAB。
- 非默认收藏夹可从文件夹菜单删除并需要二次确认，默认收藏夹不提供删除入口；删除当前收藏夹后回退到默认收藏夹或列表中的第一个。
| `startDestination` | 应用启动默认页面 | 主 pager 初始页 | 只允许选择已显示的底栏项 |
| `bottomBarTapScrollToTop` | 点击底栏回到顶部/刷新 | 选中 tab 再点时触发 scroll/refresh | 双击由 UI 测试用 `&&` 连续 adb 才可靠 |
| `bottomBarSingleTapRefresh` | 底部导航栏 → 单击回顶并刷新 | 离开顶部时单击当前 tab 也回顶并刷新 | 默认关闭；需开启 `bottomBarTapScrollToTop`；各列表在处理点击时读取，无需重启；参与 WebDAV 同步；tag 为 `appearanceSettings.bottomBarSingleTapRefresh`，支持按同名 key 搜索跳转高亮 |
| `autoHideTopBar` | 滚动时自动隐藏顶栏 | 主 tab 页顶栏可见性 | 复用底栏滚动信号 |
| `autoHideBottomBar` | 滚动时自动隐藏底栏 | 主 tab 页底栏可见性 | 只在 `MainTabs` 显示 |
| `shareActionMode` | 分享操作 | 分享按钮默认行为 | `ask` / `copy` / `share` |
| `showSearchHotSearch` | 搜索界面显示热搜 | 空搜索页内容 | 搜索页空查询状态读取 |
| `showSearchHistory` | 搜索历史 | 是否记录和展示新搜索 | 关闭后不再记录新的搜索 |
| `use_custom_nav_host` | 自定义导航 | 持久化的技术性导航开关 | 当前主要在设置页读写；实现效果前先 `rg` 运行时读取点 |
| `enable_predictive_back` | 预测性返回 | 持久化的 Android 14+ 返回动画开关 | 当前主要在设置页读写；实现效果前先 `rg` 运行时读取点 |
| `topLevelBackToLauncher` | 一级页面返回时回到桌面 | 底栏非第一个页面按返回时不再先切回第一个页面，交给系统回到桌面 | 默认关闭，保留 [#660](https://github.com/zly2006/zhihu-plus-plus/pull/660) 的“先回第一个页面”；只在 `isTopLevelBackToLauncherSupported`（Android）时显示并生效，`ZhihuMain` 通过 `rememberObservedSetting` 读取 |

### 推荐系统与内容过滤

| preference key | 入口 | 主要影响 | 注意 |
| --- | --- | --- | --- |
| `recommendationMode` | 推荐算法 | Web、Android、本地、混合推荐 | 枚举在 `RecommendationMode.kt` |
| `loginForRecommendation` | 推荐内容时登录 | 获取推荐时是否带登录凭证 | 影响服务端推荐结果 |
| `qualityFilterMode` | 质量屏蔽 | 不屏蔽、显示屏蔽规则或隐藏低质量内容 | 默认 `RULES`，旧布尔设置不迁移 |
| `enableContentFilter` | 智能内容过滤 | 过滤重复出现但未点击内容 | 关闭时相关子统计/开关应弱化 |
| `filterFollowedUserContent` | 过滤已关注用户内容 | 是否过滤关注用户内容 | 仅智能过滤开启时可操作 |
| `enableKeywordBlocking` | 关键词屏蔽 | 命中关键词时过滤 | 管理入口在 Blocklist |
| `enableUserBlocking` | 用户屏蔽 | 命中用户时过滤 | Feed 卡片更多菜单可新增屏蔽 |
| `enableTopicBlocking` | 主题屏蔽 | 命中主题时过滤 | 阈值项只在开启时显示 |
| `topicBlockingThreshold` | 主题屏蔽阈值 | 命中主题数量下限 | 必须为大于 0 的整数 |
| `blockZhihuAdPlatform` | 屏蔽知乎广告平台内容 | 匹配 `xg.zhihu.com` | 推广过滤 |
| `blockZhihuSchool` | 屏蔽知乎学堂内容 | 匹配 `d.zhihu.com` 或教育卡片 | 推广/课程过滤 |
| `blockWeChatOfficialAccount` | 屏蔽微信公众号文章 | 匹配微信外链 | 外链过滤 |
| `blockPaidContent` | 屏蔽知乎盐选付费内容 | 过滤会员付费内容 | mixed 推荐默认语义也提到盐选 |
| `reverseBlock` | 反向屏蔽 | 只保留广告和付费内容 | 调试/整活性质，别当普通过滤开关 |

### 系统、通知和开发者

| key/store | 入口 | 主要影响 | 注意 |
| --- | --- | --- | --- |
| `githubAcceleration` | GitHub 加速 / 启动询问 | 更新检查和下载是否经 gh-proxy.com | 未保存即未回答，应用启动时弹窗询问，回答前不做启动检查；取决于本机网络，不进 WebDAV 同步 |
| `checkNightlyUpdates` | Nightly 更新 | 读取 nightly 还是正式版的 update.json | 键与读取逻辑都在 `:feature:update` 的 `UpdateController` |
| `continuousUsageReminderIntervalMinutes` | 防沉迷提醒 | 连续使用提醒间隔 | 0 表示关闭 |
| `webdav.json`（`WebDavConfigFile`） | WebDAV 同步 | 手动上传/恢复屏蔽列表和已登记的设置 | 凭据不在 `SettingsStore`，也不进 Android 云备份；新增用户设置要登记到 `WebDavSyncViewModel.kt` 的 `syncedSettings` 才会同步 |
| `custom_ai.json`（`:feature:ai` 的 `CustomAiConfigFile`） | 「总结本文」弹层的「自定义 AI」 | OpenAI 兼容接口的地址、API Key、模型，以及总结是否改用它 | 与 WebDAV 凭据同样不在 `SettingsStore`、不进云备份和 WebDAV 同步；没配置完整时总结仍走知乎直答 |
| `developer` | 开发者模式 | 账号页显示开发者选项 | 账号页点击版本 5 次开启 |
| `enableScrollEndHaptic` | 开发者选项: 滚动到底震动 | 滚动边界反馈行为开关 | 改前查具体 overScroll 使用点 |
| `showDebugOverlay` | 开发者选项: 调试悬浮窗 | 调试 Feed 详情显示 | 如果 `rg` 只命中设置页，先补运行时读取点 |
| `zse96_key` | 开发者签名请求 | 调试签名相关请求 | 只在开发者页处理 |
| NotificationSettingsStore | 通知设置 | 系统通知、应用内显示、自动已读、未读红点 | 不走普通 `SettingsStore` key 命名 |

## 123Duo3 UI/UX 开关

`duo3_all` 是批量开关。开启时会写入 `duo3_home_account`、`duo3_card_appearance`、`duo3_card_layout`、`duo3_article_bar`、`duo3_article_actions`，并关闭 `showRefreshFab` 和 `buttonSkipAnswer`。底部导航栏统一使用 Material 样式，不再提供单独开关。

各子开关影响:

| key | 影响 |
| --- | --- |
| `duo3_home_account` | 主页头像承接账号入口，底栏账号项规则变化，账号页可能显示历史快捷方式 |
| `duo3_card_appearance` | Feed 卡片圆角、背景和阴影变化 |
| `duo3_card_layout` | Feed 卡片作者、图片、摘要行数和字体排版变化 |
| `duo3_card_large_title` | `duo3_card_layout` 开启后控制标题字号 |
| `duo3_article_bar` | 文章页顶/底栏框架开关；若 `rg` 只命中设置页，不要声称已有运行时效果 |
| `duo3_article_actions` | 文章页底部操作按钮样式变化 |

改任意 Duo3 开关时，至少检查 `AppearanceSettingsScreen`、`ZhihuMain`、`HomeScreen`、`AccountSettingScreen`、`FeedCard` 和 `ArticleScreen`。

## 新增 UI 的检查清单

1. 新 route: 先在 `NavDestination.kt` 定义 typed destination，再在 `ZhihuMain` 注册 route；涉及平台能力时同步补 Android/Desktop adapter。
2. 新主 tab: 同时改 `TopLevelDestination`、`MainTabPage`、底栏 `allBottomBarItems`、设置页的 `topLevelDestinationsInOrder`、默认选择和测试。
3. 新设置项: 记录 preference key、默认值、读取点、是否实时生效、是否需要重启、对应 test tag 和从账号页跳转高亮的 `settingKey`。
4. 新按钮: 优先复用 Material 3（Expressive）组件和 `AppIcons`，补稳定 test tag，描述点击后影响的状态或导航目标。
5. 新正文/卡片渲染逻辑: 同时确认 Compose Markdown、WebView、共享组件、平台 adapter、lite/full variant 差异。
6. 新手势: 明确方向、阈值，以及和现有回答切换、底栏自动隐藏、图片查看的冲突关系。

## 验证入口

UI 代码改动按 `CLAUDE.md` 的 AVD 和 UI 双代理流程执行。文档或纯注释改动不需要启动 AVD，但仍应做 Markdown 自查、关键路径 `rg` 复核和 `git diff --check`。

常用定位命令:

```bash
rg "Account\\.AppearanceSettings|Account\\.RecommendSettings|NavDestination|MainTabs" shared app
rg "rememberSettingsStore|putBoolean|putString|putInt|getBoolean|getString|getInt" shared app
rg "testTag\\(|nav_tab_|ACCOUNT_SETTINGS_|APPEARANCE_SETTINGS_" shared app
```
