import LexicalAnalysis.assign
import LexicalAnalysis.comma
import LexicalAnalysis.elseR
import LexicalAnalysis.equals
import LexicalAnalysis.id
import LexicalAnalysis.ifR
import LexicalAnalysis.intR
import LexicalAnalysis.leftBrace
import LexicalAnalysis.leftBracket
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
import LexicalAnalysis.rightBracket
import LexicalAnalysis.rightParens
import LexicalAnalysis.semicolon
import LexicalAnalysis.space
import LexicalAnalysis.times
import LexicalAnalysis.voidR
import LexicalAnalysis.whileR
import ParseRules.join
import me.sargunvohra.lib.cakeparse.api.*
import me.sargunvohra.lib.cakeparse.lexer.TokenInstance
import me.sargunvohra.lib.cakeparse.parser.BaseParser

object Parser {

    // Recursive

    val argListRef: BaseParser<ParseState> = ref { argList }
    val callRef:BaseParser<ParseState> = ref{ call }
    val expressionRef: BaseParser<ParseState> = ref { expression }
    val mulopRef: BaseParser<TokenInstance> = ref {mulop}
    val factorRef: BaseParser<Any> = ref {factor}
    val termRef: BaseParser<Any> = ref { term }
    val additiveExpressionRef: BaseParser<Any> = ref { additiveExpression }
    val statementListRef: BaseParser<Any> = ref { statementList }
    val localDeclarationsRef: BaseParser<ParseState> = ref { localDeclarations }
    val paramListRef: BaseParser<TokenInstance> = ref { paramList }
    val declarationListRef: BaseParser<ParseState> = ref { declarationList }
    val addOpRef: BaseParser<TokenInstance> = ref { addop }
    val relopRef: BaseParser<TokenInstance> = ref { relop }
    val variableRef: BaseParser<TokenInstance> = ref{ variable }
    val simpleExpressionRef: BaseParser<ParseState> = ref { simpleExpression }
    val expressionStmtRef: BaseParser<TokenInstance> = ref { expressionStatement }
    val compoundStmtRef: BaseParser<ParseState> = ref { compoundStatement }
    val statementRef: BaseParser<Any> = ref { statement}
    val selectionStmtRef: BaseParser<Any> = ref { selectionStatement }
    val iterationStmtRef: BaseParser<Any> = ref { iterationStatement }
    val returnStmtRef: BaseParser<TokenInstance> = ref { returnStatement }
    val paramsRef: BaseParser<TokenInstance> = ref { params }
    val paramRef: BaseParser<TokenInstance> = ref { param }
    val typeSpecifierRef: BaseParser<TokenInstance> = ref { typeSpecifier }
    val variableDeclarationRef: BaseParser<ParseState> = ref { variableDeclaration }
    val funcDeclarationRef: BaseParser<ParseState> = ref { funcDeclaration }
    val declarationRef: BaseParser<ParseState> = ref { declaration }

    val innerBracketRef = ref { innerBracket }


    // Normal

    val program = declarationListRef
    val declarationList = (declarationRef and declarationListRef map(::join)) or declarationRef
    val declaration = funcDeclarationRef or variableDeclarationRef
    val variableDeclaration = ((typeSpecifierRef then id before semicolon) or
                              (typeSpecifierRef then id before leftBracket before number before rightBracket before semicolon)) map {
        ParseState("", it, 0, 0)
    }

    val typeSpecifier = intR or voidR
    val funcDeclaration = typeSpecifier then id and (leftParens then paramsRef before rightParens) map {
        ParseState("entry ${it.first.raw}:", it.first, 0, 0)
    } and compoundStmtRef map(::join)
    val params = voidR or paramListRef
    val paramList = paramRef or (paramRef before comma before paramListRef)
    val param = (typeSpecifier then id) or (typeSpecifier then id before leftBracket before rightBracket)
    val compoundStatement = ((leftBrace then rightBrace) or (leftBrace then innerBracketRef before rightBrace)) map {
        if (it is ParseState) {
            it
        } else {
            ParseState("", TokenInstance(space, "", 0, 0, 0), 0, 0)
        }
    }
    val innerBracket = (localDeclarationsRef then statementListRef) or localDeclarationsRef or statementListRef
    val localDeclarations = (variableDeclaration then localDeclarationsRef) or variableDeclaration
    val statementList = (statementRef then statementListRef) or statementRef
    val statement = returnStmtRef or selectionStmtRef or iterationStmtRef or expressionStmtRef or compoundStmtRef
    val expressionStatement = semicolon or (expressionRef then semicolon)
    val ifStatement = ifR then leftParens then expressionRef then rightParens then statementRef
    val ifElseStatement = ifR then leftParens then expressionRef map {
        println("${ParseRules.next()} = ${it.code}")
        it
    } then rightParens then statementRef then elseR then statementRef
    val selectionStatement =  ifElseStatement or ifStatement
    val iterationStatement = whileR then leftParens then expressionRef then rightParens then statementRef
    val returnStatement = (returnR then semicolon) or (returnR then expressionRef then semicolon)
    val expression = (variableRef and (assign then expressionRef) map {
        ParseState("${it.first.raw} = ${it.second.token.raw}", it.first, 0 ,0)
    }) or simpleExpressionRef
    val variable = (id then leftBracket then expressionRef then rightBracket) or id

    val simpleExpression = (additiveExpressionRef and relopRef map {
        ParseState("${it.first} ${it.second.raw}", it.second, 0, 0)
    } and additiveExpressionRef map {
        ParseState("${it.first} ${it.second}", it.first.token, 0, 0)
    }) or additiveExpressionRef map {
        ParseState(it.toString(), TokenInstance(space, "", 0, 0, 0), 0, 0)
    }

    val relop = lessThanEquals or lessThan or moreThan or moreThanEquals or equals or notEquals
    val additiveExpression = (termRef and addOpRef and additiveExpressionRef) or termRef
    val addop = plus or minus
    val term = (factorRef then mulopRef then termRef) or factorRef
    val mulop = times or over
    val factor = callRef or variable or number or (leftParens then expression then rightParens)


    val emptyCall = id then leftParens then rightParens map(ParseRules::callFunction)
    val paramCall = id and (leftParens then argListRef before rightParens) map(ParseRules::callFunction)
    val call = emptyCall or paramCall

    val argList = (expression before comma and argListRef map(ParseRules::parseArguments)) or expression

    fun getParser(): BaseParser<Any> {
        return program
    }




}