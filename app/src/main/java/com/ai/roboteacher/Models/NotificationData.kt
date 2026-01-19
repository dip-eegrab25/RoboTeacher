package com.ai.roboteacher.Models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.ArrayList

@Entity(tableName = "NotificationData")

data class NotificationData(@PrimaryKey(autoGenerate = false)@ColumnInfo("id")var id:Int=0
                            ,@ColumnInfo("title")var title:String?=null
, @ColumnInfo("description")var description:String?=null
                            ,@ColumnInfo("date")var date:String?=null
                            ,@ColumnInfo("time")var time:String?=null
, @ColumnInfo("status")var status:Boolean=false,
                            @ColumnInfo("questions")var questions:List<String> = ArrayList())
