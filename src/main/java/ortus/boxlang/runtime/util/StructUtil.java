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
package ortus.boxlang.runtime.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Predicate;

import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.casters.BooleanCaster;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.services.AsyncService;
import ortus.boxlang.runtime.types.Function;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class StructUtil {

	/**
	 * Method to invoke a function for every item in a struct
	 *
	 * @param struct          The struct to iterate
	 * @param callback        The callback Function object
	 * @param callbackContext The context in which to execute the callback
	 * @param parallel        Whether to process the filter in parallel
	 * @param maxThreads      Optional max threads for parallel execution
	 * @param ordered         Boolean as to whether to maintain order in parallel execution
	 *
	 * @return
	 */
	public static void each(
	    IStruct struct,
	    Function callback,
	    IBoxContext callbackContext,
	    Boolean parallel,
	    Integer maxThreads,
	    Boolean ordered ) {

		Stream<Map.Entry<Key, Object>>		entryStream	= struct.entrySet().stream();

		Consumer<Map.Entry<Key, Object>>	exec		= item -> callbackContext.invokeFunction(
		    callback,
		    new Object[] { item.getKey().getName(), item.getValue(), struct }
		);

		if ( !parallel ) {
			entryStream.forEach( exec );
		} else if ( ordered ) {
			AsyncService.buildExecutor(
			    "StructEach_" + UUID.randomUUID().toString(),
			    AsyncService.ExecutorType.FORK_JOIN,
			    maxThreads
			).submitAndGet( () -> entryStream.parallel().forEachOrdered( exec ) );
		} else {
			AsyncService.buildExecutor(
			    "StructEach_" + UUID.randomUUID().toString(),
			    AsyncService.ExecutorType.FORK_JOIN,
			    maxThreads
			).submitAndGet( () -> entryStream.parallel().forEach( exec ) );
		}

	}

	/**
	 * Method to test if any item in the struct meets the criteria in the callback
	 *
	 * @param struct          The struct to test
	 * @param callback        The callback test to apply
	 * @param callbackContext The context in which to execute the callback
	 * @param parallel        Whether to process the filter in parallel
	 * @param maxThreads      Optional max threads for parallel execution
	 *
	 * @return The boolean value as to whether the test is met
	 */
	public static Boolean some(
	    IStruct struct,
	    Function callback,
	    IBoxContext callbackContext,
	    Boolean parallel,
	    Integer maxThreads ) {

		Stream<Map.Entry<Key, Object>>		entryStream	= struct.entrySet().stream();

		Predicate<Map.Entry<Key, Object>>	test		= item -> ( boolean ) callbackContext.invokeFunction(
		    callback,
		    new Object[] { item.getKey().getName(), item.getValue(), struct }
		);

		return !parallel
		    ? ( Boolean ) entryStream.anyMatch( test )
		    : ( Boolean ) AsyncService.buildExecutor(
		        "structSome_" + UUID.randomUUID().toString(),
		        AsyncService.ExecutorType.FORK_JOIN,
		        maxThreads
		    ).submitAndGet( () -> entryStream.parallel().anyMatch( test ) );

	}

	/**
	 * Method to test if any item in the struct meets the criteria in the callback
	 *
	 * @param struct          The struct object to filter
	 * @param callback        The callback Function object
	 * @param callbackContext The context in which to execute the callback
	 * @param parallel        Whether to process the filter in parallel
	 * @param maxThreads      Optional max threads for parallel execution
	 *
	 * @return The boolean value as to whether the test is met
	 */
	public static Boolean every(
	    IStruct struct,
	    Function callback,
	    IBoxContext callbackContext,
	    Boolean parallel,
	    Integer maxThreads ) {

		Stream<Map.Entry<Key, Object>>		entryStream	= struct.entrySet().stream();

		Predicate<Map.Entry<Key, Object>>	test		= item -> ( boolean ) callbackContext.invokeFunction(
		    callback,
		    new Object[] { item.getKey().getName(), item.getValue(), struct }
		);

		return !parallel
		    ? entryStream.dropWhile( test ).toArray().length == 0
		    : BooleanCaster.cast(
		        AsyncService.buildExecutor(
		            "ArrayEvery_" + UUID.randomUUID().toString(),
		            AsyncService.ExecutorType.FORK_JOIN,
		            maxThreads
		        ).submitAndGet( () -> entryStream.parallel().dropWhile( test ).toArray().length == 0 )
		    );

	}

	/**
	 * Method to filter a struct with a function callback and context
	 *
	 * @param struct          The struct object to filter
	 * @param callback        The callback Function object
	 * @param callbackContext The context in which to execute the callback
	 * @param parallel        Whether to process the filter in parallel
	 * @param maxThreads      Optional max threads for parallel execution
	 *
	 * @return A filtered array
	 */
	@SuppressWarnings( "unchecked" )
	public static Struct filter(
	    IStruct struct,
	    Function callback,
	    IBoxContext callbackContext,
	    Boolean parallel,
	    Integer maxThreads ) {

		Stream<Map.Entry<Key, Object>>		entryStream		= struct.entrySet().stream();
		Stream<Map.Entry<Key, Object>>		filteredStream	= null;

		Predicate<Map.Entry<Key, Object>>	test			= item -> ( boolean ) callbackContext.invokeFunction(
		    callback,
		    new Object[] { item.getKey().getName(), item.getValue(), struct }
		);

		Struct								result			= new Struct( struct.getType() );

		if ( parallel ) {
			filteredStream = entryStream.filter( test );
		} else {
			filteredStream = ( Stream<Map.Entry<Key, Object>> ) AsyncService.buildExecutor(
			    "ArrayFilter_" + UUID.randomUUID().toString(),
			    AsyncService.ExecutorType.FORK_JOIN,
			    maxThreads
			).submitAndGet( () -> entryStream.parallel().filter( test ) );
		}

		if ( struct.getType().equals( Struct.TYPES.LINKED ) ) {
			result.putAll(
			    ( LinkedHashMap<Key, Object> ) filteredStream.collect(
			        Collectors.toMap(
			            entry -> entry.getKey(),
			            entry -> entry.getValue(),
			            ( v1, v2 ) -> {
				            throw new BoxRuntimeException( "An exception occurred while filtering the struct" );
			            },
			            LinkedHashMap<Key, Object>::new
			        )
			    )
			);
		} else if ( struct.getType().equals( Struct.TYPES.SORTED ) ) {
			result.putAll(
			    ( ConcurrentSkipListMap<Key, Object> ) filteredStream.collect(
			        Collectors.toMap(
			            entry -> entry.getKey(),
			            entry -> entry.getValue(),
			            ( v1, v2 ) -> {
				            throw new BoxRuntimeException( "An exception occurred while filtering the struct" );
			            },
			            ConcurrentSkipListMap<Key, Object>::new
			        )
			    )
			);
		} else {
			result.putAll(
			    ( ConcurrentHashMap<Key, Object> ) filteredStream.collect( Collectors.toConcurrentMap( entry -> entry.getKey(), entry -> entry.getValue() ) )
			);
		}

		return result;

	}

	/**
	 * Method to map a struct to a new struct
	 *
	 * @param struct          The struct object to filter
	 * @param callback        The callback Function object
	 * @param callbackContext The context in which to execute the callback
	 * @param parallel        Whether to process the filter in parallel
	 * @param maxThreads      Optional max threads for parallel execution
	 *
	 * @return A filtered array
	 */
	public static Struct map(
	    IStruct struct,
	    Function callback,
	    IBoxContext callbackContext,
	    Boolean parallel,
	    Integer maxThreads ) {

		Stream<Map.Entry<Key, Object>>	entryStream	= struct.entrySet().stream();
		Struct							result		= new Struct( struct.getType() );

		if ( !parallel ) {
			entryStream.forEach( item -> result.put(
			    item.getKey(),
			    callbackContext.invokeFunction(
			        callback,
			        new Object[] { item.getKey().getName(), item.getValue(), struct }
			    )
			)
			);
		} else if ( struct.getType().equals( IStruct.TYPES.LINKED ) ) {
			AsyncService.buildExecutor(
			    "StructMap_" + UUID.randomUUID().toString(),
			    AsyncService.ExecutorType.FORK_JOIN,
			    maxThreads
			).submitAndGet( () -> entryStream.parallel().forEachOrdered( item -> result.put(
			    item.getKey(),
			    callbackContext.invokeFunction(
			        callback,
			        new Object[] { item.getKey().getName(), item.getValue(), struct }
			    )
			)
			) );
		} else {
			AsyncService.buildExecutor(
			    "StructMap_" + UUID.randomUUID().toString(),
			    AsyncService.ExecutorType.FORK_JOIN,
			    maxThreads
			).submitAndGet( () -> entryStream.parallel().forEach( item -> result.put(
			    item.getKey(),
			    callbackContext.invokeFunction(
			        callback,
			        new Object[] { item.getKey().getName(), item.getValue(), struct }
			    )
			)
			) );
		}
		return result;

	}

	/**
	 * Method to reduce a struct to an accumulated object
	 *
	 * @param struct          The struct object to reduce
	 * @param callback        The callback Function object
	 * @param callbackContext The context in which to execute the callback
	 * @param initialValue    The initial value of the accumulation
	 *
	 * @return the new object reduction
	 */
	public static Object reduce(
	    IStruct struct,
	    Function callback,
	    IBoxContext callbackContext,
	    Object initialValue ) {

		BiFunction<Object, Map.Entry<Key, Object>, Object> reduction = ( acc, item ) -> callbackContext.invokeFunction( callback,
		    new Object[] { acc, item.getKey().getName(), item.getValue(), struct } );

		return struct.entrySet().stream()
		    .reduce(
		        initialValue,
		        reduction,
		        ( acc, intermediate ) -> acc
		    );

	}

}