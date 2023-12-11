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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ortus.boxlang.ast.BoxExpr;
import ortus.boxlang.ast.Position;

/**
 * AST Node representing an invoked expression
 */
public class BoxExpressionInvocation extends BoxExpr {

	private final BoxExpr expr;

	public BoxExpr getExpr() {
		return expr;
	}

	private final List<BoxArgument> arguments;

	public List<BoxArgument> getArguments() {
		return arguments;
	}

	/**
	 * Function invocation i.e. create(x)
	 *
	 * @param expr       expression to invoke
	 * @param arguments  list of arguments
	 * @param position   position of the statement in the source code
	 * @param sourceText source code that originated the Node
	 */
	public BoxExpressionInvocation( BoxExpr expr, List<BoxArgument> arguments, Position position, String sourceText ) {
		super( position, sourceText );
		this.expr		= expr;
		this.arguments	= Collections.unmodifiableList( arguments );
		this.arguments.forEach( arg -> arg.setParent( this ) );
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		map.put( "expr", expr.toMap() );
		map.put( "arguments", arguments.stream().map( BoxExpr::toMap ).collect( Collectors.toList() ) );
		return map;
	}
}