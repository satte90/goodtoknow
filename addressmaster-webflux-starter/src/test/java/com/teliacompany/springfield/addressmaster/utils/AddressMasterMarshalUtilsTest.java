package com.teliacompany.springfield.addressmaster.utils;

import org.junit.Assert;
import org.junit.Test;
import se.telia.addressmaster.services.Address;

public class AddressMasterMarshalUtilsTest {
    @Test
    public void testMarshal() {
        Address address = new Address();
        address.setGatunamn("GRAN");
        address.setGatnr("2");
        address.setPostort("KÅGE");
        address.setIngang("");
        address.setPostnr("93494");
        String soapXml = AddressMasterMarshalUtils.marshalSoapRequest(address, "GetAddress");
        System.out.println(soapXml);
    }

    @Test
    public void testUnmarshal() {
        String xml = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <soap:Body>\n" +
                "        <ns1:GetAddressResponse xmlns:ns1=\"http://services.addressmaster.telia.se\">\n" +
                "            <return>\n" +
                "                <felkod>0</felkod>\n" +
                "                <gatnr>2</gatnr>\n" +
                "                <gatunamn>GRAN</gatunamn>\n" +
                "                <postnr>93494</postnr>\n" +
                "                <postort>KÅGE</postort>\n" +
                "                <punktid>105985717</punktid>\n" +
                "            </return>\n" +
                "        </ns1:GetAddressResponse>\n" +
                "    </soap:Body>\n" +
                "</soap:Envelope>";

        Address address = AddressMasterMarshalUtils.unmarshalSoapResponse(xml);
        Assert.assertEquals("0", address.getFelkod());
        Assert.assertEquals("2", address.getGatnr());
        Assert.assertEquals("GRAN", address.getGatunamn());
        Assert.assertEquals("93494", address.getPostnr());
        Assert.assertEquals("KÅGE", address.getPostort());
        Assert.assertEquals("105985717", address.getPunktid());
    }
}
