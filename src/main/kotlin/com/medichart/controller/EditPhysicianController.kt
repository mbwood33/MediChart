package com.medichart.controller

import com.lowagie.text.SpecialSymbol
import com.medichart.model.Physician
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TextArea
import javafx.stage.Stage
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import org.w3c.dom.Text

/**
 * Controller for the Edit Physician dialog
 * Handles displaying existing physician data, capturing updates, and providing the updated Physician object back to the caller
 */
class EditPhysicianController {
    @FXML lateinit var nameField: TextField
    @FXML lateinit var specialtyField: TextField
    @FXML lateinit var phoneField: TextField
    @FXML lateinit var faxField: TextField
    @FXML lateinit var emailField: TextField
    @FXML lateinit var addressArea: TextArea
    @FXML lateinit var notesArea: TextArea

    private var dialogStage: Stage? = null
    private var physicianToEdit: Physician? = null

    var isSavedSuccessful: Boolean = false
        private set
    var updatedPhysicianData: Physician? = null
        private set

    /**
     * Called by FXMLLoader after the FXML is loaded
     * Can be used for initial setup if needed
     */
    @FXML
    fun initialize() {
        // Optional: Add listeners or initial setup here if needed
    }

    /**
     * Public method called by the main controller (MediChartController) to set the Stage for this dialog
     * Needed so the controller can close itself
     * @param Stage The Stage that hosts this dialog
     */
    fun setDialogStage(stage: Stage) {
        this.dialogStage = stage

        // Optional: Handle the window's close button (the X in the title bar) to behave like Cancel
        dialogStage?.setOnCloseRequest { event ->
            isSavedSuccessful = false
            updatedPhysicianData = null
            println("Edit Physician Dialog closed via window button (treated as Cancel).")
        }
    }

    /**
     * Public method called by the main controller to set the Physician data to be edited
     * This method populates the input fields in the dialog with the existing data
     * @param physician The Physician object containing the data to display for editing
     */
    fun setPhysicianData(physician: Physician) {
        this.physicianToEdit = physician

        nameField.text = physician.name
        specialtyField.text = physician.specialty
        phoneField.text = physician.phone
        faxField.text = physician.fax
        emailField.text = physician.email
        addressArea.text = physician.address
        notesArea.text = physician.notes
    }

    /**
     * Handles the action when the "Save" button is clicked in THIS dialog
     * Captures updated input from fields, creates an updated Physician object (preserving the original ID),
     * sets result properties, and closes dialog.
     */
    @FXML
    private fun handleSaveButton() {
        println("Save button clicked in Edit Physician Dialog.")

        val name = nameField.text?.trim()
        if (name.isNullOrEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Missing Required Field", "Please enter the physician's Name.")
            return
        }

        val specialty = specialtyField.text?.trim()?.takeIf { it.isNotEmpty() }
        val phone = phoneField.text?.trim()?.takeIf { it.isNotEmpty() }
        val fax = faxField.text?.trim()?.takeIf { it.isNotEmpty() }
        val email = emailField.text?.trim()?.takeIf { it.isNotEmpty() }
        val address = addressArea.text?.trim()?.takeIf { it.isNotEmpty() }
        val notes = notesArea.text?.trim()?.takeIf { it.isNotEmpty() }

        updatedPhysicianData = Physician(
            id = physicianToEdit?.id ?: 0,
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
     * Handles the action when the "Cancel" button is clicked in THIS dialog
     * Sets result properties to indicate cancel, closes dialog
     */
    @FXML
    private fun handleCancelButton() {
        println("Cancel button clicked in Edit Physician Dialog.")
        isSavedSuccessful = false
        updatedPhysicianData = null
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
     * Helper function to show a JavaFX alert dialog.
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
        // Set the owner of the alert to this dialog stage to keep it modal within the edit physician dialog
        alert.initOwner(dialogStage) // Init owner for modality
        alert.showAndWait() // Show the alert and wait for user interaction
    }
}