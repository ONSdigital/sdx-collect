package com.github.onsdigital.perkin.transform;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NumberServiceTest {

    private NumberService classUnderTest;

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

        //When / Then
        for (int i = 0; i < 6; i++) {
            long sequence = classUnderTest.getNext();
            assertThat(sequence, is(start + i));
        }

        //Then
        long sequence = classUnderTest.getNext();
        assertThat(sequence, is(start));
    }
}
