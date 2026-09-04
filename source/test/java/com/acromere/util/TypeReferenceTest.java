package com.acromere.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypeReferenceTest {

	@Test
	void testParameterizedTypeReference() {
		TypeReference<Map<String, Integer>> typeRef = new TypeReference<>() {};

		assertThat( typeRef.getType() ).isNotNull();
		assertThat( typeRef.getTypeClass() ).isEqualTo( Map.class );
		assertThat( typeRef.toString() ).isEqualTo( Map.class.getName() );
	}

	@Test
	void testSimpleTypeReference() {
		TypeReference<String> typeRef = new TypeReference<>() {};

		assertThat( typeRef.getType() ).isEqualTo( String.class );
		assertThat( typeRef.getTypeClass() ).isEqualTo( String.class );
		assertThat( typeRef.toString() ).isEqualTo( String.class.getName() );
	}

	@Test
	void testExplicitClassConstructor() {
		TypeReference<Double> typeRef = new TypeReference<>( Double.class ) {};

		assertThat( typeRef.getType() ).isEqualTo( Double.class );
		assertThat( typeRef.getTypeClass() ).isEqualTo( Double.class );
		assertThat( typeRef.toString() ).isEqualTo( Double.class.getName() );
	}

	@Test
	@SuppressWarnings( "rawtypes" )
	void testRawTypeThrowsException() {
		assertThatThrownBy( () -> new TypeReference() {} )
			.isInstanceOf( IllegalArgumentException.class )
			.hasMessageContaining( "created without type information" );
	}

	@Test
	void testCompareTo() {
		TypeReference<List<String>> listRef1 = new TypeReference<>() {};
		TypeReference<List<String>> listRef2 = new TypeReference<>() {};

		assertThat( listRef1.compareTo( listRef2 ) ).isZero();
	}

}
