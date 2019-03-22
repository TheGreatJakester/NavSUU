package com.moulton.suunav

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.view.Display



class MapView(context: Context,attr : AttributeSet) : View(context,attr) {

    lateinit var place:Place
    lateinit var img : Bitmap
    var imgHeight : Int = -1
    var imgWidth : Int = -1


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if(place.cur_location != null) {
            img = getImage(
                Rect(
                    place.get_cur_x() - width / 2, place.get_cur_y() - height / 2,
                    place.get_cur_x() + width / 2, place.get_cur_y() + height / 2
                )
            )
            canvas?.drawBitmap(
                img, null,
                Rect(0, 0, width, height), Paint(ANTI_ALIAS_FLAG)
            )
        }
    }
    fun getImage(rect : Rect):Bitmap{
        //make sure that we have some information about the image
        if(imgHeight == -1 || imgWidth == -1){
            setDimensions()
        }
        if(rect.left < 0){
            rect.left = 0
        }
        if (rect.top < 0){
            rect.top = 0
        }
        if(rect.right >= imgWidth){
            rect.right = imgWidth
        }
        if(rect.bottom >= imgHeight){
            rect.bottom = imgHeight
        }

        val decoder = BitmapRegionDecoder.newInstance(
            context.resources.openRawResource( + R.drawable.suu),
            false
        )
        return decoder.decodeRegion(rect,BitmapFactory.Options().apply {
            outWidth = width
            outHeight = height
        })
    }
    fun setDimensions(){
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(context.resources,R.drawable.suu,options)
        imgHeight = options.outHeight
        imgWidth = options.outWidth
    }


}
