package com.medichart.database

import com.medichart.model.Medication
import com.medichart.model.PastMedication
import com.medichart.model.Surgery
import java.sql.*
import java.util.ArrayList

/**
 * Manages database connections and operations for the MediChart application.
 * Uses SQLite as the database.
 */
class DatabaseManager {

    private val DATABASE_URL = "jdbc:sqlite:medichart.db"

    /**
     * Establishes a connection to the SQLite database.
     * Creates the database file if it doesn't exist.
     *
     * @return A Connection object to the database, or null if connection fails.
     */
    private fun connect(): Connection? {
        return try {
            // db parameters
            // create a connection to the database
            DriverManager.getConnection(DATABASE_URL)
            // System.out.println("Connection to SQLite has been established.") // Optional: for debugging
        } catch (e: SQLException) {
            System.err.println("Error connecting to the database: ${e.message}")
            // In a real app, you'd want more robust error handling/logging
            null
        }
    }

    /**
     * Creates the necessary tables in the database if they do not already exist.
     * Tables include 'current_meds', 'past_meds', and 'surgeries'.
     */
    fun createTables() {
        // SQL statement for creating a new table
        val createCurrentMedsTable = """
            CREATE TABLE IF NOT EXISTS current_meds (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                generic_name TEXT NOT NULL,
                brand_name TEXT,
                dosage TEXT,
                dose_form TEXT,
                instructions TEXT,
                reason TEXT,
                prescriber TEXT,
                notes TEXT, -- Current medication notes
                start_date TEXT, -- Optional start date
                manufacturer TEXT
            );
        """.trimIndent() // Use trimIndent for multiline strings

        val createPastMedsTable = """
            CREATE TABLE IF NOT EXISTS past_meds (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                generic_name TEXT NOT NULL,
                brand_name TEXT,
                dosage TEXT,
                dose_form TEXT,
                instructions TEXT,
                reason TEXT,
                prescriber TEXT,
                history_notes TEXT, -- Notes about effectiveness, side effects, etc.
                reason_for_stopping TEXT,
                date_ranges TEXT, -- Store as a delimited string or JSON for simplicity initially
                manufacturer TEXT
            );
        """.trimIndent()

        val createSurgeriesTable = """
             CREATE TABLE IF NOT EXISTS surgeries (
                 id INTEGER PRIMARY KEY AUTOINCREMENT,
                 name TEXT NOT NULL,
                 date TEXT, -- Using TEXT for date simplicity
                 surgeon TEXT
             );
         """.trimIndent()

        connect()?.use { conn -> // Use Kotlin's 'use' for auto-closing resources
            conn.createStatement().use { stmt ->
                stmt.execute(createCurrentMedsTable)
                stmt.execute(createPastMedsTable)
                stmt.execute(createSurgeriesTable)
                // System.out.println("Tables checked/created successfully.") // Optional: for debugging
            }
        } ?: System.err.println("Failed to connect to database to create tables.") // Elvis operator for null check
    }

    /**
     * Adds a new medication to the 'current_meds' table.
     *
     * @param med The Medication object to add.
     */
    fun addMedication(med: Medication) {
        val sql = "INSERT INTO current_meds(generic_name, brand_name, dosage, dose_form, instructions, reason, prescriber, notes, start_date, manufacturer) VALUES(?,?,?,?,?,?,?,?,?,?)"

        connect()?.use { conn ->
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, med.genericName)
                pstmt.setString(2, med.brandName)
                pstmt.setString(3, med.dosage)
                pstmt.setString(4, med.doseForm)
                pstmt.setString(5, med.instructions)
                pstmt.setString(6, med.reason)
                pstmt.setString(7, med.prescriber)
                pstmt.setString(8, med.notes)
                pstmt.setString(9, med.startDate)
                pstmt.setString(10, med.manufacturer)
                pstmt.executeUpdate()
                // System.out.println("Medication added: ${med.brandName}") // Optional: for debugging
            }
        } ?: System.err.println("Failed to connect to database to add medication.")
    }

    /**
     * Retrieves all medications from the 'current_meds' table.
     *
     * @return A list of Medication objects.
     */
    fun getAllCurrentMedications(): List<Medication> {
        val sql = "SELECT id, generic_name, brand_name, dosage, dose_form, instructions, reason, prescriber, notes, start_date, manufacturer FROM current_meds"
        val medications = mutableListOf<Medication>() // Use mutable list

        connect()?.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(sql).use { rs -> // Use 'use' for ResultSet
                    // loop through the result set
                    while (rs.next()) {
                        val med = Medication(
                            rs.getInt("id"),
                            rs.getString("generic_name"),
                            rs.getString("brand_name"),
                            rs.getString("dosage"),
                            rs.getString("dose_form"),
                            rs.getString("instructions"),
                            rs.getString("reason"),
                            rs.getString("prescriber"),
                            rs.getString("notes"),
                            rs.getString("start_date"),
                            rs.getString("manufacturer")
                        )
                        medications.add(med)
                    }
                }
            }
        } ?: System.err.println("Failed to connect to database to retrieve current medications.")

        return medications // Return immutable list
    }

    /**
     * Archives a medication: moves it from 'current_meds' to 'past_meds'.
     * Note: This basic implementation copies data and deletes. A more robust version
     * would handle updating the `date_ranges` in past_meds if the medication
     * already exists in history.
     *
     * @param med The Medication object to archive.
     */
    fun archiveMedication(med: Medication) {
        // Basic Implementation: Insert into past_meds, then delete from current_meds

        // Insert into past_meds
        val insertSql = "INSERT INTO past_meds(generic_name, brand_name, dosage, dose_form, instructions, reason, prescriber, history_notes, reason_for_stopping, date_ranges, manufacturer) VALUES(?,?,?,?,?,?,?,?,?,?,?)"

        connect()?.use { conn ->
            try {
                conn.autoCommit = false // Start transaction

                conn.prepareStatement(insertSql).use { pstmt ->
                    pstmt.setString(1, med.genericName)
                    pstmt.setString(2, med.brandName)
                    pstmt.setString(3, med.dosage)
                    pstmt.setString(4, med.doseForm)
                    pstmt.setString(5, med.instructions)
                    pstmt.setString(6, med.reason)
                    pstmt.setString(7, med.prescriber)
                    pstmt.setString(8, med.notes) // Copy current notes to history_notes initially
                    pstmt.setString(9, "") // Reason for stopping - will need to be added by user later
                    pstmt.setString(10, med.startDate?.let { "$it to Present" } ?: "Unknown Start to Present") // Simple date range representation
                    pstmt.setString(11, med.manufacturer)


                    pstmt.executeUpdate()
                    // System.out.println("Medication archived to history (insert step): ${med.brandName}") // Optional: for debugging
                }

                // Delete from current_meds
                val deleteSql = "DELETE FROM current_meds WHERE id = ?"
                conn.prepareStatement(deleteSql).use { pstmt ->
                    pstmt.setInt(1, med.id)
                    pstmt.executeUpdate()
                    // System.out.println("Medication archived from current (delete step): ${med.brandName}") // Optional: for debugging
                }

                conn.commit() // Commit transaction

            } catch (e: SQLException) {
                System.err.println("Error archiving medication: ${e.message}")
                conn.rollback() // Rollback transaction on error
            } finally {
                conn.autoCommit = true // Restore auto-commit
            }
        } ?: System.err.println("Failed to connect to database for archiving.")
    }

    /**
     * Unarchives a medication: moves it from 'past_meds' back to 'current_meds'.
     *
     * @param pastMed The PastMedication object to unarchive.
     */
    fun unarchiveMedication(pastMed: PastMedication) {
        // Basic Implementation: Insert into current_meds, then delete from past_meds

        val insertSql = "INSERT INTO current_meds(generic_name, brand_name, dosage, dose_form, instructions, reason, prescriber, notes, start_date, manufacturer) VALUES(?,?,?,?,?,?,?,?,?,?)"
        val deleteSql = "DELETE FROM past_meds WHERE id = ?"

        connect()?.use { conn ->
            try {
                conn.autoCommit = false // Start transaction

                // Insert into current_meds
                conn.prepareStatement(insertSql).use { pstmt ->
                    pstmt.setString(1, pastMed.genericName)
                    pstmt.setString(2, pastMed.brandName)
                    pstmt.setString(3, pastMed.dosage)
                    pstmt.setString(4, pastMed.doseForm)
                    pstmt.setString(5, pastMed.instructions)
                    pstmt.setString(6, pastMed.reason)
                    pstmt.setString(7, pastMed.prescriber)
                    pstmt.setString(8, pastMed.historyNotes) // Copy history notes to current notes
                    // When unarchiving, the start date is "now". Let's use a placeholder.
                    pstmt.setString(9, "Re-started") // Placeholder for new start date
                    pstmt.setString(10, pastMed.manufacturer)

                    pstmt.executeUpdate()
                    // System.out.println("Medication unarchived to current (insert step): ${pastMed.brandName}") // Optional: for debugging
                }

                // Delete from past_meds
                conn.prepareStatement(deleteSql).use { pstmt ->
                    pstmt.setInt(1, pastMed.id)
                    pstmt.executeUpdate()
                    // System.out.println("Medication unarchived from history (delete step): ${pastMed.brandName}") // Optional: for debugging
                }

                conn.commit() // Commit transaction
                // System.out.println("Medication unarchived successfully.")

            } catch (e: SQLException) {
                System.err.println("Error unarchiving medication: ${e.message}")
                conn.rollback() // Rollback transaction on error
            } finally {
                conn.autoCommit = true // Restore auto-commit
            }
        } ?: System.err.println("Failed to connect to database for unarchiving.")
    }

    /**
     * Deletes a medication record from the 'current_meds' table by its ID.
     *
     * @param id The ID of the medication to delete.
     */
    fun deleteCurrentMedication(id: Int) {
        val sql = "DELETE FROM current_meds WHERE id = ?"

        connect()?.use { conn ->
            try {
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setInt(1, id)
                    val rowsAffected = pstmt.executeUpdate()
                    if (rowsAffected > 0) {
                        println("Medication with ID $id deleted successfully.") // Optional: for debugging
                    } else {
                        println("No medication found with ID $id.") // Optional: for debugging
                    }
                }
            } catch (e: SQLException) {
                System.err.println("Error deleting medication: ${e.message}")
                // TODO: Handle or log the error appropriately
            }
        } ?: System.err.println("Failed to connect to database to delete medication.")
    }

    /**
     * Deletes a past medication record from the 'past_meds' table by its ID.
     *
     * @param id The ID of the past medication to delete.
     */
    fun deletePastMedication(id: Int) {
        val sql = "DELETE FROM past_meds WHERE id = ?"

        connect()?.use { conn ->
            try {
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setInt(1, id)
                    val rowsAffected = pstmt.executeUpdate()
                    if (rowsAffected > 0) {
                        println("Past medication with ID $id deleted successfully.")    // Optional: for debugging
                    } else {
                        println("No past medication found with ID $id.")    // Optional: for debugging
                    }
                }
            } catch (e: SQLException) {
                System.err.println("Error deleting past medication: ${e.message}")
                // TODO: Handle or log the error appropriately
            }
        } ?: System.err.println("Failed to connect to database to delete past medication.")
    }



    /**
     * Retrieves all medications from the 'past_meds' table.
     * Note: The date_ranges column is currently stored as a simple string.
     * Parsing this into a List<PastMedication.DateRange> needs to be implemented.
     *
     * @return A list of PastMedication objects.
     */
    fun getAllPastMedications(): List<PastMedication> {
        val sql = "SELECT id, generic_name, brand_name, dosage, dose_form, instructions, reason, prescriber, history_notes, reason_for_stopping, date_ranges, manufacturer FROM past_meds"
        val pastMedications = mutableListOf<PastMedication>()

        connect()?.use { conn ->
            try {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        // loop through the result set
                        while (rs.next()) {
                            // TODO: Implement parsing of date_ranges string into List<PastMedication.DateRange>
                            val dateRanges = listOf<PastMedication.DateRange>() // Placeholder for parsed list

                            val pastMed = PastMedication(
                                rs.getInt("id"),
                                rs.getString("generic_name"),
                                rs.getString("brand_name"),
                                rs.getString("dosage"),
                                rs.getString("dose_form"),
                                rs.getString("instructions"),
                                rs.getString("reason"),
                                rs.getString("prescriber"),
                                rs.getString("history_notes"),
                                rs.getString("reason_for_stopping"),
                                dateRanges, // Use the parsed list
                                rs.getString("manufacturer")
                            )
                            pastMedications.add(pastMed)
                        }
                    }
                }
            } catch (e: SQLException) {
                System.err.println("Error retrieving past medications: ${e.message}")
            }
        } ?: System.err.println("Failed to connect to database to retrieve past medications.")

        return pastMedications.toList()
    }

    /**
     * Adds a new surgery to the 'surgeries' table.
     * @param surgery The Surgery object to add.
     */
    fun addSurgery(surgery: Surgery) {
        val sql = "INSERT INTO surgeries(name, date, surgeon) VALUES(?,?,?)"

        connect()?.use { conn ->
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, surgery.name)
                pstmt.setString(2, surgery.date)
                pstmt.setString(3, surgery.surgeon)
                pstmt.executeUpdate()
                // System.out.println("Surgery added: ${surgery.name}") // Optional: for debugging
            }
        } ?: System.err.println("Failed to connect to database to add surgery.")
    }

    /**
     * Retrieves all surgeries from the 'surgeries' table.
     * @return A list of Surgery objects.
     */
    fun getAllSurgeries(): List<Surgery> {
        val sql = "SELECT id, name, date, surgeon FROM surgeries"
        val surgeries = mutableListOf<Surgery>()

        connect()?.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(sql).use { rs ->
                    // loop through the result set
                    while (rs.next()) {
                        val surgery = Surgery(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("date"),
                            rs.getString("surgeon")
                        )
                        surgeries.add(surgery)
                    }
                }
            }
        } ?: System.err.println("Failed to connect to database to retrieve surgeries.")

        return surgeries
    }

    // TODO: Add methods for updating records (e.g., editing notes, adding end date when archiving)
    // TODO: Implement proper date range storage and parsing for past_meds
    // TODO: Implement delete methods for medications and surgeries
}