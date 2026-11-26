# Flutter app

Direktori ini adalah aplikasi utama Tulungin untuk Android, iOS, dan Web. Project Android native sebelumnya tetap berada di `../app` sebagai fallback. Model pengguna, job, lamaran, profil, chat, dan kategori memakai koleksi Firestore yang sama.

## Persiapan

```powershell
flutter pub get
flutter run
```

### Android

`android/app/google-services.json` berasal dari konfigurasi Firebase Android yang sudah ada di project. Package ID tetap `com.pemmob.tulungin`; plugin Google Services dikonfigurasi untuk membuat resource Firebase Android saat Gradle build.

Aktifkan Email/Password dan Google pada Firebase Authentication. Untuk Google Sign-In, daftarkan SHA-1/SHA-256 signing key Android pada Firebase Console. Jangan menaruh service-account key atau rahasia server di aplikasi.

### Web

Buat Web App pada Firebase project yang sama, aktifkan domain hosting pada Firebase Authentication, lalu isi nilai dari Firebase Web App config melalui Dart defines:

```powershell
flutter run -d chrome `
  --dart-define=FIREBASE_PROJECT_ID=your-project-id `
  --dart-define=FIREBASE_MESSAGING_SENDER_ID=your-sender-id `
  --dart-define=FIREBASE_WEB_API_KEY=your-web-api-key `
  --dart-define=FIREBASE_WEB_APP_ID=your-web-app-id `
  --dart-define=FIREBASE_AUTH_DOMAIN=your-project-id.firebaseapp.com
```

Tambahkan `--dart-define=FIREBASE_STORAGE_BUCKET=...` bila project membutuhkan nilai Storage bucket.

### iOS

Buat iOS App dengan bundle ID `com.pemmob.tulungin` (deployment target iOS 15+), tambahkan `GoogleService-Info.plist` ke Runner melalui Xcode, dan gunakan nilai Firebase iOS app config dari macOS:

```bash
flutter run -d ios \
  --dart-define=FIREBASE_PROJECT_ID=your-project-id \
  --dart-define=FIREBASE_MESSAGING_SENDER_ID=your-sender-id \
  --dart-define=FIREBASE_IOS_API_KEY=your-ios-api-key \
  --dart-define=FIREBASE_IOS_APP_ID=your-ios-app-id \
  --dart-define=FIREBASE_STORAGE_BUCKET=your-project-id.firebasestorage.app
```

iOS build memerlukan macOS dan Xcode. Untuk Google Sign-In, tambahkan URL scheme `REVERSED_CLIENT_ID` dari `GoogleService-Info.plist` ke URL Types di Runner. Tambahkan `--dart-define=GOOGLE_IOS_CLIENT_ID=...` bila ID client perlu ditentukan eksplisit. Nilai Firebase client config adalah identifier aplikasi publik; jangan menaruh service-account key atau credential server di sini.

### Cloudinary

Unggahan foto profil dan bukti menggunakan unsigned Cloudinary upload preset. Isi nama cloud dan preset saat menjalankan atau build:

```powershell
flutter run `
  --dart-define=CLOUDINARY_CLOUD_NAME=your-cloud-name `
  --dart-define=CLOUDINARY_UPLOAD_PRESET=your-unsigned-preset
```

Nilai tersebut bukan API secret. Buat preset dengan pembatasan format dan ukuran di Cloudinary Console; jangan gunakan signed upload secret di aplikasi klien. Tanpa konfigurasi, unggahan berhenti dengan pesan yang menjelaskan konfigurasi yang dibutuhkan.

## Pemeriksaan

```powershell
flutter analyze
flutter test
flutter build web
```

iOS hanya dapat dibangun dari macOS. Firebase Web/iOS config perlu disediakan menggunakan Dart defines sebelum runtime Firebase untuk platform tersebut.

## Catatan perilaku

- Pembayaran adalah simulasi yang hanya menyimpan status pada Firestore; integrasi payment gateway belum tersedia.
- Jarak difilter menggunakan lokasi perangkat jika izin diberikan. Job tanpa koordinat hanya menggunakan nilai jarak yang tersedia pada dokumen.
- Lokasi peta memakai tile OpenStreetMap dan memerlukan koneksi jaringan.
- Status verifikasi tetap berasal dari dokumen pengguna di Firestore; aktifkan aturan Firebase sesuai kebutuhan sebelum deploy.
