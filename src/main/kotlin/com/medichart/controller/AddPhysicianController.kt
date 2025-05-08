// src/main/kotlin/com/medichart/controller/AddPhysicianController.kt
package com.medichart.controller

import com.medichart.model.Physician
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.scene.control.TextArea
import javafx.stage.Stage
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

/**
 * Controller for the Add Physician dialog
 * Handles user input for a new physician and provides the resulting Physician object back to the caller.
 */
class AddPhysicianController {
    @FXML lateinit var nameField: TextField
    @FXML lateinit var specialtyField: TextField
    @FXML lateinit var phoneField: TextField
    @FXML lateinit var faxField: TextField
    @FXML lateinit var emailField: TextField
    @FXML lateinit var addressArea: TextArea
    @FXML lateinit var notesArea: TextArea

    private var dialogStage: Stage? = null

    var isSavedSuccessful: Boolean = false
        private set

    var physicianData: Physician? = null
        private set

    /**
     * Called by FXMLLoader after the FXML is loaded
     * Can be used for initial setup if needed, though less common for simple "Add" dialogs
     */
    @FXML
    fun initialize() {
        // Optional: Add listeners to fields (e.g., format phone number), or initial setup here if needed
    }

    /**
     * Public method called by the main controller (MediChartController) to set the Stage for this dialog
     * Needed so the controller can close itself
     * @param stage The Stage that hosts this dialog
     */
    fun setDialogStage(stage: Stage) {
        this.dialogStage = stage

        dialogStage?.setOnCloseRequest { event ->
            println("Add Physician Dialog closed via window button (treated as Cancel)")
            // No need to call handleCancelButton() here explicitly unless it does more than set flags and close.
        }
    }

    /**
     * Handles the action when the "Save" button is clicked in THIS dialog
     * Captures input from fields, performs validation, creates Physician object,
     * sets result properties, and closes the dialog
     */
    @FXML
    private fun handleSaveButton() {
        println("Save button clicked in Add Physician Dialog.")

        val name = nameField.text?.trim()
        if (name.isNullOrEmpty()) {
            showAlert(
                AlertType.WARNING,
                "Validation Error",
                "Missing Required Field",
                "Please enter the physician's Name."
            )
            return
        }

        val specialty = specialtyField.text?.trim()?.takeIf { it.isNotEmpty() }
        val phone = phoneField.text?.trim()?.takeIf { it.isNotEmpty() }
        val fax = faxField.text?.trim()?.takeIf { it.isNotEmpty() }
        val email = emailField.text?.trim()?.takeIf { it.isNotEmpty() }
        val address = addressArea.text?.trim()?.takeIf { it.isNotEmpty() }
        val notes = notesArea.text?.trim()?.takeIf { it.isNotEmpty() }

        physicianData = Physician(
            name = name,
            specialty = specialty,
            phone = phone,
            fax = fax,
            email = email,
            address = address,
            notes = notes
        )

        isSavedSuccessful = true
        closeDialog()
    }

    /**
     * Handles the action when the "Cancel" button is clicked in THIS dialog.
     * Sets result properties to indicate cancel, closes dialog.
     * Defaults already set isSavedSuccessful to false and physicianData to null.
     */
    @FXML
    private fun handleCancelButton() {
        println("Cancel button clicked in Add Physician Dialog.")
        // isSavedSuccessful = false    // Default value is false
        // physicianData = null     // Default value is null
        closeDialog()
    }

    // --- Helper Methods ---

    /**
     * Gets the Stage (window) this controller belongs to and closes it
     * Uses the dialogStage property set by the caller
     */
    private fun closeDialog() {
        dialogStage?.close()
    }

    /**
     * Helper function to show a JavaFX alert dialog
     * @param alertType Type of alert (e.g., INFORMATION, WARNING, ERROR).
     * @param title The title of the alert window.
     * @param header Optional header text.
     * @param content The main content text.
     */
    private fun showAlert(alertType: AlertType, title: String, header: String?, content: String) {
        val alert = Alert(alertType)
        alert.title = title
        alert.headerText = header
        alert.contentText = content
        alert.initOwner(dialogStage)
        alert.showAndWait()
    }
}

