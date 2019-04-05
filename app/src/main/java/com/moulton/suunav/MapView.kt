package com.moulton.suunav

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View



class MapView(context: Context,attr : AttributeSet) : View(context,attr) {
    lateinit var place:Place
    private lateinit var screenRect : Rect
    private lateinit var focusRect: Rect
    lateinit var imageManager : RegionManager
    private val imagePaint = Paint(ANTI_ALIAS_FLAG)
    private val pathPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 2f
    }

    init{
        viewTreeObserver.addOnGlobalLayoutListener {
            screenRect = Rect(0,0,width,height)
            imageManager.nativeScreen = screenRect
            focusRect = Rect(0,0,width,height)
        }
        val sizeOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(context.resources,R.drawable.suu,sizeOptions)
    }


    private val detector : GestureDetector = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener(){
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }
        }
    )

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(detector.onTouchEvent(event)){
            return true
        } else if(event.action == MotionEvent.ACTION_MOVE){
            if(event.historySize > 0) {
                val deltaX = event.getHistoricalX(0) - event.x
                val deltaY = event.getHistoricalY(0) - event.y
                focusRect.offset(deltaX.toInt(), deltaY.toInt())
                invalidate()
            }
            return true
        } else {
            return false
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        imageManager.drawRegion(canvas!!,focusRect,imagePaint)
        drawPaths(canvas!!)
    }
    private fun drawPaths(canvas: Canvas){
        for(point in place.graph.points){
            if(focusRect.contains(point.x,point.y)){
                for(subPoint in point.edges.keys){
                    canvas.drawLine(
                        (point.x - focusRect.left).toFloat(),
                        (point.y - focusRect.top).toFloat(),
                        (subPoint.x - focusRect.left).toFloat(),
                        (subPoint.y - focusRect.top).toFloat(),
                        pathPaint
                    )
                }
            }
        }

    }

    fun focusRectToLocation():Rect{
        focusRect.set(
            place.getCurX() - width / 2, place.getCurY() - height / 2,
            place.getCurX() + width / 2, place.getCurY() + height / 2
        )
        return focusRect
    }
}
