package com.barengific.ainance.obj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.barengific.ainance.obj.Category

@Entity
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "category") var category: String?,
    @ColumnInfo(name = "payed") var payed: String?,
    @ColumnInfo(name = "date") var date: String?
)