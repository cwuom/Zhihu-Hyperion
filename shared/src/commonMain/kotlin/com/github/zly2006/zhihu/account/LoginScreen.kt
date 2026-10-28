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

package com.github.zly2006.zhihu.account

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.icons.AppIcon
import com.github.zly2006.zhihu.icons.AppIcons
import com.github.zly2006.zhihu.icons.Icon
import com.github.zly2006.zhihu.platform.PlatformBackHandler
import com.github.zly2006.zhihu.platform.SettingsStore
import com.github.zly2006.zhihu.platform.rememberExternalUrlOpener
import com.github.zly2006.zhihu.ui.components.ActionEmphasis
import com.github.zly2006.zhihu.ui.components.ChoiceButtonGroup
import com.github.zly2006.zhihu.ui.components.ChoiceOption
import com.github.zly2006.zhihu.ui.components.IconShape
import com.github.zly2006.zhihu.ui.components.MediumActionButton
import com.github.zly2006.zhihu.ui.components.ShapedIcon
import org.koin.compose.koinInject

enum class LoginMethod(
    val label: String,
    val icon: AppIcon,
    val tag: String,
) {
    Phone("手机号", AppIcons.Smartphone, "login_mode_phone"),
    Qr("扫码", AppIcons.QrCodeScanner, "login_mode_qr"),
    Web("网页", AppIcons.Language, "login_mode_web"),
}

expect val supportedLoginMethods: List<LoginMethod>

@Composable
expect fun QrLoginPane(
    onLoginSuccess: (String) -> Unit,
    onUsePhoneLogin: () -> Unit,
)

@Composable
expect fun WebLoginPane(onLoginSuccess: (String) -> Unit)

/** 登录页和声明页的内容宽度上限：平板、桌面窗口里不把按钮和文字拉满整个窗口。 */
private val LoginContentMaxWidth = 560.dp

private const val LOGIN_NOTICES_ACCEPTED_KEY = "loginNoticesAccepted"

/**
 * 首次登录前依次确认 [loginNotices] 中的声明，然后选择登录方式；确认过的声明之后不再出现。
 *
 * 声明页之间可以返回上一页；第一页和登录方式页的返回交给导航，与其他页面一致。
 * [onNavigateBack] 为 null 表示没有可返回的应用页面，隐藏返回按钮，系统返回交给宿主处理
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginComplete: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
) {
    val openExternalUrl = rememberExternalUrlOpener()
    val settings = koinInject<SettingsStore>()
    var noticeStep by rememberSaveable {
        mutableIntStateOf(if (settings.getBoolean(LOGIN_NOTICES_ACCEPTED_KEY, false)) loginNotices.size else 0)
    }
    var selectedMethod by rememberSaveable(supportedLoginMethods) {
        mutableStateOf(supportedLoginMethods.first())
    }
    var loggedInUsername by remember { mutableStateOf<String?>(null) }
    val onLoginSuccess: (String) -> Unit = { username -> loggedInUsername = username }
    val motion = MaterialTheme.motionScheme
    val back: (() -> Unit)? = if (noticeStep in 1 until loginNotices.size) ({ noticeStep-- }) else onNavigateBack

    PlatformBackHandler(enabled = noticeStep in 1 until loginNotices.size) { noticeStep-- }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        // 没有返回按钮时也保留顶栏高度，声明页之间切换时内容不上下跳。
        TopAppBar(
            title = {},
            navigationIcon = {
                if (back != null) {
                    IconButton(onClick = back, modifier = Modifier.testTag("login_back")) {
                        Icon(AppIcons.ArrowBack, contentDescription = "返回")
                    }
                }
            },
            windowInsets = WindowInsets(0),
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
        AnimatedContent(
            targetState = noticeStep,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally(motion.defaultSpatialSpec()) { it / 4 * direction } + fadeIn(motion.defaultEffectsSpec()))
                    .togetherWith(slideOutHorizontally(motion.defaultSpatialSpec()) { -it / 4 * direction } + fadeOut(motion.defaultEffectsSpec()))
            },
            label = "login_step",
        ) { step ->
            val notice = loginNotices.getOrNull(step)
            if (notice != null) {
                LoginNoticePage(
                    step = step,
                    notice = notice,
                    onSecondaryAction = { openExternalUrl(notice.secondaryUrl) },
                    onConfirm = {
                        if (step == loginNotices.lastIndex) settings.putBoolean(LOGIN_NOTICES_ACCEPTED_KEY, true)
                        noticeStep++
                    },
                )
            } else {
                LoginMethodsPage(
                    selectedMethod = selectedMethod,
                    onSelectMethod = { selectedMethod = it },
                    onLoginSuccess = onLoginSuccess,
                )
            }
        }
    }

    loggedInUsername?.let { username ->
        AlertDialog(
            onDismissRequest = onLoginComplete,
            icon = { Icon(AppIcons.CheckCircle, contentDescription = null) },
            title = { Text("登录成功") },
            text = { Text("欢迎回来，$username") },
            confirmButton = {
                TextButton(onClick = onLoginComplete) {
                    Text("确定")
                }
            },
        )
    }
}

@Composable
private fun LoginMethodsPage(
    selectedMethod: LoginMethod,
    onSelectMethod: (LoginMethod) -> Unit,
    onLoginSuccess: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = LoginContentMaxWidth)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ShapedIcon(
                    AppIcons.Login,
                    size = 56.dp,
                    shape = IconShape.Flower,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
                Text("登录知乎", style = MaterialTheme.typography.headlineMediumEmphasized)
            }
            if (supportedLoginMethods.size > 1) {
                ChoiceButtonGroup(
                    options = supportedLoginMethods.map { ChoiceOption(it, it.label, it.icon, it.tag) },
                    selected = selectedMethod,
                    onSelect = onSelectMethod,
                )
            }
        }
        Box(
            modifier = Modifier
                .widthIn(max = LoginContentMaxWidth)
                .fillMaxWidth()
                .weight(1f),
        ) {
            when (selectedMethod) {
                LoginMethod.Phone -> PhoneLoginPane(onLoginSuccess)
                LoginMethod.Qr -> QrLoginPane(onLoginSuccess, onUsePhoneLogin = { onSelectMethod(LoginMethod.Phone) })
                LoginMethod.Web -> WebLoginPane(onLoginSuccess)
            }
        }
    }
}

@Composable
private fun LoginNoticePage(
    step: Int,
    notice: LoginNotice,
    onSecondaryAction: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .testTag("login_notice_step_${step + 1}"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 内容不超过一屏时居中，超过时（横屏手机、大字号）在这个区域内滚动，按钮始终留在底部。
        Box(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = LoginContentMaxWidth)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.semantics { contentDescription = "第 ${step + 1} 步，共 ${loginNotices.size} 步" },
                ) {
                    Text(
                        "${step + 1} / ${loginNotices.size}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
                ShapedIcon(
                    notice.icon,
                    size = 112.dp,
                    shape = notice.shape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
                Text(
                    notice.title,
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    textAlign = TextAlign.Center,
                )
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Text(
                        notice.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp),
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .widthIn(max = LoginContentMaxWidth)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MediumActionButton(
                text = notice.secondaryButtonText,
                onClick = onSecondaryAction,
                emphasis = ActionEmphasis.Outlined,
                icon = AppIcons.OpenInNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_notice_secondary_action"),
            )
            MediumActionButton(
                text = notice.confirmButtonText,
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_notice_confirm"),
            )
        }
    }
}

private class LoginNotice(
    val title: String,
    val icon: AppIcon,
    val shape: IconShape,
    val message: String,
    val secondaryButtonText: String,
    val secondaryUrl: String,
    val confirmButtonText: String,
)

private val loginNotices = listOf(
    LoginNotice(
        title = "这是第三方客户端",
        icon = AppIcons.Gavel,
        shape = IconShape.Cookie,
        message = "我清楚，本应用由开源社区开发和维护，不由知乎官方开发并运营，也不受到知乎官方的承认或支持，使用本应用的一切后果由我本人承担。我可以在 https://www.zhihu.com/app/ 下载官方应用。",
        secondaryButtonText = "下载官方 App",
        secondaryUrl = "https://www.zhihu.com/app/",
        confirmButtonText = "我已了解",
    ),
    LoginNotice(
        title = "遵守知乎协议",
        icon = AppIcons.License,
        shape = IconShape.Sunny,
        message = "在使用本应用的过程中，我承诺遵守知乎使用协议 https://www.zhihu.com/term/zhihu-terms 。我保证在使用过程中不侵犯知乎及其他作者的著作权，使用本应用产生的一切输出仅用于个人浏览和备份，不会进行传播等其他影响作者著作权的行为。",
        secondaryButtonText = "查看协议",
        secondaryUrl = "https://www.zhihu.com/term/zhihu-terms",
        confirmButtonText = "同意并继续",
    ),
)
