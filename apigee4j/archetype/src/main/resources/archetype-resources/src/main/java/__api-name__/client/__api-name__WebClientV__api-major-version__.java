#macro( ccase $str )
#foreach( $word in $str.split('-') )$word.substring(0,1).toUpperCase()$word.substring(1)#end
#end
#set( $classNamePrefix = "#ccase( $api-name )" )
package ${package}.${api-name}.client;

import com.teliacompany.apigee4j.core.client.Abstractapigee4jClient;
import ${package}.${api-name}.config.${classNamePrefix}ClientConfigV${api-major-version};
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ${classNamePrefix}WebClientV${api-major-version} extends Abstractapigee4jClient<${classNamePrefix}ClientConfigV${api-major-version}> {
    //TODO: Add static endpoints here, i.e. whatever is behind the base path defined in property: ${api-name}.endpoint.v${api-major-version}
    public static final String EXAMPLE_ENDPOINT_NAME = "/changeMe";

    @Autowired
    public ${classNamePrefix}WebClientV${api-major-version}(${classNamePrefix}ClientConfigV${api-major-version} clientConfig) {
        super(clientConfig);
    }
}