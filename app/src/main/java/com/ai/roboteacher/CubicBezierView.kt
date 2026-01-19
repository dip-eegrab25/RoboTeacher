package com.ai.roboteacher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View

class CubicBezierView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 4 control-points (could be exposed via setters or XML attrs)
    private val p0 = PointF(100f, 600f)   // start
    private val p1 = PointF(300f, 100f)   // control-1
    private val p2 = PointF(500f, 1000f)  // control-2
    private val p3 = PointF(700f, 600f)   // end

    private val curvePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }
    private val helperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw helper lines so you can see control handles
        canvas.drawLine(p0.x, p0.y, p1.x, p1.y, helperPaint)
        canvas.drawLine(p2.x, p2.y, p3.x, p3.y, helperPaint)


        // Construct and draw Bézier
        val path = Path().apply {
            moveTo(p0.x, p0.y)
            //cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)

            quadTo(p1.x, 300f, p3.x, p3.y)
        }
        canvas.drawPath(path, curvePaint)
    }
}
