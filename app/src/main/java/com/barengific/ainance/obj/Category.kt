package com.barengific.ainance.obj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "type") var type: Boolean?,
)