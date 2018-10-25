import LexicalAnalysis.id
import me.sargunvohra.lib.cakeparse.lexer.Token
import me.sargunvohra.lib.cakeparse.lexer.TokenInstance
import kotlin.math.exp

typealias TokenPair = Pair<TokenInstance, TokenInstance>

object ParseRules {

    var temporal: Int = 0
    var label: Int = 0

    private var index = 0

    fun next(): String {
        return "t${index++}"
    }

    fun join(tokens: Pair<ParseState, ParseState>): ParseState {
        val first = tokens.first
        val second = tokens.second
        return ParseState("${first.code}\n${second.code}", first.data, 0, 0)
    }

    fun callFunction(it: TokenInstance) = ParseState("call ${it.raw}", it.raw, 0, 0)

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
            ParseState("$paramCode\n$callCode", it.first.raw, 0, 0)
        }
    }

    fun parseArguments(it: Pair<ParseState, ParseState>): ParseState {
        return ParseState("${it.first.code},${it.second.code}", it.first.data, it.first.temporal, it.first.label)
    }

    fun ifStart(it: ParseState): ParseState { // From expression
        val preCode = it.code
        val nextLabel = "L${it.label}"
        return ParseState("$preCode\nif false ${it.data} goto $nextLabel", nextLabel, it.temporal, it.label + 1)
    }

    fun ifStatement(it: Pair<ParseState, ParseState>): ParseState {
        val labelCode = "Label ${it.first.data}"
        val code = "${it.first.code}\n${it.second.code}\n$labelCode"
        return ParseState(code, "", it.second.temporal, it.second.label)
    }

    fun elseStatement(it: Pair<ParseState, ParseState>): ParseState {
        return ParseState("${it.first.code}\n${it.second.code}", "", it.second.temporal, it.second.label)
    }

    fun whileStatement(it: Pair<ParseState, ParseState>): ParseState { // From expression
        val firstLabel = "L${it.first.label}"
        val lastLabel = "L${it.first.label + 1}"
        val repeat = "if false ${it.first.data} goto $lastLabel"
        val code = "{firstLabel\n${it.first.code}\n$repeat\n${it.second.code}\ngoto $firstLabel\n$lastLabel"
        return ParseState(code, "", it.second.temporal, it.second.label + 2)
    }

    fun varAssign(it: Pair<ParseState, ParseState>): ParseState { // From variable
        val varId = it.first.data
        val preCode = it.second.code
        val exprTemp = it.second.data
        return ParseState("$preCode\n$varId = $exprTemp", varId, it.first.temporal, it.first.label)
    }

    fun bracketVar(it: Pair<TokenInstance, ParseState>): ParseState { // From id
        val varName = it.first.raw
        val preCode =  it.second.code
        val exprTemp = it.second.data
        return ParseState(preCode, "$varName[$exprTemp]", it.second.temporal, it.second.label)
    }

    fun fromToken(it: TokenInstance): ParseState {
        return ParseState("", it.raw, 0, 0)
    }

    fun singleExprLeft(it: Pair<ParseState, TokenInstance>): ParseState {
        val preCode = it.first.code
        val exprTemp =  it.first.data
        val operator = it.second.raw
        return ParseState(preCode, "$exprTemp $operator", it.first.temporal, it.first.label)
    }

    fun singleExprRight(it: Pair<ParseState, ParseState>): ParseState {
        val firstPrecode = it.first.code
        val secondPrecode = it.second.code
        val partialOp = it.first.data
        val exprTemp = it.second.data
        val nextTemp = "t${it.second.temporal}"
        val operation = "$nextTemp = $partialOp $exprTemp"
        return ParseState("$firstPrecode\n$secondPrecode\n$operation", nextTemp, it.second.temporal + 1, it.second.label)
    }

    fun emptyReturn(it: TokenInstance): ParseState {
        return ParseState("return", "", 0,0)
    }

    fun varReturn(it: ParseState): ParseState {
        val preCode = it.code
        val exprTemp = it.data
        return ParseState("$preCode\nreturn $exprTemp", it.data, it.temporal, it.label)
    }

    fun generateIndexes(size: Int): String {
        var result = ""
        for (i in 1 until size) result += "$i, "
        result += " $size"
        return result
    }

    fun write(varId: String) = ParseState("write $varId", varId,0,  0)

    fun read(varId: String) = ParseState("read $varId", varId, 0, 0)
}
