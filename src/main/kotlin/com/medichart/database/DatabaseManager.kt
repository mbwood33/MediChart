package com.medichart.database

import com.medichart.model.Medication
import com.medichart.model.PastMedication
import com.medichart.model.PastMedication.DateRange
import com.medichart.model.Surgery
import java.sql.*
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Manages database connections and operations for the MediChart application.
 * Uses SQLite as the database.
 */
class DatabaseManager {

    private val DATABASE_URL = "jdbc:sqlite:medichart.db"
    // Define a formatter if you need a specific date format, but ISO is default for LocalDate.toString()
    // private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // YYYY-MM-DD

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
     * The date columns are stored as TEXT (ISO YYYY-MM-DD format).
     * NOTE: This does NOT update schema for existing tables. Delete medichart.db to apply schema changes.
     */
    fun createTables() {
        // SQL statement for creating a new table
        // start_date is TEXT, suitable for ISO LocalDate strings (YYYY-MM-DD)
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
                start_date TEXT, -- Optional start date (TEXT for YYYY-MM-DD)
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
     * Converts LocalDate start_date to String for storage.
     *
     * @param med The Medication object to add. Note: id is ignored for insertion as it's AUTOINCREMENT.
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
                // Convert LocalDate to String (YYYY-MM-DD) for database storage. Handles null automatically.
                pstmt.setString(9, med.startDate?.toString())
                pstmt.setString(10, med.manufacturer)
                pstmt.executeUpdate()
                // System.out.println("Medication added: ${med.brandName}") // Optional: for debugging
            }
        } ?: System.err.println("Failed to connect to database to add medication.")
    }

    /**
     * Retrieves all medications from the 'current_meds' table.
     * Converts String start_date from database back to LocalDate
     *
     * @return A list of Medication objects.
     */
    fun getAllCurrentMedications(): List<Medication> {
        val sql = "SELECT id, generic_name, brand_name, dosage, dose_form, instructions, reason, prescriber, notes, start_date, manufacturer FROM current_meds"
        val medications = mutableListOf<Medication>() // Use mutable list

        connect()?.use { conn ->
            try {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs -> // Use 'use' for ResultSet
                        // loop through the result set
                        while (rs.next()) {
                            // Retrieve date as String and convert to LocalDate
                            val startDateString = rs.getString("start_date")
                            val startDate = try {
                                if (startDateString != null && startDateString.isNotBlank()) {  // Check if string is not null or blank
                                    LocalDate.parse(startDateString)
                                } else {
                                    null    // Return null for null or blank strings
                                }
                            } catch (e: DateTimeParseException) {
                                System.err.println("Error parsing start_date '$startDateString': ${e.message}")
                                null    // Assign null in the catch block to ensure startDate is initialized
                            }

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
                                startDate,  // Use the parsed LocalDate
                                rs.getString("manufacturer")
                            )
                            medications.add(med)
                        }
                    }
                }
            } catch(e: SQLException) {
                System.err.println("Errpr retrieving current medications: ${e.message}")
            }
        }  ?: System.err.println("Failed to connect to database to retrieve current medications.")

        return medications.toList() // Return immutable list
    }

    /**
     * Updates an existing medication record in the current_meds table.
     * Matches the record by the Medication object's ID.
     * @param medication The Medication object with updated details.
     */
    fun updateMedication(medication: Medication) {
        // SQL UPDATE statement to update all columns based on the 'id'
        val sql = """
            UPDATE current_meds SET
                generic_name = ?, brand_name = ?, dosage = ?, dose_form = ?,
                instructions = ?, reason = ?, prescriber = ?, notes = ?,
                start_date = ?, manufacturer = ?
            WHERE id = ?;
        """.trimIndent()

        connect()?.use { conn ->    // Get a database connection and use{} for auto-closing
            conn.prepareStatement(sql).use { pstmt ->   // Prepare the SQL statement and use use {}
                pstmt.setString(1, medication.genericName)
                pstmt.setString(2, medication.brandName?.takeIf { it.isNotEmpty() })
                pstmt.setString(3, medication.dosage?.takeIf { it.isNotEmpty() })
                pstmt.setString(4, medication.doseForm?.takeIf { it.isNotEmpty() })
                pstmt.setString(5, medication.instructions?.takeIf { it.isNotEmpty() })
                pstmt.setString(6, medication.reason?.takeIf { it.isNotEmpty() })
                pstmt.setString(7, medication.prescriber?.takeIf { it.isNotEmpty() })
                pstmt.setString(8, medication.notes?.takeIf { it.isNotEmpty() })
                pstmt.setString(9, medication.startDate?.toString())
                pstmt.setString(10, medication.manufacturer?.takeIf { it.isNotEmpty() })

                // Set the WHERE clause parameter (the medication's ID)
                pstmt.setInt(11, medication.id)

                // Execute the update statement
                val affectedRows = pstmt.executeUpdate()

                if (affectedRows > 0) {
                    println("Medication ID ${medication.id} updated successfully in database.")
                } else {
                    println("No medication found with ID ${medication.id} to update in database.")
                }
            }
        } ?: System.err.println("Failed to update medication ID ${medication.id}: Could not get database connection.")
    }

    /**
     * Archives a medication: moves it from 'current_meds' to 'past_meds'.
     * Handles LocalDate start_date when creating placeholder date_ranges string.
     *
     * @param med The Medication object to archive.
     */
    fun archiveMedication(med: Medication) {
        // TODO: Transaction Management - Wrap delete and insert in a transaction for atomicity
        connect()?.use { conn ->
            try {
                conn.autoCommit = false // Start transaction

                // Delete from current_meds
                val deleteSql = "DELETE FROM current_meds WHERE id = ?;"
                conn.prepareStatement(deleteSql).use { pstmt ->
                    pstmt.setInt(1, med.id)
                    pstmt.executeUpdate()
                }

                // Add to past_meds
                val insertSql = """
                    INSERT INTO past_meds (
                        generic_name, brand_name, dosage, dose_form, instructions,
                        reason, prescriber, history_notes, reason_for_stopping, date_ranges, manufacturer
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """.trimIndent()

                // Prepare the INSERT statement
                conn.prepareStatement(insertSql).use { pstmt ->
                    pstmt.setString(1, med.genericName)
                    pstmt.setString(2, med.brandName)
                    pstmt.setString(3, med.dosage)
                    pstmt.setString(4, med.doseForm)
                    pstmt.setString(5, med.instructions)
                    pstmt.setString(6, med.reason)
                    pstmt.setString(7, med.prescriber)
                    // Placeholders for past_meds specific fields - TODO: Capture these from UI if needed
                    pstmt.setString(8, med.notes)   // Using current notes as history notes for now
                    pstmt.setString(9, "Archived by user")  // Placeholder reason for stopping
                    val initialDateRange = DateRange(med.startDate, LocalDate.now())
                    pstmt.setString(10, serializeDateRanges(listOf(initialDateRange)))
                    pstmt.setString(11, med.manufacturer)

                    pstmt.executeUpdate()
                    // System.out.println("Medication archived to history (insert step): ${med.brandName}") // Optional: for debugging
                }

                conn.commit() // Commit transaction
                println("Medication ID ${med.id} archived successfully.")

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
     * Sets the new start_date for the current med.
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
                    // Set new start_date to today's date when unarchiving
                    pstmt.setString(9, LocalDate.now().toString()) // Set new start_date to today as String
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
     * The individual DateRange start/end dates are now LocalDate?.
     *
     * @return A list of PastMedication objects.
     */
    fun getAllPastMedications(): List<PastMedication> {
        val pastMedications = mutableListOf<PastMedication>()
        // val sql = "SELECT id, generic_name, brand_name, dosage, dose_form, instructions, reason, prescriber, history_notes, reason_for_stopping, date_ranges, manufacturer FROM past_meds"
        val sql = "SELECT * FROM past_meds;"

        connect()?.use { conn ->
            try {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        // loop through the result set
                        while (rs.next()) {
                            // TODO: Implement robust parsing of date_ranges string into List<PastMedication.DateRange> using LocalDate.parse()
                            // For now, creating empty list of date ranges
                            // val dateRanges = listOf<PastMedication.DateRange>() // Placeholder for parsed list

                            val id = rs.getInt("id")
                            val genericName = rs.getString("generic_name")
                            val brandName = rs.getString("brand_name")
                            val dosage = rs.getString("dosage")
                            val doseForm = rs.getString("dose_form")
                            val instructions = rs.getString("instructions")
                            val reason = rs.getString("reason")
                            val prescriber = rs.getString("prescriber")
                            val historyNotes = rs.getString("history_notes")
                            val reasonForStopping = rs.getString("reason_for_stopping")
                            val dateRangesString = rs.getString("date_ranges")
                            val manufacturer = rs.getString("manufacturer")

                            val dateRanges = deserializeDateRanges(dateRangesString)

                            pastMedications.add(PastMedication(
                                id, genericName, brandName, dosage, doseForm, instructions,
                                reason, prescriber, historyNotes, reasonForStopping, dateRanges, manufacturer
                            ))
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
     * Converts LocalDate date to String for storage.
     * @param surgery The Surgery object to add.
     */
    fun addSurgery(surgery: Surgery) {
        val sql = "INSERT INTO surgeries(name, date, surgeon) VALUES(?,?,?)"

        connect()?.use { conn ->
            try {
                    conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setString(1, surgery.name)
                    pstmt.setString(2, surgery.date?.toString())
                    pstmt.setString(3, surgery.surgeon)
                    pstmt.executeUpdate()
                    // System.out.println("Surgery added: ${surgery.name}") // Optional: for debugging
                }
            } catch (e: SQLException) {
                System.err.println("Error adding surgery: ${e.message}")
            }
        } ?: System.err.println("Failed to connect to database to add surgery.")
    }

    /**
     * Retrieves all surgeries from the 'surgeries' table.
     * Converts String date from database back to LocalDate.
     * @return A list of Surgery objects.
     */
    fun getAllSurgeries(): List<Surgery> {
        val sql = "SELECT id, name, date, surgeon FROM surgeries"
        val surgeries = mutableListOf<Surgery>()

        connect()?.use { conn ->
            try {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        // loop through the result set
                        while (rs.next()) {
                            val dateString = rs.getString("date")
                            // Convert date string to LocalDate?, handling null and parsing errors
                            val surgeryDate = try {
                                if (dateString != null && dateString.isNotBlank()) {
                                    LocalDate.parse(dateString)
                                } else {
                                    null
                                }
                            } catch (e: DateTimeParseException) {
                                System.err.println("Error parsing surgery date '$dateString': ${e.message}")
                                null // Assign null in the catch block
                            }

                            val surgery = Surgery(
                                rs.getInt("id"),
                                rs.getString("name"),
                                surgeryDate, // <-- PASS the parsed LocalDate? here
                                rs.getString("surgeon")
                            )
                            surgeries.add(surgery)
                        }
                    }
                }
            } catch (e: SQLException) {
                System.err.println("Error retrieving surgeries: ${e.message}")
            }
        } ?: System.err.println("Failed to connect to database to retrieve surgeries.")

        return surgeries
    }

    /**
     * Helper to serialize List<DateRange> to TEXT for database
     * Converts the List<Range> to a simple JSON string format
     */
    public fun serializeDateRanges(dateRanges: List<DateRange>?): String? {
        if (dateRanges == null || dateRanges.isEmpty()) {
            return null
        }
        // Simple JSON array format: [{"startDate":"YYYY-MM-DD", "endDate":"YYYY-MM-DD"}, ...]
        return dateRanges.joinToString(";") { range ->
            "${range.startDate?.toString() ?: ""}_${range.endDate?.toString() ?: ""}"
        }
    }

    /**
     * Helper to deserialize TEXT from database to List<DateRange>
     * Parses the simple JSON string format back to a List<DateRange>
     */
    public fun deserializeDateRanges(dateRangesString: String?): List<DateRange> {
        if (dateRangesString.isNullOrBlank()) {
            return emptyList()
        }
        val ranges = mutableListOf<DateRange>()
        val rangeStrings = dateRangesString.split(";")  // Split by ';' to get individual range strings

        for (rangeStr in rangeStrings) {
            val dates = rangeStr.split("_")
            val startDateStr = dates.getOrNull(0) ?: ""
            val endDateStr = dates.getOrNull(1) ?: ""

            val startDate = try { LocalDate.parse(startDateStr).takeIf { startDateStr.isNotBlank() } } catch (e: DateTimeParseException) { null }
            val endDate = try { LocalDate.parse(endDateStr).takeIf { endDateStr.isNotBlank() } } catch (e: DateTimeParseException) { null }

            ranges.add(DateRange(startDate, endDate))
        }
        return ranges
    }

    /**
     * Updates an existing past medication record in the past_meds table.
     * Matches the record by the PastMedication object's ID.
     * @param pastMedication The PastMedication object with updated details.
     */
    // This is weird... check this...
    fun updatePastMedication(pastMedication: PastMedication) { // <-- ADD THIS NEW FUNCTION
        val sql = """
            UPDATE past_meds SET
                generic_name = ?, brand_name = ?, dosage = ?, dose_form = ?,
                instructions = ?, reason = ?, prescriber = ?, history_notes = ?,
                reason_for_stopping = ?, date_ranges = ?, manufacturer = ?
            WHERE id = ?;
        """.trimIndent()

        connect()?.use { conn ->
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, pastMedication.genericName)
                pstmt.setString(2, pastMedication.brandName?.takeIf { it.isNotEmpty() })
                pstmt.setString(3, pastMedication.dosage?.takeIf { it.isNotEmpty() })
                pstmt.setString(4, pastMedication.doseForm?.takeIf { it.isNotEmpty() })
                pstmt.setString(5, pastMedication.instructions?.takeIf { it.isNotEmpty() })
                pstmt.setString(6, pastMedication.reason?.takeIf { it.isNotEmpty() })
                pstmt.setString(7, pastMedication.prescriber?.takeIf { it.isNotEmpty() })
                pstmt.setString(8, pastMedication.historyNotes?.takeIf { it.isNotEmpty() })
                pstmt.setString(9, pastMedication.reasonForStopping?.takeIf { it.isNotEmpty() })
                // Serialize the List<DateRange> to TEXT for saving
                pstmt.setString(10, serializeDateRanges(pastMedication.dateRanges))
                pstmt.setString(11, pastMedication.manufacturer?.takeIf { it.isNotEmpty() })

                pstmt.setInt(12, pastMedication.id)

                val affectedRows = pstmt.executeUpdate()

                if (affectedRows > 0) {
                    println("Past medication ID ${pastMedication.id} updated successfully in database.")
                } else {
                    println("No past medication found with ID ${pastMedication.id} to update in database.")
                }
            }
        } ?: System.err.println("Failed to update past medication ID ${pastMedication.id}: Could not get database connection.")
    }

    // TODO: Implement delete methods for medications and surgeries
}