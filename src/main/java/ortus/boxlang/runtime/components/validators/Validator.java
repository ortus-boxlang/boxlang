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
package ortus.boxlang.runtime.components.validators;

import ortus.boxlang.runtime.components.Attribute;
import ortus.boxlang.runtime.components.Component;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.types.IStruct;

/**
 * I help validate attributes
 */
@FunctionalInterface
public interface Validator {

	public static final Validator	REQUIRED			= new Required();
	public static final Validator	NON_EMPTY			= new NonEmpty();
	public static final Validator	TYPE				= new Type();
	public static final Validator	REQUIRES			= new Requires();
	public static final Validator	GREATER_THAN_ZERO	= new GreaterThanZero();
	public static final Validator	DEFAULT_VALUE		= new DefaultValue();

	/**
	 * Validate an attribute.
	 * 
	 * @param context    The current Box context
	 * @param component  The component being validated
	 * @param attribute  The attribute being validated
	 * @param attributes The attributes being validated
	 */
	public void validate( IBoxContext context, Component component, Attribute attribute, IStruct attributes );

}