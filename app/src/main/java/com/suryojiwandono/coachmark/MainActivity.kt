package com.suryojiwandono.coachmark

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView

class MainActivity : AppCompatActivity() {
    private var textView: AppCompatTextView? = null
    private var button: AppCompatButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.text_view)
        button = findViewById(R.id.button)
        button?.setOnClickListener {
            onClickButton()
        }
    }

    private fun onClickButton() {
        val listCoachMark = mutableListOf<CoachMark>()
        listCoachMark.add(
            CoachMark(
                view = textView,
                title = "Text View",
                description = "Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old.",
                isBackground = true
            )
        )
        listCoachMark.add(
            CoachMark(
                view = button,
                title = "Button",
                description = "There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don't look even slightly believable."
            )
        )
        val coachmark = CoachMark.Builder(this)
            .coachMark(listCoachMark)
            .build()
        coachmark.show()
    }
}