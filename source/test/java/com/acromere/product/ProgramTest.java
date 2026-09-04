package com.acromere.product;

import com.acromere.settings.Settings;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ProgramTest {

	private static class MockProgram implements Program {

		@Override
		public Program getProgram() {
			return this;
		}

		@Override
		public ProductCard getCard() {
			return new ProductCard().setName( "MockProgram" );
		}

		@Override
		public Settings getSettings() {
			return null;
		}

		@Override
		public Path getDataFolder() {
			return Path.of( "/tmp/mock-program" );
		}

	}

	@Test
	void testProgramInterface() {
		Program program = new MockProgram();

		assertThat( program.getProgram() ).isSameAs( program );
		assertThat( program.getCard().getName() ).isEqualTo( "MockProgram" );
		assertThat( program.getDataFolder() ).isEqualTo( Path.of( "/tmp/mock-program" ) );
		assertThat( program.getParent() ).isNull();
	}

}
