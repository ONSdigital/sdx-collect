package com.github.onsdigital.perkin.pck.derivator;


import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ContainsDerivatorTest {

    ContainsDerivator classUnderTest ;

    @Before
    public void setUp(){
        classUnderTest = new ContainsDerivator();
    }

    @Test
    public void shouldDeriveBooleanTrueWithData(){
        //given
        String data = "We are no longer trading...";

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(ContainsDerivator.TRUE));
    }

    @Test
    public void shouldDeriveBooleanTrueWithExtraWhiteSpace(){
        //given
        String data = "  We are closed ";
        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(ContainsDerivator.TRUE));
    }

    @Test
    public void shouldDeriveBooleanFalseWithNull(){
        //given
        String data = null;
        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(ContainsDerivator.FALSE));
    }

    @Test
    public void shouldDeriveBooleanFalseWithEmptyString(){
        //given
        String data = "";
        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(ContainsDerivator.FALSE));
    }

    @Test
    public void shouldDeriveBooleanTrueWithNumbers(){
        //given
        String data = String.valueOf(12345);
        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(ContainsDerivator.TRUE));
    }

    @Test
    public void shouldDeriveBooleanFalseWhenDataWhiteSpace(){
        //given
        String data = "    ";
        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(ContainsDerivator.FALSE));
    }

}
