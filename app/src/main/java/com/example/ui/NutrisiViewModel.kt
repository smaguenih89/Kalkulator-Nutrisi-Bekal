package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Status Layar Navigasi Sederhana
 */
enum class AppScreen {
    HOME,
    INPUT_MAKANAN,
    HASIL_ANALISIS,
    RIWAYAT
}

class NutrisiViewModel(private val repository: HistoryRepository) : ViewModel() {

    // Status Layar Aktif
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Daftar Contoh Makanan yang tersedia (Dapat ditambah kustom oleh user)
    private val _availableFoods = MutableStateFlow<List<FoodInfo>>(FoodDatabase.defaultFoods)
    val availableFoods: StateFlow<List<FoodInfo>> = _availableFoods.asStateFlow()

    // Item makanan aktif dalam kotak bekal saat ini
    private val _lunchBoxItems = MutableStateFlow<List<ActiveLunchItem>>(emptyList())
    val lunchBoxItems: StateFlow<List<ActiveLunchItem>> = _lunchBoxItems.asStateFlow()

    // Penanda bekal yang sedang diinput oleh user
    private val _lunchTitle = MutableStateFlow("Bekal Sekolahku")
    val lunchTitle: StateFlow<String> = _lunchTitle.asStateFlow()

    // Filter Kategori Makanan untuk pencarian
    private val _selectedCategoryFilter = MutableStateFlow<FoodCategory?>(null)
    val selectedCategoryFilter: StateFlow<FoodCategory?> = _selectedCategoryFilter.asStateFlow()

    // Kata kunci pencarian makanan
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Ambil data riwayat kalkulasi dari Room DB reaktif (Flow -> StateFlow)
    val historyList: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setLunchTitle(title: String) {
        _lunchTitle.value = title
    }

    fun setCategoryFilter(category: FoodCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Menambah Makanan Baru secara Dinamis kemitraan siswa/orang tua ke database memori lokal
    fun addNewCustomFood(
        name: String,
        category: FoodCategory,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        fiber: Double,
        vitamin: String
    ) {
        val newFood = FoodInfo(name, category, calories, protein, carbs, fat, fiber, vitamin)
        _availableFoods.value = _availableFoods.value + newFood
    }

    // Memasukkan Item Makanan ke Kotak Bekal Saat Ini
    fun addItemToLunchBox(food: FoodInfo, weight: Double, portion: Double = 1.0) {
        val itemIndex = _lunchBoxItems.value.indexOfFirst { it.foodInfo.name == food.name }
        if (itemIndex >= 0) {
            // Jika sudah ada, update berat dan porsinya
            val existingItem = _lunchBoxItems.value[itemIndex]
            val updatedItem = existingItem.copy(
                weightGrams = weight,
                portionCount = portion
            )
            val newList = _lunchBoxItems.value.toMutableList()
            newList[itemIndex] = updatedItem
            _lunchBoxItems.value = newList
        } else {
            val newItem = ActiveLunchItem(food, weight, portion)
            _lunchBoxItems.value = _lunchBoxItems.value + newItem
        }
    }

    // Menghapus Item Makanan dari Kotak Bekal Saat Ini
    fun removeItemFromLunchBox(item: ActiveLunchItem) {
        _lunchBoxItems.value = _lunchBoxItems.value - item
    }

    // Mengosongkan Kotak Bekal Saat Ini untuk menghitung ulang dari awal
    fun clearCurrentLunchBox() {
        _lunchBoxItems.value = emptyList()
        _lunchTitle.value = "Bekal Sekolahku"
    }

    // ---------------------------------------------------------
    // PERHITUNGAN NUTRISI TOTAL (Sesuai Butir 3)
    // ---------------------------------------------------------
    
    val totalCalories: Double
        get() = _lunchBoxItems.value.sumOf { it.totalCalories }

    val totalProtein: Double
        get() = _lunchBoxItems.value.sumOf { it.totalProtein }

    val totalCarbohydrates: Double
        get() = _lunchBoxItems.value.sumOf { it.totalCarbohydrates }

    val totalFat: Double
        get() = _lunchBoxItems.value.sumOf { it.totalFat }

    val totalFiber: Double
        get() = _lunchBoxItems.value.sumOf { it.totalFiber }

    // Menggabungkan seluruh vitamin yang terdeteksi secara rapi
    val detectedVitamins: String
        get() {
            val list = _lunchBoxItems.value
                .map { it.foodInfo.vitamin }
                .flatMap { it.split(",").map { v -> v.trim() } }
                .filter { it.isNotEmpty() && !it.equals("0", ignoreCase = true) && !it.equals("-", ignoreCase = true) }
                .distinct()
            return if (list.isEmpty()) "-" else list.joinToString(", ")
        }

    // ---------------------------------------------------------
    // ANALISIS GIZI & SISTEM PENILAIAN (Sesuai Butir 4)
    // ---------------------------------------------------------

    /**
     * Mengembalikan status penilaian gizi bekal saat ini
     */
    fun calculateScore(): String {
        val items = _lunchBoxItems.value
        if (items.isEmpty()) return "Belum diisi"

        val calories = totalCalories
        val protein = totalProtein
        val carbs = totalCarbohydrates

        // Kategori makanan yang dilibatkan dalam bekal
        val categoriesPresent = items.map { it.foodInfo.category }.distinct()
        val hasProtein = categoriesPresent.contains(FoodCategory.PROTEIN)
        val hasCarb = categoriesPresent.contains(FoodCategory.KARBOHIDRAT)
        val hasVeg = categoriesPresent.contains(FoodCategory.SAYURAN)
        val hasFruit = categoriesPresent.contains(FoodCategory.BUAH)

        // Penilaian:
        // Bekal Sangat Bergizi: porsi karbo & protein ada, kalori ideal (350-700 kkal), ada sayur DAN ATAU buah, serat > 1.2g, protein sehat > 10g
        if (hasCarb && hasProtein && (hasVeg || hasFruit) && calories in 350.0..750.0 && protein >= 10.0 && totalFiber >= 1.2) {
            return "Bekal sangat bergizi"
        }
        
        // Bekal Cukup Sehat: mengandung karbohidrat dan protein, kalori 250-850 kkal, protein > 6g
        if (hasCarb && hasProtein && calories in 250.0..850.0 && protein >= 6.0) {
            return "Bekal cukup sehat"
        }

        // Diluar itu atau sangat sedikit gizi, atau didominasi camilan/snack berlebih
        return "Tidak seimbang"
    }

    /**
     * Memberikan daftar rekomendasi perbaikan gizi bekal kreatif
     */
    fun getRecommendations(): List<String> {
        val list = mutableListOf<String>()
        val items = _lunchBoxItems.value
        if (items.isEmpty()) {
            return listOf("Masukkan makanan bekalmu untuk melihat analisis gizi dan rekomendasi penyeimbang!")
        }

        val categoriesPresent = items.map { it.foodInfo.category }.distinct()
        val hasProtein = categoriesPresent.contains(FoodCategory.PROTEIN)
        val hasCarb = categoriesPresent.contains(FoodCategory.KARBOHIDRAT)
        val hasVeg = categoriesPresent.contains(FoodCategory.SAYURAN)
        val hasFruit = categoriesPresent.contains(FoodCategory.BUAH)
        val hasDrink = categoriesPresent.contains(FoodCategory.MINUMAN)

        // 1. Cek Ketiadaan Protein
        if (!hasProtein) {
            list.add("Tambahkan protein: Masukkan lauk penyumbang protein tinggi seperti telur, ayam, tempe atau tahu.")
        } else if (totalProtein < 10.0) {
            list.add("Porsi protein masih sedikit (di bawah 10g): Tambahkan berat atau porsi lauk agar kebutuhan protein anak terpenuhi.")
        }

        // 2. Cek Karbohidrat Berlebihan atau kurang
        if (!hasCarb) {
            list.add("Tambahkan karbohidrat: Masukkan nasi putih, kentang rebus atau roti tawar sebagai sumber energi utama pembelajaran.")
        } else if (totalCarbohydrates > 85.0) {
            list.add("Porsi karbohidrat terlalu banyak: Imbangi dengan memperbanyak porsi protein dan sayur, lalu kurangi bobot karbohidrat.")
        }

        // 3. Rekomendasi Sayuran
        if (!hasVeg) {
            list.add("Tambahkan sayuran: Masukkan sayuran seperti rebusan wortel, brokoli atau bayam untuk mineral, vitamin A, dan pencegah sembelit.")
        }

        // 4. Rekomendasi Buah
        if (!hasFruit) {
            list.add("Tambahkan buah: Taruhlah sepotong jeruk manis, irisan apel, atau buah pisang sebagai pelengkap rasa segar pencuci mulut.")
        }

        // 5. Kurangi gula/snack jika snack mendominasi kalori
        val snackCalories = items.filter { it.foodInfo.category == FoodCategory.SNACK }.sumOf { it.totalCalories }
        if (snackCalories > 0 && snackCalories > (totalCalories * 0.35)) {
            list.add("Kurangi makanan tinggi gula: Kandungan kalori camilan/snack terlalu mendominasi kotak bekal. Kurangi porsi snack manis.")
        }

        // 6. Air Putih / Susu pendukung hidrasi
        if (!hasDrink) {
            list.add("Sediakan Minuman: Ingatkan anak membawa air putih murni yang banyak, atau selipkan kotak susu UHT kecil.")
        }

        if (list.isEmpty()) {
            list.add("Sempurna! Komposisi bekal ini sangat meriah dan bergizi seimbang. Pertahankan kreasi nutrisi sehat ini!")
        }

        return list
    }

    // ---------------------------------------------------------
    // DATABASE HISTORY OPERATIONS (LocalStorage Room DB)
    // ---------------------------------------------------------

    fun saveCalculationToHistory(context: Context) {
        if (_lunchBoxItems.value.isEmpty()) {
            Toast.makeText(context, "Kotak bekal masih kosong! Tidak dapat disimpan.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val listFoods = _lunchBoxItems.value.joinToString { "${it.foodInfo.name} (${it.weightGrams.toInt()}g)" }
            val currentScore = calculateScore()
            val currentRecs = getRecommendations().joinToString("\n")

            val historyEntry = HistoryEntity(
                title = _lunchTitle.value,
                totalCalories = totalCalories,
                totalProtein = totalProtein,
                totalCarbohydrates = totalCarbohydrates,
                totalFat = totalFat,
                totalFiber = totalFiber,
                foodItemsSummary = listFoods,
                vitaminsList = detectedVitamins,
                score = currentScore,
                recommendations = currentRecs
            )

            repository.insert(historyEntry)
            Toast.makeText(context, "Kalkulasi disimpan ke riwayat bekal!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteHistoryEntry(id: Int, context: Context) {
        viewModelScope.launch {
            repository.deleteById(id)
            Toast.makeText(context, "Catatan riwayat berhasil dihapus", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearAllHistoryEntries(context: Context) {
        viewModelScope.launch {
            repository.clearAll()
            Toast.makeText(context, "Semua catatan riwayat dibersihkan!", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------------------------------------------------
    // EXPORT PDF OFFLINE (Sesuai Butir 6)
    // ---------------------------------------------------------

    fun exportToPdfAndShare(context: Context) {
        if (_lunchBoxItems.value.isEmpty()) {
            Toast.makeText(context, "Kotak bekal kosong, masukkan makanan terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Membuat Dokumen PDF Baru
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Ukuran A4 standar (point)
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val textPaint = Paint().apply {
                color = AndroidColor.BLACK
                textSize = 12f
                isAntiAlias = true
            }

            var yPos = 50f

            // 1. Gambar Header Judul
            paint.color = AndroidColor.parseColor("#4CAF50") // Hijau Edukatif
            canvas.drawRect(20f, yPos, 575f, yPos + 60f, paint)

            paint.color = AndroidColor.WHITE
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("LAPORAN NUTRISI BEKAL SEKOLAH", 40f, yPos + 38f, paint)

            yPos += 80f

            // 2. Info Detail Bekal
            textPaint.apply {
                isFakeBoldText = true
                textSize = 14f
            }
            canvas.drawText("Subjek: ${_lunchTitle.value}", 40f, yPos, textPaint)
            
            val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            val formattedDate = dateFormat.format(Date())
            
            textPaint.isFakeBoldText = false
            textPaint.textSize = 11f
            textPaint.color = AndroidColor.GRAY
            canvas.drawText("Dicatat pada: $formattedDate", 40f, yPos + 18f, textPaint)

            yPos += 45f

            // Horizontal Line
            paint.color = AndroidColor.LTGRAY
            canvas.drawLine(40f, yPos, 555f, yPos, paint)

            yPos += 25f

            // 3. Skor Penilaian
            textPaint.apply {
                color = AndroidColor.BLACK
                isFakeBoldText = true
                textSize = 13f
            }
            canvas.drawText("Kualifikasi Gizi Bekal:", 40f, yPos, textPaint)

            val appScore = calculateScore()
            val scoreColor = when (appScore) {
                "Bekal sangat bergizi" -> "#2E7D32"  // Hijau
                "Bekal cukup sehat" -> "#F9A825"    // Kuning Tua/Orange
                else -> "#C62828"                      // Merah
            }
            paint.color = AndroidColor.parseColor(scoreColor)
            textPaint.color = AndroidColor.parseColor(scoreColor)
            textPaint.textSize = 16f
            canvas.drawText("★ $appScore ★", 210f, yPos + 1f, textPaint)

            yPos += 35f

            // 4. Tabel Kandungan Gizi
            textPaint.apply {
                color = AndroidColor.BLACK
                textSize = 13f
                isFakeBoldText = true
            }
            canvas.drawText("Ringkasan Total Nutrisi:", 40f, yPos, textPaint)
            yPos += 20f

            // Draw a light grey background for stats box
            paint.color = AndroidColor.parseColor("#F5F5F5")
            canvas.drawRect(40f, yPos, 555f, yPos + 90f, paint)

            textPaint.apply {
                isFakeBoldText = false
                textSize = 11f
                color = AndroidColor.DKGRAY
            }

            val leftCol = 70f
            val rightCol = 320f
            
            canvas.drawText("• Energi / Kalori  : ${String.format("%.1f", totalCalories)} kkal (Porsi Gizi)", leftCol, yPos + 22f, textPaint)
            canvas.drawText("• Karbohidrat      : ${String.format("%.1f", totalCarbohydrates)} g", leftCol, yPos + 42f, textPaint)
            canvas.drawText("• Protein          : ${String.format("%.1f", totalProtein)} g", leftCol, yPos + 62f, textPaint)

            canvas.drawText("• Lemak Total  : ${String.format("%.1f", totalFat)} g", rightCol, yPos + 22f, textPaint)
            canvas.drawText("• Serat Pangan  : ${String.format("%.1f", totalFiber)} g", rightCol, yPos + 42f, textPaint)
            canvas.drawText("• Vitamin       : $detectedVitamins", rightCol, yPos + 62f, textPaint)

            yPos += 115f

            // 5. Daftar Bahan Makanan
            textPaint.apply {
                color = AndroidColor.BLACK
                textSize = 13f
                isFakeBoldText = true
            }
            canvas.drawText("Daftar Bekal terinput:", 40f, yPos, textPaint)
            yPos += 18f

            textPaint.isFakeBoldText = false
            textPaint.textSize = 11f
            _lunchBoxItems.value.forEachIndexed { idx, item ->
                val lineText = "${idx + 1}. ${item.foodInfo.name} - ${item.weightGrams.toInt()}g (${String.format("%.1f", item.totalCalories)} kkal, Protein: ${String.format("%.1f", item.totalProtein)}g)"
                canvas.drawText(lineText, 50f, yPos, textPaint)
                yPos += 18f
            }

            yPos += 15f

            // 6. Rekomendasi
            textPaint.apply {
                color = AndroidColor.BLACK
                textSize = 13f
                isFakeBoldText = true
            }
            canvas.drawText("Saran Perbaikan Gizi Edukatif:", 40f, yPos, textPaint)
            yPos += 18f

            textPaint.apply {
                isFakeBoldText = false
                textSize = 10f
                color = AndroidColor.BLACK
            }
            getRecommendations().forEach { rec ->
                // Basic text wrapping for recommendations to prevent horizontal overflow in PDF
                val words = rec.split(" ")
                val lines = mutableListOf<String>()
                var currentLine = "• "
                words.forEach { word ->
                    if ((currentLine + word).length > 85) {
                        lines.add(currentLine)
                        currentLine = "  " + word
                    } else {
                        currentLine += if (currentLine == "• " || currentLine == "  ") word else " $word"
                    }
                }
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }

                lines.forEach { line ->
                    if (yPos > 810) { // Safety check to prevent vertical overflow of PDF paper
                        // Could create a new page, but for simple app we limit text drawing
                        return@forEach
                    }
                    canvas.drawText(line, 55f, yPos, textPaint)
                    yPos += 16f
                }
                yPos += 5f
            }

            // Footer
            paint.color = AndroidColor.LTGRAY
            canvas.drawLine(40f, 790f, 555f, 790f, paint)
            textPaint.apply {
                color = AndroidColor.GRAY
                textSize = 9f
            }
            canvas.drawText("Generasi Gizi Indonesia Sehat - Kalkulator Bekal Sekolah", 180f, 808f, textPaint)

            pdfDocument.finishPage(page)

            // Menulis PDF ke file cache agar bisa dibagikan
            val cachePath = File(context.cacheDir, "pdf_laporan")
            cachePath.mkdirs()
            val pdfFile = File(cachePath, "Nutrisi_${_lunchTitle.value.replace(" ", "_")}.pdf")
            
            val fileOutputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()
            fileOutputStream.flush()
            fileOutputStream.close()

            // 7. Bagikan file PDF menggunakan Intent Share
            val authority = "${context.packageName}.fileprovider"
            val fileUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Gizi Bekal: ${_lunchTitle.value}")
                putExtra(Intent.EXTRA_TEXT, "Halo! Berikut hasil analisis nutrisi bekal sekolah dari aplikasi Kalkulator Gizi. Bekal saat ini berkategori: $appScore")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "Bagikan PDF Laporan Nutrisi"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

/**
 * Factory untuk instisiasi ViewModel dengan Parameter Repositori data Room
 */
class NutrisiViewModelFactory(private val repository: HistoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NutrisiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NutrisiViewModel(repository) as T
        }
        throw IllegalArgumentException("Kelas ViewModel tidak dikenal")
    }
}
