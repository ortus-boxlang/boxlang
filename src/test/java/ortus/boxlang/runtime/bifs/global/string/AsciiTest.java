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
package ortus.boxlang.runtime.bifs.global.string;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.compiler.parser.BoxSourceType;
import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class AsciiTest {

	static BoxRuntime	instance;
	IBoxContext			context;
	IScope				variables;
	static Key			result	= new Key( "result" );

	@BeforeAll
	public static void setUp() {
		instance = BoxRuntime.getInstance( true );
	}

	@AfterAll
	public static void teardown() {

	}

	@BeforeEach
	public void setupEach() {
		context		= new ScriptingRequestBoxContext( instance.getRuntimeContext() );
		variables	= context.getScopeNearby( VariablesScope.name );
	}

	@DisplayName( "It determines the ASCII value of a character" )
	@Test
	public void testAsciiConversion() {
		instance.executeSource(
		    """
		    result = ascii("A"); // ASCII code for 'A'
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 65 );
	}

	@DisplayName( "It trasnpiles CF" )
	@Test
	public void testTranspilesCF() {
		instance.executeSource(
		    """
		    result = asc("A"); // ASCII code for 'A'
		    """,
		    context, BoxSourceType.CFSCRIPT );
		assertThat( variables.get( result ) ).isEqualTo( 65 );
	}

	@DisplayName( "It determines the ASCII value of a character using member syntax" )
	@Test
	public void testAsciiConversionMemberSyntax() {
		instance.executeSource(
		    """
		    result = "A".ascii(); // ASCII code for 'A'
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 65 );
	}

	@DisplayName( "It throws an exception for empty input string" )
	@Test
	public void testAsciiEmptyInput() {
		assertThrows(
		    BoxRuntimeException.class,
		    () -> instance.executeSource(
		        """
		        result = ascii(""); // Empty input string
		        """,
		        context )
		);
	}
}
