public class LexError {
    public final String msg;
    public final int line;
    public final int col;

    public LexError(String msg, int line, int col) {
        this.msg = msg;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        return String.format("Erro léxico: %s (linha %d, coluna %d)", msg, line, col);
    }
}
