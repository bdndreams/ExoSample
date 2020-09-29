package com.bdn.exosample.database.dao

import androidx.room.*
import com.bdn.exosample.database.table.BookMarkVideo

/**
 * It handles the database operation for User Info
 */
@Dao
abstract class BookMarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(bookMarkVideo: BookMarkVideo): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(bookMarkVideo: BookMarkVideo): Int


    @Query("select * from BookMarkVideo where path = :videoPath")
    abstract fun getBookMarkVideo(videoPath: String): BookMarkVideo?

    @Query("select * from BookMarkVideo")
    abstract fun getAllBookMarkVideo(): List<BookMarkVideo>

}