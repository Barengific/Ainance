package com.barengific.ainance.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.barengific.ainance.obj.Expense
import java.text.SimpleDateFormat
import java.util.*

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense")
    fun getAll(): List<Expense>

    @Query("SELECT * FROM expense WHERE id IN (:expenseIds)")
    fun loadAllByIds(expenseIds: IntArray): List<Expense>

    @Query("SELECT * FROM expense WHERE description LIKE :k")
    fun findByDescription(k: String): Expense

    @Query("SELECT * FROM expense WHERE category LIKE :v")
    fun findByExpense(v: String): Expense

    @Query("SELECT * FROM Expense WHERE date BETWEEN :startDate AND :endDate")
    fun getExpensesInDateRange(startDate: String, endDate: String): List<Expense>

//    @Query("SELECT * FROM Expense WHERE date BETWEEN date(:startDate) AND date(:endDate)")
//    fun getExpensesInDateRange(startDate: Date, endDate: Date): List<Expense>

//    @Query("SELECT * FROM Expense WHERE date BETWEEN :startDate AND :endDate")
//    fun getExpensesInDateRange(startDate: String, endDate: String): List<Expense> {
//        val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
//        val startDateObj = format.parse(startDate)
//        val endDateObj = format.parse(endDate)
//        return getExpensesInDateRange(startDateObj.toString(), endDateObj.toString())
//    }

    @Insert
    fun insertAll(vararg expense: Expense)

    @Delete
    fun delete(expense: Expense)
}