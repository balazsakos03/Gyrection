# Gyrection

**Phone motion controller – turn your Android phone into a wireless Xbox 360 gamepad for Windows.**

Gyrection consists of two applications that work together over the local network:

- **📱 Android app** – reads the phone's rotation sensor, converts it into steering/throttle/brake/handbrake values, and sends them via UDP to the PC.
- **🖥️ Windows app (Rust + Slint)** – receives the UDP data, displays real-time telemetry, and emulates a virtual Xbox 360 controller via the ViGEmBus driver – so any game that supports Xbox controllers instantly recognizes the phone as a real gamepad.

---

## 🚀 Quick Start

### 1️⃣ Prerequisites
1. **Windows PC** – install the **[ViGEmBus driver](https://github.com/nefarius/ViGEmBus/releases)** (required once, takes 1 minute).
2. **Android phone** – Android 8.0 (API 26) or higher, with a rotation sensor.
3. **Network** – both devices must be on the same local network (Wi-Fi, USB tethering, or Ethernet).

### 2️⃣ Run the PC server
Download `gyrection.exe` (or build from source) and run it. If the ViGEmBus driver is installed, the "VIRTUAL CONTROLLER" card will show **"Emulated"** in green.

### 3️⃣ Install and run the Android app
- Install the **APK** on your phone (enable "Install from unknown sources" if needed).
- Open the app – it will **automatically find your PC** via broadcast discovery and connect.
- **No IP address needs to be typed!** The app auto-connects on startup.

### 4️⃣ Calibrate and play
1. Hold the phone in your **neutral driving position** (landscape, flat like a steering wheel).
2. Tap **"Calibrate Center"** – this sets the current orientation as the zero point.
3. **Tilt forward** → throttle (right trigger / RT)
4. **Tilt backward** → brake (left trigger / LT)
5. **Tilt left/right** → steering (left thumbstick)


---

## 🎮 Xbox 360 Controller Mapping

| Phone motion | Xbox 360 control | Gamepad field |
|---|---|---|
| Tilt forward (pitch up) | Right trigger (RT) | `right_trigger` (0–255) |
| Tilt backward (pitch down) | Left trigger (LT) | `left_trigger` (0–255) |
| Rotate left/right (yaw) | Left thumbstick X | `thumb_lx` (-32768..32767) |
| Handbrake button | A button | `buttons` (A flag) |

The mapping is **proportional**: the further you tilt, the more the corresponding control is activated. Calibration ensures the neutral position matches where you hold the phone.

---

## 🧪 Testing Without a Game

1. Ensure the phone is connected and the PC shows **"Connected"**.
2. Press **Win + R**, type `joy.cpl`, press Enter.
3. You should see **"Xbox 360 Controller for Windows"**.
4. Open **Properties** → the thumbstick, triggers, and buttons move in real time as you tilt the phone.



---

## 🖥️ Windows App (Rust + Slint) – Technical Details

### Dependencies
- **[Slint](https://github.com/slint-ui/slint)** (v1.17.1) – Native GUI framework.
- **[vigem-client](https://crates.io/crates/vigem-client)** (v0.1.4) – Virtual Xbox 360 controller emulation via ViGEmBus.

### Source Structure
```
gyrection/
├── src/
│   └── main.rs              – UDP server, ViGEm emulation, Slint UI updates
├── ui/
│   └── app-window.slint     – Slint UI layout (responsive, dark theme)
├── build.rs                 – Slint build script
├── Cargo.toml               – Rust dependencies
└── Cargo.lock
```

### Main Loop
1. **Startup** – binds UDP socket on `0.0.0.0:9999`, connects to ViGEmBus, creates a virtual Xbox 360 Wired controller.
2. **Receive loop** – reads UDP datagrams (200ms timeout):
   - **Discovery** – responds to `GYRECTION_DISCOVERY` with the PC's IP.
   - **Data** – parses the 41-byte packet, updates the Slint UI, and updates the virtual Xbox 360 controller.
   - **Timeout** – if no data arrives for 1.5 seconds, marks the connection as lost.
3. **UI updates** – via `slint::invoke_from_event_loop` for thread safety.

### UI Layout
The Slint window is **responsive**: minimum size 860×560, freely resizable. The "LIVE SENSOR DATA" panel stretches to fill available space.

---

## 📱 Android App – Technical Details

### Architecture
```
com.example.gyrection
├── communication/
│   ├── Connection.kt         – Interface (connect/disconnect/send)
│   ├── UdpConnection.kt      – UDP implementation with broadcast discovery
│   ├── WifiConnection.kt     – TCP implementation (legacy)
│   └── UsbConnection.kt      – USB stub (placeholder)
├── controller/
│   └── ControllerState.kt    – ControllerMapper, dead zones, logic
├── protocol/
│   └── GyrectionPacket.kt    – 41-byte binary packet format (little-endian)
├── sensor/
│   ├── Quaternion.kt         – Quaternion math (normalize, inverse, multiply)
│   ├── OrientationProcessor.kt – Quaternion → Pitch/Yaw/Roll with calibration
│   └── SensorManager.kt      – Android SensorEventListener
└── ui/
    ├── GyrectionApp.kt       – Main Compose layout (landscape, two columns)
    └── components/
        ├── ConnectionCard.kt – PC connection status + auto-connect button
        ├── OrientationCard.kt – Sensor data, gamepad output, telemetry bars
        └── HandbrakeButton.kt – Large pressable button (red when pressed)
```

### Packet Format (41 bytes, little-endian)
```
Offset  Size  Field          Description
──────────────────────────────────────────────
 0       1     magic          0x01
 1–4     4     steering       f32 (-1.0..1.0)
 5–8     4     throttle       f32 (0.0..1.0)
 9–12    4     brake          f32 (0.0..1.0)
13–16    4     handbrake      f32 (0.0 or 1.0)
17–20    4     qw (quaternion W)
21–24    4     qx (quaternion X)
25–28    4     qy (quaternion Y)
29–32    4     qz (quaternion Z)
33–36    4     pitch (rotY)   degrees
37–40    4     yaw   (rotZ)   degrees
──────────────────────────────────────────────
Total: 41 bytes
```

### Auto-Discovery
The phone uses a two-layer broadcast discovery:
1. **Limited broadcast** (`255.255.255.255:9999`) – works on most networks.
2. **Per-interface subnet broadcast** – iterates every active network interface (Wi-Fi, USB tethering, VPN) and sends the discovery message to each interface's subnet broadcast address. This ensures USB tethering and multi-network setups work.

The PC responds with: `GYRECTION_IP <ip_address>`.

---

## 🔧 Building from Source

### Windows (Rust)
```bash
cd gyrection
cargo build --release
# Executable: target/release/gyrection.exe
```

### Android
```bash
cd gyrection-android
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## 🛠️ Configuration & Tuning

### Controller Dead Zones (Android – `ControllerState.kt`)
```kotlin
class ControllerMapper(
    private val steeringDeadZone: Float = 1.0f,   // Ignore rotations below 1°
    private val tiltDeadZone: Float = 5.0f,       // Ignore tilts below 5°
    private val maxPhoneTilt: Float = 45.0f       // Full throttle/brake at 45° tilt
)
```

### ViGEmBus Driver
Required for the virtual Xbox 360 controller. Download: [ViGEmBus Releases](https://github.com/nefarius/ViGEmBus/releases)

---

## 📋 FAQ

**Q: The phone doesn't find the PC automatically.**
> A: Make sure both devices are on the same network. Check if your router has "AP Isolation" enabled. The app also supports USB tethering.

**Q: The Xbox controller doesn't appear in games.**
> A: Ensure the ViGEmBus driver is installed and the "VIRTUAL CONTROLLER" card shows **"Emulated"** (green).

**Q: Can I use USB tethering instead of Wi-Fi?**
> A: Yes! The broadcast discovery now sends to all active network interfaces, including USB tethering.

**Q: The controls feel too sensitive / not sensitive enough.**
> A: Adjust the `maxPhoneTilt` and dead zone values in `ControllerState.kt`. A lower `maxPhoneTilt` (e.g., 30°) makes controls more responsive.

---

## 🎯 Roadmap

- [x] UDP-based sensor data streaming (phone → PC)
- [x] Broadcast auto-discovery (no manual IP entry)
- [x] Multi-interface discovery (USB tethering support)
- [x] Slint GUI with responsive layout
- [x] Xbox 360 controller emulation (ViGEmBus)
- [x] Forza / game testing verified
- [ ] Fine-tuning curves (steering response, throttle ramp)
- [ ] Button mapping customization
- [ ] Wired / USB HID gamepad emulation as an alternative
- [ ] Release builds with proper code signing

---

*Built with Rust, Slint, Kotlin, Jetpack Compose, and ViGEmBus.*