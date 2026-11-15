
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
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {

    private val blue = Color.rgb(18, 75, 135)
    private val patients = mutableListOf<Patient>()

    private lateinit var listLayout: LinearLayout
    private lateinit var searchInput: EditText

    data class Patient(
        val id: String,
        val name: String,
        val age: String,
        val gender: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadPatients()
        buildScreen()
    }

    private fun buildScreen() {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.layoutDirection = View.LAYOUT_DIRECTION_RTL
        root.setPadding(18, 20, 18, 15)
        root.setBackgroundColor(Color.rgb(245, 248, 252))

        val header = TextView(this)
        header.text = "🧠 الجراحة العصبية\nNeurosurgery"
        header.textSize = 25f
        header.setTextColor(Color.WHITE)
        header.gravity = Gravity.CENTER
        header.setPadding(10, 25, 10, 25)
        header.setBackgroundColor(blue)
        root.addView(header)

        val title = TextView(this)
        title.text = "إدارة المرضى"
        title.textSize = 23f
        title.setTextColor(blue)
        title.setPadding(5, 20, 5, 15)
        root.addView(title)

        searchInput = EditText(this)
        searchInput.hint = "ابحث بالاسم أو رقم الملف"
        searchInput.textSize = 16f
        searchInput.setSingleLine(true)
        root.addView(searchInput)

        val addButton = Button(this)
        addButton.text = "+ إضافة مريض جديد"
        addButton.setBackgroundColor(blue)
        addButton.setTextColor(Color.WHITE)
        root.addView(addButton)

        val scroll = ScrollView(this)
        listLayout = LinearLayout(this)
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

        addButton.setOnClickListener {
            showAddPatientDialog()
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

    private fun showAddPatientDialog() {
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

                    val patient = Patient(
                        id = "NS-${System.currentTimeMillis()}",
                        name = name,
                        age = age,
                        gender = genderSpinner.selectedItem.toString()
                    )

                    patients.add(patient)
                    savePatients()
                    renderPatients()
                    dialog.dismiss()

                    Toast.makeText(
                        this,
                        "تم حفظ ملف المريض",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        dialog.show()
    }

    private fun renderPatients(query: String = "") {
        listLayout.removeAllViews()

        val filtered = patients.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.id.contains(query, ignoreCase = true)
        }

        if (filtered.isEmpty()) {
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
            return
        }

        filtered.forEach { patient ->
            val card = TextView(this)
            card.text = """
                👤 ${patient.name}
                رقم الملف: ${patient.id}
                العمر: ${patient.age}
                الجنس: ${patient.gender}
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

    private fun savePatients() {
        val array = JSONArray()

        patients.forEach { patient ->
            val obj = JSONObject()
            obj.put("id", patient.id)
            obj.put("name", patient.name)
            obj.put("age", patient.age)
            obj.put("gender", patient.gender)
            array.put(obj)
        }

        getSharedPreferences(
            "neurosurgery_patients",
            MODE_PRIVATE
        ).edit()
            .putString("patients", array.toString())
            .apply()
    }

    private fun loadPatients() {
        val saved = getSharedPreferences(
            "neurosurgery_patients",
            MODE_PRIVATE
        ).getString("patients", "[]")

        try {
            val array = JSONArray(saved)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                patients.add(
                    Patient(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        age = obj.getString("age"),
                        gender = obj.getString("gender")
                    )
                )
            }
        } catch (e: Exception) {
            patients.clear()
        }
    }
}
