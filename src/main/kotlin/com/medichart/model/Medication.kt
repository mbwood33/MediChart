package com.medichart.model

/**
 * Represents a medication currently being taken.
 * Data class provides automatic equals(), hashCode(), toString(), and copy().
 * All properties are immutable (val). If editing is needed, you would likely
 * create a new instance or use a mutable backing property with a getter/setter.
 */
data class Medication(
    val id: Int,    // Database ID
    val genericName: String,
    val brandName: String?,
    val dosage: String?,
    val instructions: String?,
    val reason: String?,
    val prescriber: String?,
    val notes: String?,
    val startDate: String?  // Using String for initial simplicity
)