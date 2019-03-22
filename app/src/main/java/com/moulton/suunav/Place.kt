package com.moulton.suunav

import android.location.Location

class Place(
    val graph:Graph,
    val transformation:Matrix
) {
    var cur_location : Location? = null

    fun get_cur_x() : Int{
        cur_location ?: return 0
        return Math.round(
            (transformation * arrayOf(getCoord()))[0][0]
        ).toInt()
    }

    fun get_cur_y() : Int{
        cur_location ?: return 0
        return Math.round(
            (transformation * arrayOf(getCoord()))[0][1]
        ).toInt()
    }

    fun getCoord() : Vector {
        return doubleArrayOf(cur_location?.longitude ?: 0.0,cur_location?.latitude ?: 0.0)
    }

}