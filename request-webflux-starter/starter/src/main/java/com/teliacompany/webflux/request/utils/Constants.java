package com.teliacompany.webflux.request.utils;

public final class Constants {
   public static final String DELIMITER = "---------------------------";
   public static final String SEP = ": ";
   public static final String CRLF = System.getProperty("line.separator");
   public static final String TRANSACTION_CONTEXT_KEY = "transactionContext";
   public static final String REQUEST_CONTEXT_KEY = "requestContext";
   public static final String MDC_TRANSACTION_ID_KEY = "transactionId";
   public static final String MDC_TCAD_KEY = "tcad";
   public static final String MDC_TSCID_KEY = "tscid";
   public static final String HTTP_CORRELATION_ID_HEADER = "X-Correlation-ID";
   public static final String HTTP_REQUEST_ID_HEADER = "X-Request-ID";
   public static final String HTTP_TRANSACTION_ID_HEADER = "X-Transaction-ID";
   public static final String HTTP_TRANSACTION_META_DATA_HEADER = "X-Transaction-Meta-Data";
   public static final String HTTP_TELIA_TCAD = "telia-tcad";
   public static final String HTTP_X_TCAD = "X-TCAD";
   public static final String HTTP_X_TSCID = "X-TSCID";
   public static final String HTTP_X_TRACING = "X-TRACING";
   public static final String HTTP_X_TRACE_LOG = "X-TRACE-LOG";
   public static final String REQUEST_ID = "requestId";
   public static final String REQUEST_DURATION = "requestDuration";
   public static final String TYPE = "type";
   public static final String OPERATION = "operation";
   public static final String COLLECTION = "collection";
   public static final String HOST = "host";
   public static final String ADDRESS = "address";
   public static final String HTTP_METHOD = "httpMethod";
   public static final String URI_VARIABLES = "uriVariables";
   public static final String CONTENT_TYPE = "contentType";
   public static final String PAYLOAD = "payload";
   public static final String PAYLOAD_INFO = "payload_info";
   public static final String PAYLOAD_LENGTH = "payload_length";
   public static final String HEADERS = "headers";
   public static final String COOKIES = "cookies";
   public static final String RESPONSE_CODE = "responseCode";
   public static final String DIRECTION = "direction";
   public static final String INBOUND = "Inbound";
   public static final String OUTBOUND = "Outbound";
   public static final String INTERNAL = "Internal";
   public static final String ENCODING = "Encoding";
   public static final String REQUEST = "Request";
   public static final String RESPONSE = "Response";
   public static final String DB_REQUEST = "Database Request";
   public static final String DB_RESPONSE = "Database Response";
   public static final String READ = "Read";
   public static final String DELETE = "Delete";
   public static final String WRITE = "Write";
   public static final String SUB_SYSTEM_TID = "subSystemTransactionIDs";
   public static final String SUB_SERVICE_NAME = "subServiceName";

    private Constants() { }
}
