package ortus.boxlang.ast.statement;

import java.util.Map;

import ortus.boxlang.ast.BoxExpr;
import ortus.boxlang.ast.BoxStatement;
import ortus.boxlang.ast.Position;

/**
 * AST Node representing an throw statement
 */
public class BoxThrow extends BoxStatement {

	private final BoxExpr expression;

	/**
	 * Creates the AST node
	 *
	 * @param expression argument expression to assert
	 * @param position   position of the statement in the source code
	 * @param sourceText source code that originated the Node
	 */
	public BoxThrow( BoxExpr expression, Position position, String sourceText ) {
		super( position, sourceText );
		this.expression = expression;
		this.expression.setParent( this );
	}

	public BoxExpr getExpression() {
		return expression;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		map.put( "expression", expression.toMap() );
		return map;
	}
}
