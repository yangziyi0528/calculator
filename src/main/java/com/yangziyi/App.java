package com.yangziyi;

/**
 * Command-line entry point for the calculator.
 * Usage: java -jar calculator-1.0.jar &lt;number1&gt; &lt;operator&gt; &lt;number2&gt;
 */
public class App {

    /**
     * Parses three command-line arguments (two numbers and an operator)
     * and prints the result.
     *
     * @param args number1, operator, number2
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java -jar calculator-1.0.jar "
                    + "<number1> <operator> <number2>");
            return;
        }
        Calculator c = new Calculator();
        System.out.println(c.eval(args[0], args[1], args[2]));
    }
}
