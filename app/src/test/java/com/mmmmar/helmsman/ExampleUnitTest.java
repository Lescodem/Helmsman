package com.mmmmar.helmsman;

import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        String uuid = "0000ffe0–0000–1000–8000–00805f9b34fb";
        String[] parts = uuid.split("–");
        System.out.println(Arrays.toString(parts));
        assertEquals(parts.length, 5);
    }
}