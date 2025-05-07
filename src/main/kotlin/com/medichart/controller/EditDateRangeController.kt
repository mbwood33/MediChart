package com.medichart.controller

import com.medichart.database.DatabaseManager // <-- Import DatabaseManager
import com.medichart.model.PastMedication.DateRange
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.TableView
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory
import javafx.stage.Stage
import javafx.scene.control.Button
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.scene.Scene
import java.time.LocalDate
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.fxml.FXMLLoader
import javafx.scene.layout.VBox
import java.io.IOException
import javafx.stage.Modality


/**
 * Controller for the dedicated Date Range Editor dialog.
 * Handles adding, editing, and removing DateRange objects within a list.
 */
class EditDateRangeController {

    // FXML elements for the TableView and buttons
    @FXML lateinit var dateRangesTable: TableView<DateRange>
    // Declare columns to hold DateRange data. Use LocalDate? as the type.
    @FXML lateinit var startDateColumn: TableColumn<DateRange, LocalDate?>
    @FXML lateinit var endDateColumn: TableColumn<DateRange, LocalDate?>
    @FXML lateinit var addRangeButton: Button
    @FXML lateinit var editRangeButton: Button
    @FXML lateinit var removeRangeButton: Button

    // Reference to the dialog stage
    private var dialogStage: Stage? = null
    // Reference to the DatabaseManager (passed from the main controller)
    private var dbManager: DatabaseManager? = null // <-- Property to hold dbManager

    // ObservableList to hold the DateRange data for the TableView
    // Initialize it as an empty observable list.
    private var dateRangesData: ObservableList<DateRange> = FXCollections.observableArrayList()

    // Properties to hold the result of THIS dialog (whether saved and the edited list)
    var isSavedSuccessful: Boolean = false
        private set

    var editedDateRanges: List<DateRange>? = null // Holds the final list of Date Ranges after saving


    /**
     * Called by FXMLLoader after the FXML is loaded.
     * Sets up table columns and initial properties.
     */
    @FXML
    fun initialize() {
        // Set up cell value factories for the columns
        // PropertyValueFactory works by looking for property names (like "startDate" and "endDate")
        // in the DateRange data class. It uses reflection to call getters.
        startDateColumn.cellValueFactory = PropertyValueFactory("startDate")
        endDateColumn.cellValueFactory = PropertyValueFactory("endDate")

        // TODO: Optional: Implement custom cell factories for LocalDate? columns
        // This would allow custom formatting (e.g., "yyyy-MM-dd" or "N/A") or even inline editing with DatePickers.
        // For now, PropertyValueFactory will display the default toString() of LocalDate or null.
    }

    /**
     * Public method called by the main controller (EditPastMedicationController)
     * to set the Stage and DatabaseManager, and potentially add key event listeners.
     * @param stage The Stage that hosts this dialog.
     * @param dbManager The DatabaseManager instance.
     * <-- UPDATE THIS METHOD (added dbManager parameter) -->
     */
    fun setupDialog(stage: Stage, dbManager: DatabaseManager) {
        this.dialogStage = stage
        this.dbManager = dbManager // <-- Assign the passed dbManager

        // Add event filter to the scene for key presses (Enter/Escape)
        dialogStage?.sceneProperty()?.addListener { _, _, newScene ->
            if (newScene != null) {
                newScene.setOnKeyPressed { event ->
                    when (event.code) {
                        // Decide if Enter should save, Escape should cancel etc. for THIS dialog
                        // KeyCode.ENTER -> handleSaveButton() // Potentially risky with multiple fields or TableView focus
                        KeyCode.ESCAPE -> handleCancelButton() // Escape key cancels this dialog
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
            editedDateRanges = null // Ensure result is null if dialog closed via window button
        }
    }


    /**
     * Public method to receive the initial list of Date Ranges from the calling controller.
     * Populates the TableView with this data.
     * @param dateRanges The list of DateRange objects to edit.
     */
    fun setDateRangesData(dateRanges: List<DateRange>?) {
        // Clear existing data in the ObservableList and add the received data
        dateRangesData.setAll(dateRanges ?: emptyList()) // Use setAll to replace all items
        // Set the ObservableList as the items for the TableView
        dateRangesTable.items = dateRangesData
    }


    // --- Add a helper function for loading and showing nested dialogs ---
    /**
     * Helper function to load an FXML file, create a modal dialog stage, and return the controller.
     * Sets the owner of the dialog to the current dialog stage.
     * @param fxmlPath The path to the FXML file (e.g., "/com/medichart/gui/EditSingleDateRangeDialog.fxml").
     * @param title The title for the dialog window.
     * @return The controller of the loaded FXML.
     */
    private fun <T> loadAndShowNestedDialog(fxmlPath: String, title: String): T? { // <-- ADD THIS HELPER
        try {
            val fxmlLoader = FXMLLoader(javaClass.getResource(fxmlPath))
            val dialogRoot = fxmlLoader.load<VBox>() // Assuming VBox is the root of nested dialogs

            val controller = fxmlLoader.getController<T>() // Get the specific controller type

            val dialogStage = Stage()
            dialogStage.title = title
            dialogStage.initModality(Modality.WINDOW_MODAL)
            // Set the owner of the NESTED dialog to THIS dialog stage
            dialogStage.initOwner(this.dialogStage)
            val dialogScene = Scene(dialogRoot)

            // Call the controller's setup method (assuming a method like setDialogStage exists)
            // We know the nested dialog is EditSingleDateRangeDialogController, so we can check its type
            if (controller is EditSingleDateRangeController) {
                controller.setDialogStage(dialogStage) // Pass the Stage to the nested controller
            }
            // Add checks for other potential nested dialog controllers here if needed in the future


            dialogStage.scene = dialogScene
            dialogStage.showAndWait() // Show and wait for the nested dialog to be closed

            return controller // Return the controller to get results (e.g., saved data)

        } catch (e: IOException) {
            System.err.println("Error loading nested dialog FXML: $fxmlPath - ${e.message}")
            e.printStackTrace()
            showAlert(AlertType.ERROR, "Error Loading Dialog", "Could not load the editor dialog.", "An error occurred while opening the editor.")
            return null
        }
    }


    /**
     * Handles the action when the "Add Range" button is clicked.
     * Opens a dialog to add a single DateRange.
     */
    @FXML // <-- Add FXML annotation
    private fun handleAddRange() { // <-- ADD THIS METHOD (Implementation for adding)
        println("Add Range button clicked.")
        // Use the helper to load and show the single date range editor dialog
        val dialogController = loadAndShowNestedDialog<EditSingleDateRangeController>(
            "/com/medichart/gui/EditSingleDateRangeDialog.fxml",
            "Add New Date Range"
        )

        // Check if the dialog was saved and returned a valid DateRange
        if (dialogController != null && dialogController.isSavedSuccessful) {
            val newDateRange = dialogController.editedDateRange

            if (newDateRange != null) {
                // Add the new DateRange to the ObservableList
                dateRangesData.add(newDateRange)
                // Optional: Reselect the newly added item
                dateRangesTable.selectionModel.select(newDateRange)
                // Use dbManager to serialize for debug print (dbManager is available via the property)
                println("New Date Range added: ${dbManager?.serializeDateRanges(listOf(newDateRange))}")
            } else {
                // Shouldn't happen but handled defensively
                System.err.println("Warning: Single Date Range dialog saved successfully, but returned null data.")
            }
        } else {
            // This runs if dialogInfo is null (loading failed) OR dialogController.first.isSavedSuccessful is false (under clicked Cancel in nested dialog)
            println("Single Date Range dialog cancelled or failed (loading or save cancelled).")
            if (dialogController == null) { // Specifically handle the care where loading the dialog failed
                System.err.println("Error loading Single Date Range Editor dialog.")
                // Optionally show an error to the user
                showAlert(AlertType.ERROR, "Error", "Failed to Load Dialog", "Could not open the date range editor.")
            }
        }
    }

    /**
     * Handles the action when the "Edit Selected" button is clicked.
     * Opens a dialog to edit the selected DateRange.
     * <-- IMPLEMENTATION -->
     */
    @FXML // <-- Add FXML annotation
    private fun handleEditRange() { // <-- ADD THIS METHOD (Implementation for editing)
        println("Edit Range button clicked.")
        val selectedRange = dateRangesTable.selectionModel.selectedItem

        if (selectedRange != null) {
            // Get the selected item from the TableView
            val dialogController = loadAndShowNestedDialog<EditSingleDateRangeController>(
                "/com/medichart/gui/EditSingleDateRangeDialog.fxml",
                "Edit Date Range"
            )

            if (dialogController != null) {
                dialogController.setDateRangeData(selectedRange.copy())

                if (dialogController.isSavedSuccessful) {
                    val updatedDateRange = dialogController.editedDateRange
                    val index = dateRangesData.indexOf(selectedRange)
                    if (index >= 0) {
                        if (updatedDateRange != null) {
                            dateRangesData[index] = updatedDateRange
                            println("Date Range updated: ${dbManager?.serializeDateRanges(listOf(updatedDateRange))}")
                        } else {
                            System.err.println("Warning: Single Date Range dialog saved successfully, but returned null data. Removing original range.") // Message for null updatedRange
                            dateRangesData.removeAt(index)
                        }
                    } else {
                        println("Edit Date Range dialog cancelled.")
                    }
                } else {
                    System.err.println("Failed to load the Single Date Range Editor dialog for editing.")
                    // Optionally show an error to the user
                    showAlert(AlertType.ERROR, "Error", "Failed to Load Dialog", "Could not open the date range editor.")
                }
            } else {
                showAlert(AlertType.INFORMATION, "No Selection", null, "Please select a date range to edit.")
            }
        }
    }

    /**
     * Handles the action when the "Remove Selected" button is clicked.
     * Removes the selected DateRange from the table.
     */
    @FXML // <-- Add FXML annotation
    private fun handleRemoveRange() { // <-- Ensure FXML annotation is present
        println("Remove Range button clicked.")
        // Get the selected item from the TableView
        val selectedRange = dateRangesTable.selectionModel.selectedItem

        if (selectedRange != null) {
            // TODO: Optional: Add a confirmation dialog before removing (e.g., "Are you sure you want to remove this date range?")

            // Remove the selected DateRange from the ObservableList
            dateRangesData.remove(selectedRange)
            println("Date Range removed.")
        } else {
            // Show a warning if no item is selected
            showAlert(AlertType.INFORMATION, "No Selection", null, "Please select a date range to remove.")
        }
    }


    /**
     * Handles the action when the "Save" button is clicked in THIS dialog.
     * Captures the current list of Date Ranges from the table.
     * <-- Ensure FXML annotation is present -->
     */
    @FXML
    private fun handleSaveButton() {
        println("Save button clicked in Date Range Editor.")
        // Capture the current state of the ObservableList as the result
        // Convert the ObservableList to a standard List for the result property.
        editedDateRanges = dateRangesData.toList() // toList() creates an immutable copy

        isSavedSuccessful = true // Signal that the save was successful
        closeDialog()           // Close this dialog
    }

    /**
     * Handles the action when the "Cancel" button is clicked in THIS dialog.
     * Simply closes the dialog without saving changes.
     * <-- Ensure FXML annotation is present -->
     */
    @FXML
    private fun handleCancelButton() {
        println("Cancel button clicked in Date Range Editor.")
        isSavedSuccessful = false // Signal that the save was not successful
        editedDateRanges = null // Ensure the result is null on cancel
        closeDialog()           // Close this dialog
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
        // Set the owner of the alert to this dialog stage to keep it modal within the date range editor
        alert.initOwner(dialogStage)
        alert.showAndWait() // Show the alert and wait for user interaction
    }
}