import java.util.*;

public class ScannerLexico {

    private static final Set<String> RESERVED = new HashSet<>(Arrays.asList(
        "main", "int", "float", "char", "if", "else", "while", "do", "for"
    ));

    private static final Set<Character> MARKERS =
        new HashSet<>(Arrays.asList('(', ')', '{', '}', ',', ';'));

    private final String text;
    private int i = 0;
    private int line = 1, col = 1;

    public final List<Token> tokens = new ArrayList<>();
    public final List<LexError> errors = new ArrayList<>();

    public ScannerLexico(String src) {
        this.text = src;
    }

    private char current() {
        return (i < text.length() ? text.charAt(i) : '\0');
    }

    private char peek(int k) {
        int pos = i + k;
        return (pos < text.length() ? text.charAt(pos) : '\0');
    }

    private void advance() {
        if (i < text.length()) {
            if (text.charAt(i) == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
            i++;
        }
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(current())) advance();
    }

    private boolean isLetterOrUnderscore(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isLetterDigitOrUnderscore(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    // Identificadores e Reservadas
    private void scanIdentifier() {
        int startCol = col;
        StringBuilder sb = new StringBuilder();

        while (isLetterDigitOrUnderscore(current())) {
            sb.append(current());
            advance();
        }

        String lex = sb.toString();

        if (RESERVED.contains(lex))
            tokens.add(new Token("RESERVED", lex, line, startCol));
        else
            tokens.add(new Token("IDENTIFIER", lex, line, startCol));
    }

    // Números
    private void scanNumber() {
        int startCol = col;
        StringBuilder sb = new StringBuilder();

        while (Character.isDigit(current())) {
            sb.append(current());
            advance();
        }

        if (current() == '.') {
            sb.append('.');
            advance();

            if (!Character.isDigit(current())) {
                errors.add(new LexError("Float mal formado", line, col));
                return;
            }

            while (Character.isDigit(current())) {
                sb.append(current());
                advance();
            }

            tokens.add(new Token("REAL_CONST", sb.toString(), line, startCol));
        } else {
            tokens.add(new Token("INT_CONST", sb.toString(), line, startCol));
        }
    }

    // Char literal
    private void scanChar() {
        int startCol = col;
        advance(); // abre '

        char c = current();

        if (c == '\0' || c == '\n') {
            errors.add(new LexError("Constante de caractere mal formada", line, startCol));
            return;
        }

        StringBuilder sb = new StringBuilder();

        if (c == '\\') {
            sb.append('\\');
            advance();
            if (current() == '\0' || current() == '\n') {
                errors.add(new LexError("Constante de caractere mal formada (escape incompleto)", line, col));
                return;
            }
            sb.append(current());
            advance();
        } else {
            sb.append(c);
            advance();
        }

        if (current() != '\'') {
            errors.add(new LexError("Constante de caractere mal formada (falta fechamento)", line, col));
            return;
        }

        advance(); // fecha '
        tokens.add(new Token("CHAR_CONST", "'" + sb + "'", line, startCol));
    }

    // Comentário linha
    private void scanCommentLine() {
        advance(); advance(); // //
        while (current() != '\n' && current() != '\0') advance();
    }

    // Comentário bloco
    private void scanCommentBlock() {
        int startLine = line, startCol = col;
        advance(); advance(); // /*

        while (true) {
            if (current() == '\0') {
                errors.add(new LexError("Comentário de bloco não encerrado", startLine, startCol));
                return;
            }
            if (current() == '*' && peek(1) == '/') {
                advance(); advance();
                return;
            }
            advance();
        }
    }

    public void scan() {
        while (current() != '\0') {
            skipWhitespace();

            char c = current();
            int startCol = col;

            if (c == '\0') break;

            if (isLetterOrUnderscore(c)) {
                scanIdentifier();
                continue;
            }

            if (Character.isDigit(c)) {
                scanNumber();
                continue;
            }

            if (c == '\'') {
                scanChar();
                continue;
            }

            if (c == '/') {
                if (peek(1) == '/') {
                    scanCommentLine();
                    continue;
                }
                if (peek(1) == '*') {
                    scanCommentBlock();
                    continue;
                }
                tokens.add(new Token("ARITH_OP", "/", line, col));
                advance();
                continue;
            }

            if (c == '+' || c == '-' || c == '*') {
                tokens.add(new Token("ARITH_OP", String.valueOf(c), line, col));
                advance();
                continue;
            }

            if (c == '<' || c == '>') {
                if (peek(1) == '=') {
                    tokens.add(new Token("REL_OP", "" + c + "=", line, col));
                    advance(); advance();
                } else {
                    tokens.add(new Token("REL_OP", String.valueOf(c), line, col));
                    advance();
                }
                continue;
            }

            if (c == '=') {
                if (peek(1) == '=') {
                    tokens.add(new Token("REL_OP", "==", line, col));
                    advance(); advance();
                } else {
                    tokens.add(new Token("ASSIGN", "=", line, col));
                    advance();
                }
                continue;
            }

            if (c == '!') {
                if (peek(1) == '=') {
                    tokens.add(new Token("REL_OP", "!=", line, col));
                    advance(); advance();
                } else {
                    errors.add(new LexError("Exclamação isolada (!)", line, col));
                    advance();
                }
                continue;
            }

            if (MARKERS.contains(c)) {
                tokens.add(new Token("MARKER", String.valueOf(c), line, col));
                advance();
                continue;
            }

            errors.add(new LexError("Caracter inválido '" + c + "'", line, col));
            advance();
        }

        tokens.add(new Token("EOF", "EOF", line, col));
    }
}
