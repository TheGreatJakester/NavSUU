package com.moulton.suunav

import android.graphics.*
import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicBoolean

class RegionManager(var decoder : BitmapRegionDecoder) {
    val MARGIN = 1.5

    private var bufferedRegion = Rect()
    var bufferedImage : Bitmap? = null


    var imageSize = Rect()
    init {
        imageSize.set(0,0,decoder.width,decoder.width)
    }
    //region is the part of the total image the view (typicaly) wants
    var region : Rect = Rect()
        set(region){
            if(imageSize.contains(region)){
                field = region
                if(!bufferedRegion.contains(field)){
                    loadBuffer()
                }
            } else {
                //if the region will fit in the image, move it onto the image.
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

    //where the region is relative to the buffered region
    private var regionOnBuffer = Rect()
    get(){
        field.set(region)
        field.offset(-bufferedRegion.left,-bufferedRegion.top)
        return field
    }


    interface OnBufferChangeListener{
        fun onBufferChange()
    }
    var onBufferChange : OnBufferChangeListener? = null
    fun setOnBufferChange(passedLamda:()->Unit){
        this.onBufferChange = object : OnBufferChangeListener{
            override fun onBufferChange() {
                passedLamda()
            }
        }
    }

    //decalare locks
    private var loading = AtomicBoolean(false)
    private var settingBufferLock = AtomicBoolean(false)
    //use locks
    private fun loadBuffer(){
        if(loading.compareAndSet(false,true)) {
            Thread(Runnable {
                val newBufferedRegion = Rect(
                    Math.round(region.centerX() - region.width() * .5 * MARGIN).toInt(),
                    Math.round(region.centerY() - region.height() * .5 * MARGIN).toInt(),
                    Math.round(region.centerX() + region.width() * .5 * MARGIN).toInt(),
                    Math.round(region.centerY() + region.height() * .5 * MARGIN).toInt()
                )

                val newbufferedImage = decoder.decodeRegion(newBufferedRegion, BitmapFactory.Options())
                while (!settingBufferLock.compareAndSet(false, true)) {
                }//acquire lock
                //critical code
                bufferedRegion = newBufferedRegion
                bufferedImage = newbufferedImage
                settingBufferLock.set(false)//release lock
                loading.set(false)//release lock
                //once done, perform something.
                onBufferChange?.onBufferChange()
            }).start()
        }
    }

    fun drawRegion(canvas: Canvas,region:Rect, destination: Rect,paint: Paint){
        while(!settingBufferLock.compareAndSet(false,true)){} // acquire lock
        this.region = region
        if(bufferedImage != null) {
            canvas.drawBitmap(
                bufferedImage,
                regionOnBuffer,
                destination,
                paint
            )
        }
        settingBufferLock.set(false) // release lock
    }
}