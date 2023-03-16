package com.barengific.ainance.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.barengific.ainance.obj.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category")
    fun getAll(): List<Category>

    @Query("SELECT * FROM category WHERE id IN (:wordIds)")
    fun loadAllByIds(wordIds: IntArray): List<Category>

    @Query("SELECT * FROM category WHERE name LIKE :k")
    fun findByName(k: String): Category

    @Insert
    fun insertAll(vararg category: Category)

    @Delete
    fun delete(category: Category)
}