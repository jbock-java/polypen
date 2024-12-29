package io.polypen.parse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.List;

public final class Parser {

    private static ListExpr parse(PushbackReader reader) throws IOException {
        consumeWhitespace(reader);
        List<Expr> result = new ArrayList<>();
        int c;
        loop:
        while ((c = reader.read()) != -1) {
            switch (c) {
                case '(' -> {
                    ListExpr expr = parse(reader);
                    result.add(expr);
                }
                case ')' -> {
                    break loop;
                }
                default -> {
                    reader.unread(c);
                    Expr word = readWord(reader);
                    result.add(word);
                }
            }
            consumeWhitespace(reader);
        }
        return new ListExpr(result);
    }

    private static Expr readWord(PushbackReader reader) throws IOException {
        int c = reader.read();
        if (c == -1) {
            return null;
        }
        if (c == '*') {
            return MULT;
        }
        if (c == '+') {
            return PLUS;
        }
        if (c == '-') {
            return MINUS;
        }
        reader.unread(c);
        if (Character.isDigit(c)) {
            return readNumber(reader);
        }
        return readVarExp(reader);
    }

    private static NumberExpr readNumber(PushbackReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while (Character.isDigit(c = reader.read())) {
            sb.append((char) c);
        }
        if (c != -1) {
            reader.unread(c);
        }
        return new NumberExpr(Integer.parseInt(sb.toString()));
    }

    private static VarExp readVarExp(PushbackReader reader) throws IOException {
        StringBuilder name = new StringBuilder();
        int c;
        while (true) {
            c = reader.read();
            if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_') {
                name.append((char) c);
            } else {
                break;
            }
        }
        if (c == '^') {
            NumberExpr expr = readNumber(reader);
            return new VarExp(name.toString(), expr.value);
        }
        if (c != -1) {
            reader.unread(c);
        }
        return new VarExp(name.toString(), 1);
    }

    private static void consumeWhitespace(PushbackReader reader) throws IOException {
        while (true) {
            int c = reader.read();
            if (c != ' ') {
                if (c != -1) {
                    reader.unread(c);
                }
                break;
            }
        }
    }

    public static ListExpr parse(String s) {
        try (PushbackReader reader = new PushbackReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes())))) {
            return parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public interface Expr {
    }

    public static final Expr PLUS = new Expr() {
        @Override
        public String toString() {
            return "+";
        }
    };

    public static final Expr MINUS = new Expr() {
        @Override
        public String toString() {
            return "-";
        }
    };

    public static final Expr MULT = new Expr() {
        @Override
        public String toString() {
            return "*";
        }
    };

    public record ListExpr(List<Expr> value) implements Expr {
    }

    public record NumberExpr(int value) implements Expr {
    }

    public record VarExp(String var, int exp) implements Expr {
    }

    private Parser() {
    }
}
