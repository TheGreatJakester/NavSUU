package com.moulton.suunav

import android.graphics.*
import java.lang.RuntimeException

class RegionManager(var decoder : BitmapRegionDecoder) {
    val MARGIN = 1.5
    private var bufferedRegion = Rect()
    var bufferedImage : Bitmap? = null
    var imageSize = Rect()
    init {
        val sizeOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        decoder.decodeRegion(Rect(),sizeOptions)
        imageSize.set(0,0,sizeOptions.outWidth,sizeOptions.outHeight)
    }
    //region is the part of the total image the view (typicaly) wants
    var region : Rect
        get(){
            return this.region
        }
        set(region : Rect){
            if(imageSize.contains(region)){
                this.region = region
                this.regionSize.set(0,0,region.width(),region.height())
                if(!bufferedRegion.contains(region)){
                    loadBuffer()
                }
                this.regionOffset.apply {
                    set(region)
                    offset(-bufferedRegion.left,-bufferedRegion.top)
                }
            } else {
                throw RuntimeException("region ( "+region.toString()+" )  isn't inside image decoder" + decoder.toString())
            }
        }

    //keeps the size of region
    private var regionSize = Rect()
    //where the region is relative to the buffered region
    private var regionOffset = Rect()

    private fun loadBuffer(){
        bufferedRegion.set(
            Math.round(region.centerX() - region.width()*.5*MARGIN).toInt(),
            Math.round(region.centerY() - region.height()*.5*MARGIN).toInt(),
            Math.round(region.centerX() + region.width()*.5*MARGIN).toInt(),
            Math.round(region.centerY() + region.height()*.5*MARGIN).toInt()
        )
        bufferedImage = decoder.decodeRegion(bufferedRegion,BitmapFactory.Options())
    }

    fun drawRegion(canvas: Canvas,region:Rect, destination: Rect,paint: Paint){
        this.region.set(region)
        canvas.drawBitmap(
            bufferedImage,
            regionOffset,
            destination,
            paint)
    }
}