package io.polypen.parse;

import io.polypen.parse.Parser.BindingMinusExpr;
import io.polypen.parse.Parser.Expr;
import io.polypen.parse.Parser.ListExpr;
import io.polypen.parse.Parser.MinusExpr;
import io.polypen.parse.Parser.MultExpr;
import io.polypen.parse.Parser.MultListExpr;
import io.polypen.parse.Parser.NumberExpr;
import io.polypen.parse.Parser.PlusExpr;
import io.polypen.parse.Parser.PlusListExpr;
import io.polypen.parse.Parser.VarExp;

import java.util.ArrayList;
import java.util.List;

public class Macro {

    static Expr minusMacro(Expr exprs) {
        if (exprs.size() == 1) {
            return exprs;
        }
        Expr old = null;
        Expr previous = null;
        List<Expr> result = new ArrayList<>(exprs.size());
        for (Expr expr : exprs.getExprs()) {
            if (previous instanceof MinusExpr) {
                if (needsPlusInsert(old)) {
                    result.add(Parser.PLUS);
                }
                result.add(BindingMinusExpr.of(minusMacro(expr)));
            } else {
                if (!(expr instanceof MinusExpr)) {
                    result.add(minusMacro(expr));
                }
            }
            old = previous;
            previous = expr;
        }
        return new ListExpr(result);
    }

    private static boolean needsPlusInsert(Expr prev) {
        if (prev == null) {
            return false;
        }
        return switch (prev) {
            case PlusExpr ignored -> false;
            case MultExpr ignored -> false;
            default -> true;
        };
    }

    static Expr applyStarMacro(List<Expr> exprs) {
        if (exprs.size() == 1) {
            return expandRecursively(exprs.getFirst());
        }
        PlusListExpr exprsCopy = PlusListExpr.create(exprs.size());
        MultListExpr region = MultListExpr.create(exprs.size());
        Expr previous = null;
        for (Expr expr : exprs) {
            if (isStrongBind(previous) && (isStrongBind(expr) || !region.isEmpty())) {
                region.add(previous);
            } else {
                if (!region.isEmpty()) {
                    exprsCopy.add(region.copy());
                    region.clear();
                }
                if (previous != null) {
                    exprsCopy.add(previous);
                }
            }
            previous = expandRecursively(expr);
        }
        if (exprsCopy.isEmpty()) {
            region.add(expandRecursively(previous));
            return region;
        }
        if (region.isEmpty()) {
            exprsCopy.add(expandRecursively(previous));
        } else {
            region.add(expandRecursively(previous));
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
            case BindingMinusExpr x -> BindingMinusExpr.of(applyStarMacro(List.of(x.expr())));
            default -> expr;
        };
    }

    public static boolean isStrongBind(Expr expr) {
        if (expr == null) {
            return false;
        }
        return switch (expr) {
            case ListExpr ignored -> true;
            case PlusListExpr ignored -> true;
            case MultListExpr ignored -> true;
            case MultExpr ignored -> true;
            case NumberExpr ignored -> true;
            case VarExp ignored -> true;
            case BindingMinusExpr ignored -> true;
            default -> false;
        };
    }
}
