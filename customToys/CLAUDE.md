# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android Glyph Toy application for Nothing Phone devices. It implements a **Clock Toy** that renders the current time, date, and day on the device's 25×25 LED Glyph Matrix. The toy appears in the system's Glyph Toys Manager and can be activated by users.

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device)
./gradlew connectedAndroidTest
```

- **Min SDK**: 34 (Android 14 required — Nothing Phone 3 / Phone 2 / Phone 2a)
- **Compile/Target SDK**: 34
- The Glyph Matrix SDK is bundled as `app/libs/glyph-matrix-sdk-1.0.aar`

## Architecture

The code uses a layered architecture with a **Strategy pattern** for the display:

```
MyCustomToyService (Android Service)
    └── ClockToyEngine (business logic, no Android UI dependency)
            └── GlyphMatrixDisplay (interface)
                    ├── RealGlyphMatrixDisplay   → GlyphMatrixManager (hardware)
                    └── SimulatedGlyphMatrixDisplay → StateFlow (for simulator UI)
```

### Key Files

| File | Role |
|------|------|
| `MyCustomToyService.kt` | Android Service; manages `GlyphMatrixManager` lifecycle and routes `GlyphToy.MSG_GLYPH_TOY` button events to `ClockToyEngine` via `Messenger`/`Handler` |
| `ClockToyEngine.kt` | Core logic: builds `GlyphMatrixFrame` every second, handles 12h/24h toggle (`EVENT_CHANGE`) and AOD (`EVENT_AOD`) |
| `GlyphMatrixDisplay.kt` | Single-method interface: `setFrame(frame: IntArray)` |
| `RealGlyphMatrixDisplay.kt` | Sends frames to hardware via `GlyphMatrixManager` |
| `SimulatedGlyphMatrixDisplay.kt` | Exposes frames via `StateFlow<IntArray>` for the simulator |
| `SimulatorActivity.kt` | Dev-only activity; renders a live 25×25 LED grid using Compose Canvas |
| `GlyphMatrixVisualization.kt` | Compose component that draws the LED grid |
| `MainActivity.kt` | Launcher screen; guides users to Glyph Toys Manager or opens simulator |

### Glyph Matrix SDK Concepts

- **Frame data**: `IntArray` of 625 values (25×25), each 0–2047 (brightness)
- **Object placement**: `GlyphMatrixObject.Builder` with `.setText()`, `.setTextStyle("tall")`, `.setPosition(x, y)`
- **Frame assembly**: `GlyphMatrixFrame.Builder` with `.addTop()`, `.addMid()`, `.addLow()`, then `.build(context)`
- **Rendering**: `frame.render()` returns the `IntArray` passed to `display.setFrame()`
- **Device registration**: `mGM.register(Glyph.DEVICE_23112)` in `GlyphMatrixManager.Callback.onServiceConnected`

### Service Registration (AndroidManifest.xml)

Each Glyph Toy must be registered as a `<service>` with:
- `android:exported="true"`
- Intent filter: `com.nothing.glyph.TOY`
- Required metadata: `com.nothing.glyph.toy.name`, `com.nothing.glyph.toy.image`
- Optional metadata: `com.nothing.glyph.toy.longpress` (`"1"` to enable), `com.nothing.glyph.toy.aod_support`

### Character Width Table (for centering on 25-column matrix)

`ClockToyEngine` manually computes pixel widths for centered text layout:
- Width 1: `: . (space)`
- Width 3: `1 i t`
- Width 4: most letters/digits
- Width 5: `m w`
- Gap of 1 pixel between characters

## Testing Without Hardware

Open `SimulatorActivity` (via "シミュレーターを開く" button in `MainActivity`) to preview the toy on-screen. The simulator uses `SimulatedGlyphMatrixDisplay` and `GlyphMatrixVisualization` — no Nothing Phone required.
