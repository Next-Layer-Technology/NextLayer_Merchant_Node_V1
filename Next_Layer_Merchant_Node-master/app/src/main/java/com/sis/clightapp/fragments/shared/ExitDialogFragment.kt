package com.sis.clightapp.fragments.shared

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.sis.clightapp.R

class ExitDialogFragment(val onConfirm: () -> Unit) : DialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.alert_dialog_layout)
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog.setCancelable(false)
        val alertTitle = dialog.findViewById<TextView>(R.id.alertTitle)
        val alertMessage =
            dialog.findViewById<TextView>(R.id.alertMessage)
        val yes = dialog.findViewById<Button>(R.id.yesbtn)
        val no = dialog.findViewById<Button>(R.id.nobtn)
        yes.text = "Yes"
        no.text = "No"
        alertTitle.text = getString(R.string.exit_title)
        alertMessage.text = getString(R.string.exit_subtitle)
        yes.setOnClickListener { v: View? ->
            dialog.dismiss()
            onConfirm()
        }
        no.setOnClickListener { v: View? -> dialog.dismiss() }
        dialog.show()
        return dialog
    }

    companion object {
        const val TAG = "PurchaseConfirmationDialog"
    }

}