package io.polypen.parse;

import io.polypen.parse.Parser.ListExpr;
import io.polypen.parse.Parser.NumberExpr;
import io.polypen.parse.Parser.VarExp;
import org.junit.jupiter.api.Test;

import static io.polypen.parse.Macro.applyStarMacro;
import static io.polypen.parse.Parser.MULT;
import static io.polypen.parse.Parser.PLUS;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {

    @Test
    void parse() {
        ListExpr result = Parser.parse("(a_1^12 + b12^2) * 2");
        assertEquals(ListExpr.of(
                        ListExpr.of(VarExp.of("a_1", 12), PLUS, VarExp.of("b12", 2)), MULT, NumberExpr.of(2)),
                result);
    }

    @Test
    void starMacro1() {
        ListExpr result = Parser.parse("1 + 2 * 3");
        ListExpr expanded = applyStarMacro(result);
        assertEquals(ListExpr.of(
                        NumberExpr.of(1), PLUS, ListExpr.of(NumberExpr.of(2), MULT, NumberExpr.of(3))),
                expanded);
    }

    @Test
    void starMacro2() {
        ListExpr result = Parser.parse("1 + 2 * 3 + 4");
        ListExpr expanded = applyStarMacro(result);
        assertEquals(ListExpr.of(
                        NumberExpr.of(1), PLUS, ListExpr.of(NumberExpr.of(2), MULT, NumberExpr.of(3)), PLUS, NumberExpr.of(4)),
                expanded);
    }
}
