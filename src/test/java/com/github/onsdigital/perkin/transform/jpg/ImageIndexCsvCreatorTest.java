package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.json.Collection;
import com.github.onsdigital.perkin.json.Survey;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@Slf4j
public class ImageIndexCsvCreatorTest {

    @Mock
    private Survey survey;

    @Mock
    private Collection collection;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldPrepend4DigitPeriod() {

        //Given
        when(collection.getPeriod()).thenReturn("1603");
        when(survey.getCollection()).thenReturn(collection);

        //When
        String period = ImageIndexCsvCreator.createPeriod(survey);

        //Then
        assertThat(period.length(), is(6));
        assertThat(period, is("201603"));
    }

    @Test
    public void shouldUse6DigitPeriod() {

        //Given
        when(collection.getPeriod()).thenReturn("201603");
        when(survey.getCollection()).thenReturn(collection);

        //When
        String period = ImageIndexCsvCreator.createPeriod(survey);

        //Then
        assertThat(period.length(), is(6));
        assertThat(period, is("201603"));
    }

    @Test
    public void shouldUseMax6DigitPeriod() {

        //Given
        when(collection.getPeriod()).thenReturn("201603123456");
        when(survey.getCollection()).thenReturn(collection);

        //When
        String period = ImageIndexCsvCreator.createPeriod(survey);

        //Then
        assertThat(period.length(), is(6));
        assertThat(period, is("201603"));
    }

    @Test
    public void shouldLeftPadPeriod() {

        //Given
        when(collection.getPeriod()).thenReturn("");
        when(survey.getCollection()).thenReturn(collection);

        //When
        String period = ImageIndexCsvCreator.createPeriod(survey);

        //Then
        assertThat(period.length(), is(6));
        assertThat(period, is("000000"));
    }

    @Test
    public void shouldWorkForNullPeriod() {

        //Given
        when(collection.getPeriod()).thenReturn(null);
        when(survey.getCollection()).thenReturn(collection);

        //When
        String period = ImageIndexCsvCreator.createPeriod(survey);

        //Then
        assertThat(period.length(), is(6));
        assertThat(period, is("000000"));
    }
}
