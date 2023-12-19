package app.sthenoteuthis.mobile.ui.notifications

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class WaterQualityNotification : DialogFragment() {


    companion object {
        private const val ARG_MESSAGE = "Water quality is within acceptable range"

        fun newInstance(message: String): WaterQualityNotification {
            val fragment = WaterQualityNotification()
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = arguments?.getString(ARG_MESSAGE) ?: "Water quality is within acceptable range"

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Alert")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}