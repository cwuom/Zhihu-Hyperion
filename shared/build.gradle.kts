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

import com.github.zly2006.zhihu.buildlogic.javafx
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("zhihu.kmp.library")
    id("zhihu.kmp.compose")
    id("zhihu.kmp.android.variants")
    alias(libs.plugins.kotlin.serialization)
}

// 液态玻璃的桌面依赖使用 Java 21 字节码，测试运行时不能随 Gradle 的 Java 17 启动环境变化。
tasks.named<Test>("jvmTest") {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}

// 评论表情和 iOS/macOS 应用包里复制的是同一份 misc/ 文件；桌面版放进 classpath，安装包从任意工作目录启动都能读到。
val desktopEmojiResources =
    tasks.register<Sync>("syncDesktopEmojiResources") {
        into(layout.buildDirectory.dir("generated/desktopEmojiResources"))
        into("misc") {
            from(rootProject.layout.projectDirectory.file("misc/emoji_mapping.json"))
            into("emojis") {
                from(rootProject.layout.projectDirectory.dir("misc/emojis")) { include("*.png") }
            }
        }
    }

kotlin {
    android {
        namespace = "com.github.zly2006.zhihu.shared"
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            api(projects.core.model)
            api(projects.core.navigation)
            api(projects.core.network)
            api(projects.core.account)
            api(projects.core.settings)
            api(projects.core.designsystem)
            api(projects.core.platform)
            api(projects.core.nlp)
            api(projects.core.notification)
            api(projects.core.data)
            api(projects.core.ui)
            api(projects.core.markdown)
            implementation(projects.feature.ai)
            implementation(projects.feature.editor)
            implementation(projects.feature.update)
            implementation(projects.feature.video)
            api(projects.core.database)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(projects.core.glass)
            implementation(projects.core.icons)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.core)
            implementation(libs.jetbrains.navigation.compose)
            implementation(libs.jetbrains.navigationevent.compose)
            implementation(libs.jetbrains.lifecycle.runtime.compose)
            implementation(libs.jetbrains.lifecycle.viewmodel.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.material.kolor)
            implementation(libs.ksoup)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.io.core)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlincrypto.hmac.sha1)
            implementation(libs.aboutlibraries.compose.m3)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.browser)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.livedata.ktx)
                implementation(libs.androidx.media)
                implementation(libs.androidx.webkit)
                implementation(libs.zxing.android.embedded)
                implementation(libs.zxing.core)
                implementation(libs.coil.gif)
                implementation(libs.coil.network.ktor3)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.telephoto.zoomable.image.coil3)
                implementation(libs.jsoup)
            }
        }
        jvmMain {
            // Desktop export reuses the Android WebView/export assets; they ship on the JVM classpath.
            resources.srcDir("src/androidMain/assets")
            resources.srcDir(desktopEmojiResources)
            dependencies {
                implementation(libs.compose.ui.backhandler)
                implementation(libs.androidx.navigationevent)
                implementation(libs.coil.network.ktor3)
                implementation(libs.androidx.sqlite.bundled)
                implementation(compose.desktop.currentOs)
                implementation(libs.zxing.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.kotlinx.coroutines.swing)
                // JavaFX WebView hosts the desktop risk-control verification page. compileOnly keeps the
                // platform-specific jars out of consumers; desktopApp ships them itself.
                listOf("base", "graphics", "controls", "web", "swing").forEach { module ->
                    compileOnly(javafx(module))
                }
            }
        }
        macosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmTest.dependencies {
            implementation(libs.jsoup)
            implementation(libs.compose.ui.test)
        }
    }
}
