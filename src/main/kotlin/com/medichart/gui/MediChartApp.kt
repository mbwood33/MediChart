package com.medichart.gui

import com.medichart.database.DatabaseManager
import com.medichart.controller.MediChartController // added
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import java.io.IOException
import javafx.scene.image.Image // added
import javafx.scene.control.Alert   // added
import javafx.scene.control.Alert.AlertType // added
import javafx.scene.layout.BorderPane   // added

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
     * @throws Exception if any other unexpected error occurs during setup
     */

    // New code
    @Throws(IOException::class)
    override fun start(primaryStage: Stage) {
        println("JavaFX Version: " + System.getProperty("javafx.version"))

        try {
            // --- FXML Loading and Controller Setup ---
            val fxmlLoader = FXMLLoader(MediChartApp::class.java.getResource("MediChart.fxml")) // Load the FXML file. Get the resource URL relative to the MediChartApp class
            val root = fxmlLoader.load<BorderPane>()    // Load the FXML and get the root node

            val controller = fxmlLoader.getController<MediChartController>()    // Get the controller instance automatically created and associated by the FXMLLoader.

            val dbManager = DatabaseManager()   // Create DatabaseManager instance
            dbManager.createTables()    // Ensure tables exist

            controller.setupDependencies(dbManager) // Pass the initialized DatabaseManager instance to the controller

            // --- Set Application Icon ---
            try {
                // Load the icon image from resources
                val iconStream = MediChartApp::class.java.getResourceAsStream("/icons/pill_icon.png")
                if (iconStream != null) {
                    val iconImage = Image(iconStream)
                    primaryStage.icons.add(iconImage)
                    iconStream.close()
                } else {
                    System.err.println("Application icon file not found in resources at the specified path. Icon not set.")
                }
            } catch (e: Exception) {
                System.err.println("Error loading application icon: ${e.message}")
                e.printStackTrace()
            }
            // --- End Set Application Icon

            val scene = Scene(root, 1200.0, 600.0)

            primaryStage.title = "MediChart"
            primaryStage.scene = scene
            primaryStage.show()
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle FXML loading error - show an alert to the user
            val alert = Alert(AlertType.ERROR)
            alert.title = "Error"
            alert.headerText = "Could not load application UI."
            alert.contentText = "An error occurred while loading the main application window.\nDetails: ${e.message}"
            alert.showAndWait()
        } catch (e: Exception) {
            // Catch any other unexpected errors during startup (e.g., DB initialization errors before FXML load)
            System.err.println("An unexpected error occurred during application startup: ${e.message}")
            e.printStackTrace()
            val alert = Alert(AlertType.ERROR)
            alert.title = "Error"
            alert.headerText = "Application Startup Error."
            alert.contentText = "An unexpected error occurred during application startup.\nDetails: ${e.message}"
            alert.showAndWait()
        }
    }

    // Old code
    /*
    @Throws(IOException::class) // Kotlin annotation for checked exceptions (optional but good practice)
    override fun start(stage: Stage) {  // Use 'fun' for methods
        println("JavaFX Version: " + System.getProperty("javafx.version"))

        // Initialize the database
        val dbManager = DatabaseManager()   // No 'new' keyword in Kotlin
        dbManager.createTables()    // Ensure tables exist on startup

        // Load the FXML file. Get the resource URL relative to the MediChartApp class.
        val fxmlLoader = FXMLLoader(MediChartApp::class.java.getResource("MediChart.fxml"))
        // Create the scene using the loaded FXML root node
        val scene = Scene(fxmlLoader.load(), 1200.0, 600.0)  // Use .0 for Double literals for dimensions
        stage.title = "MediChart"   // Property access for getters/setters
        stage.scene = scene
        stage.show()
    }
    */
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