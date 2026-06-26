/**
 * DATABASE CONTOH MAKANAN (Kandungan Nutrisi per 100 Gram)
 * Siswa, Guru, dan Orang Tua dapat menambah atau mengubah data makanan di bawah ini dengan mudah.
 * 
 * Keterangan Atribut:
 * - name: Nama bahan makanan
 * - category: Karbohidrat, Protein, Sayuran, Buah, Minuman, atau Snack
 * - calories: Kandungan Energi dalam kilo kalori (kkal) per 100 gram
 * - protein: Kandungan Protein dalam gram (g) per 100 gram
 * - carbohydrates: Kandungan Karbohidrat dalam gram (g) per 100 gram
 * - fat: Kandungan Lemak dalam gram (g) per 100 gram
 * - fiber: Kandungan Serat dalam gram (g) per 100 gram
 * - vitamin: Jenis Vitamin/Mineral dominan yang terkandung didalamnya
 */
const FOOD_DATABASE = [
  {
    name: "Nasi Putih",
    category: "Karbohidrat",
    calories: 130,
    protein: 2.7,
    carbohydrates: 28.0,
    fat: 0.3,
    fiber: 0.4,
    vitamin: "B1, B3"
  },
  {
    name: "Ayam Goreng",
    category: "Protein",
    calories: 246,
    protein: 25.0,
    carbohydrates: 0,
    fat: 16.0,
    fiber: 0,
    vitamin: "B6, B12, Zat Besi"
  },
  {
    name: "Telur Rebus",
    category: "Protein",
    calories: 155,
    protein: 13.0,
    carbohydrates: 1.1,
    fat: 11.0,
    fiber: 0,
    vitamin: "A, D, B12, Kalsium"
  },
  {
    name: "Tempe Goreng",
    category: "Protein",
    calories: 193,
    protein: 19.0,
    carbohydrates: 9.4,
    fat: 11.0,
    fiber: 1.4,
    vitamin: "B12, Kalsium"
  },
  {
    name: "Tahu Goreng",
    category: "Protein",
    calories: 76,
    protein: 8.0,
    carbohydrates: 1.9,
    fat: 4.8,
    fiber: 0.3,
    vitamin: "Kalsium, Zat Besi"
  },
  {
    name: "Sayur Bayam",
    category: "Sayuran",
    calories: 23,
    protein: 2.9,
    carbohydrates: 3.6,
    fat: 0.4,
    fiber: 2.2,
    vitamin: "A, C, K, Asam Folat"
  },
  {
    name: "Apel",
    category: "Buah",
    calories: 52,
    protein: 0.3,
    carbohydrates: 14.0,
    fat: 0.2,
    fiber: 2.4,
    vitamin: "C, Kalium"
  },
  {
    name: "Pisang",
    category: "Buah",
    calories: 89,
    protein: 1.1,
    carbohydrates: 23.0,
    fat: 0.3,
    fiber: 2.6,
    vitamin: "B6, C, Kalium"
  },
  {
    name: "Suku Sapi UHT",
    category: "Minuman",
    calories: 61,
    protein: 3.2,
    carbohydrates: 4.8,
    fat: 3.3,
    fiber: 0,
    vitamin: "D, B12, Kalsium"
  },
  {
    name: "Roti Tawar",
    category: "Karbohidrat",
    calories: 265,
    protein: 9.0,
    carbohydrates: 49.0,
    fat: 3.2,
    fiber: 2.7,
    vitamin: "B1, B2, Zat Besi"
  },
  {
    name: "Susu Kedelai",
    category: "Minuman",
    calories: 54,
    protein: 3.3,
    carbohydrates: 6.0,
    fat: 1.8,
    fiber: 0.6,
    vitamin: "D, Zat Besi"
  },
  {
    name: "Kentang Rebus",
    category: "Karbohidrat",
    calories: 87,
    protein: 1.9,
    carbohydrates: 20.0,
    fat: 0.1,
    fiber: 1.8,
    vitamin: "C, B6"
  },
  {
    name: "Sayur Sop Wortel",
    category: "Sayuran",
    calories: 35,
    protein: 1.0,
    carbohydrates: 7.5,
    fat: 0.2,
    fiber: 1.5,
    vitamin: "A, C"
  },
  {
    name: "Jeruk",
    category: "Buah",
    calories: 47,
    protein: 0.9,
    carbohydrates: 12.0,
    fat: 0.1,
    fiber: 2.4,
    vitamin: "C, Asam Folat"
  },
  {
    name: "Biskuit Gandum",
    category: "Snack",
    calories: 450,
    protein: 7.0,
    carbohydrates: 68.0,
    fat: 17.0,
    fiber: 4.0,
    vitamin: "B Kompleks"
  }
];

// Emoji representasi untuk mempermudah pemahaman visual siswa
const CATEGORY_EMOJIS = {
  "Karbohidrat": "🍚",
  "Protein": "🍗",
  "Sayuran": "🥦",
  "Buah": "🍎",
  "Minuman": "🥛",
  "Snack": "🍪"
};
