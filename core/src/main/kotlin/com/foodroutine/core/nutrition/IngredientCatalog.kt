package com.foodroutine.core.nutrition

import com.foodroutine.core.model.NutritionFacts
import com.foodroutine.core.model.PrepMethod

/** Catalog entry: nutrition per 100 g and the usual prep method. */
data class CatalogItem(
    val name: String,
    val per100g: NutritionFacts,
    val defaultPrep: PrepMethod = PrepMethod.NONE
)

/**
 * Built-in nutrition reference (per 100 g raw) for common Indian vegetarian
 * ingredients, used to auto-fill nutrition when composing recipes. Values
 * are typical published figures (kcal, protein g, carbs g, fiber g).
 */
object IngredientCatalog {

    private fun item(
        name: String, kcal: Double, protein: Double, carbs: Double, fiber: Double,
        prep: PrepMethod = PrepMethod.NONE
    ) = CatalogItem(name, NutritionFacts(kcal, protein, carbs, fiber), prep)

    val items: List<CatalogItem> = listOf(
        // Grains & flours
        item("Rice (raw)", 360.0, 6.8, 78.2, 1.3),
        item("Brown rice (raw)", 362.0, 7.5, 76.2, 3.4),
        item("Poha (flattened rice)", 346.0, 6.6, 77.3, 2.0),
        item("Wheat flour (atta)", 341.0, 12.1, 69.4, 11.2),
        item("Maida (refined flour)", 364.0, 10.3, 76.3, 2.7),
        item("Semolina (rava/sooji)", 360.0, 12.7, 72.8, 3.9),
        item("Ragi flour", 328.0, 7.3, 72.0, 11.5),
        item("Jowar (sorghum)", 349.0, 10.4, 72.6, 9.7),
        item("Bajra (pearl millet)", 361.0, 11.6, 67.5, 11.3),
        item("Oats", 389.0, 16.9, 66.3, 10.6),
        item("Idli rice (raw)", 356.0, 7.0, 78.0, 1.0, PrepMethod.SOAK_OVERNIGHT),
        // Pulses & legumes
        item("Toor dal (pigeon pea)", 343.0, 22.3, 62.8, 15.0),
        item("Moong dal (split)", 347.0, 24.5, 59.9, 8.2),
        item("Whole moong (green gram)", 347.0, 23.9, 62.6, 16.3, PrepMethod.SPROUT),
        item("Chana dal", 360.0, 20.8, 59.8, 12.2, PrepMethod.SOAK_OVERNIGHT),
        item("Kabuli chana (chickpeas)", 364.0, 19.3, 60.7, 17.4, PrepMethod.SOAK_OVERNIGHT),
        item("Kala chana (black chickpeas)", 360.0, 20.5, 59.0, 17.0, PrepMethod.SOAK_OVERNIGHT),
        item("Rajma (kidney beans)", 333.0, 23.6, 60.0, 24.9, PrepMethod.SOAK_OVERNIGHT),
        item("Urad dal", 341.0, 25.2, 58.9, 18.3, PrepMethod.SOAK_OVERNIGHT),
        item("Masoor dal (red lentils)", 352.0, 24.6, 63.4, 10.7),
        item("Matki (moth beans)", 343.0, 22.9, 61.5, 16.0, PrepMethod.SPROUT),
        item("Horse gram (kulthi)", 321.0, 22.0, 57.2, 5.3, PrepMethod.SPROUT),
        // Vegetables
        item("Potato", 77.0, 2.0, 17.5, 2.1),
        item("Onion", 40.0, 1.1, 9.3, 1.7),
        item("Tomato", 18.0, 0.9, 3.9, 1.2),
        item("Spinach (palak)", 23.0, 2.9, 3.6, 2.2),
        item("Fenugreek leaves (methi)", 49.0, 4.4, 6.0, 1.1),
        item("Cauliflower", 25.0, 1.9, 5.0, 2.0),
        item("Cabbage", 25.0, 1.3, 5.8, 2.5),
        item("Bottle gourd (lauki)", 14.0, 0.6, 3.4, 0.5),
        item("Ridge gourd (turai)", 20.0, 1.2, 4.4, 1.1),
        item("Bitter gourd (karela)", 17.0, 1.0, 3.7, 2.8),
        item("Okra (bhindi)", 33.0, 1.9, 7.5, 3.2),
        item("Brinjal (eggplant)", 25.0, 1.0, 5.9, 3.0),
        item("Pumpkin (kaddu)", 26.0, 1.0, 6.5, 0.5),
        item("Carrot", 41.0, 0.9, 9.6, 2.8),
        item("Green peas", 81.0, 5.4, 14.5, 5.7),
        item("French beans", 31.0, 1.8, 7.0, 2.7),
        item("Capsicum", 20.0, 0.9, 4.6, 1.7),
        item("Cucumber", 15.0, 0.7, 3.6, 0.5),
        item("Beetroot", 43.0, 1.6, 9.6, 2.8),
        item("Drumstick (moringa pods)", 37.0, 2.1, 8.5, 3.2),
        item("Green chilli", 40.0, 1.9, 8.8, 1.5),
        item("Ginger", 80.0, 1.8, 17.8, 2.0),
        item("Garlic", 149.0, 6.4, 33.1, 2.1),
        item("Coriander leaves", 23.0, 2.1, 3.7, 2.8),
        item("Curry leaves", 108.0, 6.1, 18.7, 6.4),
        // Dairy & fats
        item("Milk (whole)", 61.0, 3.2, 4.8, 0.0),
        item("Curd (dahi)", 60.0, 3.1, 4.7, 0.0),
        item("Paneer", 265.0, 18.3, 1.2, 0.0),
        item("Ghee", 900.0, 0.0, 0.0, 0.0),
        item("Butter", 717.0, 0.9, 0.1, 0.0),
        item("Groundnut oil", 884.0, 0.0, 0.0, 0.0),
        item("Coconut oil", 892.0, 0.0, 0.0, 0.0),
        item("Coconut (fresh)", 354.0, 3.3, 15.2, 9.0),
        // Nuts, seeds & misc
        item("Peanuts", 567.0, 25.8, 16.1, 8.5, PrepMethod.SOAK_OVERNIGHT),
        item("Almonds", 579.0, 21.2, 21.6, 12.5, PrepMethod.SOAK_OVERNIGHT),
        item("Cashew nuts", 553.0, 18.2, 30.2, 3.3),
        item("Sesame seeds (til)", 573.0, 17.7, 23.4, 11.8),
        item("Jaggery (gud)", 383.0, 0.4, 98.0, 0.0),
        item("Sugar", 387.0, 0.0, 99.8, 0.0),
        item("Sabudana (tapioca pearls)", 358.0, 0.2, 88.7, 0.9, PrepMethod.SOAK_OVERNIGHT),
        item("Besan (gram flour)", 387.0, 22.4, 57.8, 10.8),
        item("Tamarind", 239.0, 2.8, 62.5, 5.1),
        item("Lemon", 29.0, 1.1, 9.3, 2.8)
    )

    /** Case-insensitive lookup by exact name. */
    fun find(name: String): CatalogItem? =
        items.firstOrNull { it.name.equals(name, ignoreCase = true) }

    /** Simple substring search for editor autocomplete. */
    fun search(query: String): List<CatalogItem> {
        if (query.isBlank()) return items
        val q = query.trim().lowercase()
        return items.filter { q in it.name.lowercase() }
    }
}
