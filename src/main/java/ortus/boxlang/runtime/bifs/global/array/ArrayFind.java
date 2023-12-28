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
package ortus.boxlang.runtime.bifs.global.array;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.bifs.BoxMember;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.operators.EqualsEquals;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.BoxLangType;
import ortus.boxlang.runtime.types.Function;

@BoxBIF
@BoxBIF( alias = "ArrayFindNoCase" )
@BoxMember( type = BoxLangType.ARRAY )
@BoxMember( type = BoxLangType.ARRAY, name = "findNoCase" )
public class ArrayFind extends BIF {

	/**
	 * Constructor
	 */
	public ArrayFind() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "array", Key.array ),
		    new Argument( true, "any", Key.value )
		};
	}

	/**
	 * Return int position of value in array, case sensitive
	 * 
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 * 
	 * @argument.array The array to be searched.
	 * 
	 * @argument.value The value to found.
	 */
	public Object invoke( IBoxContext context, ArgumentsScope arguments ) {
		Array	actualArray	= arguments.getAsArray( Key.array );
		Object	value		= arguments.get( Key.value );

		if ( value instanceof Function functionValue ) {
			return findOne( context, actualArray, functionValue );
		}

		return findOne( actualArray, value, isCaseSensitive( arguments.getAsKey( BIF.__functionName ) ) );
	}

	private int findOne( Array actualArray, Object value, boolean caseSensitive ) {
		for ( int i = 0; i < actualArray.size(); i++ ) {

			if ( EqualsEquals.invoke( actualArray.get( i ), value, caseSensitive ) ) {
				return i + 1;
			} else if ( actualArray.get( i ).equals( value ) ) {
				return i + 1;
			}
		}

		return 0;
	}

	private int findOne( IBoxContext context, Array actualArray, Function functionValue ) {
		for ( int i = 0; i < actualArray.size(); i++ ) {
			if ( ( boolean ) context.invokeFunction( functionValue, new Object[] { actualArray.get( i ) } ) ) {
				return i + 1;
			}
		}

		return 0;
	}

	private boolean isCaseSensitive( Key functionName ) {
		return functionName.equals( Key.find ) || functionName.equals( Key.arrayFind );
	}
}
