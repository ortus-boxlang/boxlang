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
package ortus.boxlang.runtime.jdbc.drivers;

import ortus.boxlang.runtime.config.segments.DatasourceConfig;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.util.StructUtil;

/**
 * This is the generic JDBC driver that can be used to register datasources in the system.
 * We use a generic JDBC Url connection schema to connect to the database.
 */
public class GenericJDBCDriver implements IJDBCDriver {

	/**
	 * The name of the driver
	 */
	protected static final Key		NAME					= new Key( "Generic" );

	/**
	 * The class name of the driver
	 */
	protected static final String	DRIVER_CLASS_NAME		= "";

	/**
	 * The default delimiter for the custom parameters
	 */
	protected static final String	DEFAULT_DELIMITER		= ";";

	/**
	 * The default custom params for the connection URL
	 */
	protected static final IStruct	DEFAULT_CUSTOM_PARAMS	= Struct.of();

	/**
	 * The default configuration properties
	 */
	protected static final IStruct	DEFAULT_PROPERTIES		= Struct.of();

	@Override
	public Key getName() {
		return NAME;
	}

	@Override
	public DatabaseDriverType getType() {
		return DatabaseDriverType.GENERIC;
	}

	/**
	 * We return an empty class, because we are using a generic JDBC driver
	 * that does not have a specific class name.
	 * <p>
	 * This will be based on the connection url built by the driver and it will be expecting
	 * the class to be in the class path.
	 * <p>
	 * Custom parameters are incorporated by the {@link DatasourceConfig#incorporateCustomParams object automatically.
	 */
	@Override
	public String getClassName() {
		return DRIVER_CLASS_NAME;
	}

	@Override
	public String buildConnectionURL( DatasourceConfig config ) {
		// Validate the driver
		String jDriver = ( String ) config.properties.getOrDefault( "driver", "" );
		if ( jDriver.isEmpty() ) {
			throw new IllegalArgumentException( "The driver property is required for the Generic JDBC Driver" );
		}
		// Validate the port
		int port = ( int ) config.properties.getOrDefault( "port", 0 );
		if ( port == 0 ) {
			throw new IllegalArgumentException( "The port property is required for the Generic JDBC Driver" );
		}

		// Validate the database
		String	database	= ( String ) config.properties.getOrDefault( "database", "" );
		// Host we can use localhost
		String	host		= ( String ) config.properties.getOrDefault( "host", "localhost" );

		// Build the Generic connection URL
		return String.format(
		    "jdbc:%s://%s:%d/%s?%s",
		    jDriver,
		    host,
		    port,
		    database,
		    customParamsToQueryString( config )
		);
	}

	/**
	 * Get default properties for the driver to incorporate into the datasource config
	 */
	@Override
	public IStruct getDefaultProperties() {
		return DEFAULT_PROPERTIES;
	}

	/**
	 * This helper method is used to convert the custom parameters in the config (Key.custom)
	 * to a query string that can be used by the driver to build the connection URL.
	 * <p>
	 * We incorporate the default parameters into the custom parameters and return the query string
	 * using the driver's default delimiter.
	 *
	 * @param config The datasource config
	 *
	 * @return The custom parameters as a query string
	 */
	public static String customParamsToQueryString( DatasourceConfig config ) {
		IStruct params = new Struct( DEFAULT_CUSTOM_PARAMS );

		// If the custom parameters are a string, convert them to a struct
		if ( config.properties.get( Key.custom ) instanceof String castedParams ) {
			config.properties.put( Key.custom, StructUtil.fromQueryString( castedParams, DEFAULT_DELIMITER ) );
		}

		// Add all the custom parameters to the params struct
		config.properties.getAsStruct( Key.custom ).forEach( params::put );

		// Return it as the query string needed by the driver
		return StructUtil.toQueryString( params, DEFAULT_DELIMITER );
	}

}
