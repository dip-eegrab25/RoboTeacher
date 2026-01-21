package com.task.pupilsmeshtask

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ai.roboteacher.Models.NotificationData


@Dao
interface DataDao {

    @Insert
     fun insertData(books: NotificationData);

    @Query("select * from NotificationData")
     fun getAllData():List<NotificationData>

    @Query("select * from NotificationData where id=:id")
    suspend fun searchData(id:Int):List<NotificationData>

//    @Query("Delete from Data where id=:bookId")
//    suspend fun deleteData(bookId:Int)
//
//    @Update
//    suspend fun updateData(b: MangaData.Data)
//
//    @Delete
//    suspend fun deleteAllData(dBookList:List<MangaData.Data>)
//
//    @Query("select * from Data where LOWER(Title) like LOWER(:s || '%')")
//    suspend fun searchData(s:String):List<MangaData.Data>



}