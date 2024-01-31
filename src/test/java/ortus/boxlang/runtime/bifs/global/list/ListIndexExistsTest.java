
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

public class ListIndexExistsTest {

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

	@DisplayName( "It can determine if a list has an index" )
	@Test
	public void testHasIndex() {
		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = listIndexExists( list, 3 );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( true );
		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = listIndexExists( list, 4 );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( false );
		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = listIndexExists( list, -1 );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( false );
	}

	@DisplayName( "It can determine if a list has an index using a custom delimiter" )
	@Test
	public void testHasIndexCustomDelimiter() {
		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = listIndexExists( list, 3, ":" );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( false );
		instance.executeSource(
		    """
		    list = "a:b:c";
		    result = listIndexExists( list, 2, ":" );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( true );
	}

	@DisplayName( "It can include empty fields when checking for an index" )
	@Test
	public void testHasIndexEmptyFields() {
		instance.executeSource(
		    """
		    list = "a,b,,c";
		    result = listIndexExists( list, 4, ",", false );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( false );
		instance.executeSource(
		    """
			list = "a,b,,c";
			result = listIndexExists( list, 4, ",", true );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( true );
	}

	@DisplayName( "It can us a member function to check for list indexes" )
	@Test
	public void testMemberFunction() {
		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = list.listIndexExists( 3 );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( true );
		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = list.listIndexExists( 4 );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( false );
		instance.executeSource(
		    """
		    list = "a,b,c";
		    result = list.listGetAt( 3 );
		    """,
		    context );
		assertThat( variables.get( result ) ).isEqualTo( "c" );

	}

}
