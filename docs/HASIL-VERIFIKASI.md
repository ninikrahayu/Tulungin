# Hasil verifikasi — 29 September 2026

## Hasil akhir

Perintah `:app:assembleDebug :app:testDebugUnitTest` selesai dengan **BUILD SUCCESSFUL**.

- APK debug berhasil dibuat dari kode yang ada dalam paket.
- 15 tes lulus, 0 gagal: 10 tes aturan transaksi, 4 tes alur/repository user, dan 1 tes bawaan proyek.
- Alur UI yang dijalankan: buka daftar dan filter, ambil job, mulai pekerjaan, konfirmasi penyelesaian, ulasan, pembayaran simulasi, buka profil/chat, dan kirim pesan.
- Repository diuji dengan membuat permintaan dan tiket bantuan, kemudian dibuat ulang untuk memastikan data tetap tersimpan.
- Aturan transaksi menguji pembatasan aktor/status, bukti, ulasan, pembayaran, serta penolakan transaksi berulang.
- `git diff --check` tidak menemukan kesalahan whitespace.

## Lingkungan pemeriksaan

Gradle 9.5.0, AGP 9.3.3, JDK Temurin 17.0.20.1, SDK compile 37.0, dan Build Tools 36.0.0. Pengujian UI memakai Robolectric 4.16.1 pada API 35 dengan ukuran acuan 393 × 852 dp, density 2. Lokasi SDK dan konfigurasi proxy khusus lingkungan pemeriksaan tidak dimasukkan ke proyek.

## Pemeriksaan visual

Sembilan hasil render tersedia di `docs/preview/`. Beranda, daftar job, dan detail job diperiksa bersama referensi desain; ringkasan seluruh hasil render juga diperiksa. Posisi harga, jarak kartu Beranda, dan jarak ikon navigasi dirapikan setelah pemeriksaan. Aset ikon/logo asli Figma serta font Inter lokal dipakai.

Screenshot berasal dari View Android pada lingkungan uji, bukan emulator fisik. Status/navigation bar sistem tidak muncul seperti pada HP. Dialog dan bottom sheet memakai window terpisah sehingga hasil capture window utama tidak dijadikan bukti visual untuk keduanya. Alur filter tetap lulus tes interaksi. Kemiripan 100% pada semua perangkat belum diklaim.

## Batas pemeriksaan

- Belum dilakukan pengujian pada HP/emulator sebenarnya, rotasi, semua ukuran layar/font, serta TalkBack.
- Pemilih file sistem, izin URI setelah restart perangkat, preview berkas dari berbagai penyedia, date/time picker, dan keyboard perlu dicoba pada HP.
- Login/register/admin tetap memakai layar tim; routing demo di MainActivity sudah dikompilasi, tetapi alur UI otomatis di atas dimulai langsung dari halaman user.
- Peta adalah ilustrasi. Pembayaran, chat, bantuan, dan lawan transaksi adalah simulasi lokal. Tidak ada backend atau integrasi layanan sungguhan.
- Tanggal pada data contoh mengikuti desain; permintaan baru wajib memilih waktu mendatang.

Peringatan build tentang strip simbol `libandroidx.graphics.path.so` tidak menghentikan pembuatan APK. Pengambilan screenshot awal melalui PixelCopy mengalami timeout pada Robolectric; helper tes diganti dengan penggambaran View, lalu seluruh tes dijalankan ulang dan lulus.

## Paket percobaan

`demo-apk/Tulungin-debug.apk` adalah APK debug untuk mencoba frontend, bukan build rilis produksi. Kode sumber, aset, pengujian, dan dokumentasi tersedia lengkap. Untuk mengedit, buka folder proyek sesuai README-MULAI-DI-SINI.md.

Penyebab pasti indikator merah pada komputer Yusuf belum diketahui tanpa pesan pertama Build/Sync Output, versi Android Studio, dan Gradle JDK yang digunakan. Pelepasan pemaksaan JDK 25 pada paket ini memperbaiki salah satu kendala konfigurasi, bukan bukti bahwa semua error lokal berasal dari itu.
