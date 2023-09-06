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
package ortus.boxlang.runtime.operators;

import ortus.boxlang.runtime.dynamic.Referencer;
import ortus.boxlang.runtime.dynamic.casters.DoubleCaster;
import ortus.boxlang.runtime.scopes.Key;

/**
 * Performs Math i++
 * {@code a = b++ or a = ++b}
 */
public class Increment implements IOperator {

	/**
	 * @param object The object to increment
	 *
	 * @return The result
	 */
	public static Double invoke( Object object ) {
		return DoubleCaster.cast( object ) + 1;
	}

	/**
	 * Apply this operator to an object/key and set the new value back in the same object/key
	 *
	 * @return The result
	 */
	public static Double invokePre( Object target, Key name ) {
		Double result = invoke( Referencer.get( target, name, false ) );
		Referencer.set( target, name, result );
		return result;
	}

	/**
	 * Apply this operator to an object/key and set the new value back in the same object/key
	 *
	 * @return The variable PRIOR to the operation
	 */
	public static Double invokePost( Object target, Key name ) {
		Double	original	= DoubleCaster.cast( Referencer.get( target, name, false ) );
		Double	result		= invoke( original );
		Referencer.set( target, name, result );
		return original;
	}

}
