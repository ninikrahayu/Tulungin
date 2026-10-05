<div align="center">
  <img src="docs/logo.png" alt="Tulungin Logo" width="120">
  <br>
  <img src="docs/logo-text.png" alt="Tulungin Text Logo" width="170">
</div>

# Aplikasi Mobile Tulungin

> **Aplikasi marketplace jasa tolong-menolong & micro-tasking komunitas berbasis Android.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue?logo=kotlin)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose)](https://developer.android.com/compose)
[![Backend](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase)](https://firebase.google.com)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-29-brightgreen)](https://developer.android.com)

---

## 📱 Tampilan Antarmuka

| 1. Beranda | 2. Job Available | 3. Detail Job |
| :---: | :---: | :---: |
| <img src="docs/preview/01-beranda.png" width="220"> | <img src="docs/preview/02-job-available.png" width="220"> | <img src="docs/preview/04-detail-job.png" width="220"> |

| 4. Job Aktif & Bukti | 5. Konfirmasi Selesai | 6. Rating & Ulasan |
| :---: | :---: | :---: |
| <img src="docs/preview/05-job-aktif.png" width="220"> | <img src="docs/preview/06-konfirmasi.png" width="220"> | <img src="docs/preview/07-ulasan.png" width="220"> |

| 7. Bukti Pembayaran | 8. Chat Real-Time | 9. Profil Pengguna |
| :---: | :---: | :---: |
| <img src="docs/preview/08-pembayaran.png" width="220"> | <img src="docs/preview/10-chat.png" width="220"> | <img src="docs/preview/09-profil.png" width="220"> |

---

## ⚡ Fitur Utama

- **Cari & Filter Pekerjaan**: Eksplorasi permintaan bantuan dengan filter kategori, jarak, dan upah.
- **Buat Permintaan**: Buat permintaan bantuan lengkap dengan judul, deskripsi, lokasi, jadwal, dan estimasi upah.
- **Alur Status Pekerjaan**: `Tersedia` → `Diambil` → `Dikerjakan` → `Konfirmasi` → `Selesai`.
- **Upload Bukti Kerja & Pembayaran**: Penulung dapat mengunggah bukti pengerjaan dan peminta dapat mengunggah bukti pembayaran.
- **Chat Real-Time**: Komunikasi antara peminta dan penulung dalam pekerjaan aktif.
- **Peta & Lokasi**: Menampilkan lokasi pekerjaan menggunakan MapLibre GL.
- **Rating & Ulasan**: Memberikan penilaian dan ulasan setelah pekerjaan selesai.
- **Dashboard Admin**: Mengelola pengguna, kategori pekerjaan, dan job.

---

## 🛠️ Tech Stack

- **Platform & Bahasa**: Android Native, Kotlin 2.2.10
- **UI Framework**: Jetpack Compose, Material 3
- **Arsitektur**: MVVM (Model-View-ViewModel), StateFlow & Coroutines
- **Backend & Database**: Firebase Authentication & Cloud Firestore
- **Authentication**: Email/Password & Google Sign-In
- **Penyimpanan Media**: Cloudinary Android SDK
- **Peta & Lokasi**: MapLibre GL Native & Google Play Services Location

---

## 🚀 Cara Menjalankan

### 1. Clone Repository

```bash
git clone https://github.com/ninikrahayu/Tulungin.git
cd Tulungin
```

Kemudian buka folder project menggunakan Android Studio.

### 2. Prasyarat

- Android Studio versi terbaru
- Android SDK yang sesuai dengan konfigurasi project
- Emulator atau perangkat fisik dengan Android 10+ (API 29+)

### 3. Konfigurasi Firebase

Project menggunakan Firebase untuk Authentication dan Cloud Firestore.

Pastikan file:

```text
app/google-services.json
```

tersedia di dalam project.

Untuk menggunakan Google Sign-In pada perangkat pengembangan baru, tambahkan **SHA-1 fingerprint** perangkat pengembangan ke Firebase Project Settings, kemudian unduh kembali `google-services.json` apabila diperlukan.

### 4. Menjalankan Aplikasi

**Melalui Android Studio:**

1. Buka project Tulungin.
2. Tunggu proses Gradle Sync selesai.
3. Pilih emulator atau perangkat Android yang terhubung.
4. Klik **Run** atau tekan `Shift + F10`.

**Melalui terminal:**

```bash
./gradlew installDebug
```

Pada Windows PowerShell:

```powershell
.\gradlew.bat installDebug
```

Untuk membuat APK debug:

```bash
./gradlew assembleDebug
```

Hasil APK tersedia di:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📂 Struktur Project

```text
Tulungin/
├── app/
│   ├── src/main/
│   │   ├── java/com/pemmob/tulungin/
│   │   └── res/
│   ├── google-services.json
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 👥 Tim Pengembang

Tulungin dikembangkan sebagai project kelompok untuk memenuhi tugas **Mata Kuliah Pemrograman Mobile**, Program Studi Informatika, Fakultas Teknik, Universitas Jenderal Soedirman.

**Anggota Tim:**
- Muhammad Umar Faiz Alfa Rizqy (H1D024010)
- Sani Aprillia Anjani (H1D024011)
- Ninik Rahayu (H1D024024)
- Yusuf Rafii Ahmad (H1D024049)