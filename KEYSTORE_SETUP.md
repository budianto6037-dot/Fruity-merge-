# Keystore Setup Guide

## Cara Membuat Keystore Secara Otomatis

### 1. Menggunakan Script yang Disediakan

Kami telah menyediakan script untuk membuat keystore secara otomatis. Jalankan perintah berikut:

```bash
# Buat script executable
chmod +x scripts/generate-keystore.sh

# Jalankan script
bash scripts/generate-keystore.sh
```

### 2. Script Akan Meminta Input Berikut:

- **Nama file keystore** (default: `my-upload-key.jks`)
- **Key alias** (default: `upload`)
- **Validity** (default: `10000` hari ≈ 27 tahun)
- **Keystore password** (minimal 6 karakter)
- **Key password** (atau sama dengan keystore password)
- **Informasi sertifikat**:
  - Full Name
  - Organization Unit
  - Organization
  - City/Locality
  - State/Province
  - Country Code (e.g., ID)

### 3. Output Script

Script akan menghasilkan:
- ✅ File `my-upload-key.jks` (keystore)
- ✅ File `.env` (berisi credentials)
- ✅ Verification output (untuk memastikan keystore valid)

## Contoh Penggunaan

```bash
$ bash scripts/generate-keystore.sh

========================================
Android Keystore Generator
========================================

Masukkan informasi untuk keystore:

Nama file keystore [my-upload-key.jks]: my-app-key.jks
Key alias [upload]: upload
Validity (hari) [10000]: 10000
Keystore password: ••••••••
Confirm keystore password: ••••••••
Key password (tekan Enter untuk sama dengan keystore password): 
First and Last Name: Budianto Pratama
Organization Unit (e.g., Development): Development
Organization (e.g., Your Company): My Company
City or Locality: Jakarta
State or Province: DKI Jakarta
Country Code (e.g., ID): ID

Generating keystore...

✓ Keystore berhasil dibuat!

Informasi Keystore:
===================
File: my-app-key.jks
Key Alias: upload
Algorithm: RSA
Key Size: 2048 bits
Validity: 10000 hari

Verifying keystore...
[Output dari keytool verification]

✓ Keystore verification successful!

Membuat file .env...

✓ File .env berhasil dibuat

PENTING:
1. Jangan commit keystore ke repository!
2. Jangan commit file .env ke repository!
3. Pastikan .gitignore sudah berisi:
   - *.jks
   - .env

Untuk GitHub Actions, tambahkan secrets berikut:
Settings → Secrets and variables → Actions

KEYSTORE_PATH: /path/to/my-app-key.jks
STORE_PASSWORD: (copy dari .env)
KEY_PASSWORD: (copy dari .env)

Setup keystore selesai!
```

## Setup Secrets di GitHub Secara Otomatis

Jika Anda sudah install GitHub CLI, gunakan script berikut:

```bash
# Buat script executable
chmod +x scripts/setup-secrets.sh

# Jalankan script
bash scripts/setup-secrets.sh
```

Script ini akan:
1. Login ke GitHub CLI
2. Membaca file `.env`
3. Setup semua secrets otomatis
4. Verify secrets berhasil dibuat

## Setup Secrets Secara Manual

Jika tidak ingin menggunakan script, ikuti langkah ini:

### 1. Buka Repository Settings
- Pergi ke: `https://github.com/budianto6037-dot/Fruity-merge/settings/secrets/actions`

### 2. Tambah Secrets Berikut

**Secret 1: KEYSTORE_PATH**
- Name: `KEYSTORE_PATH`
- Value: `/path/to/my-upload-key.jks` (copy dari .env)

**Secret 2: STORE_PASSWORD**
- Name: `STORE_PASSWORD`
- Value: (password keystore dari .env)

**Secret 3: KEY_PASSWORD**
- Name: `KEY_PASSWORD`
- Value: (password key dari .env)

### 3. Verify Secrets

```bash
# List secrets menggunakan GitHub CLI
gh secret list -R budianto6037-dot/Fruity-merge
```

## ⚠️ PENTING - Security Best Practices

### DO ✅
- ✅ Simpan keystore di lokasi aman
- ✅ Backup keystore Anda
- ✅ Use strong passwords (minimal 12 karakter)
- ✅ Keep keystore credentials confidential
- ✅ Rotate passwords secara berkala

### DON'T ❌
- ❌ Jangan commit keystore ke git
- ❌ Jangan commit .env ke git
- ❌ Jangan share keystore credentials
- ❌ Jangan gunakan weak passwords
- ❌ Jangan upload keystore ke repository publik

### Pastikan .gitignore sudah berisi:

```
# Security
*.jks
*.keystore
.env
.env.local
.env.*.local

# Build outputs
/build/
/app/build/
*.apk
*.aab
```

## Troubleshooting

### Error: keytool tidak ditemukan

**Solusi**: Install Java Development Kit (JDK)
```bash
# macOS
brew install openjdk

# Ubuntu/Debian
sudo apt-get install default-jdk

# Windows
Download dari: https://www.oracle.com/java/technologies/downloads/
```

### Error: Passwords tidak cocok

**Solusi**: Pastikan password yang diinput sama (case-sensitive)

### Error: File keystore sudah ada

**Solusi**: Script akan menanyakan apakah ingin mengganti. Pilih 'y' untuk mengganti atau ubah nama file.

### Lupa keystore password

**Solusi**: Tidak ada cara untuk recover. Buat keystore baru:
```bash
rm my-upload-key.jks
bash scripts/generate-keystore.sh
```

## Testing Keystore

Untuk memverifikasi keystore:

```bash
# List contents
keytool -list -v -keystore my-upload-key.jks

# Check certificate validity
keytool -list -v -keystore my-upload-key.jks | grep -i valid
```

## Next Steps

1. ✅ Generate keystore menggunakan script
2. ✅ Setup GitHub Secrets
3. ✅ Commit perubahan (jangan commit keystore!)
4. ✅ Test build dengan `./gradlew assembleRelease`
5. ✅ Buat release tag untuk trigger CI/CD

```bash
# Test build
./gradlew assembleRelease

# Create release
git tag -a v1.0.0 -m "Version 1.0.0"
git push origin --tags
```

Selesai! GitHub Actions akan otomatis build dan release app Anda! 🎉
