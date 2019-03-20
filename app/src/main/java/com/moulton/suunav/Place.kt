package com.moulton.suunav

import android.location.Location

class Place(
    val graph:Graph,
    val x_scale : Double,
    val y_scale : Double,
    val x_offset : Double,
    val y_offset : Double
) {
    var cur_location : Location? = null

    fun get_cur_x() : Int{
        cur_location ?: return 0
        return longToX(cur_location!!.longitude)
    }
    fun get_cur_y() : Int{
        cur_location ?: return 0
        return latToY(cur_location!!.latitude)
    }

    fun xToLong(x:Int) : Double{
        return x*x_scale + x_offset
    }
    fun yToLat(y:Int) : Double{
        return y*y_scale + y_offset

    }
    fun longToX(long:Double): Int{
        return Math.round( (long - x_offset) / x_scale ).toInt()
    }
    fun latToY(lat : Double): Int{
        return Math.round( (lat - y_offset) / y_scale ).toInt()
    }


}