import me.sargunvohra.lib.cakeparse.lexer.TokenInstance

data class ParseState(
    val code: String,
    val token: TokenInstance,
    val temporal: Int,
    val label: Int
)