#macro( ccase $str )
#foreach( $word in $str.split('-') )$word.substring(0,1).toUpperCase()$word.substring(1)#end
#end
#set( $classNamePrefix = "#ccase( $api-name )" )
package ${package}.${api-name}.config;

import com.teliacompany.apigee4j.core.client.apigee4jClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration for $classNamePrefix.
 * Default configuration can be found in: ${api-name}-client.properties
 * Default profile specific config found in ${api-name}-client-${apigee4j.environment}.properties
 */
@Configuration
@PropertySource(value = {
        "classpath:/${api-name}-client.properties",
        "classpath:/${api-name}-client-${apigee4j.environment}.properties"
}, ignoreResourceNotFound = true)
public class ${classNamePrefix}ClientConfigV${api-major-version} extends apigee4jClientConfig {
    public static final String SERVICE_NAME = "${classNamePrefix}";

    @Value("${${api-name}.endpoint.v${api-major-version}}")
    private String endpoint;

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }
}
