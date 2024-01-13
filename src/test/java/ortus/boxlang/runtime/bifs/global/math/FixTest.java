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
package ortus.boxlang.runtime.bifs.global.math;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;

public class FixTest {

	static BoxRuntime	instance;
	static IBoxContext	context;
	static IScope		variables;
	static Key			result	= new Key( "result" );

	@BeforeAll
	public static void setUp() {
		instance	= BoxRuntime.getInstance( true );
		context		= new ScriptingBoxContext( instance.getRuntimeContext() );
		variables	= context.getScopeNearby( VariablesScope.name );
	}

	@AfterAll
	public static void teardown() {
		instance.shutdown();
	}

	@BeforeEach
	public void setupEach() {
		variables.clear();
	}

	@DisplayName( "It returns a fixed integer" )
	@Test
	public void testItReturnsFixedInteger() {
		instance.executeSource(
		    """
		    result = fix(1.8);
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 1 );

		instance.executeSource(
		    """
		    result = fix(-2.8);
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( -2 );

		instance.executeSource(
		    """
		    result = fix(0);
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 0 );
	}

	@DisplayName( "It returns a fixed integer member" )
	@Test
	public void testItReturnsFixedIntegerMember() {
		instance.executeSource(
		    """
		    result = (1.8).fix();
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 1 );

		instance.executeSource(
		    """
		    result = (-2.8).fix();
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( -2 );

		instance.executeSource(
		    """
		    result = (0).fix();
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 0 );
	}

}