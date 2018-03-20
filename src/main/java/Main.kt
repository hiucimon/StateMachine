

import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import java.io.File
import java.util.*

val __MODE_STACK__= Stack<String>()

val __PARSE_RES__= mutableListOf<__TOKEN__>()

data class __TRANSITION__(val name:String, val m:Regex)

data class __TOKEN__(val Value:String, val line:Int, val col:Int, val tran: __TRANSITION__?)

data class __MATCH_RESULTS__(val data:String, val remaining:String, val name:String, val trans:String)

val __TRANSITIONMAP__= hashMapOf<String,HashMap<String,String>>(
        "START" to hashMapOf("CAMPAIGN" to "CAMPAIGNMODE"),
        "CAMPAIGNMODE" to hashMapOf("EOL" to "__return__")
)

val __REGEX_STRINGS__=hashMapOf(
        "001" to "#.*",
        "002" to "[ \\t]+",
        "003" to "[0-9]+[\\.][0-9]+[\\.][0-9]+",
        "004" to "[0-9]+[\\.][0-9]+",
        "005" to "CAMPAIGN:",
        "006" to "[a-zA-Z0-9]+",
        "007" to "[|]",
        "008" to ":",
        "009" to "[^|]+",
        "010" to "[|]"
)

val __PARSE_MAP__=hashMapOf<String,HashMap<String,__TRANSITION__>>(
        "START" to hashMapOf(
                "001" to __TRANSITION__("COMMENT",         Regex("^(${__REGEX_STRINGS__["001"]})(.*?)")),
                "002" to __TRANSITION__("BLANKS",          Regex("^(${__REGEX_STRINGS__["002"]})(.*?)")),
                "003" to __TRANSITION__("VERSIONNUM",      Regex("^(${__REGEX_STRINGS__["003"]})(.*?)")),
                "004" to __TRANSITION__("NUMBER",          Regex("^(${__REGEX_STRINGS__["004"]})(.*?)")),
                "005" to __TRANSITION__("CAMPAIGN",        Regex("^(${__REGEX_STRINGS__["005"]})(.*?)")),
                "006" to __TRANSITION__("ID",              Regex("^(${__REGEX_STRINGS__["006"]})(.*?)")),
                "007" to __TRANSITION__("SEP",             Regex("^(${__REGEX_STRINGS__["007"]})(.*?)")),
                "008" to __TRANSITION__("COLON",           Regex("^(${__REGEX_STRINGS__["008"]})(.*?)"))
        ),
        "CAMPAIGNMODE" to hashMapOf(
                "001" to __TRANSITION__("CAMPAIGNSTRING",   Regex("^(${__REGEX_STRINGS__["009"]})(.*?)")),
                "002" to __TRANSITION__("SEP",              Regex("^(${__REGEX_STRINGS__["010"]})(.*?)"))
        )
)

fun __FIND_FIRST_MATCH__(IN: String, m: HashMap<String, __TRANSITION__>?):__MATCH_RESULTS__ {
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
                    return __MATCH_RESULTS__(matching,remaining,it,transname)
                }
            }
        }

    }
    val a=__MATCH_RESULTS__(IN,"","","")
    return a
}

fun main(args : Array<String>) {
    __MODE_STACK__.push("START")
    val inFile= File("./src/main/java/Twitchy-13t6-short.pcg")
    var LineNum=0
    var Column=1
    __PARSE_RES__.add(__PARSE_RES__.size,__TOKEN__("BOL",LineNum,Column,__PARSE_MAP__[__MODE_STACK__.peek()]?.get("START")))
    inFile.forEachLine {
        LineNum++
        Column=1
        var Line:String?=it
        while (Line!=null) {
            val t=if (Line.equals("")) {
                Line=null
                val t=__TRANSITION__("EOL",Regex(""))
                __PARSE_RES__.add(__PARSE_RES__.size,__TOKEN__("EOL",LineNum,Column,__PARSE_MAP__[__MODE_STACK__.peek()]?.get(t.name)))
                __PARSE_RES__.add(__PARSE_RES__.size,__TOKEN__("BOL",LineNum,1,__PARSE_MAP__[__MODE_STACK__.peek()]?.get(t.name)))
                __MATCH_RESULTS__("","","000","EOL")
            } else {
                var work = ""
                val t = __FIND_FIRST_MATCH__(Line, __PARSE_MAP__[__MODE_STACK__.peek()])
                Line = t.remaining
                __PARSE_RES__.add(__PARSE_RES__.size,__TOKEN__(t.data,LineNum,Column,__PARSE_MAP__[__MODE_STACK__.peek()]?.get(t.name)))
                t
            }
            Column=Column+t.data.length
            if (__TRANSITIONMAP__[__MODE_STACK__.peek()]?.get(t.trans)!=null) {
                if (__TRANSITIONMAP__[__MODE_STACK__.peek()]?.get(t.trans).equals("__return__")) {
                    __MODE_STACK__.pop()
                } else {
                    __MODE_STACK__.push(__TRANSITIONMAP__[__MODE_STACK__.peek()]?.get(t.trans))
                }
            }

        }
    }
    val t=__PARSE_RES__[__PARSE_RES__.lastIndex].tran
    __PARSE_RES__[__PARSE_RES__.lastIndex]=__TOKEN__("EOF",LineNum,Column,t)
    val tokenStream=__PARSE_RES__.toList().toObservable()
    tokenStream.subscribeBy(
            onNext = { println(it) },
            onError = { it.printStackTrace() },
            onComplete = { println("Done!") }
    )
}

