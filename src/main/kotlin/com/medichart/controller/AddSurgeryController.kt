package com.medichart.controller

import com.medichart.model.Surgery // Import the Surgery data class
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label // Although Label not directly accessed in code, good practice if referenced
import javafx.scene.control.TextField
import javafx.scene.control.DatePicker // Import DatePicker
import javafx.stage.Stage // Import Stage
import javafx.scene.control.Alert // Import Alert
import javafx.scene.control.Alert.AlertType // Import AlertType
import javafx.scene.control.ButtonType // Import ButtonType for dialog results (optional, but helpful)
import java.time.LocalDate // Import LocalDate

/**
 * Controller for the Add Surgery dialog.
 * Handles user input for a new surgery record and provides the resulting Surgery object back to the caller.
 */
class AddSurgeryController {
    @FXML lateinit var nameField: TextField
    @FXML lateinit var datePicker: DatePicker
    @FXML lateinit var surgeonField: TextField

    private var dialogStage: Stage? = null

    var isSavedSuccessful: Boolean = false
        private set

    var surgeryData: Surgery? = null
        private set

    /**
     * Called by FXMLLoader after the FXML is loaded
     */
    @FXML
    fun initialize() {
        datePicker.value = LocalDate.now()
    }

    /**
     * Public method called by the main controller (MediChartController) to set the Stage for this dialog.
     * This is needed so the controller can close itself.
     * @param stage The Stage that hosts this dialog.
     */
    fun setDialogStage(stage: Stage) {
        this.dialogStage = stage

        dialogStage?.setOnCloseRequest { event ->
            println("Add Surgery Dialog closed via window button (treated as Cancel).")
        }
    }

    /**
     * Handles the action when the "Save" button is clicked in THIS dialog.
     * Captures input from fields, creates Surgery object, sets result properties, closes dialog.
     */
    @FXML
    private fun handleSaveButton() {
        println("Save button clicked in Add Surgery Dialog.")

        val name = nameField.text?.trim()
        val date = datePicker.value // Can be null if not selected
        val surgeon = surgeonField.text?.trim()?.takeIf { it.isNotEmpty() }

        if (name.isNullOrEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Missing Required Field", "Please enter the surgery Name.")
            return
        }

        surgeryData = Surgery(
            name = name,
            date = date,
            surgeon = surgeon
        )

        isSavedSuccessful = true
        closeDialog()
    }

    /**
     * Handles the action when the "Cancel" button is clicked in THIS dialog.
     * Sets result properties to indicate cancel, closes dialog.
     */
    @FXML
    private fun handleCancelButton() {
        println("Cancel button clicked in Add Surgery Dialog.")
        isSavedSuccessful = false // Signal that the save was not successful
        surgeryData = null        // Ensure the result is null on cancel
        closeDialog()           // Close this dialog stage
    }

    // --- Helper Methods ---
    /**
     * Gets the Stage (window) this controller belongs to and closes it.
     * Uses the dialogStage property set by the caller.
     */
    private fun closeDialog() {
        dialogStage?.close() // Use safe call in case dialogStage was never set
    }

    /**
     * Helper function to show a JavaFX alert dialog.
     * Note: This helper is for simple alerts within the dialog itself (like validation errors),
     * not for confirmation dialogs initiated by the main controller.
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
        // Set the owner of the alert to this dialog stage to keep it modal within THIS dialog
        alert.initOwner(dialogStage) // Init owner for modality
        alert.showAndWait() // Show the alert and wait for user interaction
    }
}