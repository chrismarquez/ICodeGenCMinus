import LexicalAnalysis.comma
import LexicalAnalysis.elseR
import LexicalAnalysis.equals
import LexicalAnalysis.id
import LexicalAnalysis.ifR
import LexicalAnalysis.intR
import LexicalAnalysis.leftBrace
import LexicalAnalysis.leftBraket
import LexicalAnalysis.leftParens
import LexicalAnalysis.lessThan
import LexicalAnalysis.lessThanEquals
import LexicalAnalysis.minus
import LexicalAnalysis.moreThan
import LexicalAnalysis.moreThanEquals
import LexicalAnalysis.notEquals
import LexicalAnalysis.number
import LexicalAnalysis.over
import LexicalAnalysis.plus
import LexicalAnalysis.returnR
import LexicalAnalysis.rightBrace
import LexicalAnalysis.rightBraket
import LexicalAnalysis.rightParens
import LexicalAnalysis.semicolon
import LexicalAnalysis.times
import LexicalAnalysis.voidR
import LexicalAnalysis.whileR
import com.sun.xml.internal.rngom.parse.host.Base
import me.sargunvohra.lib.cakeparse.api.*
import me.sargunvohra.lib.cakeparse.lexer.TokenInstance
import me.sargunvohra.lib.cakeparse.parser.BaseParser

object Parser {
    val empty = empty<BaseParser<TokenInstance>>()

    // Recursive


    val argListRef: BaseParser<TokenInstance> = ref { argList }
    val argsRef: BaseParser<Any?> = ref { args }
    val callRef:BaseParser<TokenInstance> = ref{call}
    val expressionRef: BaseParser<TokenInstance> = ref { expression }
    val mulopRef: BaseParser<TokenInstance> = ref {mulop}
    val factorRef: BaseParser<TokenInstance> = ref {factor}
    val termRef: BaseParser<TokenInstance> = ref { term }
    val additiveExpRef: BaseParser<TokenInstance> = ref { additive_expression }
    val statementListRef: BaseParser<Any?> = ref { statement_list }
    val localDeclarationsRef: BaseParser<Any?> = ref { local_declarations }
    val paramListRef: BaseParser<TokenInstance> = ref { param_list }
    val declarationListRef: BaseParser<TokenInstance> = ref { declaration_list }
    val addOpRef: BaseParser<TokenInstance> = ref { addop }
    val relopRef: BaseParser<TokenInstance> = ref { relop }
    val variableRef: BaseParser<TokenInstance> = ref{ variable }
    val simpleExpressionRef: BaseParser<TokenInstance> = ref { simple_expression}
    val expressionStmtRef: BaseParser<TokenInstance> = ref { expression_stmt }
    val compoundStmtRef: BaseParser<TokenInstance> = ref { compound_stmt }
    val statementRef: BaseParser<TokenInstance> = ref { statement}
    val selectionStmtRef: BaseParser<TokenInstance> = ref { selection_stmt }
    val iterationStmtRef: BaseParser<TokenInstance> = ref { iteration_stmt }
    val returnStmtRef: BaseParser<TokenInstance> = ref { return_stmt }
    val paramsRef: BaseParser<TokenInstance> = ref { params }
    val paramRef: BaseParser<TokenInstance> = ref {param}
    val typeSpecifierRef: BaseParser<TokenInstance> = ref { type_specifier }
    val variableDeclaration: BaseParser<TokenInstance> = ref { variable_declaration }
    val funcDeclaration: BaseParser<TokenInstance> = ref { fun_declaration }
    val declarationRef: BaseParser<TokenInstance> = ref { declaration }
    // Normal

    val program = declarationListRef
    val declaration_list = (declarationListRef then declarationRef ) or declarationRef
    val declaration = variableDeclaration or funcDeclaration
    val variable_declaration = (typeSpecifierRef then id then semicolon) or
                            (typeSpecifierRef then id then leftBraket then number then rightBraket then semicolon)
    val type_specifier = intR or voidR
    val fun_declaration = type_specifier then id then leftParens then paramsRef then rightParens then compoundStmtRef
    val params = paramListRef or voidR
    val param_list = (paramListRef then comma then paramRef) or paramRef
    val param = (type_specifier then id) or type_specifier then id then leftBraket
    val compound_stmt = (leftBrace then localDeclarationsRef then statementListRef then rightBrace)
    val local_declarations = (localDeclarationsRef then variable_declaration ) or empty
    val statement_list = (statementListRef then statementRef) or empty
    val statement = expressionStmtRef or compoundStmtRef or selectionStmtRef or iterationStmtRef or returnStmtRef
    val expression_stmt = (expressionRef then semicolon) or semicolon
    val selection_stmt = (ifR then leftParens then expressionRef then rightParens then statementRef) or
                         (ifR then leftParens then expressionRef then rightParens then statementRef then elseR then statementRef)
    val iteration_stmt = whileR then leftParens then expressionRef then rightParens then statementRef
    val return_stmt = (returnR then semicolon) or (returnR then expressionRef then semicolon)
    val expression = (variableRef then equals then expressionRef) or simpleExpressionRef
    val variable = id or (id then leftBraket then expressionRef then rightBraket)
    val simple_expression = (additiveExpRef then relopRef then additiveExpRef ) or additiveExpRef

    val relop = lessThanEquals or lessThan or moreThan or moreThanEquals or equals or notEquals
    val additive_expression = (additiveExpRef then addOpRef then termRef ) or addOpRef
    val addop = plus or minus
    val term = (termRef then mulopRef then factorRef) or factorRef
    val mulop = times or over
    val factor = (leftParens then expression then rightParens) or variable or callRef or number

    val call = (id then leftParens then argsRef then rightParens)
    val args = argListRef or empty
    val argList =  (argListRef then comma then expression) or expression

    fun getParser(): BaseParser<TokenInstance> {
        return program
    }

}