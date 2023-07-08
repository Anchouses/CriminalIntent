package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.util.*

private const val TAG  = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeFragment: Fragment(), DatePickerFragment.Callbacks {

private lateinit var crime: Crime
private lateinit var titleField: EditText
private lateinit var dateButton: Button
private lateinit var solvedCheckBox: CheckBox
private lateinit var reportButton: Button
private lateinit var suspectButton: Button
private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
    ViewModelProvider(this)[CrimeDetailViewModel::class.java]
}

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {  //настраиваем экземпляр фрагмента, но не заполняем представление
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(   // заполняем представление фрагмента
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title)
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.checkBox) as CheckBox
        reportButton = view.findViewById(R.id.send_crime_report) as Button
        suspectButton = view.findViewById(R.id.choose_suspect)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer{ crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            })
    }
    override fun onStart() {     // добавляем слушателя к EditText
        super.onStart()
        val titleWatcher = object: TextWatcher{
            override fun beforeTextChanged(sequence: CharSequence?,   //sequence - последовательность, порядок
                                           start: Int,
                                           count: Int,
                                           after: Int) {
               // это оставляем пустым специально
            }

            override fun onTextChanged(sequence: CharSequence?,
                                       start: Int,
                                       before: Int,
                                       count: Int) {
                crime.title = sequence.toString()  //sequence - последовательность
            }

            override fun afterTextChanged(sequence: Editable?) {
                //и это
            }
        }
        titleField.addTextChangedListener(titleWatcher)   //Добавляем TextWatcher в список методов, которые вызываются при изменении текста TextView.
        solvedCheckBox.apply {      //
            setOnCheckedChangeListener{_, isChecked ->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)   //?????????
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->  val chooseIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooseIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

        }
    }


    override fun onStop(){
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }
    private fun updateUI(){
        titleField.setText(crime.title)
        dateButton.text = DateFormat.getLongDateFormat(context).format(this.crime.date) //crime.date.toString()   //DateFormat.getLongDateFormat(context).format(this.crime.date)
//        solvedCheckBox.isChecked = crime.isSolved
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()){
            suspectButton.text = crime.suspect
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when { resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data   // запрос всех отображаемых имен контактов
                //
                val queryFields = arrayOf(ContactsContract.Contacts.HAS_PHONE_NUMBER)   //массив контактов, (DISPLAY_NAME)
                //
                val cursor = contactUri?.let { requireActivity().contentResolver.query(it, queryFields, null, null, null) }
                cursor?.use{  // проверяем, что курсор содержит хотя бы одну строку
                    if (it.count == 0) {
                        return
                    }

                    // Первый столюец первой строки данных - это имя подозреваемого
                    it.moveToFirst()   //функция moveToFirst для перемещения курсора в первую строку
                    val suspect = it.getString(0)  //получение содержимого первого столбца в виде строки
                    crime.suspect = suspect    //присваиваем имя подозреваемого свойству suspect текущего crime
                    crimeDetailViewModel.saveCrime(crime)  //сохраняем crime с новым свойством в базу данных
                    suspectButton.text = suspect  //
                }

            }
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()

        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    companion object{
        fun newInstance(crimeId: UUID): CrimeFragment{
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args //dfkgpodfkgopakfp
            }
        }
    }

}
