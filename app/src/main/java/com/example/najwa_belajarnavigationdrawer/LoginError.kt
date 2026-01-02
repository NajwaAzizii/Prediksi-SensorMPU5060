package com.example.najwa_belajarnavigationdrawer

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.button.MaterialButton

class LoginError(
    context: Context
) : Dialog(context) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.login_error)

        // biar CardView kelihatan (background dialog transparan)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        setCancelable(true)
    }

    fun setMessage(msg: String) {
        findViewById<TextView>(R.id.tvError)?.text = msg
    }

    fun setOnRetry(listener: () -> Unit) {
        findViewById<MaterialButton>(R.id.btnRetry)?.setOnClickListener {
            dismiss()
            listener()
        }
    }
}
