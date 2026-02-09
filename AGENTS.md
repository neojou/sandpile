# AGENTS.md - sandpile Project Guidelines

## Project Overview
- **Project Name**: sandpile
- **Description**: A Kotlin Multiplatform implementation of the **Abelian Sandpile Model** (阿貝爾沙堆模型)，模擬沙粒堆積與崩塌過程，並以 Compose Multiplatform 視覺化呈現對稱、fractal-like 的曼陀羅 (Mandala) 圖案。
- **Platforms**: 
  - Desktop (JVM) – macOS / Windows / Linux
  - Web (Kotlin/JS + WebAssembly / Wasm)
- **Core Technologies**:
  - Kotlin Multiplatform (shared logic in commonMain)
  - Compose Multiplatform (UI / Canvas rendering)
  - Gradle (Kotlin DSL) for build system
- **Purpose**: 展示沙堆模型的數學美感與視覺效果，支持互動（加沙、重置、開始/暫停等）。
- **Current Status**: 基本模型與視覺化完成，正在新增 Start/Stop 按鈕等功能。
- **Online Demo**: https://neojou.github.io/mandala/sandpile/ (建議使用最新 Chrome/Edge/Safari)

## Build / Lint / Test Commands
(基於標準 Kotlin Multiplatform + Compose 專案慣例與 README 說明)

- 全專案建置與檢查  
  `./gradlew build`

- 執行桌面版 (Desktop/JVM)  
  `./gradlew :composeApp:run`

- 產生 WebAssembly (Wasm) / Kotlin/JS 發行版本  
  `./gradlew wasmJsBrowserDistribution`  
  → 輸出在 `composeApp/build/dist/wasmJs/productionExecutable/`  
  → 使用簡單 HTTP server 開啟，例如：  
  `python3 -m http.server 8000 --directory composeApp/build/dist/wasmJs/productionExecutable`  
  → 瀏覽器訪問 http://localhost:8000

- 清理建置產物  
  `./gradlew clean`

- 執行所有測試 (若有單元測試)  
  `./gradlew commonTest` 或 `./gradlew allTests`

- 單一測試 (範例，需確認實際 package/class)  
  `./gradlew :commonTest --tests "com.neojou.sandpile.SomeTest.testSandpileCollapse"`

- Lint / 格式檢查 (若有 ktlint/detekt 設定)  
  `./gradlew ktlintCheck detekt`  
  格式化：`./gradlew ktlintFormat`

## Code Style Guidelines
(從專案結構、Kotlin Multiplatform 慣例與 Compose 最佳實務推斷)

- **語言與版本**：Kotlin 1.9+ (或更高)，強烈建議使用 Coroutines + Flow 處理非同步與狀態變化。
- **Imports**：
  - 排序規則：kotlinx → android/compose → 第三方 → 專案內部
  - 避免 wildcard import (`import ....*`)，盡量 explicit
- **Naming Conventions**：
  - 變數/函數：camelCase
  - 類別/介面：PascalCase
  - 常數：SCREAMING_SNAKE_CASE 或 const val 以大寫開頭
- **Formatting**：
  - 縮排：4 spaces (ktlint 預設)
  - 行長：建議 ≤ 120 字元
  - 使用 ktlint 強制格式一致性
- **Types & Null Safety**：
  - 優先使用 explicit types，減少 `Any` 或 `Unit` 濫用
  - 盡量 immutable data structures (val > var)
  - 使用 sealed class / sealed interface 處理狀態與錯誤
- **Error Handling**：
  - 優先使用 `Result<T>` 或自訂 sealed Error class
  - 避免直接 throw Exception，除非是致命錯誤
- **Compose 相關**：
  - 使用 `@Composable` 函數清晰分離 UI 與邏輯
  - 狀態管理優先使用 `mutableStateOf` + Flow / StateFlow
  - 避免 side-effect 在 Composable 內，移到 ViewModel 或 coroutine scope
- **其他偏好**：
  - 所有 public API 建議加 KDoc
  - 單元測試使用 Kotest + coroutine-test (若有測試)
  - 避免 deprecated API (特別是舊版 Compose)

## Project Structure
```text
sandpile/
├── gradle/                     # Gradle wrapper
├── images/                     # README 用的預覽圖
├── kotlin-js-store/            # Wasm 建置產物 (generated)
├── src/
│   ├── commonMain/             # 共享核心邏輯：沙堆模型、狀態計算、數學運算
│   ├── desktopMain/            # JVM 桌面應用入口 (Compose Desktop)
│   ├── wasmMain/               # WebAssembly / Compose for Web 入口
│   └── ...                     # 其他可能的 source sets (androidMain 等)
├── .gitignore
├── build.gradle.kts            # 主建置腳本
├── gradle.properties
├── gradlew / gradlew.bat       # Gradle wrapper
├── release-to-github.sh        # 發行到 GitHub 的腳本
├── settings.gradle.kts         # 專案名稱與模組設定
└── README.md                   # 專案說明與執行指南
```


## Additional Rules / Preferences
- 優先使用 immutable data 與 functional programming 風格。
- 模型邏輯 (commonMain) 應保持純粹函數，無 side-effect。
- UI 層 (Compose) 只負責渲染與事件處理，狀態變化透過 shared ViewModel 或 Flow。
- Commit 前建議跑 `./gradlew build` 與格式化。
- 任何新功能應考慮同時支援 Desktop 與 Web (Wasm) 平台。
- 未來擴展方向 (從 README)：更多配色、不同崩塌規則、六角格、動畫錄製等。


