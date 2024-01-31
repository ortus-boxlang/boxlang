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

public class ListCompactTest {

	static BoxRuntime instance;
	static IBoxContext context;
	static IScope variables;
	static Key result = new Key("result");

	@BeforeAll
	public static void setUp() {
		instance = BoxRuntime.getInstance(true);
		context = new ScriptingRequestBoxContext(instance.getRuntimeContext());
		variables = context.getScopeNearby(VariablesScope.name);
	}

	@AfterAll
	public static void teardown() {
		instance.shutdown();
	}

	@BeforeEach
	public void setupEach() {
		variables.clear();
	}

	@DisplayName(
		"It removes the default comma delimiter from the start and end of a list"
	)
	@Test
	public void testCompactDefault() {
		instance.executeSource(
			"""
		    	list = ",a,b,c,";
		    	result = listCompact( list );
		    """,
			context
		);
		assertThat(variables.get(result)).isEqualTo("a,b,c");
		instance.executeSource(
			"""
		    list = ",a,b,c";
		    result = listCompact( list );
		    """,
			context
		);
		assertThat(variables.get(result)).isEqualTo("a,b,c");
	}

	@DisplayName(
		"It removes multiple instances of the same delimiter from the start and end of a list"
	)
	@Test
	public void testCompactMultiple() {
		instance.executeSource(
			"""
		    	list = ",,,,,,,a,b,c,,,";
		    	result = listCompact( list );
		    """,
			context
		);
		assertThat(variables.get(result)).isEqualTo("a,b,c");
	}

	@DisplayName("It can compact using a custom delimiter")
	@Test
	public void testCompactCustomDelimiter() {
		instance.executeSource(
			"""
		    	list = ":::a:b:c:";
		    	result = listCompact( list, ":" );
		    """,
			context
		);
		assertThat(variables.get(result)).isEqualTo("a:b:c");
	}

	@DisplayName("It can compact using a multi-character delimiter")
	@Test
	public void testMultiCharacterDelimiter() {
		instance.executeSource(
			"""
		    	list = ":::a::b::c:";
		    	result = listCompact( list, "::", true );
		    """,
			context
		);
		assertThat(variables.get(result)).isEqualTo(":a::b::c:");

		instance.executeSource(
			"""
		    	list = ":::a:::b:::c:";
		    	result = listCompact( list, ":::", true );
		    """,
			context
		);
		assertThat(variables.get(result)).isEqualTo("a:::b:::c:");
	}

	@DisplayName(
		"It uses a single character if the multi-character delimiter flag is false"
	)
	@Test
	public void testSingleCharacterDelimiterWhenMultiDelimiterFlagIsFalse() {
		instance.executeSource(
			"""
		    	list = ":::a:b:c:";
		    	result = listCompact( list, "::", false );
		    """,
			context
		);
		assertThat(variables.get(result)).isEqualTo("a:b:c");

		instance.executeSource(
			"""
		    	list = ":::a:b:c:";
		    	result = listCompact( list, ":::", false );
		    """,
			context
		);
		assertThat(variables.get(result)).isEqualTo("a:b:c");
	}

	@DisplayName("It can use the member function")
	@Test
	public void testMemberFunction() {
		instance.executeSource(
			"""
		    	list = ",,,,a,b,c,";
		    	result = list.listCompact();
		    """,
			context
		);
		assertThat(variables.get(result)).isEqualTo("a,b,c");
		instance.executeSource(
			"""
		    	list = "::::a::b::c:::";
		    	result = list.listCompact( "::", true );
		    """,
			context
		);
		assertThat(variables.get(result)).isEqualTo("a::b::c:::");
	}
}
