package com.github.onsdigital.perkin.transform.pck.derivator;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class DateDerivatorTest {

    private DateDerivator classUnderTest;

    @Before
    public void setUp(){
        classUnderTest = new DateDerivator();
    }

    @Test
    public void shouldDeriveDate(){
        //given
        String data = "1/1/2016";

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is("010116"));
    }

    @Test
    public void shouldDeriveDoubleDigitMonthDate(){
        //given
        String data = "1/12/2016";

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is("011216"));
    }

    @Test
    public void shouldDeriveDoubleDigitDayDate(){
        //given
        String data = "15/12/2016";

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is("151216"));
    }

    @Test
    public void shouldNotContainDataForNull(){
        //given
        String data = null;

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(nullValue()));
    }

    @Test
    public void shouldNotContainDataForWhitespace(){
        //given
        String data = "    ";

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(""));
    }
}
