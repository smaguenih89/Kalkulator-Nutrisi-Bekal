package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.HistoryRepository
import com.example.ui.AppUI
import com.example.ui.NutrisiViewModel
import com.example.ui.NutrisiViewModelFactory
import com.example.ui.theme.MyApplicationTheme

/**
 * Aktivitas Utama (MainActivity) - Titik masuk (Entry Point) aplikasi Android.
 * Disini kita melakukan inisialisasi database penyimpanan lokal (Room) secara offline,
 * mendaftarkan ViewModel, mengatur edge-to-edge render, dan memanggil rancangan UI Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Inisialisasi Database Room & Repositori lokal (tanpa perlu server luar)
        val database = AppDatabase.getDatabase(this)
        val repository = HistoryRepository(database.historyDao())
        
        // 2. Registrasi ViewModel menggunakan Custom Factory agar repositori gizi terhubung sempurna
        val viewModel = ViewModelProvider(
            this, 
            NutrisiViewModelFactory(repository)
        )[NutrisiViewModel::class.java]
        
        // 3. Mengatur layar edge-to-edge agar area status bar & navigation gesture pill menyatu indah
        enableEdgeToEdge()
        
        setContent {
            // Mengikuti preferensi tema sistem secara default, tapi bisa langsung dion/off oleh user via tombol
            val systemInDark = isSystemInDarkTheme()
            var darkTheme by remember { mutableStateOf(systemInDark) }
            
            // Re-evaluasi jika preferensi sistem berubah di latar belakang
            LaunchedEffect(systemInDark) {
                darkTheme = systemInDark
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                // panggil gabungan layar utama kalkulator nutrisi sekolah kita
                AppUI(
                    viewModel = viewModel,
                    darkTheme = darkTheme,
                    onToggleDarkTheme = { darkTheme = !darkTheme }
                )
            }
        }
    }
}
