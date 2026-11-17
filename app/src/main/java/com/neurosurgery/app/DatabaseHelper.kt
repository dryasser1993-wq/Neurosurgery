
package com.neurosurgery.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "neurosurgery.db"
        private const val DATABASE_VERSION = 9

        const val SURGICAL_NOT_PLANNED = "غير مقرر له عمل جراحي"
        const val SURGICAL_PREPARATION = "قيد التحضير للعمل الجراحي"
        const val SURGICAL_READY = "جاهز للعمل الجراحي"
        const val SURGICAL_COMPLETED = "أُجري له العمل الجراحي / منجز"

        const val STAFF_SPECIALIST = "Specialist"
        const val STAFF_RESIDENT = "Resident"
        const val STAFF_DOCTOR = "Doctor"
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
                specialist TEXT,
                admission_type TEXT,
                referral_source TEXT,
                injury_mechanism TEXT,
                outpatient_diagnosis TEXT,
                differential_diagnoses TEXT
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
        createSurgeryAssistantTable(db)
        createSurgeryLevelsTable(db)
        createAttachmentsTable(db)
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
                notes TEXT,
                gcs_eye TEXT,
                gcs_verbal TEXT,
                gcs_motor TEXT,
                gcs_total INTEGER,
                right_pupil_size TEXT,
                left_pupil_size TEXT,
                right_pupil_reaction TEXT,
                left_pupil_reaction TEXT
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
                followup_date TEXT,
                surgery_category TEXT,
                surgical_region TEXT,
                surgical_side TEXT,
                incision_type TEXT,
                access_method TEXT,
                other_approach TEXT
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
                notes TEXT,
                investigation_category TEXT
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

    private fun createSurgeryAssistantTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS surgery_assistants (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                surgery_id INTEGER NOT NULL,
                staff_id INTEGER NOT NULL,
                UNIQUE(surgery_id, staff_id)
            )
        """.trimIndent())
    }

    private fun createSurgeryLevelsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS surgery_levels (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                surgery_id INTEGER NOT NULL,
                region TEXT,
                level_name TEXT NOT NULL,
                sort_order INTEGER DEFAULT 0
            )
        """.trimIndent())
    }

    private fun createAttachmentsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS attachments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER NOT NULL,
                owner_type TEXT NOT NULL,
                owner_id INTEGER,
                file_name TEXT NOT NULL,
                file_path TEXT NOT NULL,
                mime_type TEXT,
                attachment_type TEXT,
                description TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
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

            db.execSQL("""
                INSERT OR IGNORE INTO patient_surgical_status
                    (patient_id, status, updated_at)
                SELECT id, '$SURGICAL_NOT_PLANNED', CURRENT_TIMESTAMP
                FROM patients
            """.trimIndent())
        }

        if (oldVersion < 9) {
            addColumn(db, "patients", "admission_type", "TEXT")
            addColumn(db, "patients", "referral_source", "TEXT")
            addColumn(db, "patients", "injury_mechanism", "TEXT")
            addColumn(db, "patients", "outpatient_diagnosis", "TEXT")
            addColumn(db, "patients", "differential_diagnoses", "TEXT")

            addColumn(db, "neurological_exams", "gcs_eye", "TEXT")
            addColumn(db, "neurological_exams", "gcs_verbal", "TEXT")
            addColumn(db, "neurological_exams", "gcs_motor", "TEXT")
            addColumn(db, "neurological_exams", "gcs_total", "INTEGER")
            addColumn(db, "neurological_exams", "right_pupil_size", "TEXT")
            addColumn(db, "neurological_exams", "left_pupil_size", "TEXT")
            addColumn(db, "neurological_exams", "right_pupil_reaction", "TEXT")
            addColumn(db, "neurological_exams", "left_pupil_reaction", "TEXT")

            addColumn(db, "surgeries", "surgery_category", "TEXT")
            addColumn(db, "surgeries", "surgical_region", "TEXT")
            addColumn(db, "surgeries", "surgical_side", "TEXT")
            addColumn(db, "surgeries", "incision_type", "TEXT")
            addColumn(db, "surgeries", "access_method", "TEXT")
            addColumn(db, "surgeries", "other_approach", "TEXT")

            addColumn(db, "investigations", "investigation_category", "TEXT")

            createSurgeryAssistantTable(db)
            createSurgeryLevelsTable(db)
            createAttachmentsTable(db)
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
            // Column already exists, so keep the existing database intact.
        }
    }

    /**
     * Inserts a patient and creates a default surgical status.
     * Existing file numbers are never rewritten.
     * A protocol number is generated only when the supplied number is blank.
     */
    fun insertPatient(
        name: String,
        fileNumber: String,
        age: String,
        gender: String,
        phone: String,
        address: String,
        diagnosis: String,
        notes: String,
        specialist: String = "",
        admissionType: String = "",
        referralSource: String = "",
        injuryMechanism: String = "",
        outpatientDiagnosis: String = "",
        differentialDiagnoses: String = ""
    ): Long {
        val db = writableDatabase
        var patientId = -1L

        db.beginTransaction()
        try {
            val finalFileNumber = if (fileNumber.isBlank()) {
                generateProtocolNumber(db)
            } else {
                fileNumber.trim()
            }

            val values = ContentValues().apply {
                put("full_name", name.trim())
                put("file_number", finalFileNumber)
                put("age", age)
                put("gender", gender)
                put("phone", phone)
                put("address", address)
                put("diagnosis", diagnosis)
                put("notes", notes)
                put("specialist", specialist)
                put("admission_type", admissionType)
                put("referral_source", referralSource)
                put("injury_mechanism", injuryMechanism)
                put("outpatient_diagnosis", outpatientDiagnosis)
                put("differential_diagnoses", differentialDiagnoses)
            }

            patientId = db.insert("patients", null, values)

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

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return patientId
    }

    private fun generateProtocolNumber(db: SQLiteDatabase): String {
        var next = 1L
        val cursor = db.rawQuery(
            """
            SELECT MAX(CAST(SUBSTR(file_number, 4) AS INTEGER))
            FROM patients
            WHERE file_number GLOB 'NS-[0-9]*'
            """.trimIndent(),
            null
        )
        cursor.use {
            if (it.moveToFirst() && !it.isNull(0)) {
                next = it.getLong(0) + 1L
            }
        }

        var candidate = "NS-" + next.toString().padStart(6, '0')
        while (true) {
            val check = db.rawQuery(
                "SELECT 1 FROM patients WHERE file_number = ? LIMIT 1",
                arrayOf(candidate)
            )
            val exists = check.use { it.moveToFirst() }
            if (!exists) return candidate
            next++
            candidate = "NS-" + next.toString().padStart(6, '0')
        }
    }

    fun getPatientCount(): Int =
        getCount("SELECT COUNT(*) FROM patients")

    fun getSurgeryCount(): Int =
        getCount("SELECT COUNT(*) FROM surgeries")

    fun getSurgicalStatusCount(status: String): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM patient_surgical_status WHERE status = ?",
            arrayOf(status)
        )
        cursor.use {
            return if (it.moveToFirst()) it.getInt(0) else 0
        }
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
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("patient_id", patientId)
                put("status", status)
                put("planned_surgery_type", plannedType)
                put("planned_surgery_date", plannedDate)
                put("surgeon", surgeon)
                put("notes", notes)
                put("updated_at", java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.US
                ).format(java.util.Date()))
            }

            val existing = db.update(
                "patient_surgical_status",
                values,
                "patient_id = ?",
                arrayOf(patientId.toString())
            )

            if (existing == 0) {
                val inserted = db.insert("patient_surgical_status", null, values)
                if (inserted == -1L) return false
            }

            val historyValues = ContentValues().apply {
                put("patient_id", patientId)
                put("status", status)
                put("notes", notes)
            }
            if (db.insert("patient_surgical_status_history", null, historyValues) == -1L) {
                return false
            }

            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    private fun getCount(query: String): Int {
        val cursor = readableDatabase.rawQuery(query, null)
        cursor.use {
            return if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    // Staff compatibility: old Doctor records remain available as specialists.

    fun addDoctor(name: String): Long =
        addStaff(name, STAFF_DOCTOR, "Neurosurgery")

    fun addStaff(
        name: String,
        role: String,
        specialty: String = "",
        phone: String = "",
        notes: String = ""
    ): Long {
        if (name.isBlank()) return -1L
        val values = ContentValues().apply {
            put("full_name", name.trim())
            put("role", role)
            put("specialty", specialty)
            put("phone", phone)
            put("notes", notes)
        }
        return writableDatabase.insert("staff", null, values)
    }

    fun getSpecialists(): List<String> {
        val result = mutableListOf<String>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT DISTINCT full_name
            FROM staff
            WHERE role IN (?, ?)
            ORDER BY full_name COLLATE NOCASE
            """.trimIndent(),
            arrayOf(STAFF_SPECIALIST, STAFF_DOCTOR)
        )
        cursor.use {
            while (it.moveToNext()) result.add(it.getString(0))
        }
        return result
    }

    fun getResidents(): List<String> {
        val result = mutableListOf<String>()
        val cursor = readableDatabase.rawQuery(
            "SELECT full_name FROM staff WHERE role = ? ORDER BY full_name",
            arrayOf(STAFF_RESIDENT)
        )
        cursor.use {
            while (it.moveToNext()) result.add(it.getString(0))
        }
        return result
    }

    fun getDoctors(): List<String> = getSpecialists()

    fun getDoctorId(name: String): Long {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT id FROM staff
            WHERE full_name = ? AND role IN (?, ?)
            ORDER BY CASE WHEN role = ? THEN 0 ELSE 1 END
            LIMIT 1
            """.trimIndent(),
            arrayOf(name, STAFF_SPECIALIST, STAFF_DOCTOR, STAFF_SPECIALIST)
        )
        cursor.use {
            return if (it.moveToFirst()) it.getLong(0) else -1L
        }
    }

    fun getStaffId(name: String, role: String? = null): Long {
        val sql: String
        val args: Array<String>
        if (role == null) {
            sql = "SELECT id FROM staff WHERE full_name = ? LIMIT 1"
            args = arrayOf(name)
        } else {
            sql = "SELECT id FROM staff WHERE full_name = ? AND role = ? LIMIT 1"
            args = arrayOf(name, role)
        }
        val cursor = readableDatabase.rawQuery(sql, args)
        cursor.use {
            return if (it.moveToFirst()) it.getLong(0) else -1L
        }
    }

    fun saveNeurologicalExam(values: ContentValues): Long =
        writableDatabase.insert("neurological_exams", null, values)

    fun saveInvestigation(values: ContentValues): Long =
        writableDatabase.insert("investigations", null, values)

    fun savePatientImage(values: ContentValues): Long =
        writableDatabase.insert("patient_images", null, values)

    fun saveSurgery(values: ContentValues): Long =
        writableDatabase.insert("surgeries", null, values)

    fun saveSurgeryAssistant(surgeryId: Long, staffId: Long): Long {
        val values = ContentValues().apply {
            put("surgery_id", surgeryId)
            put("staff_id", staffId)
        }
        return writableDatabase.insertWithOnConflict(
            "surgery_assistants",
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    fun getSurgeryAssistantIds(surgeryId: Long): List<Long> {
        val result = mutableListOf<Long>()
        val cursor = readableDatabase.rawQuery(
            "SELECT staff_id FROM surgery_assistants WHERE surgery_id = ? ORDER BY id",
            arrayOf(surgeryId.toString())
        )
        cursor.use {
            while (it.moveToNext()) result.add(it.getLong(0))
        }
        return result
    }

    fun saveSurgeryLevel(
        surgeryId: Long,
        region: String,
        levelName: String,
        sortOrder: Int = 0
    ): Long {
        if (levelName.isBlank()) return -1L
        val values = ContentValues().apply {
            put("surgery_id", surgeryId)
            put("region", region)
            put("level_name", levelName.trim())
            put("sort_order", sortOrder)
        }
        return writableDatabase.insert("surgery_levels", null, values)
    }

    fun saveAttachment(values: ContentValues): Long =
        writableDatabase.insert("attachments", null, values)

    fun getAttachments(patientId: Int, ownerType: String, ownerId: Long): List<ContentValues> {
        val result = mutableListOf<ContentValues>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT id, file_name, file_path, mime_type, attachment_type,
                   description, created_at
            FROM attachments
            WHERE patient_id = ? AND owner_type = ? AND owner_id = ?
            ORDER BY id DESC
            """.trimIndent(),
            arrayOf(patientId.toString(), ownerType, ownerId.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                result.add(ContentValues().apply {
                    put("id", it.getLong(0))
                    put("file_name", it.getString(1))
                    put("file_path", it.getString(2))
                    put("mime_type", it.getString(3))
                    put("attachment_type", it.getString(4))
                    put("description", it.getString(5))
                    put("created_at", it.getString(6))
                })
            }
        }
        return result
    }
}
