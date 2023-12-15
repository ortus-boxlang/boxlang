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
package ortus.boxlang.runtime.types.exceptions;

import ortus.boxlang.runtime.scopes.Key;

/**
 * This is the root exception for all exceptions thrown by BoxLang.
 */
public abstract class BoxLangException extends RuntimeException {

	public static final Key	messageKey		= Key.of( "message" );
	public static final Key	detailKey		= Key.of( "detail" );
	public static final Key	typeKey			= Key.of( "type" );
	public static final Key	tagContextKey	= Key.of( "tagContext" );

	/**
	 * Detailed message from the CFML interpreter or specified in a cfthrow tag. When the exception is generated by BoxLang (and not cfthrow), the
	 * message can contain HTML formatting and can help determine which tag threw the exception.
	 */
	public String			detail			= "";
	/**
	 * Type: Exception type, as specified in cfcatch.
	 *
	 * - application: catches application exceptions
	 * - database: catches database exceptions
	 * - template: catches BoxLang page exceptions
	 * - security: catches security exceptions
	 * - object: catches object exceptions
	 * - missingInclude: catches missing include file exceptions
	 * - expression: catches expression exceptions
	 * - lock: catches lock exceptions
	 * - custom_type: catches the specified custom exception type that is defined in a cfthrow tag
	 * - searchengine: catches Solr search engine exceptions
	 *
	 * The type must ALWAYS be set for a BoxLangException by the superclass extending this base class. This will also
	 * ensure the type string matches the exception class as well.
	 */
	public String			type			= null;
	// TODO:
	public String			tagContext		= "";

	/**
	 * Constructor
	 *
	 * @param message The message
	 */
	public BoxLangException( String message, String type ) {
		this( message, "", type, null );
	}

	/**
	 * Constructor
	 *
	 * @param message The message
	 * @param type    The type
	 * @param cause   The cause
	 */
	public BoxLangException( String message, String type, Throwable cause ) {
		this( message, "", type, cause );
	}

	/**
	 * Constructor
	 *
	 * @param message The message
	 * @param detail  The detail
	 * @param type    The type
	 */
	public BoxLangException( String message, String detail, String type ) {
		this( message, detail, type, null );
	}

	/**
	 * Constructor
	 *
	 * @param message The message
	 * @param detail  The detail
	 * @param type    The type
	 * @param cause   The cause
	 */
	public BoxLangException( String message, String detail, String type, Throwable cause ) {
		super( message );
		this.detail	= detail;
		this.type	= type;
		if ( cause != null ) {
			initCause( cause );
		}
	}
}
