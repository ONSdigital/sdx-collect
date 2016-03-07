package com.github.onsdigital.perkin.pck.derivator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DerviatorFactoryTest {

	private DerivatorFactory classUnderTest;

	private Derivator derivator;
	private String derivedValue = null;

	@Before
	public void setUp() {
		classUnderTest = new DerivatorFactory();
	}

	@Test
	public void getBooleanDerivator() throws DerivatorNotFoundException {
		//given

		//when
		derivator = classUnderTest.getDerivator("BOOLEAN");

		//then
		assertTrue(derivator instanceof BooleanDerivator);
	}

	@Test
	public void getBooleanDerivatorMixedCase() throws DerivatorNotFoundException {
		//given

		//when
		derivator = classUnderTest.getDerivator("boolean");

		//then
		assertTrue(derivator instanceof BooleanDerivator);
	}

	@Test
	public void getDefaultDerivator() {

		try {
			derivator = classUnderTest.getDerivator(" Default ");
			assertTrue(derivator instanceof DefaultDerivator);
		} catch (DerivatorNotFoundException e) {
			fail("Derivator has not been intatiated correctly:"	+ e.getMessage());
		}

	}

	@Test
	public void getContainsDataDerivator() {

		try {
			derivator = classUnderTest.getDerivator("Contains");
			assertTrue(derivator instanceof ContainsDerivator);
		} catch (DerivatorNotFoundException e) {
			fail("Derivator has not been intatiated correctly:"	+ e.getMessage());
		}
	}
	
	@Test(expected = DerivatorNotFoundException.class)
	public void shouldThrowDerivatorNotFoundException() throws DerivatorNotFoundException {
		derivator = classUnderTest.getDerivator("no-such-derivator");
		fail("Derivator should not be found...");
	}

	@Test
	public void containsDataDerivator() {

		try {
			derivator = classUnderTest.getDerivator("Contains");
			derivedValue = derivator.deriveValue("ONS");
			assertTrue(derivedValue.equals("1"));
			assertTrue(!derivedValue.equals("2"));

			derivedValue = derivator.deriveValue(null);
			assertTrue(derivedValue.equals("2"));
			assertTrue(!derivedValue.equals("1"));
		} catch (DerivatorNotFoundException e) {
			fail("Derivator has failed to be created: " + e.getMessage());
		}
	}
	
	public void defaultDerivator(){

		try {
			derivator = classUnderTest.getDerivator("Default");
			derivedValue = derivator.deriveValue("ONS");
			assertTrue(derivedValue.equals("ONS"));
			assertTrue(!derivedValue.isEmpty());
			
		} catch (DerivatorNotFoundException e) {
			fail("Derivator has failed to be created: " + e.getMessage());
		}
	}

}
