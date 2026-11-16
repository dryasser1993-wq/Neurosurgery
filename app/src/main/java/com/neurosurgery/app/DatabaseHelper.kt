
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "neurosurgery.db"
        private const val DATABASE_VERSION = 6
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
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """)

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS visits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER,
                visit_date TEXT,
                notes TEXT
            )
        """)

        createNeurologicalExamsTable(db)
        createStaffTable(db)
        createSurgeriesTable(db)
        createInvestigationsTable(db)
        createPatientImagesTable(db)
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
        """)
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
        """)
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
                procedure_details TEXT,
                complications TEXT,
                outcome TEXT,
                report TEXT,
                notes TEXT
            )
        """)
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
        """)
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
                description TEXT
            )
        """)
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

        if (oldVersion < 3) {
            createStaffTable(db)
        }

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
            // العمود موجود مسبقاً.
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
        notes: String
    ): Long {
        val values = ContentValues().apply {
            put("full_name", name)
            put("file_number", fileNumber)
            put("age", age)
            put("gender", gender)
            put("phone", phone)
            put("address", address)
            put("diagnosis", diagnosis)
            put("notes", notes)
        }

        return writableDatabase.insert("patients", null, values)
    }

    fun getPatientCount(): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM patients", null
        )
        cursor.use {
            return if (it.moveToFirst()) it.getInt(0) else 0
        }
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
            while (it.moveToNext()) {
                doctors.add(it.getString(0))
            }
        }

        return doctors
    }

    fun getDoctorId(name: String): Long {
        val cursor = readableDatabase.rawQuery(
            "SELECT id FROM staff WHERE full_name = ? AND role = ? LIMIT 1",
            arrayOf(name, "Doctor")
        )

        cursor.use {
            return if (it.moveToFirst()) it.getLong(0) else -1L
        }
    }

    fun saveNeurologicalExam(values: ContentValues): Long {
        return writableDatabase.insert(
            "neurological_exams", null, values
        )
    }

    fun saveInvestigation(values: ContentValues): Long {
        return writableDatabase.insert(
            "investigations", null, values
        )
    }

    fun savePatientImage(values: ContentValues): Long {
        return writableDatabase.insert(
            "patient_images", null, values
        )
    }

    fun saveSurgery(values: ContentValues): Long {
        return writableDatabase.insert(
            "surgeries", null, values
        )
    }
}
