package com.barengific.ainance.obj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "category") var category: Category?,
    @ColumnInfo(name = "withdraw") var withdraw: String?
)