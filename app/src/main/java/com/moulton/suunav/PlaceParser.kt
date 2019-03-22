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

        // parse points
        var id : Int = 0
        var x : Int = 0
        var y : Int = 0
        var name : String? = null
        var long : Double? = null
        var lat : Double? = null

        while(pointParser.eventType != XmlPullParser.END_DOCUMENT ){
            if(pointParser.eventType == XmlPullParser.END_TAG && pointParser.name == "place"){
                break
            }
            while (!(pointParser.eventType == XmlPullParser.START_TAG && pointParser.name == "point")){
                pointParser.next()
            }


            if(pointParser.eventType == XmlPullParser.START_TAG && pointParser.name == "point"){
                for( attrIndex in 0 ..(pointParser.attributeCount-1) ){
                    if (pointParser.getAttributeName(attrIndex) == "id"){
                        id = pointParser.getAttributeValue(attrIndex).toInt()
                    }
                }
                pointParser.next()
                while(true) {
                    var elementName = pointParser.name
                    //advance to text
                    pointParser.next()
                    when (elementName) {
                        "x" -> x = pointParser.text.toInt()
                        "y" -> y = pointParser.text.toInt()
                        "name" -> name = pointParser.text
                        "gps" -> {
                            while (true) {
                                elementName = pointParser.name
                                pointParser.next()
                                when (elementName) {
                                    "longitude" -> long = pointParser.text.toDouble()
                                    "latitude" -> lat = pointParser.text.toDouble()
                                }
                                //go to end
                                pointParser.next()
                                // got to next tag unless its the end...
                                if(pointParser.next() == XmlPullParser.END_TAG && pointParser.name == "gps"){
                                    pointParser.next()
                                    break
                                }
                            }
                        }
                    }
                    //advance to end tag
                    pointParser.next()
                    //advance to start of next tag
                    pointParser.next()
                    //but make sure that isn't actualy the next tag.
                    if(pointParser.eventType == XmlPullParser.END_TAG && pointParser.name == "point"){
                        pointParser.next()
                        break
                    }
                }
                //Do something with the data parsed.
                graph.points.add(Point(id,x,y,name))
                if(long != null || lat != null){
                    gpsPoint.add(
                        Pair(
                            Pair(long!!,lat!!),
                            Pair(x,y)
                        )
                    )
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

        //calculate matrix.
        val long_list = gpsPoint.map { it.first.first }.toDoubleArray()
        val lat_list = gpsPoint.map { it.first.second }.toDoubleArray()

        val x_list = gpsPoint.map { it.second.first.toDouble() }.toDoubleArray()
        val y_list = gpsPoint.map { it.second.second.toDouble() }.toDoubleArray()

        val coords = arrayOf(long_list,lat_list)

        val lat_transform = multipleRegression(x_list,coords)
        val long_transform = multipleRegression(y_list,coords)

        val trasforms = arrayOf(long_transform,lat_transform)

        return Place(graph,trasforms)
    }

    private fun scale(a1 : Double, a2 : Double, b1 : Double, b2 : Double):Double{
        return (a1-b1) / (a2 - b2)
    }

    private fun offSet(a : Double, b : Double, s : Double) : Double {
       return b - a*s
    }
}