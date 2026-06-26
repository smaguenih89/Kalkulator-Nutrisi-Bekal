/**
 * SCRIPT UTAMA - KALKULATOR NUTRISI BEKAL SEKOLAH
 * Mengelola interaksi UI, kalkulasi gizi, status bekal, local storage,
 * serta pembuatan laporan PDF instan untuk keperluan siswa, guru, & orang tua.
 */

// STATE / STATUS UTAMA APLIKASI
let currentLunchbox = []; // Daftar makanan yang sedang dimasukkan ke kotak bekal saat ini
let customFoods = []; // Daftar makanan buatan user (kustom)
let historyRecords = []; // Daftar riwayat perhitungan gizi tersimpan
let activeCategory = "all"; // Kategori filter aktif
let selectedFoodToModal = null; // Makanan yang sedang dipilih untuk dimasukkan (modal)

// BATASAN STANDARD GIZI HARIAN ANAK SEKOLAH (Ideal per Kali Makan Siang/Bekal)
const LIMITS = {
  minCalories: 400,
  maxCalories: 650,
  minProtein: 12, // Gram
  minCarbs: 50,  // Gram
  maxCarbs: 90,   // Gram
  minFiber: 1.5,  // Gram
};

// ELEMEN-ELEMEN DOM UTAMA
const screenHome = document.getElementById("screenHome");
const screenInput = document.getElementById("screenInput");
const screenResults = document.getElementById("screenResults");

const startCalculateBtn = document.getElementById("startCalculateBtn");
const backToInputBtn = document.getElementById("backToInputBtn");
const viewAnalysisBtn = document.getElementById("viewAnalysisBtn");
const finishAnalysisBtn = document.getElementById("finishAnalysisBtn");
const resetLunchboxBtn = document.getElementById("resetLunchboxBtn");

const searchFoodInput = document.getElementById("searchFoodInput");
const lunchTitleInput = document.getElementById("lunchTitleInput");
const foodListContainer = document.getElementById("foodListContainer");
const categoryChipsContainer = document.getElementById("categoryChipsContainer");
const lunchboxMiniItems = document.getElementById("lunchboxMiniItems");
const miniTotalCalories = document.getElementById("miniTotalCalories");

// Modals
const addFoodModal = document.getElementById("addFoodModal");
const modalFoodIcon = document.getElementById("modalFoodIcon");
const modalFoodName = document.getElementById("modalFoodName");
const modalFoodCategory = document.getElementById("modalFoodCategory");
const modalFoodWeight = document.getElementById("modalFoodWeight");
const modalFoodPortion = document.getElementById("modalFoodPortion");
const confirmAddFoodBtn = document.getElementById("confirmAddFoodBtn");
const closeAddFoodModalBtn = document.getElementById("closeAddFoodModalBtn");

const customFoodModal = document.getElementById("customFoodModal");
const openCustomFoodModalBtn = document.getElementById("openCustomFoodModalBtn");
const closeCustomFoodModalBtn = document.getElementById("closeCustomFoodModalBtn");
const cancelCustomFoodBtn = document.getElementById("cancelCustomFoodBtn");
const customFoodForm = document.getElementById("customFoodForm");

// History & Actions
const historyContainer = document.getElementById("historyContainer");
const historyCount = document.getElementById("historyCount");
const clearHistoryBtn = document.getElementById("clearHistoryBtn");
const saveToHistoryBtn = document.getElementById("saveToHistoryBtn");
const exportPdfBtn = document.getElementById("exportPdfBtn");

// Theme Toggle
const themeToggleBtn = document.getElementById("themeToggleBtn");
const themeIcon = document.getElementById("themeIcon");
const navHomeBtn = document.getElementById("navHomeBtn");

// ==========================================
// 1. STRUKTUR UTAMA DAN INISIALISASI DATA
// ==========================================

document.addEventListener("DOMContentLoaded", () => {
  // Load data gizi kustom dan riwayat dari penyimpanan lokal browser (LocalStorage)
  loadCustomFoods();
  loadHistoryRecords();
  
  // Render list makanan mula-mula
  renderFoodList();
  renderHistoryList();

  // Daftarkan listener klik
  setupEventListeners();

  // Inisialisasi tema (Mengikuti preferensi sistem jika ada)
  if (localStorage.getItem("theme") === "dark" || 
      (!localStorage.getItem("theme") && window.matchMedia("(prefers-color-scheme: dark)").matches)) {
    document.documentElement.classList.add("dark");
    themeIcon.className = "fa-solid fa-sun text-lg";
  } else {
    document.documentElement.classList.remove("dark");
    themeIcon.className = "fa-solid fa-moon text-lg";
  }
});

// Setup event listener tombol
function setupEventListeners() {
  // Pindah Layar
  startCalculateBtn.addEventListener("click", () => {
    switchScreen("input");
  });

  backToInputBtn.addEventListener("click", () => {
    switchScreen("input");
  });

  navHomeBtn.addEventListener("click", () => {
    switchScreen("home");
  });

  // Toggle Mode Gelap
  themeToggleBtn.addEventListener("click", () => {
    if (document.documentElement.classList.contains("dark")) {
      document.documentElement.classList.remove("dark");
      themeIcon.className = "fa-solid fa-moon text-lg";
      localStorage.setItem("theme", "light");
    } else {
      document.documentElement.classList.add("dark");
      themeIcon.className = "fa-solid fa-sun text-lg";
      localStorage.setItem("theme", "dark");
    }
  });

  // Filter Kategori Chips
  categoryChipsContainer.addEventListener("click", (e) => {
    const chip = e.target.closest(".category-chip");
    if (!chip) return;

    // Bersihkan kelas aktif di chip lain
    document.querySelectorAll(".category-chip").forEach(btn => {
      btn.className = "category-chip px-3.5 py-1.5 rounded-full text-xs font-bold bg-slate-100 dark:bg-zinc-800 text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-zinc-700 transition-all";
    });

    // Pasang kelas aktif pada chip terpilih
    chip.className = "category-chip active px-3.5 py-1.5 rounded-full text-xs font-bold bg-emerald-500 text-white transition-all";
    activeCategory = chip.dataset.category;
    renderFoodList();
  });

  // Input Pencarian
  searchFoodInput.addEventListener("input", () => {
    renderFoodList();
  });

  // Modal Input Berat & Porsi
  closeAddFoodModalBtn.addEventListener("click", () => {
    addFoodModal.classList.add("hidden");
  });

  // Preset Berat
  document.querySelectorAll(".preset-weight-btn").forEach(btn => {
    btn.addEventListener("click", (e) => {
      modalFoodWeight.value = e.target.dataset.weight;
      document.querySelectorAll(".preset-weight-btn").forEach(b => b.classList.remove("border-2", "border-emerald-500"));
      e.target.classList.add("border-2", "border-emerald-500");
    });
  });

  // Preset Porsi
  document.querySelectorAll(".preset-portion-btn").forEach(btn => {
    btn.addEventListener("click", (e) => {
      modalFoodPortion.value = e.target.dataset.portion;
      document.querySelectorAll(".preset-portion-btn").forEach(b => b.classList.remove("border-2", "border-teal-500"));
      e.target.classList.add("border-2", "border-teal-500");
    });
  });

  // Konfirmasi Masuk Bekal
  confirmAddFoodBtn.addEventListener("click", () => {
    const weight = parseFloat(modalFoodWeight.value) || 100;
    const portion = parseFloat(modalFoodPortion.value) || 1;
    
    if (selectedFoodToModal) {
      addFoodToLunchbox(selectedFoodToModal, weight, portion);
      addFoodModal.classList.add("hidden");
    }
  });

  // Modal Makanan Kustom Baru
  openCustomFoodModalBtn.addEventListener("click", () => {
    customFoodModal.classList.remove("hidden");
  });

  closeCustomFoodModalBtn.addEventListener("click", () => {
    customFoodModal.classList.add("hidden");
  });

  cancelCustomFoodBtn.addEventListener("click", () => {
    customFoodModal.classList.add("hidden");
  });

  // Form submit gizi kustom
  customFoodForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const newFood = {
      name: document.getElementById("customName").value,
      category: document.getElementById("customCategory").value,
      calories: parseFloat(document.getElementById("customCalories").value) || 0,
      protein: parseFloat(document.getElementById("customProtein").value) || 0,
      carbohydrates: parseFloat(document.getElementById("customCarbs").value) || 0,
      fat: parseFloat(document.getElementById("customFat").value) || 0,
      fiber: parseFloat(document.getElementById("customFiber").value) || 0,
      vitamin: document.getElementById("customVitamin").value || "-"
    };

    customFoods.push(newFood);
    saveCustomFoods();
    renderFoodList();
    customFoodForm.reset();
    customFoodModal.classList.add("hidden");
    alert(`🎉 "${newFood.name}" berhasil ditambahkan ke daftar pilihan makanan!`);
  });

  // Bersihkan kotak bekal
  resetLunchboxBtn.addEventListener("click", () => {
    if (confirm("Apakah Anda yakin ingin mengosongkan menu bekal saat ini?")) {
      currentLunchbox = [];
      updateLunchboxSummary();
    }
  });

  // Lihat Analisis
  viewAnalysisBtn.addEventListener("click", () => {
    if (currentLunchbox.length === 0) return;
    calculateAndShowResults();
  });

  // Selesaikan perhitungan & reset
  finishAnalysisBtn.addEventListener("click", () => {
    currentLunchbox = [];
    updateLunchboxSummary();
    switchScreen("home");
  });

  // Simpan riwayat gizi
  saveToHistoryBtn.addEventListener("click", () => {
    saveCurrentToHistory();
  });

  // Bersihkan semua riwayat
  clearHistoryBtn.addEventListener("click", () => {
    if (confirm("Apakah Anda yakin ingin menghapus seluruh riwayat gizi tersimpan?")) {
      historyRecords = [];
      localStorage.removeItem("lunch_history_records");
      renderHistoryList();
    }
  });

  // Ekspor / Cetak PDF Laporan
  exportPdfBtn.addEventListener("click", () => {
    window.print();
  });
}

// ==========================================
// 2. RENDERING DAN MANIPULASI TAMPILAN
// ==========================================

// Ganti Layar Navigasi
function switchScreen(screenName) {
  screenHome.classList.add("hidden");
  screenInput.classList.add("hidden");
  screenResults.classList.add("hidden");
  navHomeBtn.classList.add("hidden");

  if (screenName === "home") {
    screenHome.classList.remove("hidden");
    screenHome.classList.add("animate-fade-in");
  } else if (screenName === "input") {
    screenInput.classList.remove("hidden");
    screenInput.classList.add("animate-fade-in");
    navHomeBtn.classList.remove("hidden");
  } else if (screenName === "results") {
    screenResults.classList.remove("hidden");
    screenResults.classList.add("animate-fade-in");
    navHomeBtn.classList.remove("hidden");
  }
}

// Ambil list gabungan makanan bawaan + kustom
function getAllFoods() {
  return [...FOOD_DATABASE, ...customFoods];
}

// Render grid makanan tersedia
function renderFoodList() {
  foodListContainer.innerHTML = "";
  const query = searchFoodInput.value.toLowerCase().trim();
  const filtered = getAllFoods().filter(food => {
    const matchesSearch = food.name.toLowerCase().includes(query);
    const matchesCategory = (activeCategory === "all" || food.category === activeCategory);
    return matchesSearch && matchesCategory;
  });

  if (filtered.length === 0) {
    foodListContainer.innerHTML = `
      <div class="col-span-full py-8 text-center text-slate-400 text-xs">
        <i class="fa-solid fa-cookie-bite text-xl mb-2"></i>
        <p>Makanan tidak ditemukan. Silakan tambahkan makanan kustom baru!</p>
      </div>
    `;
    return;
  }

  filtered.forEach(food => {
    const emoji = CATEGORY_EMOJIS[food.category] || "🍱";
    const item = document.createElement("div");
    item.className = "bg-white dark:bg-zinc-900 border border-slate-200 dark:border-zinc-800 p-4 rounded-2xl flex items-center justify-between cursor-pointer hover:border-emerald-400 dark:hover:border-emerald-500 hover:shadow-md transition-all group";
    
    item.innerHTML = `
      <div class="flex items-center space-x-3">
        <span class="text-2xl">${emoji}</span>
        <div>
          <h4 class="font-bold text-slate-800 dark:text-slate-100 group-hover:text-emerald-600 transition-colors text-xs md:text-sm">${food.name}</h4>
          <p class="text-[10px] text-slate-400">${food.category} • ${food.calories} kkal/100g</p>
        </div>
      </div>
      <span class="text-xs font-bold text-emerald-600 dark:text-emerald-400 flex items-center space-x-1">
        <span>Pilih</span>
        <i class="fa-solid fa-plus-circle text-sm group-hover:scale-110 transition-transform"></i>
      </span>
    `;

    item.addEventListener("click", () => {
      openAddFoodModal(food);
    });

    foodListContainer.appendChild(item);
  });
}

// Membuka modal masukkan porsi
function openAddFoodModal(food) {
  selectedFoodToModal = food;
  modalFoodIcon.textContent = CATEGORY_EMOJIS[food.category] || "🍱";
  modalFoodName.textContent = food.name;
  modalFoodCategory.textContent = food.category;
  modalFoodWeight.value = "100";
  modalFoodPortion.value = "1";

  // Reset preset border
  document.querySelectorAll(".preset-weight-btn").forEach(b => b.classList.remove("border-2", "border-emerald-500"));
  document.querySelectorAll(".preset-portion-btn").forEach(b => b.classList.remove("border-2", "border-teal-500"));

  addFoodModal.classList.remove("hidden");
}

// Masukkan makanan terpilih ke kotak bekal gizi
function addFoodToLunchbox(food, weight, portion) {
  const itemTotalMultiplier = (weight / 100) * portion;
  
  const newItem = {
    id: Date.now() + Math.random().toString(36).substring(2, 7),
    name: food.name,
    category: food.category,
    weight: weight,
    portion: portion,
    // Hitung gizi berdasarkan gramasi porsi
    calories: Math.round(food.calories * itemTotalMultiplier),
    protein: parseFloat((food.protein * itemTotalMultiplier).toFixed(1)),
    carbohydrates: parseFloat((food.carbohydrates * itemTotalMultiplier).toFixed(1)),
    fat: parseFloat((food.fat * itemTotalMultiplier).toFixed(1)),
    fiber: parseFloat((food.fiber * itemTotalMultiplier).toFixed(1)),
    vitamin: food.vitamin || "-"
  };

  currentLunchbox.push(newItem);
  updateLunchboxSummary();
}

// Hapus satu lauk dari bekal saat ini
function removeFoodFromLunchbox(id) {
  currentLunchbox = currentLunchbox.filter(item => item.id !== id);
  updateLunchboxSummary();
}

// Sinkronkan data ringkasan bekal di bagian bawah layar input
function updateLunchboxSummary() {
  lunchboxMiniItems.innerHTML = "";
  
  if (currentLunchbox.length === 0) {
    lunchboxMiniItems.innerHTML = `<p class="text-white/60 italic text-[11px]">Belum ada makanan di kotak bekal. Klik pilihan di atas!</p>`;
    miniTotalCalories.textContent = "0";
    viewAnalysisBtn.disabled = true;
    return;
  }

  viewAnalysisBtn.disabled = false;
  let totalCal = 0;

  currentLunchbox.forEach(item => {
    totalCal += item.calories;
    const emoji = CATEGORY_EMOJIS[item.category] || "🍱";

    const chip = document.createElement("div");
    chip.className = "bg-white/15 hover:bg-white/25 text-white pl-2.5 pr-1.5 py-1 rounded-full flex items-center space-x-1.5 font-semibold transition-all";
    chip.innerHTML = `
      <span>${emoji} ${item.name} (${item.weight}g x ${item.portion})</span>
      <button class="text-white/60 hover:text-red-300 ml-1 focus:outline-none" title="Hapus">
        <i class="fa-solid fa-circle-xmark"></i>
      </button>
    `;

    // Pasang event delete
    chip.querySelector("button").addEventListener("click", (e) => {
      e.stopPropagation();
      removeFoodFromLunchbox(item.id);
    });

    lunchboxMiniItems.appendChild(chip);
  });

  miniTotalCalories.textContent = totalCal;
}

// ==========================================
// 3. KALKULASI GIZI & EVALUASI REKOMENDASI
// ==========================================

function calculateAndShowResults() {
  let totCalories = 0;
  let totProtein = 0;
  let totCarbs = 0;
  let totFat = 0;
  let totFiber = 0;
  let vitaminsSet = new Set();

  currentLunchbox.forEach(item => {
    totCalories += item.calories;
    totProtein += item.protein;
    totCarbs += item.carbohydrates;
    totFat += item.fat;
    totFiber += item.fiber;

    if (item.vitamin && item.vitamin !== "-") {
      item.vitamin.split(",").forEach(v => {
        const cleanV = v.trim();
        if (cleanV) vitaminsSet.add(cleanV);
      });
    }
  });

  // Tulis Angka Hasil
  document.getElementById("resCalories").textContent = totCalories;
  document.getElementById("resProtein").textContent = totProtein.toFixed(1) + " g";
  document.getElementById("resCarbs").textContent = totCarbs.toFixed(1) + " g";
  document.getElementById("resFat").textContent = totFat.toFixed(1) + " g";
  document.getElementById("resFiber").textContent = totFiber.toFixed(1) + " g";

  // Render Vitamin
  const resVitamins = document.getElementById("resVitamins");
  if (vitaminsSet.size > 0) {
    resVitamins.textContent = Array.from(vitaminsSet).join(", ");
  } else {
    resVitamins.textContent = "Tidak signifikan";
  }

  // HITUNG PERSENTASE PROGRESS BAR (Dibandingkan batas gizi bekal seimbang)
  // Energi target tengah = 525 kkal (antara 400-650)
  const pctCal = Math.min(Math.round((totCalories / 525) * 100), 100);
  const pctProt = Math.min(Math.round((totProtein / 15) * 100), 100);
  const pctCarbs = Math.min(Math.round((totCarbs / 70) * 100), 100);
  const pctFib = Math.min(Math.round((totFiber / 1.5) * 100), 100);

  document.getElementById("pctCaloriesText").textContent = pctCal + "%";
  document.getElementById("pctCaloriesBar").style.width = pctCal + "%";
  
  document.getElementById("pctProteinText").textContent = pctProt + "%";
  document.getElementById("pctProteinBar").style.width = pctProt + "%";
  
  document.getElementById("pctCarbsText").textContent = pctCarbs + "%";
  document.getElementById("pctCarbsBar").style.width = pctCarbs + "%";
  
  document.getElementById("pctFiberText").textContent = pctFib + "%";
  document.getElementById("pctFiberBar").style.width = pctFib + "%";

  // EVALUASI KATEGORI GIZI & REKOMENDASI EDUKATIF (Sesuai Butir 4 & 5)
  const recommendations = [];
  let scoreTitle = "Gizi Cukup Seimbang";
  let scoreDesc = "Bekal bekalmu sudah lumayan seimbang, namun bisa disempurnakan lagi dengan menambahkan beberapa variasi lauk bergizi.";
  let scoreColor = "border-emerald-500";
  let titleColor = "text-emerald-600";

  // Periksa Karbohidrat
  if (totCarbs < LIMITS.minCarbs) {
    recommendations.push("🍙 <b>Tambahkan karbohidrat</b> seperti nasi putih, kentang, atau roti gandum agar anak tidak lemas dan tetap bertenaga saat belajar.");
  } else if (totCarbs > LIMITS.maxCarbs) {
    recommendations.push("⚠️ <b>Karbohidrat terlalu tinggi!</b> Kurangi porsi nasi atau roti untuk menghindari kantuk berlebih di kelas.");
  }

  // Periksa Protein
  if (totProtein < LIMITS.minProtein) {
    recommendations.push("🍗 <b>Kekurangan Protein!</b> Tambahkan telur rebus, tempe, tahu, ikan, atau daging ayam untuk mendukung perkembangan otot dan otak anak.");
  }

  // Periksa Serat (Sayur & Buah)
  const hasVegOrFruit = currentLunchbox.some(item => item.category === "Sayuran" || item.category === "Buah");
  if (!hasVegOrFruit || totFiber < LIMITS.minFiber) {
    recommendations.push("🥦 <b>Wajib tambahkan sayur atau buah!</b> Masukkan brokoli, wortel, pisang, atau apel agar pencernaan anak sehat dan asupan vitamin tercukupi.");
  }

  // Periksa Kalori Total
  if (totCalories < LIMITS.minCalories) {
    recommendations.push("🔥 <b>Kalori kurang dari ideal:</b> Tambahkan susu kotak UHT atau cemilan gandum sehat sebagai pelengkap bekal.");
    scoreTitle = "Kurang Gizi Seimbang";
    scoreDesc = "Nilai kalori atau zat gizi bekal masih di bawah batas kebutuhan makan siang anak sekolah. Yuk, tambah lauk pauk lainnya!";
    scoreColor = "border-amber-400";
    titleColor = "text-amber-600";
  } else if (totCalories > LIMITS.maxCalories) {
    recommendations.push("⚠️ <b>Kalori berlebih:</b> Kurangi gorengan atau snack manis, ganti dengan air putih atau buah segar.");
    scoreTitle = "Kelebihan Kalori (Kurang Seimbang)";
    scoreDesc = "Bekal ini memiliki jumlah kalori yang tinggi dari porsi makan siang ideal. Kurangi porsi makanan berlemak atau berpemanis.";
    scoreColor = "border-red-400";
    titleColor = "text-red-500";
  } else {
    // Jika semua gizi dasar memenuhi standard seimbang
    if (totProtein >= LIMITS.minProtein && totCarbs >= LIMITS.minCarbs && totCarbs <= LIMITS.maxCarbs && totFiber >= LIMITS.minFiber) {
      scoreTitle = "Bekal Sangat Bergizi! ⭐";
      scoreDesc = "Sempurna & Seimbang! Bekal sekolah ini sudah memenuhi seluruh kriteria gizi seimbang harian anak sekolah.";
      scoreColor = "border-emerald-500";
      titleColor = "text-emerald-600";
    }
  }

  // Tambahkan tips umum jika menu sudah sempurna
  if (recommendations.length === 0) {
    recommendations.push("✅ <b>Sempurna!</b> Pertahankan menu ini dan lengkapi dengan air putih hangat agar konsentrasi belajar makin prima.");
  }

  // Update Tampilan Hasil
  const evalCard = document.getElementById("evaluationScoreCard");
  evalCard.className = `bg-white dark:bg-zinc-900 rounded-3xl p-6 shadow-xl border-t-8 ${scoreColor} text-center space-y-3`;
  
  const evalTitle = document.getElementById("evaluationScoreTitle");
  evalTitle.textContent = scoreTitle;
  evalTitle.className = `text-2xl md:text-3xl font-black tracking-tight ${titleColor}`;
  
  document.getElementById("evaluationScoreSubtitle").textContent = scoreDesc;

  // Render List Saran
  const recList = document.getElementById("recommendationList");
  recList.innerHTML = "";
  recommendations.forEach(rec => {
    const li = document.createElement("li");
    li.className = "flex items-start space-x-2 bg-slate-50 dark:bg-zinc-800/50 p-3 rounded-xl border border-slate-100 dark:border-zinc-800";
    li.innerHTML = `<span>📍</span><span>${rec}</span>`;
    recList.appendChild(li);
  });

  // Render Menu Bekal di Layar Hasil
  const resultsFoodList = document.getElementById("resultsFoodList");
  resultsFoodList.innerHTML = "";
  currentLunchbox.forEach(item => {
    const emoji = CATEGORY_EMOJIS[item.category] || "🍱";
    const div = document.createElement("div");
    div.className = "flex items-center justify-between text-xs py-2 border-b border-slate-100 dark:border-zinc-800 last:border-b-0";
    div.innerHTML = `
      <div class="flex items-center space-x-2">
        <span>${emoji}</span>
        <div>
          <span class="font-bold text-slate-700 dark:text-slate-200">${item.name}</span>
          <span class="text-slate-400">(${item.weight}g x ${item.portion})</span>
        </div>
      </div>
      <div class="text-right">
        <span class="font-bold text-slate-800 dark:text-slate-100">${item.calories} kkal</span>
        <div class="text-[10px] text-slate-400">P: ${item.protein}g | K: ${item.carbohydrates}g</div>
      </div>
    `;
    resultsFoodList.appendChild(div);
  });

  switchScreen("results");
}

// ==========================================
// 4. PERSISTENCE (LOCAL STORAGE) & HISTORY
// ==========================================

// Simpan data makanan kustom harian
function saveCustomFoods() {
  localStorage.setItem("lunch_custom_foods", JSON.stringify(customFoods));
}

function loadCustomFoods() {
  const data = localStorage.getItem("lunch_custom_foods");
  if (data) {
    customFoods = JSON.parse(data);
  }
}

// Simpan hasil saat ini ke daftar riwayat lokal
function saveCurrentToHistory() {
  if (currentLunchbox.length === 0) return;

  let totCalories = 0;
  let totProtein = 0;
  let totCarbs = 0;
  let totFat = 0;
  let totFiber = 0;

  currentLunchbox.forEach(item => {
    totCalories += item.calories;
    totProtein += item.protein;
    totCarbs += item.carbohydrates;
    totFat += item.fat;
    totFiber += item.fiber;
  });

  const title = lunchTitleInput.value.trim() || "Bekal Sekolah";
  const now = new Date();
  const dateString = now.toLocaleDateString("id-ID", {
    day: "numeric",
    month: "short",
    year: "numeric"
  }) + " • " + now.toLocaleTimeString("id-ID", { hour: '2-digit', minute: '2-digit' });

  const record = {
    id: Date.now().toString(),
    title: title,
    date: dateString,
    calories: totCalories,
    protein: totProtein,
    carbohydrates: totCarbs,
    fat: totFat,
    fiber: totFiber,
    menuCount: currentLunchbox.length
  };

  historyRecords.unshift(record); // Tambahkan ke baris paling atas
  localStorage.setItem("lunch_history_records", JSON.stringify(historyRecords));
  renderHistoryList();
  
  alert(`💾 Riwayat gizi "${title}" berhasil disimpan di memori offline browser!`);
  switchScreen("home");
}

function loadHistoryRecords() {
  const data = localStorage.getItem("lunch_history_records");
  if (data) {
    historyRecords = JSON.parse(data);
  }
}

// Render list riwayat gizi di Beranda
function renderHistoryList() {
  historyContainer.innerHTML = "";
  historyCount.textContent = historyRecords.length;

  if (historyRecords.length > 0) {
    clearHistoryBtn.classList.remove("hidden");
  } else {
    clearHistoryBtn.classList.add("hidden");
  }

  if (historyRecords.length === 0) {
    historyContainer.innerHTML = `
      <div class="bg-white dark:bg-zinc-900 rounded-2xl p-6 text-center text-slate-400 dark:text-slate-500 text-xs border border-dashed border-slate-300 dark:border-zinc-800">
        <i class="fa-solid fa-clock-rotate-left text-2xl mb-2 text-emerald-500"></i>
        <p>Belum ada riwayat perhitungan gizi tersimpan.</p>
        <p class="text-slate-400/70 mt-1">Selesaikan kalkulasi lalu simpan riwayat untuk melihat catatan di sini.</p>
      </div>
    `;
    return;
  }

  historyRecords.forEach(record => {
    const card = document.createElement("div");
    card.className = "bg-white dark:bg-zinc-900 rounded-2xl p-4 shadow-sm border border-slate-100 dark:border-zinc-800 flex items-center justify-between hover:shadow-md transition-shadow relative overflow-hidden group";
    
    // Status warna ringkasan kalori
    let indicatorColor = "bg-emerald-500";
    if (record.calories < LIMITS.minCalories) indicatorColor = "bg-amber-500";
    if (record.calories > LIMITS.maxCalories) indicatorColor = "bg-red-500";

    card.innerHTML = `
      <div class="flex items-center space-x-3">
        <div class="w-1.5 h-12 ${indicatorColor} rounded-full"></div>
        <div>
          <h4 class="font-extrabold text-xs md:text-sm text-slate-800 dark:text-slate-100">${record.title}</h4>
          <p class="text-[10px] text-slate-400">${record.date} • ${record.menuCount} macam menu</p>
          
          <div class="flex items-center space-x-3 mt-1.5 text-[10px] text-slate-500 font-bold">
            <span class="text-orange-600 dark:text-orange-400">🔥 ${record.calories} kkal</span>
            <span class="text-emerald-600 dark:text-emerald-400">🍗 P: ${record.protein.toFixed(1)}g</span>
            <span class="text-blue-600 dark:text-blue-400">🍚 K: ${record.carbohydrates.toFixed(1)}g</span>
          </div>
        </div>
      </div>
      
      <button class="delete-history-btn p-2 text-slate-400 hover:text-red-500 transition-colors focus:outline-none" data-id="${record.id}" title="Hapus catatan ini">
        <i class="fa-regular fa-trash-can text-sm"></i>
      </button>
    `;

    card.querySelector(".delete-history-btn").addEventListener("click", (e) => {
      e.stopPropagation();
      deleteHistoryItem(record.id);
    });

    historyContainer.appendChild(card);
  });
}

function deleteHistoryItem(id) {
  if (confirm("Hapus catatan riwayat gizi ini?")) {
    historyRecords = historyRecords.filter(item => item.id !== id);
    localStorage.setItem("lunch_history_records", JSON.stringify(historyRecords));
    renderHistoryList();
  }
}
