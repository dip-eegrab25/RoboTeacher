package com.task.pupilsmeshtask

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ai.roboteacher.Models.NotificationData
import com.books.myslelf.db.Converters


@Database(entities = [NotificationData::class] , version = 1)
@TypeConverters(Converters::class)

abstract class AppDatabase : RoomDatabase() {

    abstract fun getDataDao() : DataDao

}