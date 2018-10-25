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
    val factorRef: BaseParser<ParseState> = ref {factor}
    val termRef: BaseParser<ParseState> = ref { term }
    val additiveExpressionRef: BaseParser<ParseState> = ref { additiveExpression }
    val statementListRef: BaseParser<ParseState> = ref { statementList }
    val localDeclarationsRef: BaseParser<ParseState> = ref { localDeclarations }
    val paramListRef: BaseParser<TokenInstance> = ref { paramList }
    val declarationListRef: BaseParser<ParseState> = ref { declarationList }
    val addOpRef: BaseParser<TokenInstance> = ref { addop }
    val relopRef: BaseParser<TokenInstance> = ref { relop }
    val variableRef: BaseParser<ParseState> = ref{ variable }
    val simpleExpressionRef: BaseParser<ParseState> = ref { simpleExpression }
    val expressionStmtRef: BaseParser<ParseState> = ref { expressionStatement }
    val compoundStmtRef: BaseParser<ParseState> = ref { compoundStatement }
    val statementRef: BaseParser<ParseState> = ref { statement }
    val selectionStmtRef: BaseParser<ParseState> = ref { selectionStatement }
    val iterationStmtRef: BaseParser<ParseState> = ref { iterationStatement }
    val returnStmtRef: BaseParser<ParseState> = ref { returnStatement }
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
    val variableDeclaration = (typeSpecifierRef then id before semicolon map(ParseRules::fromToken)) or
                              (((typeSpecifierRef then id before leftBracket) and number) before rightBracket before semicolon map {
        ParseState("", "${it.first.raw}[${it.second.raw}]", 0, 0)
    })

    val typeSpecifier = intR or voidR
    val funcDeclaration = typeSpecifier then id and (leftParens then paramsRef before rightParens) map {
        ParseState("entry ${it.first.raw}:", it.first.raw, 0, 0)
    } and compoundStmtRef map(::join)
    val params = voidR or paramListRef
    val paramList = paramRef or (paramRef before comma before paramListRef)
    val param = (typeSpecifier then id) or (typeSpecifier then id before leftBracket before rightBracket)
    val compoundStatement = ((leftBrace then rightBrace) or (leftBrace then innerBracketRef before rightBrace)) map {
        if (it is ParseState) {
            it
        } else {
            ParseState("", "", 0, 0)
        }
    }
    val innerBracket = (localDeclarationsRef then statementListRef) or localDeclarationsRef or statementListRef
    val localDeclarations = (variableDeclaration then localDeclarationsRef) or variableDeclaration
    val statementList = (statementRef and statementListRef map(ParseRules::parseStatements)) or statementRef
    val statement = returnStmtRef or selectionStmtRef or iterationStmtRef or expressionStmtRef or compoundStmtRef
    val expressionStatement = semicolon map { ParseState.Empty } or (expressionRef before semicolon)
    val ifStatement = ifR then leftParens then expressionRef before rightParens map(ParseRules::ifStart) and statementRef map(ParseRules::ifStatement)
    val ifElseStatement = ifR then leftParens then expressionRef before rightParens map(ParseRules::ifStart) and
        statementRef map (ParseRules::ifStatement) before elseR and statementRef map(ParseRules::elseStatement)
    val selectionStatement =  ifElseStatement or ifStatement
    val iterationStatement = whileR then leftParens then expressionRef before rightParens and statementRef map(ParseRules::whileStatement)
    val returnStatement = (returnR then semicolon map(ParseRules::emptyReturn)) or
            (returnR then expressionRef before semicolon map(ParseRules::varReturn))

    val expression = (variableRef and (assign then expressionRef) map(ParseRules::varAssign)) or simpleExpressionRef
    val variable = (id before leftBracket and expressionRef before rightBracket map(ParseRules::bracketVar)) or (id map(ParseRules::fromToken))

    val simpleExpression = (additiveExpressionRef and relopRef map(ParseRules::singleExprLeft) and
            additiveExpressionRef map(ParseRules::singleExprRight)) or additiveExpressionRef

    val relop = lessThanEquals or lessThan or moreThan or moreThanEquals or equals or notEquals
    val additiveExpression = (termRef and addOpRef map(ParseRules::singleExprLeft) and
            additiveExpressionRef map(ParseRules::singleExprRight)) or termRef
    val addop = plus or minus
    val term = (factorRef and mulopRef map(ParseRules::singleExprLeft)
            and termRef map(ParseRules::singleExprRight)) or factorRef
    val mulop = times or over
    val factor = callRef or variable or (number map(ParseRules::fromToken)) or (leftParens then expression before rightParens)


    val emptyCall = id before leftParens before rightParens map(ParseRules::callFunction)
    val paramCall = id and (leftParens then argListRef before rightParens) map(ParseRules::callFunction)
    val call = emptyCall or paramCall

    val argList = (expression before comma and argListRef map(ParseRules::parseArguments)) or expression

    fun getParser(): BaseParser<ParseState> {
        return program
    }




}