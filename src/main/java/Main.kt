

import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import org.hiucimon.klex.pcgen_char.KLEX_PARSE
import org.hiucimon.klex.pcgen_char.KLEX_TOKEN_LIST
import org.hiucimon.klex.pcgen_char.addAll

fun main(args : Array<String>) {
    addAll()
    KLEX_PARSE("./src/main/java/Twitchy-13t6-short.pcg")
    val tokenStream=KLEX_TOKEN_LIST.toList().toObservable()
    tokenStream.subscribeBy(
            onNext = { println(it) },
            onError = { it.printStackTrace() },
            onComplete = { println("Done!") }
    )
}

