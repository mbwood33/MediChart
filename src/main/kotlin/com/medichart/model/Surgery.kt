package com.medichart.model

/**
 * Represents a past surgery.
 */
data class Surgery(
    val id: Int,    // Database ID
    val name: String,
    val date: String?,  // Uses String for date simplicity
    val surgeon: String?
)