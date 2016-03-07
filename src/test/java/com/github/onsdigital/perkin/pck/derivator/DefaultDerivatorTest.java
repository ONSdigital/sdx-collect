package com.github.onsdigital.perkin.pck.derivator;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.poi.util.StringUtil;
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
    public void shouldDeriveTextWithNoWhiteSpace(){
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
    public void shouldDeriveNull(){
        //given
        String data = null;
        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value,is(""));
    }

    @Test
    public void shouldDeriveEmptyString(){
        //given
        String data ="";

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value,is(""));
    }
}
