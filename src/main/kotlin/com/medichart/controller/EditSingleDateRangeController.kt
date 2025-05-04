package com.medichart.controller

import com.medichart.model.PastMedication.DateRange // <-- Import the nested DateRange class
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.DatePicker // <-- Import DatePicker
import javafx.scene.control.Alert // <-- Import Alert
import javafx.scene.control.Alert.AlertType // <-- Import AlertType
import javafx.stage.Stage // <-- Import Stage
import javafx.scene.Scene // Needed for dialog stage and key events
import javafx.scene.input.KeyCode // For key event handling
import javafx.scene.input.KeyEvent // For key event handling
import java.time.LocalDate // <-- Import LocalDate (for DatePicker values)


/**
 * Controller for the dialog to add or edit a single DateRange.
 * Handles setting and getting the start and end dates.
 */
class EditSingleDateRangeController {

    // FXML elements for DatePickers and buttons
    @FXML lateinit var startDatePicker: DatePicker
    @FXML lateinit var endDatePicker: DatePicker
    @FXML lateinit var saveButton: Button // Assuming you added fx:id="saveButton" in FXML
    @FXML lateinit var cancelButton: Button // Assuming you added fx:id="cancelButton" in FXML

    // Reference to the dialog stage
    private var dialogStage: Stage? = null

    // Properties to hold the result of THIS dialog (whether saved and the edited DateRange)
    var isSavedSuccessful: Boolean = false
        private set

    var editedDateRange: DateRange? = null // Holds the resulting DateRange object (or null if cancelled)


    /**
     * Called by FXMLLoader after the FXML is loaded.
     * Initializes the dialog elements.
     */
    @FXML
    fun initialize() {
        // Optional: Add listeners to DatePickers if needed (e.g., validate end date after start date)
    }

    /**
     * Public method called by the parent controller (EditDateRangeController) to set the Stage
     * and potentially add key event listeners.
     * @param stage The Stage that hosts this dialog.
     */
    fun setDialogStage(stage: Stage) {
        this.dialogStage = stage

        // Add event filter to the scene for key presses (Enter/Escape)
        dialogStage?.sceneProperty()?.addListener { _, _, newScene ->
            if (newScene != null) {
                newScene.setOnKeyPressed { event ->
                    when (event.code) {
                        KeyCode.ENTER -> handleSaveButton() // Pressing Enter saves
                        KeyCode.ESCAPE -> handleCancelButton() // Pressing Escape cancels
                        else -> {
                            // Do nothing
                        }
                    }
                }
            }
        }
        // Handle the window's close button
        dialogStage?.setOnCloseRequest { event ->
            isSavedSuccessful = false // Ensure this is false if closed without clicking Save
            editedDateRange = null // Ensure result is null if dialog closed via window button
        }
    }

    /**
     * Sets the data for the dialog. If a DateRange is provided (for editing),
     * populates the DatePickers. If dateRange is null, it's an "Add" operation.
     * @param dateRange The DateRange object to edit, or null for adding a new range.
     */
    fun setDateRangeData(dateRange: DateRange?) {
        // Populate the DatePickers with the provided data (can be null)
        startDatePicker.value = dateRange?.startDate
        endDatePicker.value = dateRange?.endDate
    }


    /**
     * Handles the action when the "Save" button is clicked in THIS dialog.
     * Captures data from the DatePickers, creates a DateRange object.
     */
    @FXML
    private fun handleSaveButton() {
        println("Save button clicked in Single Date Range Editor.")
        // Capture data from DatePickers
        val startDate = startDatePicker.value // This is LocalDate or null
        val endDate = endDatePicker.value   // This is LocalDate or null

        // TODO: Optional validation (e.g., show warning if end date is before start date when both are present)

        // Create the new or updated DateRange object
        editedDateRange = DateRange(startDate, endDate)

        isSavedSuccessful = true // Signal success
        closeDialog()           // Close the dialog
    }

    /**
     * Handles the action when the "Cancel" button is clicked in THIS dialog.
     * Simply closes the dialog without saving changes.
     */
    @FXML
    private fun handleCancelButton() {
        println("Cancel button clicked in Single Date Range Editor.")
        isSavedSuccessful = false // Signal cancellation
        editedDateRange = null // Ensure the result is null on cancel
        closeDialog()           // Close the dialog
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
        // Set the owner of the alert to this dialog stage to keep it modal within this single range editor
        alert.initOwner(dialogStage)
        alert.showAndWait() // Show the alert and wait for user interaction
    }
}