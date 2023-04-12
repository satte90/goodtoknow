package com.teliacompany.springfield.managetelephone.facade.client;

import com.teliacompany.springfield.error.exception.server.InternalServerErrorException;
import com.teliacompany.springfield.managetelephone.facade.config.ManageTelephoneNumberFacadeClientConfiguration;
import com.teliacompany.springfield.managetelephone.facade.utils.MarshalUtils;
import com.teliacompany.springfield.webflux.client.WebClient;
import com.teliasonera.gesb.resources.messageproperties.MessagePropertiesType;
import com.teliasonera.gesb.resources.telephonenumberinfo.RequestType;
import com.teliasonera.gesb.resources.telephonenumberinfo.ResponseStatus;
import com.teliasonera.gesb.resources.telephonenumberinfo.TelephoneNumberRequestType;
import com.teliasonera.gesb.resources.telephonenumberinfo.TelephoneNumberResponseType;
import reactor.core.publisher.Mono;

public class ManageTelephoneNumberFacadeClient {
    private final WebClient webClient;

    public ManageTelephoneNumberFacadeClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<TelephoneNumberResponseType> getAvailableTelephoneNumbers(final String pointId) {
        return webClient.post("")
                .body(createXmlRequest(pointId))
                .header("Content-Type", "text/xml")
                .retrieve(String.class)
                .map(payload -> payload.getBody()
                        .map(MarshalUtils::unmarshalSoapResponse)
                        .orElse(null))
                .map(response -> {
                    ResponseStatus responseStatus = response.getResponseStatus();
                    if (!("0").equalsIgnoreCase(responseStatus.getCode())) {
                        throw new InternalServerErrorException("Error received from Manage Telephone number", ManageTelephoneNumberFacadeClientConfiguration.SERVICE_NAME, responseStatus.getDescription());
                    }
                    return response;
                });
    }

    String createXmlRequest(String pointId) {
        final RequestType query = new RequestType();
        final TelephoneNumberRequestType request = new TelephoneNumberRequestType();
        final TelephoneNumberRequestType.Identifier identifier = new TelephoneNumberRequestType.Identifier();
        identifier.setPointID(pointId);
        request.setIdentifier(identifier);
        request.setTelephoneNumberFilter("%");
        request.setQuantityToReturn("1");
        request.setStartIndex("0");
        query.setRequest(request);
        query.setProperties(new MessagePropertiesType());
        return MarshalUtils.marshalSoapRequest(query);

    }

}
