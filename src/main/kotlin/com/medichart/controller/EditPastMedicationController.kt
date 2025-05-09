package com.medichart.controller

import com.medichart.database.DatabaseManager
import com.medichart.model.PastMedication
import com.medichart.model.PastMedication.DateRange
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.stage.Stage
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Button
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.control.DatePicker
import javafx.scene.layout.VBox
import javafx.stage.Modality
import java.io.IOException
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
    // @FXML lateinit var dateRangesField: TextField    // For the date range; for now, using text field
    @FXML lateinit var editDateRangesButton: Button
    @FXML lateinit var manufacturerField: TextField

    // Properties to hold the result of the dialog
    var isSavedSuccessful: Boolean = false
        private set // Make setter private

    var pastMedicationData: PastMedication? = null
        private set // Make setter private

    private var originalPastMedication: PastMedication? = null  // Holds the original Medication object being edited, including its ID
    private var dialogStage: Stage? = null  // Keep a reference to the dialog stage
    private var dbManager: DatabaseManager? = null  // Reference to the DatabaseManager (passed from the main controller)

    private var editedDateRanges: List<PastMedication.DateRange>? = null  // Property to hold the list of date ranges being edited
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
        reasonForStoppingField.text = pastMedication.reasonForStopping ?: ""
        editedDateRanges = pastMedication.dateRanges
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
        val updatedManufacturer = manufacturerField.text.trim().takeIf { it.isNotEmpty() }
        // --- End Capture from form fields ---

        // --- Create Updated PastMedication object using the stored List<DateRange> ---
        // Use the editedDateRanges property that was populated in setPastMedicationData
        // or updated by the handleEditDateRanges method.
        val finalDateRanges = editedDateRanges ?: emptyList()   // Use the stored list, default to empty if somehow null

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
            dateRanges = finalDateRanges,
            manufacturer = updatedManufacturer
        )
        // --- End Create Updated Object ---

        isSavedSuccessful = true    // Signal that the save was successful *only after* data is successfully captured/object created
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

    /**
     * Helper function to load an FXML file, create a modal dialog stage, and return the controller and stage.
     * Sets the owner of the dialog to the current dialog stage and modality.
     * This is used by handler methods within THIS controller to open other dialogs.
     * @param fxmlPath The path to the FXML file (e.g., "/com/medichart/gui/EditDateRangeDialog.fxml").
     * @param title The title for the dialog window.
     * @return A Pair of the loaded controller and the dialog Stage, or null if loading fails.
     */
    private fun <T> loadNestedDialog(fxmlPath: String, title: String): Pair<T, Stage>? {
        try {
            val fxmlLoader = FXMLLoader(javaClass.getResource(fxmlPath))
            val dialogRoot = fxmlLoader.load<VBox>()
            val controller = fxmlLoader.getController<T>()

            val dialogStage = Stage()
            dialogStage.title = title
            dialogStage.initModality(Modality.WINDOW_MODAL)
            dialogStage.initOwner(this.dialogStage)
            val dialogScene = Scene(dialogRoot)
            dialogStage.scene = dialogScene

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
     * Opens a dialog for editing the list of Date Ranges.
     */
    @FXML
    private fun handleEditDateRanges() {
        println("Edit Date Ranges button clicked.")
        // Use the helper to load the date range editor dialog FXML and get controller/stage
        // We expect the controller to be of type EditDateRangeController
        val dialogInfo = loadNestedDialog<EditDateRangeController>(
            // Ensure the FXML path matches the file created in Step 13.1
            "/com/medichart/gui/EditDateRangeDialog.fxml",
            "Edit Date Ranges" // Title for the Edit Date Range list dialog
        )

        // Check if the dialog was loaded successfully
        if (dialogInfo != null) {
            val dialogController = dialogInfo.first // Get the controller (EditDateRangeController)
            val dialogStage = dialogInfo.second // Get the stage

            // Call setup and set data BEFORE showing the dialog
            // Ensure EditDateRangeController has public methods setupDialog(Stage, DatabaseManager) and setDateRangesData(List<DateRange>?)
            // The setupDialog method in EditDateRangeController needs the Stage and the DatabaseManager instance.
            // We have the Stage from loadNestedDialog, and dbManager is a property in *this* controller.
            dialogController.setupDialog(dialogStage, dbManager!!) // <-- Call setupDialog and pass Stage and dbManager

            // Pass the current list of date ranges (from the editedDateRanges property) to the list editor controller
            dialogController.setDateRangesData(editedDateRanges) // <-- Pass the list to EditDateRangeController

            // Show the nested dialog and wait for it to be closed by the user
            dialogStage.showAndWait()

            // --- Handle the results after the nested dialog is closed ---
            // Check if the user clicked Save in the nested (EditDateRangeDialog) dialog
            if (dialogController.isSavedSuccessful) {
                // Get the updated list of date ranges from the nested dialog controller
                val updatedDateRanges = dialogController.editedDateRanges

                // Store the updated list in the editedDateRanges property of *this* controller.
                // This list will be used by handleSaveButton when the main Edit Past Medication dialog is saved.
                editedDateRanges = updatedDateRanges

                println("Date Ranges updated in editor: ${dbManager?.serializeDateRanges(editedDateRanges)}")

                // Optional: Update the label next to the button in the main Edit Past Medication dialog
                // to show a summary of the ranges (e.g., "3 ranges" or the serialized string).
                // Requires an @FXML Label next to the button and updating its text here.
                // e.g. dateRangesSummaryLabel.text = "Ranges: ${updatedDateRanges?.size ?: 0}" // Need the label @FXML var
            } else {
                // This runs if the user cancelled or closed the nested dialog.
                // The editedDateRanges property remains unchanged, which is the desired behavior.
                println("Date Range Editor dialog cancelled or failed.")
            }
        } else {
            // This runs if loadNestedDialog returned null (FXML loading failed)
            println("Failed to load the Date Range Editor dialog.")
        }
    }
}