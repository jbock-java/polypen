package io.polypen.parse;

import io.polypen.parse.Parser.Expr;
import io.polypen.parse.Parser.ListExpr;
import io.polypen.parse.Parser.MinusExpr;
import io.polypen.parse.Parser.MultExpr;
import io.polypen.parse.Parser.MultListExpr;
import io.polypen.parse.Parser.NumberExpr;
import io.polypen.parse.Parser.PlusExpr;
import io.polypen.parse.Parser.PlusListExpr;

import java.util.List;

public class Macro {

    public static final int B_STRONG = 4;
    public static final int B_MINUSBOUND = 16;
    public static final int B_END = 1;

    public static Expr applyStarMacro(List<Expr> exprs) {
        if (exprs.size() == 1) {
            return expandRecursively(exprs.getFirst());
        }
        PlusListExpr exprsCopy = PlusListExpr.create(exprs.size());
        MultListExpr region = MultListExpr.create(exprs.size());
        int[] bound = new int[exprs.size()];
        for (int i = 0; i < exprs.size() - 1; i++) {
            Expr left = exprs.get(i);
            Expr right = exprs.get(i + 1);
            if (isStrong(left, right)) {
                bound[i] |= B_STRONG;
                bound[i + 1] |= B_STRONG;
                if (left instanceof MinusExpr) {
                    bound[i + 1] |= B_MINUSBOUND;
                }
            } else if ((bound[i] & B_STRONG) != 0) {
                bound[i] |= B_END;
            }
        }
        for (int i = 0; i < exprs.size(); i++) {
            Expr expr = exprs.get(i);
            int b = bound[i];
            if ((b & B_STRONG) != 0) {
                if ((b & B_MINUSBOUND) != 0) {
                    region.add(MultListExpr.of(NumberExpr.of(-1), expandRecursively(expr)));
                } else {
                    region.add(expandRecursively(expr));
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
                exprsCopy.add(expandRecursively(expr));
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

    private static Expr unwrap(MultListExpr expr) {
        return expr.size() == 1 ? expr.getFirst() : expr;
    }

    private static Expr expandRecursively(Expr expr) {
        if (expr == null) {
            return null;
        }
        return switch (expr) {
            case ListExpr x -> applyStarMacro(x.value());
            default -> expr;
        };
    }

    public static boolean isStrong(Expr left, Expr right) {
        if (left instanceof MultExpr || right instanceof MultExpr) {
            return true;
        }
        if (left instanceof PlusExpr || right instanceof PlusExpr) {
            return false;
        }
        if (left instanceof MinusExpr) {
            return true;
        }
        if (right instanceof MinusExpr) {
            return false;
        }
        return true;
    }
}
