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
package ortus.boxlang.runtime.interop.proxies;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class GenericProxy extends BaseProxy implements InvocationHandler {

	public GenericProxy( Object target, IBoxContext context, String method ) {
		super( target, context, method );
		prepLogger( GenericProxy.class );
	}

	@Override
	public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
		try {
			return invoke( args );
		} catch ( Exception e ) {
			getLogger().error( "Error invoking GenericProxy", e );
			throw new BoxRuntimeException( "Error invoking Function", e );
		}
	}

}