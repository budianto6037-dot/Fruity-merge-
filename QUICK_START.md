# Android App release checklist dan panduan lengkap

## 1. Prerequisites ✅

```bash
# Install Java Development Kit
# macOS
brew install openjdk

# Ubuntu/Debian
sudo apt-get install default-jdk

# Windows - Download dari https://www.oracle.com/java/technologies/downloads/

# Verify Java installation
java -version
```

## 2. Generate Keystore 🔐

### Opsi A: Menggunakan Script (Recommended)

```bash
# Make script executable
chmod +x scripts/generate-keystore.sh

# Run the script
bash scripts/generate-keystore.sh
```

### Opsi B: Manual Command

```bash
keytool -genkey -v \
  -keystore my-upload-key.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias upload
```

## 3. Setup Environment Variables 🔧

File `.env` akan otomatis dibuat oleh script:

```env
KEYSTORE_PATH=/path/to/my-upload-key.jks
STORE_PASSWORD=your_password
KEY_PASSWORD=your_key_password
```

## 4. Setup GitHub Secrets 🔑

### Opsi A: Menggunakan Script

```bash
chmod +x scripts/setup-secrets.sh
bash scripts/setup-secrets.sh
```

### Opsi B: Manual Setup

1. Go to: `Settings → Secrets and variables → Actions`
2. Add 3 secrets:
   - `KEYSTORE_PATH`
   - `STORE_PASSWORD`
   - `KEY_PASSWORD`

## 5. Build APK/AAB Locally 🏗️

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
export KEYSTORE_PATH="path/to/my-upload-key.jks"
export STORE_PASSWORD="your_password"
export KEY_PASSWORD="your_key_password"

./gradlew assembleRelease

# Android App Bundle (AAB)
./gradlew bundleRelease
```

## 6. Test Build 🧪

```bash
# Run tests
./gradlew test

# Lint check
./gradlew lint

# Verify APK
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk
```

## 7. Create Release 📦

### Push tag untuk trigger CI/CD:

```bash
# Create tag
git tag -a v1.0.0 -m "Version 1.0.0"

# Push tag
git push origin --tags
```

GitHub Actions akan otomatis:
- ✅ Build APK
- ✅ Build AAB
- ✅ Run tests
- ✅ Create GitHub Release
- ✅ Upload artifacts

## 8. Upload ke Google Play Store 📱

### Setup Service Account

1. Go to: [Google Play Console](https://play.google.com/console)
2. Create service account:
   - Settings → API access
   - Create service account
   - Download JSON key

3. Add secret `PLAY_STORE_SERVICE_ACCOUNT_JSON`:
   ```bash
   gh secret set PLAY_STORE_SERVICE_ACCOUNT_JSON < service-account.json
   ```

### Deploy menggunakan Workflow

```bash
# Trigger deployment workflow
gh workflow run deploy-to-play-store.yml \
  -f track=internal \
  -f percentage=100
```

## 9. Troubleshooting 🔧

### APK tidak tersign

```bash
# Check keystore
keytool -list -v -keystore my-upload-key.jks

# Verify APK signature
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk
```

### Build error

```bash
# Clean build
./gradlew clean

# Rebuild
./gradlew assembleRelease
```

### Gradle cache issue

```bash
rm -rf ~/.gradle/caches
./gradlew build
```

## 10. Security Checklist 🛡️

- [ ] Keystore tidak di-commit ke git
- [ ] .env file tidak di-commit
- [ ] Secrets sudah di-setup di GitHub
- [ ] Passwords di-encrypt dan aman
- [ ] .gitignore berisi `*.jks` dan `.env`
- [ ] Service account key tidak di-commit
- [ ] No hardcoded credentials di code

## 11. File Locations 📁

```
project-root/
├── .github/
│   └── workflows/
│       ├── build-release.yml
│       ├── code-quality.yml
│       └── deploy-to-play-store.yml
├── scripts/
│   ├── generate-keystore.sh
│   └── setup-secrets.sh
├── app/
│   ├── build.gradle.kts
│   └── build/
│       ├── outputs/
│       │   ├── apk/
│       │   │   ├── debug/
│       │   │   │   └── app-debug.apk
│       │   │   └── release/
│       │   │       └── app-release.apk
│       │   └── bundle/
│       │       └── release/
│       │           └── app-release.aab
│       └── reports/
├── .env (⚠️ DO NOT COMMIT)
├── my-upload-key.jks (⚠️ DO NOT COMMIT)
├── .gitignore
├── gradle.properties
└── KEYSTORE_SETUP.md
```

## 12. Quick Start Summary 🚀

```bash
# 1. Generate keystore
bash scripts/generate-keystore.sh

# 2. Setup secrets (requires GitHub CLI)
bash scripts/setup-secrets.sh

# 3. Commit changes (except keystore & .env)
git add .
git commit -m "Add CI/CD configuration"
git push origin main

# 4. Test build locally
./gradlew assembleRelease

# 5. Create release tag
git tag -a v1.0.0 -m "Version 1.0.0"
git push origin --tags

# 6. Monitor GitHub Actions
# Go to: https://github.com/budianto6037-dot/Fruity-merge/actions
```

## 13. Version Management 📌

Update `app/build.gradle.kts`:

```kotlin
android {
  defaultConfig {
    applicationId = "com.aistudio.fruitymerge.qvzxwb"
    minSdk = 24
    targetSdk = 36
    versionCode = 2          // Increment setiap release
    versionName = "1.0.1"    // Semantic versioning
  }
}
```

## 14. Additional Resources 📚

- [Android Build System](https://developer.android.com/build)
- [Google Play Console](https://play.google.com/console)
- [Gradle Documentation](https://gradle.org/docs/)
- [GitHub Actions](https://docs.github.com/en/actions)

## 15. Support & Help 💬

Jika ada masalah:
1. Check `.github/workflows/` logs
2. Run `./gradlew build -i` untuk debug
3. Check dokumentasi di KEYSTORE_SETUP.md
4. Check build-instructions.md untuk detail build
