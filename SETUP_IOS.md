# Setup iOS Support

## Option 1: Flutter (Recommended untuk Kotlin to iOS)

Flutter memungkinkan Anda menulis aplikasi sekali dan deploy ke iOS dan Android.

### Installation

1. **Install Flutter SDK**
   ```bash
   # macOS
   brew install flutter
   
   # atau download dari https://flutter.dev/docs/get-started/install
   ```

2. **Initialize Flutter Project**
   ```bash
   flutter create --platforms=ios,android .
   ```

3. **Install iOS Dependencies**
   ```bash
   cd ios
   pod install
   cd ..
   ```

4. **Build iOS App**
   ```bash
   flutter build ios --release
   ```

## Option 2: Kotlin Multiplatform Mobile (KMM)

Jika ingin keep menggunakan Kotlin untuk iOS development:

### Setup KMM Plugin

1. **Add KMM Plugin ke build.gradle.kts**
   ```kotlin
   plugins {
       kotlin("multiplatform") version "1.9.0"
       id("com.android.library")
       id("native.dev.kotlinplugin") version "0.13.3"
   }
   
   kotlin {
       android()
       iosX64()
       iosArm64()
       iosSimulatorArm64()
   }
   ```

2. **Create iOS Framework**
   ```bash
   ./gradlew assembleReleaseFramework
   ```

3. **Integrate dengan Xcode**
   - Buka `ios/Fruity-merge.xcworkspace`
   - Add Kotlin framework ke Xcode project
   - Build dari Xcode

## Option 3: Xcode Direct Build (Jika sudah ada iOS project)

1. **Open Xcode Project**
   ```bash
   open ios/Runner.xcworkspace
   ```

2. **Configure Signing**
   - Select Team
   - Configure Bundle ID
   - Setup Provisioning Profiles

3. **Build**
   ```bash
   xcodebuild -workspace ios/Runner.xcworkspace \
     -scheme Runner \
     -configuration Release \
     -derivedDataPath build/ios
   ```

## Troubleshooting

### Pod install error
```bash
cd ios
rm -rf Pods Podfile.lock
pod install
```

### Xcode Build Failed
```bash
flutter clean
flutter pub get
flutter build ios --release
```

### CocoaPods version mismatch
```bash
sudo gem install cocoapods
pod repo update
```

## Generate iOS App Archive (.ipa)

```bash
# Option 1: Menggunakan Flutter
flutter build ipa --release

# Option 2: Menggunakan Xcode
xcodebuild -workspace ios/Runner.xcworkspace \
  -scheme Runner \
  -archivePath build/Runner.xcarchive \
  -configuration Release \
  archive

# Kemudian export archive
xcodebuild -exportArchive \
  -archivePath build/Runner.xcarchive \
  -exportPath build/ios/ipa \
  -exportOptionsPlist ios/ExportOptions.plist
```

## Submit ke App Store

1. Pastikan sudah punya Apple Developer Account
2. Create App ID di App Store Connect
3. Setup certificates dan provisioning profiles
4. Submit menggunakan Xcode atau Transporter app

```bash
# Menggunakan Flutter
flutter build ipa --release --export-to-file ios/ExportOptions.plist
```

## Next Steps

- Setup CodeSigning certificates
- Configure App Identifier
- Setup TestFlight untuk beta testing
- Prepare App Store listing
