package com.barengific.ainance

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.barengific.ainance.dao.CategoryDao
import com.barengific.ainance.dao.ExpenseDao
import com.barengific.ainance.dao.IncomeDao
import com.barengific.ainance.obj.Category
import com.barengific.ainance.obj.Expense
import com.barengific.ainance.obj.Income

@Database(entities = [Expense::class, Income::class, Category::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private lateinit var INSTANCE:AppDatabase
        private lateinit var context: Context
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
    abstract fun incomeDao(): IncomeDao
    abstract fun categoryDao(): CategoryDao

}