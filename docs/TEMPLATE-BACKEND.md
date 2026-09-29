# Titik integrasi backend Tulungin

UI membaca `UserSnapshot` melalui `UserViewModel` dan memanggil `UserRepository`. Implementasi aktif adalah `LocalDemoRepository`. Seluruh operasi repository berupa fungsi `suspend` agar implementasi HTTP dapat ditambahkan tanpa mengubah setiap composable.

Ganti pembuatan repository di `data/user/UserRepositoryProvider.kt` setelah implementasi backend siap. Tidak ada URL API, API key, token, atau jaringan produksi yang dipasang dalam paket ini.

## Kontrak yang disiapkan

Tabel ini merupakan rancangan endpoint, bukan endpoint yang sudah tersedia.

| Metode repository | Rancangan endpoint | Data penting |
| --- | --- | --- |
| `snapshot` | GET `/me`, GET `/jobs`, GET `/conversations` | Petakan respons menjadi `UserSnapshot`. |
| `createJob` | POST `/jobs` | Judul, kategori, deskripsi, alamat/koordinat, waktu ISO-8601, upah. |
| `acceptJob` | POST `/jobs/{id}/accept` | Identitas Penulung dari sesi server. |
| `startJob` | POST `/jobs/{id}/start` | Perubahan ACCEPTED → IN_PROGRESS. |
| `submitProof` | POST `/jobs/{id}/proof` | Multipart berkas; URI Android harus dibaca ke stream, bukan dikirim sebagai URL publik. |
| `confirmCompletion` | POST `/jobs/{id}/complete` | Peminta memeriksa bukti lalu menyelesaikan pekerjaan. |
| `submitReview` | POST `/jobs/{id}/reviews` | Rating 1–5 dan ulasan. |
| `pay` | POST `/jobs/{id}/payments` | Buat sesi pembayaran; status lunas berasal dari konfirmasi server/provider. |
| `updateProfile` | PATCH `/me` | Nama, email, nomor HP, alamat. |
| `sendMessage` | POST `/conversations/{id}/messages` | Isi pesan; pembaruan masuk melalui polling/stream/WebSocket. |
| `sendSupport` | POST `/support/tickets` | Isi kendala, nomor tiket, status. |

Saat membuat implementasi remote, terbitkan snapshot baru melalui `StateFlow` setelah respons berhasil dan propagasikan error untuk ditampilkan ViewModel. Tambahkan penyegaran awal, pagination, timeout, retry yang aman, dan status pemuatan per operasi sesuai API tim. Tampilan demo sekarang memakai satu status `busy` untuk mencegah pengiriman berulang.

## Perubahan model yang perlu diputuskan tim backend

1. `scheduledAt` saat ini adalah teks untuk demo. Gunakan waktu terstruktur/ISO-8601 beserta zona waktu pada DTO server; format untuk tampilan pada lapisan UI.
2. `distanceKm` adalah data contoh. Gunakan koordinat lokasi job dan lokasi pengguna yang berizin untuk jarak sesungguhnya.
3. `proofUri` adalah URI berkas lokal atau penanda `demo://bukti`. Ganti dengan metadata unggahan/URL yang aksesnya sesuai izin.
4. State `paid` saat ini diubah lokal untuk simulasi. Implementasi pembayaran nyata harus mengikuti status transaksi dari server.
5. Profil demo memiliki `verified = true`. Nilai terverifikasi pada produksi harus berasal dari admin/server.
6. Login pada `MainActivity` hanya pemilihan tujuan demo. Ganti dengan hasil autentikasi dan role dari server. Password registrasi demo tidak disimpan.

## Aturan domain

Alur utama: AVAILABLE → ACCEPTED → IN_PROGRESS → AWAITING_CONFIRMATION → COMPLETED. CANCELLED tersedia sebagai contoh histori; paket ini tidak menyediakan pembatalan job baru karena desainnya belum diberikan.

Peminta tidak boleh menerima job sendiri. Penulung mengirim bukti setelah mulai bekerja. Hanya peminta yang mengonfirmasi, membayar, dan mengulas. Ulasan dan pembayaran tidak boleh ganda. Validasi ini sudah diterapkan pada demo dan wajib diulang secara otoritatif pada server.

`DemoControls` dipisahkan dari `UserRepository`: reset dan simulasi aksi lawan transaksi adalah fasilitas pengujian, bukan endpoint produksi. Saat remote diaktifkan, sembunyikan akses Mode Simulasi dan tombol simulasi terkait.

## Penyimpanan dan sesi demo

State demo disimpan pada SharedPreferences `tulungin_user_demo_v1`. ViewModel menjaga operasi saat perubahan konfigurasi, sedangkan state formulir dan navigasi memakai `rememberSaveable`. Semua email login user menuju satu profil demo lokal yang sama; paket ini belum menyediakan pemisahan data per akun.

Foto/PDF dipilih melalui Android document picker. Aplikasi menyimpan izin URI baca, tidak mengunggah atau menghapus berkas aslinya. Jika berkas dipindahkan/dihapus, pilih kembali berkasnya.
