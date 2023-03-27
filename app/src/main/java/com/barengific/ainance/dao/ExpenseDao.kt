package com.barengific.ainance.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.barengific.ainance.obj.Expense
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

    @Insert
    fun insertAll(vararg expense: Expense)

    @Delete
    fun delete(expense: Expense)
}