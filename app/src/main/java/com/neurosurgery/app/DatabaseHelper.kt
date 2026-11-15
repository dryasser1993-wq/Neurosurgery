
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
        private const val DATABASE_VERSION = 2

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
            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN right_upper_motor TEXT
            """.trimIndent())

            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN left_upper_motor TEXT
            """.trimIndent())

            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN right_lower_motor TEXT
            """.trimIndent())

            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN left_lower_motor TEXT
            """.trimIndent())

            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN right_sensation TEXT
            """.trimIndent())

            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN left_sensation TEXT
            """.trimIndent())

            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN right_reflexes TEXT
            """.trimIndent())

            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN left_reflexes TEXT
            """.trimIndent())

            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN right_babinski TEXT
            """.trimIndent())

            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN left_babinski TEXT
            """.trimIndent())

            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN right_coordination TEXT
            """.trimIndent())

            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN left_coordination TEXT
            """.trimIndent())

            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN clinical_impression TEXT
            """.trimIndent())

            db.execSQL("""
                ALTER TABLE neurological_exams
                ADD COLUMN examiner_name TEXT
            """.trimIndent())
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
