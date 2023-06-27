package com.bignerdranch.android.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bignerdranch.android.criminalintent.Crime
import java.util.*

@Dao   // Data Access Object - объект доступа к базе данных
interface CrimeDao {
    @Query("SELECT * FROM Crime")   //почему с маленькой буквы?         запрос к БД
    fun getCrimes(): LiveData<List<Crime>>

    @Query("SELECT * FROM Crime WHERE id=(:id)")   // запрос к БД
    fun getCrime(id: UUID): LiveData<Crime?>

    @Update     //
    fun updateCrime(crime: Crime)

    @Insert  //вставить, включить, добавить
    fun addCrime(crime: Crime)
}