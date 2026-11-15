
package com.neurosurgery.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {

    private val blue = Color.rgb(18, 75, 135)
    private val pale = Color.rgb(245, 248, 252)
    private lateinit var database: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = DatabaseHelper(this)
        showHome()
    }

    private fun showHome() {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.layoutDirection = View.LAYOUT_DIRECTION_RTL
        root.setPadding(18, 20, 18, 15)
        root.setBackgroundColor(pale)

        val scroll = ScrollView(this)
        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL

        val header = TextView(this)
        header.text = "🧠\nالجراحة العصبية\nNeurosurgery"
        header.textSize = 27f
        header.setTextColor(Color.WHITE)
        header.gravity = Gravity.CENTER
        header.setPadding(15, 30, 15, 30)
        header.setBackgroundColor(blue)
        content.addView(header)

        val subtitle = TextView(this)
        subtitle.text = "نظام إدارة قسم جراحة الأعصاب"
        subtitle.textSize = 17f
        subtitle.setTextColor(blue)
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(10, 22, 10, 20)
        content.addView(subtitle)

        addSection(content, "إدارة المرضى")
        addMenuButton(content, "👤 إدارة المرضى") {
            showPatients()
        }

        addSection(content, "الفحص والمتابعة")
        addMenuButton(content, "🩺 الفحص العصبي") {
            showInfo("الفحص العصبي", "سيتم إضافة نموذج الفحص العصبي وربطه بملف المريض.")
        }
        addMenuButton(content, "📋 الزيارات والمتابعة") {
            showInfo("الزيارات والمتابعة", "سيتم إضافة سجل الزيارات والخطط العلاجية.")
        }

        addSection(content, "الجراحة والعمليات")
        addMenuButton(content, "🏥 العمليات الجراحية") {
            showInfo("العمليات الجراحية", "سيتم إضافة سجل العمليات وربطه بملف المريض.")
        }
        addMenuButton(content, "👨‍⚕️ الأخصائيون والمقيمون") {
            showInfo("الفريق الطبي", "سيتم إضافة إدارة الأخصائيين والمقيمين.")
        }

        addSection(content, "التقارير وإدارة النظام")
        addMenuButton(content, "📊 الإحصائيات") {
            showInfo(
                "الإحصائيات",
                "عدد المرضى المسجلين: ${database.getPatientCount()}"
            )
        }
        addMenuButton(content, "📄 التقارير") {
            showInfo("التقارير", "سيتم تطوير التقارير لاحقاً.")
        }
        addMenuButton(content, "⚙️ الإعدادات والنسخ الاحتياطي") {
            showInfo("الإعدادات", "سيتم تطوير الحماية والنسخ الاحتياطي.")
        }

        val footer = TextView(this)
        footer.text = "Neurosurgery • Beta 1.0"
        footer.gravity = Gravity.CENTER
        footer.setTextColor(blue)
        footer.setPadding(10, 30, 10, 20)
        content.addView(footer)

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun addSection(parent: LinearLayout, title: String) {
        val text = TextView(this)
        text.text = title
        text.textSize = 20f
        text.setTextColor(blue)
        text.setPadding(5, 22, 5, 10)
        parent.addView(text)
    }

    private fun addMenuButton(
        parent: LinearLayout,
        title: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = title
        button.textSize = 17f
        button.setTextColor(blue)
        button.setBackgroundColor(Color.WHITE)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 5, 0, 8)
        parent.addView(button, params)

        button.setOnClickListener { action() }
    }

    private fun showInfo(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("موافق", null)
            .show()
    }

    private fun showPatients() {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.layoutDirection = View.LAYOUT_DIRECTION_RTL
        root.setPadding(18, 18, 18, 12)
        root.setBackgroundColor(pale)

        val header = TextView(this)
        header.text = "👤 إدارة المرضى"
        header.textSize = 24f
        header.setTextColor(Color.WHITE)
        header.gravity = Gravity.CENTER
        header.setPadding(12, 22, 12, 22)
        header.setBackgroundColor(blue)
        root.addView(header)

        val backButton = Button(this)
        backButton.text = "العودة إلى الرئيسية"
        backButton.setOnClickListener { showHome() }
        root.addView(backButton)

        val searchInput = EditText(this)
        searchInput.hint = "ابحث بالاسم أو رقم الملف"
        searchInput.setSingleLine(true)
        root.addView(searchInput)

        val addButton = Button(this)
        addButton.text = "+ إضافة مريض جديد"
        addButton.setBackgroundColor(blue)
        addButton.setTextColor(Color.WHITE)
        root.addView(addButton)

        val scroll = ScrollView(this)
        val listLayout = LinearLayout(this)
        listLayout.orientation = LinearLayout.VERTICAL
        listLayout.setPadding(0, 15, 0, 15)
        scroll.addView(listLayout)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        fun renderPatients(query: String = "") {
            listLayout.removeAllViews()

            val sql = """
                SELECT id, full_name, age, gender
                FROM patients
                WHERE full_name LIKE ? OR id LIKE ?
                ORDER BY created_at DESC
            """.trimIndent()

            val cursor = database.readableDatabase.rawQuery(
                sql,
                arrayOf("%$query%", "%$query%")
            )

            var found = false

            cursor.use {
                while (it.moveToNext()) {
                    found = true

                    val id = it.getString(0)
                    val name = it.getString(1)
                    val age = it.getString(2) ?: ""
                    val gender = it.getString(3) ?: ""

                    val card = TextView(this)
                    card.text = """
                        👤 $name
                        رقم الملف: $id
                        العمر: $age
                        الجنس: $gender
                    """.trimIndent()

                    card.textSize = 16f
                    card.setTextColor(blue)
                    card.setPadding(20, 20, 20, 20)
                    card.setBackgroundColor(Color.WHITE)

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 5, 0, 12)
                    listLayout.addView(card, params)
                }
            }

            if (!found) {
                val empty = TextView(this)
                empty.text = if (query.isEmpty()) {
                    "لا يوجد مرضى مسجلون حتى الآن"
                } else {
                    "لا توجد نتائج مطابقة"
                }
                empty.textSize = 17f
                empty.setTextColor(blue)
                empty.gravity = Gravity.CENTER
                empty.setPadding(10, 35, 10, 35)
                listLayout.addView(empty)
            }
        }

        addButton.setOnClickListener {
            showAddPatientDialog {
                renderPatients(searchInput.text.toString())
            }
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                renderPatients(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        setContentView(root)
        renderPatients()
    }

    private fun showAddPatientDialog(onSaved: () -> Unit) {
        val form = LinearLayout(this)
        form.orientation = LinearLayout.VERTICAL
        form.setPadding(25, 15, 25, 5)

        val nameInput = EditText(this)
        nameInput.hint = "اسم المريض الكامل"
        form.addView(nameInput)

        val ageInput = EditText(this)
        ageInput.hint = "العمر"
        ageInput.inputType = 2
        form.addView(ageInput)

        val genderSpinner = Spinner(this)
        val genders = listOf("ذكر", "أنثى")
        genderSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            genders
        )
        form.addView(genderSpinner)

        val dialog = AlertDialog.Builder(this)
            .setTitle("إضافة مريض جديد")
            .setView(form)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حفظ", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val name = nameInput.text.toString().trim()
                    val age = ageInput.text.toString().trim()

                    if (name.isEmpty() || age.isEmpty()) {
                        Toast.makeText(
                            this,
                            "يرجى إدخال الاسم والعمر",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    val id = "NS-${System.currentTimeMillis()}"

                    val saved = database.insertPatient(
                        id = id,
                        name = name,
                        age = age,
                        gender = genderSpinner.selectedItem.toString()
                    )

                    if (saved) {
                        onSaved()
                        dialog.dismiss()
                        Toast.makeText(
                            this,
                            "تم حفظ ملف المريض",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "تعذر حفظ ملف المريض",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        dialog.show()
    }
}
