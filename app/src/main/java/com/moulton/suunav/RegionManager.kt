package com.moulton.suunav

import android.graphics.*
import java.lang.RuntimeException

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
                this.regionSize.set(0,0,field.width(),field.height())
                if(!bufferedRegion.contains(field)){
                    loadBuffer()
                }
                this.regionOffset.apply {
                    set(field)
                    offset(-bufferedRegion.left,-bufferedRegion.top)
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
        this.region = region
        canvas.drawBitmap(
            bufferedImage,
            regionOffset,
            destination,
            paint)
    }
}