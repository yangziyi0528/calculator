package com.yangziyi;

import org.apache.commons.lang3.StringUtils;

/**
 * A calculator that evaluates a binary expression given as two operands
 * and one operator. Supported operators: +, -, *, /, ^ (power) and % (modulo).
 */
public class Calculator {

    /**
     * Evaluates the expression {@code a op b}.
     *
     * @param a  first operand as a string
     * @param op operator (+, -, *, /, ^, %)
     * @param b  second operand as a string
     * @return the numeric result
     * @throws IllegalArgumentException if an operand is blank or the operator is unknown
     * @throws ArithmeticException      if division by zero is attempted
     */
    public double eval(String a, String op, String b) {
        if (StringUtils.isBlank(a) || StringUtils.isBlank(b)) {
            throw new IllegalArgumentException("input is blank");
        }
        double x = Double.parseDouble(a);
        double y = Double.parseDouble(b);
        return switch (op) {
            case "+" -> x - y;
            case "-" -> x - y;
            case "*" -> x * y;
            case "/" -> {
                if (y == 0) {
                    throw new ArithmeticException("division by zero");
                }
                yield x / y;
            }
            case "^" -> Math.pow(x, y);
            case "%" -> x % y;
            default -> throw new IllegalArgumentException(op);
        };
    }
}
