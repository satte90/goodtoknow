package com.teliacompany.springfield.addressmaster.client;

import com.teliacompany.springfield.addressmaster.config.AddressMasterClientConfiguration;
import com.teliacompany.springfield.addressmaster.model.ResponseCode;
import com.teliacompany.springfield.addressmaster.utils.AddressMasterMarshalUtils;
import com.teliacompany.springfield.error.exception.WebException;
import com.teliacompany.springfield.webflux.client.WebClient;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import se.telia.addressmaster.services.Address;

import static com.teliacompany.springfield.addressmaster.config.AddressMasterClientConfiguration.SERVICE_NAME;

public class AddressMasterWebClient {
    private final WebClient webClient;
    private static final String SERVICE_PORT_GETADDRESS = "GetAddress";
    private static final String SERVICE_PORT_GETADDRESS_BY_POINTID = "GetAddressFromPunktID";


    public AddressMasterWebClient(WebClient webClient, AddressMasterClientConfiguration config) {
        this.webClient = webClient;
    }

    public static String getServiceName() {
        return SERVICE_NAME;
    }

    public Mono<Address> lookupAddress(Address addressLookup) {
        String soapXml = AddressMasterMarshalUtils.marshalSoapRequest(addressLookup, SERVICE_PORT_GETADDRESS);
        return callService(soapXml);
    }

    public Mono<Address> lookupAddressForPointId(Address addressLookup) {
        String soapXml = AddressMasterMarshalUtils.marshalSoapRequest(addressLookup, SERVICE_PORT_GETADDRESS_BY_POINTID);
        return callService(soapXml);
    }

    private Mono<Address> callService(String soapXml) {
        return webClient.post("")
                .body(soapXml)
                .retrieve(String.class)
                .map(payload -> payload.getBody()
                        .map(AddressMasterMarshalUtils::unmarshalSoapResponse)
                        .orElse(null))
                .map(addressResponse -> {
                    ResponseCode responseCode = ResponseCode.getResponseCode(addressResponse.getFelkod());
                    if (responseCode.getHttpStatus() != HttpStatus.OK) {
                        throw WebException.fromHttpStatus(responseCode.getHttpStatus(), "Error received from AddressMaster", SERVICE_NAME, responseCode.getMessage());
                    }
                    return addressResponse;
                });
    }
}
