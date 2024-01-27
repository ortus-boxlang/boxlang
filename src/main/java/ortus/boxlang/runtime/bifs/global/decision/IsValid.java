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
package ortus.boxlang.runtime.bifs.global.decision;

import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.casters.GenericCaster;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Argument;

@BoxBIF
public class IsValid extends BIF {

	/**
	 * Constructor
	 */
	public IsValid() {
		super();
		declaredArguments = new Argument[] {
		    new Argument( true, "string", Key.type ),
		    new Argument( true, "any", Key.value ),
		    new Argument( false, "any", Key.min ),
		    new Argument( false, "any", Key.max ),
		    // @TODO: Add support for a `pattern` argument, but positionally it would be in the `min` slot, i.e. the 3rd argument.
		    // See https://docs.lucee.org/reference/functions/isvalid.html
		    new Argument( false, "any", Key.pattern ),
		};
	}

	/**
	 * Determine whether the given value is a valid instance of the given type.
	 *
	 * For example, validate that a given string is a valid email address, or that a given object is a valid struct.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.value Value to test for validity. A null is never valid.
	 */
	public Object invoke( IBoxContext context, ArgumentsScope arguments ) {
		return GenericCaster.attempt( arguments.get( Key.value ), arguments.getAsString( Key.type ) ).wasSuccessful();
	}
}