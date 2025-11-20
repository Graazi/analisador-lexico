public class Token {
    public final String type;
    public final String lexeme;
    public final int line;
    public final int col;

    public Token(String type, String lexeme, int line, int col) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        return String.format("%-12s '%s'  (l%d:c%d)", type, lexeme, line, col);
    }
}

