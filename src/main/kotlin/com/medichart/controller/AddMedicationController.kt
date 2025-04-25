package com.medichart.controller

import com.medichart.model.Medication
import javafx.fxml.FXML
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.stage.Stage
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.control.DatePicker
import java.time.LocalDate

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
    @FXML lateinit var startDatePicker: DatePicker    // For the start date
    @FXML lateinit var manufacturerField: TextField

    // Properties to hold the result of the dialog
    var isSavedSuccessful: Boolean = false
        private set // Make setter private

    var medicationData: Medication? = null
        private set // Make setter private

    // Keep a reference to the dialog stage
    private var dialogStage: Stage? = null

    /**
     * Called by FXMLLoader after the FXML is loaded and elements are injected.
     * Can be used for initial setup if needed (e.g, pre-filling fields for editing).
     */
    @FXML
    fun initialize() {
        // Initial setup if needed. For adding, fields start empty.
        // For adding, this is where you would pre-fill fields based on the Medication object being edited,
        // including setting the value of the DatePicker: startDatePicker.value = medication.startDate
    }

    /**
     * This method is called by the main controller after loading the FXML
     * to provide a reference to the dialog's stage and set up key event listeners.
     *
     * @param stage The Stage that hosts this dialog.
     */
    fun setDialogStage(stage: Stage) {
        dialogStage = stage

        // Add event filter to the scene for key presses
        dialogStage?.scene?.addEventFilter(KeyEvent.KEY_PRESSED) { event ->
            when (event.code) {
                KeyCode.ENTER -> {
                    // Check if the focus is *not* on a TextArea before triggering save with Enter
                    // This allows multiline input in TextAreas
                    if (dialogStage?.scene?.focusOwner !is TextArea) {
                        handleSaveButton()  // Call the save handler
                        event.consume() // Consume the event so it doesn't do anything else (like add a newline)
                    }
                }
                KeyCode.ESCAPE -> {
                    handleCancelButton()    // Call the cancel handler
                    event.consume() // Consume the event
                }
                else -> {
                    // Do nothing for other keys
                }
            }
        }
        // Standard tabbing between controls should work automatically once controls are added to scene.
        // If tabbing isn't working, it might indicate a more complex focus issue or OS interaction.
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
        val startDate = startDatePicker.value
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
        dialogStage?.close()   // Close the window
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