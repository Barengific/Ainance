package com.barengific.ainance.obj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.barengific.ainance.Converters
import java.util.*

@Entity
@TypeConverters(Converters::class)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "withdraw") var withdraw: String?,
    @ColumnInfo(name = "category") var category: String?,
    @ColumnInfo(name = "date") var date: Date?
)