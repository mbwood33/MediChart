package com.medichart.controller

import com.medichart.database.DatabaseManager // <-- Import DatabaseManager
import com.medichart.model.PastMedication // <-- Import PastMedication
import com.medichart.model.PastMedication.DateRange // <-- Import DateRange (nested)
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader // Needed for loading nested dialogs
import javafx.scene.Scene // Needed for dialog stage
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Button
import javafx.scene.control.Label // Needed for summary label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.VBox // Needed for loading nested dialogs root
import javafx.stage.Modality // Needed for dialog modality
import javafx.stage.Stage
import java.io.IOException // Needed for FXMLLoader

/**
 * Controller for the Add Past Medication dialog.
 * Handles inputting details for a new past medication entry.
 */
class AddPastMedicationController {
    @FXML lateinit var genericNameField: TextField
    @FXML lateinit var brandNameField: TextField
    @FXML lateinit var dosageField: TextField
    @FXML lateinit var doseFormField: TextField
    @FXML lateinit var instructionsField: TextField
    @FXML lateinit var reasonField: TextField // Reason Taken
    @FXML lateinit var prescriberField: TextField
    @FXML lateinit var historyNotesArea: TextArea
    @FXML lateinit var reasonForStoppingField: TextField
    // Date Ranges uses a button to open the editor
    @FXML lateinit var dateRangesSummaryLabel: Label
    @FXML lateinit var editDateRangesButton: Button
    @FXML lateinit var manufacturerField: TextField

    // Buttons for Save/Cancel (Add/Cancel in this dialog)
    @FXML lateinit var saveButton: Button
    @FXML lateinit var cancelButton: Button

    private var dialogStage: Stage? = null
    private var dbManager: DatabaseManager? = null
    private var editedDateRanges: List<DateRange>? = emptyList()    // Initialize as empty list for new entry

    var isSavedSuccessful: Boolean = false
        private set
    var pastMedicationData: PastMedication? = null

    /**
     * Called by FXMLLoader after the FXML is loaded.
     * Initializes the dialog elements.
     */
    @FXML
    fun initialize() {
        updateDateRangesSummaryLabel()
    }

    /**
     * Public method called by the parent controller (MediChartController)
     * to set the Stage and DatabaseManager, and potentially add key event listeners.
     * @param stage The Stage that hosts this dialog.
     * @param dbManager The DatabaseManager instance.
     */
    fun setupDialog(stage: Stage, dbManager: DatabaseManager) {
        this.dialogStage = stage
        this.dbManager = dbManager

        dialogStage?.sceneProperty()?.addListener { _, _, newScene ->
            if (newScene != null) {
                newScene.setOnKeyPressed { event ->
                    when (event.code) {
                        // KeyCode.ENTER -> handleSaveButton()
                        KeyCode.ESCAPE -> handleCancelButton()
                        else -> {
                            // Do nothing
                        }
                    }
                }
            }
        }

        // Handle the widnow's close button
        dialogStage?.setOnCloseRequest { event ->
            isSavedSuccessful = false
            pastMedicationData = null
        }
    }

    /**
     * Helper function to load a nested dialog (EditDateRangeDialog).
     * @param fxmlPath The path to the FXML file.
     * @param title The title for the dialog window.
     * @return A Pair of the loaded controller and the dialog Stage, or null if loading fails.
     */
    private fun loadNestedDialog(fxmlPath: String, title: String): Pair<EditDateRangeController, Stage>? {
        try {
            val fxmlLoader = FXMLLoader(javaClass.getResource(fxmlPath))
            val dialogRoot = fxmlLoader.load<VBox>() // Assuming VBox is the root

            val controller = fxmlLoader.getController<EditDateRangeController>() // Explicitly get the controller

            val dialogStage = Stage()
            dialogStage.title = title
            dialogStage.initModality(Modality.WINDOW_MODAL)
            dialogStage.initOwner(this.dialogStage) // Set owner to THIS dialog stage
            val dialogScene = Scene(dialogRoot)
            dialogStage.scene = dialogScene

            // Call the nested controller's setup method
            // EditDateRangeController needs Stage and dbManager
            controller.setupDialog(dialogStage, dbManager!!) // Pass Stage and dbManager

            // We don't showAndWait() here; the calling handler will do that.

            return Pair(controller, dialogStage)

        } catch (e: IOException) {
            System.err.println("Error loading nested dialog FXML: $fxmlPath - ${e.message}")
            e.printStackTrace()
            showAlert(AlertType.ERROR, "Error Loading Dialog", "Could not load the editor dialog.", "An error occurred while opening the editor.")
            return null
        }
    }

    /**
     * Handles the action when the "Edit Date Ranges..." button is clicked.
     * Opens the dialog for editing the list of Date Ranges.
     */
    @FXML
    private fun handleEditDateRanges() {
        println("Edit Date Ranges button clicked in Add Past Medication dialog.")
        val dialogInfo = loadNestedDialog("/com/medichart/gui/EditDateRangeDialog.fxml", "Edit Date Ranges")

        if (dialogInfo != null) {
            val dialogController = dialogInfo.first
            val dialogStage = dialogInfo.second

            dialogController.setDateRangesData(editedDateRanges)    // Pass the list from *this* controller's property
            dialogStage.showAndWait()

            if (dialogController.isSavedSuccessful) {
                val updatedDateRanges = dialogController.editedDateRanges
                editedDateRanges = updatedDateRanges    // Store the updated list in the editedDateRanges property of *this* controller
                println("Date Ranges updated in editor: ${dbManager?.serializeDateRanges(editedDateRanges)}")
                updateDateRangesSummaryLabel()
            } else {
                println("Date Range Editor dialog cancelled or failed.")
            }
        }
    }

    /**
     * Helper to update the label showing the date ranges summary.
     */
    private fun updateDateRangesSummaryLabel() {
        val count = editedDateRanges?.size ?: 0
        dateRangesSummaryLabel.text = dbManager?.serializeDateRanges(editedDateRanges) ?: "[Click button to edit]"
    }

    /**
     * Handles the action when the "Add" (Save) button is clicked.
     * Captures input, creates a PastMedication object, and signals success.
     */
    @FXML
    private fun handleSaveButton() {
        println("Add (Save) button clicked in Add Past Medication dialog.")
        val genericName = genericNameField.text.trim()

        if (genericName.isBlank()) {
            showAlert(
                AlertType.WARNING,
                "Validation Error",
                "Missing Required Field",
                "Please enter a Generic Name."
            )
            return
        }

        val newGenericName = genericNameField.text.trim()
        val newBrandName = brandNameField.text.trim().takeIf { it.isNotEmpty() }
        val newDosage = dosageField.text.trim().takeIf { it.isNotEmpty() }
        val newDoseForm = doseFormField.text.trim().takeIf { it.isNotEmpty() }
        val newInstructions = instructionsField.text.trim().takeIf { it.isNotEmpty() }
        val newReason = reasonField.text.trim().takeIf { it.isNotEmpty() }
        val newPrescriber = prescriberField.text.trim().takeIf { it.isNotEmpty() }
        val newHistoryNotes = historyNotesArea.text.trim().takeIf { it.isNotEmpty() } // Use .text for TextArea
        val newReasonForStopping = reasonForStoppingField.text.trim().takeIf { it.isNotEmpty() }
        val newManufacturer = manufacturerField.text.trim().takeIf { it.isNotEmpty() }
        val finalDateRanges = editedDateRanges ?: emptyList()

        pastMedicationData = PastMedication(
            id = 0,
            genericName = newGenericName,
            brandName = newBrandName,
            dosage = newDosage,
            doseForm = newDoseForm,
            instructions = newInstructions,
            reason = newReason,
            prescriber = newPrescriber,
            historyNotes = newHistoryNotes,
            reasonForStopping = newReasonForStopping,
            dateRanges = finalDateRanges,
            manufacturer = newManufacturer
        )

        isSavedSuccessful = true
        closeDialog()
    }

    /**
     * Handles the action when the "Cancel" button is clicked.
     */
    @FXML
    private fun handleCancelButton() {
        println("Cancel button clicked in Add Past Medication dialog.")
        isSavedSuccessful = false
        pastMedicationData = null
        closeDialog()
    }

    // --- Helper Methods ---
    /**
     * Gets the Stage (window) this controller belongs to and closes it.
     */
    private fun closeDialog() {
        dialogStage?.close()
    }

    /**
     * Helper function to show a JavaFX alert dialog.
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