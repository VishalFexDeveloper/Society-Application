package com.example.society.DialogBox

import android.app.ProgressDialog
import android.content.Context

object ShowProgress {
    fun showProgressDialog(context: Context, message: String): ProgressDialog {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(message)
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }
}