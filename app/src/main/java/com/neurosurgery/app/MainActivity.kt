
package com.neurosurgery.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.content.ContentValues
import android.graphics.Color
import android.text.InputType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : Activity() {

    private lateinit var database: DatabaseHelper
    private lateinit var root: LinearLayout
    private var selectedPatientId: String = ""
    private var selectedPatientName: String = ""

    private val blue = Color.rgb(25, 91, 160)
    private val darkBlue = Color.rgb(15, 55, 105)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = DatabaseHelper(this)

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(245, 248, 252))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        setContentView(root)
        showPatientsScreen()
    }

    private fun addHeader(title: String) {
        val header = TextView(this).apply {
            text = title
            textSize = 21f
            setTextColor(Color.WHITE)
            setBackgroundColor(blue)
            gravity = Gravity.CENTER
            setPadding(12, 20, 12, 20)
        }
        root.addView(
            header,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun makeButton(
        text: String,
        onClick: () -> Unit
    ): Button {
        return Button(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.WHITE)
            setBackgroundColor(blue)
            setOnClickListener { onClick() }
        }
    }

    private fun makeText(
        text: String,
        size: Float = 16f,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(darkBlue)
            if (bold) {
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            setPadding(8, 12, 8, 8)
        }
    }

    private fun makeEditText(
        hint: String,
        multiline: Boolean = false
    ): EditText {
        return EditText(this).apply {
            this.hint = hint
            textSize = 15f
            setTextColor(Color.BLACK)
            setPadding(12, 10, 12, 10)
            setBackgroundColor(Color.WHITE)
            if (multiline) {
                minLines = 2
                gravity = Gravity.TOP or Gravity.RIGHT
                inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE
            } else {
                inputType = InputType.TYPE_CLASS_TEXT
            }
        }
    }

    private fun makeSpinner(options: List<String>): Spinner {
        val spinner = Spinner(this)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options
        )
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        spinner.adapter = adapter
        return spinner
    }

    private fun addSpace(container: LinearLayout, height: Int = 8) {
        val space = View(this)
        container.addView(
            space,
            LinearLayout.LayoutParams(
                1,
                height
            )
        )
    }

    private fun showPatientsScreen() {
        root.removeAllViews()
        addHeader("الجراحة العصبية | Neurosurgery")

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
        }

        val count = database.getPatientCount()
        content.addView(
            makeText("إدارة المرضى | Patient Management", 18f, true)
        )
        content.addView(
            makeText("عدد المرضى: $count", 15f)
        )

        content.addView(
            makeButton("＋ إضافة مريض جديد | Add Patient") {
                showAddPatientDialog()
            }
        )

        addSpace(content, 12)

        val search = makeEditText("ابحث باسم المريض أو رقم الملف")
        content.addView(search)

        val listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(listContainer)

        fun loadPatients(query: String = "") {
            listContainer.removeAllViews()

            val cursor = database.readableDatabase.rawQuery(
                """
                SELECT id, full_name, age, gender
                FROM patients
                WHERE full_name LIKE ? OR id LIKE ?
                ORDER BY full_name
                """.trimIndent(),
                arrayOf("%$query%", "%$query%")
            )

            cursor.use {
                while (it.moveToNext()) {
                    val id = it.getString(0)
                    val name = it.getString(1)
                    val age = it.getString(2) ?: ""
                    val gender = it.getString(3) ?: ""

                    val patientButton = Button(this).apply {
                        text = "$name\nرقم الملف: $id   العمر: $age   الجنس: $gender"
                        textSize = 14f
                        setTextColor(darkBlue)
                        setBackgroundColor(Color.WHITE)
                        setPadding(8, 12, 8, 12)
                        setOnClickListener {
                            selectedPatientId = id
                            selectedPatientName = name
                            showPatientProfile()
                        }
                    }

                    listContainer.addView(
                        patientButton,
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            bottomMargin = 8
                        }
                    )
                }
            }

            if (listContainer.childCount == 0) {
                listContainer.addView(
                    makeText("لا توجد نتائج | No patients found")
                )
            }
        }

        search.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    loadPatients(s?.toString() ?: "")
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {}
            }
        )

        loadPatients()

        val scroll = ScrollView(this)
        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
    }

    private fun showAddPatientDialog() {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 10, 20, 10)
        }

        val name = makeEditText("اسم المريض الكامل | Full name")
        val age = makeEditText("العمر | Age")
        age.inputType = InputType.TYPE_CLASS_NUMBER

        val gender = makeSpinner(
            listOf(
                "غير محدد | Not specified",
                "ذكر | Male",
                "أنثى | Female"
            )
        )

        form.addView(makeText("اسم المريض"))
        form.addView(name)
        form.addView(makeText("العمر"))
        form.addView(age)
        form.addView(makeText("الجنس"))
        form.addView(gender)

        AlertDialog.Builder(this)
            .setTitle("إضافة مريض جديد")
            .setView(form)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حفظ") { _, _ ->
                val patientName = name.text.toString().trim()
                val patientAge = age.text.toString().trim()
                val patientGender = gender.selectedItem.toString()

                if (patientName.isEmpty()) {
                    Toast.makeText(
                        this,
                        "يرجى إدخال اسم المريض",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }

                val id = UUID.randomUUID().toString()

                val saved = database.insertPatient(
                    id,
                    patientName,
                    patientAge,
                    patientGender
                )

                if (saved) {
                    Toast.makeText(
                        this,
                        "تم حفظ المريض",
                        Toast.LENGTH_SHORT
                    ).show()
                    showPatientsScreen()
                } else {
                    Toast.makeText(
                        this,
                        "تعذر حفظ المريض",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .show()
    }

    private fun showPatientProfile() {
        root.removeAllViews()
        addHeader("ملف المريض | Patient Profile")

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        content.addView(
            makeText(selectedPatientName, 21f, true)
        )
        content.addView(
            makeText("رقم الملف: $selectedPatientId", 14f)
        )

        content.addView(
            makeButton("الفحص العصبي | Neurological Examination") {
                showNeurologicalExam()
            }
        )

        addSpace(content, 12)

        content.addView(
            makeButton("العودة إلى قائمة المرضى | Back") {
                showPatientsScreen()
            }
        )

        val scroll = ScrollView(this)
        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
    }

    private fun showNeurologicalExam() {
        root.removeAllViews()
        addHeader("الفحص العصبي | Neurological Examination")

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 10, 14, 20)
        }

        content.addView(
            makeText("المريض: $selectedPatientName", 17f, true)
        )

        val consciousness = addSpinnerSection(
            content,
            "مستوى الوعي | Level of Consciousness",
            listOf(
                "غير مفحوص | Not tested",
                "يقظ | Alert",
                "نعسان | Drowsy",
                "مشوش | Confused",
                "ذهولي | Stuporous",
                "فاقد الوعي | Comatose"
            )
        )

        val gcs = addSpinnerSection(
            content,
            "مقياس غلاسكو | Glasgow Coma Scale",
            (3..15).map { it.toString() }
        )

        val cranial = addEditSection(
            content,
            "الأعصاب القحفية I–XII | Cranial Nerves",
            "دوّن نتيجة كل عصب قحفي والجهة المصابة"
        )

        content.addView(
            makeText("القوة العضلية | Motor Power", 19f, true)
        )

        val rightUpper = addSpinnerSection(
            content,
            "الطرف العلوي الأيمن | Right Upper Limb",
            motorOptions()
        )

        val leftUpper = addSpinnerSection(
            content,
            "الطرف العلوي الأيسر | Left Upper Limb",
            motorOptions()
        )

        val rightLower = addSpinnerSection(
            content,
            "الطرف السفلي الأيمن | Right Lower Limb",
            motorOptions()
        )

        val leftLower = addSpinnerSection(
            content,
            "الطرف السفلي الأيسر | Left Lower Limb",
            motorOptions()
        )

        val rightMotorNotes = addEditSection(
            content,
            "ملاحظات القوة - اليمين | Right Motor Notes",
            "المجموعات العضلية والعضلات التي تم فحصها"
        )

        val leftMotorNotes = addEditSection(
            content,
            "ملاحظات القوة - اليسار | Left Motor Notes",
            "المجموعات العضلية والعضلات التي تم فحصها"
        )

        content.addView(
            makeText("فحص الإحساس | Sensory Examination", 19f, true)
        )

        val sensoryOptions = listOf(
            "غير مفحوص | Not tested",
            "طبيعي | Normal",
            "منخفض | Reduced",
            "غائب | Absent",
            "زائد | Increased",
            "متغير | Altered"
        )

        val rightSensation = addSpinnerSection(
            content,
            "الإحساس الأيمن | Right Sensation",
            sensoryOptions
        )

        val leftSensation = addSpinnerSection(
            content,
            "الإحساس الأيسر | Left Sensation",
            sensoryOptions
        )

        val rightSensoryNotes = addEditSection(
            content,
            "ملاحظات الإحساس - اليمين | Right Sensory Notes",
            "الموقع ونوع الإحساس"
        )

        val leftSensoryNotes = addEditSection(
            content,
            "ملاحظات الإحساس - اليسار | Left Sensory Notes",
            "الموقع ونوع الإحساس"
        )

        content.addView(
            makeText("المنعكسات | Reflexes", 19f, true)
        )

        val reflexOptions = listOf(
            "غير مفحوص | Not tested",
            "طبيعي | Normal",
            "مفرط | Brisk",
            "منخفض | Reduced",
            "غائب | Absent"
        )

        val rightReflexes = addSpinnerSection(
            content,
            "المنعكسات اليمنى | Right Reflexes",
            reflexOptions
        )

        val leftReflexes = addSpinnerSection(
            content,
            "المنعكسات اليسرى | Left Reflexes",
            reflexOptions
        )

        val babinskiOptions = listOf(
            "غير مفحوص | Not tested",
            "سلبي | Negative",
            "إيجابي | Positive",
            "غير واضح | Equivocal"
        )

        val rightBabinski = addSpinnerSection(
            content,
            "بابنسكي الأيمن | Right Babinski",
            babinskiOptions
        )

        val leftBabinski = addSpinnerSection(
            content,
            "بابنسكي الأيسر | Left Babinski",
            babinskiOptions
        )

        val rightReflexNotes = addEditSection(
            content,
            "ملاحظات المنعكسات - اليمين | Right Reflex Notes",
            "مثلاً: Biceps, Triceps, Knee, Ankle"
        )

        val leftReflexNotes = addEditSection(
            content,
            "ملاحظات المنعكسات - اليسار | Left Reflex Notes",
            "مثلاً: Biceps, Triceps, Knee, Ankle"
        )

        content.addView(
            makeText("التناسق | Coordination", 19f, true)
        )

        val coordinationOptions = listOf(
            "غير مفحوص | Not tested",
            "طبيعي | Normal",
            "غير طبيعي | Abnormal",
            "ترنح | Ataxia",
            "اختلال التناسق | Dysmetria"
        )

        val rightCoordination = addSpinnerSection(
            content,
            "التناسق الأيمن | Right Coordination",
            coordinationOptions
        )

        val leftCoordination = addSpinnerSection(
            content,
            "التناسق الأيسر | Left Coordination",
            coordinationOptions
        )

        val gait = addSpinnerSection(
            content,
            "المشي والتوازن | Gait & Balance",
            listOf(
                "غير مفحوص | Not tested",
                "طبيعي | Normal",
                "غير مستقر | Unsteady",
                "ترنح | Ataxic",
                "غير قادر على المشي | Unable to walk"
            )
        )

        val gaitNotes = addEditSection(
            content,
            "ملاحظات المشي والتوازن | Gait Notes",
            "دوّن وصف المشية والتوازن"
        )

        val impression = addEditSection(
            content,
            "الانطباع السريري | Clinical Impression",
            "ملخص نتائج الفحص"
        )

        val examiner = addEditSection(
            content,
            "اسم الفاحص | Examiner Name",
            "اسم الطبيب أو الفاحص"
        )

        val generalNotes = addEditSection(
            content,
            "ملاحظات عامة | General Notes",
            "أي تفاصيل إضافية"
        )

        addSpace(content, 16)

        content.addView(
            makeButton("حفظ الفحص العصبي | Save Examination") {

                if (selectedPatientId.isBlank()) {
                    Toast.makeText(
                        this,
                        "لم يتم تحديد مريض",
                        Toast.LENGTH_LONG
                    ).show()
                    return@makeButton
                }

                val values = ContentValues().apply {
                    put("patient_id", selectedPatientId)
                    put(
                        "exam_date",
                        SimpleDateFormat(
                            "yyyy-MM-dd HH:mm",
                            Locale.getDefault()
                        ).format(Date())
                    )

                    put("consciousness", consciousness.selectedItem.toString())
                    put("cranial_nerves", cranial.text.toString())

                    put("right_upper_motor", rightUpper.selectedItem.toString() +
                        "\n" + rightMotorNotes.text.toString())
                    put("left_upper_motor", leftUpper.selectedItem.toString() +
                        "\n" + leftMotorNotes.text.toString())

                    put("right_lower_motor", rightLower.selectedItem.toString())
                    put("left_lower_motor", leftLower.selectedItem.toString())

                    put("right_sensation", rightSensation.selectedItem.toString() +
                        "\n" + rightSensoryNotes.text.toString())
                    put("left_sensation", leftSensation.selectedItem.toString() +
                        "\n" + leftSensoryNotes.text.toString())

                    put("right_reflexes", rightReflexes.selectedItem.toString() +
                        "\n" + rightReflexNotes.text.toString())
                    put("left_reflexes", leftReflexes.selectedItem.toString() +
                        "\n" + leftReflexNotes.text.toString())

                    put("right_babinski", rightBabinski.selectedItem.toString())
                    put("left_babinski", leftBabinski.selectedItem.toString())

                    put("right_coordination", rightCoordination.selectedItem.toString())
                    put("left_coordination", leftCoordination.selectedItem.toString())

                    put("gait", gait.selectedItem.toString() +
                        "\n" + gaitNotes.text.toString())

                    put(
                        "clinical_impression",
                        "GCS: ${gcs.selectedItem}\n" +
                            impression.text.toString()
                    )

                    put("examiner_name", examiner.text.toString())
                    put("notes", generalNotes.text.toString())
                }

                try {
                    val result = database.writableDatabase.insert(
                        DatabaseHelper.TABLE_EXAMS,
                        null,
                        values
                    )

                    if (result != -1L) {
                        Toast.makeText(
                            this,
                            "تم حفظ الفحص بنجاح",
                            Toast.LENGTH_LONG
                        ).show()
                        showPatientProfile()
                    } else {
                        Toast.makeText(
                            this,
                            "تعذر حفظ الفحص",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "خطأ أثناء الحفظ: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )

        content.addView(
            makeButton("عودة إلى ملف المريض | Back") {
                showPatientProfile()
            }
        )

        val scroll = ScrollView(this)
        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
    }

    private fun motorOptions(): List<String> {
        return listOf(
            "غير مفحوص | Not tested",
            "0/5 - لا توجد حركة | No movement",
            "1/5 - انقباض بسيط | Flicker",
            "2/5 - حركة دون الجاذبية | Movement without gravity",
            "3/5 - حركة ضد الجاذبية | Against gravity",
            "4/5 - حركة ضد مقاومة | Against resistance",
            "5/5 - قوة طبيعية | Normal power"
        )
    }

    private fun addSpinnerSection(
        container: LinearLayout,
        title: String,
        options: List<String>
    ): Spinner {
        container.addView(makeText(title, 15f, true))
        val spinner = makeSpinner(options)
        container.addView(
            spinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        addSpace(container, 5)
        return spinner
    }

    private fun addEditSection(
        container: LinearLayout,
        title: String,
        hint: String
    ): EditText {
        container.addView(makeText(title, 15f, true))
        val editText = makeEditText(hint, true)
        container.addView(
            editText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        addSpace(container, 5)
        return editText
    }
}
