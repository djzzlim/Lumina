# Lumina: Secure Forensic Browser

Lumina is a privacy-first, multi-profile Android browser built on **GeckoView**. It is designed for users who require high-level forensic protection and granular control over their digital fingerprint.

## 🛡️ Core Security Philosophy
Lumina distinguishes itself by treating every browsing session as a forensic-sensitive event. It focuses on two main pillars:
1. **Data Isolation:** Separating browser identities through an encrypted multi-profile system.
2. **Persistence Prevention:** Ensuring no trace of activity remains in RAM or storage after session closure.

---

## ✨ Key Features

### 👤 Multi-Profile Management
* **Isolated Identities:** Create multiple profiles (e.g., Work, Personal, Research), each with its own set of "Lumina" bookmarks.
* **Database Encryption:** All profile metadata and site configurations are stored using **SQLCipher (v4)** with AES-256 encryption.

### 🕵️ Advanced Fingerprint Protection (Per-Site)
Configure unique security headers and browser behavior for every saved site:
* **Anti-Fingerprinting (AFP):** Toggle GeckoView's advanced tracking protection.
* **Identity Randomization:** User-Agent randomization and Locale/Timezone spoofing.

### 🔒 Forensic Hardening
* **RAM Purging:** Explicitly clears the GeckoView runtime and shuts down the browser process on exit to zero out volatile memory.
* **Screen Privacy:** Uses `FLAG_SECURE` to block screenshots and hide content from the Android Recents (Multitasking) screen.
* **Clean Cold-Start:** Automatically clears session history and temporary storage upon every fresh application launch.

### 🌐 Secure Networking
* **Custom DNS/DoH:** Integrated support for DNS-over-HTTPS providers to bypass ISP-level tracking.
* **Private-Mode by Default:** Every tab runs in an isolated private session.

---

## 🛠️ Tech Stack
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Modern, declarative UI)
* **Browser Engine:** Mozilla GeckoView (Standard-compliant, private engine)
* **Dependency Injection:** Hilt (Dagger)
* **Local Database:** Room + SQLCipher (Encrypted persistence)
* **Navigation:** Compose Navigation
* **Camera:** CameraX + ML Kit (For secure QR code scanning)

---

## 🏗️ Detailed Project Structure

The project follows a feature-based modular architecture designed for high cohesion and security isolation.

```
app/src/main/java/com/example/lumina/
├── core/                        # Singleton Managers & Data Layer
│   ├── database/                # Encrypted Persistence (Room + SQLCipher)
│   │   ├── LuminaDatabase.kt    # Main DB with migration logic
│   │   ├── LuminaInfo.kt        # Tab/Site Entity
│   │   ├── Profile.kt           # User Profile Entity
│   │   ├── DatabaseModule.kt    # Hilt DB providers
│   │   └── SecurityUtils.kt     # Keystore-backed key generation
│   ├── LuminaRepository.kt      # Site data orchestration
│   ├── ProfileRepository.kt     # Profile data orchestration
│   ├── ProfileManager.kt        # Active profile state & session init
│   └── AppPreferences.kt        # Secure DataStore for app settings
├── features/                    # UI & Feature Logic
│   ├── home/                    # Main Dashboard (Grid view of sites)
│   ├── browser/                 # GeckoView Integration (Session & UI)
│   ├── profiles/                # Profile Creation & Management
│   ├── edit_lumina/             # Advanced Security/Fingerprint settings
│   ├── new_lumina/              # Site creation & Scanner integration
│   ├── qr_scanner/              # ML Kit based QR Scanner
│   ├── advanced_options/        # Fine-grained browser configuration
│   └── settings/                # DNS and Search Engine preferences
├── navigation/                  # App Graph & Screen Routes
├── ui/theme/                    # Design System & Theming
├── LuminaApplication.kt         # Hilt App & Profile Initialization
└── MainActivity.kt              # Entry Point (FLAG_SECURE, Lifecycle Cleanup)
```

---

## 🚀 Roadmap: Intelligent Threat Protection

Currently in development:
* **Offline Phishing Detection:** Integration of a local **MLP (Multi-Layer Perceptron) Model**.
* **On-Device Inference:** Classification of URLs using an offline model to ensure that even "phishing checks" don't leak your browsing data to a third-party API.

---

## ⚙️ Getting Started

### Prerequisites
* Android Studio Ladybug or newer.
* Android SDK 34+.
* A device/emulator with ARM64 architecture (preferred for GeckoView).

### Setup
1. Clone the repository.
2. The project uses **SQLCipher**. On the first launch, a secure key is generated and stored in the Android Keystore.
3. Build and run the `:app` module.

---

## 📄 License
Copyright © 2025 Lumina Project. 
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
