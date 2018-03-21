package org.hiucimon.klex.pcgen_char

import java.io.File
import java.util.*

fun addAll(){
    __ADD_TRANSITION__("START","COMMENT","#.*")
    __ADD_TRANSITION__("START","BLANKS","[ \\t]+")
    __ADD_TRANSITION__("START","VERSIONNUM", "[0-9]+[\\.][0-9]+[\\.][0-9]+")
    __ADD_TRANSITION__("START","NUMBER", "[0-9]+[\\.][0-9]+")
    __ADD_TRANSITION__("START","CAMPAIGN","CAMPAIGN:","CAMPAIGNMODE")
    __ADD_TRANSITION__("START","ID","[a-zA-Z0-9]+")
    __ADD_TRANSITION__("START","SEP","[|]")
    __ADD_TRANSITION__("START","COLON",":")
    __ADD_TRANSITION__("CAMPAIGNMODE","CAMPAIGNSTRING","[^|]+")
    __ADD_TRANSITION__("CAMPAIGNMODE","SEP","[|]")
    __ADD_TRANSITION__("CAMPAIGNMODE","EOL","[\r\n]","__return__")
}

val __REGEX_STRINGS__=hashMapOf<String,String>()

val __PARSE_MAP__=hashMapOf<String, HashMap<String, __TRANSITION__>>()

val __TRANSITIONMAP__= hashMapOf<String, HashMap<String, String>>()

val __GLOBAL_VARS__= hashMapOf<String,Any>()

fun __ADD_TRANSITION__(mode:String,name:String,regex:String,transitionto:String?=null) {
    if (transitionto!=null) {
        val s:String=transitionto
        if (!__TRANSITIONMAP__.containsKey(mode)) {
            __TRANSITIONMAP__[mode]= HashMap<String,String>()
        }
        __TRANSITIONMAP__[mode]?.put(name,s)
    }
    val index=if (__GLOBAL_VARS__.containsKey("INDEX")) {
        val temp=__GLOBAL_VARS__["INDEX"] as Int
        temp+1
    } else {
        1
    }
    __GLOBAL_VARS__["INDEX"]=index
    val INDEX="%03d".format(index)
    __REGEX_STRINGS__.put(INDEX,regex)
    if (!__PARSE_MAP__.containsKey(mode)) {
        __PARSE_MAP__[mode]=HashMap<String,__TRANSITION__>()
    }
    __PARSE_MAP__[mode]?.put(INDEX, __TRANSITION__(name,Regex("^(${regex})(.*?)")))
}

val __MODE_STACK__= Stack<String>()

val KLEX_TOKEN_LIST= mutableListOf<__TOKEN__>()

data class __TRANSITION__(val name:String, val m:Regex)

data class __TOKEN__(val Value:String, val line:Int, val col:Int, val tran: __TRANSITION__?)

data class __MATCH_RESULTS__(val data:String, val remaining:String, val name:String, val trans:String)

fun KLEX_PARSE(fileName:String) {
    __MODE_STACK__.push("START")
    val inFile = File(fileName)
    var LineNum = 0
    var Column = 1
    KLEX_TOKEN_LIST.add(KLEX_TOKEN_LIST.size, __TOKEN__("BOL", LineNum, Column, __PARSE_MAP__[__MODE_STACK__.peek()]?.get("START")))
    inFile.forEachLine {
        LineNum++
        Column = 1
        var Line: String? = it
        while (Line != null) {
            val t = if (Line.equals("")) {
                Line = null
                val t = __TRANSITION__("EOL", Regex(""))
                KLEX_TOKEN_LIST.add(KLEX_TOKEN_LIST.size, __TOKEN__("EOL", LineNum, Column, __PARSE_MAP__[__MODE_STACK__.peek()]?.get(t.name)))
                KLEX_TOKEN_LIST.add(KLEX_TOKEN_LIST.size, __TOKEN__("BOL", LineNum, 1, __PARSE_MAP__[__MODE_STACK__.peek()]?.get(t.name)))
                __MATCH_RESULTS__("", "", "000", "EOL")
            } else {
                var work = ""
                val t = __FIND_FIRST_MATCH__(Line, __PARSE_MAP__[__MODE_STACK__.peek()])
                Line = t.remaining
                KLEX_TOKEN_LIST.add(KLEX_TOKEN_LIST.size, __TOKEN__(t.data, LineNum, Column, __PARSE_MAP__[__MODE_STACK__.peek()]?.get(t.name)))
                t
            }
            Column = Column + t.data.length
            if (__TRANSITIONMAP__[__MODE_STACK__.peek()]?.get(t.trans) != null) {
                if (__TRANSITIONMAP__[__MODE_STACK__.peek()]?.get(t.trans).equals("__return__")) {
                    __MODE_STACK__.pop()
                } else {
                    __MODE_STACK__.push(__TRANSITIONMAP__[__MODE_STACK__.peek()]?.get(t.trans))
                }
            }

        }
    }
    val t = KLEX_TOKEN_LIST[KLEX_TOKEN_LIST.lastIndex].tran
    KLEX_TOKEN_LIST[KLEX_TOKEN_LIST.lastIndex] = __TOKEN__("EOF", LineNum, Column, t)
}

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
