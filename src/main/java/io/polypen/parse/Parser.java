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
                    Expr atom = readAtom(reader);
                    result.add(atom);
                }
            }
        }
        return new ListExpr(result);
    }

    private static Expr readAtom(PushbackReader reader) throws IOException {
        consumeWhitespace(reader);
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            if (c != ' ' && c != '(' && c != ')') {
                sb.append((char) c);
            } else {
                reader.unread(c);
                break;
            }
        }
        return new Atom(sb.toString());
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

    public record Atom(String value) implements Expr {
    }

    public record ListExpr(List<Expr> values) implements Expr {
    }

    private Parser() {
    }
}
