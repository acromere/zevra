package com.acromere.log.java;

import com.acromere.log.provider.LoggerWrapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaLoggingProviderTest {

	@Test
	void testGetLoggerWrapper() {
		JavaLoggingProvider provider = new JavaLoggingProvider();
		LoggerWrapper wrapper = provider.getLoggerWrapper( "com.acromere.test" );

		assertThat( wrapper ).isNotNull();
		assertThat( wrapper ).isInstanceOf( JavaLoggingLoggerWrapper.class );
		assertThat( wrapper.getLoggerName() ).isEqualTo( "com.acromere.test" );
	}

}
