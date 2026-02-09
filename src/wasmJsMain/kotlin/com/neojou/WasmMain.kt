package com.neojou

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

/**
 * WebAssembly (Wasm) entry point for the sandpile app.
 *
 * This function creates a Compose render target via [ComposeViewport] and mounts it
 * into the DOM (currently `document.body!!`), then hosts the shared [App] composable.
 *
 * Notes:
 * - Opts into [ExperimentalComposeUiApi] because the viewport API is experimental.
 * - Some JS interop patterns (e.g., `js()` inside coroutines) may be restricted on Wasm;
 *   prefer top-level calls or an `expect/actual` wrapper if JS-triggered behavior is needed.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
    }

    // NEW: 移除 launch + dispatchEvent（Wasm 限制 js() in coroutine）；用佈局 min size 解決初始 0
    // 如果還需 top-level JS 觸發，未來用 expect/actual 包裝
}