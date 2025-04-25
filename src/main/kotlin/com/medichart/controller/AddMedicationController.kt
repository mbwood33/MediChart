package com.medichart.controller

import com.medichart.model.Medication
import javafx.fxml.FXML
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.stage.Stage
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType

/**
 * Controller for the Add/Edit Medication dialog.
 * Handles user input and provides the resulting data back to the caller.
 */
class AddMedicationController {
    // FXML elements for input fields injected by FXMLLoader
    @FXML lateinit var genericNameField: TextField
    @FXML lateinit var brandNameField: TextField
    @FXML lateinit var dosageField: TextField
    @FXML lateinit var doseFormField: TextField
    @FXML lateinit var instructionsArea: TextArea
    @FXML lateinit var reasonField: TextField
    @FXML lateinit var prescriberField: TextField
    @FXML lateinit var notesArea: TextArea
    @FXML lateinit var startDateField: TextField    // For the start date
    @FXML lateinit var manufacturerField: TextField

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
        // --- Input Validation ---
        val genericName = genericNameField.text.trim()

        if (genericName.isBlank()) {    // Check if generic name is empty or contains only whitespace
            // Show a warning message to the user
            showAlert(
                AlertType.WARNING,
                "Validation Error",
                "Missing Required Field",
                "Please enter a Generic Name for the medication."
            )
            return  // Stop the save process if validation fails
        }
        // --- End Validation ---

        // Collect data from input fields
        // !-- genericName already collected above --!
        val brandName = brandNameField.text.trim().takeIf { it.isNotEmpty() }   // Use takeIf for optional fields
        val dosage = dosageField.text.trim().takeIf { it.isNotEmpty() }
        val doseForm = doseFormField.text.trim().takeIf {it.isNotEmpty() }
        val instructions = instructionsArea.text.trim().takeIf { it.isNotEmpty() }
        val reason = reasonField.text.trim().takeIf { it.isNotEmpty() }
        val prescriber = prescriberField.text.trim().takeIf { it.isNotEmpty() }
        val notes = notesArea.text.trim().takeIf { it.isNotEmpty() }
        val startDate = startDateField.text.trim().takeIf { it.isNotEmpty() }
        val manufacturer = manufacturerField.text.trim().takeIf { it.isNotEmpty() }

        // Create a Medication object from the input
        // Use 0 for ID as the database will assign the real ID on insertion
        medicationData = Medication(
            id = 0,
            genericName = genericName,
            brandName = brandName,
            dosage = dosage,
            doseForm = doseForm,
            instructions = instructions,
            reason = reason,
            prescriber = prescriber,
            notes = notes,
            startDate = startDate,
            manufacturer = manufacturer
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

    /**
     * Helper function to show a JavaFX alert dialog.
     *
     * @param alertType The type of alert (e.g,. WARNING, ERROR, INFORMATION).
     * @param title The title of the alert window.
     * @param header The header text of the alert (can be null)
     * @param content The main content text of the alert
     */
    private fun showAlert(alertType: AlertType, title: String, header: String?, content: String) {
        val alert = Alert(alertType)
        alert.title = title
        alert.headerText = header
        alert.contentText = content
        alert.showAndWait() // Show the alert and wait for the user to close it
    }

    // TODO: Add a public method to set data for editing existing medications (takes a Medication object)
}