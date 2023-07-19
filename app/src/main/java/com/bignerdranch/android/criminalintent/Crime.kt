package com.bignerdranch.android.criminalintent

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID
@Entity  //создаем сущность Room
data class Crime(@PrimaryKey var id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var date: Date = Date(),
                 var isSolved: Boolean = false,
                 var suspect: String = ""){
    val photoFileName
        get() = "IMG_$id.jpg"   //вычисляемое свойство для получения имени фото-файла
}

