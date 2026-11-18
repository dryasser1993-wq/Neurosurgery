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
    private val navy = Color.rgb(13, 39, 70)
    private val lightBlue = Color.rgb(232, 241, 250)
    private val white = Color.WHITE
    private val dark = Color.rgb(35, 45, 55)
    private val pageBackground = Color.rgb(245, 248, 252)

    private var selectedStatus = "الكل"
    private var pendingImagePatientId = -1
    private var pendingImagePatientName = ""
    private var pendingInvestigationId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseHelper(this)
        showDashboard()
    }

    private fun createRoot(): LinearLayout {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(pageBackground)
            setPadding(16, 14, 16, 14)
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
            setBackgroundColor(navy)
            setPadding(12, 20, 12, 20)
        }
        root.addView(header)
    }

    private fun addText(value: String, size: Float = 16f, bold: Boolean = false) {
        val text = TextView(this).apply {
            this.text = value
            textSize = size
            setTextColor(dark)
            setPadding(8, 10, 8, 10)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(text)
    }

    private fun addButton(title: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = title
            setTextColor(white)
            setBackgroundColor(blue)
            textSize = 15f
            isAllCaps = false
            setOnClickListener { action() }
        }
        val params = LinearLayout.LayoutParams(-1, -2)
        params.setMargins(0, 5, 0, 5)
        root.addView(button, params)
    }

    private fun addStatistic(label: String, value: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(white)
            setPadding(16, 12, 16, 12)
            elevation = 2f
        }
        val title = TextView(this).apply {
            text = label
            textSize = 14f
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

    private fun addDashboardTile(title: String, subtitle: String, icon: String, action: () -> Unit) {
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(white)
            setPadding(10, 15, 10, 15)
            elevation = 3f
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        val symbol = TextView(this).apply {
            text = icon
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(blue)
            typeface = Typeface.DEFAULT_BOLD
        }
        val heading = TextView(this).apply {
            text = title
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(navy)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(2, 7, 2, 2)
        }
        val detail = TextView(this).apply {
            text = subtitle
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
        }
        tile.addView(symbol)
        tile.addView(heading)
        tile.addView(detail)
        val params = LinearLayout.LayoutParams(0, -2, 1f)
        params.setMargins(5, 5, 5, 5)
        dashboardRow?.addView(tile, params)
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private var dashboardRow: LinearLayout? = null

    private fun showDashboard() {
        createRoot()

        val scroll = ScrollView(this)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 8)
        }
        scroll.addView(page)

        val oldRoot = root
        root = page

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(navy)
            setPadding(16, 22, 16, 20)
        }
        val logo = TextView(this).apply {
            text = "✚  🧠"
            textSize = 34f
            gravity = Gravity.CENTER
            setTextColor(white)
        }
        val title = TextView(this).apply {
            text = "الجراحة العصبية"
            textSize = 25f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
        }
        val english = TextView(this).apply {
            text = "NEUROSURGERY  •  BETA 2.0"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(205, 224, 245))
        }
        hero.addView(logo)
        hero.addView(title)
        hero.addView(english)
        root.addView(hero)

        val welcome = TextView(this).apply {
            text = "مرحباً بك\nنظام متكامل لمتابعة المرضى والرعاية الجراحية"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(navy)
            setBackgroundColor(lightBlue)
            setPadding(14, 16, 14, 16)
        }
        val welcomeParams = LinearLayout.LayoutParams(-1, -2)
        welcomeParams.setMargins(0, 12, 0, 10)
        root.addView(welcome, welcomeParams)

        addText("الأقسام الرئيسية", 19f, true)
        addDashboardGridRow(
            Quadruple("المرضى", "قائمة المرضى", "👥", { showPatientList() }),
            Quadruple("إضافة مريض", "تسجيل ملف جديد", "＋", { showAddPatient() })
        )
        addDashboardGridRow(
            Quadruple("الفحوصات العصبية", "الفحص والمتابعة", "🧠", { showPatientPicker("الفحوصات العصبية") { id, name -> showNeurologicalExam(id, name) } }),
            Quadruple("العمليات الجراحية", "السجل الجراحي", "✚", { showPatientPicker("العمليات الجراحية") { id, name -> showPatientSurgeries(id, name) } })
        )
        addDashboardGridRow(
            Quadruple("التحاليل والاستقصاءات", "النتائج والمرفقات", "⚕", { showPatientPicker("التحاليل والاستقصاءات") { id, name -> showPatientInvestigations(id, name) } }),
            Quadruple("الكادر الطبي", "إدارة الأطباء", "♙", { showDoctors() })
        )

        addText("الإحصائيات والتقارير", 19f, true)
        addStatistic("إجمالي المرضى", db.getPatientCount().toString())
        addStatistic("غير مقرر لهم عمل جراحي", db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_NOT_PLANNED).toString())
        addStatistic("قيد التحضير للعمل الجراحي", db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_PREPARATION).toString())
        addStatistic("جاهزون للعمل الجراحي", db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_READY).toString())
        addStatistic("العمليات المنجزة", db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_COMPLETED).toString())
        addStatistic("إجمالي العمليات المسجلة", db.getSurgeryCount().toString())

        addButton("عرض الإحصائيات التفصيلية والتقارير") { showDetailedStatistics() }
        addButton("البحث في العمليات الجراحية") { showSurgerySearch() }

        val settings = TextView(this).apply {
            text = "⚙   الإعدادات والضبط\nإعدادات التطبيق ومعلومات النسخة"
            textSize = 17f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            setBackgroundColor(blue)
            setPadding(14, 19, 14, 19)
            setOnClickListener { showSettings() }
        }
        val settingsParams = LinearLayout.LayoutParams(-1, -2)
        settingsParams.setMargins(0, 12, 0, 10)
        root.addView(settings, settingsParams)

        val footer = TextView(this).apply {
            text = "〰〰〰  معاً .. من أجل رعاية أفضل  〰〰〰"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(white)
            setBackgroundColor(navy)
            setPadding(10, 16, 10, 16)
        }
        root.addView(footer)

        oldRoot.removeAllViews()
        oldRoot.addView(scroll, LinearLayout.LayoutParams(-1, -1))
        root = oldRoot
        dashboardRow = null
        setContentView(root)
    }

    private fun addDashboardGridRow(
        first: Quadruple<String, String, String, () -> Unit>,
        second: Quadruple<String, String, String, () -> Unit>
    ) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        dashboardRow = row
        addDashboardTile(first.first, first.second, first.third, first.fourth)
        addDashboardTile(second.first, second.second, second.third, second.fourth)
        root.addView(row)
        dashboardRow = null
    }

    private fun showSettings() {
        createRoot()
        addHeader("الإعدادات والضبط")
        addText("Neurosurgery – Beta 2.0", 20f, true)
        addText("إعدادات التطبيق", 17f, true)
        addText("هذه الصفحة مخصصة لإعدادات التطبيق العامة. لا يتم تعديل بيانات المرضى أو قاعدة البيانات من هذه الصفحة.")
        addStatistic("إصدار الواجهة", "Beta 2.0")
        addStatistic("قاعدة البيانات", "محلية على الجهاز")
        addButton("معلومات التطبيق") {
            AlertDialog.Builder(this)
                .setTitle("حول التطبيق")
                .setMessage("الجراحة العصبية – Neurosurgery\nنسخة تجريبية Beta 2.0\nنظام لمتابعة المرضى والفحوصات والعمليات الجراحية.")
                .setPositiveButton("حسناً", null)
                .show()
        }
        addButton("العودة إلى الشاشة الرئيسية") { showDashboard() }
        setContentView(root)
    }

    private fun showPatientPicker(title: String, action: (Int, String) -> Unit) {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT id, full_name FROM patients ORDER BY full_name",
            null
        )
        val ids = mutableListOf<Int>()
        val names = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                ids.add(it.getInt(0))
                names.add(it.getString(1) ?: "")
            }
        }
        if (names.isEmpty()) {
            showMessage("لا يوجد مرضى مسجلون بعد")
            return
        }
        AlertDialog.Builder(this)
            .setTitle("اختر المريض – $title")
            .setItems(names.toTypedArray()) { _, which ->
                action(ids[which], names[which])
            }
            .setNegativeButton("إلغاء", null)
            .show()
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
        filter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)
        filter.setSelection(statuses.indexOf(selectedStatus).coerceAtLeast(0))
        root.addView(filter)
        addButton("إضافة مريض جديد") { showAddPatient() }
        patientListContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this)
        scroll.addView(patientListContainer)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        addButton("العودة للرئيسية") { showDashboard() }
        setContentView(root)
        filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
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
            """SELECT p.id, p.full_name, p.file_number, p.diagnosis,
               COALESCE(s.status, ?) AS surgical_status
               FROM patients p LEFT JOIN patient_surgical_status s ON p.id = s.patient_id
               ORDER BY p.id DESC""",
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
                    text = "$name\nرقم الملف: $fileNumber\nالتشخيص: $diagnosis\nالحالة الجراحية: $status"
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
        if (patientListContainer.childCount == 0) addTextTo(patientListContainer, "لا يوجد مرضى ضمن هذه الحالة")
    }

    private fun addTextTo(container: LinearLayout, value: String) {
        val text = TextView(this).apply {
            text = value
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(dark)
            setPadding(12, 25, 12, 25)
        }
        container.addView(text)
    }

    private fun createInput(hint: String, multiline: Boolean = false): EditText {
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
    val screen = root

    addHeader("تسجيل مريض جديد")

    // منطقة النموذج القابلة للتمرير
    val scroll = ScrollView(this)
    val form = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(4, 8, 4, 16)
    }
    scroll.addView(form)

    // نجعل حقول النموذج تضاف إلى form
    root = form

    fun addSection(title: String) {
        addText(title, 18f, true)
    }

    fun addSpinner(label: String, options: List<String>): Spinner {
        addText(label, 15f, true)
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            options
        )
        root.addView(spinner, LinearLayout.LayoutParams(-1, -2))
        return spinner
    }

    fun addMultiChoice(
        label: String,
        options: List<String>
    ): Pair<TextView, MutableList<String>> {
        addText(label, 15f, true)

        val selected = mutableListOf<String>()
        val summary = TextView(this).apply {
            text = "اضغط لاختيار عنصر أو أكثر"
            textSize = 15f
            setTextColor(blue)
            setBackgroundColor(white)
            setPadding(14, 16, 14, 16)
            isClickable = true
            setOnClickListener {
                val checked = BooleanArray(options.size) {
                    selected.contains(options[it])
                }

                AlertDialog.Builder(this@MainActivity)
                    .setTitle(label)
                    .setMultiChoiceItems(
                        options.toTypedArray(),
                        checked
                    ) { _, which, isChecked ->
                        val item = options[which]
                        if (isChecked) {
                            if (!selected.contains(item)) selected.add(item)
                        } else {
                            selected.remove(item)
                        }
                    }
                    .setPositiveButton("تم") { _, _ ->
                        text = if (selected.isEmpty()) {
                            "لم يتم اختيار شيء"
                        } else {
                            selected.joinToString("، ")
                        }
                    }
                    .setNegativeButton("إلغاء", null)
                    .show()
            }
        }

        root.addView(
            summary,
            LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, 4, 0, 8)
            }
        )

        return Pair(summary, selected)
    }

    addSection("المعلومات الشخصية")

    val name = createInput("الاسم الكامل للمريض")
    val fileNumber = createInput("رقم الملف (اتركه فارغًا للتوليد التلقائي)")
    val age = createInput("العمر")
    val gender = addSpinner(
        "الجنس",
        listOf("ذكر", "أنثى")
    )
    val phone = createInput("رقم الهاتف")
    val address = createInput("العنوان", true)

    addSection("معلومات المراجعة والدخول")

    val admissionType = addSpinner(
        "نوع المراجعة",
        listOf(
            "إسعاف",
            "عيادة خارجية",
            "استشارة داخل المشفى",
            "دخول مبرمج",
            "محول من طبيب خارجي"
        )
    )

    val referralOptions = listOf(
        "الإسعاف",
        "العيادة الخارجية",
        "طبيب خارجي",
        "قسم آخر داخل المشفى",
        "مشفى آخر",
        "أخرى"
    )
    val referral = addMultiChoice("مصدر التحويل", referralOptions)
    val referralOther = createInput("تفاصيل مصدر التحويل الآخر")
    val referringDoctor = createInput("اسم الطبيب المحوِّل")

    addSection("آلية الإصابة")

    val injuryOptions = listOf(
        "حادث سير",
        "سقوط من ارتفاع",
        "سقوط على الأرض",
        "إصابة نافذة",
        "إصابة رياضية",
        "اعتداء",
        "إصابة عمل",
        "إصابة ولادية",
        "لا توجد إصابة",
        "أخرى"
    )
    val injury = addMultiChoice("اختر آلية الإصابة (يمكن اختيار أكثر من خيار)", injuryOptions)
    val injuryOther = createInput("تفاصيل آلية الإصابة الأخرى")
    val injuryNotes = createInput("ملاحظات وتفاصيل آلية الإصابة", true)

    addSection("التشخيص الرئيسي")

    val diagnosisOptions = listOf(
        "ورم دماغي",
        "ورم نخاعي",
        "نزف داخل القحف",
        "نزف تحت العنكبوتية",
        "أم دم دماغية",
        "تشوه شرياني وريدي",
        "استسقاء دماغي",
        "رضوض دماغية",
        "كسر في الجمجمة",
        "انضغاط النخاع الشوكي",
        "انزلاق غضروفي",
        "تضيق القناة الشوكية",
        "كسر فقري",
        "إصابة أعصاب طرفية",
        "صرع",
        "اضطراب حركي",
        "تشوه خلقي",
        "أخرى"
    )
    val diagnosis = addMultiChoice(
        "اختر التشخيص الرئيسي (يمكن اختيار أكثر من خيار)",
        diagnosisOptions
    )
    val diagnosisOther = createInput("تشخيص رئيسي آخر")
    val diagnosisNotes = createInput("ملاحظات التشخيص الرئيسي", true)

    addSection("تشخيص العيادة الخارجية")

    val outpatient = addMultiChoice(
        "اختر تشخيص العيادة الخارجية",
        diagnosisOptions
    )
    val outpatientOther = createInput("تشخيص عيادة آخر")
    val outpatientNotes = createInput("ملاحظات تشخيص العيادة الخارجية", true)

    addSection("التشخيصات التفريقية")

    val differential = addMultiChoice(
        "اختر التشخيصات التفريقية",
        diagnosisOptions
    )
    val differentialOther = createInput("تشخيص تفريقي آخر")
    val differentialNotes = createInput("ملاحظات التشخيصات التفريقية", true)

    addSection("الكادر الطبي")

    val specialists = db.getSpecialists()
    val residents = db.getResidents()

    val examiningDoctor = addSpinner(
        "الطبيب الفاحص",
        listOf("غير محدد") + specialists
    )

    val specialist = addSpinner(
        "الطبيب المختص",
        listOf("غير محدد") + specialists
    )

    val resident = addSpinner(
        "الطبيب المقيم",
        listOf("غير محدد") + residents
    )

    addSection("الحالة الجراحية")

    val statuses = listOf(
        DatabaseHelper.SURGICAL_NOT_PLANNED,
        DatabaseHelper.SURGICAL_PREPARATION,
        DatabaseHelper.SURGICAL_READY,
        DatabaseHelper.SURGICAL_COMPLETED
    )
    val statusSpinner = addSpinner("الحالة الجراحية", statuses)

    addSection("ملاحظات عامة")
    val generalNotes = createInput("اكتب الملاحظات العامة هنا", true)

    // أزرار ثابتة أسفل الشاشة
    screen.addView(
        scroll,
        LinearLayout.LayoutParams(-1, 0, 1f)
    )

    val buttons = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(4, 6, 4, 4)
        setBackgroundColor(white)
    }

    fun fixedButton(title: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = title
            isAllCaps = false
            setTextColor(white)
            setBackgroundColor(blue)
            setOnClickListener { action() }
        }
        buttons.addView(
            button,
            LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, 3, 0, 3)
            }
        )
    }

    fun selectedText(items: List<String>, other: String): String {
        val result = items.toMutableList()
        if (other.isNotBlank()) result.add("أخرى: $other")
        return result.joinToString("، ")
    }

    fixedButton("حفظ ملف المريض") {
        val patientName = name.text.toString().trim()

        if (patientName.isEmpty()) {
            name.error = "يرجى إدخال اسم المريض"
            name.requestFocus()
            return@fixedButton
        }

        val notes = buildString {
            append("الطبيب الفاحص: ${examiningDoctor.selectedItem}\n")
            append("الطبيب المقيم: ${resident.selectedItem}\n")
            append("الطبيب المحوِّل: ${referringDoctor.text}\n")
            append("ملاحظات آلية الإصابة: ${injuryNotes.text}\n")
            append("ملاحظات التشخيص الرئيسي: ${diagnosisNotes.text}\n")
            append("ملاحظات تشخيص العيادة: ${outpatientNotes.text}\n")
            append("ملاحظات التشخيصات التفريقية: ${differentialNotes.text}\n")
            append("ملاحظات عامة: ${generalNotes.text}")
        }

        val id = db.insertPatient(
            patientName,
            fileNumber.text.toString().trim(),
            age.text.toString().trim(),
            gender.selectedItem.toString(),
            phone.text.toString().trim(),
            address.text.toString().trim(),
            selectedText(
                diagnosis.second,
                diagnosisOther.text.toString().trim()
            ),
            notes,
            if (specialist.selectedItem.toString() == "غير محدد") ""
            else specialist.selectedItem.toString(),
            admissionType.selectedItem.toString(),
            selectedText(
                referral.second,
                referralOther.text.toString().trim()
            ) + if (referringDoctor.text.toString().isNotBlank()) {
                " | الطبيب المحوِّل: ${referringDoctor.text}"
            } else "",
            selectedText(
                injury.second,
                injuryOther.text.toString().trim()
            ) + if (injuryNotes.text.toString().isNotBlank()) {
                " | ${injuryNotes.text}"
            } else "",
            selectedText(
                outpatient.second,
                outpatientOther.text.toString().trim()
            ),
            selectedText(
                differential.second,
                differentialOther.text.toString().trim()
            )
        )

        if (id == -1L) {
            showMessage("تعذر حفظ بيانات المريض")
            return@fixedButton
        }

        db.savePatientSurgicalStatus(
            id.toInt(),
            statusSpinner.selectedItem.toString()
        )

        showMessage("تم تسجيل المريض بنجاح")
        showPatientDetails(id.toInt(), patientName)
    }

    fixedButton("إلغاء والعودة") {
        showPatientList()
    }

    screen.addView(
        buttons,
        LinearLayout.LayoutParams(-1, -2)
    )

    root = screen
    setContentView(screen)
}


private fun showPatientDetails(patientId: Int, patientName: String) {
    createRoot()
    addHeader("ملف المريض")
    addText(patientName, 21f, true)
    addText("رقم المريض: $patientId")

    val cursor = db.readableDatabase.rawQuery(
        """SELECT file_number, age, gender, phone, address,
                  diagnosis, notes, specialist, admission_type,
                  referral_source, injury_mechanism,
                  outpatient_diagnosis, differential_diagnoses
           FROM patients WHERE id = ?""",
        arrayOf(patientId.toString())
    )

    cursor.use {
        if (it.moveToFirst()) {
            addText("المعلومات الشخصية", 18f, true)
            addText("رقم الملف: ${it.getString(0) ?: ""}")
            addText("العمر: ${it.getString(1) ?: ""}")
            addText("الجنس: ${it.getString(2) ?: ""}")
            addText("الهاتف: ${it.getString(3) ?: ""}")
            addText("العنوان: ${it.getString(4) ?: ""}")

            addText("معلومات المراجعة والدخول", 18f, true)
            addText("نوع المراجعة: ${it.getString(8) ?: ""}")
            addText("مصدر التحويل: ${it.getString(9) ?: ""}")
            addText("آلية الإصابة: ${it.getString(10) ?: ""}")

            addText("التشخيص الطبي", 18f, true)
            addText("التشخيص الرئيسي: ${it.getString(5) ?: ""}")
            addText("تشخيص العيادة الخارجية: ${it.getString(11) ?: ""}")
            addText("التشخيصات التفريقية: ${it.getString(12) ?: ""}")
            addText("الطبيب المختص: ${it.getString(7) ?: ""}")
            addText("ملاحظات إضافية: ${it.getString(6) ?: ""}")
        }
    }

    addText("الحالة الجراحية الحالية", 18f, true)
    addText(db.getPatientSurgicalStatus(patientId), 16f, true)
addButton("تعديل بيانات المريض") {
    showEditPatient(patientId)
}
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
    addButton("العودة لقائمة المرضى") {
        showPatientList()
    }

    setContentView(root)
}


private fun showEditPatient(patientId: Int) {
    createRoot()
    addHeader("تعديل بيانات المريض")

    var nameValue = ""
    var fileNumberValue = ""
    var ageValue = ""
    var genderValue = ""
    var phoneValue = ""
    var addressValue = ""
    var diagnosisValue = ""
    var notesValue = ""
    var specialistValue = ""
    var admissionTypeValue = ""
    var referralSourceValue = ""
    var injuryMechanismValue = ""
    var outpatientDiagnosisValue = ""
    var differentialDiagnosesValue = ""

    val cursor = db.readableDatabase.rawQuery(
        """SELECT full_name, file_number, age, gender, phone,
                  address, diagnosis, notes, specialist,
                  admission_type, referral_source, injury_mechanism,
                  outpatient_diagnosis, differential_diagnoses
           FROM patients WHERE id = ?""",
        arrayOf(patientId.toString())
    )

    val found = cursor.use {
        if (it.moveToFirst()) {
            nameValue = it.getString(0) ?: ""
            fileNumberValue = it.getString(1) ?: ""
            ageValue = it.getString(2) ?: ""
            genderValue = it.getString(3) ?: ""
            phoneValue = it.getString(4) ?: ""
            addressValue = it.getString(5) ?: ""
            diagnosisValue = it.getString(6) ?: ""
            notesValue = it.getString(7) ?: ""
            specialistValue = it.getString(8) ?: ""
            admissionTypeValue = it.getString(9) ?: ""
            referralSourceValue = it.getString(10) ?: ""
            injuryMechanismValue = it.getString(11) ?: ""
            outpatientDiagnosisValue = it.getString(12) ?: ""
            differentialDiagnosesValue = it.getString(13) ?: ""
            true
        } else {
            false
        }
    }

    if (!found) {
        showMessage("لم يتم العثور على المريض")
        showPatientList()
        return
    }

    addText("المعلومات الشخصية", 18f, true)
    val name = createInput("الاسم الكامل")
    name.setText(nameValue)

    val fileNumber = createInput("رقم الملف")
    fileNumber.setText(fileNumberValue)

    val age = createInput("العمر")
    age.setText(ageValue)

    val gender = createInput("الجنس")
    gender.setText(genderValue)

    val phone = createInput("رقم الهاتف")
    phone.setText(phoneValue)

    val address = createInput("العنوان")
    address.setText(addressValue)

    addText("معلومات المراجعة والدخول", 18f, true)
    val admissionType = createInput("نوع المراجعة")
    admissionType.setText(admissionTypeValue)

    val referralSource = createInput("مصدر التحويل")
    referralSource.setText(referralSourceValue)

    val injuryMechanism = createInput("آلية الإصابة", true)
    injuryMechanism.setText(injuryMechanismValue)

    addText("التشخيص الطبي", 18f, true)
    val diagnosis = createInput("التشخيص الرئيسي", true)
    diagnosis.setText(diagnosisValue)

    val outpatientDiagnosis = createInput("تشخيص العيادة الخارجية", true)
    outpatientDiagnosis.setText(outpatientDiagnosisValue)

    val differentialDiagnoses = createInput("التشخيصات التفريقية", true)
    differentialDiagnoses.setText(differentialDiagnosesValue)

    val specialist = createInput("الطبيب المختص")
    specialist.setText(specialistValue)

    val notes = createInput("ملاحظات إضافية", true)
    notes.setText(notesValue)

    addButton("حفظ التعديلات") {
        val patientName = name.text.toString().trim()

        if (patientName.isEmpty()) {
            name.error = "أدخل اسم المريض"
            return@addButton
        }

        val result = db.updatePatient(
            patientId,
            patientName,
            fileNumber.text.toString(),
            age.text.toString(),
            gender.text.toString(),
            phone.text.toString(),
            address.text.toString(),
            diagnosis.text.toString(),
            notes.text.toString(),
            specialist.text.toString(),
            admissionType.text.toString(),
            referralSource.text.toString(),
            injuryMechanism.text.toString(),
            outpatientDiagnosis.text.toString(),
            differentialDiagnoses.text.toString()
        )

        if (result > 0) {
            showMessage("تم تحديث بيانات المريض")
            showPatientDetails(patientId, patientName)
        } else {
            showMessage("لم يتم حفظ التعديلات")
        }
    }

    addButton("إلغاء والعودة") {
        showPatientDetails(patientId, nameValue)
    }

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
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)
        spinner.setSelection(statuses.indexOf(db.getPatientSurgicalStatus(patientId)).coerceAtLeast(0))
        root.addView(spinner)
        val surgeryType = createInput("نوع العمل الجراحي المخطط")
        val surgeryDate = createInput("تاريخ العمل الجراحي")
        val surgeon = createInput("الجراح")
        val notes = createInput("ملاحظات", true)
        addButton("حفظ الحالة") {
            val success = db.savePatientSurgicalStatus(
                patientId = patientId, status = spinner.selectedItem.toString(),
                plannedType = surgeryType.text.toString(), plannedDate = surgeryDate.text.toString(),
                surgeon = surgeon.text.toString(), notes = notes.text.toString()
            )
            if (success) {
                showMessage("تم تحديث الحالة الجراحية")
                showPatientDetails(patientId, patientName)
            } else showMessage("تعذر تحديث الحالة")
        }
        addButton("رجوع") { showPatientDetails(patientId, patientName) }
        setContentView(root)
    }

    private fun showMessage(message: String) {
        AlertDialog.Builder(this).setMessage(message).setPositiveButton("حسناً", null).show()
    }

  
private fun showNeurologicalExam(patientId: Int, patientName: String) {
    createRoot()
    addHeader("الفحص العصبي الشامل")
    addText("المريض: $patientName", 18f, true)

    val selections = mutableMapOf<String, MutableList<String>>()

    fun addMultiChoice(title: String, key: String, options: List<String>) {
        addText(title, 16f, true)

        val summary = TextView(this).apply {
            text = "لم يتم الاختيار"
            textSize = 14f
            setTextColor(blue)
            setPadding(12, 12, 12, 12)
        }
        root.addView(summary)

        addButton("اختيار $title") {
            val checked = BooleanArray(options.size) { index ->
                selections[key]?.contains(options[index]) == true
            }

            AlertDialog.Builder(this)
                .setTitle(title)
                .setMultiChoiceItems(
                    options.toTypedArray(),
                    checked
                ) { _, which, isChecked ->
                    val chosen = selections.getOrPut(key) {
                        mutableListOf()
                    }
                    if (isChecked) {
                        if (!chosen.contains(options[which])) {
                            chosen.add(options[which])
                        }
                    } else {
                        chosen.remove(options[which])
                    }
                }
                .setPositiveButton("اعتماد") { _, _ ->
                    summary.text = selections[key]
                        ?.takeIf { it.isNotEmpty() }
                        ?.joinToString("، ")
                        ?: "لم يتم الاختيار"
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }
    }

    fun addSpinner(title: String, options: List<String>): Spinner {
        addText(title, 16f, true)
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
        root.addView(spinner)
        return spinner
    }

    fun score(spinner: Spinner): Int? {
        return spinner.selectedItem?.toString()
            ?.substringBefore(" -")
            ?.trim()
            ?.toIntOrNull()
    }

    val examDate = createInput("تاريخ الفحص").apply {
        setText(currentDate())
    }

    // 1. التقييم الأولي
    addText("1. الحالة العامة والتقييم الأولي", 19f, true)

    addMultiChoice("الحالة العامة", "general", listOf(
        "بحالة جيدة", "متعب", "شاحب", "متعرق",
        "مضطرب", "نعسان", "فاقد الوعي",
        "هائج", "متعاون", "غير متعاون", "متألم"
    ))

    val bloodPressure = createInput("ضغط الدم الانقباضي / الانبساطي")
    val pulse = createInput("النبض / دقيقة")
    val respiratoryRate = createInput("معدل التنفس / دقيقة")
    val temperature = createInput("درجة الحرارة °C")
    val oxygen = createInput("تشبع الأكسجين SpO2 %")
    val glucose = createInput("سكر الدم")
    val pain = createInput("درجة الألم من 0 إلى 10")

    addMultiChoice("مجرى الهواء", "airway", listOf(
        "مفتوح", "مهدد", "يحتاج إلى تدخل", "غير مفحوص"
    ))
    addMultiChoice("الاستقرار التنفسي", "respiratory", listOf(
        "مستقر", "غير مستقر", "يحتاج أكسجين",
        "يحتاج دعمًا تنفسيًا"
    ))
    addMultiChoice("الاستقرار الدوراني", "circulation", listOf(
        "مستقر", "غير مستقر", "علامات صدمة",
        "يحتاج دعمًا دورانيًا"
    ))

    // 2. مقياس غلاسكو
    addText("2. مستوى الوعي ومقياس GCS", 19f, true)

    val eye = addSpinner("فتح العين E", listOf(
        "غير مفحوص", "4 - تلقائي", "3 - للصوت",
        "2 - للألم", "1 - لا استجابة"
    ))
    val verbal = addSpinner("الاستجابة الكلامية V", listOf(
        "غير مفحوص", "5 - متوجه", "4 - مشوش",
        "3 - كلمات غير مناسبة",
        "2 - أصوات غير مفهومة",
        "1 - لا استجابة"
    ))
    val motor = addSpinner("الاستجابة الحركية M", listOf(
        "غير مفحوص", "6 - ينفذ الأوامر",
        "5 - يحدد الألم", "4 - ينسحب من الألم",
        "3 - انثناء غير طبيعي",
        "2 - بسط غير طبيعي",
        "1 - لا استجابة"
    ))

    val gcsSummary = TextView(this).apply {
        text = "GCS: لم يكتمل التقييم"
        textSize = 17f
        setTextColor(blue)
        setPadding(12, 12, 12, 12)
    }
    root.addView(gcsSummary)

    val updateGcs = {
        val e = score(eye)
        val v = score(verbal)
        val m = score(motor)

        gcsSummary.text =
            if (e != null && v != null && m != null) {
                "GCS = ${e + v + m} / 15"
            } else {
                "GCS: لم يكتمل التقييم"
            }
    }

    val gcsListener =
        object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(
                parent: android.widget.AdapterView<*>?
            ) {}

            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateGcs()
            }
        }

    eye.onItemSelectedListener = gcsListener
    verbal.onItemSelectedListener = gcsListener
    motor.onItemSelectedListener = gcsListener

    // 3. الحدقتان
    addText("3. فحص الحدقتين", 19f, true)

    val pupilSizes = listOf(
        "غير مفحوص", "1 mm", "2 mm", "3 mm",
        "4 mm", "5 mm", "6 mm", "7 mm", "8 mm"
    )
    val rightPupil = addSpinner("حجم الحدقة اليمنى", pupilSizes)
    val leftPupil = addSpinner("حجم الحدقة اليسرى", pupilSizes)

    addMultiChoice("حالة الحدقة اليمنى", "right_pupil_state", listOf(
        "طبيعية", "متوسعة", "متقبضة", "غير منتظمة"
    ))
    addMultiChoice("حالة الحدقة اليسرى", "left_pupil_state", listOf(
        "طبيعية", "متوسعة", "متقبضة", "غير منتظمة"
    ))

    val rightReaction = addSpinner("تفاعل اليمنى مع الضوء", listOf(
        "غير مفحوص", "سريع", "بطيء", "غائب", "ثابت"
    ))
    val leftReaction = addSpinner("تفاعل اليسرى مع الضوء", listOf(
        "غير مفحوص", "سريع", "بطيء", "غائب", "ثابت"
    ))

    addMultiChoice("التفاعل الحدقي", "pupil_reflex", listOf(
        "التفاعل المباشر موجود",
        "التفاعل المباشر غائب",
        "التفاعل التوافقي موجود",
        "التفاعل التوافقي غائب",
        "تفاوت حجم الحدقتين",
        "غير مفحوص"
    ))

    // 4. الأعصاب القحفية
    addText("4. الأعصاب القحفية I - XII", 19f, true)
    val cranialNerves = createInput(
        "تفاصيل فحص كل عصب قحفي على حدة", true
    )

    addMultiChoice("نتائج فحص الأعصاب القحفية", "cranial_status", listOf(
        "طبيعي", "غير طبيعي", "ضعف حركي", "شلل",
        "فقد إحساس", "اضطراب الشم", "اضطراب الرؤية",
        "اضطراب حركة العين", "اضطراب حس الوجه",
        "ضعف الوجه", "اضطراب السمع", "اضطراب البلع",
        "اضطراب الصوت", "ضعف حركة اللسان", "غير مفحوص"
    ))

    // 5. الفحص الحركي
    addText("5. القوة العضلية والفحص الحركي", 19f, true)

    val rightUpper = createInput(
        "الطرف العلوي الأيمن: الكتف والمرفق والرسغ والأصابع MRC 0-5",
        true
    )
    val leftUpper = createInput(
        "الطرف العلوي الأيسر: الكتف والمرفق والرسغ والأصابع MRC 0-5",
        true
    )
    val rightLower = createInput(
        "الطرف السفلي الأيمن: الورك والركبة والكاحل والأصابع MRC 0-5",
        true
    )
    val leftLower = createInput(
        "الطرف السفلي الأيسر: الورك والركبة والكاحل والأصابع MRC 0-5",
        true
    )

    addMultiChoice("توتر العضلات", "tone", listOf(
        "طبيعي", "مرتفع", "منخفض",
        "تشنجي", "رخو", "غير مفحوص"
    ))
    addMultiChoice("الضمور والحركات اللاإرادية", "muscle_findings", listOf(
        "لا يوجد ضمور", "ضمور عضلي", "رعشة",
        "ارتعاشات حزمية", "حركات لا إرادية", "غير مفحوص"
    ))

    // 6. الفحص الحسي
    addText("6. الفحص الحسي", 19f, true)
    val rightSensation = createInput("الفحص الحسي للطرف الأيمن", true)
    val leftSensation = createInput("الفحص الحسي للطرف الأيسر", true)

    addMultiChoice("أنواع الحس المفحوصة", "sensation_types", listOf(
        "اللمس الخفيف", "الألم", "الحرارة",
        "الاهتزاز", "وضعية المفصل", "المستوى الحسي"
    ))
    addMultiChoice("نتائج الفحص الحسي", "sensation_results", listOf(
        "طبيعي", "نقص الحس", "فقد الحس",
        "زيادة الحس", "تنميل", "مستوى حسي واضح", "غير مفحوص"
    ))

    // 7. المنعكسات
    addText("7. المنعكسات العصبية", 19f, true)

    val rightReflexes = createInput(
        "المنعكسات اليمنى: Biceps, Triceps, Brachioradialis, Patellar, Achilles مع الدرجات 0-4+",
        true
    )
    val leftReflexes = createInput(
        "المنعكسات اليسرى: Biceps, Triceps, Brachioradialis, Patellar, Achilles مع الدرجات 0-4+",
        true
    )

    val rightBabinski = addSpinner("استجابة أخمص القدم اليمنى", listOf(
        "غير مفحوص", "سلبي", "بابنسكي إيجابي", "غير واضح"
    ))
    val leftBabinski = addSpinner("استجابة أخمص القدم اليسرى", listOf(
        "غير مفحوص", "سلبي", "بابنسكي إيجابي", "غير واضح"
    ))

   
    // 8. التناسق والتوازن والمشية
    addText("8. التناسق والتوازن والمشية", 19f, true)

    val rightCoordination = createInput("التناسق الحركي الأيمن", true)
    val leftCoordination = createInput("التناسق الحركي الأيسر", true)

    addMultiChoice("اختبارات التناسق", "coordination", listOf(
        "Finger-to-nose طبيعي",
        "Finger-to-nose غير طبيعي",
        "Heel-to-shin طبيعي",
        "Heel-to-shin غير طبيعي",
        "Rapid alternating movements طبيعي",
        "Dysdiadochokinesia",
        "Romberg سلبي",
        "Romberg إيجابي",
        "غير مفحوص"
    ))

    addMultiChoice("نوع المشية", "gait_types", listOf(
        "طبيعية", "ترنحية", "تشنجية",
        "متبخترة", "Steppage gait",
        "غير قادر على المشي", "غير مفحوص"
    ))
    val gait = createInput("تفاصيل المشية", true)

    // 9. علامات السحايا
    addText("9. علامات السحايا", 19f, true)

    addMultiChoice("الفحص السحائي", "meningeal", listOf(
        "تيبس الرقبة إيجابي",
        "تيبس الرقبة سلبي",
        "Kernig إيجابي",
        "Kernig سلبي",
        "Brudzinski إيجابي",
        "Brudzinski سلبي",
        "غير مفحوص"
    ))

    // 10. الفحوصات العصبية الإضافية
    addText("10. الفحوصات العصبية الإضافية", 19f, true)

    addMultiChoice("علامات ارتفاع الضغط داخل القحف", "raised_icp", listOf(
        "صداع", "قيء", "قيء قذفي", "اضطراب الوعي",
        "اضطراب الرؤية", "وذمة حليمة العصب البصري",
        "شلل العصب السادس", "ارتفاع ضغط الدم",
        "بطء القلب", "اضطراب التنفس", "لم يُفحص"
    ))

    addMultiChoice("علامات إصابة النخاع الشوكي", "spinal_cord", listOf(
        "ضعف الأطراف الأربعة", "ضعف الطرفين السفليين",
        "شلل رباعي", "شلل نصفي سفلي",
        "فرط المنعكسات", "نقص المنعكسات",
        "Clonus", "Spasticity", "Flaccidity",
        "مستوى حسي", "لم يُفحص"
    ))
    val spinalLevel = createInput("مستوى الإصابة الشوكية، مثال C5 أو T10")

    addMultiChoice("علامات إصابة الجذور العصبية", "radiculopathy", listOf(
        "ألم جذري", "تنميل", "ضعف عضلي",
        "نقص المنعكسات",
        "Straight Leg Raising إيجابي يمين",
        "Straight Leg Raising إيجابي يسار",
        "لم يُفحص"
    ))
    val rootLevel = createInput("مستوى الجذر والجهة، مثال L5 يمين")

    addMultiChoice("إصابات الأعصاب الطرفية", "peripheral_nerves", listOf(
        "Median", "Ulnar", "Radial", "Femoral",
        "Sciatic", "Common peroneal", "Tibial",
        "إصابة عدة أعصاب", "إصابة حركية",
        "إصابة حسية", "إصابة مختلطة", "لم يُفحص"
    ))
    val peripheralDetails = createInput(
        "تفاصيل إصابة الأعصاب الطرفية والجهة", true
    )

    addMultiChoice("علامات العجز العصبي البؤري", "focal_deficit", listOf(
        "Facial weakness", "Aphasia", "Dysarthria",
        "Hemiparesis", "Hemiplegia", "Monoparesis",
        "Monoplegia", "Ataxia", "Visual field defect",
        "Neglect", "Seizure activity", "لم يُفحص"
    ))

    addMultiChoice("وظيفة المثانة والأمعاء والعجان", "sphincter", listOf(
        "التحكم بالبول طبيعي", "احتباس بول", "سلس بول",
        "التحكم بالبراز طبيعي", "سلس براز",
        "إحساس Saddle طبيعي", "نقص إحساس Saddle",
        "فقد إحساس Saddle", "Anal tone طبيعي",
        "Anal tone منخفض", "Anal tone غائب", "لم يُفحص"
    ))

    addMultiChoice("تصنيف ASIA عند الحاجة", "asia", listOf(
        "ASIA A - Complete",
        "ASIA B - Sensory incomplete",
        "ASIA C - Motor incomplete",
        "ASIA D - Motor incomplete",
        "ASIA E - Normal",
        "لم يتم إجراء تقييم ASIA كامل"
    ))

    // 11. الانطباع السريري والتوثيق
    addText("11. الانطباع السريري والتوثيق", 19f, true)

    val impression = createInput("الانطباع السريري", true)
    val examiner = createInput("اسم الطبيب الفاحص")
    val notes = createInput("ملاحظات إضافية", true)

    // 12. حفظ الفحص
    addButton("حفظ الفحص العصبي") {

        val e = score(eye)
        val v = score(verbal)
        val m = score(motor)

        val total = if (e != null && v != null && m != null) {
            e + v + m
        } else {
            null
        }

        val structuredNotes = buildString {
            appendLine("=== التقييم الأولي ===")
            appendLine("الحالة العامة: ${selections["general"]?.joinToString("، ") ?: ""}")
            appendLine("ضغط الدم: ${bloodPressure.text}")
            appendLine("النبض: ${pulse.text}")
            appendLine("معدل التنفس: ${respiratoryRate.text}")
            appendLine("درجة الحرارة: ${temperature.text}")
            appendLine("SpO2: ${oxygen.text}")
            appendLine("سكر الدم: ${glucose.text}")
            appendLine("الألم: ${pain.text}")
            appendLine("مجرى الهواء: ${selections["airway"]?.joinToString("، ") ?: ""}")
            appendLine("التنفس: ${selections["respiratory"]?.joinToString("، ") ?: ""}")
            appendLine("الدوران: ${selections["circulation"]?.joinToString("، ") ?: ""}")

            appendLine("\n=== الحدقتان ===")
            appendLine("حالة اليمنى: ${selections["right_pupil_state"]?.joinToString("، ") ?: ""}")
            appendLine("حالة اليسرى: ${selections["left_pupil_state"]?.joinToString("، ") ?: ""}")
            appendLine("التفاعل الحدقي: ${selections["pupil_reflex"]?.joinToString("، ") ?: ""}")

            appendLine("\n=== الأعصاب القحفية ===")
            appendLine("النتائج: ${selections["cranial_status"]?.joinToString("، ") ?: ""}")

            appendLine("\n=== الفحص الحركي ===")
            appendLine("توتر العضلات: ${selections["tone"]?.joinToString("، ") ?: ""}")
            appendLine("الضمور والحركات: ${selections["muscle_findings"]?.joinToString("، ") ?: ""}")

            appendLine("\n=== الفحص الحسي ===")
            appendLine("أنواع الحس: ${selections["sensation_types"]?.joinToString("، ") ?: ""}")
            appendLine("النتائج: ${selections["sensation_results"]?.joinToString("، ") ?: ""}")

            appendLine("\n=== التناسق والمشية ===")
            appendLine("التناسق: ${selections["coordination"]?.joinToString("، ") ?: ""}")
            appendLine("نوع المشية: ${selections["gait_types"]?.joinToString("، ") ?: ""}")

            appendLine("\n=== علامات السحايا ===")
            appendLine(selections["meningeal"]?.joinToString("، ") ?: "")

            appendLine("\n=== الفحوصات الإضافية ===")
            appendLine("ارتفاع الضغط داخل القحف: ${selections["raised_icp"]?.joinToString("، ") ?: ""}")
            appendLine("إصابة النخاع: ${selections["spinal_cord"]?.joinToString("، ") ?: ""}")
            appendLine("مستوى الإصابة الشوكية: ${spinalLevel.text}")
            appendLine("إصابة الجذور: ${selections["radiculopathy"]?.joinToString("، ") ?: ""}")
            appendLine("مستوى الجذر: ${rootLevel.text}")
            appendLine("الأعصاب الطرفية: ${selections["peripheral_nerves"]?.joinToString("، ") ?: ""}")
            appendLine("تفاصيل الأعصاب الطرفية: ${peripheralDetails.text}")
            appendLine("العجز البؤري: ${selections["focal_deficit"]?.joinToString("، ") ?: ""}")
            appendLine("وظيفة المصرة والعجان: ${selections["sphincter"]?.joinToString("، ") ?: ""}")
            appendLine("ASIA: ${selections["asia"]?.joinToString("، ") ?: ""}")
            appendLine("\n=== الملاحظات ===")
            appendLine(notes.text)
        }

        val values = ContentValues().apply {
            put("patient_id", patientId)
            put("exam_date", examDate.text.toString())

            put(
                "consciousness",
                "GCS: ${total ?: "غير مكتمل"} / 15"
            )

            put("gcs_eye", e?.toString())
            put("gcs_verbal", v?.toString())
            put("gcs_motor", m?.toString())

            if (total != null) {
                put("gcs_total", total)
            } else {
                putNull("gcs_total")
            }

            put(
                "right_pupil_size",
                rightPupil.selectedItem?.toString() ?: ""
            )
            put(
                "left_pupil_size",
                leftPupil.selectedItem?.toString() ?: ""
            )
            put(
                "right_pupil_reaction",
                rightReaction.selectedItem?.toString() ?: ""
            )
            put(
                "left_pupil_reaction",
                leftReaction.selectedItem?.toString() ?: ""
            )

            put(
                "cranial_nerves",
                cranialNerves.text.toString() +
                    "\n" +
                    (selections["cranial_status"]?.joinToString("، ") ?: "")
            )

            put("right_upper_motor", rightUpper.text.toString())
            put("left_upper_motor", leftUpper.text.toString())
            put("right_lower_motor", rightLower.text.toString())
            put("left_lower_motor", leftLower.text.toString())

            put("right_sensation", rightSensation.text.toString())
            put("left_sensation", leftSensation.text.toString())

            put("right_reflexes", rightReflexes.text.toString())
            put("left_reflexes", leftReflexes.text.toString())

            put(
                "right_babinski",
                rightBabinski.selectedItem?.toString() ?: ""
            )
            put(
                "left_babinski",
                leftBabinski.selectedItem?.toString() ?: ""
            )

            put("right_coordination", rightCoordination.text.toString())
            put("left_coordination", leftCoordination.text.toString())

            put("gait", gait.text.toString())
            put("clinical_impression", impression.text.toString())
            put("examiner_name", examiner.text.toString())
            put("notes", structuredNotes)
        }

        if (db.saveNeurologicalExam(values) != -1L) {
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
 

    private fun createMotorInputs(): EditText = createInput(
        "القوة العضلية: الكتف، المرفق، الرسغ، الأصابع، الورك، الركبة، الكاحل، MRC 0-5", true
    )

    private val investigationTypes = listOf(
        "CBC", "ESR", "CRP", "RBS", "HbA1c", "Urea", "Creatinine",
        "Na / K", "PT / PTT / INR", "LFT", "RFT", "Blood Group", "Crossmatch", "Other"
    )

    private fun showAddInvestigation(patientId: Int, patientName: String) {
        createRoot()
        addHeader("إضافة تحليل أو استقصاء")
        addText("المريض: $patientName", 17f, true)
        val typeSpinner = Spinner(this)
        typeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, investigationTypes)
        root.addView(typeSpinner)
        val customName = createInput("اسم التحليل أو الاستقصاء")
        val requestDate = createInput("تاريخ طلب التحليل").apply { setText(currentDate()) }
        val resultDate = createInput("تاريخ صدور النتيجة")
        val result = createInput("النتيجة والقيم المخبرية", true)
        val notes = createInput("ملاحظات إضافية", true)
        addButton("حفظ التحليل") {
            val selectedType = typeSpinner.selectedItem.toString()
            val investigationName = if (selectedType == "Other" || customName.text.toString().trim().isNotEmpty()) {
                customName.text.toString().trim()
            } else selectedType
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
            if (db.saveInvestigation(values) != -1L) {
                showMessage("تم حفظ التحليل بنجاح")
                showPatientInvestigations(patientId, patientName)
            } else showMessage("تعذر حفظ التحليل")
        }
        addButton("إرفاق صورة نتيجة") { openInvestigationImagePicker(patientId, patientName) }
        addButton("عرض تحاليل المريض") { showPatientInvestigations(patientId, patientName) }
        addButton("رجوع") { showPatientDetails(patientId, patientName) }
        setContentView(root)
    }

    private fun currentDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun openInvestigationImagePicker(patientId: Int, patientName: String, investigationId: Int = -1) {
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2001 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            try {
                val savedPath = copyImageToInternalStorage(uri, pendingImagePatientId)
                val values = ContentValues().apply {
                    put("patient_id", pendingImagePatientId)
                    put("image_type", "نتيجة تحليل")
                    put("image_name", "صورة نتيجة")
                    put("image_path", savedPath)
                    put("image_date", currentDate())
                    put("description", "مرفق طبي")
                    if (pendingInvestigationId != -1) put("investigation_id", pendingInvestigationId)
                }
                if (db.savePatientImage(values) != -1L) {
                    showMessage("تم حفظ الصورة بنجاح")
                    showPatientInvestigations(pendingImagePatientId, pendingImagePatientName)
                } else showMessage("تعذر حفظ الصورة في قاعدة البيانات")
            } catch (e: Exception) {
                showMessage("حدث خطأ أثناء حفظ الصورة: ${e.message}")
            }
        }
    }

    private fun copyImageToInternalStorage(uri: Uri, patientId: Int): String {
        val directory = File(filesDir, "patient_images/$patientId")
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, "image_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "تعذر قراءة الصورة" }
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }

    private fun showPatientInvestigations(patientId: Int, patientName: String) {
        createRoot()
        addHeader("تحاليل واستقصاءات المريض")
        addText(patientName, 18f, true)
        addButton("إضافة تحليل جديد") { showAddInvestigation(patientId, patientName) }
        val cursor = db.readableDatabase.rawQuery(
            """SELECT id, investigation_type, investigation_name, request_date,
               result_date, result, notes FROM investigations
               WHERE patient_id = ? ORDER BY id DESC""",
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
                addText("$name ($type)\nتاريخ الطلب: $request\nتاريخ النتيجة: $date\nالنتيجة: $result\nملاحظات: $notes", 15f)
                addButton("إرفاق صورة لهذه النتيجة") {
                    openInvestigationImagePicker(patientId, patientName, investigationId)
                }
                addInvestigationImages(investigationId)
            }
        }
        addButton("رجوع إلى ملف المريض") { showPatientDetails(patientId, patientName) }
        setContentView(root)
    }

    private fun addInvestigationImages(investigationId: Int) {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT image_name, image_path, image_date FROM patient_images WHERE investigation_id = ? ORDER BY id DESC",
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
        if (!File(path).exists()) {
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
        } catch (_: Exception) {
            showMessage("تعذر فتح الصورة")
        }
    }

    private fun showAddSurgery(patientId: Int, patientName: String) {
        createRoot()
        addHeader("تسجيل عملية جراحية")
        addText("المريض: $patientName", 18f, true)
        val surgeryDate = createInput("تاريخ العملية").apply { setText(currentDate()) }
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
        val duration = createInput("مدة العملية بالدقائق").apply { inputType = InputType.TYPE_CLASS_NUMBER }
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
                duration.text.toString().trim().toIntOrNull()?.let { put("duration_minutes", it) }
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
            } else showMessage("تعذر حفظ العملية الجراحية")
        }
        addButton("رجوع إلى ملف المريض") { showPatientDetails(patientId, patientName) }
        setContentView(root)
    }

    private fun showPatientSurgeries(patientId: Int, patientName: String) {
        createRoot()
        addHeader("السجل الجراحي للمريض")
        addText(patientName, 18f, true)
        addButton("تسجيل عملية جديدة") { showAddSurgery(patientId, patientName) }
        val cursor = db.readableDatabase.rawQuery(
            """SELECT id, surgery_date, surgery_type, surgeon, preop_diagnosis,
               postoperative_diagnosis, outcome, complications, report
               FROM surgeries WHERE patient_id = ? ORDER BY surgery_date DESC, id DESC""",
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
                    text = "العملية رقم: $surgeryId\nالتاريخ: $date\nنوع العملية: $type\nالجراح: $surgeon\nالتشخيص قبل العملية: $preop\nالتشخيص بعد العملية: $postop\nالنتيجة: $outcome\nالمضاعفات: $complications"
                    textSize = 15f
                    setTextColor(dark)
                    setBackgroundColor(white)
                    setPadding(14, 14, 14, 14)
                    setOnClickListener { showSurgeryReport(surgeryId, patientId, patientName) }
                }
                val params = LinearLayout.LayoutParams(-1, -2)
                params.setMargins(0, 6, 0, 6)
                root.addView(card, params)
            }
        }
        if (count == 0) addText("لا توجد عمليات جراحية مسجلة لهذا المريض")
        addButton("رجوع إلى ملف المريض") { showPatientDetails(patientId, patientName) }
        setContentView(root)
    }

    private fun showSurgeryReport(surgeryId: Int, patientId: Int, patientName: String) {
        createRoot()
        addHeader("تفاصيل التقرير الجراحي")
        val cursor = db.readableDatabase.rawQuery("SELECT * FROM surgeries WHERE id = ?", arrayOf(surgeryId.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                fun value(column: String): String {
                    val index = it.getColumnIndex(column)
                    return if (index >= 0) it.getString(index) ?: "" else ""
                }
                val fields = listOf(
                    "surgery_date" to "تاريخ العملية", "surgery_type" to "نوع العملية",
                    "surgeon" to "الجراح", "assistants" to "المساعدون",
                    "anesthetist" to "طبيب التخدير", "anesthesia_type" to "نوع التخدير",
                    "preop_diagnosis" to "التشخيص قبل العملية",
                    "postoperative_diagnosis" to "التشخيص بعد العملية", "indication" to "الاستطباب",
                    "urgency" to "درجة الاستعجال", "patient_position" to "وضعية المريض",
                    "surgical_approach" to "المدخل الجراحي", "procedure_details" to "تفاصيل العملية",
                    "intraoperative_findings" to "الموجودات أثناء العملية", "implants" to "الزرعات",
                    "complications" to "المضاعفات أثناء العملية",
                    "postoperative_complications" to "المضاعفات بعد العملية",
                    "outcome" to "النتيجة", "patient_condition" to "حالة المريض",
                    "duration_minutes" to "المدة بالدقائق", "estimated_blood_loss" to "فقد الدم",
                    "blood_transfusion" to "نقل الدم", "followup_plan" to "خطة المتابعة",
                    "followup_date" to "تاريخ المتابعة", "report" to "التقرير الكامل", "notes" to "ملاحظات"
                )
                fields.forEach { (column, label) -> addText("$label: ${value(column)}") }
            }
        }
        addButton("رجوع للسجل الجراحي") { showPatientSurgeries(patientId, patientName) }
        setContentView(root)
    }

    
private fun showDoctors() {
    createRoot()
    addHeader("إدارة الكادر الطبي")

    addText("إضافة عضو جديد", 18f, true)

    val nameInput = createInput("الاسم الكامل")

    val roles = arrayOf(
        "استشاري / اختصاصي",
        "طبيب مقيم",
        "طبيب"
    )

    val roleSpinner = Spinner(this)
    val adapter = ArrayAdapter(
        this,
        android.R.layout.simple_spinner_item,
        roles
    )
    adapter.setDropDownViewResource(
        android.R.layout.simple_spinner_dropdown_item
    )
    roleSpinner.adapter = adapter
    root.addView(roleSpinner)

    val specialtyInput = createInput("التخصص الدقيق")
    val phoneInput = createInput("رقم الهاتف")
    val notesInput = createInput("ملاحظات", true)

    addButton("حفظ عضو الكادر") {
        val name = nameInput.text.toString().trim()

        if (name.isEmpty()) {
            nameInput.error = "أدخل الاسم الكامل"
            return@addButton
        }

        val role = when (roleSpinner.selectedItemPosition) {
            0 -> DatabaseHelper.STAFF_SPECIALIST
            1 -> DatabaseHelper.STAFF_RESIDENT
            else -> DatabaseHelper.STAFF_DOCTOR
        }

        val result = db.addStaff(
            name,
            role,
            specialtyInput.text.toString().trim(),
            phoneInput.text.toString().trim(),
            notesInput.text.toString().trim()
        )

        if (result != -1L) {
            showMessage("تمت إضافة عضو الكادر بنجاح")
            showDoctors()
        } else {
            showMessage("تعذرت إضافة عضو الكادر")
        }
    }

    addText("الاستشاريون والاختصاصيون", 18f, true)

    val specialists = db.getSpecialists()
    if (specialists.isEmpty()) {
        addText("لا يوجد اختصاصيون مسجلون")
    } else {
        specialists.forEachIndexed { index, person ->
            addText("${index + 1}. $person")
        }
    }

    addText("الأطباء المقيمون", 18f, true)

    val residents = db.getResidents()
    if (residents.isEmpty()) {
        addText("لا يوجد أطباء مقيمون مسجلون")
    } else {
        residents.forEachIndexed { index, person ->
            addText("${index + 1}. $person")
        }
    }

    addButton("العودة للرئيسية") {
        showDashboard()
    }

    setContentView(root)
}


    private fun showNeurologicalExams(patientId: Int, patientName: String) {
        createRoot()
        addHeader("الفحوصات العصبية السابقة")
        addText(patientName, 18f, true)
        val cursor = db.readableDatabase.rawQuery(
            "SELECT * FROM neurological_exams WHERE patient_id = ? ORDER BY exam_date DESC, id DESC",
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
                val details = """
                    تاريخ الفحص: ${value("exam_date")}
                    الوعي و GCS: ${value("consciousness")}
                    الأعصاب القحفية: ${value("cranial_nerves")}
                    القوة العضلية - الطرف العلوي الأيمن: ${value("right_upper_motor")}
                    القوة العضلية - الطرف العلوي الأيسر: ${value("left_upper_motor")}
                    القوة العضلية - الطرف السفلي الأيمن: ${value("right_lower_motor")}
                    القوة العضلية - الطرف السفلي الأيسر: ${value("left_lower_motor")}
                    الحس الأيمن: ${value("right_sensation")}
                    الحس الأيسر: ${value("left_sensation")}
                    المنعكسات اليمنى: ${value("right_reflexes")}
                    المنعكسات اليسرى: ${value("left_reflexes")}
                    Babinski يمين: ${value("right_babinski")}
                    Babinski يسار: ${value("left_babinski")}
                    التناسق الحركي الأيمن: ${value("right_coordination")}
                    التناسق الحركي الأيسر: ${value("left_coordination")}
                    المشية: ${value("gait")}
                    الانطباع السريري: ${value("clinical_impression")}
                    الفاحص: ${value("examiner_name")}
                    الملاحظات: ${value("notes")}
                """.trimIndent()
                val card = TextView(this).apply {
                    text = "فحص رقم: ${value("id")}\n\n$details"
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
        addButton("إجراء فحص عصبي جديد") { showNeurologicalExam(patientId, patientName) }
        addButton("العودة إلى ملف المريض") { showPatientDetails(patientId, patientName) }
        setContentView(root)
    }

    private fun showSurgerySearch() {
        createRoot()
        addHeader("البحث في العمليات الجراحية")
        val surgeonInput = createInput("اسم الجراح")
        val typeInput = createInput("نوع العملية")
        val fromDate = createInput("من تاريخ YYYY-MM-DD")
        val toDate = createInput("إلى تاريخ YYYY-MM-DD")
        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
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
            val where = if (conditions.isEmpty()) "" else "WHERE " + conditions.joinToString(" AND ")
            val cursor = db.readableDatabase.rawQuery(
                "SELECT id, patient_id, surgery_date, surgery_type, surgeon FROM surgeries $where ORDER BY surgery_date DESC",
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
                        text = "العملية رقم: $surgeryId\nالمريض رقم: $patientId\nالتاريخ: $date\nالنوع: $type\nالجراح: $surgeon"
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
                            nameCursor.use { c -> if (c.moveToFirst()) patientName = c.getString(0) ?: "" }
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

    private fun showDetailedStatistics() {
        createRoot()
        addHeader("الإحصائيات التفصيلية")
        val database = db.readableDatabase
        fun getCount(query: String): Int {
            val cursor = database.rawQuery(query, null)
            return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
        }
        addText("إجمالي المرضى: ${db.getPatientCount()}", 18f, true)
        addText("إجمالي العمليات: ${db.getSurgeryCount()}", 18f, true)
        addText("المرضى غير المقرر لهم عمل جراحي: ${db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_NOT_PLANNED)}")
        addText("المرضى قيد التحضير: ${db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_PREPARATION)}")
        addText("المرضى الجاهزون للعمل الجراحي: ${db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_READY)}")
        addText("المرضى الذين أُجريت لهم عمليات: ${db.getSurgicalStatusCount(DatabaseHelper.SURGICAL_COMPLETED)}")
        addText("عدد العمليات حسب النوع", 18f, true)
        val typeCursor = database.rawQuery(
            "SELECT surgery_type, COUNT(*) FROM surgeries GROUP BY surgery_type ORDER BY COUNT(*) DESC",
            null
        )
        var hasTypes = false
        typeCursor.use {
            while (it.moveToNext()) {
                hasTypes = true
                addText("${it.getString(0) ?: "غير محدد"}: ${it.getInt(1)}")
            }
        }
        if (!hasTypes) addText("لا توجد عمليات مسجلة")
        addText("عدد العمليات حسب الجراح", 18f, true)
        val surgeonCursor = database.rawQuery(
            "SELECT surgeon, COUNT(*) FROM surgeries GROUP BY surgeon ORDER BY COUNT(*) DESC",
            null
        )
        var hasSurgeons = false
        surgeonCursor.use {
            while (it.moveToNext()) {
                hasSurgeons = true
                addText("${it.getString(0) ?: "غير محدد"}: ${it.getInt(1)}")
            }
        }
        if (!hasSurgeons) addText("لا توجد بيانات جراحين")
        addText("الاختلاطات المسجلة", 18f, true)
        val complications = getCount(
            """SELECT COUNT(*) FROM surgeries
               WHERE (complications IS NOT NULL AND TRIM(complications) != '')
               OR (postoperative_complications IS NOT NULL AND TRIM(postoperative_complications) != '')"""
        )
        addText("عدد العمليات ذات الاختلاطات المسجلة: $complications")
        addButton("العودة للرئيسية") { showDashboard() }
        setContentView(root)
    }

    override fun onBackPressed() {
        showDashboard()
    }
}
