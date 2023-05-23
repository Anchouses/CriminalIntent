package com.bignerdranch.android.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.criminalintent.database.CrimeDatabase
import java.util.*

const val DATABASE_NAME = "crime-database"   //data/data/com.bignerdranch.android.criminalintent/databases/crime-database
class CrimeRepository private constructor(context: Context){   //синглтон (одноэлементный класс - в процессе приложения единовременно существует только один его класс)

    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME).build()

    private val crimeDao = database.crimeDao()
//    init {
//        val ass = database.openHelper.writableDatabase.path
//        Log.d("aaaaaaa", ass.orEmpty())
//    }
    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    companion object{
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context){
            if (INSTANCE == null){
                INSTANCE = CrimeRepository(context) }
        }
        fun get(): CrimeRepository {
            return INSTANCE ?:
            throw java.lang.IllegalStateException("CrimeRepository must be initialized")
        }
    }
}