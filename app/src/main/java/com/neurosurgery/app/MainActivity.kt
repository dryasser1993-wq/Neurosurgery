
package com.neurosurgery.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.content.ContentValues
import android.graphics.Color
import android.graphics.Typeface
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
    private val backgroundColor = Color.rgb(245, 248, 252)
    private val dark = Color.rgb(35, 45, 55)

    private data class MotorField(
        val group: String,
        val side: String,
        val muscle: String,
        val field: Spinner
    )

    private data class CranialField(
        val name: String,
        val status: Spinner,
        val note: EditText
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseHelper(this)
        home()
    }

    // ---------------- GENERAL UI ----------------

    private fun screen(title: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 12, 14, 24)
            setBackgroundColor(backgroundColor)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        if (title.isNotEmpty()) {
            root.addView(TextView(this).apply {
                text = title
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(12, 20, 12, 20)
                setBackgroundColor(blue)
            })
        }

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun heading(value: String) {
        root.addView(TextView(this).apply {
            text = value
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(blue)
            setPadding(4, 18, 4, 8)
        })
    }

    private fun text(value: String, size: Float = 15f) {
        root.addView(TextView(this).apply {
            text = value
            textSize = size
            setTextColor(dark)
            setPadding(5, 8, 5, 8)
        })
    }

    private fun button(value: String, action: () -> Unit) {
        root.addView(Button(this).apply {
            text = value
            textSize = 15f
            setTextColor(Color.WHITE)
            setBackgroundColor(blue)
            setOnClickListener { action() }
        })
    }

    private fun input(
        hint: String,
        type: Int = InputType.TYPE_CLASS_TEXT
    ): EditText {
        return EditText(this).apply {
            this.hint = hint
            inputType = type
            textSize = 16f
            setPadding(10, 8, 10, 8)
            root.addView(this)
        }
    }

    private fun spinner(options: List<String>): Spinner {
        return Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_item,
                options
            ).also {
                it.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )
            }
            root.addView(this)
        }
    }

    private fun dateNow(): String {
        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(Date())
    }

    private fun showMessage(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("موافق", null)
            .show()
    }

    private fun backButton() {
        button("العودة إلى القائمة الرئيسية") { home() }
    }

    // ---------------- HOME DASHBOARD ----------------

    private fun home() {
        screen("")

        root.addView(TextView(this).apply {
            text = "Neurosurgery\nالجراحة العصبية"
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(12, 30, 12, 30)
            setBackgroundColor(blue)
        })

        text("نظام إدارة قسم الجراحة العصبية", 16f)

        root.addView(TextView(this).apply {
            text = "👥  المرضى المسجلون: ${db.getPatientCount()}"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(blue)
            gravity = Gravity.CENTER
            setPadding(12, 22, 12, 22)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 22f
                setStroke(2, lightBlue)
            }
        })

        heading("الأقسام الرئيسية")

        fun makeCard(
            icon: String,
            title: String,
            description: String,
            action: () -> Unit
        ): LinearLayout {
            return LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(8, 18, 8, 18)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = 20f
                    setStroke(2, lightBlue)
                }

                addView(TextView(this@MainActivity).apply {
                    text = icon
                    textSize = 29f
                    gravity = Gravity.CENTER
                })

                addView(TextView(this@MainActivity).apply {
                    text = title
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(blue)
                    gravity = Gravity.CENTER
                    setPadding(3, 8, 3, 5)
                })

                addView(TextView(this@MainActivity).apply {
                    text = description
                    textSize = 11f
                    setTextColor(Color.DKGRAY)
                    gravity = Gravity.CENTER
                })

                setOnClickListener { action() }
            }
        }

        fun addCardRow(
            first: LinearLayout,
            second: LinearLayout
        ) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            row.addView(first, LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply { setMargins(5, 6, 5, 6) })

            row.addView(second, LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply { setMargins(5, 6, 5, 6) })

            root.addView(row)
        }

        addCardRow(
            makeCard("👥", "إدارة المرضى", "تسجيل وبحث الملفات") {
                patients()
            },
            makeCard("🧠", "الفحص العصبي", "الفحص السريري التفصيلي") {
                choosePatientForExam()
            }
        )

        addCardRow(
            makeCard("📅", "الزيارات والمتابعة", "متابعة المرضى") {
                placeholder("الزيارات والمتابعة")
            },
            makeCard("🩻", "الاستقصاءات والصور", "الفحوصات والتصوير") {
                placeholder("الاستقصاءات والصور")
            }
        )

        addCardRow(
            makeCard("🏥", "العمليات الجراحية", "إدارة العمليات") {
                placeholder("العمليات الجراحية")
            },
            makeCard("👨‍⚕️", "الأطباء والكادر", "إدارة الفريق الطبي") {
                doctors()
            }
        )

        addCardRow(
            makeCard("📊", "التقارير والإحصائيات", "إحصائيات القسم") {
                statistics()
            },
            makeCard("⚙️", "الإعدادات", "إعدادات التطبيق") {
                placeholder("الإعدادات والنسخ الاحتياطي")
            }
        )

        text("Neurosurgery | Beta 1.0")
    }

    private fun placeholder(title: String) {
        screen(title)
        text("هذا القسم مخصص للتطوير في إصدار قادم.")
        backButton()
    }

    // ---------------- PATIENT MANAGEMENT ----------------

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

            val cursor = db.readableDatabase.rawQuery(
                """
                SELECT id, full_name, file_number, diagnosis
                FROM patients
                WHERE full_name LIKE ? OR file_number LIKE ?
                ORDER BY id DESC
                """.trimIndent(),
                arrayOf("%$query%", "%$query%")
            )

            if (cursor.count == 0) {
                results.addView(TextView(this).apply {
                    text = "لا توجد نتائج."
                    setTextColor(dark)
                    textSize = 16f
                })
            }

            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val name = cursor.getString(1) ?: ""
                val file = cursor.getString(2) ?: ""
                val diagnosis = cursor.getString(3) ?: ""

                results.addView(Button(this).apply {
                    text = "$name\nرقم الملف: $file\nالتشخيص: $diagnosis"
                    textSize = 14f
                    setTextColor(blue)
                    setBackgroundColor(Color.WHITE)
                    setOnClickListener { patientDetails(id) }
                })
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
        val age = input("العمر", InputType.TYPE_CLASS_NUMBER)

        heading("الجنس")
        val gender = spinner(listOf("ذكر", "أنثى"))

        val phone = input("رقم الهاتف", InputType.TYPE_CLASS_PHONE)
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
                showMessage("تعذر حفظ بيانات المريض.")
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
                "رقم الملف", "العمر", "الجنس", "الهاتف",
                "العنوان", "التشخيص", "ملاحظات"
            )

            for (i in labels.indices) {
                text("${labels[i]}: ${cursor.getString(i + 1) ?: ""}")
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

    // ---------------- SELECT PATIENT ----------------

    private fun choosePatientForExam() {
        screen("اختيار المريض للفحص العصبي")

        val cursor = db.readableDatabase.rawQuery(
            "SELECT id, full_name, file_number FROM patients ORDER BY id DESC",
            null
        )

        if (cursor.count == 0) {
            text("لا يوجد مرضى مسجلون.")
        }

        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val name = cursor.getString(1) ?: ""
            val file = cursor.getString(2) ?: ""

            root.addView(Button(this).apply {
                text = "$name - $file"
                setOnClickListener { neurologicalExam(id) }
            })
        }

        cursor.close()
        backButton()
    }

    // ---------------- NEUROLOGICAL EXAM ----------------

    private fun neurologicalExam(patientId: Int) {
        screen("الفحص العصبي")

        text("تاريخ الفحص: ${dateNow()}")

        heading("مستوى الوعي")
        val consciousness = spinner(
            listOf(
                "Alert - يقظ",
                "Drowsy - نعسان",
                "Confused - مشوش",
                "Stuporous - سبات",
                "Comatose - غيبوبة"
            )
        )

        val gcs = input("GCS من 3 إلى 15", InputType.TYPE_CLASS_NUMBER)

        heading("الأعصاب القحفية Cranial Nerves")

        val cranialFields = mutableListOf<CranialField>()

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
            cranialFields.add(CranialField(nerve, status, note))
        }

        heading("القوة العضلية Motor Power")

        val motorFields = mutableListOf<MotorField>()

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

        fun addMotorGroup(
            group: String,
            side: String,
            muscles: List<String>
        ) {
            heading("$group - $side")
            for (muscle in muscles) {
                text(muscle)
                val grade = spinner(
                    listOf("Not tested", "0", "1", "2", "3", "4", "5")
                )
                motorFields.add(
                    MotorField(group, side, muscle, grade)
                )
            }
        }

        addMotorGroup("Upper", "Right", upperMuscles)
        addMotorGroup("Upper", "Left", upperMuscles)
        addMotorGroup("Lower", "Right", lowerMuscles)
        addMotorGroup("Lower", "Left", lowerMuscles)

        fun motorText(group: String, side: String): String {
            return motorFields
                .filter { it.group == group && it.side == side }
                .joinToString("\n") {
                    "${it.muscle}: ${it.field.selectedItem}"
                }
        }

        heading("الإحساس Sensory Examination")

        heading("Right Side")
        val rightSensation = spinner(
            listOf("Not tested", "Normal", "Reduced", "Absent", "Increased")
        )
        val rightSensationNote = input("ملاحظات الجانب الأيمن")

        heading("Left Side")
        val leftSensation = spinner(
            listOf("Not tested", "Normal", "Reduced", "Absent", "Increased")
        )
        val leftSensationNote = input("ملاحظات الجانب الأيسر")

        heading("المنعكسات Reflexes")

        heading("Right Reflexes")
        val rightReflexes = spinner(
            listOf("Not tested", "Normal", "Brisk", "Reduced", "Absent")
        )
        val rightBabinski = spinner(
            listOf("Not tested", "Negative", "Positive")
        )

        heading("Left Reflexes")
        val leftReflexes = spinner(
            listOf("Not tested", "Normal", "Brisk", "Reduced", "Absent")
        )
        val leftBabinski = spinner(
            listOf("Not tested", "Negative", "Positive")
        )

        val reflexNotes = input("ملاحظات المنعكسات")

        heading("التناسق الحركي Coordination")

        heading("Right")
        val rightCoordination = spinner(
            listOf("Not tested", "Normal", "Abnormal", "Ataxia")
        )

        heading("Left")
        val leftCoordination = spinner(
            listOf("Not tested", "Normal", "Abnormal", "Ataxia")
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

        heading("الانطباع السريري Clinical Impression")
        val impression = input("الانطباع السريري")

        heading("الطبيب الفاحص")
        val doctors = db.getDoctors().toMutableList()
        if (doctors.isEmpty()) doctors.add("Not specified")
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

            val cranialText = cranialFields.joinToString("\n") {
                "${it.name}: ${it.status.selectedItem} - ${it.note.text}"
            }

            val values = ContentValues().apply {
                put("patient_id", patientId)
                put("exam_date", dateNow())
                put(
                    "consciousness",
                    "${consciousness.selectedItem}; GCS: $gcsValue"
                )
                put("cranial_nerves", cranialText)

                put("right_upper_motor", motorText("Upper", "Right"))
                put("left_upper_motor", motorText("Upper", "Left"))
                put("right_lower_motor", motorText("Lower", "Right"))
                put("left_lower_motor", motorText("Lower", "Left"))

                put(
                    "right_sensation",
                    "${rightSensation.selectedItem}: ${rightSensationNote.text}"
                )
                put(
                    "left_sensation",
                    "${leftSensation.selectedItem}: ${leftSensationNote.text}"
                )

                put("right_reflexes", rightReflexes.selectedItem.toString())
                put("left_reflexes", leftReflexes.selectedItem.toString())
                put("right_babinski", rightBabinski.selectedItem.toString())
                put("left_babinski", leftBabinski.selectedItem.toString())

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
                put("notes", "${reflexNotes.text}\n${notes.text}")
            }

            try {
                val result = db.saveNeurologicalExam(values)

                if (result != -1L) {
                    AlertDialog.Builder(this)
                        .setMessage("تم حفظ الفحص العصبي بنجاح.")
                        .setPositiveButton("عرض الفحص") { _, _ ->
                            displayExam(result.toInt())
                        }
                        .setNegativeButton("ملف المريض") { _, _ ->
                            patientDetails(patientId)
                        }
                        .show()
                } else {
                    showMessage("تعذر حفظ الفحص العصبي.")
                }
            } catch (e: Exception) {
                showMessage("حدث خطأ أثناء الحفظ: ${e.message}")
            }
        }

        backButton()
    }

    // ---------------- COMPARISON TABLES ----------------

    private fun addComparisonTable(
        title: String,
        leftTitle: String,
        rightTitle: String,
        leftData: String,
        rightData: String
    ) {
        heading(title)

        val leftMap = parseMotorData(leftData)
        val rightMap = parseMotorData(rightData)

        val names = (rightMap.keys + leftMap.keys).distinct()

        val table = TableLayout(this).apply {
            isStretchAllColumns = true
            setBackgroundColor(Color.WHITE)
        }

        fun addRow(
            first: String,
            second: String,
            third: String,
            header: Boolean = false
        ) {
            val row = TableRow(this@MainActivity)

            listOf(first, second, third).forEach { value ->
                val cell = TextView(this@MainActivity).apply {
                    text = value
                    textSize = 13f
                    gravity = Gravity.CENTER
                    setPadding(5, 12, 5, 12)
                    setTextColor(if (header) Color.WHITE else dark)
                    if (header) {
                        setBackgroundColor(blue)
                        typeface = Typeface.DEFAULT_BOLD
                    } else {
                        setBackgroundColor(Color.WHITE)
                    }
                }

                row.addView(
                    cell,
                    TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )
            }

            table.addView(row)
        }

        addRow("العضلة / العنصر", rightTitle, leftTitle, true)

        if (names.isEmpty()) {
            addRow("لا توجد بيانات", "-", "-")
        } else {
            names.forEach { name ->
                addRow(
                    name,
                    rightMap[name] ?: "—",
                    leftMap[name] ?: "—"
                )
            }
        }

        root.addView(table)
    }

    private fun parseMotorData(data: String): Map<String, String> {
        val result = linkedMapOf<String, String>()

        data.lines().forEach { line ->
            val index = line.indexOf(':')
            if (index > 0) {
                val name = line.substring(0, index).trim()
                val value = line.substring(index + 1).trim()
                if (name.isNotEmpty()) {
                    result[name] = value
                }
            }
        }

        return result
    }

    private fun addSimpleComparison(
        title: String,
        right: String,
        left: String
    ) {
        heading(title)

        val table = TableLayout(this).apply {
            isStretchAllColumns = true
        }

        val row = TableRow(this)

        listOf("الجانب الأيمن", "الجانب الأيسر").forEach {
            val cell = TextView(this@MainActivity).apply {
                text = it
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setPadding(8, 12, 8, 12)
                setBackgroundColor(blue)
            }
            row.addView(
                cell,
                TableRow.LayoutParams(0, -2, 1f)
            )
        }
        table.addView(row)

        val valuesRow = TableRow(this)

        listOf(right, left).forEach {
            val cell = TextView(this@MainActivity).apply {
                text = if (it.isBlank()) "—" else it
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(dark)
                setPadding(8, 14, 8, 14)
                setBackgroundColor(Color.WHITE)
            }
            valuesRow.addView(
                cell,
                TableRow.LayoutParams(0, -2, 1f)
            )
        }

        table.addView(valuesRow)
        root.addView(table)
    }

    // ---------------- DISPLAY SAVED EXAM ----------------

    private fun displayExam(examId: Int) {
        screen("تفاصيل الفحص العصبي")

        val cursor = db.readableDatabase.rawQuery(
            """
            SELECT * FROM neurological_exams WHERE id = ?
            """.trimIndent(),
            arrayOf(examId.toString())
        )

        if (!cursor.moveToFirst()) {
            cursor.close()
            text("لم يتم العثور على الفحص.")
            backButton()
            return
        }

        fun get(column: String): String {
            val index = cursor.getColumnIndex(column)
            return if (index >= 0) cursor.getString(index) ?: "" else ""
        }

        text("تاريخ الفحص: ${get("exam_date")}")
        text("رقم المريض: ${get("patient_id")}")
        heading("مستوى الوعي")
        text(get("consciousness"))

        heading("الأعصاب القحفية Cranial Nerves")
        text(get("cranial_nerves"))

        addComparisonTable(
            "القوة العضلية - الأطراف العلوية",
            "يسار",
            "يمين",
            get("left_upper_motor"),
            get("right_upper_motor")
        )

        addComparisonTable(
            "القوة العضلية - الأطراف السفلية",
            "يسار",
            "يمين",
            get("left_lower_motor"),
            get("right_lower_motor")
        )

        addSimpleComparison(
            "الإحساس Sensation",
            get("right_sensation"),
            get("left_sensation")
        )

        addSimpleComparison(
            "المنعكسات Reflexes",
            get("right_reflexes"),
            get("left_reflexes")
        )

        addSimpleComparison(
            "Babinski",
            get("right_babinski"),
            get("left_babinski")
        )

        addSimpleComparison(
            "التناسق الحركي Coordination",
            get("right_coordination"),
            get("left_coordination")
        )

        heading("المشي والتوازن Gait")
        text(get("gait"))

        heading("الانطباع السريري")
        text(get("clinical_impression"))

        heading("الطبيب الفاحص")
        text(get("examiner_name"))

        heading("ملاحظات")
        text(get("notes"))

        cursor.close()

        backButton()
    }

    private fun previousExams(patientId: Int) {
        screen("الفحوصات العصبية السابقة")

        val cursor = db.readableDatabase.rawQuery(
            """
            SELECT id, exam_date, clinical_impression, examiner_name
            FROM neurological_exams
            WHERE patient_id = ?
            ORDER BY id DESC
            """.trimIndent(),
            arrayOf(patientId.toString())
        )

        if (cursor.count == 0) {
            text("لا توجد فحوصات سابقة لهذا المريض.")
        }

        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val date = cursor.getString(1) ?: ""
            val impression = cursor.getString(2) ?: ""
            val examiner = cursor.getString(3) ?: ""

            root.addView(Button(this).apply {
                text = "تاريخ الفحص: $date\nالانطباع: $impression\nالطبيب: $examiner"
                textSize = 14f
                setTextColor(blue)
                setBackgroundColor(Color.WHITE)
                setOnClickListener { displayExam(id) }
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
            text("لا يوجد أطباء مسجلون.")
        } else {
            list.forEachIndexed { index, name ->
                text("${index + 1}. $name")
            }
        }

        backButton()
    }

    private fun addDoctorDialog(afterSave: () -> Unit) {
        val field = EditText(this).apply {
            hint = "اسم الطبيب"
        }

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
                        showMessage("تعذرت إضافة الطبيب.")
                    }
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    // ---------------- STATISTICS ----------------

    private fun statistics() {
        screen("التقارير والإحصائيات")

        heading("إحصائيات المرضى")
        text("إجمالي المرضى المسجلين: ${db.getPatientCount()}")

        backButton()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        home()
    }
}
