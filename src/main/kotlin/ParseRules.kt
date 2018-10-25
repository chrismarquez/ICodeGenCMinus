
import me.sargunvohra.lib.cakeparse.lexer.TokenInstance

typealias TokenPair = Pair<TokenInstance, TokenInstance>

object ParseRules {

    var temporal: Int = 0
    var label: Int = 0

    private var index = 0

    fun next(): String {
        return "t${index++}"
    }

    fun join(it: Pair<ParseState, ParseState>): ParseState {
        val preCode = it.first.code + if (it.second.code != "") "\n" else ""
        val postCode = it.second.code
        return ParseState("$preCode$postCode", it.first.data, temporal, label)
    }

    fun callFunction(it: TokenInstance): ParseState {
        val functionId = it.raw
        val nextTemp = if (functionId != "read") "t$temporal" else "read"
        return ParseState("call $functionId", nextTemp, ++temporal, label)
    }

    fun callFunction(it: Pair<TokenInstance, ParseState>): ParseState {
        val begin = "begin_args\n"
        val functionId = it.first.raw
        val params = it.second.data.split(",")
        return if (functionId == "write" && params.size == 1) {
            val preCode = it.second.code + if (it.second.code != "") "\n" else ""
            val param = params[0]
            ParseState("${preCode}write $param", param,it.second.temporal,  it.second.label)
        } else {
            var paramCode = ""
            val preCode = it.second.code + if (it.second.code != "") "\n" else ""
            val temp = "t${it.second.temporal}"
            params.forEach { paramCode += "param $it\n" }
            val indexes = generateIndexes(params.size)
            val callCode = "$temp = call $functionId, $indexes"
            ParseState("$begin$preCode$paramCode$callCode", temp, it.second.temporal, it.second.label)
        }
    }

    fun parseArguments(it: Pair<ParseState, ParseState>): ParseState {
        val firstArg = it.first.data
        val otherArgs = it.second.data
        val preCode = it.first.code + if (it.first.code != "") "\n" else ""
        val nextCode = it.second.code + if (it.second.code != "") "\n" else ""
        return ParseState("$preCode$nextCode", "$firstArg,$otherArgs", it.first.temporal, it.first.label)
    }

    fun parseStatements(it: Pair<ParseState, ParseState>): ParseState {
        val preCode = it.first.code + if (it.first.code != "") "\n" else ""
        val postCode = it.second.code + if (it.second.code != "") "\n" else ""
        return ParseState("$preCode$postCode", "", it.first.temporal, it.second.temporal)
    }

    fun ifStart(it: ParseState): ParseState { // From expression
        val preCode = it.code + if (it.code != "") "\n" else ""
        val nextLabel = "L${it.label}"
        return ParseState("${preCode}if false ${it.data} goto $nextLabel", nextLabel, it.temporal, it.label + 1)
    }

    fun ifStatement(it: Pair<ParseState, ParseState>): ParseState {
        val preCode = it.first.code + if (it.first.code != "") "\n" else ""
        val postCode = it.second.code + if (it.second.code != "") "\n" else ""
        val labelCode = "label ${it.first.data}"
        val code = "$preCode$postCode$labelCode"
        return ParseState(code, "", it.second.temporal, it.second.label)
    }

    fun elseStatement(it: Pair<ParseState, ParseState>): ParseState {
        val preCode = it.first.code + if (it.first.code != "") "\n" else ""
        val postCode = it.second.code
        return ParseState("$preCode$postCode", "", it.second.temporal, it.second.label)
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
        val preCode = it.second.code + if (it.second.code != "") "\n" else ""
        val exprTemp = it.second.data
        return if (exprTemp == "read") {
            ParseState("read $varId", varId, it.first.temporal, it.first.label)
        } else ParseState("$preCode$varId = $exprTemp", varId, it.first.temporal, it.first.label)

    }

    fun bracketVar(it: Pair<TokenInstance, ParseState>): ParseState { // From id
        val varName = it.first.raw
        val preCode =  it.second.code
        val exprTemp = it.second.data
        return ParseState(preCode, "$varName[$exprTemp]", it.second.temporal, it.second.label)
    }

    fun fromToken(it: TokenInstance): ParseState {
        return ParseState("", it.raw, temporal, label)
    }

    fun singleExprLeft(it: Pair<ParseState, TokenInstance>): ParseState {
        val preCode = it.first.code
        val exprTemp =  it.first.data
        val operator = it.second.raw
        return ParseState(preCode, "$exprTemp $operator", it.first.temporal, it.first.label)
    }

    fun singleExprRight(it: Pair<ParseState, ParseState>): ParseState {
        val firstPrecode = it.first.code + if (it.first.code != "") "\n" else ""
        val secondPrecode = it.second.code + if (it.second.code != "") "\n" else ""
        val partialOp = it.first.data
        val exprTemp = it.second.data
        val nextTemp = "t${it.second.temporal}"
        val operation = "$nextTemp = $partialOp $exprTemp"
        return ParseState("$firstPrecode$secondPrecode$operation", nextTemp, it.second.temporal + 1, it.second.label)
    }

    fun emptyReturn(it: TokenInstance): ParseState {
        return ParseState("return", "", temporal, label)
    }

    fun varReturn(it: ParseState): ParseState {
        val preCode = it.code + if (it.code != "") "\n" else ""
        val exprTemp = it.data
        return ParseState("${preCode}return $exprTemp", it.data, it.temporal, it.label)
    }

    fun generateIndexes(size: Int): String {
        var result = ""
        for (i in 1 until size) result += "$i, "
        result += if (size == 1) size.toString() else " $size"
        return result
    }

    fun read(varId: String) = ParseState("read $varId", varId, temporal, label)
}
