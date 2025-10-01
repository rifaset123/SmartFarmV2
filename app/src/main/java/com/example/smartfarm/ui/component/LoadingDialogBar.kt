package com.example.smartfarm.ui.component

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import com.example.smartfarm.R

class LoadingDialogBar(private val context: Context) {
    private var dialog: Dialog? = null

    fun showDialog(title: String) {
        dialog = Dialog(context)
        dialog?.setContentView(R.layout.item_dialog)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val textView: TextView? = dialog?.findViewById(R.id.loadingText)
        textView?.text = title
        dialog?.show()
    }

    fun hideDialog() {
        dialog?.dismiss()
    }
}