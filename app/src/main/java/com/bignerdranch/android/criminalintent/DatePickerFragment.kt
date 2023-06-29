package com.bignerdranch.android.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"
class DatePickerFragment: DialogFragment() {

    interface Callbacks {
        fun onDateSelected(date:Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dateListener = DatePickerDialog.OnDateSetListener{
            _: DatePicker, year: Int, month: Int, day: Int ->       // _ - DatePicker, от которого исходит результут. Тут не используется
            val resultDate: Date = GregorianCalendar(year, month, day).time
            targetFragment?.let {
                (it as Callbacks).onDateSelected(resultDate)
            }
        }
        val date = arguments?.getSerializable(ARG_DATE) as Date // ????????
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog( //   Creates a new date picker dialog for the specified date using the parent context's default date picker dialog theme.
            requireContext(),    // (require -  требовать),    родительский контекст
            dateListener,         //listener – the listener to call when the user sets the date
            initialYear,
            initialMonth,
            initialDay
        )
    }

    companion object{
        fun newInstance(date: Date): DatePickerFragment{
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}