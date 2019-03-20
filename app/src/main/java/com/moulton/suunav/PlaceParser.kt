package com.moulton.suunav

import android.content.Context
import org.xmlpull.v1.XmlPullParser

class PlaceParser(val c : Context) {
    fun parse(pointId : Int, pathId: Int) : Place{
        var graph = Graph()
        val pointParser = c.resources.getXml(pointId)
        val pathParser = c.resources.getXml(pathId)


        // parse points
        var id : Int = 0
        var x : Int = 0
        var y : Int = 0
        var name : String? = null

        var long : Double? = null
        var lat : Double? = null

        var long_1 : Double = 0.0
        var lat_1 : Double = 0.0

        var cx_1 : Int = 0
        var cy_1 : Int = 0

        var long_2 : Double = 0.0
        var lat_2 : Double = 0.0

        var cx_2 : Int = 0
        var cy_2 : Int = 0



        //
        while(pointParser.eventType != XmlPullParser.END_DOCUMENT ){
            if(pointParser.next() == XmlPullParser.START_TAG){
                if (pointParser.name == "Point"){
                    id = pathParser.idAttribute.toInt()
                    //parse the rest of the point

                    //while we haven't hit the end of the place tag...
                    while(pointParser.next() == XmlPullParser.START_TAG){
                        when(pointParser.name){
                            "x" -> {
                                if(pointParser.next() == XmlPullParser.TEXT){
                                    x = pointParser.text.toInt()
                                    // go to end tag
                                    pointParser.next()
                                }
                            }
                            "y" -> {
                                if(pointParser.next() == XmlPullParser.TEXT){
                                    y = pointParser.text.toInt()
                                }
                            }
                            "name" -> {
                                if(pointParser.next() == XmlPullParser.TEXT){
                                    name = pointParser.text
                                }
                            }
                            "GPS" -> {
                                while(pointParser.next() == XmlPullParser.START_TAG) {
                                    when (pathParser.name) {
                                        "longitude" -> {
                                            if (pointParser.next() == XmlPullParser.TEXT) {
                                                long = pointParser.text.toDouble()
                                            }
                                        }
                                        "latitude" -> {
                                            if (pointParser.next() == XmlPullParser.TEXT) {
                                                lat = pointParser.text.toDouble()
                                            }
                                        }
                                    }
                                    //advance to end
                                    pointParser.next()
                                }
                            }
                        }
                        pointParser.next()
                    }

                }
                //Do something with the data parsed.
                graph.points.add(Point(id,x,y,name))
                if(long != null){
                    if(long_1 == 0.0){
                        long_1 = long
                        cx_1 = x
                    } else {
                        long_2 = long
                        cx_2 = x
                    }
                }

                if(lat != null){
                    if(lat_1 == 0.0){
                        lat_1 = lat
                        cy_1 = y
                    } else {
                        lat_2 = lat
                        cy_2 = y
                    }
                }

                //reset some that probably won't get rewriten
                name = null
                lat = null
                long = null
            }
        }
        val x_scale = scale(long_1,cx_1.toDouble(),long_2,cx_2.toDouble())
        val y_scale = scale(lat_1,cy_1.toDouble(),lat_2,cy_2.toDouble())

        val x_offset = offSet(cx_1.toDouble(),long_1,x_scale)
        val y_offset = offSet(cy_1.toDouble(),lat_1,y_scale)
        //parse the paths

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

        return Place(graph,x_scale,y_scale,x_offset,y_offset)
    }

    private fun scale(a1 : Double, a2 : Double, b1 : Double, b2 : Double):Double{
        return (a1-b1) / (a2 - b2)
    }

    private fun offSet(a : Double, b : Double, s : Double) : Double {
       return b - a*s
    }
}