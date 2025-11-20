import java.io.*;

public class main {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Uso: java Main <arquivo_fonte>");
            return;
        }

        String fileName = args[0];
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append("\n");
        } catch (Exception e) {
            System.out.println("Erro ao abrir arquivo: " + e.getMessage());
            return;
        }

        ScannerLexico scanner = new ScannerLexico(sb.toString());
        scanner.scan();

        System.out.println("---- Tokens reconhecidos ----");
        for (Token t : scanner.tokens) {
            System.out.println(t);
        }

        if (!scanner.errors.isEmpty()) {
            System.out.println("\nCompilação: FALHOU");
            for (LexError e : scanner.errors) {
                System.out.println(e);
            }
        } else {
            System.out.println("\nCompilação: SUCESSO. Nenhum erro léxico encontrado.");
        }
    }
}
