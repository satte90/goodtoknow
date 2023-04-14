# Request Webflux Starter 

Changes should be pushed to github and then only pushed to bitbucket using jenkins-release.sh script. See working with git section below

## About
Webflux starter for reactive spring boot microservices

+ Provides a process wrapper to set up a subscriber context for the reactive chain as well as input/output logging
+ Provides a webclient for http requests. Utilizes the subscriber context. Logs request/responses.
+ Ported LoggingConfig from springfield-spring-logging-starter to work with spring webflux
+ Provides metrics reporting for transactions and webClient requests, request duration and number of failures

Example logging output
```
2019-07-26T10:46:30,072 --- [INFO ] --- [ctor-http-nio-3] - [9ed25665-10a8-44d8-9036-2d6ad4404a50] c.t.s.w.u.RequestLogger                 : Inbound message
---------------------------
ID: 0
Type: Request
Host: localhost
Address: /demo3
HttpMethod: PUT
ContentType: null
Headers: [pear:"green", Accept:"*/*", Content-Length:"0", Cookie:"JSESSIONID=F847D90F4092283ACD247480B331CC44", User-Agent:"insomnia/6.5.4", Host:"localhost:8080", banana:"yellow"]
---------------------------

2019-07-26T10:46:30,102 --- [INFO ] --- [ctor-http-nio-3] - [9ed25665-10a8-44d8-9036-2d6ad4404a50] c.t.s.w.u.RequestLogger                 : Outbound message
---------------------------
ID: 1
Type: Request
Host: http://localhost:8090
Address: /server2/delayit
HttpMethod: PUT
ContentType: null
Headers: [banana:"yellow", pear:"green"]
Payload: {"ms":1000}
---------------------------

2019-07-26T10:46:30,150 --- [INFO ] --- [ctor-http-nio-3] - [9ed25665-10a8-44d8-9036-2d6ad4404a50] c.t.s.w.u.RequestLogger                 : Outbound message
---------------------------
ID: 2
Type: Request
Host: http://localhost:8090
Address: /server2/delayit
HttpMethod: PUT
ContentType: null
Headers: [banana:"yellow", pear:"green"]
Payload: {"ms":3000}
---------------------------

2019-07-26T10:46:31,233 --- [INFO ] --- [ctor-http-nio-7] - [9ed25665-10a8-44d8-9036-2d6ad4404a50] c.t.s.w.u.RequestLogger                 : Inbound message
---------------------------
ID: 1
Type: Response
RequestDuration: 1157
ContentType: text/plain;charset=UTF-8
ResponseCode: 200 OK
Headers: [Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY", X-Content-Type-Options:"nosniff", Referrer-Policy:"no-referrer", Content-Type:"text/plain;charset=UTF-8", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", X-XSS-Protection:"1 ; mode=block", Content-Length:"58", X-Transaction-ID:"9ed25665-10a8-44d8-9036-2d6ad4404a50"]
Payload: {"seconds":1,"tid":"9ed25665-10a8-44d8-9036-2d6ad4404a50"}
---------------------------

2019-07-26T10:46:31,263 --- [INFO ] --- [ctor-http-nio-7] - [9ed25665-10a8-44d8-9036-2d6ad4404a50] c.t.s.w.u.RequestLogger                 : Outbound message
---------------------------
ID: 3
Type: Request
Host: http://localhost:8090
Address: /server2/delayit
HttpMethod: PUT
ContentType: null
Headers: [banana:"yellow", pear:"green"]
Payload: {"ms":1000}
---------------------------

2019-07-26T10:46:32,275 --- [INFO ] --- [ctor-http-nio-7] - [9ed25665-10a8-44d8-9036-2d6ad4404a50] c.t.s.w.u.RequestLogger                 : Inbound message
---------------------------
ID: 3
Type: Response
RequestDuration: 1012
ContentType: text/plain;charset=UTF-8
ResponseCode: 200 OK
Headers: [Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY", X-Content-Type-Options:"nosniff", Referrer-Policy:"no-referrer", Content-Type:"text/plain;charset=UTF-8", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", X-XSS-Protection:"1 ; mode=block", Content-Length:"58", X-Transaction-ID:"9ed25665-10a8-44d8-9036-2d6ad4404a50"]
Payload: {"seconds":1,"tid":"9ed25665-10a8-44d8-9036-2d6ad4404a50"}
---------------------------

2019-07-26T10:46:33,216 --- [INFO ] --- [ctor-http-nio-9] - [9ed25665-10a8-44d8-9036-2d6ad4404a50] c.t.s.w.u.RequestLogger                 : Inbound message
---------------------------
ID: 2
Type: Response
RequestDuration: 3067
ContentType: text/plain;charset=UTF-8
ResponseCode: 200 OK
Headers: [Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY", X-Content-Type-Options:"nosniff", Referrer-Policy:"no-referrer", Content-Type:"text/plain;charset=UTF-8", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", X-XSS-Protection:"1 ; mode=block", Content-Length:"58", X-Transaction-ID:"9ed25665-10a8-44d8-9036-2d6ad4404a50"]
Payload: {"seconds":3,"tid":"9ed25665-10a8-44d8-9036-2d6ad4404a50"}
---------------------------

2019-07-26T10:46:33,221 --- [INFO ] --- [ctor-http-nio-9] - [9ed25665-10a8-44d8-9036-2d6ad4404a50] c.t.s.w.u.RequestLogger                 : Outbound message
---------------------------
ID: 0
Type: Response
RequestDuration: 3155
ResponseCode: 204 NO_CONTENT
Headers: [X-Transaction-ID:"9ed25665-10a8-44d8-9036-2d6ad4404a50"]
---------------------------
```


## Usage

### SetUp
1) Add maven dependency:
    ```
    <dependency>
        <groupId>com.teliacompany.webflux</groupId>
        <artifactId>request-webflux-starter</artifactId>
        <version>${request-webflux-starter-version}</version>
    </dependency>
    ```
2) Remove any dependencies to ```spring-boot-starter-web```, replace with ```spring-boot-starter-webflux```
3) Remove dependency to ```springfield-spring-logging-starter```. It does not work for reactive spring boot applications.

### RequestProcessor
For code examples you can check out our telia BE repos

Request state (what we normally would store in thread-local) is "stored" in a [SubscriberContext](https://projectreactor.io/docs/core/release/reference/#context)
This starter will store a "TransactionContext" in the SubscriberContext which holds TransactionId (TID) and other request data which is needed for logging. This is
up when calling requestProcessor.processRequest(). The subscriber context is then accessible through the reactive chain.

Example code:
```
@Controller
public class ReactivePocController {
    @Bean
    public RouterFunction<ServerResponse> route(RequestProcessor requestProcessor, PocService pocService) {
        return RouterFunctions
                .route(GET("/demo1").and(accept(APPLICATION_JSON)), requestProcessor.processRequest(pocService::demo1))
                .andRoute(POST("/demo2").and(accept(APPLICATION_JSON).and(contentType(APPLICATION_JSON))), requestProcessor.processRequest(PocRequest.class, pocService::demo2))
                .andRoute(PUT("/demo3").and(accept(APPLICATION_JSON)), requestProcessor.processRequest(pocService::demo3));
    }
}
```

_The RequestProcessor bean is automatically registered and available. It is created using logging config which can be configured in your property-files. (see logging section below)_

The requestProcessor.processRequest() requires 1-2 parameters, if available the request class (for POST, PUT etc) and a Supplier or Function. 
(You cannot pass a Consumer, if you don't want to return anything, return ```Mono<Void>```)


```
public Mono<String> demo1() {
    ...
}
...
public Mono<PocResponse> demo2(PocRequest request) {
    ...
}
```

### WebClient

_This has the same name as the real WebClient, which could be confusing, if you come up with a better name feel free to make a pull request be4 it is to late :)_

The ```com.teliacompany.springfield.webflux.client.WebClient``` provides additional input/output logging (similar to the servlet rest template). It inherits the
transactionId from the TransactionContext.

Example setup
```
@Bean
public WebClient server2Client(LoggingConfig loggingConfig) {
    WebClientConfig config = WebClientConfig.builder()
            .withHost("http://localhost:8090")
            .withBasePath("server2")
            .withServiceName("Server 2")
            .build();

    return WebClientBuilder.withConfig(config, loggingConfig)
            .defaultHeader("apple", "red")
            .build();
}
```

Example usage

```
private Mono<Optional<Server2DelayedResponse>> requestWithDelay(int ms, String bananaColor, String pearColor) {
    Server2DelayRequest server2Request = new Server2DelayRequest();
    server2Request.setMs(ms);

    return server2Client.put("sub/path")
            .header("banana", bananaColor)
            .header("pear", pearColor)
            .body(server2Request)
            .retrieve(Server2DelayedResponse.class)
            .flatMap(webClientResponse -> {
                if(webClientResponse.getBody().isPresent()) {
                    Server2DelayedResponse body = webClientResponse.getBody().get();
                    LOG.info("Got tid \"{}\" after aprox {} seconds", body.getTid(), body.getSeconds());
                    return Mono.just(Optional.of(body));
                }
                return Mono.just(Optional.empty());
            });
}
```
_Side-Note: If we don't get a response with body, we are retuning a mono of an empty optional. For 3 reasons: (1) Returning null is not allowed 
(we will get NPE when chaining). (2) Returning Mono.just(null) will also fail when we chan a map to it and (3) Mono.empty() needs to be handled 
using for example switchIfEmpty_ 

### Accessing the transaction context

The request processor provides an convenience method for getting the transaction context: ```RequestProcessor.getTransactionContext()```

Example usage:
```
...
RequestProcessor.getTransactionContext()
    .flatMap(requestContext -> {
        LOG.info("messageIdCounter = {}", requestContext.getTid());
        ...
    }
```

### Logging

#### Log4J Config
You need to provide your service with your own log4j config. Following is an example used by Vårt Telia:
 + [log4j2.xml](https://diva.teliacompany.net/bitbucket/projects/DCVT/repos/augustus-springfield-reactive/browse/base-server/src/main/resources/log4j2.xml)
 + [log4j2-dev.xml](https://diva.teliacompany.net/bitbucket/projects/DCVT/repos/augustus-springfield-reactive/browse/base-server/src/main/resources/log4j2-dev.xml) 
```
<Configuration status="info" name="example" packages="">
    <Appenders>
        <!-- Default service appender. Note that the name="default" is important. This name must have
             the same name as the ones used in services if they override this file. -->
        <Console name="default" target="SYSTEM_OUT">
            <PatternLayout pattern="[%-5level] %d{ISO8601} --- [%15.15t] - [%mdc{transactionId}] - [%mdc{tcad}] - [%mdc{tscid}] - %-40.40c{1.}: %m%n"/>
        </Console>
        <RollingFile name="serviceLogFile" fileName="logs/service.log" filePattern="logs/service-%d{MM-dd-yyyy}.log">
            <PatternLayout pattern='{ "level":"%level", "time":"%d{ISO8601}", "thread":"%t", "transactionId":"%mdc{transactionId}", "loggerName":"%c{1.}", "tcad":"%mdc{tcad}", "tscid":"%mdc{tscid}", "message":"%enc{%m}{JSON}" }%n' />
            <Policies>
                <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
            </Policies>
            <DefaultRolloverStrategy max="3"/>
        </RollingFile>
        <!-- Adapter appender -->
        <RollingFile name="adapterLogFile" fileName="logs/adapter.log" filePattern="logs/adapter-%d{MM-dd-yyyy}.log">
            <PatternLayout pattern='{ "level":"%level", "time":"%d{ISO8601}", "thread":"%t", "transactionId":"%mdc{transactionId}", "tcad":"%mdc{tcad}", "tscid":"%mdc{tscid}", %m }%n' />
            <Policies>
                <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
            </Policies>
            <DefaultRolloverStrategy max="3"/>
        </RollingFile>
    </Appenders>
    <Loggers>
        <Root level="info" additivity="false">
            <AppenderRef ref="default"/>
            <AppenderRef ref="serviceLogFile"/>
        </Root>
        <Logger name="com.teliacompany.springfield.webflux.log" level="info" additivity="false">
            <AppenderRef ref="adapterLogFile"/>
        </Logger>
    </Loggers>
</Configuration>
```

Logging can be configured much like ```springfield-spring-logging-starter```. TODO: Document it here!

## Whats happening?

Both RequestProcessor and WebClient has a lets call it "chain definition" describing at a relatively high level what it does. See respective class for an updated chain. 

Basically it wraps your whole (almost) execution chain.
1)  Http request is incoming
2)  Spring filters are applied
3)  Request reaches your rest controller or router function. Request is still a String basically, we need to handle conversion manually to be able to inject a body logger
4)  RequestProcessor is called with the HttpRequest, requestClass, your handler method and optionally meta data method.
5)  Creates a transaction context with transaction id e.t.c. (Transaction id is either generated or extracted from http headers)
6)  Converts the request to specified Java requestClass
7)  Executes addTransactionMetaData method. Some meta data is always added, tscid and tcadid if provided in request headers
8)  Logs the request, including string representation of the body and meta data. Optionally base64 encodes body and headers. Sensitive data is filtered*
9)  Request string is cleared
10) Executes your main method with converted request class (if any)
11) Receives response from main method and creates a response
12) Logs response
13) Returns response

If you make calls using the WebClient these have a similar chain (10-x):

1) Creates request spec
2) Converts request (to json or string only atm)
3) Logs request
4) Exchange request
5) Set meta data, preserves TID, Tcad, TSCID and custom metaData. Set TID, tcad and tscid in MDC
6) Handle response errors (log and rethrow as springfield-erors and/or applies custom error handler)
7) Extract payload 
8) Log response
9) Convert response, wrap in WebClientResponse
10) \<your code continues here> 

 \* Sensitive data includes authorization header and json body fields concerning passwords. See JsonPayloadLoggingFilter for details.


### TODO Improvements / Known problems
+ Only Mono support, Not tested for Flux<>. One issue is that the Spring Router only supports Mono, which is a bit surprising so it could also be something that I've missed...
+ Only json and plain text support atm

## Working with git

This repository has two remotes that should be in sync:
* Github: https://github.com/telia-company/toca-request-webflux-starter
* Bitbucket: https://diva.teliacompany.net/bitbucket/projects/DCCOMMON/repos/request-webflux-starter

The master repo is in __GitHub__ and the pipeline in github is responsible for promoting and setting new development versions.

### Remove / rename the current remote
If you have cloned this from either github or bitbucket, rename that remote to github or bitbucket, or remove it and re-add it using command below.

Run ```git remote -vv``` to show what remotes currently exist

Run ```git remote rm origin```

### Add remote(s) that is missing:
* ```git remote add github git@github.com:telia-company/toca-request-webflux-starter.git```
* ```git remote add bitbucket ssh://git@diva.teliacompany.net:7999/dccommon/request-webflux-starter.git```

### Set default upstream remote
```git branch --set-upstream-to github/master```

### Verify remotes

Run ```git remote -vv``` again and you should have:
```
bitbucket       ssh://git@diva.teliacompany.net:7999/dccommon/request-webflux-starter.git (fetch)
bitbucket       ssh://git@diva.teliacompany.net:7999/dccommon/request-webflux-starter.git (push)
github  git@github.com:telia-company/toca-request-webflux-starter.git (fetch)
github  git@github.com:telia-company/toca-request-webflux-starter.git (push)
```

### Release to "Classic" (Bitbucket/jenkins pipeline)

If you have the remote repos set up like above you can simply run the ```jenkins-release.sh``` script.
It will fetch all tags and find the latest one, the check-out the commit for that tag
and push it to bitbucket/master. Finally, it will check-out master again.

---
