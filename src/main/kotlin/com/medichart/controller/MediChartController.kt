package com.medichart.controller

import com.medichart.database.DatabaseManager
import com.medichart.model.Medication
import com.medichart.model.PastMedication
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

    // Past Medications Tab Buttons (added 5/3/25)
    @FXML lateinit var addPastMedicationButton: Button
    @FXML lateinit var editPastMedicationButton: Button
    @FXML lateinit var unarchiveButton: Button
    @FXML lateinit var deletePastMedicationButton: Button

    // Other potential FXML elements (added 5/3/25)
    // @FXML lateinit var sortBrandNameButton: Button // Add this if you added fx:id="sortBrandNameButton"
    // @FXML lateinit var printCurrentMedsButton: Button // Add this if you added fx:id
    // @FXML lateinit var exportCurrentMedsPdfButton: Button // Add this if you added fx:id
    // @FXML lateinit var exportCurrentMedsWordButton: Button // Add this if you added fx:id
    // @FXML lateinit var addSurgeryButton: Button // Add this if you added fx:id

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
        dbManager.createTables()    // Ensure tables exist on startup

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

        // --- ENABLE INLINE EDITING FOR CURRENT MEDICATIONS TABLE ---
        currentMedicationsTable.isEditable = true   // Enable inline editing for Current Medications Table

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

        // Set up inline editing using the setupStringInLineEditing extension function for current medications
        // This configures the cell factory and the onEditCommit handler for each column.
        // The lambda defines how to create a new Medication object using copy().
        // Database update is left as a TODO for a separate "Save" feature.

        // Generic Name
        currentGenericNameColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue ->
            item.copy(genericName = newValue)   // Define how to update genericName
        }
        // Brand Name
        currentBrandNameColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue ->
            item.copy(brandName = newValue.takeIf { it.isNotEmpty () })   // Define how to update brandName
        }
        // Dosage
        currentDosageColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue ->
            item.copy(dosage = newValue.takeIf { it.isNotEmpty() })   // Define how to update dosage
        }
        // Dosage From
        currentDoseFormColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue ->
            item.copy(doseForm = newValue.takeIf { it.isNotEmpty() })   // Define how to update doseForm
        }
        // Instructions
        currentInstructionsColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue ->
            item.copy(instructions = newValue.takeIf { it.isNotEmpty() })   // Define how to update instructions
        }
        // Manufacturer
        currentManufacturerColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue ->
            item.copy(manufacturer = newValue.takeIf { it.isNotEmpty() })   // Define how to update manufacturer
        }
        // Reason
        currentReasonColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue ->
            item.copy(reason = newValue.takeIf { it.isNotEmpty() })   // Define how to update reason
        }
        // Prescriber
        currentPrescriberColumn.setupStringInLineEditing(currentMedicationsTable, dbManager) { item, newValue ->
            item.copy(prescriber = newValue.takeIf { it.isNotEmpty() })   // Define how to update prescriber
        }

        // TODO: (Future) Apply Custom Cell Factory for Word Wrapping to Notes columns.

        // --- ENABLE INLINE EDITING FOR PAST MEDICATIONS TABLE ---
        pastMedicationsTable.isEditable = true   // Enable inline editing for Past Medications Table

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

        // Set up inline editing using the setupStringInLineEditing extension function for past medications
        // This configures the cell factory and the onEditCommit handler for each column.
        // The lambda defines how to create a new Medication object using copy().
        // Database update is left as a TODO for a separate "Save" feature.

        // Generic Name
        pastGenericNameColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue ->
            item.copy(genericName = newValue)   // Define how to update genericName
        }
        // Brand Name
        pastBrandNameColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue ->
            item.copy(brandName = newValue.takeIf { it.isNotEmpty () })   // Define how to update brandName
        }
        // Dosage
        pastDosageColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue ->
            item.copy(dosage = newValue.takeIf { it.isNotEmpty() })   // Define how to update dosage
        }
        // Dosage From
        pastDoseFormColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue ->
            item.copy(doseForm = newValue.takeIf { it.isNotEmpty() })   // Define how to update doseForm
        }
        // Instructions
        pastInstructionsColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue ->
            item.copy(instructions = newValue.takeIf { it.isNotEmpty() })   // Define how to update instructions
        }
        // Manufacturer
        pastManufacturerColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue ->
            item.copy(manufacturer = newValue.takeIf { it.isNotEmpty() })   // Define how to update manufacturer
        }
        // Reason
        pastReasonColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue ->
            item.copy(reason = newValue.takeIf { it.isNotEmpty() })   // Define how to update reason
        }
        // Prescriber
        pastPrescriberColumn.setupStringInLineEditing(pastMedicationsTable, dbManager) { item, newValue ->
            item.copy(prescriber = newValue.takeIf { it.isNotEmpty() })   // Define how to update prescriber
        }

        // TODO: Add inline editing for past History Notes (TextArea) and Date Ranges (custom cell/dialog)

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
     * EDITED: Added dialog loading/showing logic, getting data from dialog controller, and selection logic.
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

                // Get the controller for the dialog
                val dialogController = fxmlLoader.getController<EditMedicationController>() // Get the Edit dialog controller

                // Create a new stage for the dialog window
                val dialogStage = Stage()
                dialogStage.title = "Edit Medication"   // Set the dialog title
                dialogStage.initModality(Modality.WINDOW_MODAL) // Make it modal (blocks input to parent window)
                dialogStage.initOwner(currentMedicationsTable.scene.window) // Set the owner stage for centering
                val dialogScene = Scene(dialogRoot) // Create the scene from the loaded FXML root

                // Pass the Stage reference to the dialog controller (for closing the dialog etc.)
                dialogController.setDialogStage(dialogStage)    // Pass Stage

                // --- Pass the selected Medication data to the dialog controller ---
                dialogController.setMedicationData(selectedMed)

                dialogStage.scene = dialogScene
                dialogStage.showAndWait()

                // --- Handle the results after the dialog is closed ---
                // This part is similar to handleAddMedication, but handles saving/updating the item.
                // Check if the user clicked Save in the dialog
                if (dialogController.isSavedSuccessful) {
                    // Get the updated data from the dialog controller (This assumes Save button in dialog
                    // captured the data and put it into dialogController.medicationData)
                    val updatedMedication = dialogController.medicationData

                    // Ensure the updated data was captured from the dialog
                    if (updatedMedication != null) {
                        // TODO: Implement the database update method in DatabaseManager first!
                        // Call the database update method with the updated medication object
                        dbManager.updateMedication(updatedMedication)

                        // For now, since DB update isn't implemented, just update the item in the TableView's list
                        // Find the index of the original item in the list and replace it with the updated one
                        // val index = currentMedicationsTable.items.indexOf(selectedMed)  // Use the original object to find its position
                        // if (index >= 0) {
                        //     currentMedicationsTable.items[index] = updatedMedication // Replace the item in the list
                        //     // The TableView should update automatically because its item list is Observable.
                        // }

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
            }
        } else {
            // TODO: Show a warning or information dialog to the user (e.g., using javafx.scene.control.Alert)
            println("No medication selected for editing.")  // Placeholder print
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
                        println("Edit Past dialog cclosed, but no updated past medication data was captured (Save may have failed).")
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
     * Handles adding a new medication directly to the archive (Past Medications).
     * TODO: Implement Add Past Medication dialog
     */
    @FXML
    private fun handleAddPastMedication() {
        println("Add to Archive button clicked. (TODO)")
        val alert = Alert(AlertType.INFORMATION) // Placeholder
        alert.title = "Add Past Medication (TODO)"
        alert.headerText = null
        alert.contentText = "Add Past Medication functionality is not yet implemented."
        alert.initOwner(pastMedicationsTable.scene.window)
        alert.showAndWait()
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

    // --- Reporting and Export Methods (Placeholder) ---

    /**
     * Handles printing the current medications table.
     * (Implementation needed - requires external libraries/JavaFX Printing API)
     */
    @FXML
    private fun handlePrintCurrentMeds() {
        println("Print Current Medications clicked (Implementation needed)")
        val alert = Alert(AlertType.INFORMATION) // Placeholder
        alert.title = "Print (TODO)"
        alert.headerText = null
        alert.contentText = "Print functionality is not yet implemented."
        alert.initOwner(currentMedicationsTable.scene.window)
        alert.showAndWait()
    }
    // TODO: Implement printing logic (requires JavaFX Printing API or a library)

    /**
     * Handles exporting the current medications table to PDF.
     * (Implementation needed - requires a PDF library like iText or PDFBox)
     */
    @FXML
    private fun handleExportCurrentMedsPDF() {
        println("Export Current Medications PDF clicked (Implementation needed)")
        val alert = Alert(AlertType.INFORMATION) // Placeholder
        alert.title = "Export PDF (TODO)"
        alert.headerText = null
        alert.contentText = "Export to PDF functionality is not yet implemented."
        alert.initOwner(currentMedicationsTable.scene.window)
        alert.showAndWait()
    }
    // TODO: Implement PDF export logic (requires a library like iText, Apache PDFBox)

    /**
     * Handles exporting the current medication table to Word.
     * (Implementation needed - requires a library like Apache POI)
     */
    @FXML
    private fun handleExportCurrentMedsWord() {
        println("Export Current Medications Word clicked (Implementation needed)")
        val alert = Alert(AlertType.INFORMATION) // Placeholder
        alert.title = "Export Word (TODO)"
        alert.headerText = null
        alert.contentText = "Export to Word functionality is not yet implemented."
        alert.initOwner(currentMedicationsTable.scene.window)
        alert.showAndWait()
    }
    // TODO: Implement Word export logic (requires a library like Apache POI)
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