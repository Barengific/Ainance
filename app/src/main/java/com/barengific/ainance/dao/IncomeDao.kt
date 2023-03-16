package com.barengific.ainance.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.barengific.ainance.obj.Income

@Dao
interface IncomeDao {
    @Query("SELECT * FROM income")
    fun getAll(): List<Income>

    @Query("SELECT * FROM income WHERE id IN (:incomeIds)")
    fun loadAllByIds(incomeIds: IntArray): List<Income>

    @Query("SELECT * FROM income WHERE description LIKE :k")
    fun findByDescription(k: String): Income

    @Query("SELECT * FROM income WHERE category LIKE :v")
    fun findByCategory(v: String): Income

    @Insert
    fun insertAll(vararg income: Income)

    @Delete
    fun delete(income: Income)
}