package com.moulton.suunav

import android.location.Location
import java.util.*

class Place(
    val graph:Graph,
    val transformation:Matrix
) {
    var cur_location : Location? = null

    fun getCurX() : Int{
        cur_location ?: return 0
        val transform = transformation * arrayOf(getCoord()).transpose()
        return Math.round(transform[0][0]).toInt()
    }

    fun getCurY() : Int{
        cur_location ?: return 0
        val transform = transformation * arrayOf(getCoord()).transpose()
        return Math.round(transform[1][0]).toInt()
    }

    private fun getCoord() : Vector {
        return doubleArrayOf(cur_location?.longitude ?: 0.0,cur_location?.latitude ?: 0.0 , 1.0)
    }

    private fun getRoute(p1 : Point , p2 : Point):List<Point>{
    //simple breadth first search
        //define some tools
        class PointInRoute(val point : Point) : Comparable<PointInRoute>{
            var isRoot = false
            var isEnd = false

            var fromPoint : PointInRoute? = null
            set(p){
                if(p == null){
                    field = p
                } else if(point.isConectedTo(p.point)){
                    field = p
                } else { //Try real hard not to hit this line of code.
                    field = null
                }
            }

            val curDistance : Float = Float.MAX_VALUE
            get(){
                if(isRoot){
                    return 0f
                } else if(fromPoint != null){
                    return fromPoint!!.curDistance + fromPoint!!.point.edges[point]!!
                } else{
                    return field
                }
            }

            fun goToPoint(p:PointInRoute):Boolean{
                if(point.isConectedTo(p.point)){
                    //we know that point.edges contains p.point because they are connected
                    if(p.curDistance > curDistance + point.edges[p.point]!!){
                        p.fromPoint = this
                        return true
                    }
                }
                return false
            }

            override fun compareTo(other: PointInRoute): Int {
                if(this.curDistance == other.curDistance){
                    return 0
                } else if(this.curDistance > other.curDistance){
                    return 1
                } else{
                    return -1
                }
            }
        }
        fun path(endPoint : PointInRoute) : List<Point>{
            var previousPoint = endPoint.fromPoint
            val out = mutableListOf<Point>()
            while(previousPoint != null){
                if(previousPoint.isRoot){
                    out.add(previousPoint.point)
                    return out
                } else{
                    out.add(previousPoint.point)
                    previousPoint = previousPoint.fromPoint
                }
            }
            return listOf() //maybe validate the graph to make sure that it is a connected one. This way, this line of code is never run.

        }
        //build some collections
        val pointInRouteQueue = PriorityQueue<PointInRoute>()
        val pointInRouteMap = mutableMapOf<Point,PointInRoute>()
        for(p in this.graph.points){
            pointInRouteMap[p] = PointInRoute(p)
        }
        //set up goals
        pointInRouteMap[p1]!!.isRoot = true
        pointInRouteMap[p2]!!.isEnd = true
        //use collection
        pointInRouteQueue.addAll(pointInRouteMap.values)

        //start looking
        while(!pointInRouteQueue.isEmpty()){
            //look at the point with the shortest distance
            val currentPoint : PointInRoute = pointInRouteQueue.poll()
            //look at all the points they are connected too.
            for(connectedPoint in currentPoint.point.edges.keys){
                val nextPoint = pointInRouteMap[connectedPoint]!!
                currentPoint.goToPoint(nextPoint)
                //if we find the end, return the path too it
                if(nextPoint.isEnd){
                    return path(nextPoint)
                }
            }
        }
        // You should never hit this line, but so the compiler is happy...
        return listOf()
    }

}