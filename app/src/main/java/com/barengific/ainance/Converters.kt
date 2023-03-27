package com.barengific.ainance

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class Converters {
    @TypeConverter
    fun fromDate(date: Date?): String? {
        return date?.let { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it) }
    }

    @TypeConverter
    fun toDate(dateString: String?): Date? {
        return dateString?.let { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(it) }
    }

}