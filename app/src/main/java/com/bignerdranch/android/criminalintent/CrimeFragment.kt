package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.text.DateFormat.getDateInstance
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val REQUEST_PHONE = 3
private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeFragment: Fragment(), DatePickerFragment.Callbacks {

private lateinit var crime: Crime
private lateinit var titleField: EditText
private lateinit var dateButton: Button
private lateinit var solvedCheckBox: CheckBox
private lateinit var reportButton: Button
private lateinit var suspectButton: Button
private lateinit var callButton: Button
private lateinit var photoButton: ImageButton
private lateinit var photoView: ImageView
private lateinit var photoFile: File   //
private lateinit var photoUri: Uri
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
        callButton = view.findViewById(R.id.call_suspect)
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer{ crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)  //сохранение местонахождения файла фотографии
                    photoUri = FileProvider.getUriForFile(requireActivity(), // FileProvider.getUriForFile преобразует локальный путь к файлу в URI, который видит приложение камеры
                        "com.bignerdranch.android.criminalintent.fileprovider", photoFile)
                    updateUI()
                }
            }
        )
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

            //pickContactIntent.addCategory(Intent.CATEGORY_HOME)  // дополнительная категория, которая предотвращает совпадения приложений адресной книги с интентом
            val packageManager: PackageManager = requireActivity().packageManager   //PackageManager  знает все о компонентах, установленных на устройстве Андроид
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)  //вызывая resolvedActivity даем ему команду найти  Activity, соответствующую интенту
            if (resolvedActivity == null) {            //возвращается экземпляр ResolveInfo  с инфой об активити
                isEnabled = false   // если поиск активити вернул результат null, то кнопка suspectButton блокируется
            }

        }

        callButton.setOnClickListener{
//            Intent(Intent.ACTION_DIAL).apply {
//                ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
//                putExtra()
//            }



//                val packageManager: PackageManager = requireActivity().packageManager   //PackageManager  знает все о компонентах, установленных на устройстве Андроид
//                val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(newIntent, PackageManager.MATCH_DEFAULT_ONLY)  //вызывая resolvedActivity даем ему команду найти  Activity, соответствующую интенту
//                if (resolvedActivity == null) {            //возвращается экземпляр ResolveInfo  с инфой об активити
//                    isEnabled =
//                        false   // если поиск активити вернул результат null, то кнопка suspectButton блокируется
//                }


        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager  //создаем свойство packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)  // создаем  Intent для запуска Activity камеры и создания снимка
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY) // MATCH_DEFAULT_ONLY - Флаг разрешения и запроса: если установлен, будут рассматриваться только фильтры, поддерживающие Intent.CATEGORY_DEFAULT
            if (resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri) //Добавить расширенные данные в объект Intent.

                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)   // список всех Activity, которые могут обрабатывать интент cameraImage

                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(cameraActivity.activityInfo.packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION) // FLAG_GRANT_WRITE_URI_PERMISSION флаг разрешение приложению камеры записывать photoUri
                }

                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

        photoView.setOnClickListener{

        }
    }


    override fun onStop(){
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDetach(){
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION) //отзыв разрешения на запись у активити с камерой
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }
    private fun updateUI(){
        titleField.setText(crime.title)
        dateButton.text = getDateInstance().format(this.crime.date)//getLongDateFormat(context).format(this.crime.date) //crime.date.toString()   //DateFormat.getLongDateFormat(context).format(this.crime.date)
//        solvedCheckBox.isChecked = crime.isSolved
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()){
            suspectButton.text = crime.suspect
        }

        updatePhotoView()
    }

    private fun updatePhotoView() {  //функция для обновления photoView
        if (photoFile.exists()){   //если  photoFile существует
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when { resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data   // запрос всех отображаемых имен контактов
                //
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)   //массив контактов, (DISPLAY_NAME)
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

            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION) //отзыв разрешения на запись у активити с камерой
                updatePhotoView()
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
                arguments = args
            }
        }
    }

}
