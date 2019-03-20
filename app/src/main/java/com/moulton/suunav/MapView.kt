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


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        img = getImage(Rect(1000,200,1000+width,200+height))
        canvas?.drawBitmap(img,null,
            Rect(0,0,width,height),Paint(ANTI_ALIAS_FLAG))
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


}
