# Charging Monitor Overlay (Root) 🔋

A lightweight Android overlay application that displays **real battery
charging data** directly on your screen while charging.

Designed for AMOLED devices with: - Burn-in safe pixel shifting - Real
battery voltage, current and wattage - Root-based accurate sysfs
readings - Minimal resource usage

------------------------------------------------------------------------

# 📱 Features

-   🔋 Battery percentage
-   ⚡ Real battery voltage (V)
-   🔌 Real battery current (A)
-   🔥 Real battery charging power (W)
-   ⏳ Estimated time until full
-   🛡 Burn-in safe pixel shifting (moves overlay periodically)
-   🚀 Foreground service for stability
-   🔒 Root-based data reading from `/sys/class/power_supply/`
-   ⚙ Show only while charging (v1.8+)
-   ▶ Start / Stop overlay controls

------------------------------------------------------------------------

# ⚠ Root Required

This app reads battery data directly from:

    /sys/class/power_supply/battery/

Root (Magisk or similar) is required for accurate: - current_now -
voltage_now - power_now (if available)

Without root, Android restricts access.

------------------------------------------------------------------------

# 🔧 Installation

## Option 1 -- Install APK

1.  Download the latest release APK from GitHub.
2.  Enable "Install unknown apps".
3.  Install APK.
4.  Grant:
    -   "Appear on top"
    -   Root permission (Magisk will prompt)
5.  Press **START OVERLAY**.

## Option 2 -- Build via GitHub Actions

1.  Push project to your GitHub repository.
2.  GitHub Actions will automatically build debug APK.
3.  Download artifact from Actions tab.
4.  Install on device.

------------------------------------------------------------------------

# 📊 Why Charger Shows 15W but App Shows 11--12W?

This is completely normal.

### 🔌 Cable Meter = Charger Output

Example:

    9V × 1.6A = 14.4W

This is power coming FROM the charger.

------------------------------------------------------------------------

### 📱 App = Real Battery Power

Example:

    3.85V × 3.08A = 11.9W

This is power going INTO the battery.

------------------------------------------------------------------------

### ⚡ Why the difference?

Between charger and battery there are:

-   DC-DC conversion losses
-   Heat
-   PMIC efficiency loss
-   Internal phone consumption
-   Cable resistance

Typical charging efficiency: **75--85%**

So:

15W from charger\
→ \~11--12W into battery

Your app shows the real battery intake power.

------------------------------------------------------------------------

# 🛡 Burn-In Protection

To protect AMOLED panels, the overlay:

-   Moves slightly every few seconds
-   Shifts position randomly within safe bounds
-   Prevents static pixel damage

------------------------------------------------------------------------

# 📌 Changelog

## v1.8

-   Added "Show only while charging" option
-   Improved overlay visibility logic
-   Stability improvements

## v1.7

-   Added AMOLED burn-in safe pixel shifting
-   Improved root execution reliability
-   Minor UI refinements

## v1.6

-   Improved root current detection
-   Added current_avg fallback
-   Improved watt calculation accuracy

## v1.5

-   Root charging detection
-   Start / Stop overlay buttons
-   Improved service lifecycle

## v1.4

-   Minimal stable build
-   Overlay stability improvements

## v1.3

-   GitHub-ready structure
-   Foreground service stabilization

## v1.2

-   Crash fixes
-   Manifest corrections

## v1.1

-   Initial root sysfs reading
-   Overlay service implementation

## v1.0

-   Initial project release
-   Basic overlay display

------------------------------------------------------------------------

# 🧠 Technical Notes

-   Voltage is read in microvolts → converted to volts
-   Current is read in microamps → converted to amps
-   Wattage = Voltage × Current
-   Time to full is read from `time_to_full_now` when available

------------------------------------------------------------------------

# 🚀 Future Ideas

-   Charging efficiency % calculation
-   Temperature monitoring
-   Graph history view
-   Lock screen specific mode

------------------------------------------------------------------------

# 📜 License

Personal project -- free to modify and improve.
