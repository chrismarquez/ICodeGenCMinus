import me.sargunvohra.lib.cakeparse.api.lexer
import me.sargunvohra.lib.cakeparse.api.token
import me.sargunvohra.lib.cakeparse.lexer.Lexer
import me.sargunvohra.lib.cakeparse.parser.ParsableToken

object LexicalAnalysis {

    val space = token("space", "[ \\t\\n\\r]", true)
    val id = token("id", "([A-Za-z]([A-Za-z0-9])*)|(_([_A-Za-z0-9])*[A-za-z]([A-Za-z0-9])*)")
    val number = token("number", "[0-9]+")

    val leftBrace = token("leftBrace", "\\{")
    val rightBrace = token("rightBrace", "\\}")
    val leftBraket = token("leftBraket", "\\[")
    val rightBraket = token("rightBraket", "\\]")
    val leftParens = token("leftParens", "\\(")
    val rightParens = token("rightParens", "\\)")
    val comma = token("comma", "\\,")
    val semicolon = token("semicolon", ";")

    val plus = token("plus", "\\+")
    val minus = token("minus", "-")
    val times = token("times", "\\*")
    val over = token("over", "\\/")

    val equals = token("equals", "==")
    val lessThan = token("lessThan", "<")
    val moreThan = token("moreThan", ">")
    val lessThanEquals = token("lessThanEquals", "<=")
    val moreThanEquals = token("moreThanEquals", ">=")
    val notEquals = token("notEquals", "!=")

    val assign = token("asign", "=")

    val whileR = token("while", "while")
    val ifR = token("if", "if")
    val elseR = token("else", "else")
    val returnR = token("return", "return")
    val intR = token("int", "int")
    val voidR = token("void", "void")

    val Lex: Lexer
        get() = setOf(
            space, id, number,
            leftBrace, rightBrace, leftBraket, rightBraket, leftParens, rightParens, comma,
            plus, minus, times, over,
            equals, lessThan, moreThan, lessThanEquals, moreThanEquals, notEquals,
            assign,
            whileR, ifR, elseR, returnR, intR, voidR
        ).lexer()
}