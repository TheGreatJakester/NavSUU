package com.moulton.suunav

import android.graphics.*
import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicBoolean

class RegionManager(var decoder : BitmapRegionDecoder,var nativeScreen:Rect) {
    private val MARGIN = 1.5
    private val settingBuffer = AtomicBoolean(false)

    private var bufferedRegion = Rect()
    set(r:Rect){
        if(!r.equals(field)){
            isMappingDirty = true
        }
        field = r
    }
    private var bufferedImage : Bitmap? = null
    private var bufferCompairedToImage = Pair(1f,1f)

    //This rect represents where the region is on the buffer. It needs to be updated if the region or buffer changes.
    private var isMappingDirty = true
    private var bufferRegionMapping = Rect()
    get(){
        if(isMappingDirty){
            field.set(
                Math.round((region.left - bufferedRegion.left)*bufferCompairedToImage.first),
                Math.round((region.top - bufferedRegion.top)*bufferCompairedToImage.second),
                Math.round((bufferedRegion.right - region.right)*bufferCompairedToImage.first),
                Math.round((bufferedRegion.bottom - region.bottom)*bufferCompairedToImage.second)
            )
            isMappingDirty = false
        }
        return field
    }

    var imageSize = Rect(0,0,decoder.width,decoder.height)
    //region is the part of the total image the view wants
    var region : Rect = Rect()
        set(region){
            //make sure that the image actualy has the region. If it does, set the region.
            if(imageSize.contains(region)){
                field = region
                isMappingDirty = true
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
        Thread(loadBuffer).start()
    }

    val loadBuffer = Runnable {
        val newBufferedRegion = Rect(
                Math.round(region.centerX() - region.width()*.5*MARGIN).toInt(),
                Math.round(region.centerY() - region.height()*.5*MARGIN).toInt(),
                Math.round(region.centerX() + region.width()*.5*MARGIN).toInt(),
                Math.round(region.centerY() + region.height()*.5*MARGIN).toInt()
        )
        var newBufferCompairedToImage : Pair<Float,Float>
        val bufferOptions = BitmapFactory.Options().apply {
            if(region.width() > nativeScreen.width() || region.height() > region.height()){
                //region is too big, scale down.
                outWidth = Math.round(nativeScreen.width() * MARGIN).toInt()
                outHeight = Math.round(nativeScreen.height() * MARGIN).toInt()
                newBufferCompairedToImage = Pair(
                    outWidth.toFloat() / newBufferedRegion.width(),
                    outHeight.toFloat() / newBufferedRegion.height()
                )
            } else {
                //region is small enough, go ahead and load in all the buffer it wants
                outWidth = newBufferedRegion.width()
                outHeight = newBufferedRegion.height()
                newBufferCompairedToImage = Pair(1f,1f)
            }
        }
        val newBitmap = decoder.decodeRegion(newBufferedRegion,bufferOptions)
        while(!settingBuffer.compareAndSet(false,true)){}//spin lock
            this.bufferedImage = newBitmap
            this.bufferedRegion = newBufferedRegion
            this.bufferCompairedToImage = newBufferCompairedToImage
        settingBuffer.set(false) //release lock
    }

    fun drawRegion(canvas: Canvas,region:Rect, paint: Paint){
        while(!settingBuffer.compareAndSet(false,true)){}//spin lock
        this.region = region
        if(bufferedImage != null) {
            canvas.drawBitmap(
                bufferedImage,
                bufferRegionMapping,
                nativeScreen,
                paint
            )
        }
        settingBuffer.set(false) //release lock
    }
}