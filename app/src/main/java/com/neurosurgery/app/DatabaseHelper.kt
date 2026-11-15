
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
        private const val DATABASE_VERSION = 3

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

        createExamsTable(db)

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

        createStaffTable(db)
        createIndexes(db)
    }

    private fun createExamsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE neurological_exams (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id TEXT NOT NULL,
                exam_date TEXT NOT NULL,
                consciousness TEXT,
                cranial_nerves TEXT,
                right_upper_motor TEXT,
                left_upper_motor TEXT,
                right_lower_motor TEXT,
                left_lower_motor TEXT,
                right_sensation TEXT,
                left_sensation TEXT,
                right_reflexes TEXT,
                left_reflexes TEXT,
                right_babinski TEXT,
                left_babinski TEXT,
                right_coordination TEXT,
                left_coordination TEXT,
                gait TEXT,
                clinical_impression TEXT,
                examiner_name TEXT,
                notes TEXT,
                FOREIGN KEY(patient_id)
                    REFERENCES patients(id)
                    ON DELETE CASCADE
            )
        """.trimIndent())
    }

    private fun createStaffTable(db: SQLiteDatabase) {
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
    }

    private fun createIndexes(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_visits_patient ON visits(patient_id)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_exams_patient ON neurological_exams(patient_id)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_surgeries_patient ON surgeries(patient_id)"
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        if (oldVersion < 2) {
            val columns = listOf(
                "right_upper_motor",
                "left_upper_motor",
                "right_lower_motor",
                "left_lower_motor",
                "right_sensation",
                "left_sensation",
                "right_reflexes",
                "left_reflexes",
                "right_babinski",
                "left_babinski",
                "right_coordination",
                "left_coordination",
                "clinical_impression",
                "examiner_name"
            )

            columns.forEach { column ->
                db.execSQL(
                    "ALTER TABLE neurological_exams ADD COLUMN $column TEXT"
                )
            }
        }

        if (oldVersion < 3) {
            createStaffTable(db)
        }

        createIndexes(db)
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

        return writableDatabase.insert(
            TABLE_PATIENTS,
            null,
            values
        ) != -1L
    }

    fun getPatientCount(): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM patients",
            null
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    fun addDoctor(name: String): Boolean {
        if (name.trim().isEmpty()) return false

        val values = ContentValues().apply {
            put("full_name", name.trim())
            put("role", "Doctor")
            put("specialty", "Neurosurgery")
        }

        return writableDatabase.insert(
            TABLE_STAFF,
            null,
            values
        ) != -1L
    }

    fun getDoctors(): List<String> {
        val doctors = mutableListOf<String>()

        readableDatabase.rawQuery(
            """
            SELECT full_name FROM staff
            WHERE role = 'Doctor'
            ORDER BY full_name
            """.trimIndent(),
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                doctors.add(cursor.getString(0))
            }
        }

        return doctors
    }
}
