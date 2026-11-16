
package com.neurosurgery.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.content.ContentValues
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import android.text.InputType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var db: DatabaseHelper
    private lateinit var root: LinearLayout

    private val blue = Color.rgb(20, 75, 135)
    private val lightBlue = Color.rgb(232, 241, 250)
    private val dark = Color.rgb(35, 45, 55)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseHelper(this)
        home()
    }

    private fun screen(title: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 24)
            setBackgroundColor(Color.WHITE)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val header = TextView(this).apply {
            text = title
            textSize = 23f
            setTextColor(Color.WHITE)
            setBackgroundColor(blue)
            gravity = Gravity.CENTER
            setPadding(12, 20, 12, 20)
        }
        root.addView(header)

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun text(value: String, size: Float = 16f): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(dark)
            setPadding(4, 10, 4, 10)
        }
    }

    private fun heading(value: String) {
        root.addView(TextView(this).apply {
            text = value
            textSize = 19f
            setTextColor(blue)
            setPadding(4, 18, 4, 8)
        })
    }

    private fun button(
        value: String,
        action: () -> Unit
    ) {
        root.addView(Button(this).apply {
            text = value
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(blue)
            setOnClickListener { action() }
        })
    }

    private fun input(
        hint: String,
        type: Int = InputType.TYPE_CLASS_TEXT
    ): EditText {
        val edit = EditText(this)
        edit.hint = hint
        edit.textSize = 16f
        edit.inputType = type
        edit.setPadding(10, 8, 10, 8)
        root.addView(edit)
        return edit
    }

    private fun spinner(options: List<String>): Spinner {
        val s = Spinner(this)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options
        )
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        s.adapter = adapter
        root.addView(s)
        return s
    }

    private fun dateNow(): String {
        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(Date())
    }

    private fun home() {
        screen("الجراحة العصبية\nNeurosurgery")
        root.addView(text("نظام إدارة قسم الجراحة العصبية", 18f))
        root.addView(text("الإصدار التجريبي Beta 1.0"))

        button("1 - إدارة المرضى") { patients() }
        button("2 - الفحص العصبي") { choosePatientForExam() }
        button("3 - الزيارات والمتابعة") {
            placeholder("الزيارات والمتابعة")
        }
        button("4 - الاستقصاءات والصور") {
            placeholder("الاستقصاءات والصور")
        }
        button("5 - العمليات الجراحية") {
            placeholder("العمليات الجراحية")
        }
        button("6 - الأطباء والكادر الطبي") { doctors() }
        button("7 - التقارير والإحصائيات") { statistics() }
        button("8 - الإعدادات والنسخ الاحتياطي") {
            placeholder("الإعدادات والنسخ الاحتياطي")
        }
    }

    private fun backButton() {
        button("العودة إلى القائمة الرئيسية") { home() }
    }

    private fun placeholder(title: String) {
        screen(title)
        root.addView(text("هذا القسم مخصص للتطوير في الإصدارات القادمة."))
        backButton()
    }

    // ---------------- PATIENTS ----------------

    private fun patients() {
        screen("إدارة المرضى")

        button("إضافة مريض جديد") { addPatient() }

        heading("البحث عن مريض")
        val search = input("الاسم أو رقم الملف")
        search.setSingleLine(true)

        val results = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(results)

        fun loadPatients(query: String) {
            results.removeAllViews()

            val database = db.readableDatabase
            val cursor = database.rawQuery(
                """
                SELECT id, full_name, file_number, diagnosis
                FROM patients
                WHERE full_name LIKE ? OR file_number LIKE ?
                ORDER BY id DESC
                """.trimIndent(),
                arrayOf("%$query%", "%$query%")
            )

            if (cursor.count == 0) {
                results.addView(text("لا توجد نتائج."))
            }

            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val name = cursor.getString(1) ?: ""
                val file = cursor.getString(2) ?: ""
                val diagnosis = cursor.getString(3) ?: ""

                val item = Button(this).apply {
                    text = "$name\nرقم الملف: $file\nالتشخيص: $diagnosis"
                    textSize = 15f
                    setTextColor(blue)
                    setBackgroundColor(lightBlue)
                    setOnClickListener { patientDetails(id) }
                }
                results.addView(item)
            }
            cursor.close()
        }

        button("بحث") {
            loadPatients(search.text.toString().trim())
        }

        loadPatients("")
        backButton()
    }

    private fun addPatient() {
        screen("إضافة مريض جديد")

        val name = input("الاسم الكامل")
        val fileNumber = input("رقم الملف")
        val age = input(
            "العمر",
            InputType.TYPE_CLASS_NUMBER
        )
        heading("الجنس")
        val gender = spinner(listOf("ذكر", "أنثى"))

        val phone = input(
            "رقم الهاتف",
            InputType.TYPE_CLASS_PHONE
        )
        val address = input("العنوان")
        val diagnosis = input("التشخيص")
        val notes = input("ملاحظات")

        button("حفظ بيانات المريض") {
            if (name.text.toString().trim().isEmpty()) {
                name.error = "يرجى إدخال اسم المريض"
                return@button
            }

            val result = db.insertPatient(
                name.text.toString().trim(),
                fileNumber.text.toString().trim(),
                age.text.toString().trim(),
                gender.selectedItem.toString(),
                phone.text.toString().trim(),
                address.text.toString().trim(),
                diagnosis.text.toString().trim(),
                notes.text.toString().trim()
            )

            if (result != -1L) {
                AlertDialog.Builder(this)
                    .setMessage("تم حفظ بيانات المريض بنجاح.")
                    .setPositiveButton("موافق") { _, _ -> patients() }
                    .show()
            } else {
                AlertDialog.Builder(this)
                    .setMessage("تعذر حفظ بيانات المريض.")
                    .setPositiveButton("موافق", null)
                    .show()
            }
        }

        backButton()
    }

    private fun patientDetails(patientId: Int) {
        screen("ملف المريض")

        val cursor = db.readableDatabase.rawQuery(
            """
            SELECT full_name, file_number, age, gender,
                   phone, address, diagnosis, notes
            FROM patients WHERE id = ?
            """.trimIndent(),
            arrayOf(patientId.toString())
        )

        if (cursor.moveToFirst()) {
            heading(cursor.getString(0) ?: "")
            val labels = listOf(
                "رقم الملف",
                "العمر",
                "الجنس",
                "الهاتف",
                "العنوان",
                "التشخيص",
                "ملاحظات"
            )

            for (i in labels.indices) {
                root.addView(
                    text("${labels[i]}: ${cursor.getString(i + 1) ?: ""}")
                )
            }
        }
        cursor.close()

        button("إجراء فحص عصبي جديد") {
            neurologicalExam(patientId)
        }

        button("عرض الفحوصات السابقة") {
            previousExams(patientId)
        }

        backButton()
    }

    // ---------------- NEUROLOGICAL EXAM ----------------

    private fun choosePatientForExam() {
        screen("اختيار المريض للفحص العصبي")

        val results = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(results)

        val cursor = db.readableDatabase.rawQuery(
            "SELECT id, full_name, file_number FROM patients ORDER BY id DESC",
            null
        )

        if (cursor.count == 0) {
            root.addView(text("لا يوجد مرضى مسجلون."))
        }

        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val name = cursor.getString(1) ?: ""
            val file = cursor.getString(2) ?: ""

            results.addView(Button(this).apply {
                text = "$name - $file"
                setOnClickListener { neurologicalExam(id) }
            })
        }
        cursor.close()

        backButton()
    }

    private fun neurologicalExam(patientId: Int) {
        screen("الفحص العصبي")

        root.addView(text("تاريخ الفحص: ${dateNow()}"))

        heading("مستوى الوعي")
        val consciousnessField = spinner(
            listOf(
                "Alert - يقظ",
                "Drowsy - نعسان",
                "Confused - مشوش",
                "Stuporous - سبات",
                "Comatose - غيبوبة"
            )
        )

        val gcs = input(
            "GCS (من 3 إلى 15)",
            InputType.TYPE_CLASS_NUMBER
        )

        heading("الأعصاب القحفية Cranial Nerves")

        val cranial = mutableListOf<Triple<String, Spinner, EditText>>()

        val nerveNames = listOf(
            "I - Olfactory",
            "II - Optic",
            "III - Oculomotor",
            "IV - Trochlear",
            "V - Trigeminal",
            "VI - Abducens",
            "VII - Facial",
            "VIII - Vestibulocochlear",
            "IX - Glossopharyngeal",
            "X - Vagus",
            "XI - Accessory",
            "XII - Hypoglossal"
        )

        for (nerve in nerveNames) {
            heading(nerve)
            val status = spinner(
                listOf("Normal", "Abnormal", "Not tested")
            )
            val note = input("ملاحظات $nerve")
            cranial.add(Triple(nerve, status, note))
        }

        heading("القوة العضلية Motor Power")

        val motorEntries =
            mutableListOf<Triple<String, String, Spinner>>()

        fun addMotorGroup(group: String, side: String, muscles: List<String>) {
            heading("$group - $side")
            for (muscle in muscles) {
                root.addView(text(muscle))
                val grade = spinner(
                    listOf(
                        "Not tested",
                        "0",
                        "1",
                        "2",
                        "3",
                        "4",
                        "5"
                    )
                )
                motorEntries.add(Triple(group, side, grade))
            }
        }

        val upperMuscles = listOf(
            "Deltoid",
            "Biceps",
            "Triceps",
            "Wrist extensors",
            "Finger flexors"
        )

        val lowerMuscles = listOf(
            "Iliopsoas",
            "Quadriceps",
            "Hamstrings",
            "Ankle dorsiflexors",
            "Plantar flexors"
        )

        addMotorGroup("Upper", "Right", upperMuscles)
        addMotorGroup("Upper", "Left", upperMuscles)
        addMotorGroup("Lower", "Right", lowerMuscles)
        addMotorGroup("Lower", "Left", lowerMuscles)

        fun motorText(group: String, side: String): String {
            return motorEntries
                .filter { it.first == group && it.second == side }
                .joinToString("\n") {
                    "${it.third.tag ?: ""}: ${it.third.selectedItem}"
                }
        }

        // ---------------- SENSATION ----------------

        heading("الإحساس Sensory Examination")

        heading("Right Side")
        val rightSensory = spinner(
            listOf(
                "Not tested",
                "Normal",
                "Reduced",
                "Absent",
                "Increased"
            )
        )
        val rightSensoryNote = input("ملاحظات الإحساس في الجانب الأيمن")

        heading("Left Side")
        val leftSensory = spinner(
            listOf(
                "Not tested",
                "Normal",
                "Reduced",
                "Absent",
                "Increased"
            )
        )
        val leftSensoryNote = input("ملاحظات الإحساس في الجانب الأيسر")

        // ---------------- REFLEXES ----------------

        heading("المنعكسات Reflexes")

        heading("Right Reflexes")
        val rightReflexes = spinner(
            listOf(
                "Not tested",
                "Normal",
                "Brisk",
                "Reduced",
                "Absent"
            )
        )
        val rightBabinski = spinner(
            listOf("Not tested", "Negative", "Positive")
        )

        heading("Left Reflexes")
        val leftReflexes = spinner(
            listOf(
                "Not tested",
                "Normal",
                "Brisk",
                "Reduced",
                "Absent"
            )
        )
        val leftBabinski = spinner(
            listOf("Not tested", "Negative", "Positive")
        )

        val reflexNotes = input("ملاحظات المنعكسات")

        // ---------------- COORDINATION ----------------

        heading("التناسق الحركي Coordination")

        heading("Right")
        val rightCoordination = spinner(
            listOf(
                "Not tested",
                "Normal",
                "Abnormal",
                "Ataxia"
            )
        )

        heading("Left")
        val leftCoordination = spinner(
            listOf(
                "Not tested",
                "Normal",
                "Abnormal",
                "Ataxia"
            )
        )

        heading("المشي والتوازن Gait")
        val gait = spinner(
            listOf(
                "Not tested",
                "Normal",
                "Abnormal",
                "Ataxic gait",
                "Unsteady gait"
            )
        )

        // ---------------- IMPRESSION ----------------

        heading("الانطباع السريري Clinical Impression")
        val impression = input("الانطباع السريري")

        heading("الطبيب الفاحص")
        val doctors = db.getDoctors().toMutableList()
        if (doctors.isEmpty()) {
            doctors.add("Not specified")
        }
        val examiner = spinner(doctors)

        button("إضافة طبيب جديد") {
            addDoctorDialog {
                neurologicalExam(patientId)
            }
        }

        heading("ملاحظات إضافية")
        val notes = input("ملاحظات")

        button("حفظ الفحص العصبي") {
            val gcsValue = gcs.text.toString().trim()
            if (gcsValue.isNotEmpty()) {
                val number = gcsValue.toIntOrNull()
                if (number == null || number !in 3..15) {
                    gcs.error = "أدخل قيمة من 3 إلى 15"
                    return@button
                }
            }

            val cranialText = cranial.joinToString("\n") {
                "${it.first}: ${it.second.selectedItem} - ${it.third.text}"
            }

            val values = ContentValues().apply {
                put("patient_id", patientId)
                put("exam_date", dateNow())
                put(
                    "consciousness",
                    "${consciousnessField.selectedItem}; GCS: $gcsValue"
                )
                put("cranial_nerves", cranialText)

                put(
                    "right_upper_motor",
                    motorText("Upper", "Right")
                )
                put(
                    "left_upper_motor",
                    motorText("Upper", "Left")
                )
                put(
                    "right_lower_motor",
                    motorText("Lower", "Right")
                )
                put(
                    "left_lower_motor",
                    motorText("Lower", "Left")
                )

                put(
                    "right_sensation",
                    "${rightSensory.selectedItem}: ${rightSensoryNote.text}"
                )
                put(
                    "left_sensation",
                    "${leftSensory.selectedItem}: ${leftSensoryNote.text}"
                )

                put(
                    "right_reflexes",
                    "${rightReflexes.selectedItem}"
                )
                put(
                    "left_reflexes",
                    "${leftReflexes.selectedItem}"
                )
                put(
                    "right_babinski",
                    rightBabinski.selectedItem.toString()
                )
                put(
                    "left_babinski",
                    leftBabinski.selectedItem.toString()
                )

                put(
                    "right_coordination",
                    rightCoordination.selectedItem.toString()
                )
                put(
                    "left_coordination",
                    leftCoordination.selectedItem.toString()
                )
                put("gait", gait.selectedItem.toString())
                put("clinical_impression", impression.text.toString())
                put("examiner_name", examiner.selectedItem.toString())
                put(
                    "notes",
                    "${reflexNotes.text}\n${notes.text}"
                )
            }

            val result = db.saveNeurologicalExam(values)

            if (result != -1L) {
                AlertDialog.Builder(this)
                    .setMessage("تم حفظ الفحص العصبي بنجاح.")
                    .setPositiveButton("موافق") { _, _ ->
                        patientDetails(patientId)
                    }
                    .show()
            } else {
                AlertDialog.Builder(this)
                    .setMessage("تعذر حفظ الفحص العصبي.")
                    .setPositiveButton("موافق", null)
                    .show()
            }
        }

        backButton()
    }

    // ---------------- PREVIOUS EXAMS ----------------

    private fun previousExams(patientId: Int) {
        screen("الفحوصات العصبية السابقة")

        val cursor = db.readableDatabase.rawQuery(
            """
            SELECT exam_date, consciousness, clinical_impression,
                   examiner_name, notes
            FROM neurological_exams
            WHERE patient_id = ?
            ORDER BY id DESC
            """.trimIndent(),
            arrayOf(patientId.toString())
        )

        if (cursor.count == 0) {
            root.addView(text("لا توجد فحوصات سابقة لهذا المريض."))
        }

        while (cursor.moveToNext()) {
            heading("تاريخ الفحص: ${cursor.getString(0) ?: ""}")
            root.addView(
                text("الوعي: ${cursor.getString(1) ?: ""}")
            )
            root.addView(
                text("الانطباع السريري: ${cursor.getString(2) ?: ""}")
            )
            root.addView(
                text("الطبيب: ${cursor.getString(3) ?: ""}")
            )
            root.addView(
                text("ملاحظات: ${cursor.getString(4) ?: ""}")
            )
            root.addView(View(this).apply {
                setBackgroundColor(lightBlue)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
                )
            })
        }

        cursor.close()
        backButton()
    }

    // ---------------- DOCTORS ----------------

    private fun doctors() {
        screen("الأطباء والكادر الطبي")

        button("إضافة طبيب") {
            addDoctorDialog { doctors() }
        }

        heading("قائمة الأطباء")

        val list = db.getDoctors()
        if (list.isEmpty()) {
            root.addView(text("لا يوجد أطباء مسجلون."))
        } else {
            list.forEachIndexed { index, name ->
                root.addView(text("${index + 1}. $name"))
            }
        }

        backButton()
    }

    private fun addDoctorDialog(afterSave: () -> Unit) {
        val field = EditText(this)
        field.hint = "اسم الطبيب"

        AlertDialog.Builder(this)
            .setTitle("إضافة طبيب")
            .setView(field)
            .setPositiveButton("حفظ") { _, _ ->
                val name = field.text.toString().trim()
                if (name.isNotEmpty()) {
                    val result = db.addDoctor(name)
                    if (result != -1L) {
                        Toast.makeText(
                            this,
                            "تمت إضافة الطبيب",
                            Toast.LENGTH_SHORT
                        ).show()
                        afterSave()
                    } else {
                        Toast.makeText(
                            this,
                            "تعذرت إضافة الطبيب",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    // ---------------- STATISTICS ----------------

    private fun statistics() {
        screen("التقارير والإحصائيات")

        val count = db.getPatientCount()
        heading("إحصائيات المرضى")
        root.addView(text("إجمالي عدد المرضى المسجلين: $count"))

        backButton()
    }

    override fun onBackPressed() {
        home()
    }
}
