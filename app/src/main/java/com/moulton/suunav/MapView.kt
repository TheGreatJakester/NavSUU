package com.moulton.suunav

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.view.Display
import android.view.ViewTreeObserver


class MapView(context: Context,attr : AttributeSet) : View(context,attr) {

    lateinit var screenRect : Rect
    lateinit var place:Place
    lateinit var img : Bitmap

    val focusRect = Rect()
    val imgPaint = Paint(ANTI_ALIAS_FLAG)

    init{
        viewTreeObserver.addOnGlobalLayoutListener {
            screenRect = Rect(0,0,width,height)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setDimensions()
        if(place.cur_location != null) {
            updateFocusRect()
            img = getImage(focusRect)
            canvas?.drawBitmap(
                img, null,
                screenRect, imgPaint
            )
        }
    }
    fun getImage(rect : Rect):Bitmap{
        val decoder = BitmapRegionDecoder.newInstance(
            context.resources.openRawResource( + R.drawable.suu),
            false
        )
        return decoder.decodeRegion(rect,BitmapFactory.Options().apply {
            outWidth = width
            outHeight = height
        })
    }
    fun updateFocusRect(){
        focusRect.apply {
            left = place.get_cur_x() - width / 2
            top = place.get_cur_y() - height / 2
            right = place.get_cur_x() + width / 2
            bottom = place.get_cur_y() + height / 2

        }
    }
    fun setDimensions(){
        /*
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(context.resources,R.drawable.suu,options)
        imgHeight = options.outHeight
        imgWidth = options.outWidth
        */




        screenRect.apply {
            top = 0
            left = 0
            right = width
            bottom = height
        }
    }
}
