package com.example.data

/**
 * Kategori Makanan Bekal Sekolah
 */
enum class FoodCategory(val displayName: String, val icon: String) {
    KARBOHIDRAT("Karbohidrat", "🍚"),
    PROTEIN("Protein", "🍗"),
    SAYURAN("Sayuran", "🥦"),
    BUAH("Buah", "🍎"),
    MINUMAN("Minuman", "🥛"),
    SNACK("Snack/Camilan", "🍪")
}

/**
 * Model Data untuk Makanan dalam Database (per 100 gram)
 */
data class FoodInfo(
    val name: String,
    val category: FoodCategory,
    val calories: Double,    // kkal per 100g
    val protein: Double,     // gram per 100g
    val carbohydrates: Double, // gram per 100g
    val fat: Double,         // gram per 100g
    val fiber: Double,       // gram per 100g
    val vitamin: String      // deskripsi vitamin/mineral
)

/**
 * Item makanan aktif dalam bekal yang sedang dihitung
 */
data class ActiveLunchItem(
    val foodInfo: FoodInfo,
    val weightGrams: Double,
    val portionCount: Double = 1.0
) {
    // Menghitung nutrisi berdasarkan berat makanan dan porsi
    // Rumus: (nilai nutrisi per 100g * berat * porsi) / 100
    val totalCalories: Double get() = (foodInfo.calories * weightGrams * portionCount) / 100.0
    val totalProtein: Double get() = (foodInfo.protein * weightGrams * portionCount) / 100.0
    val totalCarbohydrates: Double get() = (foodInfo.carbohydrates * weightGrams * portionCount) / 100.0
    val totalFat: Double get() = (foodInfo.fat * weightGrams * portionCount) / 100.0
    val totalFiber: Double get() = (foodInfo.fiber * weightGrams * portionCount) / 100.0
}

/**
 * DATABASE CONTOH MAKANAN (Sesuai Permintaan)
 * Siswa, Ibu, atau Guru dapat menambahkan atau mengubah data makanan di bawah ini dengan mudah.
 * Satuan data di bawah ini adalah kandungan gizi per 100 gram makanan.
 */
object FoodDatabase {
    val defaultFoods = listOf(
        FoodInfo(
            name = "Nasi Putih",
            category = FoodCategory.KARBOHIDRAT,
            calories = 130.0,
            protein = 2.7,
            carbohydrates = 28.0,
            fat = 0.3,
            fiber = 0.4,
            vitamin = "B1, B3"
        ),
        FoodInfo(
            name = "Ayam Goreng",
            category = FoodCategory.PROTEIN,
            calories = 246.0,
            protein = 25.0,
            carbohydrates = 0.0,
            fat = 16.0,
            fiber = 0.0,
            vitamin = "B6, B12, Zat Besi"
        ),
        FoodInfo(
            name = "Telur Rebus",
            category = FoodCategory.PROTEIN,
            calories = 155.0,
            protein = 13.0,
            carbohydrates = 1.1,
            fat = 11.0,
            fiber = 0.0,
            vitamin = "A, D, B12, Riboflavin"
        ),
        FoodInfo(
            name = "Tempe Goreng",
            category = FoodCategory.PROTEIN,
            calories = 193.0,
            protein = 19.0,
            carbohydrates = 9.4,
            fat = 11.0,
            fiber = 1.4,
            vitamin = "B12, Kalsium"
        ),
        FoodInfo(
            name = "Tahu Goreng",
            category = FoodCategory.PROTEIN,
            calories = 76.0,
            protein = 8.0,
            carbohydrates = 1.9,
            fat = 4.8,
            fiber = 0.3,
            vitamin = "Zat Besi, Kalsium"
        ),
        FoodInfo(
            name = "Sayur Bayam",
            category = FoodCategory.SAYURAN,
            calories = 23.0,
            protein = 2.9,
            carbohydrates = 3.6,
            fat = 0.4,
            fiber = 2.2,
            vitamin = "A, C, K, Asam Folat"
        ),
        FoodInfo(
            name = "Apel",
            category = FoodCategory.BUAH,
            calories = 52.0,
            protein = 0.3,
            carbohydrates = 14.0,
            fat = 0.2,
            fiber = 2.4,
            vitamin = "C, Kalium"
        ),
        FoodInfo(
            name = "Pisang",
            category = FoodCategory.BUAH,
            calories = 89.0,
            protein = 1.1,
            carbohydrates = 23.0,
            fat = 0.3,
            fiber = 2.6,
            vitamin = "B6, C, Kalium"
        ),
        FoodInfo(
            name = "Susu Sapi UHT",
            category = FoodCategory.MINUMAN,
            calories = 61.0,
            protein = 3.2,
            carbohydrates = 4.8,
            fat = 3.3,
            fiber = 0.0,
            vitamin = "D, B12, Kalsium"
        ),
        FoodInfo(
            name = "Roti Tawar",
            category = FoodCategory.KARBOHIDRAT,
            calories = 265.0,
            protein = 9.0,
            carbohydrates = 49.0,
            fat = 3.2,
            fiber = 2.7,
            vitamin = "B1, B2, Zat Besi"
        ),
        // Item tambahan untuk variasi bekal sehat anak sekolah
        FoodInfo(
            name = "Susu Kedelai",
            category = FoodCategory.MINUMAN,
            calories = 54.0,
            protein = 3.3,
            carbohydrates = 6.0,
            fat = 1.8,
            fiber = 0.6,
            vitamin = "D, Zat Besi"
        ),
        FoodInfo(
            name = "Kentang Rebus",
            category = FoodCategory.KARBOHIDRAT,
            calories = 87.0,
            protein = 1.9,
            carbohydrates = 20.0,
            fat = 0.1,
            fiber = 1.8,
            vitamin = "C, B6"
        ),
        FoodInfo(
            name = "Sayur Sop Wortel",
            category = FoodCategory.SAYURAN,
            calories = 35.0,
            protein = 1.0,
            carbohydrates = 7.5,
            fat = 0.2,
            fiber = 1.5,
            vitamin = "A, C"
        ),
        FoodInfo(
            name = "Jeruk",
            category = FoodCategory.BUAH,
            calories = 47.0,
            protein = 0.9,
            carbohydrates = 12.0,
            fat = 0.1,
            fiber = 2.4,
            vitamin = "C, Asam Folat"
        ),
        FoodInfo(
            name = "Biskuit Gandum",
            category = FoodCategory.SNACK,
            calories = 450.0,
            protein = 7.0,
            carbohydrates = 68.0,
            fat = 17.0,
            fiber = 4.0,
            vitamin = "B Kompleks"
        )
    )
}
