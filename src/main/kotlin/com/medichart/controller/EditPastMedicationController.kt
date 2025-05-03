package com.medichart.controller

import com.medichart.database.DatabaseManager
import com.medichart.model.PastMedication
import com.medichart.model.PastMedication.DateRange
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
 * Based on the AddMedicationController, adapted for editing.
 */
class EditPastMedicationController {
    // FXML elements for input fields injected by FXMLLoader
    @FXML lateinit var genericNameField: TextField
    @FXML lateinit var brandNameField: TextField
    @FXML lateinit var dosageField: TextField
    @FXML lateinit var doseFormField: TextField
    @FXML lateinit var instructionsField: TextField
    @FXML lateinit var reasonField: TextField
    @FXML lateinit var prescriberField: TextField
    @FXML lateinit var historyNotesArea: TextArea
    @FXML lateinit var reasonForStoppingField: TextField
    @FXML lateinit var dateRangesField: TextField    // For the date range; for now, using text field
    @FXML lateinit var manufacturerField: TextField

    // Properties to hold the result of the dialog
    var isSavedSuccessful: Boolean = false
        private set // Make setter private

    var pastMedicationData: PastMedication? = null
        private set // Make setter private

    private var originalPastMedication: PastMedication? = null  // Holds the original Medication object being edited, including its ID

    // Keep a reference to the dialog stage
    private var dialogStage: Stage? = null

    // Reference to the DatabaseManager (passed from the main controller)
    private var dbManager: DatabaseManager? = null

    /**
     * Called by FXMLLoader after the FXML is loaded.
     * Initializes the dialog elements.
     * For editing, this method can be used for initial setup or validation binding.
     */
    @FXML
    fun initialize() {
        // Initial setup if needed. For adding, fields start empty.
        // For adding, fields start empty. For editing, fields are populated by setMedicationData().
        // Any setup that depends on @FXML injected fields (like DatePicker converters or listeners) can go here.
        // You might want to add DatePicker converter setup here later if needed.
    }

    /**
     * Public method called by the main controller to set the Stage and add key event listeners.
     * Also adds a handler for the window's close button.
     * @param stage The Stage that hosts this dialog.
     */
    fun setDialog(stage: Stage, dbManager: DatabaseManager) {
        this.dialogStage = stage
        this.dbManager = dbManager

        // Add event filter to the scene for key presses (Handles ENTER and ESCAPE)
        dialogStage?.sceneProperty()?.addListener { _, _, newScene ->   // Use addListener on sceneProperty
            newScene?.setOnKeyPressed { event ->
                when (event.code) {
                    KeyCode.ENTER -> {
                        val focusOwner = newScene.focusOwner    // Get focus owner from new scene
                        if (focusOwner != null && focusOwner !is TextArea) {
                            handleSaveButton()  // Call the save handler
                            event.consume() // Consume the event
                        }
                    }
                    KeyCode.ESCAPE -> {
                        handleCancelButton()    // Call the cancel handler
                        event.consume() // Consume the event
                    }
                    else -> {
                        // Do nothing
                    }
                }
            }
        }

        // Handle the window's close button (sets isSavedSuccessful to false if closed this way)
        dialogStage?.setOnCloseRequest { event ->
            isSavedSuccessful = false   // Ensure this is false if the user clones the window via the title bar button
            // By default, the window closes. We don't need to consume the event unless we want to prevent closing under certain conditions.
        }
    }

    /**
     * Public method to receive the Medication data to be edited.
     * Called by the main controller before showing the dialog.
     * IMPLEMENTED: Logic to populate the form fields with data from the provided Medication object.
     */
    fun setPastMedicationData(pastMedication: PastMedication) {
        originalPastMedication = pastMedication // Store the original data so we have the ID later for updating

        // --- Populate form fields with data from the Medication object ---
        genericNameField.text = pastMedication.genericName
        brandNameField.text = pastMedication.brandName ?: ""    // Handle potential nulls by setting empty string
        dosageField.text = pastMedication.dosage ?: ""
        doseFormField.text = pastMedication.doseForm ?: ""
        instructionsField.text = pastMedication.instructions ?: ""
        reasonField.text = pastMedication.reason ?: ""
        prescriberField.text = pastMedication.prescriber ?: ""
        historyNotesArea.text = pastMedication.historyNotes ?: ""
        // Serialize the List<DateRange> to a string using the helper from dbManager
        dateRangesField.text = dbManager?.serializeDateRanges(pastMedication.dateRanges) ?: ""
        manufacturerField.text = pastMedication.manufacturer ?: ""
        // --- End Populate form fields ---
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

        // --- Implement capturing from form fields ---
        val updatedGenericName = genericNameField.text.trim()   // Required field
        val updatedBrandName = brandNameField.text.trim().takeIf { it.isNotEmpty() }    // Optional field (use takeIf)
        val updatedDosage = dosageField.text.trim().takeIf { it.isNotEmpty() }
        val updatedDoseForm = doseFormField.text.trim().takeIf { it.isNotEmpty() }
        val updatedInstructions = instructionsField.text.trim().takeIf { it.isNotEmpty() }
        val updatedReason = reasonField.text.trim().takeIf { it.isNotEmpty() }
        val updatedPrescriber = prescriberField.text.trim().takeIf { it.isNotEmpty() }
        val updatedHistoryNotes = historyNotesArea.text.trim().takeIf { it.isNotEmpty() } // Use .text for TextArea
        val updatedReasonForStopping = reasonForStoppingField.text.trim().takeIf { it.isNotEmpty() }
        val dateRangesString = dateRangesField.text.trim()
        val updatedManufacturer = manufacturerField.text.trim().takeIf { it.isNotEmpty() }
        // --- End Capture from form fields ---

        // --- Deserialize Date Ranges and Create Updated PastMedication object ---
        val updatedDateRanges = dbManager?.deserializeDateRanges(dateRangesString) ?: emptyList()

        // --- Create an UPDATED Medication object using the originalMedication.id and copied/updated fields ---
        // Ensure originalMedication is not null before accessing its ID
        pastMedicationData = originalPastMedication?.copy(
            id = originalPastMedication?.id ?: 0,  // Use the original ID, default to 0 if somehow null (shouldn't happen)
            genericName = updatedGenericName,
            brandName = updatedBrandName,
            dosage = updatedDosage,
            doseForm = updatedDoseForm,
            instructions = updatedInstructions,
            reason = updatedReason,
            prescriber = updatedPrescriber,
            historyNotes = updatedHistoryNotes,
            reasonForStopping = updatedReasonForStopping,
            dateRanges = updatedDateRanges,
            manufacturer = updatedManufacturer
        )
        // --- End Create Updated Object ---

        // Signal that the save was successful *only after* data is successfully captured/object created
        isSavedSuccessful = true

        closeDialog()   // Close the dialog window
    }

    /**
     * Handles the action when the Cancel button is clicked.
     * Simply closes the dialog without saving.
     */
    @FXML
    private fun handleCancelButton() {
        isSavedSuccessful = false   // Already false by default, but good to be explicit
        closeDialog()   // Close the dialog window
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
}