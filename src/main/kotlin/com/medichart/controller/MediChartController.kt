package com.medichart.controller

import com.medichart.database.DatabaseManager
import com.medichart.model.Medication
import com.medichart.model.PastMedication
import com.medichart.model.PastMedication.DateRange
import com.medichart.model.Surgery
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.TableCell
import javafx.scene.control.TableView
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory   // Still use PropertyValueFactory for JavaFX
import javafx.scene.layout.VBox
import javafx.scene.layout.HBox
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.IOException
import java.time.LocalDate
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.util.Callback
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.converter.DefaultStringConverter
import javafx.scene.control.TableColumn.CellEditEvent
import javafx.collections.ObservableList
import javafx.scene.input.MouseEvent
import javafx.collections.FXCollections
import javafx.event.EventHandler
import java.util.Comparator   // Needed for sorting
import kotlin.random.Random
import javafx.beans.property.SimpleObjectProperty   // Might be needed for DatePickerTableCell
import javafx.beans.binding.Bindings    // Might be needed for complex cell value factories
import javafx.scene.control.Button
import javafx.scene.layout.Region   // If Region is used in FXML HBoxes
import javafx.stage.FileChooser
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.File

import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import com.lowagie.text.DocumentException
import com.lowagie.text.Font
import com.lowagie.text.FontFactory
import com.lowagie.text.Chunk
import com.lowagie.text.Phrase
import com.lowagie.text.Element
import com.lowagie.text.pdf.draw.LineSeparator
import com.sun.javafx.fxml.expression.Expression.add

/**
 * Controller class for the main MediChart GUI.
 * Handles interactions between the GUI elements and the database.
 */
class MediChartController {
    private lateinit var dbManager: DatabaseManager

    // FXML elements for the Current Medications Table
    @FXML lateinit var currentMedicationsTable: TableView<Medication>
    @FXML lateinit var currentGenericNameColumn: TableColumn<Medication, String>
    @FXML lateinit var currentBrandNameColumn: TableColumn<Medication, String>
    @FXML lateinit var currentDosageColumn: TableColumn<Medication, String>
    @FXML lateinit var currentDoseFormColumn: TableColumn<Medication, String>
    @FXML lateinit var currentInstructionsColumn: TableColumn<Medication, String>
    @FXML lateinit var currentReasonColumn: TableColumn<Medication, String>
    @FXML lateinit var currentPrescriberColumn: TableColumn<Medication, String>
    @FXML lateinit var currentNotesColumn: TableColumn<Medication, String>
    @FXML lateinit var currentStartDateColumn: TableColumn<Medication, LocalDate>
    @FXML lateinit var currentManufacturerColumn: TableColumn<Medication, String>

    // FXML elements for the Medications History Table
    @FXML lateinit var pastMedicationsTable: TableView<PastMedication>
    @FXML lateinit var pastGenericNameColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastBrandNameColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastDosageColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastDoseFormColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastInstructionsColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastReasonColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastPrescriberColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastHistoryNotesColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastReasonForStoppingColumn: TableColumn<PastMedication, String>
    @FXML lateinit var pastDateRangesColumn: TableColumn<PastMedication, List<PastMedication.DateRange>>
    @FXML lateinit var pastManufacturerColumn: TableColumn<PastMedication, String>

    // FXML elements for the Surgeries Table
    @FXML lateinit var surgeriesTable: TableView<Surgery>
    @FXML lateinit var surgeryNameColumn: TableColumn<Surgery, String>
    @FXML lateinit var surgeryDateColumn: TableColumn<Surgery, String>
    @FXML lateinit var surgerySurgeonColumn: TableColumn<Surgery, String>

    // FXML elements for Buttons (added 5/3/25)
    @FXML lateinit var addMedicationButton: Button
    @FXML lateinit var editMedicationButton: Button
    @FXML lateinit var archiveMedicationButton: Button
    @FXML lateinit var deleteCurrentMedicationButton: Button
    @FXML lateinit var addPastMedicationButton: Button
    @FXML lateinit var editPastMedicationButton: Button
    @FXML lateinit var unarchiveButton: Button
    @FXML lateinit var deletePastMedicationButton: Button

    private lateinit var currentMedicationsData: ObservableList<Medication>
    private lateinit var pastMedicationsData: ObservableList<PastMedication>
    private lateinit var surgeriesData: ObservableList<Surgery>

    /**
     * This method is called by the application entry point (MediChartApp)
     * AFTER the FXML has been loaded and initialize() has been called.
     * It's used to inject dependencies and perform setup that requires them.
     * @param dbManager The initialized DatabaseManager instance.
     */
    fun setupDependencies(dbManager: DatabaseManager) { // ADD THIS NEW METHOD
        this.dbManager = dbManager // Initialize the lateinit var here

        // --- Database Initialization (Already done in App, so just pass the instance) ---
        // dbManager.createTables() // REMOVE - should be done once in App startup

        // --- Data Loading ---
        // Load data into tables after dbManager is set up
        loadCurrentMedications() // CALL THESE METHODS HERE
        loadPastMedications()    // CALL THESE METHODS HERE
        loadSurgeries()          // CALL THESE METHODS HERE

        // --- Inline Editing Setup (Requires dbManager) ---
        // Set up inline editing using the setupStringInLineEditing extension function for current medications
        // These calls require the dbManager instance. MOVE THESE BLOCKS HERE FROM initialize()
        currentGenericNameColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue -> item.copy(genericName = newValue) }
        currentBrandNameColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue -> item.copy(brandName = newValue.takeIf { it.isNotEmpty () }) }
        currentDosageColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue -> item.copy(dosage = newValue.takeIf { it.isNotEmpty() }) }
        currentDoseFormColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue -> item.copy(doseForm = newValue.takeIf { it.isNotEmpty() }) }
        currentInstructionsColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue -> item.copy(instructions = newValue.takeIf { it.isNotEmpty() }) }
        currentManufacturerColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue -> item.copy(manufacturer = newValue.takeIf { it.isNotEmpty() }) }
        currentReasonColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue -> item.copy(reason = newValue.takeIf { it.isNotEmpty() }) }
        currentPrescriberColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue -> item.copy(prescriber = newValue.takeIf { it.isNotEmpty() }) }
        // TODO: Add inline editing for current Start Date (DatePicker) - Move setup here if it needs dbManager
        // TODO: Add inline editing for current Notes (TextArea) - Move setup here if it needs dbManager

        // Set up inline editing using the setupStringInLineEditing extension function for past medications
        // These calls require the dbManager instance. MOVE THESE BLOCKS HERE FROM initialize()
        pastGenericNameColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue -> item.copy(genericName = newValue) }
        pastBrandNameColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue -> item.copy(brandName = newValue.takeIf { it.isNotEmpty () }) }
        pastDosageColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue -> item.copy(dosage = newValue.takeIf { it.isNotEmpty() }) }
        pastDoseFormColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue -> item.copy(doseForm = newValue.takeIf { it.isNotEmpty() }) }
        pastInstructionsColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue -> item.copy(instructions = newValue.takeIf { it.isNotEmpty() }) }
        pastManufacturerColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue -> item.copy(manufacturer = newValue.takeIf { it.isNotEmpty() }) }
        pastReasonColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue -> item.copy(reason = newValue.takeIf { it.isNotEmpty() }) }
        pastPrescriberColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue -> item.copy(prescriber = newValue.takeIf { it.isNotEmpty() }) }
        pastReasonForStoppingColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue -> item.copy(reasonForStopping = newValue.takeIf { it.isNotEmpty() }) }
        // TODO: Add inline editing for past History Notes (TextArea) - Move setup here if it needs dbManager
        // TODO: Add inline editing for past Date Ranges (custom cell/dialog) - Move setup here if it needs dbManager
    }

    /**
     * Public method called by the application entry point (MediChartApp)
     * to provide the initialized DatabaseManager instance.
     * This is the standard way to inject this dependency.
     * @param dbManager The initialized DatabaseManager instance.
     */
    fun setDbManager(dbManager: DatabaseManager) { // <-- KEEP OR ADD THIS METHOD
        this.dbManager = dbManager
    }

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method is called by the FXMLLoader.
     * Sets up table columns and loads initial data from the database.
     */
    @FXML
    fun initialize() {
        // Set up Current Medications Table Columns
        // PropertyValueFactory still works with kotlin data class properties by looking for getters (which val/var provide)
        currentGenericNameColumn.cellValueFactory = PropertyValueFactory("genericName")
        currentBrandNameColumn.cellValueFactory = PropertyValueFactory("brandName")
        currentDosageColumn.cellValueFactory = PropertyValueFactory("dosage")
        currentDoseFormColumn.cellValueFactory = PropertyValueFactory("doseForm")
        currentInstructionsColumn.cellValueFactory = PropertyValueFactory("instructions")
        currentReasonColumn.cellValueFactory = PropertyValueFactory("reason")
        currentPrescriberColumn.cellValueFactory = PropertyValueFactory("prescriber")
        currentNotesColumn.cellValueFactory = PropertyValueFactory("notes")
        currentStartDateColumn.cellValueFactory = PropertyValueFactory("startDate")
        currentManufacturerColumn.cellValueFactory = PropertyValueFactory("manufacturer")

        // TODO: Apply Custom Cell Factory for Word Wrapping to Notes Column
        // TODO: Implement custom cell factory for Start Date (DatePickerTableCell)

        // Set up Past Medications Table Columns
        pastGenericNameColumn.cellValueFactory = PropertyValueFactory("genericName")
        pastBrandNameColumn.cellValueFactory = PropertyValueFactory("brandName")
        pastDosageColumn.cellValueFactory = PropertyValueFactory("dosage")
        pastDoseFormColumn.cellValueFactory = PropertyValueFactory("doseForm")
        pastInstructionsColumn.cellValueFactory = PropertyValueFactory("instructions")
        pastReasonColumn.cellValueFactory = PropertyValueFactory("reason")
        pastPrescriberColumn.cellValueFactory = PropertyValueFactory("prescriber")
        pastHistoryNotesColumn.cellValueFactory = PropertyValueFactory("historyNotes")
        pastReasonForStoppingColumn.cellValueFactory = PropertyValueFactory("reasonForStopping")
        pastDateRangesColumn.cellValueFactory = PropertyValueFactory("dateRanges")
        pastManufacturerColumn.cellValueFactory = PropertyValueFactory("manufacturer")

        // TODO: (Future) Apply Custom Cell Factory for Word Wrapping to Past Meds History Notes column
        // TODO: (Future) Implement custom cell factory for pastDateRangesColumn to format/edit the List<DateRange>

        // Set up Surgeries Table Columns
        surgeryNameColumn.cellValueFactory = PropertyValueFactory("name")
        surgeryDateColumn.cellValueFactory = PropertyValueFactory("date")
        surgerySurgeonColumn.cellValueFactory = PropertyValueFactory("surgeon")

        // TODO: (Future) Implement custom cell factory for surgeryDateColumn (DatePickerTableCell)
        // TODO: (Future) Add inline editing for Surgeries table

        currentMedicationsTable.isEditable = true   // Enable inline editing for Current Medications Table
        pastMedicationsTable.isEditable = true   // Enable inline editing for Past Medications Table

        // --- Enforce Double-Click for Editing (Current Meds) ---
        // Add an event filter to the TableView to require double-clicking for editing.
        // This prevents single clicks from potentially triggering edit mode after programmatic selection.
        currentMedicationsTable.addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
            if (currentMedicationsTable.isEditable && !event.isConsumed) {  // Check if table is editable and event hasn't been handled
                val cell = event.target as? TableCell<*, *> // Try to cast target to TableCell
                if (cell != null && cell.tableColumn != null) { // Ensure it's a click on a cell within a column
                    if (event.clickCount == 1) {
                        // If it's a single click on an editable cell, consume the event
                        // This prevents single-click editing while allowing selection change
                        if (cell.tableColumn.isEditable) {  // Only consume if the column itself is editable
                            event.consume() // Consume the event to stop it from triggering default edit logic
                        }
                        // TODO: Add checks for other cell types like DatePickerTableCell if they become editable
                    }
                }
            }
        }
        // --- End Enforce Double-Click

        // --- Enforce Double-Click for Editing (Past Meds) ---
        // TODO: Add Double-Click enforcement for past medications table if desired
        pastMedicationsTable.addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
            if (pastMedicationsTable.isEditable && !event.isConsumed) {
                val cell = event.target as? TableCell<*, *>
                if (cell != null && cell.tableColumn != null) {
                    if (event.clickCount == 1) {
                        if (cell.tableColumn.isEditable) {
                            // If it's a text cell and editable, consume the single click to prevent immediate edit
                            if (cell is TextFieldTableCell<*, *>) {
                                event.consume()
                            }
                            // TODO: Add checks for other cell types like custom date range cell if it becomes editable
                        }
                    }
                }
            }
        }
        // --- End Enforce Double-Click (Past Meds) ---
    }

    /**
     * Loads current medications from the database and updates the TableView.
     */
    fun loadCurrentMedications() {
        val meds = dbManager.getAllCurrentMedications() // This uses dbManager
        currentMedicationsData = FXCollections.observableArrayList(meds)
        currentMedicationsTable.items = currentMedicationsData  // Set the data to the table
        println("Loaded ${meds.size} current medications.")
    }

    /**
     * Loads past medications from the database and updates the TableView.
     */
    fun loadPastMedications() {
        println("Loading past medications...")
        val meds = dbManager.getAllPastMedications() // This uses dbManager
        pastMedicationsData = FXCollections.observableArrayList(meds)
        pastMedicationsTable.items = pastMedicationsData
        println("Loaded ${meds.size} past medications.")
    }

    /**
     * Loads surgeries from the database and updates the TableView
     */
    fun loadSurgeries() {
        println("Loading surgeries...")
        val surgeries = dbManager.getAllSurgeries() // This uses dbManager
        surgeriesData = FXCollections.observableArrayList(surgeries)
        surgeriesTable.items = surgeriesData
        println("Loaded ${surgeries.size} surgeries.")
    }

    // --- Event Handlers for GUI Actions (Placeholder Methods) ---
    // These methods are connected to buttons or other GUI elements via the FXML fx:onAction attribute.
    // Use @FXML to make them accessible to the FXMLLoader.

    /**
     * Handles the action of adding a new medication.
     * Opens a dialog for the user to input medication details.
     * Selects the newly added item after refresh.
     * EDITED: Added dialog loading/showing logic, getting data from dialog controller, and selection logic.
     */
    @FXML
    private fun handleAddMedication() {
        try {
            // Load the FXML for the add medication dialog
            val fxmlLoader = FXMLLoader(javaClass.getResource("/com/medichart/gui/AddMedicationDialog.fxml"))
            val dialogRoot = fxmlLoader.load<VBox>()    // Load the root element (VBox)
            val dialogController = fxmlLoader.getController<AddMedicationController>()

            // Create a new stage for the dialog window
            val dialogStage = Stage()
            dialogStage.title = "Add New Medication"
            dialogStage.initModality(Modality.WINDOW_MODAL) // Make it modal (blocks input to parent window)
            dialogStage.initOwner(currentMedicationsTable.scene.window) // Use any element to get the scene and window
            val dialogScene = Scene(dialogRoot) // Create the scene
            dialogStage.scene = dialogScene

            dialogController.setDialogStage(dialogStage)    // Passes the Stage reference and sets up key event listeners

            dialogStage.showAndWait()   // Show the dialog and wait for it to be closed by the user

            // After the dialog is closed, check if the user clicked Save
            if (dialogController.isSavedSuccessful) {
                val newMedication = dialogController.medicationData // Get the Medication object from the dialog controller
                if (newMedication != null) {
                    dbManager.addMedication(newMedication)  // Add the new medication to the database
                    loadCurrentMedications()    // Refresh the current medications table to show the new entry

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
            System.err.println("Error loading Add Medication dialog FXML: ${e.message}")
            e.printStackTrace() // Print stack trace for debugging
            showAlert(AlertType.ERROR, "Error Loading Dialog", "Could not load the add dialog.", "An error occurred while trying to open the add window.") // Use showAlert helper
        }
    }

    /**
     * Handles the action of editing the selected current medication.
     * Opens a dialog to edit medication details.
     * EDITED: Implemented dialog loading, showing, and passing data to the dialog controller.
     */
    @FXML
    private fun handleEditMedication() {
        println("Edit Medications button clicked.")
        val selectedMed = currentMedicationsTable.selectionModel.selectedItem   // Get selected item from table
        if (selectedMed != null) {
            println("Edit: ${selectedMed.brandName ?: selectedMed.genericName}")
            try {
                // --- Load the Edit Medication dialog FXML ---
                val fxmlLoader = FXMLLoader(javaClass.getResource("/com/medichart/gui/EditMedicationDialog.fxml")) // <-- CORRECTED LINE
                val dialogRoot = fxmlLoader.load<VBox>() // Load the root element (VBox)
                val dialogController = fxmlLoader.getController<EditMedicationController>() // Get the Edit dialog controller

                val dialogStage = Stage()
                dialogStage.title = "Edit Medication"   // Set the dialog title
                dialogStage.initModality(Modality.WINDOW_MODAL) // Make it modal (blocks input to parent window)
                dialogStage.initOwner(currentMedicationsTable.scene.window) // Set the owner stage for centering
                val dialogScene = Scene(dialogRoot) // Create the scene from the loaded FXML root
                dialogStage.scene = dialogScene

                dialogController.setDialogStage(dialogStage)    // Pass Stage
                dialogController.setMedicationData(selectedMed)

                dialogStage.showAndWait()

                if (dialogController.isSavedSuccessful) {
                    val updatedMedication = dialogController.medicationData
                    if (updatedMedication != null) {
                        dbManager.updateMedication(updatedMedication)
                        loadCurrentMedications()    // Refresh the table to be sure (or rely on ObservableList update)
                        // TODO: Add selection highlighting for the edited item after refresh?
                        println("Medication updated successfully (in memory): ${updatedMedication.brandName ?: updatedMedication.genericName}") // Placeholder print
                    } else {
                        println("Edit dialog closed, but no updated medication data was captured (Save may have failed or not implemented yet).")   // Placeholder print
                    }
                } else {
                    println("Edit Medication dialog cancelled.")    // Placeholder print
                }
            } catch (e: IOException) {
                // Handle potential errors during FXML loading
                System.err.println("Error loading Edit Medication dialog FXML: ${e.message}")
                e.printStackTrace()
                // TODO: Show an error message to the user
                showAlert(AlertType.ERROR, "Error Loading Dialog", "Could not load the edit dialog.", "An error occurred while trying to open the edit window.") // Use showAlert helper
            }
        } else {
            // TODO: Show a warning or information dialog to the user (e.g., using javafx.scene.control.Alert)
            println("No medication selected for editing.")  // Placeholder print
            showAlert(AlertType.INFORMATION, "No Selection", null, "Please select a current medication in the table to edit.") // Use showAlert helper
        }
    }

    /**
     * Handles the action of archiving the selected current medication.
     * Moves the selected medication from the current list to the history list.
     * TODO: Add a dialog to capture ReasonForStopping
     */
    @FXML
    private fun handleArchiveMedication() {
        println("Archive Medication button clicked.")
        val selectedMed = currentMedicationsTable.selectionModel.selectedItem
        if (selectedMed != null) {
            // TODO: Add confirmation dialog before archiving

            val selectedIndex = currentMedicationsTable.selectionModel.selectedIndex // Get index BEFORE refresh

            dbManager.archiveMedication(selectedMed)
            loadCurrentMedications() // Refresh current table
            loadPastMedications() // Refresh history table

            println("Medication archived: ${selectedMed.brandName ?: selectedMed.genericName}")

            // Select the item at the same index in the updated current list, or the last item if index is out of bounds.
            if (currentMedicationsTable.items.isNotEmpty()) {
                val newIndexToSelect = if (selectedIndex < currentMedicationsTable.items.size) selectedIndex else currentMedicationsTable.items.size - 1
                currentMedicationsTable.selectionModel.select(newIndexToSelect)
            } else {
                // If the table is now empty, clear selection
                currentMedicationsTable.selectionModel.clearSelection()
            }
            // TODO: Also consider selecting the newly archived item in the past medications table?
        } else {
            // TODO: Show a warning or information dialog to the user (e.g., using javafx.scene.control.Alert)
            println("No medication selected for archiving.")
            val alert = Alert(AlertType.INFORMATION)
            alert.title = "No Selection"
            alert.headerText = null
            alert.contentText = "Please select a current medication in the table to archive."
            alert.initOwner(currentMedicationsTable.scene.window)
            alert.showAndWait()
        }
    }

    /**
     * Handles the action of deleting the selected current medication.
     * Gets the selected item, confirms deletion, calls database delete, and refreshes table.
     * Selects the item at the same index (or the last) after refresh.
     * EDITED: Added deletion logic and selection logic
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
            if (currentMedicationsTable.items.isNotEmpty()) {
                val newIndexToSelect = if (selectedIndex < currentMedicationsTable.items.size) selectedIndex else currentMedicationsTable.items.size - 1
                currentMedicationsTable.selectionModel.select(newIndexToSelect)
            } else {
                currentMedicationsTable.selectionModel.clearSelection() // Clear selection if table is empty
            }

            println("Current medication deleted.")
        } else {
            println("No medication selected for deletion.")
            // TODO: Show a warning or information dialog to the user (e.g., using javafx.scene.control.Alert)
            val alert = Alert(AlertType.INFORMATION)
            alert.title = "No Selection"
            alert.headerText = null
            alert.contentText = "Please select a current medication in the table to delete."
            alert.initOwner(currentMedicationsTable.scene.window)
            alert.showAndWait()
        }
    }

    /**
     * Handles adding a new medication directly to the archive (Past Medications).
     */
    @FXML
    private fun handleAddPastMedication() {
        println("Add to Archive button clicked. (TODO)")
        try {
            val fxmlLoader = FXMLLoader(javaClass.getResource("/com/medichart/gui/AddPastMedicationDialog.fxml"))
            val dialogRoot = fxmlLoader.load<VBox>()
            val dialogController = fxmlLoader.getController<AddPastMedicationController>()

            val dialogStage = Stage()
            dialogStage.title = "Add Past Medication"
            dialogStage.initModality(Modality.WINDOW_MODAL)
            dialogStage.initOwner(pastMedicationsTable.scene.window)
            val dialogScene = Scene(dialogRoot)
            dialogStage.scene = dialogScene

            dialogController.setupDialog(dialogStage, dbManager)

            dialogStage.showAndWait()

            if (dialogController.isSavedSuccessful) {
                val newPastMedication = dialogController.pastMedicationData

                if (newPastMedication != null) {
                    dbManager.addPastMedication(newPastMedication)
                    loadPastMedications()

                    // Optional: Add selection highlighting for the newly added item after refresh?
                    // This requires retrieving the item from the database after loading, which
                    // might involve finding it by generic name or another unique identifier if ID wasn't set on add.
                    // For simplicity, we just reload and don't auto-select for now.

                    println("New Past Medication added: ${newPastMedication.brandName ?: newPastMedication.genericName}")
                } else {
                    println("Add Past dialog closed, but no past medication data was captured.")
                }
            } else {
                println("Add Past Medication dialog cancelled.")
            }
        } catch (e: IOException) {
            // Handle potential errors during FXML loading (e.g., file not found, FXML syntax error)
            System.err.println("Error loading Add Past Medication dialog FXML: ${e.message}")
            e.printStackTrace() // Print stack trace for debugging
            // Show an error message to the user (using a helper function if available)
            val alert = Alert(AlertType.ERROR)
            alert.title = "Error Loading Dialog"
            alert.headerText = "Could not load the add dialog."
            alert.contentText = "An error occurred while trying to open the add window."
            alert.initOwner(pastMedicationsTable.scene.window)
            alert.showAndWait()
        }
    }


    /**
     * Handles the action of editing the selected past medication.
     * Opens a dialog to edit past medication details.
     * Added 5/3/25
     */
    @FXML
    private fun handleEditPastMedication() {
        println("Edit Past Medication button clicked.")
        val selectedPastMed = pastMedicationsTable.selectionModel.selectedItem
        if (selectedPastMed != null) {
            println("Edit Past: ${selectedPastMed.brandName ?: selectedPastMed.genericName}")
            try {
                // --- Load the Edit Past Medication dialog FXML ---
                val fxmlLoader = FXMLLoader(javaClass.getResource("/com/medichart/gui/EditPastMedicationDialog.fxml"))
                val dialogRoot = fxmlLoader.load<VBox>()
                val dialogController = fxmlLoader.getController<EditPastMedicationController>()

                val dialogStage = Stage()
                dialogStage.title = "Edit Past Medication"
                dialogStage.initModality(Modality.WINDOW_MODAL)
                dialogStage.initOwner(pastMedicationsTable.scene.window)
                val dialogScene = Scene(dialogRoot)

                // Pass the Stage and DatabaseManager to the dialog controller's setup method
                dialogController.setDialog(dialogStage, dbManager)

                // Pass the selected Past Medication data to the dialog controller
                dialogController.setPastMedicationData(selectedPastMed)

                dialogStage.scene = dialogScene
                dialogStage.showAndWait()

                // --- Handle the results after the dialog is closed ---
                // Check if the user clicked Save in the dialog
                if (dialogController.isSavedSuccessful) {
                    val updatedPastMedication = dialogController.pastMedicationData // Get the updated data from the dialog controller

                    // Ensure the updated data was captured from the dialog
                    if (updatedPastMedication != null) {
                        // Call the DB update method for past medications
                        dbManager.updatePastMedication(updatedPastMedication)
                        loadPastMedications()
                        // TODO: Add selection highlighting for the edited item after refresh?
                        println("Past Medication updated successfully: ${updatedPastMedication.brandName ?: updatedPastMedication.genericName}")
                    } else {
                        println("Edit Past dialog closed, but no updated past medication data was captured (Save may have failed).")
                    }
                } else {
                    println("Edit Past Medication dialog cancelled.")
                }
            } catch (e: IOException) {
                System.err.println("Error loading Edit Past Medication dialog FXML: ${e.message}")
                e.printStackTrace()
                // TODO: Show an error message to the user
                val alert = Alert(AlertType.ERROR)
                alert.title = "Error Loading Dialog"
                alert.headerText = "Could not load the edit dialog."
                alert.contentText = "An error occurred while trying to open the edit window."
                alert.initOwner(pastMedicationsTable.scene.window)
                alert.showAndWait()
            }
        } else {
            // TODO: Show a warning or information dialog to he user
            println("No past medication selected for editing.")
            val alert = Alert(AlertType.INFORMATION)
            alert.title = "No Selection"
            alert.headerText = null
            alert.contentText = "Please select a past medication in the table to edit."
            alert.initOwner(pastMedicationsTable.scene.window)
            alert.showAndWait()
        }
    }

    /**
     * Handles the action of unarchiving the selected past medication.
     * Moves the selected medication from past_meds back to current_meds.
     * Selects the item at the original index (or the last) in the *past* table after refresh,
     * keeping focus on the history list.
     * EDITED: Added unarchive logic and selection logic for the past table.
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
            if (pastMedicationsTable.items.isNotEmpty()) {
                val newIndexToSelect = if (selectedIndex < pastMedicationsTable.items.size) selectedIndex else pastMedicationsTable.items.size - 1
                pastMedicationsTable.selectionModel.select(newIndexToSelect)
            } else {
                // If the past table is now empty after unarchiving the last item, clear selection
                pastMedicationsTable.selectionModel.clearSelection()
            }
        } else {
            println("No past medication selected for unarchiving.")
            // TODO: Show warning dialog
            val alert = Alert(AlertType.INFORMATION)
            alert.title = "No Selection"
            alert.headerText = null
            alert.contentText = "Please select a past medication in the table to unarchive."
            alert.initOwner(pastMedicationsTable.scene.window)
            alert.showAndWait()
        }
    }

    /**
     * Handles the action of deleting the selected past medication.
     * Gets the selected item, confirms deletion, calls database delete, and refreshes table.
     * Selects the item at the same index (or the last) after refresh in the past table.
     * EDITED: Added deletion logic and selection logic for the past table.
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
            val alert = Alert(AlertType.INFORMATION)
            alert.title = "No Selection"
            alert.headerText = null
            alert.contentText = "Please select a past medication in the table to delete."
            alert.initOwner(pastMedicationsTable.scene.window)
            alert.showAndWait()
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
        val alert = Alert(AlertType.INFORMATION) // Placeholder
        alert.title = "Add Surgery (TODO)"
        alert.headerText = null
        alert.contentText = "Add Surgery functionality is not yet implemented."
        alert.initOwner(surgeriesTable.scene.window)
        alert.showAndWait()
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
    // @FXML
    @FXML
    private fun handleSortByBrandName() {
        println("Sort by Brand Name button clicked.")
        // Use the items list and sort in memory
        val sortedList = currentMedicationsTable.items.sortedWith(compareBy { it.brandName ?: "" }) // Handle null brand names for sorting
        // Replace the current items with the sorted list
        currentMedicationsTable.items.setAll(sortedList) // setAll clears and adds all, triggering table update
        println("Table sorted by Brand Name.")
    }

    // --- Reporting and Export Methods ---

    // TODO: Implement PDF export

    /**
     * Handles exporting the current medications table to a CSV file.
     * Opens a file chooser dialog for the user to select a save location.
     */
    @FXML
    private fun handleExportCurrentMedsCSV() {
        println("Export Current Medications CSV menu item clicked.")

        val fileChooser = FileChooser()
        fileChooser.title = "Export Current Medications to CSV"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"))
        fileChooser.initialFileName = "current_medications_${LocalDate.now()}.csv"

        val file = fileChooser.showSaveDialog(currentMedicationsTable.scene.window) // Set owner window

        // Check if the user selected a file (didn't cancel)
        if (file != null) {
            val data = currentMedicationsTable.items    // Get the data from the TableView's items list

            if (data.isEmpty()) {
                showAlert(AlertType.INFORMATION, "Export Failed", "No Data to Export", "The Current Medications table is empty. Nothing was exported.")
                return  // Exit the method if no data
            }

            try {
                // Use a BufferedWriter and FileWriter to write to the file efficiently
                // The use{} block ensures the writer and underlying stream are closed automatically.
                BufferedWriter(FileWriter(file)).use { writer ->
                    // Write CSV Header Row based on the columns you want to include.
                    // Define the desired header fields here
                    val header = listOf(
                        "Brand Name", "Generic Name", "Dosage", "Dose Form",
                        "Instructions", "Reason", "Prescriber", "Start Date",
                        "Notes", "Manufacturer"
                    )

                    // Write Data Rows
                    data.forEach { medication ->
                        // Extract data for each column from the Medication object
                        // Use toString() for LocalDate? and handle nulls by passing null to escapeCsvField
                        val rowData = listOf(
                            medication.brandName,
                            medication.genericName, // Non-nullable String
                            medication.dosage,
                            medication.doseForm,
                            medication.instructions,
                            medication.reason,
                            medication.prescriber,
                            medication.startDate?.toString(),   // Convert LocalDate? to String?
                            medication.notes,
                            medication.manufacturer
                        )
                        // Join row data with commas, escape fields, and write the data line
                        writer.write(rowData.joinToString(",") {field -> escapeCsvField(field) })
                        writer.newLine()    // Write a newLine character
                    }

                    println("Current Medications dat exported successfully to ${file.absolutePath}")
                    showAlert(AlertType.INFORMATION, "Export Successful", "Export Complete", "Current Medications data has been successfully exported to:\n${file.absolutePath}")

                }
            } catch (e: IOException) {
                System.err.println("Error writing CSV file: ${e.message}")
                e.printStackTrace()
                showAlert(AlertType.ERROR, "Export Failed", "Error Writing File", "An error occurred while writing the CSV file:\n${e.message}")
            } catch (e: Exception) {
                System.err.println("An unexpected error occurred during CSV export: ${e.message}")
                e.printStackTrace()
                showAlert(AlertType.ERROR, "Export Failed", "Unexpected Error", "An unexpected error occurred during the export process:\n${e.message}")
            }
        } else {
            println("CSV export cancelled by user.")
        }
    }

    /**
     * Handles exporting the current medications table data to a PDF file using OpenPDF.
     * Implements a table layout with basic pagination handling by the library.
     * EDITED: Implementation using OpenPDF.
     */
    @FXML
    private fun handleExportCurrentMedsPDF() {
        println("Export Current Meds to PDF menu item clicked (OpenPDF - Enhanced List Format.")

        val data = currentMedicationsTable.items

        if (data.isEmpty()) {
            showAlert(AlertType.INFORMATION, "Export Failed", "No Data to Export", "The Current Medications table is empty. Nothing was exported.")
            return
        }

        val fileChooser = FileChooser()
        fileChooser.title = "Export Current Medications to PDF"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"))
        fileChooser.initialFileName = "current_medications_${LocalDate.now()}_formatted_list.pdf" // Changed default filename

        val file = fileChooser.showSaveDialog(currentMedicationsTable.scene.window)

        if (file != null) {
            var document: Document? = null

            try {
                document = Document()
                PdfWriter.getInstance(document, file.outputStream())
                document.open()

                val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16f) // Slightly larger title
                val sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f) // Font for labels like "Instructions:", "Reason:"
                val normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10f) // Font for data values
                val italicFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10f) // Example italic font for Brand Name

                val title = Paragraph("Current Medications List", titleFont).apply {
                    alignment = Element.ALIGN_CENTER
                    spacingAfter = 25f
                }
                document.add(title)

                data.forEachIndexed { index, medication ->
                    if (index > 0) {
                        // val separator = Paragraph("---", normalFont).apply {
                        //     alignment = Element.ALIGN_CENTER
                        //     spacingBefore = 15f
                        //     spacingAfter = 15f
                        // }
                        // document.add(separator)

                        // Alternative: Add a horizontal line (requires different OpenPDF elements/imports)
                        val line = LineSeparator()
                        line.setOffset(5f)
                        document.add(Chunk(line))
                    }

                    val mainInfoPhrase = Phrase().apply {
                        add(Chunk(medication.genericName ?: "N/A", normalFont))
                        if (!medication.brandName.isNullOrEmpty()) {
                            add(Chunk(" (${medication.brandName})", italicFont))
                        }
                        if (!medication.dosage.isNullOrEmpty()) {
                            add(Chunk(" - ${medication.dosage}", normalFont))
                        }
                        if (!medication.doseForm.isNullOrEmpty()) {
                            add(Chunk(" ${medication.doseForm}", normalFont))
                        }
                    }
                    val mainInfoParagraph = Paragraph(mainInfoPhrase).apply {
                        spacingAfter = 5f
                    }
                    document.add(mainInfoParagraph)

                    val instructionsParagraph = Paragraph().apply {
                        add(Chunk("Instructions: ", sectionFont))
                        add(Chunk(medication.instructions ?: "M/A", normalFont))
                        spacingAfter = 3f
                    }
                    document.add(instructionsParagraph)

                    val reasonParagraph = Paragraph().apply {
                        add(Chunk("Reason taking: ", sectionFont))
                        add(Chunk(medication.reason ?: "N/A", normalFont))
                        spacingAfter = 5f
                    }
                    document.add(reasonParagraph)
                }
                document.close()
                println("Current Medications data exported successfully to PDF: ${file.absolutePath}")
                showAlert(AlertType.INFORMATION, "Export Successful", "Export Complete", "Current Medications data has been successfully exported to:\n${file.absolutePath}")
            } catch (e: DocumentException) {
                System.err.println("PDF Document error during export: ${e.message}")
                e.printStackTrace()
                showAlert(AlertType.ERROR, "Export Failed", "Document Error", "A PDF document error occurred during export:\n${e.message}")
            } catch (e: IOException) {
                System.err.println("Error writing PDF file: ${e.message}")
                e.printStackTrace()
                showAlert(AlertType.ERROR, "Export Failed", "Error Writing File", "An error occurred while writing the PDF file:\n${e.message}")
            } catch (e: Exception) {
                System.err.println("An unexpected error occurred during PDF export: ${e.message}")
                e.printStackTrace()
                showAlert(AlertType.ERROR, "Export Failed", "Unexpected Error", "An unexpected error occurred during the export process:\n${e.message}")
            } finally {
                if (document != null && document.isOpen) {
                    try {
                        document.close()
                    } catch (e: Exception) {
                        System.err.println("Error closing PDF document in finally block: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        } else {
            println("PDF export cancelled by user.")
        }
    }

    // (Old, basic PDF exportation method)
    /*
    @FXML // Add @FXML annotation
    private fun handleExportCurrentMedsPDF() { // Method name remains the same
        println("Export Current Meds to PDF menu item clicked (OpenPDF - List Format).")

        val data = currentMedicationsTable.items // Get the ObservableList of Medication objects

        if (data.isEmpty()) {
            showAlert(AlertType.INFORMATION, "Export Failed", "No Data to Export", "The Current Medications table is empty. Nothing was exported.")
            return
        }

        // Use FileChooser to get a save location from the user (already implemented)
        val fileChooser = FileChooser()
        fileChooser.title = "Export Current Medications to PDF"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"))
        fileChooser.initialFileName = "current_medications_${LocalDate.now()}_list.pdf" // Changed default filename slightly

        val file = fileChooser.showSaveDialog(currentMedicationsTable.scene.window) // Set owner window

        // Check if the user selected a file (didn't cancel)
        if (file != null) {
            // --- OpenPDF Implementation (List Format) ---
            val document = Document() // Create a new document instance

            try {
                // Get a PdfWriter instance to write the document content to the chosen file
                // Using file.outputStream() is a common way to get an OutputStream from a File
                PdfWriter.getInstance(document, file.outputStream())

                // Open the document for writing.
                // Margins can be set here: Document(marginLeft, marginRight, marginTop, marginBottom)
                // Default is 36 points (0.5 inch) margin on all sides if not specified.
                // Example with custom margins (1 inch on all sides):
                // val oneInch = 72f // 72 points per inch
                // val document = Document(oneInch, oneInch, oneInch, oneInch)
                document.open() // Open the document with default margins (or custom if specified above)

                // Define basic fonts using FontFactory (simpler than manual font handling)
                val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f)
                val normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10f)
                val boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f) // Use bold font for labels if desired

                // Add a title paragraph to the document
                val title = Paragraph("Current Medications List", titleFont)
                title.spacingAfter = 20f // Add vertical space after the title (in points)
                document.add(title) // Add the title paragraph to the document

                // Add each medication's details in a list format
                data.forEachIndexed { index, medication ->
                    // Add spacing before each medication entry, except the first one
                    // This helps visually separate entries
                    if (index > 0) {
                        document.add(Paragraph("", normalFont).apply { spacingAfter = 10f }) // Add empty paragraph with space
                    }

                    // Line 1: Generic (Brand) Dosage Dose Form
                    val line1Text = "${medication.genericName} (${medication.brandName ?: ""}) ${medication.dosage ?: ""} ${medication.doseForm ?: ""}"
                    // Create a paragraph for this line and add it to the document
                    document.add(Paragraph(line1Text, normalFont))


                    // Line 2: Instructions and Reason taking
                    // Using Chunk to mix styles or add specific spacing/elements if needed,
                    // but simple string concatenation with spaces is fine for basic layout.
                    val instructionsReasonText = buildString {
                        append("Instructions: ")
                        append(medication.instructions ?: "N/A")
                        append("    Reason taking: ") // Using multiple spaces for separation
                        append(medication.reason ?: "N/A")
                    }
                    // Create a paragraph for this line and add it
                    document.add(Paragraph(instructionsReasonText, normalFont))


                    // Line 3: Prescriber, Start Date, Manufacturer
                    val line3Text = buildString {
                        append("Prescriber: ")
                        append(medication.prescriber ?: "N/A")
                    }
                    // Create a paragraph for this line and add it
                    document.add(Paragraph(line3Text, normalFont))


                    // Add Notes as a separate line if available and not empty
                    if (!medication.notes.isNullOrEmpty()) {
                        document.add(Paragraph("Notes: ${medication.notes}", normalFont))
                    }

                    // Add vertical space after the entire medication entry
                    val entryEndSpace = Paragraph("", normalFont).apply { spacingAfter = 15f } // Space after each entry
                    document.add(entryEndSpace)

                    // OpenPDF automatically handles pagination when you add content (Paragraphs, etc.)
                    // If adding a Paragraph causes it to exceed the page height, OpenPDF will
                    // automatically start a new page for the remaining content and add the rest there.
                }

                document.close() // Close the document (CRUCIAL!) - Finalizes the PDF file

                println("Current Medications data exported successfully to PDF: ${file.absolutePath}")

                // Show success message alert
                showAlert(AlertType.INFORMATION, "Export Successful", "Export Complete", "Current Medications data has been successfully exported to:\n${file.absolutePath}")

            } catch (e: DocumentException) {
                // Handle OpenPDF/iText document-specific exceptions (e.g., issues adding content)
                System.err.println("PDF Document error during export: ${e.message}")
                e.printStackTrace()
                showAlert(AlertType.ERROR, "Export Failed", "Document Error", "A PDF document error occurred during export:\n${e.message}")
            } catch (e: IOException) {
                // Handle file writing errors (e.g., permission issues, disk full)
                System.err.println("Error writing PDF file: ${e.message}")
                e.printStackTrace()
                showAlert(AlertType.ERROR, "Export Failed", "Error Writing File", "An error occurred while writing the PDF file:\n${e.message}")
            } catch (e: Exception) {
                // Catch any other unexpected exceptions during the process
                System.err.println("An unexpected error occurred during PDF export: ${e.message}")
                e.printStackTrace()
                showAlert(AlertType.ERROR, "Export Failed", "Unexpected Error", "An unexpected error occurred during the export process:\n${e.message}")
            } finally {
                // Ensure the document is closed even if an exception occurs *before* the final document.close() call.
                // Check if the document object was successfully assigned (is not null) AND if it is open before trying to close.
                if (document != null && document.isOpen) { // <-- USE THIS CHECK
                    try {
                        document.close() // Attempt to close the document
                    } catch (e: Exception) {
                        // Handle potential errors during the closing process itself within the finally block
                        System.err.println("Error closing PDF document in finally block: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
            // --- End OpenPDF Implementation (List Format) ---
        } else {
            // This block executes if the user cancelled the file chooser dialog
            println("PDF export cancelled by user.")
        }
    }
    */



    /**
     * Handles exporting the past medications table data to a CSV file.
     */
    @FXML
    private fun handleExportPastMedsCSV() {
        println("Export Current Medications CSV menu item clicked.")

        val fileChooser = FileChooser()
        fileChooser.title = "Export Past Medications to CSV"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"))
        fileChooser.initialFileName = "past_medications_${LocalDate.now()}.csv"

        val file = fileChooser.showSaveDialog(currentMedicationsTable.scene.window) // Set owner window

        // Check if the user selected a file (didn't cancel)
        if (file != null) {
            val data = pastMedicationsTable.items    // Get the data from the TableView's items list

            if (data.isEmpty()) {
                showAlert(AlertType.INFORMATION, "Export Failed", "No Data to Export", "The Past Medications table is empty. Nothing was exported.")
                return  // Exit the method if no data
            }

            try {
                // Use a BufferedWriter and FileWriter to write to the file efficiently
                // The use{} block ensures the writer and underlying stream are closed automatically.
                BufferedWriter(FileWriter(file)).use { writer ->
                    // Write CSV Header Row based on the columns you want to include.
                    // Define the desired header fields here
                    val header = listOf(
                        "Brand Name", "Generic Name", "Dosage", "Dose Form",
                        "Instructions", "Reason Taken", "Prescriber", "History Notes",
                        "Reason Stopped", "Date Range(s)", "Manufacturer"
                    )
                    writer.write(header.joinToString(",") { field -> escapeCsvField(field) })
                    writer.newLine()

                    // Write Data Rows
                    data.forEach { medication ->
                        // Extract data for each column from the Medication object
                        // Use toString() for LocalDate? and handle nulls by passing null to escapeCsvField
                        val rowData = listOf(
                            medication.brandName,
                            medication.genericName, // Non-nullable String
                            medication.dosage,
                            medication.doseForm,
                            medication.instructions,
                            medication.reason,
                            medication.prescriber,
                            medication.historyNotes,
                            medication.reasonForStopping,
                            dbManager?.serializeDateRanges(medication.dateRanges),
                            medication.manufacturer
                        )
                        // Join row data with commas, escape fields, and write the data line
                        writer.write(rowData.joinToString(",") {field -> escapeCsvField(field) })
                        writer.newLine()    // Write a newLine character
                    }

                    println("Past Medications data exported successfully to ${file.absolutePath}")
                    showAlert(AlertType.INFORMATION, "Export Successful", "Export Complete", "Past Medications data has been successfully exported to:\n${file.absolutePath}")

                }
            } catch (e: IOException) {
                System.err.println("Error writing CSV file: ${e.message}")
                e.printStackTrace()
                showAlert(AlertType.ERROR, "Export Failed", "Error Writing File", "An error occurred while writing the CSV file:\n${e.message}")
            } catch (e: Exception) {
                System.err.println("An unexpected error occurred during CSV export: ${e.message}")
                e.printStackTrace()
                showAlert(AlertType.ERROR, "Export Failed", "Unexpected Error", "An unexpected error occurred during the export process:\n${e.message}")
            }
        } else {
            println("CSV export cancelled by user.")
        }
    }

    /**
     * Helper function to properly escape a string field for CSV writing
     * Handles fields containing commas, double quotes, or newlines by enclosing them in double quotes.
     * Double quotes within the field are escaped by doubling them (" becomes "").
     * Handles null input values by treating them as empty strings.
     * @param field The string value to escape. Can be null.
     * @return The escaped string suitable for CSV
     */
    private fun escapeCsvField(field: String?): String {
        val value = field ?: "" // Treat null values as empty strings for export
        // Check if the field needs to be quoted. A field should be quoted if it contains:
        // 1. A comma (,)
        // 2. A double quote (")
        // 3. A newline character (\n)
        // 4. Sometimes leading/trailing whitespace is reason to quote, but often optional. Let's keep it simple.
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            val escapedValue = value.replace("\"", "\"\"")  // Escape any double quotes *within* the value by doubling them (" becomes "")
            return "\"$escapedValue\""  // Enclose the entire value in double quotes
        }
        return value // If the field does not contain any characters requiring quoting, return the value as is.
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

// *********************************************************************************************************************

/**
 * Sets up a TableColumn<Medication, String> for inline editing using TextFieldTableCell.
 * Applies a DefaultStringConverter and handles updating the Medication object in the TableView's items list.
 * This function is an extension on TableColumn<Medication, String>.
 *
 * @param tableView The TableView that this column belongs to. Needed to access and update the list items.
 * @param updateItem The lambda that defines how to create a new Medication object with the updated value.
 * It takes the original Medication object (oldItem: Medication) and the new String value (newValue: String?)
 * as parameters and should return the updated Medication object.
 * IMPLEMENTED: Call to dbManager.updateMedication() to save changes to the database.
 */
fun TableColumn<Medication, String>.setupStringInLineEditing(
    tableView: TableView<Medication>,   // Need access to the TableView to update its items
    dbManager: DatabaseManager,
    updateItem: (oldItem: Medication, newValue: String) -> Medication  // Lambda for creating the updated item
) {
    // Set the cell factory to use TextFieldTableCell for String columns
    this.cellFactory = TextFieldTableCell.forTableColumn(DefaultStringConverter())  // 'this' refers to the TableColumn

    // Set the onEditCommit handler
    this.onEditCommit = EventHandler { event: CellEditEvent<Medication, String> ->   // 'this' refers to the TableColumn
        val item = event.rowValue   // The original Medication object being edited
        val newValue = event.newValue   // The new String value from the editor

        // Find the index of the item in the table's observable list
        // Using indexOf requires the data class to have correct equals() and hashCode() which Kotlin data classes provide.
        val itemIndex = tableView.items.indexOf(item)   // Use the provided TableView

        if (itemIndex >= 0) {
            // Use the provided lambda to create the updated item
            val updatedItem = updateItem(item, newValue)
            // Replace the old item with the new one in the table's ObservableList
            tableView.items[itemIndex] = updatedItem
            // IMPLEMENTED: Call the database update method to save the change persistently
            dbManager.updateMedication(updatedItem)
            println("Inline edit committed and database updated for ID ${updatedItem.id}.")
        }
    }
}

/**
 * Sets up a TableColumn<PastMedication, String> for inline editing using TextFieldTableCell.
 * Applies a DefaultStringConverter and handles updating the PastMedication object in the TableView's items list.
 * This function is an extension on TableColumn<PastMedication, String>.
 *
 * @param tableView The TableView that this column belongs to. Needed to access and update the list items.
 * @param dbManager The DatabaseManager instance to save the updated item.
 * @param updateItem The lambda that defines how to create a new PastMedication object with the updated value.
 * It takes the original PastMedication object (oldItem: PastMedication) and the new String value (newValue: String?)
 * as parameters and should return the updated PastMedication object.
 * IMPLEMENTED: Call to dbManager.updatePastMedication() to save changes to the database.
 */
@JvmName("setupPastMedicationStringInLineEditing")
fun TableColumn<PastMedication, String>.setupStringInLineEditing(
    tableView: TableView<PastMedication>,
    dbManager: DatabaseManager,
    updateItem: (oldItem: PastMedication, newValue: String) -> PastMedication
) {
    this.cellFactory = TextFieldTableCell.forTableColumn(DefaultStringConverter())

    this.onEditCommit = EventHandler { event: CellEditEvent<PastMedication, String> ->
        val item = event.rowValue
        val newValue = event.newValue

        val itemIndex = tableView.items.indexOf(item)

        if (itemIndex >= 0 ) {
            val updatedItem = updateItem(item, newValue)

            // Update the item in the TableView's ObservableList first for immediate visual feedback
            tableView.items[itemIndex] = updatedItem

            // Call the database update method to save the change persistently
            dbManager.updatePastMedication(updatedItem)
            println("Inline edit commited and database updated for PastMedication ID ${updatedItem.id}.")   // Optional: for debugging
        }
    }
}

// TODO: Add other extension functions for different cell types (TextArea, DatePicker) here later.
// Example for DatePickerTableCell (requires implementing a custom cell factory):
// fun TableColumn<Medication, LocalDate>.setupDatePickerInLineEditing(
//     tableView: TableView<Medication>,
//     dbManager: DatabaseManager,
//     updateItem: (oldItem: Medication, newValue: LocalDate?) -> Medication
// ) {
//     this.cellFactory = Callback { DatePickerTableCell() } // Need a custom cell factory class or lambda
//
//     this.onEditCommit = EventHandler { event: CellEditEvent<Medication, LocalDate> ->
//         val item = event.rowValue
//         val newValue = event.newValue // This would be a LocalDate or null
//
//         val itemIndex = tableView.items.indexOf(item)
//
//         if (itemIndex >= 0) {
//             val updatedItem = updateItem(item, newValue)
//             tableView.items[itemIndex] = updatedItem
//             dbManager.updateMedication(updatedItem)
//             println("Inline date edit committed and database updated for Medication ID ${updatedItem.id}.")
//         }
//     }
// }
// Need a similar one for PastMedication if date fields are editable inline.


// Example for TextAreaTableCell (requires implementing a custom cell factory):
// fun TableColumn<Medication, String>.setupTextAreaInLineEditing(
//     tableView: TableView<Medication>,
//     dbManager: DatabaseManager,
//     updateItem: (oldItem: Medication, newValue: String) -> Medication
// ) {
//    // Need a custom cell factory that returns a TableCell containing a TextArea
//    this.cellFactory = Callback { TextAreaTableCell<Medication, String>() } // Need a custom cell factory class or lambda
//    // onEditCommit handler would be similar to String editing
//    this.onEditCommit = EventHandler { event: CellEditEvent<Medication, String> ->
//         val item = event.rowValue
//         val newValue = event.newValue
//
//         val itemIndex = tableView.items.indexOf(item)
//
//         if (itemIndex >= 0) {
//             val updatedItem = updateItem(item, newValue)
//             tableView.items[itemIndex] = updatedItem
//             dbManager.updateMedication(updatedItem)
//             println("Inline text area edit committed and database updated for Medication ID ${updatedItem.id}.")
//         }
//    }
// }
// Need a similar one for PastMedication historyNotes.