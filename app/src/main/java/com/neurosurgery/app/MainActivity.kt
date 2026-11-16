package com.neurosurgery.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.*
import android.database.Cursor
import android.text.InputType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import java.io.FileOutputStream

class MainActivity : Activity() {

    private lateinit var db: DatabaseHelper
    private lateinit var root: LinearLayout
    private lateinit var patientListContainer: LinearLayout

    private val blue = Color.rgb(21, 75, 135)
    private val lightBlue = Color.rgb(232, 241, 250)
    private val white = Color.WHITE
    private val dark = Color.rgb(35, 45, 55)

    private var selectedStatus = "الكل"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseHelper(this)
        showDashboard()
    }

    private fun createRoot(): LinearLayout {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(245, 247, 250))
            setPadding(16, 16, 16, 16)
        }
        return root
    }

    private fun addHeader(title: String) {
        val header = TextView(this).apply {
            text = title
            textSize = 22f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setBackgroundColor(blue)
            setPadding(12, 20, 12, 20)
        }
        root.addView(header)
    }

    private fun addText(
        value: String,
        size: Float = 16f,
        bold: Boolean = false
    ) {
        val text = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(dark)
            setPadding(8, 10, 8, 10)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(text)
    }

    private fun addButton(
        title: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = title
            setTextColor(white)
            setBackgroundColor(blue)
            textSize = 15f
            setOnClickListener { action() }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 5, 0, 5)
        root.addView(button, params)
    }

    private fun addStatistic(label: String, value: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(white)
            setPadding(16, 12, 16, 12)
        }

        val title = TextView(this).apply {
            text = label
            textSize = 15f
            setTextColor(dark)
        }

        val number = TextView(this).apply {
            text = value
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(blue)
        }

        card.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        card.addView(number)

        val params = LinearLayout.LayoutParams(-1, -2)
        params.setMargins(0, 4, 0, 4)
        root.addView(card, params)
    }

    private fun showDashboard() {
        createRoot()
        addHeader("الجراحة العصبية - Neurosurgery")

        addText("لوحة المعلومات والإحصائيات", 19f, true)

        addStatistic("إجمالي المرضى", db.getPatientCount().toString())
        addStatistic(
            "غير مقرر لهم عمل جراحي",
            db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_NOT_PLANNED).toString()
        )
        addStatistic(
            "قيد التحضير",
            db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_PREPARATION).toString()
        )
        addStatistic(
            "جاهزون للعمل الجراحي",
            db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_READY).toString()
        )
        addStatistic(
            "العمليات المنجزة",
            db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_COMPLETED).toString()
        )
        addStatistic("إجمالي العمليات المسجلة", db.getSurgeryCount().toString())

        addButton("قائمة المرضى") { showPatientList() }
        addButton("إضافة مريض جديد") { showAddPatient() }
        addButton("الأطباء") { showDoctors() }
        addButton("البحث في العمليات الجراحية") { showSurgerySearch() }
        addButton("الإحصائيات التفصيلية") { showDetailedStatistics() }

        setContentView(root)
    }

    private fun showPatientList() {
        createRoot()
        addHeader("قائمة المرضى")

        val statuses = listOf(
            "الكل",
            DatabaseHelper.SURGICAL_NOT_PLANNED,
            DatabaseHelper.SURGICAL_PREPARATION,
            DatabaseHelper.SURGICAL_READY,
            DatabaseHelper.SURGICAL_COMPLETED
        )

        val filter = Spinner(this)
        filter.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            statuses
        )
        filter.setSelection(statuses.indexOf(selectedStatus).coerceAtLeast(0))
        root.addView(filter)

        addButton("إضافة مريض جديد") { showAddPatient() }

        patientListContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this)
        scroll.addView(patientListContainer)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        addButton("العودة للرئيسية") { showDashboard() }
        setContentView(root)

        filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedStatus = statuses[position]
                loadPatients()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        loadPatients()
    }

    private fun loadPatients() {
        patientListContainer.removeAllViews()

        val cursor = db.readableDatabase.rawQuery(
            """
            SELECT p.id, p.full_name, p.file_number,
                   p.diagnosis,
                   COALESCE(s.status, ?) AS surgical_status
            FROM patients p
            LEFT JOIN patient_surgical_status s
            ON p.id = s.patient_id
            ORDER BY p.id DESC
            """.trimIndent(),
            arrayOf(DatabaseHelper.SURGICAL_NOT_PLANNED)
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(0)
                val name = it.getString(1) ?: ""
                val fileNumber = it.getString(2) ?: ""
                val diagnosis = it.getString(3) ?: ""
                val status = it.getString(4) ?: ""

                if (selectedStatus != "الكل" && selectedStatus != status) continue

                val item = TextView(this).apply {
                    text = """
                        $name
                        رقم الملف: $fileNumber
                        التشخيص: $diagnosis
                        الحالة الجراحية: $status
                    """.trimIndent()
                    textSize = 15f
                    setTextColor(dark)
                    setBackgroundColor(white)
                    setPadding(14, 14, 14, 14)
                    setOnClickListener { showPatientDetails(id, name) }
                }

                val params = LinearLayout.LayoutParams(-1, -2)
                params.setMargins(0, 5, 0, 5)
                patientListContainer.addView(item, params)
            }
        }

        if (patientListContainer.childCount == 0) {
            val empty = TextView(this).apply {
                text = "لا يوجد مرضى ضمن هذه الحالة"
                textSize = 16f
                setTextColor(dark)
                gravity = Gravity.CENTER
                setPadding(12, 25, 12, 25)
            }
            patientListContainer.addView(empty)
        }
    }

    private fun createInput(
        hint: String,
        multiline: Boolean = false
    ): EditText {
        val edit = EditText(this).apply {
            this.hint = hint
            textSize = 15f
            setTextColor(dark)
            setHintTextColor(Color.GRAY)
            if (multiline) {
                minLines = 3
                gravity = Gravity.TOP
            }
        }
        root.addView(edit, LinearLayout.LayoutParams(-1, -2))
        return edit
    }

    private fun showAddPatient() {
        createRoot()
        addHeader("إضافة مريض جديد")

        val name = createInput("الاسم الكامل")
        val fileNumber = createInput("رقم الملف")
        val age = createInput("العمر")
        val gender = createInput("الجنس")
        val phone = createInput("رقم الهاتف")
        val address = createInput("العنوان")
        val diagnosis = createInput("التشخيص")
        val specialist = createInput("الطبيب المختص")
        val notes = createInput("ملاحظات", true)

        addText("الحالة الجراحية", 17f, true)
        val statuses = listOf(
            DatabaseHelper.SURGICAL_NOT_PLANNED,
            DatabaseHelper.SURGICAL_PREPARATION,
            DatabaseHelper.SURGICAL_READY,
            DatabaseHelper.SURGICAL_COMPLETED
        )

        val statusSpinner = Spinner(this)
        statusSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            statuses
        )
        root.addView(statusSpinner)

        addButton("حفظ بيانات المريض") {
            val patientName = name.text.toString().trim()
            if (patientName.isEmpty()) {
                name.error = "يرجى إدخال اسم المريض"
                return@addButton
            }

            val id = db.insertPatient(
                patientName,
                fileNumber.text.toString(),
                age.text.toString(),
                gender.text.toString(),
                phone.text.toString(),
                address.text.toString(),
                diagnosis.text.toString(),
                notes.text.toString(),
                specialist.text.toString()
            )

            if (id == -1L) {
                showMessage("تعذر حفظ بيانات المريض")
                return@addButton
            }

            val status = statusSpinner.selectedItem.toString()
            if (status != DatabaseHelper.SURGICAL_NOT_PLANNED) {
                db.savePatientSurgicalStatus(id.toInt(), status)
            }

            showMessage("تم تسجيل المريض بنجاح")
            showPatientDetails(id.toInt(), patientName)
        }

        addButton("رجوع") { showDashboard() }
        setContentView(root)
    }

    private fun showPatientDetails(patientId: Int, patientName: String) {
        createRoot()
        addHeader("ملف المريض")
        addText(patientName, 21f, true)
        addText("رقم المريض: $patientId")

        val cursor = db.readableDatabase.rawQuery(
            """
            SELECT file_number, age, gender, phone,
                   address, diagnosis, notes, specialist
            FROM patients
            WHERE id = ?
            """.trimIndent(),
            arrayOf(patientId.toString())
        )

        cursor.use {
            if (it.moveToFirst()) {
                addText("رقم الملف: ${it.getString(0) ?: ""}")
                addText("العمر: ${it.getString(1) ?: ""}")
                addText("الجنس: ${it.getString(2) ?: ""}")
                addText("الهاتف: ${it.getString(3) ?: ""}")
                addText("العنوان: ${it.getString(4) ?: ""}")
                addText("التشخيص: ${it.getString(5) ?: ""}")
                addText("الملاحظات: ${it.getString(6) ?: ""}")
                addText("الطبيب المختص: ${it.getString(7) ?: ""}")
            }
        }

        val currentStatus = db.getPatientSurgicalStatus(patientId)
        addText("الحالة الجراحية الحالية", 18f, true)
        addText(currentStatus, 16f, true)

        addButton("تعديل الحالة الجراحية") {
            showEditSurgicalStatus(patientId, patientName)
        }
        addButton("تسجيل فحص عصبي") {
            showNeurologicalExam(patientId, patientName)
        }
        addButton("عرض الفحوصات العصبية السابقة") {
            showNeurologicalExams(patientId, patientName)
        }
        addButton("تسجيل تحليل أو استقصاء") {
            showAddInvestigation(patientId, patientName)
        }
        addButton("عرض التحاليل والاستقصاءات") {
            showPatientInvestigations(patientId, patientName)
        }
        addButton("تسجيل عملية جراحية") {
            showAddSurgery(patientId, patientName)
        }
        addButton("عرض العمليات السابقة") {
            showPatientSurgeries(patientId, patientName)
        }
        addButton("العودة لقائمة المرضى") { showPatientList() }

        setContentView(root)
    }

    private fun showEditSurgicalStatus(patientId: Int, patientName: String) {
        createRoot()
        addHeader("تعديل الحالة الجراحية")

        val statuses = listOf(
            DatabaseHelper.SURGICAL_NOT_PLANNED,
            DatabaseHelper.SURGICAL_PREPARATION,
            DatabaseHelper.SURGICAL_READY,
            DatabaseHelper.SURGICAL_COMPLETED
        )

        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            statuses
        )

        val current = db.getPatientSurgicalStatus(patientId)
        spinner.setSelection(statuses.indexOf(current).coerceAtLeast(0))
        root.addView(spinner)

        val surgeryType = createInput("نوع العمل الجراحي المخطط")
        val surgeryDate = createInput("تاريخ العمل الجراحي")
        val surgeon = createInput("الجراح")
        val notes = createInput("ملاحظات", true)

        addButton("حفظ الحالة") {
            val status = spinner.selectedItem.toString()
            val success = db.savePatientSurgicalStatus(
                patientId = patientId,
                status = status,
                plannedType = surgeryType.text.toString(),
                plannedDate = surgeryDate.text.toString(),
                surgeon = surgeon.text.toString(),
                notes = notes.text.toString()
            )

            if (success) {
                showMessage("تم تحديث الحالة الجراحية")
                showPatientDetails(patientId, patientName)
            } else {
                showMessage("تعذر تحديث الحالة")
            }
        }

        addButton("رجوع") { showPatientDetails(patientId, patientName) }
        setContentView(root)
    }

    private fun showMessage(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("حسناً", null)
            .show()
    }

    private fun showNeurologicalExam(patientId: Int, patientName: String) {
        createRoot()
        addHeader("الفحص العصبي التفصيلي")
        addText("المريض: $patientName", 18f, true)

        val examDate = createInput("تاريخ الفحص")
        examDate.setText(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )

        addText("مستوى الوعي و GCS", 18f, true)
        val consciousness = createInput("الوعي، GCS، E1 V1 M1 والمجموع", true)

        addText("الأعصاب القحفية", 18f, true)
        val cranialNerves = createInput(
            "I - XII، الحدقات، حركات العين، الوجه والبلع...",
            true
        )

        addText("القوة العضلية - الطرف العلوي الأيمن", 17f, true)
        val rightUpper = createMotorInputs()
        addText("القوة العضلية - الطرف العلوي الأيسر", 17f, true)
        val leftUpper = createMotorInputs()
        addText("القوة العضلية - الطرف السفلي الأيمن", 17f, true)
        val rightLower = createMotorInputs()
        addText("القوة العضلية - الطرف السفلي الأيسر", 17f, true)
        val leftLower = createMotorInputs()

        addText("الفحص الحسي", 18f, true)
        val rightSensation = createInput("الحس في الطرف الأيمن", true)
        val leftSensation = createInput("الحس في الطرف الأيسر", true)

        addText("المنعكسات", 18f, true)
        val rightReflexes = createInput("المنعكسات في الطرف الأيمن")
        val leftReflexes = createInput("المنعكسات في الطرف الأيسر")

        addText("علامة بابنسكي", 18f, true)
        val rightBabinski = createInput("Babinski يمين")
        val leftBabinski = createInput("Babinski يسار")

        addText("التناسق الحركي", 18f, true)
        val rightCoordination = createInput("التناسق الحركي يمين")
        val leftCoordination = createInput("التناسق الحركي يسار")

        addText("المشية", 18f, true)
        val gait = createInput("وصف المشية", true)
        addText("الانطباع السريري", 18f, true)
        val impression = createInput("Clinical Impression", true)
        val examiner = createInput("اسم الفاحص")
        val notes = createInput("ملاحظات إضافية", true)

        addButton("حفظ الفحص العصبي") {
            val values = ContentValues().apply {
                put("patient_id", patientId)
                put("exam_date", examDate.text.toString())
                put("consciousness", consciousness.text.toString())
                put("cranial_nerves", cranialNerves.text.toString())
                put("right_upper_motor", rightUpper.text.toString())
put("left_upper_motor", leftUpper.text.toString())
put("right_lower_motor", rightLower.text.toString())
put("left_lower_motor", leftLower.text.toString())
                put("right_sensation", rightSensation.text.toString())
                put("left_sensation", leftSensation.text.toString())
                put("right_reflexes", rightReflexes.text.toString())
                put("left_reflexes", leftReflexes.text.toString())
                put("right_babinski", rightBabinski.text.toString())
                put("left_babinski", leftBabinski.text.toString())
                put("right_coordination", rightCoordination.text.toString())
                put("left_coordination", leftCoordination.text.toString())
                put("gait", gait.text.toString())
                put("clinical_impression", impression.text.toString())
                put("examiner_name", examiner.text.toString())
                put("notes", notes.text.toString())
            }

            val result = db.saveNeurologicalExam(values)
            if (result != -1L) {
                showMessage("تم حفظ الفحص العصبي بنجاح")
                showPatientDetails(patientId, patientName)
            } else {
                showMessage("تعذر حفظ الفحص العصبي")
            }
        }

        addButton("رجوع إلى ملف المريض") {
            showPatientDetails(patientId, patientName)
        }
        setContentView(root)
    }

    private fun createMotorInputs(): EditText {
        return createInput(
            "القوة العضلية: الكتف، المرفق، الرسغ، الأصابع، الورك، الركبة، الكاحل، MRC 0-5",
            true
        )
    }

    private val investigationTypes = listOf(
        "CBC", "ESR", "CRP", "RBS", "HbA1c", "Urea",
        "Creatinine", "Na / K", "PT / PTT / INR", "LFT",
        "RFT", "Blood Group", "Crossmatch", "Other"
    )

    private fun showAddInvestigation(patientId: Int, patientName: String) {
        createRoot()
        addHeader("إضافة تحليل أو استقصاء")
        addText("المريض: $patientName", 17f, true)

        val typeSpinner = Spinner(this)
        typeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            investigationTypes
        )
        root.addView(typeSpinner)

        val customName = createInput("اسم التحليل أو الاستقصاء")
        val requestDate = createInput("تاريخ طلب التحليل")
        requestDate.setText(currentDate())
        val resultDate = createInput("تاريخ صدور النتيجة")
        val result = createInput("النتيجة والقيم المخبرية", true)
        val notes = createInput("ملاحظات إضافية", true)

        addButton("حفظ التحليل") {
            val selectedType = typeSpinner.selectedItem.toString()
            val investigationName =
                if (selectedType == "Other") {
                    customName.text.toString().trim()
                } else {
                    if (customName.text.toString().trim().isEmpty()) {
                        selectedType
                    } else {
                        customName.text.toString().trim()
                    }
                }

            if (investigationName.isEmpty()) {
                customName.error = "أدخل اسم التحليل"
                return@addButton
            }

            val values = ContentValues().apply {
                put("patient_id", patientId)
                put("investigation_type", selectedType)
                put("investigation_name", investigationName)
                put("request_date", requestDate.text.toString())
                put("result_date", resultDate.text.toString())
                put("result", result.text.toString())
                put("notes", notes.text.toString())
            }

            val id = db.saveInvestigation(values)
            if (id != -1L) {
                showMessage("تم حفظ التحليل بنجاح")
                showPatientInvestigations(patientId, patientName)
            } else {
                showMessage("تعذر حفظ التحليل")
            }
        }

        addButton("إرفاق صورة نتيجة") {
            openInvestigationImagePicker(patientId, patientName)
        }
        addButton("عرض تحاليل المريض") {
            showPatientInvestigations(patientId, patientName)
        }
        addButton("رجوع") { showPatientDetails(patientId, patientName) }
        setContentView(root)
    }

    private fun currentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private var pendingImagePatientId = -1
    private var pendingImagePatientName = ""
    private var pendingInvestigationId = -1

    private fun openInvestigationImagePicker(
        patientId: Int,
        patientName: String,
        investigationId: Int = -1
    ) {
        pendingImagePatientId = patientId
        pendingImagePatientName = patientName
        pendingInvestigationId = investigationId

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, 2001)
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2001 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            try {
                val savedPath = copyImageToInternalStorage(
                    uri,
                    pendingImagePatientId
                )

                val values = ContentValues().apply {
                    put("patient_id", pendingImagePatientId)
                    put("image_type", "نتيجة تحليل")
                    put("image_name", "صورة نتيجة")
                    put("image_path", savedPath)
                    put("image_date", currentDate())
                    put("description", "مرفق طبي")
                    if (pendingInvestigationId != -1) {
                        put("investigation_id", pendingInvestigationId)
                    }
                }

                val id = db.savePatientImage(values)
                if (id != -1L) {
                    showMessage("تم حفظ الصورة بنجاح")
                    showPatientInvestigations(
                        pendingImagePatientId,
                        pendingImagePatientName
                    )
                } else {
                    showMessage("تعذر حفظ الصورة في قاعدة البيانات")
                }
            } catch (e: Exception) {
                showMessage("حدث خطأ أثناء حفظ الصورة: ${e.message}")
            }
        }
    }

    private fun copyImageToInternalStorage(uri: Uri, patientId: Int): String {
        val directory = File(filesDir, "patient_images/$patientId")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, "image_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "تعذر قراءة الصورة" }
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    private fun showPatientInvestigations(patientId: Int, patientName: String) {
        createRoot()
        addHeader("تحاليل واستقصاءات المريض")
        addText(patientName, 18f, true)

        addButton("إضافة تحليل جديد") {
            showAddInvestigation(patientId, patientName)
        }

        val cursor = db.readableDatabase.rawQuery(
            """
            SELECT id, investigation_type, investigation_name,
                   request_date, result_date, result, notes
            FROM investigations
            WHERE patient_id = ?
            ORDER BY id DESC
            """.trimIndent(),
            arrayOf(patientId.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                val investigationId = it.getInt(0)
                val type = it.getString(1) ?: ""
                val name = it.getString(2) ?: ""
                val request = it.getString(3) ?: ""
                val date = it.getString(4) ?: ""
                val result = it.getString(5) ?: ""
                val notes = it.getString(6) ?: ""

                addText(
                    """
                    $name ($type)
                    تاريخ الطلب: $request
                    تاريخ النتيجة: $date
                    النتيجة: $result
                    ملاحظات: $notes
                    """.trimIndent(),
                    15f
                )

                addButton("إرفاق صورة لهذه النتيجة") {
                    openInvestigationImagePicker(
                        patientId,
                        patientName,
                        investigationId
                    )
                }
                addInvestigationImages(investigationId)
            }
        }

        addButton("رجوع إلى ملف المريض") {
            showPatientDetails(patientId, patientName)
        }
        setContentView(root)
    }

    private fun addInvestigationImages(investigationId: Int) {
        val cursor = db.readableDatabase.rawQuery(
            """
            SELECT image_name, image_path, image_date
            FROM patient_images
            WHERE investigation_id = ?
            ORDER BY id DESC
            """.trimIndent(),
            arrayOf(investigationId.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                val imageName = it.getString(0) ?: "صورة"
                val path = it.getString(1) ?: ""
                val date = it.getString(2) ?: ""
                addText("المرفق: $imageName - $date")
                addButton("فتح الصورة") { openSavedImage(path) }
            }
        }
    }

    private fun openSavedImage(path: String) {
        val file = File(path)
        if (!file.exists()) {
            showMessage("الصورة غير موجودة")
            return
        }

        try {
            val bitmap = android.graphics.BitmapFactory.decodeFile(path)
            if (bitmap == null) {
                showMessage("تعذر قراءة الصورة")
                return
            }

            val imageView = ImageView(this).apply {
                setImageBitmap(bitmap)
                adjustViewBounds = true
            }

            AlertDialog.Builder(this)
                .setTitle("عرض الصورة")
                .setView(ScrollView(this).apply { addView(imageView) })
                .setPositiveButton("إغلاق", null)
                .show()
        } catch (e: Exception) {
            showMessage("تعذر فتح الصورة")
        }
    }

    private fun showAddSurgery(patientId: Int, patientName: String) {
        createRoot()
        addHeader("تسجيل عملية جراحية")
        addText("المريض: $patientName", 18f, true)

        val surgeryDate = createInput("تاريخ العملية")
        surgeryDate.setText(currentDate())
        val surgeryType = createInput("نوع العملية")
        val surgeon = createInput("الجراح الرئيسي")
        val assistants = createInput("الجراحون المساعدون")
        val anesthetist = createInput("طبيب التخدير")
        val anesthesiaType = createInput("نوع التخدير")

        addText("التشخيص والاستطباب", 18f, true)
        val preopDiagnosis = createInput("التشخيص قبل العملية", true)
        val postoperativeDiagnosis = createInput("التشخيص بعد العملية", true)
        val indication = createInput("استطباب العمل الجراحي", true)
        val urgency = createInput("نوع العملية: إسعافية أو انتخابية")

        addText("التفاصيل الجراحية", 18f, true)
        val patientPosition = createInput("وضعية المريض")
        val approach = createInput("المدخل الجراحي")
        val procedureDetails = createInput("تفاصيل الإجراء الجراحي", true)
        val findings = createInput("الموجودات أثناء العملية", true)
        val implants = createInput("الزرعات والمواد المستخدمة", true)

        addText("المضاعفات والنتيجة", 18f, true)
        val complications = createInput("المضاعفات أثناء العملية", true)
        val postoperativeComplications = createInput("المضاعفات بعد العملية", true)
        val outcome = createInput("نتيجة العملية")
        val patientCondition = createInput("حالة المريض عند انتهاء العملية", true)

        addText("البيانات الإضافية", 18f, true)
        val duration = createInput("مدة العملية بالدقائق")
        duration.inputType = InputType.TYPE_CLASS_NUMBER
        val bloodLoss = createInput("كمية الدم المفقود")
        val transfusion = createInput("نقل الدم")
        val followupPlan = createInput("خطة المتابعة بعد العملية", true)
        val followupDate = createInput("تاريخ المتابعة")
        val report = createInput("التقرير الجراحي الكامل", true)
        val notes = createInput("ملاحظات إضافية", true)

        addButton("حفظ العملية الجراحية") {
            if (surgeryType.text.toString().trim().isEmpty()) {
                surgeryType.error = "أدخل نوع العملية"
                return@addButton
            }
            if (surgeon.text.toString().trim().isEmpty()) {
                surgeon.error = "أدخل اسم الجراح"
                return@addButton
            }

            val surgeonName = surgeon.text.toString().trim()
            val surgeonId = db.getDoctorId(surgeonName)

            val values = ContentValues().apply {
                put("patient_id", patientId)
                put("surgery_date", surgeryDate.text.toString())
                put("surgery_type", surgeryType.text.toString())
                put("surgeon", surgeonName)
                if (surgeonId != -1L) put("surgeon_id", surgeonId)
                put("assistants", assistants.text.toString())
                put("anesthetist", anesthetist.text.toString())
                put("anesthesia_type", anesthesiaType.text.toString())
                put("preop_diagnosis", preopDiagnosis.text.toString())
                put("postoperative_diagnosis", postoperativeDiagnosis.text.toString())
                put("indication", indication.text.toString())
                put("urgency", urgency.text.toString())
                put("patient_position", patientPosition.text.toString())
                put("surgical_approach", approach.text.toString())
                put("procedure_details", procedureDetails.text.toString())
                put("intraoperative_findings", findings.text.toString())
                put("implants", implants.text.toString())
                put("complications", complications.text.toString())
                put("postoperative_complications", postoperativeComplications.text.toString())
                put("outcome", outcome.text.toString())
                put("patient_condition", patientCondition.text.toString())

                val durationValue = duration.text.toString().trim().toIntOrNull()
                if (durationValue != null) put("duration_minutes", durationValue)

                put("estimated_blood_loss", bloodLoss.text.toString())
                put("blood_transfusion", transfusion.text.toString())
                put("followup_plan", followupPlan.text.toString())
                put("followup_date", followupDate.text.toString())
                put("report", report.text.toString())
                put("notes", notes.text.toString())
            }

            val id = db.saveSurgery(values)
            if (id != -1L) {
                db.savePatientSurgicalStatus(
                    patientId = patientId,
                    status = DatabaseHelper.SURGICAL_COMPLETED,
                    plannedType = surgeryType.text.toString(),
                    plannedDate = surgeryDate.text.toString(),
                    surgeon = surgeonName,
                    notes = "تم تسجيل عملية جراحية برقم $id"
                )
                showMessage("تم حفظ العملية وتحديث حالة المريض")
                showPatientSurgeries(patientId, patientName)
            } else {
                showMessage("تعذر حفظ العملية الجراحية")
            }
        }

        addButton("رجوع إلى ملف المريض") {
            showPatientDetails(patientId, patientName)
        }
        setContentView(root)
    }

    private fun showPatientSurgeries(patientId: Int, patientName: String) {
        createRoot()
        addHeader("السجل الجراحي للمريض")
        addText(patientName, 18f, true)

        addButton("تسجيل عملية جديدة") {
            showAddSurgery(patientId, patientName)
        }

        val cursor = db.readableDatabase.rawQuery(
            """
            SELECT id, surgery_date, surgery_type, surgeon,
                   preop_diagnosis, postoperative_diagnosis,
                   outcome, complications, report
            FROM surgeries
            WHERE patient_id = ?
            ORDER BY surgery_date DESC, id DESC
            """.trimIndent(),
            arrayOf(patientId.toString())
        )

        var count = 0
        cursor.use {
            while (it.moveToNext()) {
                count++
                val surgeryId = it.getInt(0)
                val date = it.getString(1) ?: ""
                val type = it.getString(2) ?: ""
                val surgeon = it.getString(3) ?: ""
                val preop = it.getString(4) ?: ""
                val postop = it.getString(5) ?: ""
                val outcome = it.getString(6) ?: ""
                val complications = it.getString(7) ?: ""

                val card = TextView(this).apply {
                    text = """
                        العملية رقم: $surgeryId
                        التاريخ: $date
                        نوع العملية: $type
                        الجراح: $surgeon
                        التشخيص قبل العملية: $preop
                        التشخيص بعد العملية: $postop
                        النتيجة: $outcome
                        المضاعفات: $complications
                    """.trimIndent()
                    textSize = 15f
                    setTextColor(dark)
                    setBackgroundColor(white)
                    setPadding(14, 14, 14, 14)
                    setOnClickListener {
                        showSurgeryReport(surgeryId, patientId, patientName)
                    }
                }

                val params = LinearLayout.LayoutParams(-1, -2)
                params.setMargins(0, 6, 0, 6)
                root.addView(card, params)
            }
        }

        if (count == 0) addText("لا توجد عمليات جراحية مسجلة لهذا المريض")
        addButton("رجوع إلى ملف المريض") {
            showPatientDetails(patientId, patientName)
        }
        setContentView(root)
    }

    private fun showSurgeryReport(
        surgeryId: Int,
        patientId: Int,
        patientName: String
    ) {
        createRoot()
        addHeader("تفاصيل التقرير الجراحي")

        val cursor = db.readableDatabase.rawQuery(
            "SELECT * FROM surgeries WHERE id = ?",
            arrayOf(surgeryId.toString())
        )

        cursor.use {
            if (it.moveToFirst()) {
                fun value(column: String): String {
                    val index = it.getColumnIndex(column)
                    return if (index >= 0) it.getString(index) ?: "" else ""
                }

                val fields = listOf(
                    "surgery_date" to "تاريخ العملية",
                    "surgery_type" to "نوع العملية",
                    "surgeon" to "الجراح",
                    "assistants" to "المساعدون",
                    "anesthetist" to "طبيب التخدير",
                    "anesthesia_type" to "نوع التخدير",
                    "preop_diagnosis" to "التشخيص قبل العملية",
                    "postoperative_diagnosis" to "التشخيص بعد العملية",
                    "indication" to "الاستطباب",
                    "urgency" to "درجة الاستعجال",
                    "patient_position" to "وضعية المريض",
                    "surgical_approach" to "المدخل الجراحي",
                    "procedure_details" to "تفاصيل العملية",
                    "intraoperative_findings" to "الموجودات أثناء العملية",
                    "implants" to "الزرعات",
                    "complications" to "المضاعفات أثناء العملية",
                    "postoperative_complications" to "المضاعفات بعد العملية",
                    "outcome" to "النتيجة",
                    "patient_condition" to "حالة المريض",
                    "duration_minutes" to "المدة بالدقائق",
                    "estimated_blood_loss" to "فقد الدم",
                    "blood_transfusion" to "نقل الدم",
                    "followup_plan" to "خطة المتابعة",
                    "followup_date" to "تاريخ المتابعة",
                    "report" to "التقرير الكامل",
                    "notes" to "ملاحظات"
                )
                for ((column, label) in fields) {
                    addText("$label: ${value(column)}")
                }
            }
        }

        addButton("رجوع للسجل الجراحي") {
            showPatientSurgeries(patientId, patientName)
        }
        setContentView(root)
    }

    // ==============================
    // إدارة الأطباء
    // ==============================

    private fun showDoctors() {
        createRoot()
        addHeader("إدارة الأطباء")

        val doctorName = createInput("اسم الطبيب")
        addButton("إضافة طبيب") {
            val name = doctorName.text.toString().trim()
            if (name.isEmpty()) {
                doctorName.error = "أدخل اسم الطبيب"
                return@addButton
            }

            val result = db.addDoctor(name)
            if (result != -1L) {
                showMessage("تمت إضافة الطبيب")
                showDoctors()
            } else {
                showMessage("تعذرت إضافة الطبيب")
            }
        }

        addText("قائمة الأطباء", 18f, true)
        val doctors = db.getDoctors()
        if (doctors.isEmpty()) {
            addText("لا يوجد أطباء مسجلون")
        } else {
            doctors.forEachIndexed { index, doctor ->
                addText("${index + 1}. $doctor")
            }
        }

        addButton("العودة للرئيسية") { showDashboard() }
        setContentView(root)
    }

    // ==============================
    // عرض الفحوصات العصبية السابقة
    // ==============================

    private fun showNeurologicalExams(patientId: Int, patientName: String) {
        createRoot()
        addHeader("الفحوصات العصبية السابقة")
        addText(patientName, 18f, true)

        val cursor = db.readableDatabase.rawQuery(
            """
            SELECT *
            FROM neurological_exams
            WHERE patient_id = ?
            ORDER BY exam_date DESC, id DESC
            """.trimIndent(),
            arrayOf(patientId.toString())
        )

        var count = 0
        cursor.use {
            while (it.moveToNext()) {
                count++

                fun value(column: String): String {
                    val index = it.getColumnIndex(column)
                    return if (index >= 0) it.getString(index) ?: "" else ""
                }

                val examId = value("id")
                val details = """
                    تاريخ الفحص: ${value("exam_date")}

                    الوعي و GCS:
                    ${value("consciousness")}

                    الأعصاب القحفية:
                    ${value("cranial_nerves")}

                    القوة العضلية - الطرف العلوي الأيمن:
                    ${value("right_upper_motor")}

                    القوة العضلية - الطرف العلوي الأيسر:
                    ${value("left_upper_motor")}

                    القوة العضلية - الطرف السفلي الأيمن:
                    ${value("right_lower_motor")}

                    القوة العضلية - الطرف السفلي الأيسر:
                    ${value("left_lower_motor")}

                    الحس الأيمن:
                    ${value("right_sensation")}

                    الحس الأيسر:
                    ${value("left_sensation")}

                    المنعكسات اليمنى:
                    ${value("right_reflexes")}

                    المنعكسات اليسرى:
                    ${value("left_reflexes")}

                    Babinski يمين:
                    ${value("right_babinski")}

                    Babinski يسار:
                    ${value("left_babinski")}

                    التناسق الحركي الأيمن:
                    ${value("right_coordination")}

                    التناسق الحركي الأيسر:
                    ${value("left_coordination")}

                    المشية:
                    ${value("gait")}

                    الانطباع السريري:
                    ${value("clinical_impression")}

                    الفاحص:
                    ${value("examiner_name")}

                    الملاحظات:
                    ${value("notes")}
                """.trimIndent()

                val card = TextView(this).apply {
                    text = "فحص رقم: $examId\n\n$details"
                    textSize = 14f
                    setTextColor(dark)
                    setBackgroundColor(white)
                    setPadding(14, 14, 14, 14)
                }

                val params = LinearLayout.LayoutParams(-1, -2)
                params.setMargins(0, 6, 0, 6)
                root.addView(card, params)
            }
        }

        if (count == 0) addText("لا توجد فحوصات عصبية سابقة")
        addButton("إجراء فحص عصبي جديد") {
            showNeurologicalExam(patientId, patientName)
        }
        addButton("العودة إلى ملف المريض") {
            showPatientDetails(patientId, patientName)
        }
        setContentView(root)
    }

    // ==============================
    // البحث في العمليات الجراحية
    // ==============================

    private fun showSurgerySearch() {
        createRoot()
        addHeader("البحث في العمليات الجراحية")

        val surgeonInput = createInput("اسم الجراح")
        val typeInput = createInput("نوع العملية")
        val fromDate = createInput("من تاريخ YYYY-MM-DD")
        val toDate = createInput("إلى تاريخ YYYY-MM-DD")

        val results = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val scroll = ScrollView(this)
        scroll.addView(results)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        addButton("بحث") {
            results.removeAllViews()
            val conditions = mutableListOf<String>()
            val args = mutableListOf<String>()

            if (surgeonInput.text.toString().trim().isNotEmpty()) {
                conditions.add("surgeon LIKE ?")
                args.add("%${surgeonInput.text.toString().trim()}%")
            }
            if (typeInput.text.toString().trim().isNotEmpty()) {
                conditions.add("surgery_type LIKE ?")
                args.add("%${typeInput.text.toString().trim()}%")
            }
            if (fromDate.text.toString().trim().isNotEmpty()) {
                conditions.add("surgery_date >= ?")
                args.add(fromDate.text.toString().trim())
            }
            if (toDate.text.toString().trim().isNotEmpty()) {
                conditions.add("surgery_date <= ?")
                args.add(toDate.text.toString().trim())
            }

            val where = if (conditions.isEmpty()) {
                ""
            } else {
                "WHERE " + conditions.joinToString(" AND ")
            }

            val cursor = db.readableDatabase.rawQuery(
                """
                SELECT id, patient_id, surgery_date,
                       surgery_type, surgeon
                FROM surgeries
                $where
                ORDER BY surgery_date DESC
                """.trimIndent(),
                args.toTypedArray()
            )

            var count = 0
            cursor.use {
                while (it.moveToNext()) {
                    count++
                    val surgeryId = it.getInt(0)
                    val patientId = it.getInt(1)
                    val date = it.getString(2) ?: ""
                    val type = it.getString(3) ?: ""
                    val surgeon = it.getString(4) ?: ""

                    val item = TextView(this).apply {
                        text = """
                            العملية رقم: $surgeryId
                            المريض رقم: $patientId
                            التاريخ: $date
                            النوع: $type
                            الجراح: $surgeon
                        """.trimIndent()
                        textSize = 15f
                        setTextColor(dark)
                        setBackgroundColor(white)
                        setPadding(12, 12, 12, 12)

                        setOnClickListener {
                            val nameCursor = db.readableDatabase.rawQuery(
                                "SELECT full_name FROM patients WHERE id = ?",
                                arrayOf(patientId.toString())
                            )
                            var patientName = ""
                            nameCursor.use { c ->
                                if (c.moveToFirst()) {
                                    patientName = c.getString(0) ?: ""
                                }
                            }
                            showSurgeryReport(surgeryId, patientId, patientName)
                        }
                    }

                    val params = LinearLayout.LayoutParams(-1, -2)
                    params.setMargins(0, 5, 0, 5)
                    results.addView(item, params)
                }
            }

            if (count == 0) {
                val empty = TextView(this).apply {
                    text = "لا توجد نتائج مطابقة"
                    textSize = 16f
                    setTextColor(dark)
                    gravity = Gravity.CENTER
                    setPadding(12, 20, 12, 20)
                }
                results.addView(empty)
            }
        }

        addButton("العودة للرئيسية") { showDashboard() }
        setContentView(root)
    }

    // ==============================
    // الإحصائيات التفصيلية
    // ==============================

    private fun showDetailedStatistics() {
        createRoot()
        addHeader("الإحصائيات التفصيلية")

        val database = db.readableDatabase

        fun getCount(query: String): Int {
            val cursor = database.rawQuery(query, null)
            return cursor.use {
                if (it.moveToFirst()) it.getInt(0) else 0
            }
        }

        addText("إجمالي المرضى: ${db.getPatientCount()}", 18f, true)
        addText("إجمالي العمليات: ${db.getSurgeryCount()}", 18f, true)

        addText(
            "المرضى غير المقرر لهم عمل جراحي: ${
                db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_NOT_PLANNED)
            }"
        )
        addText(
            "المرضى قيد التحضير: ${
                db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_PREPARATION)
            }"
        )
        addText(
            "المرضى الجاهزون للعمل الجراحي: ${
                db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_READY)
            }"
        )
        addText(
            "المرضى الذين أُجريت لهم عمليات: ${
                db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_COMPLETED)
            }"
        )

        addText("عدد العمليات حسب النوع", 18f, true)
        val typeCursor = database.rawQuery(
            """
            SELECT surgery_type, COUNT(*)
            FROM surgeries
            GROUP BY surgery_type
            ORDER BY COUNT(*) DESC
            """.trimIndent(),
            null
        )

        var hasTypes = false
        typeCursor.use {
            while (it.moveToNext()) {
                hasTypes = true
                val type = it.getString(0) ?: "غير محدد"
                val count = it.getInt(1)
                addText("$type: $count")
            }
        }
        if (!hasTypes) addText("لا توجد عمليات مسجلة")

        addText("عدد العمليات حسب الجراح", 18f, true)
        val surgeonCursor = database.rawQuery(
            """
            SELECT surgeon, COUNT(*)
            FROM surgeries
            GROUP BY surgeon
            ORDER BY COUNT(*) DESC
            """.trimIndent(),
            null
        )

        var hasSurgeons = false
        surgeonCursor.use {
            while (it.moveToNext()) {
                hasSurgeons = true
                val surgeon = it.getString(0) ?: "غير محدد"
                val count = it.getInt(1)
                addText("$surgeon: $count")
            }
        }
        if (!hasSurgeons) addText("لا توجد بيانات جراحين")

        addText("الاختلاطات المسجلة", 18f, true)
        val complications = getCount(
            """
            SELECT COUNT(*)
            FROM surgeries
            WHERE
                (complications IS NOT NULL AND TRIM(complications) != '')
                OR
                (postoperative_complications IS NOT NULL
                 AND TRIM(postoperative_complications) != '')
            """.trimIndent()
        )
        addText("عدد العمليات ذات الاختلاطات المسجلة: $complications")

        addButton("العودة للرئيسية") { showDashboard() }
        setContentView(root)
    }
}
