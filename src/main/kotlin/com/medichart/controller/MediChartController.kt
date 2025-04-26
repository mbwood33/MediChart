package com.medichart.controller

import com.medichart.database.DatabaseManager
import com.medichart.model.Medication
import com.medichart.model.PastMedication
import com.medichart.model.Surgery
import javafx.beans.property.Property
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory   // Still use PropertyValueFactory for JavaFX
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.IOException
import java.util.Comparator   // Needed for sorting
import javafx.scene.layout.VBox
import javafx.scene.layout.HBox
import java.time.LocalDate

/**
 * Controller class for the main MediChart GUI.
 * Handles interactions between the GUI elements and the database.
 */
class MediChartController {
    // FXML elements injected by the FXMLLoader
    // Use 'lateinit var' for FXML injected fields that will be initialized by FXMLLoader
    @FXML lateinit var currentMedicationsTable: TableView<Medication>
    @FXML lateinit var currentBrandNameColumn: TableColumn<Medication, String>
    @FXML lateinit var currentGenericNameColumn: TableColumn<Medication, String>
    @FXML lateinit var currentDosageColumn: TableColumn<Medication, String>
    @FXML lateinit var currentDoseFormColumn: TableColumn<Medication, String>
    @FXML lateinit var currentInstructionsColumn: TableColumn<Medication, String>
    @FXML lateinit var currentReasonColumn: TableColumn<Medication, String>
    @FXML lateinit var currentPrescriberColumn: TableColumn<Medication, String>
    @FXML lateinit var currentNotesColumn: TableColumn<Medication, String>
    @FXML lateinit var currentStartDateColumn: TableColumn<Medication, LocalDate>
    @FXML lateinit var currentManufacturerColumn: TableColumn<Medication, String>

    @FXML lateinit var pastMedicationsTable: TableView<PastMedication>
    @FXML lateinit var pastBrandNameColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastGenericNameColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastDosageColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastDoseFormColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastInstructionsColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastReasonColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastPrescriberColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastHistoryNotesColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastDateRangesColumn: TableColumn<PastMedication, List<PastMedication.DateRange>>
    @FXML lateinit var pastReasonForStoppingColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastManufacturerColumn: TableColumn<PastMedication, String>
    // Note: Displaying List<DateRange> in a TableColumn directly requires custom cell factories
    // @FXML lateinit var pastDateRangesColumn: TableColumn<PastMedication, List<PastMedication.DateRange>>

    @FXML lateinit var surgeriesTable: TableView<Surgery>
    @FXML lateinit var surgeryNameColumn: TableColumn<Surgery, String>
    @FXML lateinit var surgeryDateColumn: TableColumn<Surgery, String>
    @FXML lateinit var surgerySurgeonColumn: TableColumn<Surgery, String>

    private lateinit var dbManager: DatabaseManager // lateinit because it's initialized in initialize()
    private lateinit var currentMedicationsData: ObservableList<Medication>
    private lateinit var pastMedicationsData: ObservableList<PastMedication>
    private lateinit var surgeriesData: ObservableList<Surgery>

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method is called by the FXMLLoader.
     * Sets up table columns and loads initial data from the database.
     */
    @FXML
    fun initialize() {
        dbManager = DatabaseManager()   // Initialize dbManager

        // Set up Current Medications Table Columns
        // PropertyValueFactory still works with kotlin data class properties by looking for getters (which val/var provide)
        currentBrandNameColumn.cellValueFactory = PropertyValueFactory("brandName")
        currentGenericNameColumn.cellValueFactory = PropertyValueFactory("genericName")
        currentDosageColumn.cellValueFactory = PropertyValueFactory("dosage")
        currentDoseFormColumn.cellValueFactory = PropertyValueFactory("doseForm")
        currentInstructionsColumn.cellValueFactory = PropertyValueFactory("instructions")
        currentReasonColumn.cellValueFactory = PropertyValueFactory("reason")
        currentPrescriberColumn.cellValueFactory = PropertyValueFactory("prescriber")
        currentNotesColumn.cellValueFactory = PropertyValueFactory("notes")
        currentStartDateColumn.cellValueFactory = PropertyValueFactory("startDate")
        currentManufacturerColumn.cellValueFactory = PropertyValueFactory("manufacturer")

        // Set up Past Medications Table Columns
        pastBrandNameColumn.cellValueFactory = PropertyValueFactory("brandName")
        pastGenericNameColumn.cellValueFactory = PropertyValueFactory("genericName")
        pastDosageColumn.cellValueFactory = PropertyValueFactory("dosage")
        pastDoseFormColumn.cellValueFactory = PropertyValueFactory("doseForm")
        pastInstructionsColumn.cellValueFactory = PropertyValueFactory("instructions")
        pastReasonColumn.cellValueFactory = PropertyValueFactory("reason")
        pastPrescriberColumn.cellValueFactory = PropertyValueFactory("prescriber")
        pastHistoryNotesColumn.cellValueFactory = PropertyValueFactory("historyNotes")
        pastDateRangesColumn.cellValueFactory = PropertyValueFactory("dateRanges")
        pastReasonForStoppingColumn.cellValueFactory = PropertyValueFactory("reasonForStopping")
        pastManufacturerColumn.cellValueFactory = PropertyValueFactory("manufacturer")
        // TODO: Implement custom cell factory for pastDateRangeColumn if needed to display List<DateRange>
        // pastDateRangesColumn..cellValueFactory = PropertyValueFactory("dateRanges")  // This won't work directly for List

        // Set up Surgeries Table Columns
        surgeryNameColumn.cellValueFactory = PropertyValueFactory("name")
        surgeryDateColumn.cellValueFactory = PropertyValueFactory("date")
        surgerySurgeonColumn.cellValueFactory = PropertyValueFactory("surgeon")

        // Load data into tables
        loadCurrentMedications()
        loadPastMedications()
        loadSurgeries()
    }

    /**
     * Loads current medications from the database and updates the TableView.
     */
    private fun loadCurrentMedications() {
        val medications = dbManager.getAllCurrentMedications()
        // Convert List to ObservableList for JavaFX TableView
        currentMedicationsData = FXCollections.observableArrayList(medications)
        currentMedicationsTable.items = currentMedicationsData  // Set the data to the table
    }

    /**
     * Loads past medications from the database and updates the TableView.
     */
    private fun loadPastMedications() {
        val pastMedications = dbManager.getAllPastMedications()
        pastMedicationsData = FXCollections.observableArrayList(pastMedications)
        pastMedicationsTable.items = pastMedicationsData
    }

    /**
     * Loads surgeries from the database and updates the TableView
     */
    private fun loadSurgeries() {
        val surgeries = dbManager.getAllSurgeries()
        surgeriesData = FXCollections.observableArrayList(surgeries)
        surgeriesTable.items = surgeriesData
    }

    // --- Event Handlers for GUI Actions (Placeholder Methods) ---
    // These methods are connected to buttons or other GUI elements via the FXML fx:onAction attribute.
    // Use @FXML to make them accessible to the FXMLLoader.

    /**
     * Handles the action of adding a new medication.
     * Opens a dialog for the user to input medication details.
     * Selects the newly added item after refresh.
     */
    @FXML
    private fun handleAddMedication() {
        try {
            // Load the FXML for the add medication dialog
            val fxmlLoader = FXMLLoader(javaClass.getResource("/com/medichart/gui/AddMedicationDialog.fxml"))
            val dialogRoot = fxmlLoader.load<VBox>()    // Load the root element (VBox)

            // Get the controller for the dialog
            val dialogController = fxmlLoader.getController<AddMedicationController>()

            // Create a new stage for the dialog window
            val dialogStage = Stage()
            dialogStage.title = "Add New Medication"
            dialogStage.initModality(Modality.WINDOW_MODAL) // Make it modal (blocks input to parent window)
            // Set the owner stage so the dialog is centered over the main window
            dialogStage.initOwner(currentMedicationsTable.scene.window) // Use any element to get the scene and window
            val dialogScene = Scene(dialogRoot) // Create the scene

            dialogController.setDialogStage(dialogStage)    // Passes the Stage reference and sets up key event listeners

            dialogStage.scene = dialogScene // Set the scene
            dialogStage.showAndWait()   // Show the dialog and wait for it to be closed by the user

            // After the dialog is closed, check if the user clicked Save
            if (dialogController.isSavedSuccessful) {
                val newMedication = dialogController.medicationData // Get the Medication object from the dialog controller

                // Ensure the medication data was actually captured
                if (newMedication != null) {
                    dbManager.addMedication(newMedication)  // Add the new medication to the database
                    loadCurrentMedications()    // Refresh the current medications table to show the new entry

                    // Select the last item in the table (the newly added one)
                    if (currentMedicationsTable.items.isNotEmpty()) {
                        currentMedicationsTable.selectionModel.selectLast()
                    }

                    println("Medication added successfully: ${newMedication.brandName ?: newMedication.genericName}")
                } else {
                    println("Dialog closed, but no medication data was captured.")
                }
            } else {
                println("Add Medication dialog cancelled.")
            }
        } catch (e: IOException) {
            // Handle potential errors during FXML loading
            System.err.println("Error loading Add Medication dialog FXML: ${e.message}")
            e.printStackTrace() // Print stack trace for debugging
            // TODO: Show an error message to the user
        }
    }

    /**
     * Handles the action of editing the selected current medication.
     * (Implementation needed - typically opens a dialog with existing data)
     */
    @FXML
    private fun handleEditMedication() {
        val selectedMed = currentMedicationsTable.selectionModel.selectedItem   // Get selected item from table
        if (selectedMed != null) {
            println("Edit Medication button clicked for: ${selectedMed.brandName} (Implementation needed)")
            // TODO: Implement editing logic. Liekly involves:
            // TODO: Loading AddMedicationDialog.fxml again.
            // TODO: Getting the AddMedicationController instance.
            // TODO: Showing the dialog
            // TODO: If saved, getting the updated data from the controller.
            // TODO: Calling dbManager.updateMedication(updateMedObject) method (needs to be added).
            // TODO: Calling loadCurrentMedications() to refresh the table
        } else {
            println("No medication selected for editing.")
            // TODO: Show a warning or information dialog to the user (e.g., using javafx.scene.control.Alert)
        }
    }

    /**
     * Handles the action of archiving the selected current medication.
     * Moves the selected medication from the current list to the history list.
     * Selects the item at the same index (or the last) after refreshes in the current table.
     */
    @FXML
    private fun handleArchiveMedication() {
        println("Archive Medication button clicked.")
        val selectedMed = currentMedicationsTable.selectionModel.selectedItem
        if (selectedMed != null) {
            val selectedIndex = currentMedicationsTable.selectionModel.selectedIndex // Get index BEFORE refresh

            dbManager.archiveMedication(selectedMed)
            loadCurrentMedications() // Refresh current table
            loadPastMedications() // Refresh history table
            println("Medication archived: ${selectedMed.brandName ?: selectedMed.genericName}")

            // Select the item at the same index in the updated current list, or the last item if index is out of bounds.
            // This effectively selects the item that moved into the archived item's old position.
            if (currentMedicationsTable.items.isNotEmpty()) {
                val newIndexToSelect = if (selectedIndex < currentMedicationsTable.items.size) selectedIndex else currentMedicationsTable.items.size - 1
                currentMedicationsTable.selectionModel.select(newIndexToSelect)
            } else {
                // If the table is now empty, clear selection
                currentMedicationsTable.selectionModel.clearSelection()
            }
            // TODO: Also consider selecting the newly archived item in the past medications table?
            // If so, you'd need to find it in the pastMedicationsTable.items after loadPastMedications()
            // For now, just handling selection in the original table (current).
        } else {
            // TODO: Show a warning or information dialog to the user (e.g., using javafx.scene.control.Alert)
            println("No medication selected for archiving.")
        }
    }

    /**
     * Handles the action of deleting the selected current medication.
     * Gets the selected item, confirms deletion, calls database delete, and refreshes table.
     * Selects the item at the same index (or the last) after refresh.
     * TODO: Add a confirmation dialog.
     */
    @FXML
    private fun handleDeleteCurrentMedication() {
        println("Delete Medication button clicked.")
        val selectedMed = currentMedicationsTable.selectionModel.selectedItem

        if (selectedMed != null) {
            val selectedIndex = currentMedicationsTable.selectionModel.selectedIndex    // Get index BEFORE refresh

            // TODO: Add a confirmation dialog here before actually deleting!
            // e.g., Alert(AlertType.CONFIRMATION, "Are you sure you want to delete {$selectedMed.brandName ?: selectedMed.genericName}?")
            // If user confirms:

            println("Delete Medication button called for: ${selectedMed.brandName ?: selectedMed.genericName}")

            dbManager.deleteCurrentMedication(selectedMed.id)   // Call the database method to delete the record
            loadCurrentMedications()    // Refresh the current medications table to show the change

            // Select the item at the same index in the updated list, or the last item if index is out of bounds.
            // This selects the item that moved into the deleted item's old position.
            if (currentMedicationsTable.items.isNotEmpty()) {
                val newIndexToSelect = if (selectedIndex < currentMedicationsTable.items.size) selectedIndex else currentMedicationsTable.items.size - 1
                currentMedicationsTable.selectionModel.select(newIndexToSelect)
            } else {
                // If the table is now empty, clear selection
                currentMedicationsTable.selectionModel.clearSelection()
            }

            println("Current medication deleted.")
        } else {
            println("No medication selected for deletion.")
            // TODO: Show a warning or information dialog to the user (e.g., using javafx.scene.control.Alert)
        }
    }

    /**
     * Handles the action of unarchiving the selected past medication.
     * Moves the selected medication from past_meds back to current_meds.
     * Selects the newly added item in the current table.
     */
    @FXML
    private fun handleUnarchiveMedication() {
        println("Unarchive Medication button clicked.")
        val selectedPastMed = pastMedicationsTable.selectionModel.selectedItem
        if (selectedPastMed != null) {
            val selectedIndex = pastMedicationsTable.selectionModel.selectedIndex   // Get index BEFORE refresh

            dbManager.unarchiveMedication(selectedPastMed)  // Call database method to move the record
            loadCurrentMedications()    // Refresh history table
            loadPastMedications()   // Refresh current table
            println("Medication unarchived: ${selectedPastMed.brandName ?: selectedPastMed.genericName}")

            // Select the item at the same index in the updated past list, or the last item if index is out of bounds.
            // This keeps focus in the past table near where the item was removed.
            if (pastMedicationsTable.items.isNotEmpty()) {
                val newIndexToSelect = if (selectedIndex < pastMedicationsTable.items.size) selectedIndex else pastMedicationsTable.items.size - 1
                pastMedicationsTable.selectionModel.select(newIndexToSelect)
            } else {
                // If the past table is now empty after unarchiving the last item, clear selection
                pastMedicationsTable.selectionModel.clearSelection()
            }
        } else {
            // TODO: Show a warning to the user
            println("No past medication selected for unarchiving.")
        }
    }

    /**
     * Handles the action of deleting the selected past medication.
     * Gets the selected item, confirms deletion, calls database delete, and refreshes table.
     * Selects the item at the same index (or the last) after refresh in the past table.
     * TODO: Add a confirmation dialog.
     */
    @FXML
    private fun handleDeletePastMedication() {
        println("Delete Past Medication button clicked.")
        val selectedPastMed = pastMedicationsTable.selectionModel.selectedItem

        if (selectedPastMed != null) {
            val selectedIndex = pastMedicationsTable.selectionModel.selectedIndex   // Get index BEFORE refresh (past table)

            // TODO: Add a confirmation dialog here before actually deleting past med!
            // If user confirms:

            println("Delete Past Medication butten clicked for: {${selectedPastMed.brandName ?: selectedPastMed.genericName}")

            dbManager.unarchiveMedication(selectedPastMed)
            loadPastMedications()   // Refresh history table

            // Select the item at the same index in the updated list, or the last item if index is out of bounds.
            // This selects the item that moved into the deleted item's old position.
            if (pastMedicationsTable.items.isNotEmpty()) {
                val newIndexToSelect = if (selectedIndex < pastMedicationsTable.items.size) selectedIndex else pastMedicationsTable.items.size - 1
                pastMedicationsTable.selectionModel.select(newIndexToSelect)
            } else {
                // If the table is now empty, clear selection
                pastMedicationsTable.selectionModel.clearSelection()
            }
            println("Past medication deleted.")
        } else {
            println("No past medication selected for deletion.")
            // TODO: Show a warning or information dialog to the user
        }
    }

    /**
     * Handles the action of adding a new surgery record.
     * (Implementation needed - typically opens a dialog for input)
     */
    @FXML
    private fun handleAddSurgery() {
        println("Add Surgery button clicked (Implementation needed)")
        // TODO: Implement getting input from the user (e.g., show a form/dialog)
        // TODO: Create a new Surgery object from input.
        // TODO: Call dbManager.addSurgery(newSurgeryObject).
        // TODO: Call loadSurgeries() to refresh the table.
    }

    // --- Sorting Methods (Placeholder) ---
    // JavaFX TableView columns are sortable by default when you click their headers,
    // if the cellValueFactory is set up correctly (as we have done).
    // These methods are only needed if you want dedicated sort buttons or menu items.

    /**
     * Sorts the current medications table by Brand Name (A-Z).
     * Note: TableView columns are sortable by default when clicking headers.
     * This method shows how programmatic sorting could be done.
     */
    @FXML
    private fun handleSortByBrandName() { println("Sort by Brand Name clicked (TableView handles by default)") }
    // TODO: Add handlers for sorting by Generic Name, Reason, Prescriber if separate buttons/menu items are desired

    // --- Reporting and Export Methods (Placeholder) ---

    /**
     * Handles printing the current medications table.
     * (Implementation needed - requires external libraries/JavaFX Printing API)
     */
    @FXML
    private fun handlePrintCurrentMeds() { println("Print Current Medications clicked (Implementation needed)") }
    // TODO: Implement printing logic (requires JavaFX Printing API or a library)

    /**
     * Handles exporting the current medications table to PDF.
     * (Implementation needed - requires a PDF library like iText or PDFBox)
     */
    @FXML
    private fun handleExportCurrentMedsPDF() { println("Export Current Medications PDF clicked (Implementation needed)") }
    // TODO: Implement PDF export logic (requires a library like iText, Apache PDFBox)

    /**
     * Handles exporting the current medication table to Word.
     * (Implementation needed - requires a library like Apache POI)
     */
    @FXML
    private fun handleExportCurrentMedsWord() { println("Export Current Medications Word clicked (Implementation needed)") }
    // TODO: Implement Word export logic (requires a library like Apache POI)
}