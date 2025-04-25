package com.medichart.model

import java.time.LocalDate

/**
 * Represents a past surgery.
 */
data class Surgery(
    val id: Int,    // Database ID
    val name: String,
    val date: LocalDate?,  // Uses String for date simplicity
    val surgeon: String?
)