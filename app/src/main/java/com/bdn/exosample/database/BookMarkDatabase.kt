package com.bdn.exosample.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bdn.exosample.database.dao.BookMarkDao
import com.bdn.exosample.database.table.BookMarkVideo


/**
 * Database class of the application
 * Provides database connection
 */
@Database(
    entities = [BookMarkVideo::class],
    version = 2,
    exportSchema = true
)

abstract class BookMarkDatabase : RoomDatabase() {

    abstract fun bookMarkDao(): BookMarkDao


    companion object {
        @Volatile
        private var INSTANCE: BookMarkDatabase? = null

        fun getDatabase(
            application: Application
        ): BookMarkDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    application,
                    BookMarkDatabase::class.java,
                    "BookMarkDB"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
