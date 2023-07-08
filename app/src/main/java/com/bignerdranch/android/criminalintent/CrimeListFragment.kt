package com.bignerdranch.android.criminalintent

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*


private const val TAG = "CrimeListFragment"

class CrimeListFragment: Fragment() {

    /**
     * требуемый интерфейс
     */
    interface Callbacks{
        fun onCrimeSelected(crimeId: UUID)
    }
    private var callbacks: Callbacks? = null
    private lateinit var crimeRecyclerView: RecyclerView
//    private lateinit var addCrime: Button
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())   //передаем в адаптер пустой лист в ожидании загрузки БД

    override fun onAttach(context: Context) {   //функция жизненного цикла Fragment - зачем ее переопределять (для установки и отмены свойства callbacks)
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this)[CrimeListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater,  //наполнение
        container: ViewGroup?,     //
        savedInstanceState: Bundle?  //сохраненное состояние экземпляра
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)   // (resource, root, attach to root)
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(   //используется для регистрации наблюдателя за экземпляром LiveData
            viewLifecycleOwner,       // определяет время жизни наблюдателя. Владелец жизненного цикла тут фрагмент, viewLifecycleOwner - его интерфейс
            Observer { crimes ->       // Observer -  объект, отвечающий за реакцию на новые данные LiveData - наблюдатель
                crimes?.let {          // когда список преступлений готов, наблюдатель посылает список в updateUI для adapter
                    Log.i(TAG, "Got crimes ${crimes.size}")
//                    addCrime = view.findViewById(R.id.button)
//                    if (crimes.isEmpty()) addCrime.isEnabled = true
                    updateUI(crimes)
                }
            })
    }

    override fun onDetach(){   //функция жизненного цикла Fragment
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }
    private fun updateUI(crimes: List<Crime>){   //
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

    private inner class CrimeHolder(view:View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var crime: Crime
        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime){
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = DateFormat.getLongDateFormat(context).format(this.crime.date)             //format("EEEE, dd MMM, hh:mm", this.crime.date)

            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>): RecyclerView.Adapter<CrimeHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)

            return CrimeHolder(view)
        }

        override fun getItemCount(): Int = crimes.size

       override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
           val crime = crimes[position]
           holder.bind(crime)
       }

    }
    companion object {
        fun newInstance(): CrimeListFragment{
            return CrimeListFragment()
        }
    }

}