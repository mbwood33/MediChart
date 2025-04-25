package com.medichart.controller

import com.medichart.model.Medication
import javafx.fxml.FXML
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.stage.Stage

/**
 * Controller for the Add/Edit Medication dialog.
 * Handles user input and provides the resulting data back to the caller.
 */
class AddMedicationController {
    // FXML elements for input fields injected by FXMLLoader
    @FXML lateinit var genericNameField: TextField
    @FXML lateinit var brandNameField: TextField
    @FXML lateinit var dosageField: TextField
    @FXML lateinit var instructionsArea: TextArea
    @FXML lateinit var reasonField: TextField
    @FXML lateinit var prescriberField: TextField
    @FXML lateinit var notesArea: TextArea
    @FXML lateinit var startDateField: TextField    // For the start date

    // Properties to hold the result of the dialog
    var isSavedSuccessful: Boolean = false
        private set // Make setter private

    var medicationData: Medication? = null
        private set // Make setter private

    /**
     * Called by FXMLLoader after the FXML is loaded and elements are injected.
     * Can be used for initial setup if needed (e.g, pre-filling fields for editing).
     */
    @FXML
    fun initialize() {
        // Initial setup if needed. For adding, fields start empty.
        // For editing, this is where you would pre-fill fields based on the Medication object being edited.
    }

    /**
     * Handles the action when the Save button is clicked.
     * Collects input, creates a Medication object, and signals success.
     */
    @FXML
    private fun handleSaveButton() {
        // TODO: Add input validation here (e.g., check if generic name is not empty)
        // If validation fails, show an error message and return

        // Collect data from input fields
        val genericName = genericNameField.text.trim()  // Use trim() to remove leading/trailing whitespace
        val brandName = brandNameField.text.trim().takeIf { it.isNotEmpty() }   // Use takeIf for optional fields
        val dosage = dosageField.text.trim().takeIf { it.isNotEmpty() }
        val instructions = instructionsArea.text.trim().takeIf { it.isNotEmpty() }
        val reason = reasonField.text.trim().takeIf { it.isNotEmpty() }
        val prescriber = prescriberField.text.trim().takeIf { it.isNotEmpty() }
        val notes = notesArea.text.trim().takeIf { it.isNotEmpty() }
        val startDate = startDateField.text.trim().takeIf { it.isNotEmpty() }

        // Create a Medication object from the input
        // Use 0 for ID as the database will assign the real ID on insertion
        medicationData = Medication(
            id = 0,
            genericName = genericName,
            brandName = brandName,
            dosage = dosage,
            instructions = instructions,
            reason = reason,
            prescriber = prescriber,
            notes = notes,
            startDate = startDate
        )

        // Signal that the save was successful
        isSavedSuccessful = true

        // Close the dialog window
        closeDialog()
    }

    /**
     * Handles the action when the Cancel button is clicked.
     * Simply closes the dialog without saving.
     */
    @FXML
    private fun handleCancelButton() {
        // Signal that the save was cancelled
        isSavedSuccessful = false   // Already false by default, but good to be explicit

        // Close the dialog window
        closeDialog()
    }

    /**
     * Gets the Stage (window) this controller belongs to and closes it.
     * Assumes the root element of the FXML is part of a Stage.
     */
    private fun closeDialog() {
        // Get the stage from any of the FXML elements
        val stage = genericNameField.scene.window as Stage  // Cast to Stage
        stage.close()   // Close the window
    }

    // TODO: Add a public method to set data for editing existing medications (takes a Medication object)
}