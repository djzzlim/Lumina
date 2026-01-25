# Lumina: Secure Forensic Browser

Lumina is a privacy-first, multi-profile Android browser built on **GeckoView**. It is designed for users who require high-level forensic protection and granular control over their digital fingerprint.

## 🛡️ Core Security Philosophy
Lumina distinguishes itself by treating every browsing session as a forensic-sensitive event. It focuses on three main pillars:
1. **Data Isolation:** Separating browser identities through an encrypted multi-profile system.
2. **Persistence Prevention:** Ensuring no trace of activity remains in RAM or storage after session closure.
3. **Intelligent Threat Protection:** Using local, privacy-preserving AI to detect threats without leaking data.

---

## ✨ Key Features

### 👤 Multi-Profile Management
* **Isolated Identities:** Create multiple profiles (e.g., Work, Personal, Research), each with its own set of "Lumina" bookmarks.
* **Database Encryption:** All profile metadata and site configurations are stored using **SQLCipher (v4)** with AES-256 encryption.

### 🤖 AI Engine (Phishing Detection)
Lumina's phishing detection is powered by a specialized Transformer model optimized for mobile efficiency.
- **Model:** [URLBERT Tiny v4 Phishing Classifier](https://huggingface.co/CrabInHoney/urlbert-tiny-v4-phishing-classifier)
- **Architecture:** BERT-Tiny (4.4M parameters)
- **Implementation:** Quantized and exported to **ONNX Runtime** for sub-20ms local inference on Android.
- **Privacy:** All inference runs offline; no URLs are sent to the cloud.
- **Custom Tokenization:** Implements a punctuation-aware WordPiece tokenizer optimized for URL structure analysis.

### 🕵️ Advanced Fingerprint Protection (Per-Site)
Configure unique security headers and browser behavior for every saved site:
* **Anti-Fingerprinting (AFP):** Toggle GeckoView's advanced tracking protection (Resist Fingerprinting).
* **Identity Randomization:** User-Agent randomization and Locale/Timezone spoofing.
* **Hardware Spoofing:** Mask WebGL, AudioContext, and Screen dimensions.

### 🔒 Forensic Hardening
* **RAM Purging:** Explicitly clears the GeckoView runtime and shuts down the browser process on exit.
* **Screen Privacy:** Uses `FLAG_SECURE` to block screenshots and hide content from the Android Recents screen.
* **Auto-Close Timer:** Configurable inactivity timeout that performs a forensic wipe of session data.

### 🌐 Secure Networking
* **Custom DNS/DoH:** Support for Cloudflare, Google, AdGuard, Quad9, or any custom DoH endpoint.
* **Hybrid Safe Browsing:** Choice between Google Safe Browsing API or Lumina's offline ML model.

---

## 🛠️ Tech Stack
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose
* **Browser Engine:** Mozilla GeckoView
* **AI/ML:** ONNX Runtime + URLBERT Tiny v4
* **Dependency Injection:** Hilt (Dagger)
* **Local Database:** Room + SQLCipher
* **Camera:** CameraX + ML Kit (For secure QR code scanning)

---

## 🏗️ Detailed Project Structure

```
app/src/main/java/com/example/lumina/
├── core/                        # Core Logic & Data Layer
│   ├── browser/                 # GeckoView Management
│   ├── database/                # Encrypted Persistence (Room + SQLCipher)
│   ├── di/                      # Dependency Injection (Hilt)
│   ├── ml/                      # Local AI Phishing Protection (ONNX)
│   ├── utils/                   # UI & Data Utilities
│   ├── AppPreferences.kt        # Secure DataStore settings
│   ├── LuminaRepository.kt      # Main Data Orchestrator
│   ├── ProfileManager.kt        # Profile Lifecycle Logic
│   └── ProfileRepository.kt     # Profile Persistence logic
├── features/                    # UI & Feature Logic (MVI/MVVM)
│   ├── advanced_options/        # Site-specific forensic settings
│   ├── browser/                 # Browser Engine & Safety Interceptors
│   ├── edit_lumina/             # Bookmark/Site editing
│   ├── home/                    # Main Lumina Dashboard
│   ├── new_lumina/              # Secure site creation
│   ├── profiles/                # Identity management
│   ├── qr_scanner/              # Secure QR code integration
│   └── settings/                # Global privacy configuration
├── navigation/                  # Compose Navigation & Routes
├── ui/theme/                    # Design System (Material 3)
└── LuminaApplication.kt         # App Entry & Hilt Setup
```

---

## ⚙️ Getting Started

### Prerequisites
* Android Studio Ladybug or newer.
* Android SDK 34+.
* **urlbert_phishing.onnx** and **vocab.txt** must be placed in `app/src/main/assets/`.

### Setup
1. Clone the repository.
2. The project uses **SQLCipher**. On the first launch, a secure key is generated and stored in the Android Keystore.
3. Build and run the `:app` module.

---

## 📄 License
Copyright © 2025 Lumina Project. 
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
