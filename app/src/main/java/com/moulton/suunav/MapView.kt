package com.moulton.suunav

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller


class MapView(context: Context,attr : AttributeSet) : View(context,attr) {
    lateinit var place:Place
    private lateinit var screenRect : Rect
    private lateinit var focusRect: Rect
    private var imgRect : Rect
    lateinit var imageManager : RegionManager
    val imagePaint = Paint(ANTI_ALIAS_FLAG)

    init{
        viewTreeObserver.addOnGlobalLayoutListener {
            screenRect = Rect(0,0,width,height)
            focusRect = Rect(0,0,width,height)
        }
        val sizeOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(context.resources,R.drawable.suu,sizeOptions)
        imgRect = Rect(0,0,sizeOptions.outWidth,sizeOptions.outHeight)
    }

    //input handlers
    //private val scroller = Scroller(context,null,true)
    private val detector : GestureDetector = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener(){
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }
            //TODO : Implement fling
            /*override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                val SCALE = 8
                scroller.fling(
                    focusRect.right,focusRect.top,
                    (velocityX/SCALE).toInt(),(velocityY/SCALE).toInt(),
                    imgRect.right, imgRect.left - screenRect.left,
                    imgRect.top, imgRect.bottom - screenRect.bottom
                )
                postInvalidate()
                return true
            }*/

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
        /*scroller.apply{
            if(!isFinished){
                computeScrollOffset()
                focusRect.offsetTo(currX,currY)
            }
        }*/
        imageManager.drawRegion(canvas!!,focusRect,screenRect,imagePaint)
    }
    fun focusRectToLocation():Rect{
        focusRect.set(
            place.get_cur_x() - width / 2, place.get_cur_y() - height / 2,
            place.get_cur_x() + width / 2, place.get_cur_y() + height / 2
        )
        return focusRect
    }
}
