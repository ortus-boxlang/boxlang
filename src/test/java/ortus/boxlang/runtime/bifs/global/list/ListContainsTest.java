
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

package ortus.boxlang.runtime.bifs.global.list;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class ListContainsTest {

	static BoxRuntime	instance;
	static IBoxContext	context;
	static IScope		variables;
	static Key			result	= new Key( "result" );

	@BeforeAll
	public static void setUp() {
		instance	= BoxRuntime.getInstance( true );
		context		= new ScriptingRequestBoxContext( instance.getRuntimeContext() );
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

	@DisplayName( "It can search" )
	@Test
	public void testCanSearch() {

		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = listContains( list, 'b' );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 2 );

		instance.executeSource(
		    """
		    list = "1.234,2.345,3.456";
		    result = listContains( list, 3.456 );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 3 );
	}

	@DisplayName( "It can search case insensitively" )
	@Test
	public void testCanSearchNoCase() {

		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = listContains( list, 'B' );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 0 );

		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = listContainsNoCase( list, 'B' );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 2 );

	}

	@DisplayName( "It will throw an error if the search is not a string" )
	@Test
	public void testCanSearchUDF() {
		assertThrows(
		    BoxRuntimeException.class,
		    () -> instance.executeSource(
		        """
		        list = "a,b,c";
		        result = listContains( list, i->i=="b" );
		        """,
		        context )
		);

	}

	@DisplayName( "It can search member" )
	@Test
	public void testCanSearchMember() {

		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = list.listContains( 'b' );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 2 );

		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = list.listContains( 'B' );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 0 );

		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = list.listContainsNoCase( 'B' );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( 2 );
	}

}