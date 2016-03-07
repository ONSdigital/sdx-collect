package com.github.onsdigital.perkin.pck.derivator;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BooleanDerivatorTest {

	private BooleanDerivator classUnderTest;

	private Derivator derivator;
	private String derivedValue = null;

	@Before
	public void setUp() {
		classUnderTest = new BooleanDerivator();
	}

	@Test
	public void shouldDeriveBooleanTrueLowerCase() {
		//given

		//when
		String value = classUnderTest.deriveValue("y");

		//then
		assertThat(value, is(BooleanDerivator.TRUE));
	}

	@Test
	public void shouldDeriveBooleanTrueWhitespace() {
		//given

		//when
		String value = classUnderTest.deriveValue(" y ");

		//then
		assertThat(value, is(BooleanDerivator.TRUE));
	}

	@Test
	public void shouldDeriveBooleanTrueUpperCase() {
		//given

		//when
		String value = classUnderTest.deriveValue("Y");

		//then
		assertThat(value, is(BooleanDerivator.TRUE));
	}

	@Test
	public void shouldDeriveBooleanFalseLowerCase() {
		//given

		//when
		String value = classUnderTest.deriveValue("n");

		//then
		assertThat(value, is(BooleanDerivator.FALSE));
	}

	@Test
	public void shouldDeriveBooleanFalseUpperCase() {
		//given

		//when
		String value = classUnderTest.deriveValue("N");

		//then
		assertThat(value, is(BooleanDerivator.FALSE));
	}

	@Test
	public void shouldDeriveBooleanFalseEmptyString() {
		//given

		//when
		String value = classUnderTest.deriveValue("");

		//then
		assertThat(value, is(BooleanDerivator.FALSE));
	}

	@Test
	public void shouldDeriveBooleanFalseNull() {
		//given

		//when
		String value = classUnderTest.deriveValue(null);

		//then
		assertThat(value, is(BooleanDerivator.FALSE));
	}
}
