
import LexicalAnalysis.id
import LexicalAnalysis.minus
import LexicalAnalysis.number
import LexicalAnalysis.plus
import me.sargunvohra.lib.cakeparse.api.*
import me.sargunvohra.lib.cakeparse.lexer.TokenInstance
import java.io.File


fun main() {

    val file = File("cMinusCode.c").readText()

    try {
        val tokens = LexicalAnalysis.Lex.lex(file)
        //val list = tokens.toList()
        val result = tokens.parseToEnd(Parser.getParser())
        val state = result.value
        println(state.code)
    } catch (e: Exception) {
        System.err.println(e)
    }

}