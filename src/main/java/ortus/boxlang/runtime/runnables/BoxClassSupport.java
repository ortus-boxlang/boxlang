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
package ortus.boxlang.runtime.runnables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ortus.boxlang.runtime.context.FunctionBoxContext;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.casters.BooleanCaster;
import ortus.boxlang.runtime.scopes.BaseScope;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.scopes.ThisScope;
import ortus.boxlang.runtime.scopes.VariablesScope;
import ortus.boxlang.runtime.types.Array;
import ortus.boxlang.runtime.types.Function;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Property;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;
import ortus.boxlang.runtime.types.meta.BoxMeta;
import ortus.boxlang.runtime.types.meta.ClassMeta;

/**
 * The methods in this class are an extension of IClassRunnable. They are here for better readability
 * since IClassRunnables don't extend a base class, there are placeholders in the BoxClassTransformer that
 * delegate to these methods.
 */
public class BoxClassSupport {

	public static void pseudoConstructor( IClassRunnable thisClass, IBoxContext context ) {
		context.pushTemplate( thisClass );
		try {
			// loop over properties and create variables.
			for ( var property : thisClass.getProperties().values() ) {
				if ( thisClass.getVariablesScope().get( property.name() ) == null ) {
					thisClass.getVariablesScope().assign( context, property.name(), property.defaultValue() );
				}
			}
			// TODO: pre/post interceptor announcements here
			thisClass._pseudoConstructor( context );
		} finally {
			context.popTemplate();
		}
	}

	public static BoxMeta getBoxMeta( IClassRunnable thisClass ) {
		if ( thisClass._getbx() == null ) {
			thisClass._setbx( new ClassMeta( thisClass ) );
		}
		return thisClass._getbx();
	}

	/**
	 * Represent as string, or throw exception if not possible
	 *
	 * @return The string representation
	 */
	public static String asString( IClassRunnable thisClass ) {
		return "Class: " + thisClass.getName().getName();
	}

	/**
	 * A helper to look at the "output" annotation, caching the result
	 *
	 * @return Whether the function can output
	 */
	public static boolean canOutput( IClassRunnable thisClass ) {
		// Initialize if neccessary
		if ( thisClass.getCanOutput() == null ) {
			thisClass.setCanOutput( BooleanCaster.cast(
			    thisClass.getAnnotations()
			        .getOrDefault(
			            Key.output,
			            false
			        )
			) );
		}
		return thisClass.getCanOutput();
	}

	/**
	 * Set the super class.
	 */
	public static void setSuper( IClassRunnable thisClass, IClassRunnable _super ) {
		thisClass._setSuper( _super );
		_super.setChild( thisClass );
		// This runs before the psedu constructor and init, so the base class will override anything it declares
		// System.out.println( "Setting super class: " + _super.getName().getName() + " into " + thisClass.getName().getName() );
		// System.out.println( "Setting super class variables: " + _super.getVariablesScope().asString() );
		thisClass.getVariablesScope().addAll( _super.getVariablesScope().getWrapped() );
		thisClass.getThisScope().addAll( _super.getThisScope().getWrapped() );

		// merge properties that don't already exist
		for ( var entry : _super.getProperties().entrySet() ) {
			if ( !thisClass.getProperties().containsKey( entry.getKey() ) ) {
				thisClass.getProperties().put( entry.getKey(), entry.getValue() );
			}
		}
		// merge getterLookup and setterLookup
		thisClass.getGetterLookup().putAll( _super.getGetterLookup() );
		thisClass.getSetterLookup().putAll( _super.getSetterLookup() );

		// merge annotations
		for ( var entry : _super.getAnnotations().entrySet() ) {
			Key key = entry.getKey();
			if ( !thisClass.getAnnotations().containsKey( key ) && !key.equals( Key._EXTENDS ) && !key.equals( Key._IMPLEMENTS ) ) {
				thisClass.getAnnotations().put( key, entry.getValue() );
			}
		}

	}

	/**
	 * Get the bottom class in the inheritance chain
	 */
	public static IClassRunnable getBottomClass( IClassRunnable thisClass ) {
		if ( thisClass.getChild() != null ) {
			return thisClass.getChild().getBottomClass();
		}
		return thisClass;
	}

	/**
	 * --------------------------------------------------------------------------
	 * IReferenceable Interface Methods
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Assign a value to a key
	 *
	 * @param key   The key to assign
	 * @param value The value to assign
	 */
	public static Object assign( IClassRunnable thisClass, IBoxContext context, Key key, Object value ) {
		// TODO: implicit setters
		thisClass.getThisScope().assign( context, key, value );
		return value;
	}

	/**
	 * Dereference this object by a key and return the value, or throw exception
	 *
	 * @param key  The key to dereference
	 * @param safe Whether to throw an exception if the key is not found
	 *
	 * @return The requested object
	 */
	public static Object dereference( IClassRunnable thisClass, IBoxContext context, Key key, Boolean safe ) {

		// Special check for $bx
		if ( key.equals( BoxMeta.key ) ) {
			return thisClass.getBoxMeta();
		}

		// TODO: implicit getters
		return thisClass.getThisScope().dereference( context, key, safe );
	}

	/**
	 * Dereference this object by a key and invoke the result as an invokable (UDF, java method) using positional arguments
	 *
	 * @param name                The key to dereference
	 * @param positionalArguments The positional arguments to pass to the invokable
	 * @param safe                Whether to throw an exception if the key is not found
	 *
	 * @return The requested object
	 */
	public static Object dereferenceAndInvoke( IClassRunnable thisClass, IBoxContext context, Key name, Object[] positionalArguments, Boolean safe ) {
		// TODO: component member methods?

		BaseScope scope = thisClass.getThisScope();
		// we are a super class, so we reached here via super.method()
		if ( thisClass.getChild() != null ) {
			scope = thisClass.getVariablesScope();
		}

		// Look for function in this
		Object value = scope.get( name );
		if ( value instanceof Function function ) {
			FunctionBoxContext functionContext = Function.generateFunctionContext(
			    function,
			    // Function contexts' parent is the caller. The function will "know" about the CFC it's executing in
			    // because we've pushed the CFC onto the template stack in the function context.
			    context,
			    name,
			    positionalArguments,
			    thisClass
			);

			functionContext.setThisClass( thisClass );
			return function.invoke( functionContext );
		}

		if ( value != null ) {
			throw new BoxRuntimeException(
			    "key '" + name.getName() + "' of type  '" + value.getClass().getName() + "'  is not a function " );
		}

		// Check for generated accessors
		Object hasAccessors = thisClass.getAnnotations().get( Key.accessors );
		if ( hasAccessors != null && BooleanCaster.cast( hasAccessors ) ) {
			Property getterProperty = thisClass.getGetterLookup().get( name );
			if ( getterProperty != null ) {
				return thisClass.getBottomClass().getVariablesScope().dereference( context, thisClass.getGetterLookup().get( name ).name(), safe );
			}
			Property setterProperty = thisClass.getSetterLookup().get( name );
			// System.out.println( "setterProperty lookup: " + setterProperty );
			if ( setterProperty != null ) {
				Key thisName = setterProperty.name();
				if ( positionalArguments.length == 0 ) {
					throw new BoxRuntimeException( "Missing argument for setter '" + name.getName() + "'" );
				}
				thisClass.getBottomClass().getVariablesScope().assign( context, thisName, positionalArguments[ 0 ] );
				return thisClass;
			}
		}

		if ( thisClass.getThisScope().get( Key.onMissingMethod ) != null ) {
			return thisClass.dereferenceAndInvoke( context, Key.onMissingMethod, new Object[] { name.getName(), positionalArguments }, safe );
		}

		if ( !safe ) {
			throw new BoxRuntimeException( "Method '" + name.getName() + "' not found" );
		}
		return null;
	}

	/**
	 * Dereference this object by a key and invoke the result as an invokable (UDF, java method)
	 *
	 * @param name           The name of the key to dereference, which becomes the method name
	 * @param namedArguments The arguments to pass to the invokable
	 * @param safe           If true, return null if the method is not found, otherwise throw an exception
	 *
	 * @return The requested return value or null
	 */
	public static Object dereferenceAndInvoke( IClassRunnable thisClass, IBoxContext context, Key name, Map<Key, Object> namedArguments, Boolean safe ) {

		BaseScope scope = thisClass.getThisScope();
		// we are a super class, so we reached here via super.method()
		if ( thisClass.getChild() != null ) {
			scope = thisClass.getVariablesScope();
		}

		Object value = scope.get( name );
		if ( value instanceof Function function ) {
			FunctionBoxContext functionContext = Function.generateFunctionContext(
			    function,
			    // Function contexts' parent is the caller. The function will "know" about the CFC it's executing in
			    // because we've pushed the CFC onto the template stack in the function context.
			    context,
			    name,
			    namedArguments,
			    thisClass
			);

			functionContext.setThisClass( thisClass );
			return function.invoke( functionContext );
		}

		if ( thisClass.getSuper() != null && thisClass.getSuper().getThisScope().get( name ) != null ) {
			return thisClass.getSuper().dereferenceAndInvoke( context, name, namedArguments, safe );
		}

		if ( value != null ) {
			throw new BoxRuntimeException(
			    "key '" + name.getName() + "' of type  '" + value.getClass().getName() + "'  is not a function " );
		}

		// Check for generated accessors
		Object hasAccessors = thisClass.getAnnotations().get( Key.accessors );
		if ( hasAccessors != null && BooleanCaster.cast( hasAccessors ) ) {
			Property getterProperty = thisClass.getGetterLookup().get( name );
			if ( getterProperty != null ) {
				return thisClass.getBottomClass().getVariablesScope().dereference( context, getterProperty.name(), safe );
			}
			Property setterProperty = thisClass.getSetterLookup().get( name );
			if ( setterProperty != null ) {
				Key thisName = setterProperty.name();
				if ( !namedArguments.containsKey( thisName ) ) {
					throw new BoxRuntimeException( "Missing argument for setter '" + name.getName() + "'" );
				}
				thisClass.getBottomClass().getVariablesScope().assign( context, thisName, namedArguments.get( thisName ) );
				return thisClass;
			}
		}

		if ( thisClass.getThisScope().get( Key.onMissingMethod ) != null ) {
			Map<Key, Object> args = new HashMap<Key, Object>();
			args.put( Key.missingMethodName, name.getName() );
			args.put( Key.missingMethodArguments, namedArguments );
			return thisClass.dereferenceAndInvoke( context, Key.onMissingMethod, args, safe );
		}

		if ( !safe ) {
			throw new BoxRuntimeException( "Method '" + name.getName() + "' not found" );
		}
		return null;
	}

	/**
	 * Get the combined metadata for this function and all it's parameters
	 * This follows the format of Lucee and Adobe's "combined" metadata
	 * TODO: Move this to compat module
	 *
	 * @return The metadata as a struct
	 */
	public static IStruct getMetaData( IClassRunnable thisClass ) {
		IStruct meta = new Struct( IStruct.TYPES.SORTED );
		meta.putIfAbsent( "hint", "" );
		meta.putIfAbsent( "output", thisClass.canOutput() );

		// Assemble the metadata
		var functions = new ArrayList<Object>();
		// loop over target's variables scope and add metadata for each function
		for ( var entry : thisClass.getThisScope().keySet() ) {
			var value = thisClass.getThisScope().get( entry );
			if ( value instanceof Function fun ) {
				functions.add( fun.getMetaData() );
			}
		}
		meta.put( "name", thisClass.getName().getName() );
		meta.put( "accessors", false );
		meta.put( "functions", Array.fromList( functions ) );
		// meta.put( "hashCode", hashCode() );
		var properties = new Array();
		// loop over properties list and add struct for each property
		for ( var entry : thisClass.getProperties().entrySet() ) {
			var	property		= entry.getValue();
			var	propertyStruct	= new Struct( IStruct.TYPES.LINKED );
			propertyStruct.put( "name", property.name().getName() );
			propertyStruct.put( "type", property.type() );
			propertyStruct.put( "default", property.defaultValue() );
			if ( property.documentation() != null ) {
				propertyStruct.putAll( property.documentation() );
			}
			if ( property.annotations() != null ) {
				propertyStruct.putAll( property.annotations() );
			}
			properties.add( propertyStruct );
		}
		meta.put( "properties", properties );
		meta.put( "type", "Component" );
		meta.put( "name", thisClass.getName().getName() );
		meta.put( "fullname", thisClass.getName().getName() );
		meta.put( "path", thisClass.getRunnablePath().toString() );
		meta.put( "persisent", false );

		if ( thisClass.getDocumentation() != null ) {
			meta.putAll( thisClass.getDocumentation() );
		}
		if ( thisClass.getAnnotations() != null ) {
			meta.putAll( thisClass.getAnnotations() );
		}
		if ( thisClass.getSuper() != null ) {
			meta.put( "extends", thisClass.getSuper().getMetaData() );
		}
		return meta;
	}

	public static void registerInterface( IClassRunnable thisClass, BoxInterface _interface ) {
		_interface.validateClass( thisClass );
		VariablesScope	variablesScope	= thisClass.getVariablesScope();
		ThisScope		thisScope		= thisClass.getThisScope();
		thisClass.getInterfaces().add( _interface );
		// Add in default methods to the this and variables scopes
		for ( Map.Entry<Key, Function> entry : _interface.getDefaultMethods().entrySet() ) {
			if ( !variablesScope.containsKey( entry.getKey() ) ) {
				variablesScope.put( entry.getKey(), entry.getValue() );
			}
			if ( !thisScope.containsKey( entry.getKey() ) && entry.getValue().getAccess() == Function.Access.PUBLIC ) {
				thisScope.put( entry.getKey(), entry.getValue() );
			}
		}
	}

}