# 🎉 Fruity-merge - Complete APK, AAB, and iOS Build Setup

Dokumentasi lengkap dan script otomatis untuk build Android APK, AAB, dan iOS app dari repository Kotlin Anda.

---

## 📚 Documentation Files

### 1. **KEYSTORE_SETUP.md** 🔐
Panduan membuat keystore Android secara otomatis dengan script.
- ✅ Cara generate keystore
- ✅ Setup environment variables
- ✅ Setup GitHub Secrets
- ✅ Security best practices
- ✅ Troubleshooting

### 2. **build-instructions.md** 🏗️
Instruksi lengkap untuk build APK, AAB, dan iOS.
- ✅ Build Debug APK
- ✅ Build Release APK
- ✅ Build AAB untuk Google Play
- ✅ Build iOS app
- ✅ Generate keystore manual
- ✅ Troubleshooting

### 3. **SETUP_IOS.md** 🍎
Panduan setup iOS development dengan 3 opsi.
- ✅ Flutter (Recommended)
- ✅ Kotlin Multiplatform Mobile (KMM)
- ✅ Xcode Direct Build
- ✅ Generate iOS App Archive (.ipa)
- ✅ Submit ke App Store

### 4. **RELEASE_CHECKLIST.md** ✅
Checklist komprehensif sebelum melakukan release.
- ✅ Pre-release checks
- ✅ Security verification
- ✅ Testing procedures
- ✅ Beta testing
- ✅ Post-release monitoring

### 5. **QUICK_START.md** 🚀
Panduan quick start dengan step-by-step instructions.
- ✅ Prerequisites
- ✅ Generate keystore
- ✅ Setup environment
- ✅ Build locally
- ✅ Create release
- ✅ Version management

---

## 🛠️ Scripts (di folder `scripts/`)

### 1. **setup-all.sh** ⭐ MAIN SCRIPT
All-in-one setup script dengan interactive menu.

**Usage**:
```bash
chmod +x scripts/setup-all.sh
bash scripts/setup-all.sh
```

**Menu Options**:
1. Generate Keystore only
2. Setup GitHub Secrets only
3. Generate Keystore + Setup Secrets (Complete) ⭐
4. Verify Setup
5. Exit

### 2. **generate-keystore.sh**
Standalone script untuk generate keystore Android.

**Usage**:
```bash
bash scripts/generate-keystore.sh
```

### 3. **setup-secrets.sh**
Standalone script untuk setup GitHub Secrets.

**Usage**:
```bash
bash scripts/setup-secrets.sh
```

### 4. **scripts/README.md**
Dokumentasi lengkap untuk semua scripts dengan examples.

---

## ⚙️ GitHub Actions Workflows (di folder `.github/workflows/`)

### 1. **build-release.yml** 📦
Build APK, AAB, dan iOS otomatis ketika push tag.

**Trigger**:
```bash
git tag -a v1.0.0 -m "Version 1.0.0"
git push origin --tags
```

**Jobs**:
- ✅ Build Android (Debug APK, Release APK, AAB)
- ✅ Build iOS (Release build)
- ✅ Run Tests & Lint
- ✅ Create GitHub Release
- ✅ Upload to Google Play (Internal)
- ✅ Slack Notifications

### 2. **code-quality.yml** 🔍
Lint checks dan security scan pada setiap push/PR.

**Trigger**: Push ke `main`/`develop` atau PR

**Jobs**:
- ✅ Lint checks
- ✅ Security scan (Trivy)
- ✅ Dependency updates check

### 3. **deploy-to-play-store.yml** 📱
Manual deployment ke Google Play Store.

**Trigger**: Manual workflow dispatch

**Inputs**:
- Track: `internal`, `alpha`, `beta`, `production`
- Rollout percentage: 0-100%

---

## 🚀 Quick Start (5 Menit)

### Step 1: Generate Keystore & Setup
```bash
# Make script executable
chmod +x scripts/setup-all.sh

# Run setup
bash scripts/setup-all.sh

# Pilih option 3 (Generate + Setup Secrets)
# Follow interactive prompts
```

### Step 2: Update .gitignore
```bash
# Pastikan .gitignore memiliki:
echo "*.jks" >> .gitignore
echo ".env" >> .gitignore
git add .gitignore
```

### Step 3: Commit & Push
```bash
git add .
git commit -m "Add CI/CD pipeline configuration"
git push origin main
```

### Step 4: Create Release Tag
```bash
# Update version di app/build.gradle.kts terlebih dahulu
# Kemudian:
git tag -a v1.0.0 -m "Version 1.0.0"
git push origin --tags
```

### Step 5: Monitor GitHub Actions
```
Actions → build-release → Follow logs
```

**GitHub Actions akan otomatis**:
- ✅ Build Debug APK
- ✅ Build Release APK
- ✅ Build AAB
- ✅ Run tests
- ✅ Create GitHub Release
- ✅ Upload artifacts

---

## 📋 GitHub Secrets yang Diperlukan

Setup di: `Settings → Secrets and variables → Actions`

| Secret Name | Description | Value |
|-------------|-------------|-------|
| `KEYSTORE_PATH` | Path ke keystore file | `/path/to/my-upload-key.jks` |
| `STORE_PASSWORD` | Keystore password | (dari .env) |
| `KEY_PASSWORD` | Key alias password | (dari .env) |
| `PLAY_STORE_SERVICE_ACCOUNT_JSON` | Google Play credentials (optional) | JSON from Google Play Console |
| `SLACK_WEBHOOK` | Slack notifications (optional) | Webhook URL |

**Setup otomatis** dengan script `setup-secrets.sh`.

---

## 📦 Build Outputs

### Setelah successful build, file akan tersedia di:

```
app/build/outputs/
├── apk/
│   ├── debug/
│   │   └── app-debug.apk                 # Debug APK
│   └── release/
│       └── app-release.apk               # Release APK (signed)
└── bundle/
    └── release/
        └── app-release.aab               # Android App Bundle (Google Play)
```

### Download dari GitHub:
1. Go to: `https://github.com/budianto6037-dot/Fruity-merge/releases`
2. Download artifacts dari latest release

---

## 🔒 Security Checklist

- [ ] Keystore NOT committed to git
- [ ] .env file NOT committed to git
- [ ] *.jks in .gitignore
- [ ] .env in .gitignore
- [ ] Strong passwords (12+ characters)
- [ ] GitHub Secrets properly configured
- [ ] No hardcoded credentials in code
- [ ] Service account key NOT committed
- [ ] Regular backups of keystore

---

## 📱 Version Management

Update `app/build.gradle.kts` sebelum release:

```kotlin
android {
  defaultConfig {
    versionCode = 2              // Increment every release
    versionName = "1.0.1"        // Semantic versioning (MAJOR.MINOR.PATCH)
  }
}
```

---

## 🎯 Release Workflow

```
1. Update versionCode & versionName
   ↓
2. Commit changes: git commit -m "Bump version"
   ↓
3. Create tag: git tag -a v1.0.0 -m "Version 1.0.0"
   ↓
4. Push tag: git push origin --tags
   ↓
5. GitHub Actions builds automatically
   ↓
6. Download APK/AAB from GitHub Release
   ↓
7. Upload AAB to Google Play Console
   ↓
8. Monitor release → Staged rollout → Full rollout
```

---

## 🔧 Troubleshooting

### Common Issues

**Problem**: Build fails dengan "Keystore not found"
```bash
# Solution: Verify keystore path
ls -la my-upload-key.jks
echo $KEYSTORE_PATH
```

**Problem**: Java not found
```bash
# Solution: Install JDK
brew install openjdk          # macOS
sudo apt install default-jdk  # Ubuntu
```

**Problem**: GitHub Actions secrets not working
```bash
# Solution: Verify secrets
gh secret list -R budianto6037-dot/Fruity-merge
```

**Problem**: Gradle cache issues
```bash
# Solution: Clean and rebuild
./gradlew clean
./gradlew assembleRelease
```

Lihat **scripts/README.md** untuk troubleshooting lengkap.

---

## 📚 Additional Resources

- [Android Build System](https://developer.android.com/build)
- [Google Play Console](https://play.google.com/console)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Gradle Documentation](https://gradle.org/docs/)
- [GitHub CLI](https://cli.github.com)

---

## 📞 Support & Help

1. **Untuk Keystore Issues**: Lihat `KEYSTORE_SETUP.md`
2. **Untuk Build Issues**: Lihat `build-instructions.md`
3. **Untuk iOS Issues**: Lihat `SETUP_IOS.md`
4. **Untuk Release Workflow**: Lihat `RELEASE_CHECKLIST.md`
5. **Untuk Quick Start**: Lihat `QUICK_START.md`
6. **Untuk Scripts**: Lihat `scripts/README.md`

---

## ✨ What's Included

### Documentation (6 files)
- ✅ KEYSTORE_SETUP.md
- ✅ build-instructions.md
- ✅ SETUP_IOS.md
- ✅ RELEASE_CHECKLIST.md
- ✅ QUICK_START.md
- ✅ README.md (this file)

### Scripts (4 files)
- ✅ scripts/setup-all.sh (Main - All-in-One)
- ✅ scripts/generate-keystore.sh
- ✅ scripts/setup-secrets.sh
- ✅ scripts/README.md

### GitHub Actions Workflows (3 files)
- ✅ .github/workflows/build-release.yml
- ✅ .github/workflows/code-quality.yml
- ✅ .github/workflows/deploy-to-play-store.yml

### Configuration Files (Updated)
- ✅ gradle.properties (optimized for builds)
- ✅ .gitignore (updated for security)

---

## 🎓 Learning Path

### For Beginners
1. Start with: `QUICK_START.md`
2. Run: `bash scripts/setup-all.sh`
3. Follow: Step-by-step instructions

### For Intermediate
1. Read: `KEYSTORE_SETUP.md` (understand keystore)
2. Read: `build-instructions.md` (understand build process)
3. Customize: GitHub Actions workflows as needed

### For Advanced
1. Study: All workflow files in `.github/workflows/`
2. Modify: Scripts for custom needs
3. Integrate: With your CI/CD pipeline
4. Deploy: Custom deployment strategies

---

## 🎉 You're All Set!

Sekarang Anda siap untuk:
- ✅ Build APK untuk testing
- ✅ Build AAB untuk Google Play Store
- ✅ Build iOS app untuk App Store
- ✅ Automate releases dengan GitHub Actions
- ✅ Deploy ke multiple platforms

**Happy Building! 🚀**

---

## 📝 Version Info

- **Created**: 2026-07-20
- **Project**: Fruity-merge
- **Language**: Kotlin 100%
- **Build System**: Gradle Kotlin DSL
- **Target Platforms**: Android, iOS

---

*Dokumentasi lengkap untuk Fruity-merge CI/CD Pipeline*
