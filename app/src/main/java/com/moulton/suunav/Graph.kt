package com.moulton.suunav

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

}
