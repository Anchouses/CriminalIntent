package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import kotlin.math.roundToInt

fun getScaledBitmap(path: String, activity: Activity): Bitmap{  // функця для масштфабирования Bitmap  под размер конкретной Activity
    val size = Point()
    activity.windowManager.defaultDisplay.getSize(size)   //activity.windowManager.defaultDisplay.getSize(size)
    return getScaledBitmap(path, size.x, size.y)  //size.x, size.y

}

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
    // чтение размеров изображения на диске
    var options = BitmapFactory.Options()   // вызываем ф-ю Options
    options.inJustDecodeBounds = true   // true - декодер возвращает null  вместо рисунка, но будут заданы поля , что позволит вызывающему запрашивать точечный рисунок без выделения памяти для пикселей
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    //выясняем, на сколько нужно уменьшить
    var inSampleSize = 1   // определяет величину образца каждого исходного изображения (1 - 1:1, 2 - 1/2 от размера и 1/4 от кол-ва пикселей, 4 - 1/4 от размера и 1/16 от кол-ва пикселей)
    if (srcHeight > destHeight || srcWidth > destWidth) {  // src - source, dest - destination
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destHeight

        val sampleScale = if (heightScale > widthScale) {
            heightScale
        } else {
            widthScale
        }
        inSampleSize = sampleScale.roundToInt()  //округление до ближайшего целого.   inSampleSize - коэффициент, на который будем уменьшать изображение
    }

    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    // Чтение и сознание окончательного растрового изображения
    return BitmapFactory.decodeFile(path, options)
}
