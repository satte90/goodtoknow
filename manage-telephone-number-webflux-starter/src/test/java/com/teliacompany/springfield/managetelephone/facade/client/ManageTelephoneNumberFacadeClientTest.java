package com.teliacompany.springfield.managetelephone.facade.client;

import com.teliacompany.springfield.managetelephone.facade.utils.MarshalUtils;
import com.teliacompany.springfield.webflux.client.WebClient;
import com.teliasonera.gesb.resources.telephonenumberinfo.TelephoneNumberResponseType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ManageTelephoneNumberFacadeClientTest {
    private ManageTelephoneNumberFacadeClient client;

    @Before
    public void setUp() {
        WebClient mockClient = Mockito.mock(WebClient.class);
        client = new ManageTelephoneNumberFacadeClient(mockClient);
    }



    @Test
    public void testCreateRequest() {
        String actual = client.createXmlRequest("130037238");
        Assert.assertNotNull(actual);
    }

    @Test
    public void testUnMarshallResponse() {
        String expectedNumber = "031826895";
        String responseString = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <SOAP-ENV:Body>\n" +
                "      <ns0:GetAvaliableTelephoneNumbersResponse xmlns:ns0=\"http://www.teliasonera.com/gesb/resources/TelephoneNumberInfo.xsd\">\n" +
                "         <ns0:ResponseStatus>\n" +
                "            <ns0:Code>0</ns0:Code>\n" +
                "            <ns0:Source>IPMS</ns0:Source>\n" +
                "            <ns0:Description/>\n" +
                "         </ns0:ResponseStatus>\n" +
                "         <ns0:TelephoneNumberList>\n" +
                "            <ns0:TelephoneNumber>031826895</ns0:TelephoneNumber>\n" +
                "         </ns0:TelephoneNumberList>\n" +
                "         <ns0:EndIndex>1</ns0:EndIndex>\n" +
                "         <ns0:EndOfList>True</ns0:EndOfList>\n" +
                "      </ns0:GetAvaliableTelephoneNumbersResponse>\n" +
                "   </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";
        TelephoneNumberResponseType response = MarshalUtils.unmarshalSoapResponse(responseString);
        Assert.assertNotNull(response);
        Assert.assertEquals("0", response.getResponseStatus().getCode());
        Assert.assertEquals(expectedNumber, response.getTelephoneNumberList().getTelephoneNumber().get(0));

    }
}
