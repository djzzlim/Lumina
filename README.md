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

### 🤖 Local AI Phishing Detection
* **On-Device Inference:** Uses a local **URLBERT** model (ONNX) to classify URLs in real-time.
* **Privacy-First Safety:** Unlike traditional browsers that send your URLs to a cloud API (like Safe Browsing), Lumina performs phishing detection locally on your device.
* **Custom Tokenization:** Implements a punctuation-aware WordPiece tokenizer optimized for URL structure analysis.

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
* **AI/ML:** ONNX Runtime (Mobile)
* **Dependency Injection:** Hilt (Dagger)
* **Local Database:** Room + SQLCipher
* **Camera:** CameraX + ML Kit (For secure QR code scanning)

---

## 🏗️ Detailed Project Structure

```
app/src/main/java/com/example/lumina/
├── core/                        # Singleton Managers & Data Layer
│   ├── ml/                      # Local AI Phishing Protection
│   │   ├── PhishingDetector.kt  # ONNX Inference Engine
│   │   └── WordPieceTokenizer.kt # URL-aware tokenizer
│   ├── database/                # Encrypted Persistence (Room + SQLCipher)
│   ├── di/                      # Hilt Modules (Gecko, ML, Database)
│   └── AppPreferences.kt        # Secure DataStore for app settings
├── features/                    # UI & Feature Logic
│   ├── browser/                 # GeckoView Integration & Safety Interceptors
│   ├── settings/                # Security & Privacy configuration
│   └── ...                      # Feature modules
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
