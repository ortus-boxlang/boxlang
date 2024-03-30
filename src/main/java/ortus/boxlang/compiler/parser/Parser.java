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
package ortus.boxlang.compiler.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import ortus.boxlang.compiler.ast.BoxExpression;
import ortus.boxlang.compiler.ast.BoxScript;
import ortus.boxlang.compiler.ast.BoxStatement;
import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;

public class Parser {

	private static BoxRuntime runtime = BoxRuntime.getInstance();

	/**
	 * Attempt to detect the type of source code based on the contents
	 *
	 * @param file File to check
	 *
	 * @return a BoxFileType
	 *
	 * @see BoxSourceType
	 */
	public static BoxSourceType detectFile( File file ) {
		Optional<String> ext = getFileExtension( file.getAbsolutePath() );
		if ( !ext.isPresent() ) {
			throw new RuntimeException( "No file extension found for path : " + file.getAbsolutePath() );
		}

		switch ( ext.get() ) {
			case "cfs" -> {
				return BoxSourceType.CFSCRIPT;
			}
			case "cfm" -> {
				return BoxSourceType.CFTEMPLATE;
			}
			case "cfml" -> {
				return BoxSourceType.CFTEMPLATE;
			}
			case "cfc" -> {
				try {
					List<String> content = Files.readAllLines( file.toPath() );
					// TODO: This approach can be tricked by comments
					if ( content.stream()
					    .anyMatch( lines -> lines.toLowerCase().contains( "<cfcomponent" ) || lines.toLowerCase().contains( "<cfinterface" ) ) ) {
						return BoxSourceType.CFTEMPLATE;
					}
				} catch ( IOException e ) {
					throw new RuntimeException( e );
				}
				return BoxSourceType.CFSCRIPT;
			}
			case "bxm" -> {
				return BoxSourceType.BOXTEMPLATE;
			}
			case "bxs" -> {
				return BoxSourceType.BOXSCRIPT;
			}
			case "bx" -> {
				try {
					List<String> content = Files.readAllLines( file.toPath() );
					// TODO: This approach can be tricked by comments
					if ( content.stream()
					    .anyMatch( lines -> lines.toLowerCase().contains( "<bx:class" ) || lines.toLowerCase().contains( "<bx:interface" ) ) ) {
						return BoxSourceType.BOXTEMPLATE;
					}
				} catch ( IOException e ) {
					throw new RuntimeException( e );
				}
				return BoxSourceType.BOXSCRIPT;
			}
			default -> {
				throw new RuntimeException( "Unsupported file: " + file.getAbsolutePath() );
			}
		}

	}

	public static Optional<String> getFileExtension( String filename ) {
		return Optional.ofNullable( filename )
		    .filter( f -> f.contains( "." ) )
		    .map( f -> f.substring( filename.lastIndexOf( "." ) + 1 ).toLowerCase() );
	}

	/**
	 * Parse a script file
	 *
	 * @param file source file to parse
	 *
	 * @return a ParsingResult containing the AST with a BoxScript as root and the list of errors (if any)
	 *
	 * @throws IOException
	 *
	 * @see BoxScript
	 * @see ParsingResult
	 */

	public ParsingResult parse( File file ) throws IOException {
		BoxSourceType	fileType	= detectFile( file );
		AbstractParser	parser;
		switch ( fileType ) {
			case CFSCRIPT -> {
				parser = new CFScriptParser();
			}
			case CFTEMPLATE -> {
				parser = new CFTemplateParser();
			}
			case BOXSCRIPT -> {
				parser = new BoxScriptParser();
			}
			case BOXTEMPLATE -> {
				parser = new BoxTemplateParser();
			}
			default -> {
				throw new RuntimeException( "Unsupported file: " + file.getAbsolutePath() );
			}
		}
		ParsingResult	result	= parser.parse( file );

		IStruct			data	= Struct.of(
		    "file", file,
		    "result", result
		);
		runtime.announce( "onParse", data );
		return ( ParsingResult ) data.get( "result" );
	}

	/**
	 * Parse a script string expression
	 *
	 * @param code source of the expression to parse
	 *
	 * @return a ParsingResult containing the AST with a BoxExpr as root and the list of errors (if any)
	 *
	 * @throws IOException
	 *
	 * @see ParsingResult
	 * @see BoxExpression
	 */
	public ParsingResult parse( String code, BoxSourceType sourceType ) throws IOException {
		AbstractParser parser;
		switch ( sourceType ) {
			case CFSCRIPT -> {
				parser = new CFScriptParser();
			}
			case CFTEMPLATE -> {
				parser = new CFTemplateParser();
			}
			case BOXSCRIPT -> {
				parser = new BoxScriptParser();
			}
			case BOXTEMPLATE -> {
				parser = new BoxTemplateParser();
			}
			default -> {
				throw new RuntimeException( "Unsupported language" );
			}
		}
		ParsingResult	result	= parser.parse( code );

		IStruct			data	= Struct.of(
		    "code", code,
		    "result", result
		);
		runtime.announce( "onParse", data );
		return ( ParsingResult ) data.get( "result" );

	}

	/**
	 * Parse a script string statement
	 *
	 * @param code source of the expression to parse
	 *
	 * @return a ParsingResult containing the AST with a BoxStatement as root and the list of errors (if any)
	 *
	 * 
	 * @see ParsingResult
	 * @see BoxStatement
	 */
	public ParsingResult parseExpression( String code ) {
		try {
			ParsingResult	result	= new BoxScriptParser().parseExpression( code );

			IStruct			data	= Struct.of(
			    "code", code,
			    "result", result
			);
			runtime.announce( "onParse", data );
			return ( ParsingResult ) data.get( "result" );
		} catch ( IOException e ) {
			throw new BoxRuntimeException( "Error parsing expression", e );
		}
	}

	public ParsingResult parseStatement( String code ) throws IOException {
		ParsingResult	result	= new BoxScriptParser().parseStatement( code );

		IStruct			data	= Struct.of(
		    "code", code,
		    "result", result
		);
		runtime.announce( "onParse", data );
		return ( ParsingResult ) data.get( "result" );
	}

}