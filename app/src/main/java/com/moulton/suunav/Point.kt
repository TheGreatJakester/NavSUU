package com.moulton.suunav


data class Edge(
    var destination: Point,
    var distance: Double
)

open class Point(var Id :Int, var x:Int, var y: Int, var name: String?){
    constructor (p:Point):this(p.Id,p.x,p.y,p.name){
        this.edges = p.edges
    }

    var edges = HashMap<Point,Float>()

    fun addEdge(p :Point){
        //its like... bad dynamic programming
        edges[p] = dist(p)
    }

    fun isConectedTo(p:Point) : Boolean {
        return this.edges.containsKey(p)
    }

    fun dist(p :Point): Float{
        return Math.pow(
            Math.pow((x-p.x).toDouble(),2.0) +
                    Math.pow((y-p.y).toDouble(),2.0)
            ,.5).toFloat()
    }
}
