package com.moulton.suunav


data class Edge(
    var destination: Point,
    var distance: Double
)

class Point(var Id :Int, var x:Int, var y: Int, var name: String?){

    var edges : MutableList<Edge> = mutableListOf()
    fun addEdge(p :Point){
        edges.add(
            Edge(p, dist(p))
        )
    }
    private fun dist(p :Point): Double{
        return Math.pow(
            Math.pow((x-p.x).toDouble(),2.0) +
                    Math.pow((y-p.y).toDouble(),2.0)
            ,.5)
    }
}
