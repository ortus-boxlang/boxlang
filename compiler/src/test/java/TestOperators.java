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

public class TestOperators {

	@Test
	public void testConcat() throws IOException {
		String expression = """
						"Hello " & "world";
			""";

		BoxLangParser parser = new BoxLangParser();
		BoxLangTranspiler transpiler = new BoxLangTranspiler();
		ParsingResult result = parser.parseExpression( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "Concat.invoke(\"Hello \", \"world\")", javaAST.toString() );

	}

	@Test
	public void testConcatWithVariable() throws IOException {
		String expression = """
						someObject & "world";
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );
		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "Concat.invoke(someObject, \"world\")", javaAST.toString() );

	}

	@Test
	public void testPlus() throws IOException {
		String expression = """
						1 + 2
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "Plus.invoke(1, 2)", javaAST.toString() );

	}

	@Test
	public void testPlusWithVariable() throws IOException {
		String expression = """
						1 + aNumber
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "Plus.invoke(1, aNumber)", javaAST.toString() );

	}

	@Test
	public void testMinus() throws IOException {
		String expression = """
						1 - 2
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "Minus.invoke(1, 2)", javaAST.toString() );

	}

	@Test
	public void testStar() throws IOException {
		String expression = """
						1 * 2
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "Multiply.invoke(1, 2)", javaAST.toString() );

	}

	@Test
	public void testSlash() throws IOException {
		String expression = """
						1 / 2
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "Divide.invoke(1, 2)", javaAST.toString() );

	}

	@Test
	public void testContains() throws IOException {
		String expression = """
						"Brad Wood" contains "Wood"
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "Contains.contains(\"Brad Wood\", \"Wood\")", javaAST.toString() );

	}

	@Test
	public void testContainsWithVariable() throws IOException {
		String expression = """
						"Brad Wood" contains wood
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "Contains.contains(\"Brad Wood\", wood)", javaAST.toString() );

	}

	@Test
	public void testDoesNotContains() throws IOException {
		String expression = """
						"Brad Wood" does not contains "Luis"
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "!Contains.contains(\"Brad Wood\", \"Luis\")", javaAST.toString() );

	}

	@Test
	public void testNegate() throws IOException {
		String expression = """
						!True
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "Negate.invoke(\"True\")", javaAST.toString() );

	}

	@Test
	public void testNegateNegate() throws IOException {
		String expression = """
						!!False
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "Negate.invoke(Negate.invoke(\"False\"))", javaAST.toString() );

	}

	@Test
	public void testTernary() throws IOException {
		String expression = """
						isGood ? "eat" : "toss"
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "Ternary.invoke(isGood, \"eat\", \"toss\")", javaAST.toString() );

	}

	@Test
	public void testScopeRead() throws IOException {
		String expression = """
						variables["system"]
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseExpression( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "variablesScope.get(Key.of(\"system\"))", javaAST.toString() );

	}

	@Test
	public void testScopeWrite() throws IOException {
		String expression = """
						variables["system"] = ""
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseStatement( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "variablesScope.put(Key.of(\"system\"), \"\");", javaAST.toString() );

	}

	@Test
	public void testElvis() throws IOException {
		String expression = """
						maybeNull ?: "use if null"
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseStatement( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "Elvis.invoke( maybeNull , \"use if null\" )", javaAST.toString() );

	}

	@Test
	public void testElvisLeftDereferencing() throws IOException {
		String expression = """
						variables.foo.bar ?: "brad"
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseStatement( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( """
			Elvis.invoke(
				Referencer.get(
					context.getScopeLocal( Key.of( "variables" ) )
					  .get(
					    Key.of( "foo" ),
					    true
					  ),
						Key.of( "bar" ),
					  true
				),
			  "brad"
			)""".replaceAll( "[ \\r\\n\\t]", "" ), javaAST.toString().replaceAll( "[ \\t\\r\\n]", "" ) );

	}

	@Test
	public void testXor() throws IOException {
		String expression = """
						isCar XOR isCar
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseStatement( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "XOR.invoke( isCar, isCar )", javaAST.toString() );

	}

	@Test
	public void testInstanceOf() throws IOException {
		String expression = """
			foo instanceOf "String"
			""";

		BoxLangParser parser = new BoxLangParser();
		ParsingResult result = parser.parseStatement( expression );

		Node javaAST = BoxLangTranspiler.transform( result.getRoot() );

		assertEquals( "InstanceOf.invoke( context, foo, \"String\" )", javaAST.toString() );

	}

}
