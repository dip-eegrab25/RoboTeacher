package com.books.myslelf.db

import androidx.room.TypeConverter


class Converters {

//    @TypeConverter
//    fun fromListToString(villainList:List<MangaData.Data>?):String?{
//
//        return Gson().toJson(villainList)
//
//    }

    @TypeConverter
    fun fromListToString(quesList:List<String>):String?{

        return quesList.joinToString(",")

    }

    @TypeConverter
    fun fromStringToList(quesString:String?):List<String?>?{

        return quesString?.split(",")?:ArrayList<String?>()

    }

//    @TypeConverter
//    fun fromStringToVillainList(villainString:String?):ArrayList<MangaData.Data>?{
//
//        return Gson().fromJson(villainString, object : TypeToken<List<MangaData.Data>>() {
//
//        }.type)
//
//    }


}