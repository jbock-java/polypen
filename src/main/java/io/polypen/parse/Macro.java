package io.polypen.parse;

import io.polypen.parse.Parser.BindingMinusExpr;
import io.polypen.parse.Parser.Expr;
import io.polypen.parse.Parser.ListExpr;
import io.polypen.parse.Parser.MinusExpr;
import io.polypen.parse.Parser.MultExpr;
import io.polypen.parse.Parser.MultListExpr;
import io.polypen.parse.Parser.PlusExpr;
import io.polypen.parse.Parser.PlusListExpr;

import java.util.ArrayList;
import java.util.List;

public class Macro {

    public static Expr minusMacro(Expr exprs) {
        if (exprs.size() == 1) {
            return exprs;
        }
        Expr previous = null;
        List<Expr> result = new ArrayList<>(exprs.size());
        for (Expr expr : exprs.getExprs()) {
            if (previous instanceof MinusExpr) {
                result.add(BindingMinusExpr.of(minusMacro(expr)));
            } else {
                if (!(expr instanceof MinusExpr)) {
                    result.add(minusMacro(expr));
                }
            }
            previous = expr;
        }
        return new ListExpr(result);
    }

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
                bound[i] = 1;
                bound[i + 1] = 1;
            } else {
                bound[i] *= 2;
            }
        }
        for (int i = 0; i < exprs.size(); i++) {
            Expr expr = exprs.get(i);
            if (bound[i] == 1) {
                region.add(expandRecursively(expr));
            } else if (bound[i] == 2) {
                region.add(expandRecursively(expr));
                exprsCopy.add(region.copy());
                region.clear();
            } else {
                if (!region.isEmpty()) {
                    exprsCopy.add(region.copy());
                    region.clear();
                }
                exprsCopy.add(expandRecursively(expr));
            }
        }
        if (exprsCopy.isEmpty()) {
            return region;
        }
        if (!region.isEmpty()) {
            exprsCopy.add(region);
        }
        return exprsCopy;
    }

    private static Expr expandRecursively(Expr expr) {
        if (expr == null) {
            return null;
        }
        return switch (expr) {
            case ListExpr x -> applyStarMacro(x.value());
            case BindingMinusExpr x -> BindingMinusExpr.of(expandRecursively(x.expr()));
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
        if (right instanceof BindingMinusExpr) {
            return false;
        }
        return true;
    }
}
