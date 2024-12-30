package io.polypen.parse;

import io.polypen.parse.Parser.Expr;
import io.polypen.parse.Parser.ListExpr;
import io.polypen.parse.Parser.MinusExpr;
import io.polypen.parse.Parser.MultExpr;
import io.polypen.parse.Parser.NumberExpr;
import io.polypen.parse.Parser.PlusExpr;
import io.polypen.parse.Parser.VarExp;

import java.util.ArrayList;
import java.util.List;

public class Macro {

    static ListExpr applyStarMacro(ListExpr listExpr) {
        List<Expr> exprs = listExpr.value();
        List<Expr> exprsCopy = new ArrayList<>(exprs.size());
        List<Expr> region = new ArrayList<>(exprs.size());
        Expr previous = null;
        for (int i = 0; i < exprs.size(); i++) {
            Expr expr = exprs.get(i);
            if (isStrongBind(previous) && (!isPlus(expr) || !region.isEmpty())) {
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
        if (region.isEmpty()) {
            exprsCopy.add(previous);
        } else {
            region.add(previous);
            exprsCopy.add(new ListExpr(region));
        }
        return new ListExpr(exprsCopy);
    }

    public static boolean isStrongBind(Expr expr) {
        if (expr == null) {
            return false;
        }
        return expr instanceof MultExpr || expr instanceof VarExp || expr instanceof NumberExpr;
    }

    public static boolean isPlus(Expr expr) {
        return expr instanceof PlusExpr || expr instanceof MinusExpr;
    }
}
