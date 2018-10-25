import me.sargunvohra.lib.cakeparse.lexer.TokenInstance

data class ParseState(
    val code: String,
    val data: String,
    val temporal: Int,
    val label: Int
) {

    companion object {
        val Empty = ParseState("", "", 0, 0)
    }

}