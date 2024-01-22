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

package ortus.boxlang.runtime.bifs.global.array;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;
import ortus.boxlang.runtime.types.IStruct;

public class ArrayUnshiftTest {

	static BoxRuntime	instance;
	static IBoxContext	context;
	static IStruct		variables;
	static Key			result	= new Key( "result" );

	@BeforeAll
	public static void setUp() {
		instance	= BoxRuntime.getInstance( true );
		context		= new ScriptingRequestBoxContext( instance.getRuntimeContext() );
		variables	= ( IStruct ) context.getScopeNearby( VariablesScope.name );
	}

	@AfterAll
	public static void teardown() {
		instance.shutdown();
	}

	@BeforeEach
	public void setupEach() {
		variables.clear();
	}

	@DisplayName( "It should grow the array" )
	@Test
	public void testSize() {
		instance.executeSource(
		    """
		          result = [ "b", "c" ];
		    ArrayUnshift( result, "a" );
		      """,
		    context );
		assertThat( variables.getAsArray( result ).size() ).isEqualTo( 3 );
	}

	@DisplayName( "It should set the first element to the new value" )
	@Test
	public void testFirst() {
		instance.executeSource(
		    """
		        result = [ "b", "c" ];
		    ArrayUnshift( result, "a" );
		    """,
		    context );
		assertThat( variables.getAsArray( result ).get( 0 ) ).isEqualTo( "a" );
	}

	@DisplayName( "It should return the new size" )
	@Test
	public void testReturnSize() {
		instance.executeSource(
		    """
		        result = [ "b", "c" ];
		    result = ArrayUnshift( result, "a" );
		    """,
		    context );
		assertThat( variables.getAsInteger( result ) ).isEqualTo( 3 );
	}

	@DisplayName( "It should allow you to call it as a member function" )
	@Test
	public void testMemberInvocation() {
		instance.executeSource(
		    """
		        result = [ "b", "c" ];
		    	result = result.unshift( "a" );
		    """,
		    context );
		assertThat( variables.getAsInteger( result ) ).isEqualTo( 3 );
	}
}
