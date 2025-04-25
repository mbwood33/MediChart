package com.medichart.model

import java.time.LocalDate

/**
 * Represents a medication taken in the past.
 * Data class provides automatic equals(), hashCode(), toString(), and copy().
 */
data class PastMedication(
    val id: Int,    // Database ID
    val genericName: String,
    val brandName: String?,
    val dosage: String?,
    val doseForm: String?,
    val instructions: String?,
    val reason: String?,
    val prescriber: String?,
    val historyNotes: String?,  // Notes about effectiveness, side effects, tolerability, etc.
    val reasonForStopping: String?,
    val dateRanges: List<DateRange> = listOf(),  // To track multiple periods of taking
    val manufacturer: String?
) {
    /**
     * Inner data class to represent a start and end date range.
     */
    data class DateRange(
        val startDate: LocalDate?, // Uses String, could be LocalDate
        val endDate: LocalDate?    // Using String, could be Local Date
    ) {
        /**
         * Provides a string representation for the date range.
         */
        override fun toString(): String {
            return "${startDate?.toString() ?: "Unknown Start"} to ${endDate?.toString() ?: "Present"}"
        }
    }
}