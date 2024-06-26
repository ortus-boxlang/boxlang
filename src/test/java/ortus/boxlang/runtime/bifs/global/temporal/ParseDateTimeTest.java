
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

package ortus.boxlang.runtime.bifs.global.temporal;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.context.ScriptingRequestBoxContext;
import ortus.boxlang.runtime.dynamic.casters.IntegerCaster;
import ortus.boxlang.runtime.dynamic.casters.StringCaster;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.VariablesScope;
import ortus.boxlang.runtime.types.DateTime;

public class ParseDateTimeTest {

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

	@DisplayName( "It tests the BIF ParseDateTime with a full ISO including offset" )
	@Test
	public void testParseDateTimeFullISO() {
		instance.executeSource(
		    """
		    result = parseDateTime( "2024-01-14T00:00:01.0001Z" );
		    """,
		    context );
		DateTime result = ( DateTime ) variables.get( Key.of( "result" ) );
		assertThat( result ).isInstanceOf( DateTime.class );
		assertThat( result.toString() ).isInstanceOf( String.class );
		assertThat( IntegerCaster.cast( result.format( "yyyy" ) ) ).isEqualTo( 2024 );
		assertThat( IntegerCaster.cast( result.format( "M" ) ) ).isEqualTo( 1 );
		assertThat( IntegerCaster.cast( result.format( "d" ) ) ).isEqualTo( 14 );
		assertThat( IntegerCaster.cast( result.format( "H" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "m" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "s" ) ) ).isEqualTo( 1 );
		assertThat( IntegerCaster.cast( result.format( "n" ) ) ).isEqualTo( 100000 );
	}

	@DisplayName( "It tests the BIF ParseDateTime with a full ISO without offset" )
	@Test
	public void testParseDateTimeNoOffset() {
		instance.executeSource(
		    """
		    result = parseDateTime( "2024-01-14T00:00:01.0001" );
		    """,
		    context );
		DateTime result = ( DateTime ) variables.get( Key.of( "result" ) );
		assertThat( result ).isInstanceOf( DateTime.class );
		assertThat( result.toString() ).isInstanceOf( String.class );
		assertThat( IntegerCaster.cast( result.format( "yyyy" ) ) ).isEqualTo( 2024 );
		assertThat( IntegerCaster.cast( result.format( "M" ) ) ).isEqualTo( 1 );
		assertThat( IntegerCaster.cast( result.format( "d" ) ) ).isEqualTo( 14 );
		assertThat( IntegerCaster.cast( result.format( "H" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "m" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "s" ) ) ).isEqualTo( 1 );
		assertThat( IntegerCaster.cast( result.format( "n" ) ) ).isEqualTo( 100000 );
	}

	@DisplayName( "It tests the BIF ParseDateTime with without any time" )
	@Test
	public void testParseDateTimeNoTime() {
		instance.executeSource(
		    """
		    result = parseDateTime( "2024-01-14" );
		    """,
		    context );
		DateTime result = ( DateTime ) variables.get( Key.of( "result" ) );
		assertThat( result ).isInstanceOf( DateTime.class );
		assertThat( result.toString() ).isInstanceOf( String.class );
		assertThat( IntegerCaster.cast( result.format( "yyyy" ) ) ).isEqualTo( 2024 );
		assertThat( IntegerCaster.cast( result.format( "M" ) ) ).isEqualTo( 1 );
		assertThat( IntegerCaster.cast( result.format( "d" ) ) ).isEqualTo( 14 );
		assertThat( IntegerCaster.cast( result.format( "H" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "m" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "s" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "n" ) ) ).isEqualTo( 0 );
	}

	@DisplayName( "It tests the BIF ParseDateTime with a dot-delimited date" )
	@Test
	public void testParseDateTimeDateDot() {
		instance.executeSource(
		    """
		    result = parseDateTime( "2024.01.14" );
		    """,
		    context );
		DateTime result = ( DateTime ) variables.get( Key.of( "result" ) );
		assertThat( result ).isInstanceOf( DateTime.class );
		assertThat( result.toString() ).isInstanceOf( String.class );
		assertThat( IntegerCaster.cast( result.format( "yyyy" ) ) ).isEqualTo( 2024 );
		assertThat( IntegerCaster.cast( result.format( "M" ) ) ).isEqualTo( 1 );
		assertThat( IntegerCaster.cast( result.format( "d" ) ) ).isEqualTo( 14 );
		assertThat( IntegerCaster.cast( result.format( "H" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "m" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "s" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "n" ) ) ).isEqualTo( 0 );
	}

	@DisplayName( "It tests the BIF ParseDateTime in mm/dd/yyyy format" )
	@Test
	public void testParseDateSlashes() {
		instance.executeSource(
		    """
		    result = parseDateTime( "01/14/2024" );
		    """,
		    context );
		DateTime result = ( DateTime ) variables.get( Key.of( "result" ) );
		assertThat( result ).isInstanceOf( DateTime.class );
		assertThat( result.toString() ).isInstanceOf( String.class );
		assertThat( IntegerCaster.cast( result.format( "yyyy" ) ) ).isEqualTo( 2024 );
		assertThat( IntegerCaster.cast( result.format( "M" ) ) ).isEqualTo( 1 );
		assertThat( IntegerCaster.cast( result.format( "d" ) ) ).isEqualTo( 14 );
		assertThat( IntegerCaster.cast( result.format( "H" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "m" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "s" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "n" ) ) ).isEqualTo( 0 );
	}

	@DisplayName( "It tests the BIF ParseDateTime with a format argument" )
	@Test
	public void testParseDateTimeFormat() {
		variables.put( Key.of( "format" ), "yyyy-MM-dd'T'HH:mm:ss.SSSSX" );
		instance.executeSource(
		    """
		    result = parseDateTime( "2024-01-14T00:00:01.0001Z", format );
		    """,
		    context );
		DateTime result = ( DateTime ) variables.get( Key.of( "result" ) );
		assertThat( result ).isInstanceOf( DateTime.class );
		assertThat( result.toString() ).isInstanceOf( String.class );
		assertThat( IntegerCaster.cast( result.format( "yyyy" ) ) ).isEqualTo( 2024 );
		assertThat( IntegerCaster.cast( result.format( "M" ) ) ).isEqualTo( 1 );
		assertThat( IntegerCaster.cast( result.format( "d" ) ) ).isEqualTo( 14 );
		assertThat( IntegerCaster.cast( result.format( "H" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "m" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "s" ) ) ).isEqualTo( 1 );
		assertThat( IntegerCaster.cast( result.format( "n" ) ) ).isEqualTo( 100000 );
	}

	@DisplayName( "It tests the BIF ParseDateTime with a timezone argument" )
	@Test
	public void testParseDateTimeTimezone() {
		variables.put( Key.of( "format" ), "yyyy-MM-dd HH:mm:ss" );
		instance.executeSource(
		    """
		    result = parseDateTime( "2024-01-14 00:00:01", format, "America/Los_Angeles" );
		    """,
		    context );
		DateTime result = ( DateTime ) variables.get( Key.of( "result" ) );
		assertThat( result ).isInstanceOf( DateTime.class );
		assertThat( result.toString() ).isInstanceOf( String.class );
		assertThat( IntegerCaster.cast( result.format( "yyyy" ) ) ).isEqualTo( 2024 );
		assertThat( IntegerCaster.cast( result.format( "M" ) ) ).isEqualTo( 1 );
		assertThat( IntegerCaster.cast( result.format( "d" ) ) ).isEqualTo( 14 );
		assertThat( IntegerCaster.cast( result.format( "H" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "m" ) ) ).isEqualTo( 0 );
		assertThat( IntegerCaster.cast( result.format( "s" ) ) ).isEqualTo( 1 );
		assertThat( IntegerCaster.cast( result.format( "n" ) ) ).isEqualTo( 0 );
		assertThat( StringCaster.cast( result.format( "z" ) ).charAt( 0 ) ).isEqualTo( "P".charAt( 0 ) );
		assertThat( StringCaster.cast( result.format( "z" ) ).charAt( 2 ) ).isEqualTo( "T".charAt( 0 ) );
	}

}
