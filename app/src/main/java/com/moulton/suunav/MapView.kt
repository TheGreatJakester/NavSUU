package com.moulton.suunav

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View


class MapView(context: Context,attr : AttributeSet) : View(context,attr) {

    lateinit var place:Place
    lateinit var img : Bitmap
    private lateinit var screenRect : Rect
    private lateinit var focusRect: Rect
    private var imgRect : Rect

    private var imageManager : RegionManager

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


        imageManager = RegionManager(
            BitmapRegionDecoder.newInstance(
                context.resources.openRawResource( + R.drawable.suu),
                true
            )
        )


    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if(place.cur_location != null) {
            focusRectToLocation()
            //imageManager.drawRegion(canvas!!,focusRect,screenRect,Paint(ANTI_ALIAS_FLAG))
        }
    }
    fun focusRectToLocation():Rect{
        focusRect.set(
            place.get_cur_x() - width / 2, place.get_cur_y() - height / 2,
            place.get_cur_x() + width / 2, place.get_cur_y() + height / 2
        )
        return focusRect
    }
}
