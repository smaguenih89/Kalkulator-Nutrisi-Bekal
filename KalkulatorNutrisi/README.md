# 🎒 Kalkulator Nutrisi Bekal Sekolah - Web Edition

Aplikasi web interaktif luring (offline-ready) untuk menghitung dan mengevaluasi status keseimbangan gizi (Kalori, Protein, Karbohidrat, Lemak, Serat, dan Vitamin) dari kotak bekal makanan anak sekolah secara instan. 

Dibuat khusus menggunakan **HTML5, Tailwind CSS, dan JavaScript murni (Vanilla JS)** agar sangat ringan, responsif di HP & Komputer, serta mudah dideploy secara gratis di platform seperti **Netlify**, **GitHub Pages**, atau **Vercel**.

---

## 🚀 Cara Deploy ke Netlify

Netlify adalah cara termudah dan tercepat untuk meluncurkan aplikasi web Anda secara gratis. Berikut adalah langkah-langkah mengunggah folder ini ke Netlify secara manual tanpa perlu integrasi Git:

### Metode 1: Upload Langsung (Drag & Drop) - Paling Mudah!
1. Download seluruh isi direktori `/KalkulatorNutrisi/` ini ke dalam satu folder di komputer Anda (beri nama misalnya `KalkulatorNutrisi`).
2. Pastikan di dalam folder tersebut terdapat file-file berikut:
   - `index.html`
   - `style.css`
   - `script.js`
   - `database.js`
3. Buka browser dan pergi ke situs [Netlify Drop](https://app.netlify.com/drop).
4. **Tarik (Drag) & Lepas (Drop)** folder `KalkulatorNutrisi` tersebut ke dalam kotak unggahan yang tersedia di halaman Netlify Drop.
5. Selesai! Netlify akan langsung memproses dan memberikan Anda tautan (URL) publik (misalnya `https://nama-unik-anda.netlify.app`) yang bisa diakses dari HP siswa maupun laptop guru di sekolah.

---

## 🛠️ Menjalankan Secara Lokal di Komputer
Jika ingin mencobanya secara offline langsung di laptop/PC tanpa koneksi internet:
1. Masuk ke folder `KalkulatorNutrisi`.
2. Klik ganda (double-click) pada file **`index.html`**.
3. Aplikasi akan langsung terbuka di browser favorit Anda (Chrome, Edge, Firefox, dll) dan semua fitur berjalan 100% tanpa memerlukan instalasi apa pun!

---

## ✨ Fitur Unggulan Versi Web:
1. **Penyimpanan Lokal Offline (LocalStorage):** Riwayat perhitungan gizi tersimpan aman di browser masing-masing pengguna tanpa database server.
2. **Kategori & Filter Cerdas:** Filter lauk berdasarkan Karbohidrat, Protein, Sayuran, Buah, Minuman, atau Snack.
3. **Penghitung Porsi Fleksibel:** Pengguna dapat mengatur berat (gram) dan kelipatan porsi makan bekal dengan mudah.
4. **Detektor & Rekomendasi Ahli Gizi:** Mengevaluasi kekurangan gizi secara otomatis dan memberikan tips kesehatan edukatif harian.
5. **Cetak & Cetak Laporan PDF:** Memanfaatkan fitur cetak browser (`window.print()`) yang dikombinasikan dengan stylesheet khusus cetak yang rapi dan profesional.
6. **Dukungan Mode Gelap (Dark Mode):** Tombol estetik terintegrasi untuk beralih mode siang/malam dengan nyaman di mata.
