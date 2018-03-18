

import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import java.io.File

fun main(args : Array<String>) {
    val parseRes= mutableListOf<Token>()
    transitionMap["000"]=Transition("EOL",Regex("^($)(.)"))
    transitionMap["001"]=Transition("COMMENT",Regex("^[ ]*(#.*)($)"))
    transitionMap["002"]=Transition("BLANKS",Regex("^([ \\t]+)(.*?)"))
    transitionMap["003"]=Transition("VERSIONNUM",Regex("^([0-9]+[\\.][0-9]+[\\.][0-9]+)(.*?)"))
    transitionMap["004"]=Transition("NUMBER",Regex("^([0-9]+[\\.][0-9]+)(.*?)"))
    transitionMap["005"]=Transition("ID",Regex("^([a-zA-Z0-9]+)(.*?)"))
    transitionMap["006"]=Transition("SEP",Regex("^([|]|$)(.*?)"))
    transitionMap["007"]=Transition("COLON",Regex("^(:)(.*?)"))
    CurrentMode["START"]=transitionMap
    val inFile= File("./src/main/java/Twitchy-13t6-short.pcg")
    var LineNum=0
    var Column=0
    inFile.forEachLine {
        LineNum++
        var Line:String?=it
        while (Line!=null) {
            if (Line.equals("")) {
                Line=null
                val t=Transition("EOL",Regex(""))
                parseRes.add(parseRes.size,Token("EOL",LineNum,Column,CurrentMode["START"]?.get(t.name)))
                println("EOL")
            } else {
                var work = ""
                val t = findBestMatch(Line, CurrentMode["START"])
                println(Line)
                if (CurrentMode["START"]?.get(t.name) != null) {

                    println("Token=${t.data} Rule=${t.name} Name=${CurrentMode["START"]?.get(t.name)?.name}")
                }
                Line = t.remaining
                parseRes.add(parseRes.size,Token(t.data,LineNum,Column,CurrentMode["START"]?.get(t.name)))
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

data class matchRes(val data:String,val remaining:String,val name:String)

fun findBestMatch(IN: String, m: HashMap<String, Transition>?):matchRes {
    var longest=-1
    println("Checking -->${IN}<->${m}<-")
    m?.keys?.forEach {
        println("-->${m?.get(it)}")
        val t= m?.get(it)

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
                    return matchRes(matching,remaining,it)
                }
            }
        }

    }
    val a=matchRes(IN,"","")
    return a
}

fun keyValue(key: String,value: String): HashMap<String, String> {
    val ret=hashMapOf<String,String>()
    ret[key]=value
    return ret
}


val transitionMap=hashMapOf<String,Transition>()

var CurrentMode= hashMapOf<String,HashMap<String,Transition>>()

val tmap= hashMapOf<String,HashMap<String,String>>(
        Pair("START",keyValue("CAMPAIGN","CAMPAIGNMODE")),
        Pair("CAMPAIGNMODE",keyValue("EOL","START"))

)

