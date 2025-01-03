package io.polypen.parse;

import io.polypen.Monomial;
import io.polypen.Polynomial;

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

    public sealed interface Expr permits PlusExpr, MinusExpr, MultExpr, ListExpr, NumberExpr, VarExp {
    }

    public static final class PlusExpr implements Expr {
        @Override
        public String toString() {
            return "+";
        }
    }

    public static final Expr PLUS = new PlusExpr();

    public static final class MinusExpr implements Expr {
        @Override
        public String toString() {
            return "-";
        }
    }

    public static final Expr MINUS = new MinusExpr();

    public static final class MultExpr implements Expr {
        @Override
        public String toString() {
            return "*";
        }
    }

    public static final Expr MULT = new MultExpr();

    public static Polynomial eval(Expr expr) {
        return switch (expr) {
            case ListExpr listExpr -> {
                if (listExpr.value.size() == 1) {
                    yield eval(listExpr.value().getFirst());
                }
                List<Expr> exprs = Macro.applyStarMacro(listExpr);
                if (exprs.size() == 1) {
                    yield eval(exprs.getFirst());
                }
                Polynomial result;
                if (hasPlus(exprs)) {
                    result = Polynomial.ZERO;
                    int sign = 1;
                    for (Expr exp : exprs) {
                        if (isMinus(exp)) {
                            sign = -1;
                            continue;
                        }
                        if (isPlus(exp)) {
                            sign = 1;
                            continue;
                        }
                        Polynomial p = eval(exp);
                        result = result.add(p.multiply(sign));
                    }
                } else {
                    result = Polynomial.ONE;
                    for (Expr exp : exprs) {
                        if (isOperator(exp)) {
                            continue;
                        }
                        Polynomial p = eval(exp);
                        result = result.multiply(p);
                    }
                }
                yield result;
            }
            case NumberExpr numberExpr -> new Monomial(numberExpr.value, 0).polynomial();
            case VarExp varExp -> new Monomial(1, varExp.exp).polynomial();
            default -> throw new IllegalStateException(expr.toString());
        };
    }

    private static boolean isOperator(Expr expr) {
        return switch (expr) {
            case MinusExpr ignored -> true;
            case MultExpr ignored -> true;
            case PlusExpr ignored -> true;
            default -> false;
        };
    }

    private static boolean isPlus(Expr expr) {
        return expr instanceof PlusExpr;
    }

    private static boolean isMinus(Expr expr) {
        return expr instanceof MinusExpr;
    }

    private static boolean hasPlus(List<Expr> exprs) {
        for (Expr expr : exprs) {
            if (expr instanceof PlusExpr || expr instanceof MinusExpr) {
                return true;
            }
        }
        return false;
    }

    public record ListExpr(List<Expr> value) implements Expr {
        public static ListExpr of(Expr... value) {
            return new ListExpr(List.of(value));
        }

    }

    public record NumberExpr(int value) implements Expr {
        public static NumberExpr of(int value) {
            return new NumberExpr(value);
        }
    }

    public record VarExp(String var, int exp) implements Expr {
        public static VarExp of(String var, int exp) {
            return new VarExp(var, exp);
        }
    }

    private Parser() {
    }
}
