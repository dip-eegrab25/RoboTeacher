package com.ai.roboteacher

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloadTask(var c:WeakReference<Context>):AsyncTask<String,Float,Int>() {

    interface ModelDownloadListener{

        fun onDownloadStarted()
        fun onDownloadComplete(result:Int)
        fun onError()
        fun onProgress(progress:Float)
    }

    var totalModelSize = 79348845
    var modelDownloadListener:ModelDownloadListener?=null

    var filesToDownload = 0

    override fun onPreExecute() {
        super.onPreExecute()

        modelDownloadListener = c.get() as ModelDownloadListener
        modelDownloadListener!!.onDownloadStarted()



    }

    override fun onProgressUpdate(vararg values: Float?) {
        super.onProgressUpdate(*values)

        modelDownloadListener!!.onProgress(values[0]!!)
    }


    override fun doInBackground(vararg params: String?): Int {

        filesToDownload = params.size

        var filesDownloaded = 0
        var downloaded = 0

        for (fileurl in params) { //modelArr contains model urls

            fileurl?.split("/").also {

                val fileName = it?.get(it.size-1)

                var input: InputStream? = null
                var output: FileOutputStream? = null
                var connection: HttpURLConnection? = null

                var url:URL? = null

                var oldFile = File(c.get()?.getExternalFilesDir(null),fileName!!)

                try {


                    if (!oldFile.exists()) {

                        url = URL(fileurl)
                        connection = url.openConnection() as HttpURLConnection
                        connection.connect()

                        // total size
                        val totalSize = connection.contentLength


                        input = connection.inputStream

                        output = FileOutputStream(oldFile)

                        val buffer = ByteArray( 1024*1024)   // 8KB buffer (standard)

                        var read: Int

                        // Cache views once


                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloaded += read

                            if (totalSize > 0) {

                                val percent = (downloaded * 100f / totalModelSize)
                                publishProgress(percent)

                            }
                        }

                        output?.flush()
                        input?.close()
                        output?.close()

                        filesDownloaded++ // to keep track of downloaded files

                    }

//                    else {
//
//                        filesDownloaded++ //if already exists update file counter
//                        // which will be checked later while loading model
//
////
//
//                    }





                } catch (ex: Exception) {

                    Log.d(ModelDownloadTask::class.java.name, "doInBackground: "+ex.message)

                    if (oldFile.exists()) {

                        oldFile.delete()
                    }



                }


            }

        }

        return filesDownloaded


    }

    override fun onPostExecute(result: Int?) {
        super.onPostExecute(result)

        if (result == filesToDownload) {

            modelDownloadListener!!.onDownloadComplete(result)

        } else {

            modelDownloadListener!!.onError()


        }
    }
}