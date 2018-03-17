

import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import java.io.File

//val p=arrayOf<String>(
//        "fun testFunc(v : String) {",
//        "   println(v)",
//        "}"
//)

val Pipes= hashMapOf<String, Observables>()
val COMMENT1=Regex("^[ ]*#.*")
val COMMENT2=Regex("^#.*")
val BLANKLINE=Regex("^(\\\\s+)")

fun main(args : Array<String>) {
    println("Hello, world!")
    var list:List<Any> = arrayListOf("One", 2, "Three", "Four", 4.5, "Five", 6.0f)
    var observable: Observable<Any> = list.toObservable()
    observable.subscribeBy( // named arguments for
            onNext = { println(it) },
            onError = { it.printStackTrace() },
            onComplete = { println("Done!") }
    )
    val parseRes= mutableListOf<Token>()
    transitionMap["a"]=Transition("ID",Regex("^([a-zA-Z0-9]+)(.*?)"))
    transitionMap["b"]=Transition("SEP",Regex("^([|]|$)(.*?)"))
    transitionMap["c"]=Transition("COLON",Regex("^(:)(.*?)"))
    val inFile= File("./src/main/java/pcgen_char.klex")
    var LineNum=0
    var Column=0
    inFile.forEachLine {
        LineNum++
        val temp=COMMENT1.replace(it,"")
        val temp2=COMMENT2.replace(temp,"")
        if (!temp2.equals("")) {
            println(temp2)
            transitionMap.forEach { t, u ->
                println("-->${t}<->${u.name}<--")
                val temp3=u.m.matchEntire(temp2)
                if (temp3 != null) {
                    parseRes.add(Token(temp3.destructured.component1(),LineNum,Column,u))
                    println(temp3.destructured.component1())
                }
            }
        }
    }
    println(parseRes.toList())
    val tokenStream=parseRes.toList().toObservable()
    tokenStream.subscribeBy( // named arguments for
            onNext = { println(it) },
            onError = { it.printStackTrace() },
            onComplete = { println("Done!") }
    )
}

val transitionMap=hashMapOf<String,Transition>()

data class Transition(val name:String,val m:Regex)

data class Token(val Value:String,val line:Int,val col:Int,val Tran:Transition)
//    val writer = PrintWriter("./Generated/code.kt")  // java.io.PrintWriter
//
//    for (a in p) {  // history: Map<Member, String>
//        writer.append("$a\n")
//    }
//
//    writer.close()