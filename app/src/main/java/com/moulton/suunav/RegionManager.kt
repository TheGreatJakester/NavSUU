package com.moulton.suunav

import android.graphics.*
import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicBoolean

class RegionManager(var decoder : BitmapRegionDecoder,val nativeScreen:Rect) {
    private val MARGIN = 1.5
    val BUFFERING_NOW_LOCK = AtomicBoolean(false)
    val SETTING_BUFFER_LOCK = AtomicBoolean(false)



    private var bufferedRegion = Rect()
    private var bufferedImage : Bitmap? = null
    private var bufferCompairedToImage = Pair(1f,1f)
    
    //This rect represents where the region is on the buffer. It needs to be updated if the region or buffer changes.
    private var bufferRegionMapping = Rect()
    private fun updateBufferRegionMapping(){
        bufferRegionMapping.set(
            Math.round((region.left - bufferedRegion.left)*bufferCompairedToImage.first),
            Math.round((region.top - bufferedRegion.top)*bufferCompairedToImage.second),
            Math.round((bufferedRegion.right - region.right)*bufferCompairedToImage.first),
            Math.round((bufferedRegion.bottom - region.bottom)*bufferCompairedToImage.second)
        )
    }
    var imageSize = Rect(0,0,decoder.width,decoder.height)

    //region is the part of the total image the view wants
    var region : Rect = Rect()
        set(region){
            //make sure that the image actualy has the region. If it does, set the region.
            if(imageSize.contains(region)){
                field = region
                updateBufferRegionMapping()
                //if even part of the region is outside the buffer, reload the buffer.
                if(!bufferedRegion.contains(field)){
                    loadBuffer()
                }
            } else {
                if (region.width() < imageSize.width() && region.height() < imageSize.height()) {
                    if(region.top < imageSize.top){
                        region.offset(0,imageSize.top - region.top)
                    } else if(region.bottom > imageSize.bottom){
                        region.offset(0,imageSize.bottom - region.bottom)
                    }

                    if(region.left < imageSize.left){
                        region.offset(imageSize.left - region.left , 0)
                    } else if(region.right > imageSize.right){
                        region.offset(imageSize.right - region.right,0)
                    }
                    //recursive
                    this.region = region
                } else {
                    throw RuntimeException("region ( " + region.flattenToString() + " ) doesn't fit inside image decoder" + decoder.toString())
                }
            }
        }

    private fun loadBuffer(){
        if(BUFFERING_NOW_LOCK.compareAndSet(false,true)){
            //start the buffer
        }
        //you ready for some multi threaded goodness?
        bufferedRegion.set(
            Math.round(region.centerX() - region.width()*.5*MARGIN).toInt(),
            Math.round(region.centerY() - region.height()*.5*MARGIN).toInt(),
            Math.round(region.centerX() + region.width()*.5*MARGIN).toInt(),
            Math.round(region.centerY() + region.height()*.5*MARGIN).toInt()
        )
        bufferedImage = decoder.decodeRegion(bufferedRegion,BitmapFactory.Options())
    }

    val loadBuffer = Runnable {
        if(BUFFERING_NOW_LOCK.compareAndSet(false,true)){
            var newBufferedRegion = Rect(
                Math.round(region.centerX() - region.width()*.5*MARGIN).toInt(),
                Math.round(region.centerY() - region.height()*.5*MARGIN).toInt(),
                Math.round(region.centerX() + region.width()*.5*MARGIN).toInt(),
                Math.round(region.centerY() + region.height()*.5*MARGIN).toInt()
            )
            val bufferOptions = BitmapFactory.Options().apply {
                outWidth = newBufferedRegion.width()
                outHeight = newBufferedRegion.height()
            }
        }
    }

    fun drawRegion(canvas: Canvas,region:Rect, paint: Paint){
        this.region = region
        canvas.drawBitmap(
            bufferedImage,
            bufferRegionMapping,
            nativeScreen,
            paint)
    }
}