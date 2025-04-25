package com.medichart.gui

import com.medichart.database.DatabaseManager
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import java.io.IOException

/**
 * The main application class for MediChart.
 * Extends javafx.application.Application to launch the GUI.
 */
class MediChartApp : Application() {    // Extend Application() directly
    /**
     * The main entry point for the JavaFX application.
     * Sets the primary stage and loads the main FXML scene.
     *
     * @param stage The primary stage for this application, onto which
     * the application scene can be set. Applications may create other stages, if needed,
     * but they will not be primary stages.
     * @throws IOException If the FXML file cannot be loaded.
     */
    @Throws(IOException::class) // Kotlin annotation for checked exceptions (optional but good practice)
    override fun start(stage: Stage) {  // Use 'fun' for methods
        // Initialize the database
        val dbManager = DatabaseManager()   // No 'new' keyword in Kotlin
        dbManager.createTables()    // Ensure tables exist on startup

        // Load the FXML file. Get the resource URL relative to the MediChartApp class.
        val fxmlLoader = FXMLLoader(MediChartApp::class.java.getResource("MediChart.fxml"))
        // Create the scene using the loaded FXML root node
        val scene = Scene(fxmlLoader.load(), 800.0, 600.0)  // Use .0 for Double literals for dimensions
        stage.title = "MediChart"   // Property access for getters/setters
        stage.scene = scene
        stage.show()
    }
}

/**
 * The main function that launches the JavaFX application.
 * This is the entry point when running the application.
 *
 * @param args Command line arguments passed to the application.
 */
fun main(args: Array<String>) { // Standard Kotlin main function
    // Launch the JavaFX application. Pass the application class and command line aruments.
    Application.launch(MediChartApp::class.java, *args) // Use spread operator * for varargs
}