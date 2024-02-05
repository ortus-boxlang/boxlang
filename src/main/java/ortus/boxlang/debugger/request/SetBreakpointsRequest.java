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
package ortus.boxlang.debugger.request;

import ortus.boxlang.debugger.DebugAdapter;
import ortus.boxlang.debugger.types.Breakpoint;
import ortus.boxlang.debugger.types.Source;

/**
 * Models the request to add a breakpoint to a line of source code
 */
public class SetBreakpointsRequest extends AbstractRequest {

	public String					program;
	public SetBreakpointArguments	arguments;

	/**
	 * The arguments of the SetBreakpoint Request
	 */
	public static class SetBreakpointArguments {

		public Source		source;
		public Breakpoint[]	breakpoints;
	}

	@Override
	public void accept( DebugAdapter adapter ) {
		adapter.visit( this );
	}

}
