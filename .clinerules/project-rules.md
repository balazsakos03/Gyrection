# Cline Rules for Gyrection Project

## Tiltott fájlok és mappák (Ne olvasd be ezeket token takarékosság miatt):
- `gyrection-android/app/src/main/res/` (Kifejezetten tiltva: képek, ikonok, mipmap, drawable, értékek xml-jei)
- `gyrection/target/`
- `gyrection-android/build/`
- `gyrection-android/app/build/`
- `.gradle/`
- `.git/`

## Projekt Architektúra Áttekintés:
- **Rust backend/desktop (`/gyrection`):** Slint UI alapon (`app-window.slint`), fő logika a `src/main.rs`-ben[cite: 1].
- **Android kliens (`/gyrection-android`):** Kotlin / Jetpack Compose alapú app[cite: 1]. Kommunikáció (USB, UDP, WiFi) a `communication/` mappában, szenzorok kezelése a `sensor/` mappában[cite: 1].

## Alapvető utasítások:
- Csak azokat a konkrét fájlokat olvasd be tartalmuk szerint, amiket kifejezetten kérek vagy amik a hibajavításhoz feltétlenül szükségesek.
- Ne szkenneld át automatikusan az Android erőforrás (res) mappákat.