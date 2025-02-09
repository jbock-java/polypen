package io.polypen.parse;

import io.polypen.parse.Parser.HeadToken;
import io.polypen.parse.Parser.ListToken;
import io.polypen.parse.Parser.MinusToken;
import io.polypen.parse.Parser.MultToken;
import io.polypen.parse.Parser.PlusToken;
import io.polypen.parse.Parser.Token;
import io.polypen.parse.Parser.VarExp;

import java.util.List;

import static io.polypen.parse.Parser.HeadToken.createMult;
import static io.polypen.parse.Parser.HeadToken.createPlus;

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
        HeadToken exprsCopy = createPlus(tokens.size());
        HeadToken region = createMult(tokens.size());
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
            Token transformed = applyStarMacro(token);
            if ((b & B_STRONG) != 0) {
                if ((b & B_MINUSBOUND) != 0) {
                    HeadToken neg = HeadToken.ofMult(VarExp.constant(-1), transformed);
                    region.add(neg);
                } else {
                    region.add(transformed);
                }
                if ((b & B_END) != 0) {
                    exprsCopy.add(unwrap(region.copy()));
                    region.clear();
                }
            } else {
                exprsCopy.add(transformed);
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

    private static Token unwrap(HeadToken expr) {
        return expr.size() == 1 ? expr.getFirst() : expr;
    }

    enum Troolean {
        TRUE(true), FALSE(false), DONT_CARE(true),
        ;
        final boolean b;

        Troolean(boolean b) {
            this.b = b;
        }
    }

    private static Troolean isRightStrong(Token right) {
        if (right instanceof MultToken) {
            return Troolean.TRUE;
        }
        if (right instanceof PlusToken) {
            return Troolean.FALSE;
        }
        if (right instanceof MinusToken) {
            return Troolean.FALSE;
        }
        return Troolean.DONT_CARE;
    }

    private static Troolean isLeftStrong(Token left) {
        if (left instanceof MultToken) {
            return Troolean.TRUE;
        }
        if (left instanceof PlusToken) {
            return Troolean.FALSE;
        }
        if (left instanceof MinusToken) {
            return Troolean.TRUE;
        }
        return Troolean.DONT_CARE;
    }

    public static boolean isStrong(Token left, Token right) {
        Troolean l = isLeftStrong(left);
        if (l != Troolean.DONT_CARE) {
            return l.b;
        }
        Troolean r = isRightStrong(right);
        return r.b;
    }
}
