import LexicalAnalysis.comma
import LexicalAnalysis.equals
import LexicalAnalysis.id
import LexicalAnalysis.leftBraket
import LexicalAnalysis.rightBraket
import me.sargunvohra.lib.cakeparse.api.*
import me.sargunvohra.lib.cakeparse.lexer.TokenInstance
import me.sargunvohra.lib.cakeparse.parser.BaseParser

object Parser {

    // Recursive

    val argListRef: BaseParser<TokenInstance> = ref { argList }
    val expressionRef: BaseParser<TokenInstance> = ref { expression }

    // Normal

    val variable = id or (id then leftBraket then expressionRef then rightBraket)
    val expression = (variable then equals then expressionRef) or simpleExpression
    val argList =  (argListRef then comma then expression) or expression

}