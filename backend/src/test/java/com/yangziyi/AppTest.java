package com.yangziyi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream capturedOut;

    @BeforeEach
    void redirectOutput() {
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
    }

    @Test
    void validArgumentsPrintResult() {
        App.main(new String[]{"2", "+", "2"});
        assertTrue(capturedOut.toString().trim().contains("4.0"));
    }

    @Test
    void missingArgumentsPrintUsage() {
        App.main(new String[]{});
        String output = capturedOut.toString();
        assertTrue(output.contains("Usage"), "Expected usage message but got: " + output);
    }

    @Test
    void tooFewArgumentsPrintUsage() {
        App.main(new String[]{"2", "+"});
        assertTrue(capturedOut.toString().contains("Usage"));
    }
}
