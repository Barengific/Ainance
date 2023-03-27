package com.barengific.ainance

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.barengific.ainance.dao.CategoryDao
import com.barengific.ainance.dao.ExpenseDao
import com.barengific.ainance.obj.Category
import com.barengific.ainance.obj.Expense

@Database(entities = [Expense::class, Category::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        fun getInstance(con: Context):AppDatabase= Room.databaseBuilder(
            con,
            AppDatabase::class.java,
            "expenSea"
        )

            .createFromAsset("expenses.db")
            .allowMainThreadQueries()
            .build()

    }

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao

}