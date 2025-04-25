package com.medichart.controller

import com.medichart.database.DatabaseManager
import com.medichart.model.Medication
import com.medichart.model.PastMedication
import com.medichart.model.Surgery
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory   // Still use PropertyValueFactory for JavaFX
import java.util.Comparator   // Needed for sorting

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
    @FXML lateinit var currentInstructionsColumn: TableColumn<Medication, String>
    @FXML lateinit var currentReasonColumn: TableColumn<Medication, String>
    @FXML lateinit var currentPrescriberColumn: TableColumn<Medication, String>
    @FXML lateinit var currentNotesColumn: TableColumn<Medication, String>
    @FXML lateinit var currentStartDateColumn: TableColumn<Medication, String>

    @FXML lateinit var pastMedicationsTable: TableView<PastMedication>
    @FXML lateinit var pastBrandNameColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastGenericNameColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastDosageColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastInstructionsColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastReasonColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastPrescriberColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastHistoryNotesColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastReasonForStoppingColumn: TableColumn<PastMedication, String>
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
        currentInstructionsColumn.cellValueFactory = PropertyValueFactory("instructions")
        currentReasonColumn.cellValueFactory = PropertyValueFactory("reason")
        currentPrescriberColumn.cellValueFactory = PropertyValueFactory("prescriber")
        currentNotesColumn.cellValueFactory = PropertyValueFactory("notes")
        currentStartDateColumn.cellValueFactory = PropertyValueFactory("startDate")

        // Set up Past Medications Table Columns
        pastBrandNameColumn.cellValueFactory = PropertyValueFactory("brandName")
        pastGenericNameColumn.cellValueFactory = PropertyValueFactory("genericName")
        pastDosageColumn.cellValueFactory = PropertyValueFactory("dosage")
        pastInstructionsColumn.cellValueFactory = PropertyValueFactory("instructions")
        pastReasonColumn.cellValueFactory = PropertyValueFactory("reason")
        pastPrescriberColumn.cellValueFactory = PropertyValueFactory("prescriber")
        pastHistoryNotesColumn.cellValueFactory = PropertyValueFactory("historyNotes")
        pastReasonForStoppingColumn.cellValueFactory = PropertyValueFactory("reasonForStopping")
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
     * (Implementation needed - typically opens a dialog for input)
     */
    @FXML
    private fun handleAddMedication() {
        println("Add Medication button clicked (Implementation needed)")
        // TODO: Implement getting input from the user (e.g., show a from/dialog).
        // TODO: Create a new Medication object from input.
        // TODO: Call dbManager.addMedication(newMedObject).
        // TODO: Call loadCurrentMedications() to refresh the table.
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
            // TODO: Implement showing an edit form/dialog pre-filled with selectedMed data.
            // TODO: Get updated data from the user.
            // TODO: Create/update a Medication object.
            // TODO: Call dbManager.updateMedication(updateMedObject) - requires adding this method to DatabaseManager.
            // TODO: Call loadCurrentMedications() to refresh the table
        } else {
            println("No medication selected for editing.")
            // TODO: Show a warning or information dialog to the user (e.g., using javafx.scene.control.Alert)
        }
    }

    /**
     * Handles the action of archiving the selected current medication.
     * Moves the selected medication from the current list to the history list.
     */
    @FXML
    private fun handleArchiveMedication() {
        val selectedMed = currentMedicationsTable.selectionModel.selectedItem
        if (selectedMed != null) {
            println("Archive Medication button clicked for: ${selectedMed.brandName}")
            dbManager.archiveMedication(selectedMed)    // Call database method to move the record
            // After archiving, reload both tables to reflect changes
            loadCurrentMedications()
            loadPastMedications()
        } else {
            println("No medication selected for archiving.")
            // TODO: Show a warning to the user.
        }
    }

    /**
     * Handles the action of unarchiving the selected past medication.
     * Moves the selected medication from the history list back to the current list.
     */
    @FXML
    private fun handleUnarchiveMedication() {
        val selectedPastMed = pastMedicationsTable.selectionModel.selectedItem
        if (selectedPastMed != null) {
            println("Unarchive Medication button clicked for: ${selectedPastMed.brandName}")
            dbManager.unarchiveMedication(selectedPastMed)  // Call database method to move the record
            // After unarchiving, reload both tables to reflect changes
            loadCurrentMedications()
            loadPastMedications()
        } else {
            println("No past medication selected for unarchiving.")
            // TODO: Show a warning to the user
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
    private fun handleSortByBrandName() {
        println("Sort by Brand Name clicked (TableView handles by default)")
        // Example of programmatic sorting on the ObservableList:
        // val sortedList = currentMedicationsData.sortedWith(compareBy { it.brandName ?: "" }) // Handle potential nulls during sorting
        // currentMedicationsTable.items = FXCollections.observableArrayList(sortedList)    // Update table items
    }

    // TODO: Add handlers for sorting by Generic Name, Reason, Prescriber if separate buttons/menu items are desired

    // --- Reporting and Export Methods (Placeholder) ---

    /**
     * Handles printing the current medications table.
     * (Implementation needed - requires external libraries/JavaFX Printing API)
     */
    @FXML
    private fun handlePrintCurrentMeds() {
        println("Print Current Medications clicked (Implementation needed)")
        // TODO: Implement printing logic (requires JavaFX Printing API or a library)
    }

    /**
     * Handles exporting the current medications table to PDF.
     * (Implementation needed - requires a PDF library like iText or PDFBox)
     */
    @FXML
    private fun handleExportCurrentMedsPDF() {
        println("Export Current Medications PDF clicked (Implementation needed)")
        // TODO: Implement PDF export logic (requires a library like iText, Apache PDFBox)
    }

    /**
     * Handles exporting the current medication table to Word.
     * (Implementation needed - requires a library like Apache POI)
     */
    @FXML
    private fun handleExportCurrentMedsWord() {
        println("Export Current Medications Word clicked (Implementation needed)")
        // TODO: Implement Word export logic (requires a library like Apache POI)
    }
}