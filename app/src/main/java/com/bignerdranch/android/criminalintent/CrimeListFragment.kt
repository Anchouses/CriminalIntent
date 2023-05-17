package com.bignerdranch.android.criminalintent

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "CrimeListFragment"

class CrimeListFragment: Fragment() {

    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = null

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this)[CrimeListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {   // можно убрать
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Total crimes: ${crimeListViewModel.crimes.size}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,  //наполнение
        container: ViewGroup?,     //
        savedInstanceState: Bundle?  //сохраненное состояние экземпляра
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)   // (resource, root, attach to root)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)

        updateUI()

        return view

    }

    private fun updateUI(){
        val crimes = crimeListViewModel.crimes
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

    private abstract class CrimeHolder(view:View) : RecyclerView.ViewHolder(view) {
        lateinit var crime: Crime
        val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
    }
    private inner class CrimeHolderUsual(view:View) :
        View.OnClickListener, CrimeHolder(view) {  // inner - внутренний класс
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
            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_LONG)
                .show()
        }
    }

    private inner class CrimeHolderDangerous(view:View) : CrimeHolder(view) {  // inner - внутренний класс

        val button: Button = view.findViewById(R.id.call_police)

        fun bind(crime: Crime){
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = DateFormat.format("EEEE, MMM dd yyyy", this.crime.date)
            button.setOnClickListener{
                Toast.makeText(context, "Police on the way!", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>): RecyclerView.Adapter<CrimeHolder>(){

        override fun getItemViewType(position: Int): Int {
            val crime: Crime = crimes[position]
            return when (crime.requiresPolice){
                false -> 1
                true -> 0
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            return when (viewType) {
                1 -> {val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
                    CrimeHolderUsual(view)}
                else -> {val view = layoutInflater.inflate(R.layout.list_item_crime_police, parent, false)
                    CrimeHolderDangerous(view)}
            }
        }

        override fun getItemCount(): Int = crimes.size

       override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
           val crime = crimes[position]

           when (holder){
              is CrimeHolderUsual -> holder.bind(crime)
               is CrimeHolderDangerous -> holder.bind(crime)
           }
       }

    }
    companion object {
        fun newInstance(): CrimeListFragment{
            return CrimeListFragment()
        }
    }

}