package com.medichart.controller

import com.medichart.database.DatabaseManager
import com.medichart.model.Medication
import com.medichart.model.PastMedication
import com.medichart.model.PastMedication.DateRange
import com.medichart.model.Surgery
import com.medichart.model.Physician
import com.medichart.controller.AddPhysicianController
import com.medichart.controller.EditPhysicianController
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
import javafx.beans.property.Property
import javafx.scene.Parent
import javafx.scene.control.ButtonType
import kotlin.Pair

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

    @FXML lateinit var addMedicationButton: Button
    @FXML lateinit var editMedicationButton: Button
    @FXML lateinit var archiveMedicationButton: Button
    @FXML lateinit var deleteCurrentMedicationButton: Button

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

    @FXML lateinit var addPastMedicationButton: Button
    @FXML lateinit var editPastMedicationButton: Button
    @FXML lateinit var unarchiveButton: Button
    @FXML lateinit var deletePastMedicationButton: Button

    // FXML elements for the Surgeries Table
    @FXML lateinit var surgeriesTable: TableView<Surgery>
    @FXML lateinit var surgeryNameColumn: TableColumn<Surgery, String>
    @FXML lateinit var surgeryDateColumn: TableColumn<Surgery, String>
    @FXML lateinit var surgerySurgeonColumn: TableColumn<Surgery, String>

    @FXML lateinit var addSurgeryButton: Button
    @FXML lateinit var editSurgeryButton: Button
    @FXML lateinit var deleteSurgeryButton: Button

    // FXML elements for the Physicians Table
    @FXML lateinit var physiciansTable: TableView<Physician>
    @FXML lateinit var physicianNameColumn: TableColumn<Physician, String>
    @FXML lateinit var physicianSpecialtyColumn: TableColumn<Physician, String?>
    @FXML lateinit var physicianPhoneColumn: TableColumn<Physician, String?>
    @FXML lateinit var physicianFaxColumn: TableColumn<Physician, String?>
    @FXML lateinit var physicianEmailColumn: TableColumn<Physician, String?>
    @FXML lateinit var physicianAddressColumn: TableColumn<Physician, String?>
    @FXML lateinit var physicianNotesColumn: TableColumn<Physician, String?>

    @FXML lateinit var addPhysicianButton: Button
    @FXML lateinit var editPhysicianButton: Button
    @FXML lateinit var deletePhysicianButton: Button

    private lateinit var currentMedicationsData: ObservableList<Medication>
    private lateinit var pastMedicationsData: ObservableList<PastMedication>
    private lateinit var surgeriesData: ObservableList<Surgery>
    private lateinit var physiciansData: ObservableList<Physician>

    /**
     * This method is called by the application entry point (MediChartApp)
     * AFTER the FXML has been loaded and initialize() has been called.
     * It's used to inject dependencies and perform setup that requires them.
     * @param dbManager The initialized DatabaseManager instance.
     */
    fun setupDependencies(dbManager: DatabaseManager) { // ADD THIS NEW METHOD
        this.dbManager = dbManager // Initialize the lateinit var here

        // Load data into tables after dbManager is set up
        loadCurrentMedications()
        loadPastMedications()
        loadSurgeries()
        loadPhysicians()
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

        pastDateRangesColumn.cellValueFactory = PropertyValueFactory("dateRanges")
        pastDateRangesColumn.cellFactory = Callback { DateRangeTableCell() }

        // TODO: (Future) Apply Custom Cell Factory for Word Wrapping to Past Meds History Notes column
        // TODO: (Future) Implement custom cell factory for pastDateRangesColumn to format/edit the List<DateRange>

        // Set up Surgeries Table Columns
        surgeryNameColumn.cellValueFactory = PropertyValueFactory("name")
        surgeryDateColumn.cellValueFactory = PropertyValueFactory("date")
        surgerySurgeonColumn.cellValueFactory = PropertyValueFactory("surgeon")

        // TODO: Optional: Implement custom cell factories for Date column formatting if needed (Task A.2)
        // TODO: (Future) Add inline editing for Surgeries table

        // Set up cell value factories for the Physicians TableView columns
        physicianNameColumn.cellValueFactory = PropertyValueFactory("name")
        physicianSpecialtyColumn.cellValueFactory = PropertyValueFactory("specialty")
        physicianPhoneColumn.cellValueFactory = PropertyValueFactory("phone")
        physicianFaxColumn.cellValueFactory = PropertyValueFactory("fax")
        physicianEmailColumn.cellValueFactory = PropertyValueFactory("email")
        physicianAddressColumn.cellValueFactory = PropertyValueFactory("address")
        physicianNotesColumn.cellValueFactory = PropertyValueFactory("notes")

        // TODO: Optional: Implement custom cell factories for word wrap on Notes or other formatting if needed later (Task A.2)
        // TODO: Optional: Implement custom cell factories for clickable links (email, phone, address) if needed later

        currentMedicationsTable.isEditable = false
        pastMedicationsTable.isEditable = false
        surgeriesTable.isEditable = false
        physiciansTable.isEditable = false

        // Double-click to open edit medication dialog for current medications table
        currentMedicationsTable.addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
            if (event.clickCount == 2) {
                val selectedMed = currentMedicationsTable.selectionModel.selectedItem
                if (selectedMed != null) { // Ensure it's a click on a cell within a column
                    event.consume()
                    handleEditMedication()
                }
            }
            else if (currentMedicationsTable.isEditable && !event.isConsumed) {
                val cell = event.target as? TableCell<*, *>
                if (cell != null && cell.tableColumn != null) {
                    if (event.clickCount == 1) {
                        if (cell.tableColumn.isEditable) {
                            if (cell is TextFieldTableCell<*, *>) {
                                event.consume()
                            }
                        }
                    }
                }
            }
        }

        // --- Enforce Double-Click for Editing (Past Meds) ---
        pastMedicationsTable.addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
            if (event.clickCount == 2) {
                val selectedMed = pastMedicationsTable.selectionModel.selectedItem
                if (selectedMed != null) {
                    event.consume()
                    handleEditMedication()
                }
            }
            else if (pastMedicationsTable.isEditable && !event.isConsumed) {
                val cell = event.target as? TableCell<*, *>
                if (cell != null && cell.tableColumn != null) {
                    if (event.clickCount == 1) {
                        if (cell.tableColumn.isEditable) {
                            // If it's a text cell and editable, consume the single click to prevent immediate edit
                            if (cell is TextFieldTableCell<*, *>) {
                                event.consume()
                            }
                        }
                    }
                }
            }
        }

        surgeriesTable.addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
            if (event.clickCount == 2) {
                val selectedSurgery = surgeriesTable.selectionModel.selectedItem
                if (selectedSurgery != null) {
                    event.consume()
                    handleEditSurgery()
                }
            }
        }

        // --- Enforce Double-Click to Open Edit Dialog (Physicians) ---
        physiciansTable.addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
            if (event.clickCount == 2) {
                val selectedPhysician = physiciansTable.selectionModel.selectedItem
                if (selectedPhysician != null) {
                    event.consume()
                    handleEditPhysician()  // Call the *future* edit dialog handler method
                }
            }
            // TODO: Add logic here if you decide to enable inline editing behavior (if any)
            // else if (physiciansTable.isEditable && !event.isConsumed) { ... }
        }
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

    /**
     * Loads physician records from the database and updates the Physicians TableView
     * Made public for calling from setupDependencies
     */
    fun loadPhysicians() {
        println("Loading physicians...")
        val physicians = dbManager.getAllPhysicians()
        physiciansData = FXCollections.observableArrayList(physicians)
        physiciansTable.items = physiciansData
        println("Loaded ${physicians.size} physicians.")
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

            val confirmationResult = showAlert(
                AlertType.CONFIRMATION,
                "Confirm Delete",
                "Delete Medication Record?",
                "Are you sure you want to permanently delete the record for ${selectedMed.genericName}?"
            )

            if (confirmationResult == ButtonType.OK) {
                println("User confirmed delete. Deleting medicaiton with ID: ${selectedMed.id}")
                dbManager.deleteCurrentMedication(selectedMed.id)
                println("Medication deleted from DB.")
                loadCurrentMedications()    // Refresh the current medications table to show the change

                if (currentMedicationsTable.items.isNotEmpty()) {
                    val newIndexToSelect = if (selectedIndex < currentMedicationsTable.items.size) selectedIndex else currentMedicationsTable.items.size - 1
                    currentMedicationsTable.selectionModel.select(newIndexToSelect)
                } else {
                    currentMedicationsTable.selectionModel.clearSelection() // Clear selection if table is empty
                }
            } else {
                println("Delete cancelled by user or dialog closed.")
            }
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
        val selectedPastMedication = pastMedicationsTable.selectionModel.selectedItem

        if (selectedPastMedication != null) {
            val selectedIndex = pastMedicationsTable.selectionModel.selectedIndex   // Get index BEFORE refresh
            println("Attempting to delete Past Medication: ${selectedPastMedication.genericName} (ID: ${selectedPastMedication.id}")

            val confirmationResult = showAlert(
                AlertType.CONFIRMATION,
                "Confirm Delete",
                "Delete Medication History Record?",
                "Are you sure you want to permanently delete the history record for ${selectedPastMedication.genericName}?"
            )

            if (confirmationResult == ButtonType.OK) {
                println("User confirmed delete. Deleting past medication with ID: ${selectedPastMedication.id}")
                dbManager.unarchiveMedication(selectedPastMedication)
                loadPastMedications()   // Refresh history table

                // Select the item at the same index in the updated list, or the last item if index is out of bounds.
                if (pastMedicationsTable.items.isNotEmpty()) {
                    val newIndexToSelect = if (selectedIndex < pastMedicationsTable.items.size) selectedIndex else pastMedicationsTable.items.size - 1
                    pastMedicationsTable.selectionModel.select(newIndexToSelect)
                } else {
                    // If the table is now empty, clear selection
                    pastMedicationsTable.selectionModel.clearSelection()
                }
            }
        } else {
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

    @FXML
    private fun handleEditSurgery() {
        println("Edit Surgery button clicked (Handler not fully implemented).")
        val selectedSurgery = surgeriesTable.selectionModel.selectedItem // Get the selected Surgery object
        if (selectedSurgery != null) {
            println("Editing Surgery: ${selectedSurgery.name}")
            // TODO: Implement loading and showing the Edit Surgery Dialog (Later step)
            showAlert(AlertType.INFORMATION, "TODO", "Edit Surgery", "Edit Surgery functionality is not yet implemented.") // Placeholder alert
        } else {
            println("No surgery selected for editing.")
            showAlert(AlertType.INFORMATION, "No Selection", null, "Please select a surgery in the table to edit.")
        }
    }

    @FXML
    private fun handleDeleteSurgery() {
        println("Delete Surgery button clicked.")
        val selectedSurgery = surgeriesTable.selectionModel.selectedItem // Get the selected item

        if (selectedSurgery != null) {
            println("Attempting to delete Surgery: ${selectedSurgery.name} (ID: ${selectedSurgery.id})")

            val confirmationResult = showAlert(
                AlertType.CONFIRMATION,      // Use CONFIRMATION type for the alert
                "Confirm Delete",            // Title for the confirmation dialog window
                "Delete Surgery Record?",  // Header text asking for confirmation
                "Are you sure you want to permanently delete the record for ${selectedSurgery.name}?" // Content text asking for confirmation
            )

            if (confirmationResult == ButtonType.OK) {
                println("User confirmed delete. Deleting surgery with ID: ${selectedSurgery.id}")
                dbManager.deleteSurgery(selectedSurgery.id) // Calls the DB method
                println("Surgery deleted from DB.")
                loadSurgeries() // Call the method to reload data into the table
            } else {
                println("Delete cancelled by user or dialog closed.")
            }
        } else {
            println("No surgery selected for deletion.")
            showAlert(AlertType.INFORMATION, "No Selection", null, "Please select a surgery in the table to delete.")
        }
    }

    @FXML
    private fun handleAddPhysician() {
        println("Add Physician button clicked.")

        // --- Load and Show the Add Physician Dialog ---
        val ownerStage = currentMedicationsTable.scene.window as? Stage

        val dialogInfo = loadDialog<AddPhysicianController>(
            "/com/medichart/gui/AddPhysicianDialog.fxml", // Path to the FXML file
            "Add New Physician", // Title for the dialog window
            ownerStage // Set the main window as the owner for modality
        )

        // --- Handle the result after the dialog is loaded and potentially shown ---
        // Check if the dialog was loaded successfully (dialogInfo is not null)
        if (dialogInfo != null) {
            val dialogController = dialogInfo.first // Get the controller instance
            val dialogStage = dialogInfo.second   // Get the dialog stage reference

            dialogStage.showAndWait()

            // --- Process the result from the dialog ---
            if (dialogController.isSavedSuccessful) {
                val newPhysician = dialogController.physicianData

                // Ensure a Physician object was returned (it should be if saved successfully)
                if (newPhysician != null) {
                    val generatedId = dbManager.addPhysician(newPhysician) // Calls the DB method from Step 39

                    // Check if the database insertion was successful (generatedId will be > 0)
                    if (generatedId != -1L) {
                        println("Physician added to DB successfully with ID: $generatedId")
                        loadPhysicians()
                        // Optional: Select the newly added item in the table (requires finding it by the generatedId)
                        // val addedPhysician = physiciansData.find { it.id == generatedId }
                        // physiciansTable.selectionModel.select(addedPhysician)
                    } else {
                        System.err.println("Error adding physician to database.")
                        showAlert(AlertType.ERROR, "Database Error", "Failed to Add Physician", "Could not save the new physician to the database.")
                    }
                } else {
                    // This case should ideally not happen if isSavedSuccessful is true in the dialog
                    System.err.println("Warning: Add Physician dialog saved successfully, but returned null data.")
                    showAlert(AlertType.WARNING, "Internal Error", "Save Data Missing", "The dialog saved, but no physician data was returned.")
                }
            } else {
                println("Add Physician dialog cancelled.")
            }
        } else {
            showAlert(AlertType.ERROR, "Error Loading Dialog", "Could not open the Add Physician dialog.", "An error occurred while opening the dialog.")
        }
    }


    @FXML
    private fun handleEditPhysician() {
        println("Edit Physician button clicked.")

        val ownerStage = currentMedicationsTable.scene.window as? Stage
        val selectedPhysician = physiciansTable.selectionModel.selectedItem

        val dialogInfo = loadDialog<EditPhysicianController>(
            "/com/medichart/gui/EditPhysicianDialog.fxml",
            "Edit Physician",
            ownerStage
        )

        if (selectedPhysician != null) {
            println("Attempting to edit Physician: ${selectedPhysician.name}")

            if (dialogInfo != null) {
                val dialogController = dialogInfo.first // Get the controller instance (non-nullable here)
                val dialogStage = dialogInfo.second   // Get the dialog stage reference (non-nullable Stage here)

                dialogController.setPhysicianData(selectedPhysician.copy()) // Pass a copy to avoid modifying original data prematurely
                dialogStage.showAndWait() // <-- CALL showAndWait() HERE IN THE HANDLER METHOD

                if (dialogController.isSavedSuccessful) { // Check if the user clicked Save in the dialog
                    val updatedPhysician = dialogController.updatedPhysicianData // Get the updated Physician object (Physician?)

                    if (updatedPhysician != null) {
                        dbManager.updatePhysician(updatedPhysician) // Calls the DB method from Step 39
                        println("Physician updated in DB: ${updatedPhysician.name} (ID: ${updatedPhysician.id})")
                        loadPhysicians() // Call the method to reload data into the table (implemented in Step 41)
                    } else {
                        System.err.println("Warning: Edit Physician dialog saved successfully, but returned null data.")
                        showAlert(AlertType.WARNING, "Internal Error", "Save Data Missing", "The dialog saved, but no physician data was returned.")
                    }
                } else {
                    println("Edit Physician dialog cancelled.")
                }
            } else {
                showAlert(AlertType.ERROR, "Error Loading Dialog", "Could not open the Edit Physician dialog.", "An error occurred while opening the dialog.")
            }
        } else {
            println("No physician selected for editing.")
            showAlert(AlertType.INFORMATION, "No Selection", null, "Please select a physician in the table to edit.")
        }
    }

    @FXML
    private fun handleDeletePhysician() {
        println("Delete Physician button clicked.")
        // Get selected item from physiciansTable
        val selectedPhysician = physiciansTable.selectionModel.selectedItem
        if (selectedPhysician != null) {
            println("Attempting to delete Physician: ${selectedPhysician.name} (ID: ${selectedPhysician.id})")

            val confirmationResult = showAlert(
                AlertType.CONFIRMATION,
                "Confirm Delete",
                "Delete Physician Record",
                "Are you sure you want to permanently delete the record for ${selectedPhysician.name}?"
            )
            if (confirmationResult == ButtonType.OK) {
                println("User confirmed delete. Deleting physician with ID: ${selectedPhysician.id}")
                dbManager.deletePhysician(selectedPhysician.id)
                loadPhysicians()
            } else {
                println("Delete cancelled by user or dialog closed.")
            }
        } else {
            println("No physician selected for deletion.")
            // Use showAlert helper to inform the user
            showAlert(AlertType.INFORMATION, "No Selection", null, "Please select a physician in the table to delete.")
        }
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
     * @return The ButtonType clicked by the user, or null if the dialog was closed without clicking a standard button
     */
    private fun showAlert(alertType: AlertType, title: String, header: String?, content: String): ButtonType? {
        val alert = Alert(alertType)
        alert.title = title
        alert.headerText = header
        alert.contentText = content

        alert.initOwner(physiciansTable?.scene?.window as? Stage)

        val result = alert.showAndWait()

        return result.orElse(null)
    }

    /**
     * Custom TableCell to display a List<PastMedication.DateRange> as a formatted string
     */
    class DateRangeTableCell : TableCell<PastMedication, List<PastMedication.DateRange>>() {
        override fun updateItem(item: List<PastMedication.DateRange>?, empty: Boolean) {
            super.updateItem(item, empty)   // Always call super method

            if (empty || item == null || item.isEmpty()) {
                text = null
                graphic = null
            } else {
                text = DatabaseManager.formatAllDateRanges(item)
                graphic = null
            }
        }
    }

    /**
     * Helper function to load an FXML file and create a modal dialog stage.
     * Does NOT show the dialog; returns the controller and stage for the caller to manage.
     * Sets the owner of the dialog to the specified owner stage (makes it modal relative to the owner).
     * @param fxmlPath The path to the FXML file (e.g., "/com/medichart/gui/AddPhysicianDialog.fxml").
     * @param title The title for the dialog window.
     * @param owner The Stage that owns this dialog (makes it modal relative to this owner), or null for no owner.
     * @return A Pair containing the controller of the loaded FXML and the dialog Stage, or null if loading fails.
     */
    private fun <T> loadDialog(fxmlPath: String, title: String, owner: Stage?): Pair<T, Stage>? {
        try {
            // Load the FXML file
            val fxmlLoader = FXMLLoader(javaClass.getResource(fxmlPath))
            val dialogRoot: Parent = fxmlLoader.load() // Use Parent for the root element type

            // Get the controller instance that was created by the FXMLLoader
            val controller = fxmlLoader.getController<T>()
            println("DEBUG: loadDialog: Controller type is: ${controller?.javaClass?.name ?: "null"}")  // DEBUG

            // Create a new Stage for the dialog window
            val dialogStage = Stage()
            dialogStage.title = title // Set the title bar text
            dialogStage.initModality(Modality.WINDOW_MODAL) // Make the dialog modal
            dialogStage.initOwner(owner) // Set the owner stage (makes it modal relative to the owner)
            dialogStage.scene = Scene(dialogRoot) // Set the scene with the loaded FXML root

            // If the controller has a method to receive the stage (like setDialogStage), call it.
            // We need to check the specific controller types we expect to load this way.
            if (controller is AddPhysicianController) {
                (controller as? AddPhysicianController)?.setDialogStage(dialogStage)
            }
            if (controller is EditPhysicianController) {
                (controller as? EditPhysicianController)?.setDialogStage(dialogStage)
            }
            // TODO: Add similar check and call for EditPhysicianController here later when you implement it (Step 46)
            // if (controller is EditPhysicianController) { controller.setDialogStage(dialogStage) }

            // Return a Pair containing the controller and the dialog stage
            return Pair(controller, dialogStage)

        } catch (e: IOException) {
            System.err.println("Error loading dialog FXML: $fxmlPath - ${e.message}")
            e.printStackTrace()
            // Return null if loading fails. The caller can handle showing a user alert.
            return null
        } catch (e: Exception) {
            System.err.println("An unexpected error occurred while loading dialog FXML: $fxmlPath - ${e.message}")
            e.printStackTrace()
            return null
        }
    }
}


