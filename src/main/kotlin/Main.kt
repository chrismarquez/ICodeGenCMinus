
import LexicalAnalysis.id
import LexicalAnalysis.minus
import LexicalAnalysis.number
import LexicalAnalysis.plus
import me.sargunvohra.lib.cakeparse.api.*
import me.sargunvohra.lib.cakeparse.lexer.Token
import me.sargunvohra.lib.cakeparse.lexer.TokenInstance
import kotlin.math.min


fun main() {

    val gen = "t1"
    val code = mutableListOf<String>()

    val sum = number and (plus then number) map {
        code.add("$gen = ${it.first.raw} +  ${it.second.raw}")
        TokenInstance(id, gen, 0, 0, 0)
    }

    val minus = number and (minus then number)

    val addOp = sum or minus

    try {
        val result = LexicalAnalysis.Lex.lex("2 - 3").parseToEnd(addOp)
        println(result)
    } catch (e: Exception) {
        System.err.println(e)
    }
}