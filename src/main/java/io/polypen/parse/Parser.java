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

    public sealed interface Expr permits PlusExpr, MinusExpr, MultExpr, ListExpr, NumberExpr, VarExp, PlusListExpr, MultListExpr {
        int size();

        Expr getFirst();

        List<Expr> getExprs();
    }

    public static final class PlusExpr implements Expr {
        @Override
        public String toString() {
            return "+";
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Expr getFirst() {
            return null;
        }

        @Override
        public List<Expr> getExprs() {
            return List.of();
        }
    }

    public static final Expr PLUS = new PlusExpr();

    public static final class MinusExpr implements Expr {
        @Override
        public String toString() {
            return "-";
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Expr getFirst() {
            return null;
        }

        @Override
        public List<Expr> getExprs() {
            return List.of();
        }
    }

    public static final Expr MINUS = new MinusExpr();

    public static final class MultExpr implements Expr {
        @Override
        public String toString() {
            return "*";
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Expr getFirst() {
            return null;
        }

        @Override
        public List<Expr> getExprs() {
            return List.of();
        }
    }

    public static final Expr MULT = new MultExpr();

    public static Polynomial eval(Expr expr) {
        Expr exprs = Macro.applyStarMacro(expr.getExprs());
        return switch (exprs) {
            case PlusListExpr listExpr -> {
                if (listExpr.value.size() == 1) {
                    yield eval(listExpr.value().getFirst());
                }
                if (exprs.size() == 1) {
                    yield eval(exprs.getFirst());
                }
                Polynomial result = Polynomial.ZERO;
                int sign = 1;
                for (Expr exp : exprs.getExprs()) {
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
                yield result;
            }
            case MultListExpr listExpr -> {
                if (listExpr.value.size() == 1) {
                    yield eval(listExpr.value().getFirst());
                }
                if (exprs.size() == 1) {
                    yield eval(exprs.getFirst());
                }
                Polynomial result;
                result = Polynomial.ONE;
                for (Expr exp : exprs.getExprs()) {
                    if (isOperator(exp)) {
                        continue;
                    }
                    Polynomial p = eval(exp);
                    result = result.multiply(p);
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

    private static boolean hasPlus(Expr exprs) {
        for (Expr expr : exprs.getExprs()) {
            if (expr instanceof PlusExpr || expr instanceof MinusExpr) {
                return true;
            }
        }
        return false;
    }

    public record MultListExpr(List<Expr> value) implements Expr {
        public static MultListExpr of(Expr... value) {
            return new MultListExpr(List.of(value));
        }

        @Override
        public int size() {
            return value().size();
        }

        @Override
        public Expr getFirst() {
            return value.getFirst();
        }

        @Override
        public List<Expr> getExprs() {
            return value;
        }
    }

    public record PlusListExpr(List<Expr> value) implements Expr {
        public static PlusListExpr of(Expr... value) {
            return new PlusListExpr(List.of(value));
        }


        @Override
        public int size() {
            return value().size();
        }

        @Override
        public Expr getFirst() {
            return value.getFirst();
        }

        @Override
        public List<Expr> getExprs() {
            return value;
        }
    }

    public record ListExpr(List<Expr> value) implements Expr {
        public static ListExpr of(Expr... value) {
            return new ListExpr(List.of(value));
        }

        @Override
        public int size() {
            return value().size();
        }

        @Override
        public Expr getFirst() {
            return value.getFirst();
        }

        @Override
        public List<Expr> getExprs() {
            return value;
        }
    }

    public record NumberExpr(int value) implements Expr {
        public static NumberExpr of(int value) {
            return new NumberExpr(value);
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Expr getFirst() {
            return this;
        }

        @Override
        public List<Expr> getExprs() {
            return List.of(this);
        }
    }

    public record VarExp(String var, int exp) implements Expr {
        public static VarExp of(String var, int exp) {
            return new VarExp(var, exp);
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Expr getFirst() {
            return this;
        }

        @Override
        public List<Expr> getExprs() {
            return List.of(this);
        }
    }

    private Parser() {
    }
}
