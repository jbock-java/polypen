package io.polypen.parse;

import org.junit.jupiter.api.Test;

class ParserTest {

    @Test
    void parse() {
        Parser.ListExpr result = Parser.parse("(a b) 2 3");
        System.out.println(result);
    }
}
