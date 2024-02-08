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
package ortus.boxlang.debugger.event;

/**
 * Models the Exit event for the Debug Protocol
 */
public class ExitEvent extends Event {

	public ExitBody body;

	private class ExitBody {

		@SuppressWarnings( value = { "unused" } )
		public int exitCode;
	}

	/**
	 * Constructor
	 * 
	 * @param exitCode The exit code of the program being debugged
	 */
	public ExitEvent( int exitCode ) {
		super( "exit" );

		this.body			= new ExitBody();
		this.body.exitCode	= exitCode;
	}

}
