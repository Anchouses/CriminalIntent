package com.bignerdranch.android.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.criminalintent.database.CrimeDatabase
import java.util.*
import java.util.concurrent.Executors

const val DATABASE_NAME = "crime-database"   //data/data/com.bignerdranch.android.criminalintent/databases/crime-database
class CrimeRepository private constructor(context: Context){   //синглтон (одноэлементный класс - в процессе приложения единовременно существует только один его класс)

    private val database: CrimeDatabase = Room.databaseBuilder(   // реализация Database
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME).build()

    private val crimeDao = database.crimeDao()
    private val executor = Executors.newSingleThreadExecutor()  // Исполнитель, который ссылается на новый поток (thread - поток)

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    fun updateCrime(crime: Crime){
        executor.execute {                  // функция execute  принимает на выполнение блок кода
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime){
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }

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