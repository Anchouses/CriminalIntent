package com.bignerdranch.android.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog( //   Creates a new date picker dialog for the specified date using the parent context's default date picker dialog theme.
            requireContext(),    // (require -  требовать),    родительский контекст
            null,         //listener – the listener to call when the user sets the date
            initialYear,
            initialMonth,
            initialDay
        )
    }
}