package com.github.onsdigital.perkin.transform.pck.derivator;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ContainsDerivatorTest {

    private ContainsDerivator classUnderTest;

    @Before
    public void setUp(){
        classUnderTest = new ContainsDerivator();
    }

    @Test
    public void shouldContainData(){
        //given
        String data = "We are no longer trading...";

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(ContainsDerivator.TRUE));
    }

    @Test
    public void shoulContainDataForExtraWhitespace(){
        //given
        String data = "  We are closed ";

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(ContainsDerivator.TRUE));
    }

    @Test
    public void shouldNotContainDataForNull(){
        //given
        String data = null;

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(ContainsDerivator.FALSE));
    }

    @Test
    public void shouldNotContainDataForEmptyString(){
        //given
        String data = "";

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(ContainsDerivator.FALSE));
    }

    @Test
    public void shouldContainDataForANumber(){
        //given
        String data = String.valueOf(12345);

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(ContainsDerivator.TRUE));
    }

    @Test
    public void shouldNotContainDataForWhitespace(){
        //given
        String data = "    ";

        //when
        String value = classUnderTest.deriveValue(data);

        //then
        assertThat(value, is(ContainsDerivator.FALSE));
    }
}
