package com.ai.roboteacher.Notification

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.setPadding
import com.ai.roboteacher.R
import com.ai.roboteacher.Utils


class SampleNotification {

    var windowManager:WindowManager?=null
    var lp:WindowManager.LayoutParams?=null
    var view:View?=null
    var initialTouchX = 0f
    var X = 0f




    constructor(c:Context,query:String,notificationClickListener: NotificationClickListener) {

        //this.notificationClickListener = notificationClickListener

        windowManager = c.getSystemService(WINDOW_SERVICE) as WindowManager

        val px = (Utils.getScreenWidth(c)*0.40f).toInt()

        val width = Utils.getScreenWidth(c)*0.30f
        var startXBound = Utils.getScreenWidth(c)*0.20f
        var endXBound = Utils.getScreenWidth(c)*0.80f

        view = LayoutInflater.from(c).inflate(R.layout.layout_notif , null)
        val txtQuery:TextView = view!!.findViewById(R.id.txt_query)
        txtQuery.text = query
        //view!!.setPadding(10)

        lp = WindowManager.LayoutParams(
            width.toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        lp!!.gravity = Gravity.TOP or Gravity.LEFT
        lp!!.x = px
        lp!!.y = 10

        windowManager!!.addView(view , lp)


        view!!.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                when(event!!.action) {

                    MotionEvent.ACTION_DOWN->{

                        X = lp!!.x.toFloat()

                        initialTouchX = event.rawX

                        return true
                    }

                    MotionEvent.ACTION_MOVE->{

                        lp!!.x = (X+(event.rawX-initialTouchX)).toInt()

                       windowManager!!.updateViewLayout(view,lp)

                        return true
                    }

                    MotionEvent.ACTION_UP->{

                        if (lp!!.x>endXBound || lp!!.x<startXBound) {

                            //lp!!.x = Utils.getScreenHeight(c)+10
                            windowManager!!.removeView(view)

                        } else {

                            lp!!.x = px
                            windowManager!!.updateViewLayout(view,lp)
                        }

                        if (event.rawX == initialTouchX) {

                            windowManager!!.removeView(view)

                            notificationClickListener.onNotificationClicked()

                            Log.d(SampleNotification::class.java.name, "onTouch: ")
                        }

                        return true
                    }

                    else->{

                        return false
                    }


                }


            }


        })

    }



    interface NotificationClickListener{

        fun onNotificationClicked()
    }
}