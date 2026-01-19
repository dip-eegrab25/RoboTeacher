package com.ai.roboteacher.DB

import android.content.Context
import androidx.room.RoomDatabase
import com.ai.roboteacher.Models.NotificationData
import com.task.pupilsmeshtask.AppDatabase

object DBConfig {

    var appDatabase:AppDatabase?=null

    public fun getInstance(c:Context) {

        if (appDatabase == null) {

            appDatabase = androidx.room.Room.databaseBuilder(c, AppDatabase::class.java, "notification-db").build()
        }

    }

    public fun getAllNotifications():List<NotificationData> {

        appDatabase?.let {

           return appDatabase!!.getDataDao().getAllData()
        }?:return ArrayList<NotificationData>()

    }

    public fun insertNotification(data:NotificationData) {

        appDatabase?.getDataDao()?.insertData(data)

    }
}