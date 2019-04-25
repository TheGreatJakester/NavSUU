package com.moulton.suunav

import android.location.Location
import java.util.*

class Place(
    val graph:Graph,
    val transformation:Matrix
) {
    var curLocation: Location? = null

    var nearestPoint: Point = Point(-1,-1,-1,null)
    get(){
        var out : Point = field
        var curPoint = Point(-1,getCurX(),getCurY(),null)
        var curDistance : Float = Float.MAX_VALUE
        for(possibleNearest in graph.points){
            if(possibleNearest.dist(curPoint) < curDistance){
                out = possibleNearest
                curDistance = possibleNearest.dist(curPoint)
            }
        }
        return out
    }

    fun getCurX() : Int{
        curLocation ?: return 0
        val transform = transformation * arrayOf(getCoord()).transpose()
        return Math.round(transform[0][0]).toInt()
    }

    fun getCurY() : Int{
        curLocation ?: return 0
        val transform = transformation * arrayOf(getCoord()).transpose()
        return Math.round(transform[1][0]).toInt()
    }

    private fun getCoord() : Vector {
        return doubleArrayOf(curLocation?.longitude ?: 0.0,curLocation?.latitude ?: 0.0 , 1.0)
    }

    fun getRoute(p1 : Point , p2 : Point):List<Point>{
        return graph.getPath(listOf(p1), listOf(p2))
    }

}