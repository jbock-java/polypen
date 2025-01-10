package io.polypen;

import io.polypen.parse.Parser;
import io.polypen.parse.Parser.ListToken;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (in.hasNextLine()) {
            String line = in.nextLine();
            sb.append(line);
        }
        ListToken expression = Parser.parse(sb.toString());
        System.out.println(Parser.eval(expression));
    }
}
