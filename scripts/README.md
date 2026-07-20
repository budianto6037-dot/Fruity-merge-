#!/bin/bash

# README untuk scripts directory
# Panduan lengkap penggunaan semua scripts

cat > /dev/null << 'EOF'

# Scripts Directory - Panduan Lengkap

## 📋 Daftar Scripts

### 1. `setup-all.sh` - All-in-One Setup (Recommended)
**Purpose**: Setup lengkap dalam satu script
**Usage**:
```bash
chmod +x scripts/setup-all.sh
bash scripts/setup-all.sh
```

**Menu Options**:
1. Generate Keystore only
2. Setup GitHub Secrets only
3. Generate Keystore + Setup Secrets (Complete)
4. Verify Setup
5. Exit

**Fitur**:
- ✅ Interactive menu
- ✅ Validasi input
- ✅ Error handling
- ✅ Verification built-in
- ✅ Color-coded output

---

### 2. `generate-keystore.sh` - Generate Keystore
**Purpose**: Membuat Android keystore untuk signing APK/AAB
**Usage**:
```bash
chmod +x scripts/generate-keystore.sh
bash scripts/generate-keystore.sh
```

**Output**:
- `my-upload-key.jks` (atau nama custom)
- `.env` file dengan credentials

**Requirements**:
- Java Development Kit (JDK) installed
- keytool available

**Proses**:
1. Validasi keytool tersedia
2. Tanya input informasi keystore
3. Generate keystore dengan RSA-2048
4. Verify keystore
5. Create `.env` file
6. Tampilkan security warnings

---

### 3. `setup-secrets.sh` - Setup GitHub Secrets
**Purpose**: Otomatis setup secrets di GitHub repository
**Usage**:
```bash
chmod +x scripts/setup-secrets.sh
bash scripts/setup-secrets.sh
```

**Requirements**:
- GitHub CLI installed
- `.env` file exists
- GitHub account authenticated

**Secrets yang di-setup**:
- `KEYSTORE_PATH`
- `STORE_PASSWORD`
- `KEY_PASSWORD`

**Proses**:
1. Validasi `.env` exists
2. Validasi GitHub CLI installed
3. Login ke GitHub
4. Tanya repository info
5. Setup 3 secrets
6. Verify secrets created

---

## 🚀 Quick Start Guide

### Scenario 1: First Time Setup

```bash
# Step 1: Run all-in-one setup
bash scripts/setup-all.sh

# Choose option 3 (Generate + Setup Secrets)
# Follow interactive prompts

# Step 2: Add to .gitignore
echo "*.jks" >> .gitignore
echo ".env" >> .gitignore

# Step 3: Commit (DON'T commit keystore or .env)
git add .
git commit -m "Add CI/CD pipeline configuration"
git push origin main

# Step 4: Create release tag
git tag -a v1.0.0 -m "Version 1.0.0"
git push origin --tags

# GitHub Actions akan otomatis build!
```

---

### Scenario 2: Only Generate Keystore

```bash
bash scripts/setup-all.sh
# Choose option 1
```

---

### Scenario 3: Only Setup Secrets

```bash
bash scripts/setup-all.sh
# Choose option 2
```

---

### Scenario 4: Verify Setup

```bash
bash scripts/setup-all.sh
# Choose option 4
```

---

## 🔍 Troubleshooting

### Java not found
```bash
# Install Java
brew install openjdk           # macOS
sudo apt install default-jdk   # Ubuntu
# or download from https://www.oracle.com/java/technologies/downloads/

# Verify
java -version
```

### GitHub CLI not found
```bash
# Install GitHub CLI
brew install gh                # macOS
sudo apt install gh            # Ubuntu
# or download from https://cli.github.com

# Verify
gh --version

# Login
gh auth login
```

### Keystore password mismatch
- Script akan otomatis validasi
- Pastikan password sama (case-sensitive)

### .env file not found
- Run `setup-all.sh` option 1 terlebih dahulu
- Atau copy dari output generate-keystore.sh

### Secrets tidak terlihat di GitHub
```bash
# Verify dengan GitHub CLI
gh secret list -R username/repo
```

---

## 📁 File Structure Setelah Setup

```
project-root/
├── .github/
│   └── workflows/
│       ├── build-release.yml
│       ├── code-quality.yml
│       └── deploy-to-play-store.yml
├── scripts/
│   ├── setup-all.sh              ← Main script
│   ├── generate-keystore.sh
│   └── setup-secrets.sh
├── app/
│   └── build.gradle.kts
├── .env                          ← Created by script (⚠️ DO NOT COMMIT)
├── my-upload-key.jks             ← Created by script (⚠️ DO NOT COMMIT)
├── .gitignore                    ← Should include *.jks and .env
├── gradle.properties
└── [other project files]
```

---

## ✅ Checklist Setelah Setup

- [ ] Keystore file created
- [ ] .env file created
- [ ] GitHub secrets setup
- [ ] .gitignore updated with *.jks and .env
- [ ] Keystore NOT committed to git
- [ ] .env NOT committed to git
- [ ] Test build locally: `./gradlew assembleRelease`
- [ ] Create release tag: `git tag -a v1.0.0 -m "v1.0.0"`
- [ ] Push tag: `git push origin --tags`
- [ ] Verify GitHub Actions ran

---

## 🔐 Security Best Practices

### DO ✅
- ✅ Keep keystore in safe location
- ✅ Use strong passwords (12+ chars)
- ✅ Backup keystore regularly
- ✅ Use .env for local credentials
- ✅ Use GitHub Secrets for CI/CD
- ✅ Review .gitignore regularly
- ✅ Rotate passwords annually

### DON'T ❌
- ❌ Commit keystore to git
- ❌ Commit .env to git
- ❌ Share keystore password
- ❌ Use weak passwords
- ❌ Upload keystore to public repos
- ❌ Hardcode credentials in code
- ❌ Share credentials via email/chat

---

## 🎯 Using GitHub Actions Workflows

### 1. Build Release (Automatic on tag)

```bash
# Create and push tag
git tag -a v1.0.0 -m "Version 1.0.0"
git push origin --tags

# GitHub Actions will:
# ✅ Build Debug APK
# ✅ Build Release APK
# ✅ Build AAB
# ✅ Run tests
# ✅ Create GitHub Release
# ✅ Upload artifacts
```

### 2. Code Quality Checks (Automatic on push/PR)

Berjalan otomatis ketika:
- Push ke `main` atau `develop`
- Create Pull Request

Checks yang dijalankan:
- ✅ Lint checks
- ✅ Security scan
- ✅ Dependency updates

### 3. Deploy to Google Play (Manual trigger)

```bash
# Trigger workflow
gh workflow run deploy-to-play-store.yml \
  -f track=internal \
  -f percentage=100

# Or access via GitHub UI:
# Actions → Deploy to Google Play Store → Run workflow
```

---

## 📊 Version Management

Update `app/build.gradle.kts` sebelum release:

```kotlin
android {
  defaultConfig {
    applicationId = "com.aistudio.fruitymerge.qvzxwb"
    minSdk = 24
    targetSdk = 36
    versionCode = 2           // Increment every release
    versionName = "1.0.1"     // Semantic versioning
  }
}
```

---

## 🔗 External Resources

- [Android Keystore Docs](https://developer.android.com/studio/publish/app-signing)
- [GitHub Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [GitHub Actions](https://docs.github.com/en/actions)
- [Google Play Console](https://play.google.com/console)
- [Gradle Documentation](https://gradle.org/docs/)

---

## 💬 FAQ

**Q: Bagaimana jika lupa keystore password?**
A: Tidak ada cara recover. Buat keystore baru dengan script.

**Q: Apakah aman menyimpan keystore di local machine?**
A: Ya, selama tidak di-commit ke git dan password kuat.

**Q: Boleh share keystore dengan team?**
A: Tidak recommended. Gunakan GitHub Secrets untuk secure storage.

**Q: Bagaimana cara update keystore?**
A: Generate keystore baru dan update GitHub Secrets.

**Q: Apakah scripts work di Windows?**
A: Scripts adalah bash. Gunakan Git Bash atau WSL di Windows.

---

## 📞 Support

Jika ada masalah:
1. Check script output messages
2. Read dokumentasi:
   - `KEYSTORE_SETUP.md`
   - `build-instructions.md`
   - `RELEASE_CHECKLIST.md`
   - `QUICK_START.md`
3. Check GitHub Actions logs
4. Verify environment (Java, keytool, git, gh)

---

## 📝 Version History

- v1.0 - Initial release with all scripts and workflows

EOF

cat << 'EOF'
# Scripts README created successfully!
EOF
