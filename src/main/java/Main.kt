

import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import java.io.File
import java.util.*

fun main(args : Array<String>) {
    currentMode.push("START")
    val parseRes= mutableListOf<Token>()
    GlobalRegexMap["001"]=Transition("COMMENT",Regex("^[ ]*(#.*)($)"))
    GlobalRegexMap["002"]=Transition("BLANKS",Regex("^([ \\t]+)(.*?)"))
    GlobalRegexMap["003"]=Transition("VERSIONNUM",Regex("^([0-9]+[\\.][0-9]+[\\.][0-9]+)(.*?)"))
    GlobalRegexMap["004"]=Transition("NUMBER",Regex("^([0-9]+[\\.][0-9]+)(.*?)"))
    GlobalRegexMap["005"]=Transition("CAMPAIGN",Regex("^(CAMPAIGN:)(.*?)"))
    GlobalRegexMap["006"]=Transition("ID",Regex("^([a-zA-Z0-9]+)(.*?)"))
    GlobalRegexMap["007"]=Transition("SEP",Regex("^([|])(.*?)"))
    GlobalRegexMap["008"]=Transition("COLON",Regex("^(:)(.*?)"))
//    CampaignRegexMap["000"]=Transition("COLON",Regex("^(:)(.*?)"))
    CampaignRegexMap["001"]=Transition("CAMPAIGNSTRING",Regex("^([^|]+)(.*?)"))
    CampaignRegexMap["002"]=Transition("SEP",Regex("^([|])(.*?)"))
    CurrentMode["START"]=GlobalRegexMap
    CurrentMode["CAMPAIGNMODE"]=CampaignRegexMap
    val inFile= File("./src/main/java/Twitchy-13t6-short.pcg")
    var LineNum=0
    var Column=0
    inFile.forEachLine {
        LineNum++
        var Line:String?=it
        while (Line!=null) {
            val t=if (Line.equals("")) {
                Line=null
                val t=Transition("EOL",Regex(""))
                parseRes.add(parseRes.size,Token("EOL",LineNum,Column,CurrentMode[currentMode.peek()]?.get(t.name)))
                matchRes("","","000","EOL")
            } else {
                var work = ""
                val t = findBestMatch(Line, CurrentMode[currentMode.peek()])
                Line = t.remaining
                parseRes.add(parseRes.size,Token(t.data,LineNum,Column,CurrentMode[currentMode.peek()]?.get(t.name)))
                t
            }
            if (tmap[currentMode.peek()]?.get(t.trans)!=null) {
                if (tmap[currentMode.peek()]?.get(t.trans).equals("__return__")) {
                    currentMode.pop()
                } else {
                    currentMode.push(tmap[currentMode.peek()]?.get(t.trans))
                }
            }

        }
    }
    val tokenStream=parseRes.toList().toObservable()
    tokenStream.subscribeBy(
            onNext = { println(it) },
            onError = { it.printStackTrace() },
            onComplete = { println("Done!") }
    )
}



data class Transition(val name:String,val m:Regex)

data class Token(val Value:String, val line:Int, val col:Int, val Tran: Transition?)

data class matchRes(val data:String,val remaining:String,val name:String,val trans:String)

fun findBestMatch(IN: String, m: HashMap<String, Transition>?):matchRes {
    var longest=-1
    m?.keys?.forEach {
        val t= m?.get(it)
        val transname=if (t!=null) {
            t.name
        } else {
            ""
        }
        if (t != null) {
            val temp3=t.m.matchEntire(IN)
            if (temp3!=null) {
                val remaining = if (temp3.groups.size>1) {
                    temp3.destructured.component2()
                } else {
                    ""
                }

                val matching = if (temp3.groups.size>0) {
                    temp3.destructured.component1()
                } else {
                    ""
                }
                if (matching!=null && matching.length>longest) {
                    return matchRes(matching,remaining,it,transname)
                }
            }
        }

    }
    val a=matchRes(IN,"","","")
    return a
}

fun keyValue(key: String,value: String): HashMap<String, String> {
    val ret=hashMapOf<String,String>()
    ret[key]=value
    return ret
}

val currentMode= Stack<String>()

val GlobalRegexMap=hashMapOf<String,Transition>()
val CampaignRegexMap=hashMapOf<String,Transition>()

var CurrentMode= hashMapOf<String,HashMap<String,Transition>>()

val tmap= hashMapOf<String,HashMap<String,String>>(
        Pair("START",keyValue("CAMPAIGN","CAMPAIGNMODE")),
        Pair("CAMPAIGNMODE",keyValue("EOL","__return__"))

)

