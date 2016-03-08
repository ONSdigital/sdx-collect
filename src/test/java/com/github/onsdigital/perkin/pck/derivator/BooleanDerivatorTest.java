package com.github.onsdigital.perkin.pck.derivator;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BooleanDerivatorTest {

	private BooleanDerivator classUnderTest;

	private Derivator derivator;

	@Before
	public void setUp() {
		classUnderTest = new BooleanDerivator();
	}

	@Test
	public void shouldDeriveBooleanTrueLowerCase() {
		//given
		String data = "y";

		//when
		String value = classUnderTest.deriveValue(data);

		//then
		assertThat(value, is(BooleanDerivator.TRUE));
	}

	@Test
	public void shouldDeriveBooleanTrueWhitespace() {
		//given
		String data = " y ";

		//when
		String value = classUnderTest.deriveValue(data);

		//then
		assertThat(value, is(BooleanDerivator.TRUE));
	}

	@Test
	public void shouldDeriveBooleanTrueUpperCase() {
		//given
		String data = "Y";

		//when
		String value = classUnderTest.deriveValue(data);

		//then
		assertThat(value, is(BooleanDerivator.TRUE));
	}

	@Test
	public void shouldDeriveBooleanFalseLowerCase() {
		//given
		String data = "n";

		//when
		String value = classUnderTest.deriveValue(data);

		//then
		assertThat(value, is(BooleanDerivator.FALSE));
	}

	@Test
	public void shouldDeriveBooleanFalseUpperCase() {
		//given
		String data = "N";

		//when
		String value = classUnderTest.deriveValue(data);

		//then
		assertThat(value, is(BooleanDerivator.FALSE));
	}

	@Test
	public void shouldDeriveBooleanFalseEmptyString() {
		//given
		String data = "";

		//when
		String value = classUnderTest.deriveValue(data);

		//then
		assertThat(value, is(BooleanDerivator.FALSE));
	}

	@Test
	public void shouldDeriveBooleanFalseNull() {
		//given
		String data = null;

		//when
		String value = classUnderTest.deriveValue(data);

		//then
		assertThat(value, is(BooleanDerivator.FALSE));
	}
}
