package io.polypen.parse;

import org.junit.jupiter.api.Test;

class ParserTest {

    @Test
    void parse() {
        Parser.ListExpr result = Parser.parse("(a_1^12 + b12^2) * 2");
        System.out.println(result);
    }
}
