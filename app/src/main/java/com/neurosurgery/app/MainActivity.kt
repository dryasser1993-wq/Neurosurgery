
package com.neurosurgery.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private val blue = Color.rgb(18, 75, 135)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Color.rgb(245, 248, 252))

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        
        layout.setPadding(20, 25, 20, 20)

        val header = TextView(this)
        header.text = "🧠\nالجراحة العصبية\nNeurosurgery"
        header.textSize = 27f
        header.setTextColor(Color.WHITE)
        header.gravity = Gravity.CENTER
        header.setPadding(15, 35, 15, 35)
        header.setBackgroundColor(blue)
        layout.addView(header)
layout.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
        val subtitle = TextView(this)
        subtitle.text = "نظام إدارة المرضى والفحوصات والعمليات"
        subtitle.textSize = 16f
        subtitle.setTextColor(blue)
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(10, 25, 10, 20)
        layout.addView(subtitle)

        addSection(layout, "إدارة المرضى")
        addItem(layout, "👤 ملف المرضى")
        addItem(layout, "🩺 الفحص العصبي")
        addItem(layout, "📋 الزيارات والمتابعة")

        addSection(layout, "الجراحة والعمليات")
        addItem(layout, "🏥 العمليات الجراحية")
        addItem(layout, "👨‍⚕️ الأخصائيون والمقيمون")
        addItem(layout, "📊 الإحصائيات")

        addSection(layout, "إدارة النظام")
        addItem(layout, "📄 التقارير")
        addItem(layout, "☁️ المزامنة والنسخ الاحتياطي")
        addItem(layout, "⚙️ الإعدادات")

        val footer = TextView(this)
        footer.text = "Neurosurgery • Beta 1.0"
        footer.gravity = Gravity.CENTER
        footer.setTextColor(blue)
        footer.setPadding(10, 30, 10, 20)
        layout.addView(footer)

        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun addSection(parent: LinearLayout, title: String) {
        val text = TextView(this)
        text.text = title
        text.textSize = 20f
        text.setTextColor(blue)
        text.setPadding(5, 25, 5, 12)
        parent.addView(text)
    }

    private fun addItem(parent: LinearLayout, title: String) {
        val item = TextView(this)
        item.text = title
        item.textSize = 18f
        item.setTextColor(blue)
        item.setBackgroundColor(Color.WHITE)
        item.setPadding(20, 22, 20, 22)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 5, 0, 10)
        parent.addView(item, params)
    }
}
