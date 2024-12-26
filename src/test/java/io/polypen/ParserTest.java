package io.polypen;

import org.junit.jupiter.api.Test;

import static io.polypen.Parser.Type.PRODUCT;
import static io.polypen.Parser.Type.SUM;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {

    @Test
    void outerOp() {
        assertEquals(PRODUCT, Parser.outerop(""));
        assertEquals(PRODUCT, Parser.outerop("1"));
        assertEquals(PRODUCT, Parser.outerop("x"));
        assertEquals(PRODUCT, Parser.outerop("1 + x"));
        assertEquals(PRODUCT, Parser.outerop("(1 + x)"));
        assertEquals(PRODUCT, Parser.outerop("(1 + x) (2 + x)"));
        assertEquals(PRODUCT, Parser.outerop("(1 + x) * (2 + x)"));
        assertEquals(SUM, Parser.outerop("(1 + x) + (2 + x)"));
    }
}
