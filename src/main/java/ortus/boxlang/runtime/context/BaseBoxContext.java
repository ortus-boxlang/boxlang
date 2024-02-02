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
package ortus.boxlang.runtime.context;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.bifs.BIFDescriptor;
import ortus.boxlang.runtime.dynamic.casters.FunctionCaster;
import ortus.boxlang.runtime.dynamic.casters.StringCaster;
import ortus.boxlang.runtime.loader.ImportDefinition;
import ortus.boxlang.runtime.runnables.BoxTemplate;
import ortus.boxlang.runtime.runnables.ITemplateRunnable;
import ortus.boxlang.runtime.runnables.RunnableLoader;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.scopes.IScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.services.FunctionService;
import ortus.boxlang.runtime.types.Function;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Query;
import ortus.boxlang.runtime.types.QueryColumn;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.UDF;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;
import ortus.boxlang.runtime.types.exceptions.KeyNotFoundException;
import ortus.boxlang.runtime.types.exceptions.ScopeNotFoundException;

/**
 * This context represents the context of ANYTHING that can execute in BoxLang
 */
public class BaseBoxContext implements IBoxContext {

	/**
	 * --------------------------------------------------------------------------
	 * Private Properties
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Any context can have a parent it can delegate to
	 */
	protected IBoxContext					parent;

	/**
	 * A way to discover the current executing template
	 */
	protected ArrayDeque<ITemplateRunnable>	templates	= new ArrayDeque<>();

	/**
	 * A way to discover the current executing componenet
	 */
	protected ArrayDeque<IStruct>			components	= new ArrayDeque<>();

	/**
	 * A way to track query loops
	 */
	protected LinkedHashMap<Query, Integer>	queryLoops	= new LinkedHashMap<>();

	/**
	 * A buffer to write output to
	 */
	protected ArrayDeque<StringBuffer>		buffers		= new ArrayDeque<>();

	/**
	 * The function service we can use to retrieve BIFS and member methods
	 */
	private final FunctionService			functionService;

	/**
	 * Creates a new execution context with a bounded execution template and parent context
	 *
	 * @param parent The parent context
	 */
	public BaseBoxContext( IBoxContext parent ) {
		this.parent				= parent;
		this.functionService	= BoxRuntime.getInstance().getFunctionService();
		buffers.push( new StringBuffer() );
	}

	/**
	 * Creates a new execution context with no execution template
	 */
	public BaseBoxContext() {
		this( null );
	}

	/**
	 * --------------------------------------------------------------------------
	 * Getters & Setters
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Push a template to the stack
	 *
	 * @param template The template that this execution context is bound to
	 *
	 * @return IBoxContext
	 */
	public IBoxContext pushTemplate( ITemplateRunnable template ) {
		this.templates.push( template );
		return this;
	}

	/**
	 * Pop a template from the stack
	 *
	 * @return The template that this execution context is bound to
	 */
	public ITemplateRunnable popTemplate() {
		return this.templates.pop();
	}

	/**
	 * Push a Component to the stack
	 *
	 * @param name           The name of the component
	 * @param executionState The state for this component execution
	 *
	 * @return This context
	 */
	public IBoxContext pushComponent( IStruct executionState ) {
		this.components.push( executionState );
		return this;
	}

	/**
	 * Pop a template from the stack
	 *
	 * @return This context
	 */
	public IBoxContext popComponent() {
		this.components.pop();
		return this;
	}

	/**
	 * Gets the execution state for the closest component.
	 *
	 * @return The execution state for the closest component, null if none was found
	 */
	public IStruct findClosestComponent( Key name ) {
		IStruct[] componentArray = getComponents();
		for ( int i = 1; i < componentArray.length; i++ ) {
			IStruct component = componentArray[ i ];
			if ( component.get( Key._NAME ).equals( name ) ) {
				return component;
			}
		}
		return null;
	}

	/**
	 * Get the stack of components as an array
	 *
	 * @return This context
	 */
	public IStruct[] getComponents() {
		// get parent components and append our own
		if ( hasParent() ) {
			IStruct[]	parentComponents	= getParent().getComponents();
			IStruct[]	myComponents		= this.components.toArray( new IStruct[ 0 ] );
			IStruct[]	allComponents		= new IStruct[ parentComponents.length + myComponents.length ];
			System.arraycopy( parentComponents, 0, allComponents, 0, parentComponents.length );
			System.arraycopy( myComponents, 0, allComponents, parentComponents.length, myComponents.length );
			return allComponents;
		}
		return this.components.toArray( new IStruct[ 0 ] );
	}

	/**
	 * Has the execution context been bound to a template?
	 *
	 * @return True if bound, else false
	 */
	public boolean hasTemplates() {
		return !this.templates.isEmpty();
	}

	/**
	 * Finds the closest template
	 *
	 * @return The template instance if found, null if this code is not called from a template
	 */
	public ITemplateRunnable findClosestTemplate() {
		// If this context has templates, grab the first
		if ( hasTemplates() ) {
			return this.templates.peek();
		}

		// Otherwise, if we have a parent, ask them
		if ( hasParent() ) {
			return getParent().findClosestTemplate();
		}

		// There are none to be found!
		return null;
	}

	/**
	 * Finds the base (first) template in this request
	 *
	 * @return The template instance if found, null if this code is not called from a template
	 */
	public ITemplateRunnable findBaseTemplate() {
		ITemplateRunnable result = null;
		// If we have a parent, ask them
		if ( hasParent() ) {
			result = getParent().findBaseTemplate();
			if ( result != null ) {
				return result;
			}
		}
		// Otherwise, If this context has templates, grab the last
		if ( hasTemplates() ) {
			return this.templates.peekLast();
		}

		// There are none to be found!
		return null;
	}

	/**
	 * rethrows the closest exception
	 */
	public void rethrow() {

		// If we have a parent, ask them
		if ( hasParent() ) {
			getParent().rethrow();
		}

		throw new BoxRuntimeException( "No exception to rethrow.  You can only rethrow inside of a catch block." );
	}

	/**
	 * Returns the parent box context. Null if none.
	 *
	 * @return The parent box context. Null if none.
	 */
	public IBoxContext getParent() {
		return this.parent;
	}

	/**
	 * Sets the parent box context.
	 *
	 * @param parentContext The parent context to set
	 *
	 * @return This context
	 */
	public IBoxContext setParent( IBoxContext parentContext ) {
		this.parent = parentContext;
		return this;
	}

	/**
	 * Inject a parent context, moving the current parent to the grandparent
	 * Any existing parent in the passed context will be overwritten with the current parent
	 *
	 * @param parentContext The parent context to inject
	 *
	 * @return This context
	 */
	public IBoxContext injectParentContext( IBoxContext parentContext ) {
		parentContext.setParent( getParent() );
		setParent( parentContext );
		return this;
	}

	/**
	 * Inject a top parent context above the request-type context, moving the request context's current parent to its grandparent
	 *
	 * @param parentContext The parent context to inject
	 *
	 * @return This context
	 */
	public IBoxContext injectTopParentContext( IBoxContext parentContext ) {
		var requestContext = getParentOfType( RequestBoxContext.class );
		// If there is no request-type context (unlikely), just fall back to injecting our own parent
		if ( requestContext == null ) {
			return injectParentContext( parentContext );
		}
		requestContext.injectParentContext( parentContext );
		return this;
	}

	/**
	 * Verifies if a parent context is attached to this context
	 *
	 * @return True if a parent context is attached to this context, else false
	 */
	public Boolean hasParent() {
		return this.parent != null;
	}

	/**
	 * Invoke a function call such as foo() using positional args. Will check for a registered BIF first, then search known scopes for a UDF.
	 *
	 * @return Return value of the function call
	 */
	public Object invokeFunction( Key name, Object[] positionalArguments ) {
		BIFDescriptor bif = findBIF( name );
		if ( bif != null ) {
			return bif.invoke( this, positionalArguments, false, name );
		}

		Function function = findFunction( name );
		return invokeFunction( function, name, function.createArgumentsScope( positionalArguments ) );
	}

	/**
	 * Invoke a function call such as foo() using named args. Will check for a registered BIF first, then search known scopes for a UDF.
	 *
	 * @return Return value of the function call
	 */
	public Object invokeFunction( Key name, Map<Key, Object> namedArguments ) {
		BIFDescriptor bif = findBIF( name );
		if ( bif != null ) {
			return bif.invoke( this, namedArguments, false, name );
		}

		Function function = findFunction( name );
		return invokeFunction( function, name, function.createArgumentsScope( namedArguments ) );
	}

	/**
	 * Invoke a function call such as foo() using no args.
	 *
	 * @return Return value of the function call
	 */
	public Object invokeFunction( Key name ) {
		BIFDescriptor bif = findBIF( name );
		if ( bif != null ) {
			return bif.invoke( this, false );
		}

		Function function = findFunction( name );
		return invokeFunction( function, name, function.createArgumentsScope( new Object[] {} ) );
	}

	/**
	 * Invoke a function expression such as (()=>{})() using positional args.
	 * This method will validate the incoming object is a function type.
	 *
	 * @return Return value of the function call
	 */
	public Object invokeFunction( Object function, Object[] positionalArguments ) {
		Function func = FunctionCaster.cast( function );
		return invokeFunction( func, func.getName(), func.createArgumentsScope( positionalArguments ) );
	}

	/**
	 * Find out if the given function name is a BIF in the Function Service
	 *
	 * @param name The name of the function to find
	 *
	 * @return The BIFDescriptor if found, else null
	 */
	private BIFDescriptor findBIF( Key name ) {
		return this.functionService.getGlobalFunction( name );
	}

	/**
	 * Invoke a function expression such as (()=>{})() using named args.
	 * This method will validate the incoming object is a function type.
	 *
	 * @return Return value of the function call
	 */
	public Object invokeFunction( Object function, Map<Key, Object> namedArguments ) {
		Function func = FunctionCaster.cast( function );
		return invokeFunction( func, func.getName(), func.createArgumentsScope( namedArguments ) );
	}

	/**
	 * Invoke a function expression such as (()=>{})() using no args.
	 * This method will validate the incoming object is a function type.
	 *
	 * @return Return value of the function call
	 */
	public Object invokeFunction( Object function ) {
		Function func = FunctionCaster.cast( function );
		return invokeFunction( func, func.getName(), func.createArgumentsScope( new Object[] {} ) );
	}

	/**
	 * Invoke a function expression such as (()=>{})() using named args.
	 *
	 * @return Return value of the function call
	 */
	public Object invokeFunction( Function function, Key calledName, ArgumentsScope argumentsScope ) {
		return function.invoke( Function.generateFunctionContext( function, getFunctionParentContext(), calledName, argumentsScope ) );
	}

	/**
	 * Find a function in the corrent context. Will check for a registered BIF first, then search known scopes for a UDF.
	 *
	 * @param name The name of the function to find
	 *
	 * @return The function instance
	 */
	private Function findFunction( Key name ) {
		ScopeSearchResult result = null;
		try {
			result = scopeFindNearby( name, null );
		} catch ( KeyNotFoundException e ) {
			throw new BoxRuntimeException( "Function '" + name.getName() + "' not found" );
		}
		if ( result == null ) {
			throw new BoxRuntimeException( "Function '" + name.getName() + "' not found" );
		}
		Object value = result.value();
		if ( value instanceof Function fun ) {
			return fun;
		} else {
			throw new BoxRuntimeException(
			    "Variable '" + name + "' of type  '" + value.getClass().getName() + "'  is not a function." );
		}
	}

	/**
	 * Invoke a template in the current context
	 *
	 * @param templatePath A relateive template path
	 */
	public void includeTemplate( String templatePath ) {

		// Load template class, compiling if neccessary
		BoxTemplate template = RunnableLoader.getInstance().loadTemplateRelative( this, templatePath );

		template.invoke( this );
	}

	/**
	 * Get a scope from the context. If not found, the parent context is asked.
	 * Don't search for scopes which are nearby to an execution context
	 *
	 * @param name The name of the scope to get
	 *
	 * @return The requested scope
	 *
	 * @throws ScopeNotFoundException If the scope was not found in any context
	 */
	public IScope getScope( Key name ) throws ScopeNotFoundException {
		throw new BoxRuntimeException( "Unimplemented method 'getScope'" );
	}

	/**
	 * Get a scope from the context. If not found, the parent context is asked.
	 * Search all known scopes
	 *
	 * @param name    The name of the scope to get
	 * @param shallow true, do not delegate to parent or default scope if not found
	 *
	 * @return The requested scope
	 *
	 * @throws ScopeNotFoundException If the scope was not found in any context
	 */
	public IScope getScopeNearby( Key name, boolean shallow ) throws ScopeNotFoundException {
		throw new BoxRuntimeException( "Unimplemented method 'getScopeNearby'" );
	}

	/**
	 * Try to get the requested key from an unknown scope
	 * Meaning it needs to search scopes in order according to it's context.
	 * Unlike scopeFindNearby(), this version only searches trancedent scopes like
	 * cgi or server which are never encapsulated like variables is inside a CFC.
	 *
	 * If defaultScope is null and the key can't be found, a KeyNotFoundException will be thrown
	 * If defaultScope is not null, it will return a record with the default scope and null value if the key is not found
	 *
	 * @param key The key to search for
	 *
	 * @return The value of the key if found
	 *
	 */
	public ScopeSearchResult scopeFind( Key key, IScope defaultScope ) {
		throw new BoxRuntimeException( "Unimplemented method 'scopeFind'" );
	}

	/**
	 * Try to get the requested key from an unknown scope
	 * Meaning it needs to search scopes in order according to it's context.
	 * A nearby lookup is used for the closest context to the executing code
	 *
	 * If defaultScope is null and the key can't be found, a KeyNotFoundException will be thrown
	 * If defaultScope is not null, it will return a record with the default scope and null value if the key is not found
	 *
	 * @param key The key to search for
	 *
	 * @return The value of the key if found
	 *
	 */
	public ScopeSearchResult scopeFindNearby( Key key, IScope defaultScope, boolean shallow ) {
		throw new BoxRuntimeException( "Unimplemented method 'scopeFindNearby'" );
	}

	/**
	 * Search any query loops for a column name matching the uncscoped variable
	 * 
	 * @param key The key to search for
	 * 
	 * @return A ScopeSearchResult if found, else null
	 */
	protected ScopeSearchResult queryFindNearby( Key key ) {
		if ( queryLoops.size() > 0 ) {
			var queries = queryLoops.keySet().toArray( new Query[ 0 ] );
			for ( int i = queries.length - 1; i >= 0; i-- ) {
				Query query = queries[ i ];
				if ( key.equals( Key.recordCount ) ) {
					return new ScopeSearchResult( null, query.size() );
				}
				if ( key.equals( Key.currentRow ) ) {
					return new ScopeSearchResult( null, queryLoops.get( query ) + 1 );
				}
				if ( key.equals( Key.columnList ) ) {
					return new ScopeSearchResult( null, query.getColumnList() );
				}
				if ( query.hasColumn( key ) ) {
					// TODO: create query scope wrapper for edge cases
					return new ScopeSearchResult( null, query.getCell( key, queryLoops.get( query ) ) );
				}
			}
		}
		return null;
	}

	/**
	 * Register a UDF with the local context.
	 *
	 * @param udf The UDF to register
	 */
	public void registerUDF( UDF udf ) {
		throw new BoxRuntimeException( "This context cannot register a function" );
	}

	/**
	 * Get the default variable assignment scope for this context
	 *
	 * @return The scope reference to use
	 */
	public IScope getDefaultAssignmentScope() {
		throw new BoxRuntimeException( "Unimplemented method 'getDefaultAssignmentScope'" );
	}

	/**
	 * Finds the closest function call name
	 *
	 * @return The called name of the function if found, null if this code is not called from a function
	 */
	public Key findClosestFunctionName() {
		if ( hasParent() ) {
			return getParent().findClosestFunctionName();
		}
		return null;
	}

	/**
	 * Get parent context for a function execution happening in this context
	 *
	 * @return The context to use
	 */
	public IBoxContext getFunctionParentContext() {
		return this;
	}

	/**
	 * Try to get the requested key from an unkonwn scope but overriding the parent to check if not found
	 *
	 * @param key The key to search for
	 *
	 * @return The value of the key if found
	 *
	 */
	public ScopeSearchResult scopeFindNearby( Key key, IScope defaultScope ) {
		return scopeFindNearby( key, defaultScope, false );
	}

	/**
	 * Get a scope from the context. If not found, the parent context is asked.
	 * Search all known scopes
	 *
	 * @param name The name of the scope to get
	 *
	 * @return The requested scope
	 *
	 * @throws ScopeNotFoundException If the scope was not found in any context
	 */
	public IScope getScopeNearby( Key name ) throws ScopeNotFoundException {
		return getScopeNearby( name, false );
	}

	/**
	 * Retrieve all known imports for the current template
	 *
	 * @return List of import definitions
	 */
	public List<ImportDefinition> getCurrentImports() {
		ITemplateRunnable template = findClosestTemplate();
		if ( template == null ) {
			return null;
		}

		return template.getImports();
	}

	/**
	 * If input is a QueryColumn, unwrap it to the underlying value
	 * If input is not a QueryColumn, return it as-is
	 *
	 * @param value The value to unwrap
	 *
	 * @return The unwrapped value
	 */
	public Object unwrapQueryColumn( Object value ) {
		if ( value instanceof QueryColumn col ) {
			return col.getCell( getQueryRow( col.getQuery() ) );
		}
		return value;
	}

	/**
	 * Get the current query row
	 *
	 * @param query The query to get the row from
	 *
	 * @return The current row
	 */
	public int getQueryRow( Query query ) {
		// If we're not looping over this query, then we're on the first row
		if ( !queryLoops.containsKey( query ) ) {
			return 0;
		}
		return queryLoops.get( query );

	}

	/**
	 * Register a query loop
	 *
	 * @param query The query to register
	 */
	public void registerQueryLoop( Query query, int row ) {
		queryLoops.put( query, row );
	}

	/**
	 * Unregister a query loop
	 *
	 * @param query The query to unregister
	 */
	public void unregisterQueryLoop( Query query ) {
		queryLoops.remove( query );
	}

	/**
	 * Increment the query loop
	 *
	 * @param query The query to increment
	 */
	public void incrementQueryLoop( Query query ) {
		queryLoops.put( query, queryLoops.get( query ) + 1 );
	}

	/**
	 * Write output to this buffer. Any input object will be converted to a string
	 *
	 * @param o The object to write
	 *
	 * @return This context
	 */
	public IBoxContext writeToBuffer( Object o ) {
		getBuffer().append( StringCaster.cast( o ) );
		return this;
	}

	/**
	 * Flush the buffer to the output stream and then clears the local buffers
	 *
	 * @param force true, flush even if output is disabled
	 *
	 * @return This context
	 */
	public IBoxContext flushBuffer( boolean force ) {
		// If there are extra buffers registered, we ignore flush requests since someone
		// out there is wanting to capture our buffer instead.
		if ( hasParent() && buffers.size() == 1 ) {
			StringBuffer thisBuffer = getBuffer();
			synchronized ( thisBuffer ) {
				getParent().writeToBuffer( thisBuffer.toString() );
				thisBuffer.setLength( 0 );
			}
		}
		return this;
	}

	/**
	 * Clear the buffer
	 *
	 * @return This context
	 */
	public IBoxContext clearBuffer() {
		getBuffer().setLength( 0 );
		return this;
	}

	/**
	 * Get the buffer
	 *
	 * @return The buffer
	 */
	public StringBuffer getBuffer() {
		return this.buffers.peek();
	}

	/**
	 * Push a buffer onto the stack. This is mostly so components can capture any output generated in their body
	 * 
	 * @param buffer The buffer to push
	 * 
	 * @return This context
	 */
	public IBoxContext pushBuffer( StringBuffer buffer ) {
		this.buffers.push( buffer );
		return this;
	}

	/**
	 * Pop a buffer from the stack
	 * 
	 * @return This context
	 */
	public IBoxContext popBuffer() {
		this.buffers.pop();
		return this;
	}

	/**
	 * Get the contexual config struct. Each context has a chance to add in config of their
	 * own to the struct, or override existing config with a new struct of their own design.
	 * It depends on whether the context wants its changes to exist for the rest of the entire
	 * request or only for code that executes in the current context and below.
	 *
	 * @return A struct of configuration
	 */
	public IStruct getConfig() {
		if ( hasParent() ) {
			return getParent().getConfig();
		}
		return new Struct();
	}

	/**
	 * Convenience method to retrieve a config item
	 *
	 * @param itemKey the object key
	 *
	 * @return
	 */
	public Object getConfigItem( Key itemKey ) {
		return getConfig().get( itemKey );
	}

	/**
	 * Convenience method to retrieve a config item with with an optional default
	 *
	 * @param itemKey      the object key
	 * @param defaultValue a default value to return
	 *
	 * @return
	 */
	public Object getConfigItem( Key itemKey, Object defaultValue ) {
		return getConfig().getOrDefault( itemKey, defaultValue );
	}

	/**
	 * Get the BoxLang runtime
	 * '
	 *
	 * @return The runtime
	 */
	public BoxRuntime getRuntime() {
		return BoxRuntime.getInstance();
	}

	/**
	 * Serach for an ancestor context of the given type
	 * 
	 * @param <T> The type of context to search for
	 *
	 * @return The matching parent context, or null if one is not found of this type.
	 */
	@Override
	@SuppressWarnings( "unchecked" )
	public <T> T getParentOfType( Class<T> type ) {
		if ( type.isAssignableFrom( this.getClass() ) ) {
			return ( T ) this;
		}
		if ( hasParent() ) {
			return ( T ) getParent().getParentOfType( type );
		}
		return null;
	}

}
