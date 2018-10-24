import LexicalAnalysis.id
import me.sargunvohra.lib.cakeparse.lexer.Token
import me.sargunvohra.lib.cakeparse.lexer.TokenInstance

typealias TokenPair = Pair<TokenInstance, TokenInstance>

object ParseRules {

    private var index = 0

    fun next(): String {
        return "t${index++}"
    }

    fun join(tokens: Pair<ParseState, ParseState>): ParseState {
        val first = tokens.first
        val second = tokens.second
        return ParseState("${first.code}\n${second.code}", first.token, 0, 0)
    }

    fun callFunction(it: TokenInstance) = ParseState("call ${it.raw}", it, 0, 0)

    fun callFunction(it: Pair<TokenInstance, ParseState>): ParseState {
        val functionId = it.first.raw
        val params = it.second.code.split(",")
        return if (functionId == "write" && params.size == 1) {
            write(params[0])
        } else {
            var paramCode = ""
            params.forEach { paramCode += "param $it\n" }
            val indexes = generateIndexes(params.size)
            val callCode = "call $functionId, $indexes"
            ParseState("$paramCode\n$callCode", it.first, 0, 0)
        }
    }

    fun parseArguments(it: Pair<ParseState, ParseState>): ParseState {
        return ParseState("${it.first.code},${it.second.code}", it.first.token, it.first.temporal, it.first.label)
    }

    fun generateIndexes(size: Int): String {
        var result = ""
        for (i in 1 until size) result += "$i, "
        result += " $size"
        return result
    }

    fun write(varId: String) = ParseState("write $varId", TokenInstance(id, varId, 0 , 0, 0), 0, 0)

    fun read(varId: String) = ParseState("read $varId", TokenInstance(id, varId, 0 , 0, 0), 0, 0)
}
