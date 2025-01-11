package io.polypen.parse;

import io.polypen.Monomial;
import io.polypen.Polynomial;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Parser {

    private static ListToken parse(PushbackReader reader) throws IOException {
        consumeWhitespace(reader);
        List<Token> result = new ArrayList<>();
        int c;
        loop:
        while ((c = reader.read()) != -1) {
            switch (c) {
                case '(' -> {
                    ListToken expr = parse(reader);
                    result.add(expr);
                }
                case ')' -> {
                    break loop;
                }
                default -> {
                    reader.unread(c);
                    Token word = readWord(reader);
                    result.add(word);
                }
            }
            consumeWhitespace(reader);
        }
        return new ListToken(result);
    }

    private static Token readWord(PushbackReader reader) throws IOException {
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
            int value = readNumber(reader);
            return VarExp.constant(value);
        }
        return readVarExp(reader);
    }

    private static int readNumber(PushbackReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while (Character.isDigit(c = reader.read())) {
            sb.append((char) c);
        }
        if (c != -1) {
            reader.unread(c);
        }
        return Integer.parseInt(sb.toString());
    }

    private static VarExp readVarExp(PushbackReader reader) throws IOException {
        int c;
        while (true) {
            c = reader.read();
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != '_') {
                break;
            }
        }
        if (c == '^') {
            return VarExp.of(readNumber(reader));
        }
        if (c != -1) {
            reader.unread(c);
        }
        return VarExp.of(1);
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

    public static ListToken parse(String s) {
        try (PushbackReader reader = new PushbackReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes())))) {
            return parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public sealed interface Token permits PlusToken, MinusToken, MultToken, ListToken, VarExp, PlusListToken, MultListToken {
        int size();

        Token getFirst();

        List<Token> getExprs();
    }

    public static final class PlusToken implements Token {
        @Override
        public String toString() {
            return "+";
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Token getFirst() {
            return this;
        }

        @Override
        public List<Token> getExprs() {
            return List.of(this);
        }
    }

    public static final Token PLUS = new PlusToken();

    public static final class MinusToken implements Token {
        @Override
        public String toString() {
            return "-";
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Token getFirst() {
            return this;
        }

        @Override
        public List<Token> getExprs() {
            return List.of(this);
        }
    }

    public static final Token MINUS = new MinusToken();

    public static final class MultToken implements Token {
        @Override
        public String toString() {
            return "*";
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Token getFirst() {
            return this;
        }

        @Override
        public List<Token> getExprs() {
            return List.of(this);
        }
    }

    public static final Token MULT = new MultToken();

    public static Polynomial eval(ListToken token) {
        Token exprs = Macro.applyStarMacro(token);
        return _eval(exprs);
    }

    private static Polynomial _eval(Token exprs) {
        return switch (exprs) {
            case PlusListToken listExpr -> {
                if (listExpr.value.size() == 1) {
                    yield _eval(listExpr.value().getFirst());
                }
                if (exprs.size() == 1) {
                    yield _eval(exprs.getFirst());
                }
                Polynomial result = Polynomial.ZERO;
                for (Token exp : exprs.getExprs()) {
                    if (isMinus(exp)) {
                        continue;
                    }
                    if (isPlus(exp)) {
                        continue;
                    }
                    Polynomial p = _eval(exp);
                    result = result.add(p);
                }
                yield result;
            }
            case MultListToken listExpr -> {
                if (listExpr.value.size() == 1) {
                    yield _eval(listExpr.value().getFirst());
                }
                if (exprs.size() == 1) {
                    yield _eval(exprs.getFirst());
                }
                Polynomial result;
                result = Polynomial.ONE;
                for (Token exp : exprs.getExprs()) {
                    if (isOperator(exp)) {
                        continue;
                    }
                    Polynomial p = _eval(exp);
                    result = result.multiply(p);
                }
                yield result;
            }
            case VarExp varExp -> new Monomial(varExp.factor, varExp.exp).polynomial();
            default -> throw new IllegalStateException(exprs.toString());
        };
    }

    private static boolean isOperator(Token token) {
        return switch (token) {
            case MinusToken ignored -> true;
            case MultToken ignored -> true;
            case PlusToken ignored -> true;
            default -> false;
        };
    }

    private static boolean isPlus(Token token) {
        return token instanceof PlusToken;
    }

    private static boolean isMinus(Token token) {
        return token instanceof MinusToken;
    }

    public record MultListToken(List<Token> value) implements Token {
        public static MultListToken create(int capacity) {
            return new MultListToken(new ArrayList<>(capacity));
        }

        public static MultListToken of(Token... value) {
            return new MultListToken(List.of(value));
        }

        @Override
        public String toString() {
            return value.stream().map(Objects::toString).collect(Collectors.joining(" ", "(* ", ")"));
        }

        public static MultListToken of(int... value) {
            List<Token> list = IntStream.of(value).mapToObj(value1 -> VarExp.constant(value1)).map(s -> (Token) s).toList();
            return new MultListToken(list);
        }

        public void add(Token token) {
            addIfNotOperator(value, token);
        }

        public MultListToken copy() {
            return new MultListToken(List.copyOf(value));
        }

        public void clear() {
            value.clear();
        }

        public boolean isEmpty() {
            return value.isEmpty();
        }

        @Override
        public int size() {
            return value().size();
        }

        @Override
        public Token getFirst() {
            return value.getFirst();
        }

        @Override
        public List<Token> getExprs() {
            return value;
        }
    }

    private static void addIfNotOperator(List<Token> tokens, Token token) {
        if (!isOperator(token)) {
            tokens.add(token);
        }
    }

    public record PlusListToken(List<Token> value) implements Token {
        public static PlusListToken create(int capacity) {
            return new PlusListToken(new ArrayList<>(capacity));
        }

        @Override
        public String toString() {
            return value.stream().map(Objects::toString).collect(Collectors.joining(" ", "(+ ", ")"));
        }

        public static PlusListToken of(Token... value) {
            return new PlusListToken(List.of(value));
        }

        public void add(Token token) {
            addIfNotOperator(value, token);
        }

        public boolean isEmpty() {
            return value.isEmpty();
        }

        @Override
        public int size() {
            return value().size();
        }

        @Override
        public Token getFirst() {
            return value.getFirst();
        }

        @Override
        public List<Token> getExprs() {
            return value;
        }
    }

    public record ListToken(List<Token> value) implements Token {
        public static ListToken of(Token... value) {
            return new ListToken(List.of(value));
        }

        @Override
        public int size() {
            return value().size();
        }

        @Override
        public Token getFirst() {
            return value.getFirst();
        }

        @Override
        public List<Token> getExprs() {
            return value;
        }
    }

    public record VarExp(int factor, int exp) implements Token {
        public static VarExp constant(int factor) {
            return new VarExp(factor, 0);
        }

        public static VarExp of(int exp) {
            return new VarExp(1, exp);
        }

        @Override
        public String toString() {
            if (exp == 0) {
                return Integer.toString(factor);
            }
            if (exp == 1) {
                return "x";
            }
            return "x^" + exp;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Token getFirst() {
            return this;
        }

        @Override
        public List<Token> getExprs() {
            return List.of(this);
        }
    }

    private Parser() {
    }
}
