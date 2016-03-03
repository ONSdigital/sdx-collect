package com.github.onsdigital;

import com.github.onsdigital.Configuration;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ConfigurationTest {

    private static final String KEY = "myKey";

    @After
    public void tearDown() {
        System.clearProperty(KEY);
    }

    @Test
    public void shouldGetSystemProperty() {
        //given
        System.setProperty(KEY, "myValue");

        //when
        String value = Configuration.get(KEY);

        //then
        assertThat(value, is("myValue"));
    }

    @Test
    public void shouldGetDefaultValueForUnsetSystemProperty() {
        //given
        String defaultValue = "myDefaultValue";

        //when
        String value = Configuration.get(KEY, defaultValue);

        //then
        assertThat(value, is(defaultValue));
    }

    @Test
    public void shouldGetNullForUnsetSystemProperty() {
        //given

        //when
        String value = Configuration.get(KEY);

        //then
        assertThat(value, is(nullValue()));
    }

    @Test
    public void shouldGetIntSystemProperty() {
        //given
        int defaultValue = -1;
        System.setProperty(KEY, "3");

        //when
        int value = Configuration.getInt(KEY, defaultValue);

        //then
        assertThat(value, is(3));
    }

    @Test
    public void shouldGetDefaultIntForUnsetSystemProperty() {
        //given
        int defaultValue = -1;

        //when
        int value = Configuration.getInt(KEY, defaultValue);

        //then
        assertThat(value, is(defaultValue));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldFailForInvalidInt() {
        //given
        int defaultValue = -1;
        System.setProperty(KEY, "oops");

        //when
        Configuration.getInt(KEY, defaultValue);
    }
}
