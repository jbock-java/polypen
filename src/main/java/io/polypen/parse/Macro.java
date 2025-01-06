package io.polypen.parse;

import io.polypen.parse.Parser.Expr;
import io.polypen.parse.Parser.ListExpr;
import io.polypen.parse.Parser.MultExpr;
import io.polypen.parse.Parser.MultListExpr;
import io.polypen.parse.Parser.NumberExpr;
import io.polypen.parse.Parser.PlusListExpr;
import io.polypen.parse.Parser.VarExp;

import java.util.ArrayList;
import java.util.List;

public class Macro {

    static Expr applyStarMacro(List<Expr> exprs) {
        if (exprs.size() == 1) {
            return expandRecursively(exprs.getFirst());
        }
        List<Expr> exprsCopy = new ArrayList<>(exprs.size());
        List<Expr> region = new ArrayList<>(exprs.size());
        Expr previous = null;
        for (Expr expr : exprs) {
            if (isStrongBind(previous) && (isStrongBind(expr) || !region.isEmpty())) {
                region.add(previous);
            } else {
                if (!region.isEmpty()) {
                    exprsCopy.add(new MultListExpr(new ArrayList<>(region)));
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
            return new MultListExpr(region);
        }
        if (region.isEmpty()) {
            exprsCopy.add(expandRecursively(previous));
        } else {
            region.add(expandRecursively(previous));
            exprsCopy.add(new MultListExpr(region));
        }
        return new PlusListExpr(exprsCopy);
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
            default -> false;
        };
    }
}
