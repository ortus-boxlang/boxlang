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
package ortus.boxlang.ast.expression;

import ortus.boxlang.ast.BoxExpr;
import ortus.boxlang.ast.Position;

/**
 * AST Node representing access with a dot like:
 * <code>object.property</code> or <code>object.method()</code>
 */
public class BoxDotAccess extends BoxAccess {

	/**
	 * Creates the AST node
	 *
	 * @param context    expression representing the object
	 * @param access     expression after the dot
	 * @param safe       boolean save operation
	 * @param position   position of the statement in the source code
	 * @param sourceText source code that originated the Node
	 */
	public BoxDotAccess( BoxExpr context, Boolean safe, BoxExpr access, Position position, String sourceText ) {
		super( context, safe, access, position, sourceText );
	}

}