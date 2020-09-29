package com.bdn.exosample.database.table

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BookMarkVideo")
data class BookMarkVideo(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    var path:String?,
    var isBookMark:Boolean?= false) {
}