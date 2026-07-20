# Build Instructions untuk APK, AAB, dan iOS

## Prerequisites
- Android Studio atau command line tools
- Java Development Kit (JDK) 11+
- Gradle 8.0+
- Untuk iOS: Xcode, CocoaPods, Flutter (jika menggunakan cross-platform)

## 1. Build APK (Android)

### Debug APK
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK
Pastikan Anda memiliki keystore untuk signing:
```bash
# Set environment variables
export KEYSTORE_PATH="path/to/my-upload-key.jks"
export STORE_PASSWORD="your_store_password"
export KEY_PASSWORD="your_key_password"

# Build release APK
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

## 2. Build Android App Bundle (AAB) untuk Google Play

### Release AAB
```bash
export KEYSTORE_PATH="path/to/my-upload-key.jks"
export STORE_PASSWORD="your_store_password"
export KEY_PASSWORD="your_key_password"

./gradlew bundleRelease
```
Output: `app/build/outputs/bundle/release/app-release.aab`

Upload file `.aab` ke Google Play Console untuk distribusi.

## 3. Build untuk iOS

Untuk mendukung iOS, Anda perlu salah satu dari:

### Option A: Flutter (Cross-platform - Recommended)
Jika Anda ingin menggunakan Kotlin bersama Flutter:

```bash
flutter build ios --release
```

### Option B: Kotlin Multiplatform Mobile (KMM)
Untuk berbagi kode Kotlin di antara Android dan iOS:

1. Konversi project ke KMM
2. Buat modul iOS
3. Gunakan Xcode untuk build iOS app

```bash
./gradlew assembleDebug   # Build iOS debug
./gradlew assembleRelease # Build iOS release
```

### Option C: React Native (Alternative)
Jika Anda lebih suka React Native dengan Kotlin native modules:

```bash
npm install
npm run build:ios
```

## 4. Generate Keystore untuk Production

Jika belum memiliki keystore:

```bash
keytool -genkey -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

Simpan informasi berikut dengan aman:
- Store password
- Key password
- Alias: upload

## 5. Troubleshooting

### Error: "Could not find build cache"
```bash
./gradlew clean build
```

### Error: "Keystore not found"
Pastikan path KEYSTORE_PATH benar dan environment variables sudah diset.

### Error: "Invalid APK" di Google Play
- Verify versionCode sudah di-increment
- Ensure targetSdk sesuai dengan policy Google Play terbaru
- Check min/target SDK compatibility

## 6. Continuous Integration

Untuk CI/CD pipeline, lihat file `.github/workflows/` untuk GitHub Actions setup.
