package com.example.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUI(viewModel: NutrisiViewModel, darkTheme: Boolean, onToggleDarkTheme: () -> Unit) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val lunchBoxItems by viewModel.lunchBoxItems.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val lunchTitle by viewModel.lunchTitle.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bekal Sehat 🎒",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Tombol Mode Gelap / Terang Edukatif
                    IconButton(
                        onClick = onToggleDarkTheme,
                        modifier = Modifier.testTag("dark_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Mode Gelap/Terang",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (currentScreen != AppScreen.HOME) {
                        IconButton(
                            onClick = { viewModel.setScreen(AppScreen.HOME) },
                            modifier = Modifier.testTag("nav_home_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Kembali ke Beranda",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Animasi Transisi antar Layar yang Halus
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        historyList = historyList,
                        onStartCalculate = { viewModel.setScreen(AppScreen.INPUT_MAKANAN) }
                    )
                    AppScreen.INPUT_MAKANAN -> InputMakananScreen(
                        viewModel = viewModel,
                        lunchBoxItems = lunchBoxItems,
                        lunchTitle = lunchTitle,
                        onNavigateToResults = { viewModel.setScreen(AppScreen.HASIL_ANALISIS) },
                        onBack = { viewModel.setScreen(AppScreen.HOME) }
                    )
                    AppScreen.HASIL_ANALISIS -> HasilAnalisisScreen(
                        viewModel = viewModel,
                        lunchBoxItems = lunchBoxItems,
                        lunchTitle = lunchTitle,
                        onBackToInput = { viewModel.setScreen(AppScreen.INPUT_MAKANAN) },
                        onDone = {
                            viewModel.clearCurrentLunchBox()
                            viewModel.setScreen(AppScreen.HOME)
                        }
                    )
                    AppScreen.RIWAYAT -> RiwayatScreen(
                        viewModel = viewModel,
                        historyList = historyList,
                        onBack = { viewModel.setScreen(AppScreen.HOME) }
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// 1. LAYANAN HALAMAN BERANDA (HomeScreen)
// --------------------------------------------------------------------------
@Composable
fun HomeScreen(
    viewModel: NutrisiViewModel,
    historyList: List<HistoryEntity>,
    onStartCalculate: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcoming Hero Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tampilkan Gambar Edukatif Lunchbox yang digenerate AI
                    Image(
                        painter = painterResource(id = R.drawable.img_lunchbox_hero),
                        contentDescription = "Mascot lunch box anak sekolah sehat",
                        modifier = Modifier
                            .size(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Kalkulator Nutrisi Bekal Sekolah",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Hitung kandungan gizi bekal sekolahmu dengan mudah secara instan offline agar tumbuh seimbang & berprestasi!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onStartCalculate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("mulai_hitung_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mulai Hitung Bekal Gizi 🎒", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Edu-Tip Promtion Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info Gizi",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Tips Gizi: Bekal sehat harus seimbang, memiliki Karbohidrat (energi), Protein (otot), serta Sayur & Buah (serat & vitamin).",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Section Riwayat Perhitungan (Sesuai Butir 6)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riwayat Catatan Bekal 📝",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (historyList.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearAllHistoryEntries(context) },
                        colors = ButtonDefaults.textButtonColors(contentColor = GiziKurangMerah),
                        modifier = Modifier.testTag("hapus_semua_riwayat_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus Semua", fontSize = 11.sp)
                    }
                }
            }
        }

        if (historyList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada riwayat perhitungan",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Kalkulasi bekalmu dan klik Simpan Riwayat untuk melihat log harian disini.",
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        } else {
            items(historyList) { history ->
                HistoryRowItem(history = history, onDelete = {
                    viewModel.deleteHistoryEntry(history.id, context)
                })
            }
        }
    }
}

@Composable
fun HistoryRowItem(history: HistoryEntity, onDelete: () -> Unit) {
    val dateText = remember(history.timestamp) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(history.timestamp))
    }

    val borderAndBgColor = when (history.score) {
        "Bekal sangat bergizi" -> Pair(GiziBaikHijau, CardProtein)
        "Bekal cukup sehat" -> Pair(GiziPerhatianKuning, CardCalories)
        else -> Pair(GiziKurangMerah, CardCarbo)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderAndBgColor.first.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = borderAndBgColor.second)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = history.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = borderAndBgColor.first),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = history.score,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Bahan: ${history.foodItemsSummary}",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥 ${history.totalCalories.toInt()} kkal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = "🥩 Protein: ${String.format("%.1f", history.totalProtein)}g",
                        fontSize = 10.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "🥦 Vitamin: ${history.vitaminsList}",
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = dateText,
                    fontSize = 9.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .testTag("delete_item_history_btn_${history.id}")
                    .padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus Riwayat",
                    tint = Color.Red.copy(alpha = 0.8f)
                )
            }
        }
    }
}


// --------------------------------------------------------------------------
// 2. INPUT DATA MAKANAN (InputMakananScreen)
// --------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputMakananScreen(
    viewModel: NutrisiViewModel,
    lunchBoxItems: List<ActiveLunchItem>,
    lunchTitle: String,
    onNavigateToResults: () -> Unit,
    onBack: () -> Unit
) {
    val searchVal by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCat by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val availableFoods by viewModel.availableFoods.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFoodForDialog by remember { mutableStateOf<FoodInfo?>(null) }
    var showCustomFoodDialog by remember { mutableStateOf(false) }

    // Memfilter data makanan berdasarkan pencarian teks dan filter kategori
    val filteredFoods = remember(availableFoods, searchVal, selectedCat) {
        availableFoods.filter { food ->
            val matchQuery = food.name.contains(searchVal, ignoreCase = true)
            val matchCat = selectedCat == null || food.category == selectedCat
            matchQuery && matchCat
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Form Title Bekal (Misal: "Bekal Senin Budi")
        OutlinedTextField(
            value = lunchTitle,
            onValueChange = { viewModel.setLunchTitle(it) },
            label = { Text("Nama Penanda Kotak Bekal") },
            leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lunch_title_field")
                .background(MaterialTheme.colorScheme.surface)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Searh & filter section
        OutlinedTextField(
            value = searchVal,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Cari nama makanan...", fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(MaterialTheme.colorScheme.surface)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Category Selectors (Sesuai Butir 2)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                InputChip(
                    selected = (selectedCat == null),
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("Semua", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            items(FoodCategory.values()) { cat ->
                InputChip(
                    selected = (selectedCat == cat),
                    onClick = { viewModel.setCategoryFilter(cat) },
                    label = { Text("${cat.icon} ${cat.displayName}", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tombol Tambah Makanan Kustom (Alternatif Edukatif Siswa PKL)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pilih Makanan untuk Bekal:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            TextButton(
                onClick = { showCustomFoodDialog = true },
                modifier = Modifier.testTag("add_custom_food_trigger")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah Makanan Kustom", fontSize = 11.sp)
            }
        }

        // List of Available Foods (Pilih item makanan)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredFoods.isEmpty()) {
                item {
                    Text(
                        text = "Makanan tidak ditemukan. Klik 'Tambah Makanan Kustom' diatas untuk menambahkan makanan baru!",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            } else {
                items(filteredFoods) { food ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFoodForDialog = food
                                showAddDialog = true
                            }
                            .testTag("food_row_item_${food.name}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(food.category.icon, fontSize = 18.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = food.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "${food.category.displayName} • ${food.calories.toInt()} kkal/100g",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "🥩P: ${food.protein}g | 🍚K: ${food.carbohydrates}g | 🥑L: ${food.fat}g | 🥦Vit: ${food.vitamin}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Ke Bekal",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Dashboard "Makanan yang sudah masuk kotak bekal"
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Isi Kotak Bekal Saat Ini (${lunchBoxItems.size} Item):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                if (lunchBoxItems.isEmpty()) {
                    Text(
                        text = "Belum ada lauk yang dimasukkan. Klik list makanan di atas!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    // Quick view of items in dynamic chips list
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(lunchBoxItems) { item ->
                            AssistChip(
                                onClick = { viewModel.removeItemFromLunchBox(item) },
                                label = { Text("${item.foodInfo.name} (${item.weightGrams.toInt()}g)", fontSize = 10.sp) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus",
                                        modifier = Modifier.size(12.dp)
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Estimasi Kalori Saat Ini:",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${viewModel.totalCalories.toInt()} kkal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.clearCurrentLunchBox() },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Kotak")
                        }

                        Button(
                            onClick = onNavigateToResults,
                            enabled = lunchBoxItems.isNotEmpty(),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("hitung_total_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                contentColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text("Buka Laporan 📊", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // DIALOG POPUP: INPUT BERAT & PORSI MAKANAN (Sesuai Butir 2 & 3)
    // -------------------------------------------------------------
    if (showAddDialog && selectedFoodForDialog != null) {
        val food = selectedFoodForDialog!!
        var weightStr by remember { mutableStateOf("100") }
        var portionStr by remember { mutableStateOf("1.0") }
        
        // Cek porsi jika ada sebelumnya di box untuk preset isian awal
        val existing = lunchBoxItems.find { it.foodInfo.name == food.name }
        LaunchedEffect(selectedFoodForDialog) {
            if (existing != null) {
                weightStr = existing.weightGrams.toInt().toString()
                portionStr = existing.portionCount.toString()
            } else {
                weightStr = "100"
                portionStr = "1.0"
            }
        }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tambah ${food.name}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Kategori: ${food.category.displayName}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // TextField Berat dalam Gram
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it.filter { char -> char.isDigit() } },
                        label = { Text("Berat Makanan (gram)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("food_weight_field")
                    )

                    // Quick Weight Presets
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("50", "100", "150", "200").forEach { weightPreset ->
                            Button(
                                onClick = { weightStr = weightPreset },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text("${weightPreset}g", fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // TextField Jumlah Porsi
                    OutlinedTextField(
                        value = portionStr,
                        onValueChange = { portionStr = it },
                        label = { Text("Jumlah Porsi (contoh: 1, 1.5, 2)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("food_portion_field")
                    )

                    // Quick Portion Presets
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("1.0", "1.5", "2.0").forEach { pPreset ->
                            Button(
                                onClick = { portionStr = pPreset },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text("${pPreset} Porsi", fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hasil hitung nutrisi di popup sebagai pratinjau kalkulator otomatis (Butir 3)
                    val weightDouble = weightStr.toDoubleOrNull() ?: 100.0
                    val portionDouble = portionStr.toDoubleOrNull() ?: 1.0
                    val expectedCalories = (food.calories * weightDouble * portionDouble) / 100.0

                    Text(
                        text = "Kontribusi: ${String.format("%.1f", expectedCalories)} kkal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Batal")
                        }

                        Button(
                            onClick = {
                                val finalWeight = weightStr.toDoubleOrNull() ?: 100.0
                                val finalPortion = portionStr.toDoubleOrNull() ?: 1.0
                                viewModel.addItemToLunchBox(food, finalWeight, finalPortion)
                                showAddDialog = false
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("simpan_lauk_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Masukkan Kotak")
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // DIALOG POPUP: TAMBAH MAKANAN KUSTOM BARU (Edukatif Siswa PKL)
    // -------------------------------------------------------------
    if (showCustomFoodDialog) {
        var newName by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf(FoodCategory.PROTEIN) }
        var newCalories by remember { mutableStateOf("") }
        var newProtein by remember { mutableStateOf("") }
        var newCarbohydrate by remember { mutableStateOf("") }
        var newFat by remember { mutableStateOf("") }
        var newFiber by remember { mutableStateOf("") }
        var newVitamin by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showCustomFoodDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .heightIn(max = 550.dp)
                ) {
                    Text(
                        text = "Tambah Item Gizi Kustom 🍎",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Scrollable Fields
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Nama Makanan (misal: Sosis Bakar)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Text("Pilih Kategori Makanan:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(FoodCategory.values()) { cat ->
                                    InputChip(
                                        selected = (newCategory == cat),
                                        onClick = { newCategory = cat },
                                        label = { Text("${cat.icon} ${cat.displayName}", fontSize = 10.sp) }
                                    )
                                }
                            }
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newCalories,
                                    onValueChange = { newCalories = it },
                                    label = { Text("Kalori (kkal/100g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = newCarbohydrate,
                                    onValueChange = { newCarbohydrate = it },
                                    label = { Text("Karbohidat (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newProtein,
                                    onValueChange = { newProtein = it },
                                    label = { Text("Protein (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = newFat,
                                    onValueChange = { newFat = it },
                                    label = { Text("Lemak (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = newFiber,
                                onValueChange = { newFiber = it },
                                label = { Text("Serat (g/100g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = newVitamin,
                                onValueChange = { newVitamin = it },
                                label = { Text("Vitamin Terkandung (contoh: A, C, Kapsul)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCustomFoodDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    viewModel.addNewCustomFood(
                                        name = newName,
                                        category = newCategory,
                                        calories = newCalories.toDoubleOrNull() ?: 0.0,
                                        protein = newProtein.toDoubleOrNull() ?: 0.0,
                                        carbs = newCarbohydrate.toDoubleOrNull() ?: 0.0,
                                        fat = newFat.toDoubleOrNull() ?: 0.0,
                                        fiber = newFiber.toDoubleOrNull() ?: 0.0,
                                        vitamin = if (newVitamin.isBlank()) "-" else newVitamin
                                    )
                                    showCustomFoodDialog = false
                                }
                            },
                            modifier = Modifier.weight(1.2f).testTag("simpan_custom_food_btn")
                        ) {
                            Text("Simpan Gizi", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}


// --------------------------------------------------------------------------
// 3. TAMPILAN DASHBOARD HASIL (HasilAnalisisScreen)
// --------------------------------------------------------------------------
@Composable
fun HasilAnalisisScreen(
    viewModel: NutrisiViewModel,
    lunchBoxItems: List<ActiveLunchItem>,
    lunchTitle: String,
    onBackToInput: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    
    // Hasil-hasil nutrisi utama terhitung
    val cal = viewModel.totalCalories
    val prot = viewModel.totalProtein
    val carb = viewModel.totalCarbohydrates
    val fat = viewModel.totalFat
    val fiber = viewModel.totalFiber
    val vitamins = viewModel.detectedVitamins

    // Status Penilaian (Sesuai Butir 4 & 5)
    val score = viewModel.calculateScore()
    val scoreColorAndDetails = when (score) {
        "Bekal sangat bergizi" -> Triple(GiziBaikHijau, "Luar Biasa!", "Bekal bepergian ini komplit dan memenuhi syarat anak cerdas.")
        "Bekal cukup sehat" -> Triple(GiziPerhatianKuning, "Cukup Sehat!", "Bekal sudah bagus memiliki unsur utama, tingkatkan ragam sayurnya!")
        else -> Triple(GiziKurangMerah, "Tidak Seimbang", "Gizi tidak/belum seimbang. Silakan tambahkan sayur/lauk kaya gizi!")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER NAVIGASI
        item {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onBackToInput() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Kembali ke form")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Isian Kotak Bekal", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }

        // AKREDITASI PENILAIAN CARD (Sesuai Butir 4 & 5)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = scoreColorAndDetails.first)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = lunchTitle,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = score,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "★ ${scoreColorAndDetails.second} ★",
                        color = Color.Yellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = scoreColorAndDetails.third,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        // KALORI HERO CARD (Dashboard Butir 5)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardCalories),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total Energi Kalori Bekal:", fontSize = 12.sp, color = Color.Gray)
                        Text("${String.format("%.1f", cal)} kkal", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Rekomendasi target makan siang: 400 - 650 kkal", fontSize = 10.sp, color = Color.DarkGray)
                    }
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.Yellow.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 28.sp)
                    }
                }
            }
        }

        // PROGRESS BAR KEBUTUHAN NUTRISI (Sesuai Butir 5)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rasio Penyebaran & Kecukupan Nutrisi:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Target representatif makan siang sehat anak:
                    // Karbo: 70g, Protein: 15g, Lemak: 15g, Serat: 3g
                    NutrientProgressBar(title = "Karbohidrat 🍚", amount = carb, target = 70.0, unit = "g", barColor = GiziPerhatianKuning)
                    Spacer(modifier = Modifier.height(10.dp))
                    NutrientProgressBar(title = "Protein 🍗", amount = prot, target = 15.0, unit = "g", barColor = GiziBaikHijau)
                    Spacer(modifier = Modifier.height(10.dp))
                    NutrientProgressBar(title = "Lemak 🥓", amount = fat, target = 15.0, unit = "g", barColor = GiziKurangMerah)
                    Spacer(modifier = Modifier.height(10.dp))
                    NutrientProgressBar(title = "Serat 🥦", amount = fiber, target = 3.0, unit = "g", barColor = SchoolTealTertiary)
                }
            }
        }

        // VITAMIN MINI PANEL (Tampilan Menyenangkan untuk Anak)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Daftar Vitamin/Mineral Terbawa 🌟",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                if (vitamins == "-" || vitamins.isBlank()) {
                    Text("Belum terdeteksi kandungan vitamin spesifik. Masukkan buah/sayur!", fontSize = 11.sp, color = Color.Gray)
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        vitamins.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { vit ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardVitamins),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("✨", fontSize = 10.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = vit, fontSize = 11.sp, color = SchoolTealTertiary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // GRAFIK SEDERHANA NUTRISI (NATIVE CANVAS BAR CHART) (Sesuai Butir 5)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Grafik Komposisi Berat Gizi (gram):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    val maxVal = remember(carb, prot, fat, fiber) {
                        maxOf(carb, prot, fat, fiber, 10.0) // Set minimal scale 10g
                    }

                    // Native Canvas drawings for lightweight graphic (Butir 7 animations & icons)
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        
                        val categories = listOf("Karbo", "Protein", "Lemak", "Serat")
                        val values = listOf(carb, prot, fat, fiber)
                        val colors = listOf(GiziPerhatianKuning, GiziBaikHijau, GiziKurangMerah, SchoolTealTertiary)
                        
                        val barCount = 4
                        val paddingRatio = 0.3f
                        val contentWidth = canvasWidth * (1f - paddingRatio)
                        val barWidth = contentWidth / barCount
                        val totalGap = canvasWidth * paddingRatio
                        val barGap = totalGap / (barCount + 1)
                        
                        // Garis dasar
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, canvasHeight - 24f),
                            end = Offset(canvasWidth, canvasHeight - 24f),
                            strokeWidth = 2f
                        )

                        for (i in 0 until barCount) {
                            val valRatio = (values[i] / maxVal).toFloat()
                            val fixedHeight = (canvasHeight - 50f) * valRatio
                            val startX = barGap + i * (barWidth + barGap)
                            val startY = canvasHeight - 24f - fixedHeight
                            
                            // Gambar Batang Grafik
                            drawRect(
                                color = colors[i],
                                size = androidx.compose.ui.geometry.Size(barWidth, fixedHeight),
                                topLeft = Offset(startX, startY)
                            )
                            
                            // Nilai diatas batang
                            val labelValue = String.format("%.1f", values[i])
                            drawContext.canvas.nativeCanvas.drawText(
                                "${labelValue}g",
                                startX + barWidth/2 - 12f,
                                startY - 8f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.BLACK
                                    textSize = 10.sp.toPx()
                                    isAntiAlias = true
                                }
                            )

                            // Label nama kategori dibawah
                            drawContext.canvas.nativeCanvas.drawText(
                                categories[i],
                                startX + barWidth/2 - 18f,
                                canvasHeight - 8f,
                                android.graphics.Paint().apply {
                                    val isDarkActive = colors[i] == GiziBaikHijau // adjust dynamic color contrasts
                                    color = android.graphics.Color.DKGRAY
                                    textSize = 10.sp.toPx()
                                    isAntiAlias = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // REKOMENDASI GIZI (Sesuai Butir 4)
        item {
            val suggestions = viewModel.getRecommendations()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💡", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rekomendasi Ahli Gizi untuk Bekal Ini:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    suggestions.forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = suggestion,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        // AKSI FOOTER (Butir 6: Export PDF, Simpan, Reset)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.saveCalculationToHistory(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("simpan_riwayat_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GiziBaikHijau)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan ke Riwayat Bekal 💾", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.exportToPdfAndShare(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_pdf_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SchoolTealTertiary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ekspor PDF & Share 📄", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDone,
                        modifier = Modifier
                            .weight(0.8f)
                            .height(48.dp)
                            .testTag("selesai_hitung_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Mulai Baru 🔄", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun NutrientProgressBar(
    title: String,
    amount: Double,
    target: Double,
    unit: String,
    barColor: Color
) {
    val progressRatio = (amount / target).coerceIn(0.0, 1.2f.toDouble()).toFloat()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${String.format("%.1f", amount)}$unit / ${target.toInt()}$unit (${(progressRatio*100).toInt()}%)",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progressRatio / 1.2f }, // scale 120% max visually
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}


// --------------------------------------------------------------------------
// 4. LAYAR RIWAYAT LOG SEMUA (Alternatif Riwayat detail)
// --------------------------------------------------------------------------
@Composable
fun RiwayatScreen(viewModel: NutrisiViewModel, historyList: List<HistoryEntity>, onBack: () -> Unit) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBack() }
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kembali ke Beranda", fontWeight = FontWeight.Bold)
        }

        Text(
            text = "Seluruh Catatan Riwayat Bekal",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (historyList.isEmpty()) {
                item {
                    Text("Belum ada riwayat aktivitas tersimpan.")
                }
            } else {
                items(historyList) { history ->
                    HistoryRowItem(history = history, onDelete = {
                        viewModel.deleteHistoryEntry(history.id, context)
                    })
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// FLOWROW IMPLEMENTATION helper (Untuk menyumbang susunan Vitamin horizontal otomatis)
// --------------------------------------------------------------------------
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Implementasi tata letak FlowRow sederhana menggunakan M3 Row dengan pembungkus dasar
    // Untuk Compose sederhana, kita buat Row dengan scroll horizontal, atau bungkus Box.
    // Demi kehandalan rilis di emulator, kita letakkan Row horizontal scrollable yang ramah performa.
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}
