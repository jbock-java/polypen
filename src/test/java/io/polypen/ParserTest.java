package io.polypen;

import io.polypen.Expressions.Expression;
import io.polypen.Expressions.Product;
import io.polypen.Expressions.Sum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.polypen.Parser.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ParserTest {

    @Test
    void parseProduct() {
        Expression expression = parse("(a + 1) * (a - 1)");
        assertInstanceOf(Product.class, expression);
        Product product = (Product) expression;
        assertEquals(List.of("(a + 1)", "(a - 1)"), product.factors().stream().toList());
    }

    @Test
    void parseSum() {
        Expression expression = parse("(a + 1) + (a - 1)");
        assertInstanceOf(Sum.class, expression);
        Sum sum = (Sum) expression;
        assertEquals(List.of("(a + 1)", "+(a - 1)"), sum.terms().stream().toList());
    }

    @Test
    void parseDifference() {
        assertEquals(parse("2").eval(),
                parse("(a + 1) - (a - 1)").eval());
    }

    @Test
    void negativeLiteral() {
        assertEquals(parse("-x - 1").eval(),
                parse("-(x + 1)").eval());
    }

    @Test
    void doubleNegative() {
        assertEquals(parse("-x + 1").eval(),
                parse("-(x - 1)").eval());
    }
}
