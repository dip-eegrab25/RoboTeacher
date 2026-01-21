package com.ai.roboteacher

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.View
import android.view.WindowManager

class FakeShutdownOverlay(private val context: Context) {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    fun show() {
        if (overlayView != null) return  // already shown

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        overlayView = View(context).apply {
            setBackgroundColor(Color.BLACK)
            isClickable = true
            isFocusable = true
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager?.addView(overlayView, params)

//        overlayView!!.setOnClickListener {
//
//            hide()
//
//        }
    }

    fun hide() {
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
    }
}
