# Referensi desain user

Sumber: https://www.figma.com/design/RfN1ULQRALl2NoTOUtNmoG/Pemmob?node-id=145-674

Desain dibaca langsung dari Figma dan dibandingkan dengan seluruh 20 lampiran. Terdapat 19 frame user unik; beberapa gambar chat/edit profil pada lampiran merupakan duplikat. Profil dan Lokasi juga diperiksa langsung dari Figma.

| Tampilan | Node Figma | Implementasi |
| --- | --- | --- |
| Beranda | 145:674 | `HomeScreen` |
| Buat Permintaan | 155:715 | `CreateRequestScreen` |
| Job Available | 155:766 | `BrowseJobsScreen` |
| Filter | 160:1264 | Bottom sheet di `BrowseJobsScreen` |
| Detail Job | 156:785 | `JobDetailScreen` |
| Konfirmasi Ambil Job | 156:827 | Dialog di `JobDetailScreen` |
| Job Aktif | 156:860 | `ActiveJobScreen` |
| Bukti Penyelesaian | 156:906 | `ProofScreen` |
| Konfirmasi Penyelesaian | 156:939 | `ConfirmCompletionScreen` |
| Rating dan Ulasan | 156:974 | `ReviewScreen` |
| Histori | 157:917 | `HistoryScreen` |
| Detail Histori | 157:985 | `HistoryDetailScreen` |
| Chat | 157:1031 | `ChatListScreen` |
| Chat Room | 157:1067 | `ChatRoomScreen` |
| Lokasi Job | 157:1106 | `MapScreen` |
| Profil | 157:1136 | `ProfileScreen` |
| Edit Profil | 157:1202 | `EditProfileScreen` |
| Pembayaran | 157:1245 | `PaymentScreen` |
| Bantuan | 157:1285 | `SupportScreen` |

## Acuan visual

- Frame 393 × 852; margin konten 24; celah utama 14.
- Primary `#0F282F`, mint `#D7F3F3`, outline `#CAC4D0`, surface `#F9F9F9`, secondary text `#5F6368`.
- Font Inter lokal: regular, medium, semibold, bold. Logo dan ikon memakai ekspor node Figma, disimpan sebagai SVG lokal.
- Navigasi bawah: Beranda, Job Avail, tombol tambah di tengah, Histori, Profil. Pill mint menunjukkan tab aktif.
- Header halaman detail menampilkan tombol kembali. Header tab utama tanpa tombol kembali.
- Status bar, kamera, dan area gesture mengikuti perangkat Android; kamera hitam dan jam 9:30 dari mockup tidak digambar sebagai elemen aplikasi.
- Konten dapat digulir dan menyesuaikan ukuran layar serta keyboard. Tidak menggunakan tangkapan layar desain sebagai isi layar aplikasi.

## Tambahan untuk menghubungkan alur

1. Bagian Aktivitas Saya di bawah kartu Beranda membuka permintaan milik sendiri dan job yang diambil.
2. Menu Chat, Bantuan, Mode Simulasi, dan Keluar ditambahkan setelah Edit Profil.
3. Lokasi pada kartu detail dapat diketuk untuk membuka peta ilustrasi.
4. Detail Histori menyediakan ulasan/pembayaran ketika memang masih perlu dilakukan.
5. Tombol simulasi lawan transaksi tersedia pada status tertentu agar kedua sisi transaksi dapat diuji pada satu perangkat.
6. Status kosong, validasi formulir, pesan kesalahan, dan konfirmasi transaksi ditambahkan karena tidak semuanya digambar pada Figma.

Tambahan ini disengaja agar seluruh halaman dapat dicoba. Kemiripan 100% belum boleh dianggap terbukti hanya dari kode atau pembacaan Figma. Pengujian visual pada perangkat dengan ukuran acuan tetap diperlukan; hasil dan keterbatasan pemeriksaan dicatat dalam HASIL-VERIFIKASI.md.

## Sumber teknis

- AGP 9.3: https://developer.android.com/build/releases/agp-9-3-0-release-notes
- Android Studio: https://developer.android.com/studio/releases
- AndroidSVG: https://bigbadaboom.github.io/androidsvg/
- Inter: https://github.com/google/fonts/tree/main/ofl/inter — lisensi font terdapat di `app/src/main/assets/INTER-LICENSE.txt`.
