package com.github.onsdigital.perkin.transform.pck.derivator;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class DefaultDerivatorTest {

    private DefaultDerivator classUnderTest;

    @Before
    public void setUp() {
        classUnderTest = new DefaultDerivator();
    }

    @Test
    public void shouldTrimWhitespace(){
        //given
        String data = " Hello ";

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is("Hello"));
    }

    @Test
    public void shouldDeriveTextUnchanged(){
        //given
        String data = "teXT UncHanged";

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is("teXT UncHanged"));
    }

    @Test
    public void shouldDeriveNumbersAsText(){
        //given
        String data = String.valueOf(12345);

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is("12345"));
    }

    @Test
    public void shouldDeriveNullAsEmptyString(){
        //given
        String data = null;

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(""));
    }

    @Test
    public void shouldDeriveEmptyString(){
        //given
        String data = "";

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value,is(""));
    }
}
