package io.polypen;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static Polynomial evalProduct(String s) {
        List<Polynomial> polynomials = Parser.parseProduct(s).stream().map(Polynomial::parse).toList();
        Polynomial result = Polynomial.ONE;
        for (Polynomial p : polynomials) {
            result = result.multiply(p);
        }
        return result;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (in.hasNextLine()) {
            String line = in.nextLine();
            sb.append(line);
        }
        System.out.println(evalProduct(sb.toString()));
    }
}
