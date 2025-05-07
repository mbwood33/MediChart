// src/main/kotlin/com/medichart/model/Physician.kt
package com.medichart.model

/**
 * Data class representing a Physician record
 */
data class Physician(
    val id: Long = 0,   // Database primary key
    val name: String,   // Physician's full name, assuming this is a required field
    val specialty: String?, // Medical specialty (e.g., Cardiology, Pediatrics); made nullable as it might not always be known/applicable
    val phone: String?,
    val fax: String?,
    val email: String?,
    val address: String?,   // Full address string
    val notes: String?
)