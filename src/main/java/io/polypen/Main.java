package io.polypen;

import io.polypen.Expressions.Expression;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (in.hasNextLine()) {
            String line = in.nextLine();
            sb.append(line);
        }
        Expression expression = Parser.parseProduct(sb.toString());
        System.out.println(expression.eval());
    }
}
