package com.yangziyi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    Calculator c = new Calculator();

    // ==================== Normal calculations ====================

    @Test
    void addWorks() {
        assertEquals(4.0, c.eval("2", "+", "2"));
    }

    @Test
    void subtractWorks() {
        assertEquals(3.0, c.eval("5", "-", "2"));
    }

    @Test
    void multiplyWorks() {
        assertEquals(12.0, c.eval("3", "*", "4"));
    }

    @Test
    void divideWorks() {
        assertEquals(2.0, c.eval("10", "/", "5"));
    }

    // ==================== Decimal and negative operands ====================

    @Test
    void addDecimals() {
        assertEquals(3.7, c.eval("1.5", "+", "2.2"), 0.0001);
    }

    @Test
    void subtractNegative() {
        assertEquals(-5.0, c.eval("-2", "-", "3"));
    }

    @Test
    void multiplyNegativeByPositive() {
        assertEquals(-10.0, c.eval("-2", "*", "5"));
    }

    @Test
    void divideNegativeByPositive() {
        assertEquals(-2.5, c.eval("-5", "/", "2"));
    }

    // ==================== Boundary values ====================

    @Test
    void addZero() {
        assertEquals(5.0, c.eval("5", "+", "0"));
    }

    @Test
    void multiplyByZero() {
        assertEquals(0.0, c.eval("5", "*", "0"));
    }

    @Test
    void divideByOne() {
        assertEquals(7.0, c.eval("7", "/", "1"));
    }

    @Test
    void divideZeroByNumber() {
        assertEquals(0.0, c.eval("0", "/", "5"));
    }

    // ==================== Division by zero ====================

    @Test
    void divideByZeroThrows() {
        assertThrows(ArithmeticException.class,
                () -> c.eval("5", "/", "0"));
    }

    @Test
    void divideNegativeByZeroThrows() {
        assertThrows(ArithmeticException.class,
                () -> c.eval("-5", "/", "0"));
    }

    // ==================== Blank / null input ====================

    @Test
    void blankFirstInputThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> c.eval("", "+", "2"));
    }

    @Test
    void blankSecondInputThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> c.eval("2", "+", ""));
    }

    @Test
    void nullFirstInputThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> c.eval(null, "+", "2"));
    }

    @Test
    void nullSecondInputThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> c.eval("2", "+", null));
    }

    @Test
    void whitespaceOnlyInputThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> c.eval("   ", "+", "2"));
    }

    // ==================== Extended operators (Challenge 1) ====================

    @Test
    void powerWorks() {
        assertEquals(8.0, c.eval("2", "^", "3"));
    }

    @Test
    void powerOfZero() {
        assertEquals(1.0, c.eval("5", "^", "0"));
    }

    @Test
    void powerOfOne() {
        assertEquals(7.0, c.eval("7", "^", "1"));
    }

    @Test
    void powerNegative() {
        assertEquals(0.25, c.eval("2", "^", "-2"), 0.0001);
    }

    @Test
    void moduloWorks() {
        assertEquals(1.0, c.eval("5", "%", "2"));
    }

    @Test
    void moduloByZero() {
        assertEquals(Double.NaN, c.eval("5", "%", "0"));
    }

    @Test
    void moduloNegative() {
        assertEquals(-1.0, c.eval("-5", "%", "2"));
    }

    // ==================== Invalid operators ====================

    @Test
    void unknownOperatorThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> c.eval("2", "&", "2"));
    }

    @Test
    void emptyOperatorThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> c.eval("2", "", "2"));
    }

    @Test
    void nullOperatorThrows() {
        assertThrows(NullPointerException.class,
                () -> c.eval("2", null, "2"));
    }

    // ==================== Non-numeric input ====================

    @Test
    void nonNumericFirstInputThrows() {
        assertThrows(NumberFormatException.class,
                () -> c.eval("abc", "+", "2"));
    }

    @Test
    void nonNumericSecondInputThrows() {
        assertThrows(NumberFormatException.class,
                () -> c.eval("2", "+", "xyz"));
    }

    @Test
    void mixedInputThrows() {
        assertThrows(NumberFormatException.class,
                () -> c.eval("2a", "+", "3"));
    }

    // ==================== Precision tests ====================

    @Test
    void multiplyDecimalsPrecision() {
        assertEquals(0.3, c.eval("0.1", "*", "3"), 0.0001);
    }

    @Test
    void addLargeNumbers() {
        assertEquals(2000000000.0, c.eval("1000000000", "+", "1000000000"));
    }
}
