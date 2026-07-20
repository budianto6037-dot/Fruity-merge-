# Release Checklist

Sebelum membuild APK, AAB, atau iOS app, pastikan Anda telah menyelesaikan checklist ini:

## Pre-Release

- [ ] Semua tests passing: `./gradlew test`
- [ ] Code review completed
- [ ] Version bump in `build.gradle.kts`:
  - [ ] Increment `versionCode`
  - [ ] Update `versionName`
- [ ] Update CHANGELOG.md
- [ ] All dependencies up to date: `./gradlew dependencyUpdates`
- [ ] No lint errors: `./gradlew lint`

## Security

- [ ] No hardcoded secrets atau credentials
- [ ] API keys di `.env` atau secrets manager
- [ ] SSL/TLS certificates valid
- [ ] Permissions di AndroidManifest.xml up to date
- [ ] Privacy policy updated

## Android Build

### APK Release
- [ ] Keystore tersedia dan path benar
- [ ] Build command: `./gradlew assembleRelease`
- [ ] Verify APK signature: `jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk`
- [ ] Test on real device
- [ ] Check file size < 100MB

### AAB (Android App Bundle)
- [ ] Build command: `./gradlew bundleRelease`
- [ ] Upload ke Google Play Console
- [ ] Set rollout percentage (start with 5-10%)
- [ ] Monitor for crashes
- [ ] Gradual rollout hingga 100%

## iOS Build (jika applicable)

- [ ] Xcode version updated
- [ ] Developer Certificate valid (tidak expired)
- [ ] Provisioning profile updated
- [ ] Bundle ID correct
- [ ] Build: `flutter build ipa --release`
- [ ] Test on simulator dan real device
- [ ] Check file size < 500MB

## Testing Before Release

- [ ] Functional testing on multiple devices
- [ ] Different Android versions (min SDK and up)
- [ ] Different iOS versions (jika applicable)
- [ ] Network conditions (WiFi, 4G, 3G)
- [ ] Battery consumption test
- [ ] Memory leak check
- [ ] Performance profiling

## Beta Testing

- [ ] Setup Google Play Beta testing
- [ ] Distribute ke testers
- [ ] Monitor crash reports dan reviews
- [ ] Wait minimum 3-7 days

## Release

- [ ] Tag repository: `git tag -a v1.0.0 -m "Version 1.0.0"`
- [ ] Push tags: `git push origin --tags`
- [ ] Update release notes di GitHub
- [ ] Upload artifacts
- [ ] Announcement ke users

## Post-Release

- [ ] Monitor crash reports
- [ ] Respond to user feedback
- [ ] Fix critical bugs immediately
- [ ] Plan hotfix if needed
- [ ] Analytics review

## Rollback Plan

Jika ada critical issue:
- [ ] Prepare hotfix branch
- [ ] Test thoroughly
- [ ] Build dan release hotfix (versionCode++)
- [ ] Communicate dengan users

## Version Management

Current version: Update in `app/build.gradle.kts`
- versionCode: Increment setiap release
- versionName: Follow semantic versioning (MAJOR.MINOR.PATCH)

Example:
```
versionCode = 2  // increment dari 1
versionName = "1.0.1"  // patch update
```
