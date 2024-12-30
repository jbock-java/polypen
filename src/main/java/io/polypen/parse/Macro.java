package io.polypen.parse;

import io.polypen.parse.Parser.Expr;
import io.polypen.parse.Parser.ListExpr;
import io.polypen.parse.Parser.MultExpr;
import io.polypen.parse.Parser.NumberExpr;
import io.polypen.parse.Parser.VarExp;

import java.util.ArrayList;
import java.util.List;

public class Macro {

    static List<Expr> applyStarMacro(ListExpr listExpr) {
        List<Expr> exprs = listExpr.value();
        List<Expr> exprsCopy = new ArrayList<>(exprs.size());
        List<Expr> region = new ArrayList<>(exprs.size());
        Expr previous = null;
        for (Expr expr : exprs) {
            if (isStrongBind(previous) && (isStrongBind(expr) || !region.isEmpty())) {
                region.add(previous);
            } else {
                if (!region.isEmpty()) {
                    exprsCopy.add(new ListExpr(new ArrayList<>(region)));
                    region.clear();
                }
                if (previous != null) {
                    exprsCopy.add(previous);
                }
            }
            previous = expr;
        }
        if (exprsCopy.isEmpty()) {
            return listExpr.value();
        }
        if (region.isEmpty()) {
            exprsCopy.add(previous);
        } else {
            region.add(previous);
            exprsCopy.add(new ListExpr(region));
        }
        return exprsCopy;
    }

    public static boolean isStrongBind(Expr expr) {
        if (expr == null) {
            return false;
        }
        return switch (expr) {
            case ListExpr ignored -> true;
            case MultExpr ignored -> true;
            case NumberExpr ignored -> true;
            case VarExp ignored -> true;
            default -> false;
        };
    }
}
