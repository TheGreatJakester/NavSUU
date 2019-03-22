package com.moulton.suunav

import android.content.Context
import org.xmlpull.v1.XmlPullParser

class PlaceParser(val c : Context) {
    fun parse(pointId : Int, pathId: Int) : Place{
        var graph = Graph()
        val pointParser = c.resources.getXml(pointId)
        val pathParser = c.resources.getXml(pathId)

        val gpsPoint = mutableListOf<
            Pair<
                Pair<Double,Double>,
                Pair<Int,Int>
            >
        >()

        while(pointParser.next() != XmlPullParser.END_TAG){
            if(pointParser.eventType != XmlPullParser.START_TAG){
                continue
            }
            if(pointParser.name == "point"){
                val (point,gps) = parsePoint(pointParser)
                graph.points.add(point)
                if(gps != null){
                    gpsPoint.add(gps)
                }
            }
        }

        while(pathParser.eventType != XmlPullParser.END_DOCUMENT){
            if(pathParser.next() == XmlPullParser.START_TAG){
                if(pathParser.name == "path"){
                    var p1 = -1
                    var p2 = -1
                    for (atrIndex in 0..(pathParser.attributeCount-1) ){
                        when(pathParser.getAttributeName(atrIndex)){
                            "p1" -> p1 = pathParser.getAttributeValue(atrIndex).toInt()
                            "p2" -> p2 = pathParser.getAttributeValue(atrIndex).toInt()
                        }
                    }
                    graph.addEdgeById(p1,p2)
                }
                pathParser.next()
            }
        }
        
        //TODO implment your own way to get a matrix...

        var trasforms : Matrix = arrayOf(doubleArrayOf())
        //TODO, throw error
        if(gpsPoint.size >= 2){
            val p1 = gpsPoint[0]
            val p2 = gpsPoint[1]
            var longSlope = p1.second.first - p2.second.first / p1.first.first - p2.first.first
            var longOffset = p1.second.first - longSlope * p1.first.first

            var latSlope = p1.second.second - p2.second.second / p1.first.second - p2.first.second
            var latOffset = p1.second.second - latSlope * p1.first.second

            trasforms = arrayOf(
                doubleArrayOf(
                    longSlope,
                    0.0, //you could use this to control rotation, if needed
                    longOffset
                ),
                doubleArrayOf(
                    0.0,
                    latSlope,
                    latOffset
                )
            )
        } // elese { error }

        return Place(graph,trasforms)
    }

    private fun parsePoint(pointParser: XmlPullParser) :
            Pair<Point,
                Pair<
                    Pair<Double,Double>,
                    Pair<Int,Int>
                >?
            > {
        pointParser.require(XmlPullParser.START_TAG, "", "point")
        var id : Int = -1
        for (attrIndex in 0..pointParser.attributeCount-1){
            if( pointParser.getAttributeName(attrIndex) == "id"){
                id = pointParser.getAttributeValue(attrIndex).toInt()
            }
        }
        var x: Int = 0
        var y: Int = 0
        var name: String? = null
        var gps: Pair<Double, Double>? = null

        while (pointParser.next() != XmlPullParser.END_TAG) {
            when (pointParser.name) {
                "x" -> x = readText(pointParser).toInt()
                "y" -> y = readText(pointParser).toInt()
                "name" -> name = readText(pointParser)
                "gps" -> gps = parseGPS(pointParser)
            }
        }
        if(gps != null) {
            return Pair(Point(id, x, y, name), Pair(gps, Pair(x, y)))
        } else {
            return Pair(Point(id!!, x, y, name), null )
        }
    }

    private fun readText(parser: XmlPullParser) : String{
        var out = ""
        if(parser.next() == XmlPullParser.TEXT){
            out =  parser.text
            parser.nextTag()
        }
        return out
    }

    private fun parseGPS(pointParser: XmlPullParser): Pair<Double, Double>? {
        pointParser.require(XmlPullParser.START_TAG, "", "gps")
        var long : Double = 0.0
        var lat : Double = 0.0
        while (pointParser.next() != XmlPullParser.END_TAG) {
            when (pointParser.name) {
                "longitude" -> long = readText(pointParser).toDouble()
                "latitude" -> lat = readText(pointParser).toDouble()
            }
        }
        return Pair(long,lat)
    }
}