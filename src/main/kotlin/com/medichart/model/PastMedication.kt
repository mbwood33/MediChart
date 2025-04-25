package com.medichart.model

/**
 * Represents a medication taken in the past.
 * Data class provides automatic equals(), hashCode(), toString(), and copy().
 */
data class PastMedication(
    val id: Int,    // Database ID
    val genericName: String,
    val brandName: String?,
    val dosage: String?,
    val instructions: String?,
    val reason: String?,
    val prescriber: String?,
    val historyNotes: String?,  // Notes about effectiveness, side effects, tolerability, etc.
    val reasonForStopping: String?,
    val dateRanges: List<DateRange> = listOf()  // To track multiple periods of taking
) {
    /**
     * Inner data class to represent a start and end date range.
     */
    data class DateRange(
        val startDate: String?, // Uses String, could be LocalDate
        val endDate: String?    // Using String, could be Local Date
    ) {
        /**
         * Provides a string representation for the date range.
         */
        override fun toString(): String {
            return "${startDate ?: "Unknown Start"} to ${endDate ?: "Present"}"
        }
    }
}