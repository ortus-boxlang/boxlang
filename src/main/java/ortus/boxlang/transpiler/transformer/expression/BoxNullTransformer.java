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
package ortus.boxlang.transpiler.transformer.expression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.NullLiteralExpr;

import ortus.boxlang.ast.BoxNode;
import ortus.boxlang.transpiler.JavaTranspiler;
import ortus.boxlang.transpiler.transformer.AbstractTransformer;
import ortus.boxlang.transpiler.transformer.TransformerContext;

/**
 * Transform a BoxIntegerLiteral Node the equivalent Java Parser AST nodes
 */
public class BoxNullTransformer extends AbstractTransformer {

	Logger logger = LoggerFactory.getLogger( BoxNullTransformer.class );

	public BoxNullTransformer( JavaTranspiler transpiler ) {
		super( transpiler );
	}

	/**
	 * Transform BoxIntegerLiteral argument
	 *
	 * @param node    a BoxIntegerLiteral instance
	 * @param context transformation context
	 *
	 * @return generates a Java Parser integer Literal
	 */
	@Override
	public Node transform( BoxNode node, TransformerContext context ) throws IllegalStateException {
		Node javaExpr = new NullLiteralExpr();
		logger.info( node.getSourceText() + " -> " + javaExpr );
		addIndex( javaExpr, node );
		return javaExpr;
	}
}