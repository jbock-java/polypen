package io.polypen.parse;

import io.polypen.parse.Parser.ListToken;
import io.polypen.parse.Parser.MinusToken;
import io.polypen.parse.Parser.MultListToken;
import io.polypen.parse.Parser.MultToken;
import io.polypen.parse.Parser.PlusListToken;
import io.polypen.parse.Parser.PlusToken;
import io.polypen.parse.Parser.Token;
import io.polypen.parse.Parser.VarExp;

import java.util.List;

public class Macro {

    public static final int B_STRONG = 4;
    public static final int B_MINUSBOUND = 16;
    public static final int B_END = 1;

    public static Token applyStarMacro(Token input) {
        if (!(input instanceof ListToken)) {
            return input;
        }
        List<Token> tokens = input.getExprs();
        if (tokens.size() == 1) {
            return applyStarMacro(tokens.getFirst());
        }
        PlusListToken exprsCopy = PlusListToken.create(tokens.size());
        MultListToken region = MultListToken.create(tokens.size());
        int[] bound = new int[tokens.size()];
        for (int i = 0; i < tokens.size() - 1; i++) {
            Token left = tokens.get(i);
            Token right = tokens.get(i + 1);
            if (isStrong(left, right)) {
                bound[i] |= B_STRONG;
                bound[i + 1] |= B_STRONG;
                if (left instanceof MinusToken) {
                    bound[i + 1] |= B_MINUSBOUND;
                }
            } else if ((bound[i] & B_STRONG) != 0) {
                bound[i] |= B_END;
            }
        }
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            int b = bound[i];
            if ((b & B_STRONG) != 0) {
                if ((b & B_MINUSBOUND) != 0) {
                    region.add(MultListToken.of(VarExp.constant(-1), applyStarMacro(token)));
                } else {
                    region.add(applyStarMacro(token));
                }
                if ((b & B_END) != 0) {
                    exprsCopy.add(unwrap(region.copy()));
                    region.clear();
                }
            } else {
                if (!region.isEmpty()) {
                    exprsCopy.add(unwrap(region.copy()));
                    region.clear();
                }
                exprsCopy.add(applyStarMacro(token));
            }
        }
        if (exprsCopy.isEmpty()) {
            return unwrap(region);
        }
        if (!region.isEmpty()) {
            exprsCopy.add(unwrap(region));
        }
        return exprsCopy;
    }

    private static Token unwrap(MultListToken expr) {
        return expr.size() == 1 ? expr.getFirst() : expr;
    }

    public static boolean isStrong(Token left, Token right) {
        if (left instanceof MultToken || right instanceof MultToken) {
            return true;
        }
        if (left instanceof PlusToken || right instanceof PlusToken) {
            return false;
        }
        if (left instanceof MinusToken) {
            return true;
        }
        if (right instanceof MinusToken) {
            return false;
        }
        return true;
    }
}
