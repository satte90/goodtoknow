package com.teliacompany.springfield.addressmaster.utils;

import com.teliacompany.springfield.error.exception.server.InternalServerErrorException;
import se.telia.addressmaster.services.Address;
import com.teliacompany.springfield.addressmaster.model.AddressResponse;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public final class AddressMasterMarshalUtils {
    private static final JAXBContext context = createContext();
    private static final TransformerFactory factory = createFactory();
    private static final DocumentBuilderFactory documentBuilderFactory = createDocumentBuilderFactory();

    private AddressMasterMarshalUtils() {
        // Not to be instantiated
    }

    private static DocumentBuilderFactory createDocumentBuilderFactory() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return documentBuilderFactory;
        } catch(ParserConfigurationException e) {
            throw new InternalServerErrorException("Could not create factory!", "N/A", e);
        }
    }

    private static TransformerFactory createFactory() {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return factory;
        } catch(TransformerConfigurationException e) {
            throw new InternalServerErrorException("Could not create TransformerFactory!", "N/A", e);
        }
    }

    private static JAXBContext createContext() {
        try {
            return JAXBContext.newInstance(Address.class, AddressResponse.class);
        } catch(JAXBException e) {
            throw new InternalServerErrorException("Could not create context for package se.telia.addressmaster.services.*");
        }
    }

    public static Address unmarshalSoapResponse(String xml) {
        try {
            SOAPMessage message = MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            Unmarshaller unMarshaller = context.createUnmarshaller();
            JAXBElement<AddressResponse> jaxbElement = unMarshaller.unmarshal(message.getSOAPBody().extractContentAsDocument(), AddressResponse.class);
            return jaxbElement.getValue().getAddress();
        } catch(JAXBException | SOAPException | IOException e) {
            throw new InternalServerErrorException("Could not unwrap payload", "", e);
        }
    }

    public static String marshalSoapRequest(Address address, String servicePortName) {
        try {
            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            soapMessage.getSOAPPart().getEnvelope().setPrefix("soap");
            soapMessage.getSOAPHeader().setPrefix("soap");
            SOAPBody body = soapMessage.getSOAPBody();
            body.setPrefix("soap");
            SOAPBodyElement getAddress = body.addBodyElement(new QName("http://services.addressmaster.telia.se", servicePortName, "ns1"));
            SOAPElement adr = getAddress.addChildElement(QName.valueOf("adr"));
            addElement(adr, "boxnr", address.getBoxnr());
            addElement(adr, "felkod", address.getFelkod());
            addElement(adr, "format", address.getFormat());
            addElement(adr, "gatnr", address.getGatnr());
            addElement(adr, "gatunamn", address.getGatunamn());
            addElement(adr, "ingang", address.getIngang());
            addElement(adr, "lghnr", address.getLghnr());
            addElement(adr, "postnr", address.getPostnr());
            addElement(adr, "postort", address.getPostort());
            addElement(adr, "punktid", address.getPunktid());
            addElement(adr, "trappantal", address.getTrappantal());
            addElement(adr, "tvth", address.getTvth());
            addElement(adr, "uppgang", address.getUppgang());
            final StringWriter sw = new StringWriter();
            factory.newTransformer().transform(new DOMSource(soapMessage.getSOAPPart()), new StreamResult(sw));
            return sw.toString();
        } catch(SOAPException | TransformerException e) {
            throw new InternalServerErrorException("Could not create request", "N/A", e);
        }
    }

    private static void addElement(SOAPElement soapElement, String elementName, String elementValue) throws SOAPException {
        if(elementValue != null) {
            soapElement.addChildElement(QName.valueOf(elementName)).setValue(elementValue);
        }
    }


}
