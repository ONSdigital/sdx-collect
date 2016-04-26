package com.github.onsdigital.perkin.transform;

import org.junit.After;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NumberServiceTest {

    private NumberService classUnderTest;

    @After
    public void tearDown() {
        classUnderTest.destroy();
    }

    @Test
    public void shouldStartAtCorrectNumber() {
        //Given
        classUnderTest = new NumberService("test", 5L, 10L);

        //When
        long start = classUnderTest.getNext();

        //Then
        assertThat(start, is(5L));
    }

    @Test
    public void shouldWrapAroundToStart() {
        //Given
        long start = 5L;
        classUnderTest = new NumberService("test", start, 10L);
        classUnderTest.reset();

        //When / Then
        for (int i = 0; i < 6; i++) {
            long sequence = classUnderTest.getNext();
            assertThat(sequence, is(start + i));
        }

        //Then
        long sequence = classUnderTest.getNext();
        assertThat(sequence, is(start));
    }

    @Test
    public void shouldWrapAroundToStartWhenSaved() {
        //Given
        long start = 5L;
        classUnderTest = new NumberService("test", start, 10L);
        classUnderTest.reset();

        //When / Then
        for (int i = 0; i < 6; i++) {
            // Re-initialise (and re-load) on every increment:
            classUnderTest = new NumberService("test", start, 10L);
            long sequence = classUnderTest.getNext();
            assertThat(sequence, is(start + i));
        }

        //Then
        long sequence = new NumberService("test", start, 10L).getNext();
        assertThat(sequence, is(start));
    }
}
