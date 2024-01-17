/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ortus.boxlang.compiler;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;

import ortus.boxlang.ast.BoxDocumentation;
import ortus.boxlang.ast.BoxScript;
import ortus.boxlang.ast.expression.BoxStringLiteral;
import ortus.boxlang.ast.statement.BoxArgumentDeclaration;
import ortus.boxlang.ast.statement.BoxDocumentationAnnotation;
import ortus.boxlang.ast.statement.BoxFunctionDeclaration;
import ortus.boxlang.parser.BoxCFParser;
import ortus.boxlang.parser.BoxDOCParser;
import ortus.boxlang.parser.BoxParser;
import ortus.boxlang.parser.ParsingResult;
import ortus.boxlang.transpiler.JavaTranspiler;

public class TestUDF extends TestBase {

	public boolean isParsable( String statement ) throws IOException {
		BoxParser		parser	= new BoxParser();
		ParsingResult	result	= parser.parseStatement( statement );
		return result.isCorrect();
	}

	public Node transformUDF( String statement ) throws IOException {
		BoxParser		parser	= new BoxParser();
		ParsingResult	result	= parser.parseStatement( statement );
		assertTrue( result.isCorrect() );

		JavaTranspiler transpiler = new JavaTranspiler();
		transpiler.setProperty( "packageName", "ortus.test" );
		transpiler.setProperty( "classname", "MyUDF" );
		return transpiler.transform( result.getRoot() );
	}

	@Test
	public void userDefinedFunction() throws IOException {

		assertTrue( isParsable(
		    """
		    public String function foo(
		    	required string param1 hint="My param",
		    	numeric param2=42 luis="majano"
		    ) hint="my UDF" output=false brad="wood" {
		      return "value";
		    }
		      """
		) );

	}

	@Test
	public void functionDocumentation() throws IOException {

		String			documentation	= """
		                                  /**
		                                  * This function does cool stuff
		                                  *
		                                  * @name Pass the name here that you want
		                                  * @name.isCool yes
		                                  *
		                                  * @author Brad Wood
		                                  * @returns Only the coolest value ever
		                                  */
		                                  		                                  """;
		BoxDOCParser	parser			= new BoxDOCParser(0,0);
		ParsingResult	result			= parser.parse( null, documentation );
		assertTrue( result.isCorrect() );
		BoxDocumentation docs = ( BoxDocumentation ) result.getRoot();

		assertTrue( ( ( BoxDocumentationAnnotation ) docs.getAnnotations().get( 0 ) ).getKey().getValue().equals( "name" ) );
		assertThat(
		    ( ( BoxStringLiteral ) ( ( BoxDocumentationAnnotation ) docs.getAnnotations().get( 0 ) ).getValue() ).getValue().trim()
		).isEqualTo( "Pass the name here that you want" );
	}

	@Test
	public void userDefinedFunctionDocumentation() throws IOException {

		BoxCFParser		parser	= new BoxCFParser();
		String			code	= """
		                          				/**
		                          				* This function does cool stuff
		                          				*
		                          				* @name Pass the name here that you want
		                          				* @name.isCool yes
		                          				*
		                          				* @author Brad Wood
		                          				* @returns Only the coolest value ever
		                          				*/
		                          				@myAnnotation "value" "another value"
		                          				@name.foo "bar"
		                          				string function greet( required string name='Brad' inject="myService" ) key="value" keyOnly {
		                          				  return "Brad";
		                          				}
		                          """;

		ParsingResult	result	= parser.parse( code );

		assertTrue( result.isCorrect() );
		BoxScript script = ( BoxScript ) result.getRoot();
		script.getStatements().forEach( stmt -> {
			stmt.walk().forEach( it -> {
				BoxStringLiteral value;
				if ( it instanceof BoxFunctionDeclaration func ) {
					Assertions.assertEquals( 3, func.getAnnotations().size() );

				}
				if ( it instanceof BoxArgumentDeclaration arg ) {
					if ( arg.getName().equalsIgnoreCase( "name" ) ) {

						Assertions.assertEquals( 2, arg.getDocumentation().size() );
						Assertions.assertEquals( "hint", arg.getDocumentation().get( 0 ).getKey().getValue() );
						value = ( BoxStringLiteral ) arg.getDocumentation().get( 0 ).getValue();
						Assertions.assertEquals( "Pass the name here that you want", value.getValue().trim() );

						Assertions.assertEquals( "isCool", arg.getDocumentation().get( 1 ).getKey().getValue() );
						value = ( BoxStringLiteral ) arg.getDocumentation().get( 1 ).getValue();
						Assertions.assertEquals( "yes", value.getValue().trim() );
					}
				}

			} );
		} );

		CompilationUnit		javaAST		= ( CompilationUnit ) transformUDF( code );
		VariableDeclarator	arguments	= javaAST.getType( 0 ).getFieldByName( "arguments" ).get().getVariable( 0 );
		Assertions.assertEquals( 1, arguments.getInitializer().get().asArrayInitializerExpr().getValues().size() );
		VariableDeclarator annotations = javaAST.getType( 0 ).getFieldByName( "annotations" ).get().getVariable( 0 );
		assertEqualsNoWhiteSpaces( """
		                           Struct.of(MyUDF.keys[6],Array.of("value","anothervalue"),MyUDF.keys[7],"value",MyUDF.keys[8],"")
		                                                     """, annotations.getInitializer().get().toString() );
		VariableDeclarator documentation = javaAST.getType( 0 ).getFieldByName( "documentation" ).get().getVariable( 0 );
		assertEqualsNoWhiteSpaces( """
		                           Struct.of(MyUDF.keys[9],"BradWood",MyUDF.keys[10],"Onlythecoolestvalueever",MyUDF.keys[3],"Thisfunctiondoescoolstuff")
		                                                 """, documentation.getInitializer().get().toString() );
	}

	@Test
	public void userDefinedFunctionArguments() throws IOException {

		BoxCFParser		parser	= new BoxCFParser();
		ParsingResult	result	= parser.parse(

		    """
		    @myAnnotation "value" "another value"
		               	@name.foo "bar"
		               	string function greet( required string name='Brad' inject="myService" ) key="value" keyOnly {
		               	  return "Brad";
		               	}
		      """
		);
		assertTrue( result.isCorrect() );
		BoxScript script = ( BoxScript ) result.getRoot();
		script.getStatements().forEach( stmt -> {
			stmt.walk().forEach( it -> {
				BoxStringLiteral value;
				if ( it instanceof BoxFunctionDeclaration func ) {
					Assertions.assertEquals( 3, func.getAnnotations().size() );

				}
				if ( it instanceof BoxArgumentDeclaration arg ) {
					if ( arg.getName().equalsIgnoreCase( "name" ) ) {

						Assertions.assertEquals( 2, arg.getAnnotations().size() );
						Assertions.assertEquals( "inject", arg.getAnnotations().get( 0 ).getKey().getValue() );
						value = ( BoxStringLiteral ) arg.getAnnotations().get( 0 ).getValue();
						Assertions.assertEquals( "myService", value.getValue() );
						Assertions.assertEquals( "foo", arg.getAnnotations().get( 1 ).getKey().getValue() );
						value = ( BoxStringLiteral ) arg.getAnnotations().get( 1 ).getValue();
						Assertions.assertEquals( "bar", value.getValue() );
					}
				}

			} );
		} );

	}

	@Test
	public void userDefinedFunctionArgumentsAnnotation() throws IOException {

		BoxCFParser		parser	= new BoxCFParser();
		ParsingResult	result	= parser.parse(

		    """
		    @param1.key "value"
		    @param1.brad "wood"
		    @param2.key "value"
		    @param2.luis "majano"
		    function foo(
		    required string param1="default",
		      param2
		    ) {}
		       """
		);
		assertTrue( result.isCorrect() );
		BoxScript script = ( BoxScript ) result.getRoot();
		script.getStatements().forEach( stmt -> {
			stmt.walk().forEach( it -> {
				BoxStringLiteral value;
				if ( it instanceof BoxArgumentDeclaration arg ) {
					if ( arg.getName().equalsIgnoreCase( "param1" ) ) {

						Assertions.assertEquals( 2, arg.getAnnotations().size() );
						Assertions.assertEquals( "key", arg.getAnnotations().get( 0 ).getKey().getValue() );
						value = ( BoxStringLiteral ) arg.getAnnotations().get( 0 ).getValue();
						Assertions.assertEquals( "value", value.getValue() );
						Assertions.assertEquals( "brad", arg.getAnnotations().get( 1 ).getKey().getValue() );
						value = ( BoxStringLiteral ) arg.getAnnotations().get( 1 ).getValue();
						Assertions.assertEquals( "wood", value.getValue() );
					}
					if ( arg.getName().equalsIgnoreCase( "param2" ) ) {
						Assertions.assertEquals( 2, arg.getAnnotations().size() );
						Assertions.assertEquals( "key", arg.getAnnotations().get( 0 ).getKey().getValue() );
						value = ( BoxStringLiteral ) arg.getAnnotations().get( 0 ).getValue();
						Assertions.assertEquals( "value", value.getValue() );
						Assertions.assertEquals( "luis", arg.getAnnotations().get( 1 ).getKey().getValue() );
						value = ( BoxStringLiteral ) arg.getAnnotations().get( 1 ).getValue();
						Assertions.assertEquals( "majano", value.getValue() );
					}
				}

			} );
		} );
	}

}
