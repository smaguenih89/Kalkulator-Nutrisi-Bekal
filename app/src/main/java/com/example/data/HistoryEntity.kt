package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room Database untuk menyimpan Riwayat Perhitungan Bekal Sekolah
 */
@Entity(tableName = "lunch_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,                // Nama penanda bekal, misal: "Bekal Budi - Senin"
    val timestamp: Long = System.currentTimeMillis(),
    
    // Nutrisi Total Terakumulasi
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbohydrates: Double,
    val totalFat: Double,
    val totalFiber: Double,
    
    // Ringkasan Teks
    val foodItemsSummary: String,     // Nama-nama makanan yang dimasukkan, contoh: "Nasi Putih, Ayam Goreng, Sayur Bayam"
    val vitaminsList: String,         // Daftar gabungan vitamin terdeteksi
    val score: String,                // Penilaian: "Tidak seimbang", "Bekal cukup sehat", "Bekal sangat bergizi"
    val recommendations: String       // Rekomendasi gizi yang dihitung sistem
)
