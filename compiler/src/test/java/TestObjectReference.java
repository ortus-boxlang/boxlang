/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import com.github.javaparser.ast.Node;
import org.junit.Test;
import ourtus.boxlang.parser.BoxLangParser;
import ourtus.boxlang.parser.ParsingResult;
import ourtus.boxlang.transpiler.BoxLangTranspiler;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestObjectReference {

	private void assertEqualsNoWhiteSpaces( String expected, String actual ) {
		assertEquals( expected.replaceAll( "[ \\t\\r\\n]", "" ), actual.replaceAll( "[ \\t\\r\\n]", "" ) );
	}

	@Test
	public void testDereferenceByKey() throws IOException {
		String expression = """
						foo.bar
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );
		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEqualsNoWhiteSpaces(
			"Referencer.get( context.scopeFindNearby( Key.of( \"foo\" ), null ).value(), Key.of( \"bar\" ), false )",
			javaAST.toString() );
	}

	@Test
	public void testDereferenceByKeyFromKnownScope() throws IOException {
		String expression = """
						variables.foo
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );
		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEqualsNoWhiteSpaces( "context.getScopeNearby( Key.of( \"variablesScope\" ) ).get( Key.of( \"foo\" ) )",
			javaAST.toString() );
	}
}
