package com.ai.roboteacher.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
import com.ai.roboteacher.R
import com.anim.circleanim.MyAnim
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import org.json.JSONObject

class AttendanceActivity:AppCompatActivity() {

    private var webView:WebView?=null
    private var url:String = "http://192.168.1.41/panda/pages/Attendance/video_feed.php?class_id=%d"
    private var bundle:Bundle?=null
    private var progressAnim:MyAnim?=null
    private var back:ImageView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enterFullScreenMode(window)
        setContentView(R.layout.activity_attendance)

        bundle = intent.extras

        webView = findViewById(R.id.web_view)
        progressAnim = findViewById(R.id.progress_anim)
        back = findViewById(R.id.back)
        back!!.visibility = View.GONE
        progressAnim!!.startAnim()

        back!!.setOnClickListener {

            stopAttendance(bundle!!,"stop")


        }


        //webView!!.webViewClient = SSLWebViewClient()
        webView!!.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowFileAccess = true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            databaseEnabled = true
            //WebContentsDebuggingEnable(true)

        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView!!.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                Log.d("WebViewConsole", "${msg.message()} [${msg.sourceId()}:${msg.lineNumber()}]")
                return true
            }

        }

        webView!!.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Log.d("abcde", "Page started: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d("abcde", "Page finished: $url")
                progressAnim!!.visibility = View.GONE
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                Log.e("abcde", "Error: ${error?.description}")
                progressAnim!!.visibility = View.GONE
            }
        }

//        val uurl = String.format(url,bundle!!.getString("class","0").toInt())
//
//        Log.d("abcde", "onCreate: "+uurl)

        //webView!!.loadUrl(uurl)

//        webView!!.settings.javaScriptEnabled = true
//        webView!!.settings.domStorageEnabled = true
//
//        webView!!.webViewClient = object : WebViewClient(){
//
//            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//                return super.shouldOverrideUrlLoading(view, url)
//            }
//
//            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//                super.onPageStarted(view, url, favicon)
//                Log.d("abcde", "onPageStarted: ")
//
//
//            }
//
//            override fun onPageFinished(view: WebView?, url: String?) {
//                super.onPageFinished(view, url)
//                Log.d("abcde", "onPageFinished: ")
//
//                view!!.settings.apply {
//
//                    javaScriptEnabled = true
//                    domStorageEnabled = true
//                    cacheMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
//
//                }
//
//                //view.loadUrl( "javascript:window.location.reload( true )" );
//
//                //view.reload()
//
////                Handler().postDelayed({
////
////                    Log.d("abcde", "onPageFinished: ")
////                    view.loadUrl( "javascript:window.location.reload( true )" );
////                                      },3000)
//
//
//                //view.loadUrl(url!!)
//
//                //webView!!.reload()
//            }
//
//            override fun shouldInterceptRequest(
//                view: WebView?,
//                url: String?
//            ): WebResourceResponse? {
//                return super.shouldInterceptRequest(view, url)
//            }
//        }
//
//
        CoroutineScope(Dispatchers.Main).launch{

            delay(10000)

            progressAnim!!.visibility = View.GONE
            back!!.visibility = View.VISIBLE

            //webView!!.webViewClient = WebViewClient()

//            webView!!.settings.apply {
//                javaScriptEnabled = true
//                domStorageEnabled = true
//                databaseEnabled = true
//                cacheMode = WebSettings.LOAD_NO_CACHE
//                loadsImagesAutomatically = true
//                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//                useWideViewPort = true
//                loadWithOverviewMode = true
//            }
//
//            CookieManager.getInstance().apply {
//                setAcceptCookie(true)
//                setAcceptThirdPartyCookies(webView, true)
//            }
//
//            webView!!.clearCache(true)



            val uurl = String.format(url,bundle!!.getString("class","0").toInt())

            Log.d("abcde", "onCreate: "+uurl)

            try {

                webView!!.loadUrl(uurl)

            } catch (ex:Exception) {

                Log.d(AttendanceActivity::class.java.name, "onCreate: ${ex.message}")

                ex.printStackTrace()


            }



//            var j = Intent(Intent.ACTION_VIEW).apply {
//
//                val uurl = String.format(url,bundle!!.getString("class","0").toInt())
//
//                addCategory(Intent.CATEGORY_BROWSABLE)
//                data = Uri.parse(uurl)
//
//            }
//
//            startActivity(j)

        }


    }

    private fun enterFullScreenMode(window: Window) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            window.insetsController?.apply {

                hide(WindowInsetsCompat.Type.statusBars())
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

        } else {


            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
        }



    }

    private fun stopAttendance(bundle: Bundle,type:String) {

        Log.d("abcde", "startAttendance: ")

        OkHttpClientInstance.getInstance(object : OkHttpClientInstance.ResultReceiver{
            override fun onResult(code: Int, response: Response?) {

                response?.let {

                    if (response.isSuccessful && code == 200) {

                        response.body.let {

                            var json: JSONObject = JSONObject(it!!.string())

                            Log.d("abcde", json.getString("status"))

                            if (json.getString("status").equals("started")) {

//                                var intent = Intent(this@ChoiceActivity,AttendanceActivity::class.java)
//                                intent.putExtras(bundle)
//                                startActivity(intent)


                            } else if (json.getString("status").equals("stopped")) {

                                finish()


                            } else {

                                runOnUiThread {

                                    Toast.makeText(this@AttendanceActivity,json.getString("status").capitalize(),
                                        Toast.LENGTH_SHORT).show()
                                }


                            }

                        }
                    } else {

                        runOnUiThread {

                            Toast.makeText(this@AttendanceActivity,"Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }

            override fun onError(code: Int, response: Response?) {

                Log.d("abcde", "onError: "+response?.message)

                runOnUiThread {

                    Toast.makeText(this@AttendanceActivity,"Error:${response?.message}", Toast.LENGTH_SHORT).show()
                }


            }

            override fun onException(error: String?) {

                Log.d("abcde", "Exception: "+error)

                runOnUiThread {

                    Toast.makeText(this@AttendanceActivity,"Exception:${error}", Toast.LENGTH_SHORT).show()
                }

            }


        })

        val m = mutableMapOf<String,String>()
        m.put("class_number",intent!!.extras!!.getString("class",""))

        if (type.equals("start")) {

            OkHttpClientInstance.postJson(OkHttpClientInstance.ATTBASEURL+ OkHttpClientInstance.startAtt,m)

        } else if (type.equals("stop")) {

            OkHttpClientInstance.postJson(OkHttpClientInstance.ATTBASEURL+ OkHttpClientInstance.stopAtt,m)

        }

    }

    override fun onBackPressed() {

        //stopAttendance(bundle!!,"stop")
    }

    class SSLWebViewClient : WebViewClient() {

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            handler?.proceed()
        }
    }
}