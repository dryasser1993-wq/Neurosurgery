package com.neurosurgery.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "neurosurgery.db"
        private const val DATABASE_VERSION = 8

        const val SURGICAL_NOT_PLANNED = "غير مقرر له عمل جراحي"
        const val SURGICAL_PREPARATION = "قيد التحضير للعمل الجراحي"
        const val SURGICAL_READY = "جاهز للعمل الجراحي"
        const val SURGICAL_COMPLETED = "أُجري له العمل الجراحي / منجز"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS patients (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                file_number TEXT,
                age TEXT,
                gender TEXT,
                phone TEXT,
                address TEXT,
                diagnosis TEXT,
                notes TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                specialist TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS visits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER,
                visit_date TEXT,
                notes TEXT
            )
        """.trimIndent())

        createNeurologicalExamsTable(db)
        createStaffTable(db)
        createSurgeriesTable(db)
        createInvestigationsTable(db)
        createPatientImagesTable(db)
        createPatientSurgicalStatusTables(db)
    }

    private fun createNeurologicalExamsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS neurological_exams (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER,
                exam_date TEXT,
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
                notes TEXT
            )
        """.trimIndent())
    }

    private fun createStaffTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS staff (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                role TEXT,
                specialty TEXT,
                phone TEXT,
                notes TEXT
            )
        """.trimIndent())
    }

    private fun createSurgeriesTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS surgeries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER,
                surgery_date TEXT,
                surgery_type TEXT,
                surgeon TEXT,
                surgeon_id INTEGER,
                assistants TEXT,
                preop_diagnosis TEXT,
                postoperative_diagnosis TEXT,
                indication TEXT,
                urgency TEXT,
                anesthesia_type TEXT,
                anesthetist TEXT,
                patient_position TEXT,
                surgical_approach TEXT,
                procedure_details TEXT,
                intraoperative_findings TEXT,
                implants TEXT,
                complications TEXT,
                postoperative_complications TEXT,
                outcome TEXT,
                patient_condition TEXT,
                duration_minutes TEXT,
                estimated_blood_loss TEXT,
                blood_transfusion TEXT,
                report TEXT,
                notes TEXT,
                followup_plan TEXT,
                followup_date TEXT
            )
        """.trimIndent())
    }

    private fun createInvestigationsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS investigations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER NOT NULL,
                investigation_type TEXT,
                investigation_name TEXT,
                request_date TEXT,
                result_date TEXT,
                result TEXT,
                notes TEXT
            )
        """.trimIndent())
    }

    private fun createPatientImagesTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS patient_images (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER NOT NULL,
                image_type TEXT,
                image_name TEXT,
                image_path TEXT NOT NULL,
                image_date TEXT,
                description TEXT,
                investigation_id INTEGER
            )
        """.trimIndent())
    }

    private fun createPatientSurgicalStatusTables(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS patient_surgical_status (
                patient_id INTEGER PRIMARY KEY,
                status TEXT NOT NULL DEFAULT '$SURGICAL_NOT_PLANNED',
                planned_surgery_type TEXT,
                planned_surgery_date TEXT,
                surgeon TEXT,
                notes TEXT,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS patient_surgical_status_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER NOT NULL,
                status TEXT NOT NULL,
                changed_at TEXT DEFAULT CURRENT_TIMESTAMP,
                notes TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            addColumn(db, "neurological_exams", "exam_date", "TEXT")
            addColumn(db, "neurological_exams", "right_upper_motor", "TEXT")
            addColumn(db, "neurological_exams", "left_upper_motor", "TEXT")
            addColumn(db, "neurological_exams", "right_lower_motor", "TEXT")
            addColumn(db, "neurological_exams", "left_lower_motor", "TEXT")
            addColumn(db, "neurological_exams", "right_sensation", "TEXT")
            addColumn(db, "neurological_exams", "left_sensation", "TEXT")
            addColumn(db, "neurological_exams", "right_reflexes", "TEXT")
            addColumn(db, "neurological_exams", "left_reflexes", "TEXT")
            addColumn(db, "neurological_exams", "right_babinski", "TEXT")
            addColumn(db, "neurological_exams", "left_babinski", "TEXT")
            addColumn(db, "neurological_exams", "right_coordination", "TEXT")
            addColumn(db, "neurological_exams", "left_coordination", "TEXT")
            addColumn(db, "neurological_exams", "clinical_impression", "TEXT")
            addColumn(db, "neurological_exams", "examiner_name", "TEXT")
        }

        if (oldVersion < 3) createStaffTable(db)

        if (oldVersion < 4) {
            createStaffTable(db)
            createNeurologicalExamsTable(db)
        }

        if (oldVersion < 5) {
            createInvestigationsTable(db)
            createPatientImagesTable(db)
        }

        if (oldVersion < 6) {
            addColumn(db, "surgeries", "surgeon_id", "INTEGER")
            addColumn(db, "surgeries", "assistants", "TEXT")
            addColumn(db, "surgeries", "preop_diagnosis", "TEXT")
            addColumn(db, "surgeries", "procedure_details", "TEXT")
            addColumn(db, "surgeries", "complications", "TEXT")
            addColumn(db, "surgeries", "outcome", "TEXT")
            addColumn(db, "surgeries", "report", "TEXT")
            addColumn(db, "surgeries", "notes", "TEXT")
        }

        if (oldVersion < 7) {
            addColumn(db, "patients", "specialist", "TEXT")
        }

        if (oldVersion < 8) {
            // Expand the existing surgery record without deleting old operations.
            addColumn(db, "surgeries", "postoperative_diagnosis", "TEXT")
            addColumn(db, "surgeries", "indication", "TEXT")
            addColumn(db, "surgeries", "urgency", "TEXT")
            addColumn(db, "surgeries", "anesthesia_type", "TEXT")
            addColumn(db, "surgeries", "anesthetist", "TEXT")
            addColumn(db, "surgeries", "patient_position", "TEXT")
            addColumn(db, "surgeries", "surgical_approach", "TEXT")
            addColumn(db, "surgeries", "intraoperative_findings", "TEXT")
            addColumn(db, "surgeries", "implants", "TEXT")
            addColumn(db, "surgeries", "postoperative_complications", "TEXT")
            addColumn(db, "surgeries", "patient_condition", "TEXT")
            addColumn(db, "surgeries", "duration_minutes", "TEXT")
            addColumn(db, "surgeries", "estimated_blood_loss", "TEXT")
            addColumn(db, "surgeries", "blood_transfusion", "TEXT")
            addColumn(db, "surgeries", "followup_plan", "TEXT")
            addColumn(db, "surgeries", "followup_date", "TEXT")

            addColumn(db, "patient_images", "investigation_id", "INTEGER")
            createPatientSurgicalStatusTables(db)

            // Give every existing patient a default status while preserving all existing rows.
            db.execSQL("""
                INSERT OR IGNORE INTO patient_surgical_status
                    (patient_id, status, updated_at)
                SELECT id, '$SURGICAL_NOT_PLANNED', CURRENT_TIMESTAMP
                FROM patients
            """.trimIndent())
        }
    }

    private fun addColumn(
        db: SQLiteDatabase,
        table: String,
        column: String,
        type: String
    ) {
        try {
            db.execSQL("ALTER TABLE $table ADD COLUMN $column $type")
        } catch (_: Exception) {
            // Column may already exist in a database with a partial prior upgrade.
        }
    }

    fun insertPatient(
        name: String,
        fileNumber: String,
        age: String,
        gender: String,
        phone: String,
        address: String,
        diagnosis: String,
        notes: String,
        specialist: String = ""
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("full_name", name)
            put("file_number", fileNumber)
            put("age", age)
            put("gender", gender)
            put("phone", phone)
            put("address", address)
            put("diagnosis", diagnosis)
            put("notes", notes)
            put("specialist", specialist)
        }

        val patientId = db.insert("patients", null, values)
        if (patientId != -1L) {
            val statusValues = ContentValues().apply {
                put("patient_id", patientId)
                put("status", SURGICAL_NOT_PLANNED)
            }
            db.insertWithOnConflict(
                "patient_surgical_status",
                null,
                statusValues,
                SQLiteDatabase.CONFLICT_IGNORE
            )
        }
        return patientId
    }

    fun getPatientCount(): Int = getCount("SELECT COUNT(*) FROM patients")

    fun getSurgeryCount(): Int = getCount("SELECT COUNT(*) FROM surgeries")

    fun getSurgicalStatusCount(status: String): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM patient_surgical_status WHERE status = ?",
            arrayOf(status)
        )
        cursor.use { return if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    fun getPatientSurgicalStatus(patientId: Int): String {
        val cursor = readableDatabase.rawQuery(
            "SELECT status FROM patient_surgical_status WHERE patient_id = ? LIMIT 1",
            arrayOf(patientId.toString())
        )
        cursor.use {
            return if (it.moveToFirst()) {
                it.getString(0) ?: SURGICAL_NOT_PLANNED
            } else {
                SURGICAL_NOT_PLANNED
            }
        }
    }

    fun savePatientSurgicalStatus(
        patientId: Int,
        status: String,
        plannedType: String = "",
        plannedDate: String = "",
        surgeon: String = "",
        notes: String = ""
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("patient_id", patientId)
            put("status", status)
            put("planned_surgery_type", plannedType)
            put("planned_surgery_date", plannedDate)
            put("surgeon", surgeon)
            put("notes", notes)
        }

        val result = db.insertWithOnConflict(
            "patient_surgical_status",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )

        if (result == -1L) return false

        val historyValues = ContentValues().apply {
            put("patient_id", patientId)
            put("status", status)
            put("notes", notes)
        }
        db.insert("patient_surgical_status_history", null, historyValues)
        return true
    }

    private fun getCount(query: String): Int {
        val cursor = readableDatabase.rawQuery(query, null)
        cursor.use { return if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    fun addDoctor(name: String): Long {
        val values = ContentValues().apply {
            put("full_name", name)
            put("role", "Doctor")
            put("specialty", "Neurosurgery")
        }
        return writableDatabase.insert("staff", null, values)
    }

    fun getDoctors(): List<String> {
        val doctors = mutableListOf<String>()
        val cursor = readableDatabase.rawQuery(
            "SELECT full_name FROM staff WHERE role = ? ORDER BY full_name",
            arrayOf("Doctor")
        )
        cursor.use {
            while (it.moveToNext()) doctors.add(it.getString(0))
        }
        return doctors
    }

    fun getDoctorId(name: String): Long {
        val cursor = readableDatabase.rawQuery(
            "SELECT id FROM staff WHERE full_name = ? AND role = ? LIMIT 1",
            arrayOf(name, "Doctor")
        )
        cursor.use { return if (it.moveToFirst()) it.getLong(0) else -1L }
    }

    fun saveNeurologicalExam(values: ContentValues): Long =
        writableDatabase.insert("neurological_exams", null, values)

    fun saveInvestigation(values: ContentValues): Long =
        writableDatabase.insert("investigations", null, values)

    fun savePatientImage(values: ContentValues): Long =
        writableDatabase.insert("patient_images", null, values)

    fun saveSurgery(values: ContentValues): Long =
        writableDatabase.insert("surgeries", null, values)
}
