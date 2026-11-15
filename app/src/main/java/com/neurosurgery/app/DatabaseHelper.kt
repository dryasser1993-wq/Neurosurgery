
package com.neurosurgery.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "neurosurgery.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_PATIENTS = "patients"
        const val TABLE_VISITS = "visits"
        const val TABLE_EXAMS = "neurological_exams"
        const val TABLE_SURGERIES = "surgeries"
        const val TABLE_STAFF = "staff"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE patients (
                id TEXT PRIMARY KEY NOT NULL,
                full_name TEXT NOT NULL,
                age TEXT,
                gender TEXT,
                phone TEXT,
                address TEXT,
                medical_history TEXT,
                allergies TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE visits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id TEXT NOT NULL,
                visit_date TEXT NOT NULL,
                complaint TEXT,
                diagnosis TEXT,
                treatment_plan TEXT,
                notes TEXT,
                FOREIGN KEY(patient_id)
                    REFERENCES patients(id)
                    ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE neurological_exams (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id TEXT NOT NULL,
                exam_date TEXT NOT NULL,
                consciousness TEXT,
                cranial_nerves TEXT,
                motor_power TEXT,
                sensation TEXT,
                reflexes TEXT,
                coordination TEXT,
                gait TEXT,
                notes TEXT,
                FOREIGN KEY(patient_id)
                    REFERENCES patients(id)
                    ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE surgeries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id TEXT NOT NULL,
                surgery_date TEXT NOT NULL,
                procedure_name TEXT NOT NULL,
                surgeon TEXT,
                assistants TEXT,
                anesthesia TEXT,
                findings TEXT,
                complications TEXT,
                outcome TEXT,
                notes TEXT,
                FOREIGN KEY(patient_id)
                    REFERENCES patients(id)
                    ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE staff (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL,
                specialty TEXT,
                phone TEXT,
                notes TEXT
            )
        """.trimIndent())

        db.execSQL(
            "CREATE INDEX idx_visits_patient ON visits(patient_id)"
        )

        db.execSQL(
            "CREATE INDEX idx_exams_patient ON neurological_exams(patient_id)"
        )

        db.execSQL(
            "CREATE INDEX idx_surgeries_patient ON surgeries(patient_id)"
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Database migrations will be added here.
    }

    fun insertPatient(
        id: String,
        name: String,
        age: String,
        gender: String
    ): Boolean {
        val now = System.currentTimeMillis().toString()

        val values = ContentValues().apply {
            put("id", id)
            put("full_name", name)
            put("age", age)
            put("gender", gender)
            put("created_at", now)
            put("updated_at", now)
        }

        val result = writableDatabase.insert(
            TABLE_PATIENTS,
            null,
            values
        )

        return result != -1L
    }

    fun getPatientCount(): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM patients",
            null
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                0
            }
        }
    }
}
