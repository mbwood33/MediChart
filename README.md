# MediChart

A simple, personal desktop application built with JavaFX and Kotlin for managing current and past medications and other health information.

## About

MediChart is designed to provide a straightforward way for individuals to keep track of their prescribed and over-the-counter medications, dosage instructions, history, and other relevant health details like surgeries. It aims to be a local, privacy-focused tool running directly on your desktop.

## Features

Based on the current state of development (up to v0.5.0 equivalent functionality):

* **Current Medications Management:**
    * Add new medications with details: Generic Name (required), Brand Name, Dosage, Dose Form, Instructions, Reason, Prescriber, Notes, Start Date, and Manufacturer.
    * Uses a user-friendly DatePicker for selecting the Start Date.
    * Basic input validation for required fields (Generic Name).
    * View current medications in a table with configurable columns.
    * Archive medications that are no longer being taken, moving them to history.
    * Delete selected current medications.
* **Medication History:**
    * View archived medications in a separate table.
    * Unarchive selected medications from history back to the current list.
    * Delete selected medications from history.
    * Includes a column to display Date Range(s) medication was taken (Note: The display is currently the default string representation of the internal list; robust parsing and display formatting are future enhancements).
* **Surgeries:**
    * Includes a basic tab and table structure to list surgeries (Note: Add, Edit, and Delete functionality for surgeries are future enhancements).
* **Data Persistence:**
    * All data is stored locally in a single SQLite database file (`medichart.db`) created in the application's working directory.
    * Dates (Start Date) are stored as TEXT in 'YYYY-MM-DD' format and converted to/from `java.time.LocalDate` in the application logic.
* **User Interface:**
    * Built using JavaFX for a native desktop look and feel.
    * Improved Add Medication dialog with adjusted sizing, word wrapping for long text fields (Instructions, Notes), and keyboard shortcuts (Enter to Save, Esc to Cancel).

## Technologies Used

* **Language:** Kotlin
* **UI Framework:** JavaFX
* **Build Tool:** Apache Maven
* **Database:** SQLite (local file-based)
* **JDBC Driver:** `org.xerial:sqlite-jdbc`
* **Date/Time API:** `java.time` (built-in JDK)
* **Testing Framework:** JUnit 5 (included as a dependency for future test implementation)

## Getting Started

### Prerequisites

* Java Development Kit (JDK) version 23 or compatible (e.g., OpenJDK 23) installed.
* Apache Maven installed and configured.
* Git installed.

### Cloning the Repository

Open your terminal or command prompt and run the following command, replacing `mbwood33/MediChart.git` with your actual GitHub repository path:

```bash
git clone [https://github.com/mbwood33/MediChart.git](https://github.com/your_username/MediChart.git)
cd MediChart