package com.moulton.suunav

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View



class MapView(context: Context,attr : AttributeSet) : View(context,attr) {
    private val imagePaint = Paint(ANTI_ALIAS_FLAG)
    private val pathPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 2f
    }
    private val routePaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 6f
    }
    private val locationPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val pointTextPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        textSize = 32f
    }

    lateinit var place:Place
    lateinit var mapImage : Bitmap
    var route : List<Point>? = null
    private lateinit var screenRect : Rect
    private var focusRect : Rect = Rect(0,0,0,0)

    fun bounded(low: Int, value : Int, high:Int):Int{
        if(value < low){
            return low
        } else if(value > high){
            return high
        } else{
            return value
        }
    }

    fun setFocusRect(left:Int,top:Int,right:Int,bottom:Int) {
        focusRect.left = bounded(0,left,mapImage.width)
        focusRect.top = bounded(0,top,mapImage.height)
        focusRect.right = bounded(0,right,mapImage.width)
        focusRect.bottom = bounded(0,bottom,mapImage.height)
    }
    fun offSetFocusRect(dx:Int,dy:Int){
        val adjustedDx : Int = bounded(-focusRect.left,dx,mapImage.width - focusRect.right)
        val adjustedDy : Int = bounded(-focusRect.top,dy,mapImage.height - focusRect.bottom)
        focusRect.offset(adjustedDx,adjustedDy)
    }

    init{
        viewTreeObserver.addOnGlobalLayoutListener {
            screenRect = Rect(0,0,width,height)
            focusRect = Rect(0,0,width,height)
        }
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
        } else if(event.action == MotionEvent.ACTION_MOVE) {
            if (event.historySize > 0) {
                if(event.pointerCount >= 2){
                    //start zoom and move

                    //detect how much zoom has happened
                    val initialDistance = Math.pow(
                            Math.pow(event.getHistoricalX(0,0).toDouble()-event.getHistoricalX(1,0).toDouble(),2.0)+
                            Math.pow(event.getHistoricalY(0,0).toDouble()-event.getHistoricalY(1,0).toDouble(),2.0)
                        ,.5)
                    val currentDistance = Math.pow(
                            Math.pow(event.getHistoricalX(0,event.historySize - 1).toDouble()-event.getHistoricalX(1,event.historySize -1).toDouble(),2.0)+
                            Math.pow(event.getHistoricalY(0,event.historySize - 1).toDouble()-event.getHistoricalY(1,event.historySize - 1).toDouble(),2.0)
                        ,.5)

                    val deltaDistance = initialDistance - currentDistance

                    val initialCenterX = event.getHistoricalX(0,0) + event.getHistoricalX(1,0) / 2
                    val initialCenterY = event.getHistoricalY(0,0) + event.getHistoricalY(1,0) / 2

                    val currentCenterX = event.getHistoricalX(0,event.historySize - 1) + event.getHistoricalX(1,event.historySize - 1) / 2
                    val currentCenterY = event.getHistoricalY(0,event.historySize - 1) + event.getHistoricalY(1,event.historySize - 1) / 2

                    val deltaX = initialCenterX - currentCenterX
                    val deltaY = initialCenterY - currentCenterY



                } else {
                    val deltaX = event.getHistoricalX(0) - event.x
                    val deltaY = event.getHistoricalY(0) - event.y
                    offSetFocusRect(deltaX.toInt(), deltaY.toInt())
                    invalidate()
                }
                
            }
            return true
        } else {
            return false
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas!!)
        drawLocation(canvas)
        canvas.drawBitmap(mapImage,focusRect,screenRect,imagePaint)
        if(route != null){
            drawRoute(canvas,route!!)
        }
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

    private fun drawPointText(canvas: Canvas){
        for(point in place.graph.points){
            if(focusRect.contains(point.x,point.y)){
                canvas.drawText(
                    point.Id.toString(),
                    (point.x - focusRect.left).toFloat(),
                    (point.y - focusRect.top).toFloat(),
                    pointTextPaint
                )
            }
        }
    }

    private fun drawRoute(canvas: Canvas, route: List<Point>){
        for(pointindex in 0..(route.size-2)){
            val point = route[pointindex]
            val nextPoint = route[pointindex+1]
            if (focusRect.contains(point.x,point.y) || focusRect.contains(nextPoint.x,nextPoint.y)) {
                canvas.drawLine(
                    (point.x - focusRect.left).toFloat(),
                    (point.y - focusRect.top).toFloat(),
                    (nextPoint.x - focusRect.left).toFloat(),
                    (nextPoint.y - focusRect.top).toFloat(),
                    routePaint
                )
            }
        }
    }

    private fun drawLocation(canvas: Canvas){
        canvas.drawCircle((place.getCurX()-focusRect.left).toFloat(),(place.getCurY()-focusRect.top).toFloat(),15f,locationPaint)
    }

    fun focusRectToLocation():Rect{
        focusRect.set(
            place.getCurX() - width / 2, place.getCurY() - height / 2,
            place.getCurX() + width / 2, place.getCurY() + height / 2
        )
        return focusRect
    }
}
