package com.moulton.suunav

import java.util.*

class Graph {
    var points = mutableListOf<Point>()
    fun addEdgeById(p1Id : Int, p2Id : Int){
        var p1 = getPointbyId(p1Id)
        var p2 = getPointbyId(p2Id)
        if (p1 != null && p2 != null){
            addEdge(p1,p2)
        }
    }
    fun addEdge(p1 : Point, p2 : Point){
        p1.addEdge(p2)
        p2.addEdge(p1)
    }

    fun getPointbyId(id : Int) : Point?{
        var out : Point? = null
        points.forEach {
            if(it.Id == id){
                out = it
            }
        }
        return out
    }

    fun getPath(startPoints:List<Point>,endPoints : List<Point>):List<Point>{
        class LinkedPoint(var orgin :Point) : Point(orgin){
            var previousPointInPath : LinkedPoint? = null
            var isStartPoint = false
            var isEndPoint = false
            var isVisited = false
            fun getPathLength():Float{
                if(isStartPoint){
                    return 0f
                }
                else if(previousPointInPath != null){
                    return previousPointInPath!!.getPathLength() + previousPointInPath!!.edges[this.orgin]!!
                } else{
                    return Float.MAX_VALUE
                }
            }
            fun connectIfShorter(possibleNextPoint : LinkedPoint){
                if(getPathLength() + this.edges[possibleNextPoint.orgin]!! < possibleNextPoint.getPathLength()){
                    possibleNextPoint.previousPointInPath = this
                }
            }
            private fun pathToHereFromStart(endPoint: LinkedPoint,pathSoFar:MutableList<LinkedPoint>):List<LinkedPoint>{
                pathSoFar.add(endPoint)
                if(endPoint.isStartPoint)return pathSoFar
                else if(endPoint.previousPointInPath != null)return pathToHereFromStart(endPoint.previousPointInPath!!,pathSoFar)
                else return pathSoFar


            }
            fun pathToHereFromStart():List<LinkedPoint>{
                if(isVisited) {
                    return pathToHereFromStart(this, mutableListOf())
                } else {
                    return listOf()
                }
            }
        }

        val linkedPoints = hashMapOf<Point,LinkedPoint>()
        points.forEach {
            linkedPoints[it] = LinkedPoint(it)
        }
        startPoints.forEach{
            linkedPoints[it]?.isStartPoint = true
        }
        endPoints.forEach {
            linkedPoints[it]?.isEndPoint = true
        }
        val toSearch = ArrayDeque<LinkedPoint>(startPoints.map{linkedPoints[it]})
        var curPoint : LinkedPoint = toSearch.peek()
        while(!toSearch.isEmpty()){
            curPoint = toSearch.poll()
            curPoint.isVisited = true
            if(curPoint.isEndPoint){
                break //curPoint is now the end point
            }
            for(possibleNextPoint in curPoint.edges.keys){
                val linkedPossibleNextPoint = linkedPoints[possibleNextPoint]!!
                curPoint.connectIfShorter(linkedPossibleNextPoint)
                if(!linkedPossibleNextPoint.isVisited) toSearch.add(linkedPossibleNextPoint)

            }
        }
        return curPoint.pathToHereFromStart()
    }

}
