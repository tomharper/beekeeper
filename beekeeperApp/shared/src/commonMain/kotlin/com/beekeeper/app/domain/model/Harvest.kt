package com.beekeeper.app.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Represents a honey or product harvest
 */
@Serializable
data class Harvest(
    val id: String,
    val hiveId: String,
    val apiaryId: String? = null,
    val harvestDate: LocalDate,

    // Product details
    val productType: HarvestProductType,
    val quantity: Double,
    val unit: HarvestUnit,

    // Quality metrics
    val moisture: Double? = null,  // percentage
    val color: HoneyColor? = null,
    val flavor: String? = null,
    val floralSource: String? = null,

    // Processing
    val processed: Boolean = false,
    val processingDate: LocalDate? = null,
    val containerCount: Int? = null,
    val containerSize: Double? = null,
    val containerUnit: HarvestUnit? = null,

    // Additional info
    val weather: String? = null,
    val notes: String = "",
    val photos: List<String> = emptyList(),

    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents beekeeping expenses
 */
@Serializable
data class Expense(
    val id: String,
    val date: LocalDate,
    val category: ExpenseCategory,
    val description: String,
    val amount: Double,
    val currency: String = "USD",
    val vendor: String? = null,
    val hiveId: String? = null,
    val apiaryId: String? = null,
    val receiptPhoto: String? = null,
    val notes: String = "",
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents beekeeping income
 */
@Serializable
data class Income(
    val id: String,
    val date: LocalDate,
    val category: IncomeCategory,
    val description: String,
    val amount: Double,
    val currency: String = "USD",
    val buyer: String? = null,
    val relatedHarvestId: String? = null,
    val quantity: Double? = null,
    val unit: HarvestUnit? = null,
    val notes: String = "",
    val createdAt: String,
    val updatedAt: String
)

// Enums

@Serializable
enum class HarvestProductType {
    HONEY,
    BEESWAX,
    PROPOLIS,
    POLLEN,
    ROYAL_JELLY,
    BEE_VENOM,
    NUCLEUS_COLONY,
    PACKAGE_BEES,
    QUEEN_BEE,
    OTHER
}

@Serializable
enum class HarvestUnit {
    GRAMS,
    KILOGRAMS,
    OUNCES,
    POUNDS,
    MILLILITERS,
    LITERS,
    GALLONS,
    FRAMES,
    COLONIES,
    QUEENS,
    OTHER
}

@Serializable
enum class HoneyColor {
    WATER_WHITE,
    EXTRA_WHITE,
    WHITE,
    EXTRA_LIGHT_AMBER,
    LIGHT_AMBER,
    AMBER,
    DARK_AMBER,
    DARK
}

@Serializable
enum class ExpenseCategory {
    // Equipment
    HIVE_BODIES,
    FRAMES,
    FOUNDATION,
    TOOLS,
    PROTECTIVE_GEAR,
    EXTRACTION_EQUIPMENT,

    // Bees
    PACKAGE_BEES,
    NUCLEUS_COLONIES,
    QUEENS,

    // Supplies
    FEED,
    MEDICATION,
    TREATMENTS,
    PACKAGING,

    // Operations
    FUEL,
    MAINTENANCE,
    REPAIRS,
    RENT,

    // Other
    EDUCATION,
    REGISTRATION,
    INSURANCE,
    OTHER
}

@Serializable
enum class IncomeCategory {
    HONEY_SALES,
    WAX_SALES,
    PROPOLIS_SALES,
    POLLEN_SALES,
    BEE_SALES,
    QUEEN_SALES,
    POLLINATION_SERVICES,
    EDUCATIONAL_SERVICES,
    EQUIPMENT_SALES,
    OTHER
}
