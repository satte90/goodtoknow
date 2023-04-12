package com.teliacompany.springfield.managetelephone.facade.utils;

import com.teliacompany.springfield.error.exception.server.InternalServerErrorException;
import com.teliasonera.gesb.resources.telephonenumberinfo.ObjectFactory;
import com.teliasonera.gesb.resources.telephonenumberinfo.RequestType;
import com.teliasonera.gesb.resources.telephonenumberinfo.TelephoneNumberResponseType;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
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

public final class MarshalUtils {
    private static final JAXBContext context = createContext();
    private static final TransformerFactory factory = createFactory();
    private static final DocumentBuilderFactory documentBuilderFactory = createDocumentBuilderFactory();

    private MarshalUtils() {
        // Not to be instantiated
    }

    private static DocumentBuilderFactory createDocumentBuilderFactory() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return documentBuilderFactory;
        } catch (ParserConfigurationException e) {
            throw new InternalServerErrorException("Could not create factory!", "N/A", e);
        }
    }

    private static TransformerFactory createFactory() {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return factory;
        } catch (TransformerConfigurationException e) {
            throw new InternalServerErrorException("Could not create TransformerFactory!", "N/A", e);
        }
    }

    private static JAXBContext createContext() {
        try {
            return JAXBContext.newInstance("com.teliasonera.gesb.resources.telephonenumberinfo");
        } catch (JAXBException e) {
            throw new InternalServerErrorException("Could not create context for package com.teliasonera.gesb.resources.telephonenumberinfo.*");
        }
    }

    public static TelephoneNumberResponseType unmarshalSoapResponse(String xml) {
        try {
            SOAPMessage message = MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            Unmarshaller unMarshaller = context.createUnmarshaller();
            JAXBElement jaxbElement = (JAXBElement) unMarshaller.unmarshal(message.getSOAPBody().extractContentAsDocument());
            return (TelephoneNumberResponseType) jaxbElement.getValue();
        } catch (JAXBException | SOAPException | IOException e) {
            throw new InternalServerErrorException("Could not unwrap payload", "", e);
        }
    }

    public static String marshalSoapRequest(RequestType requestType) {
        try {
            JAXBElement<RequestType> rootElement = new ObjectFactory().createGetAvaliableTelephoneNumbersRequest(requestType);
            Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(rootElement, document);
            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            soapMessage.getSOAPBody().addDocument(document);
            final StringWriter sw = new StringWriter();
            factory.newTransformer().transform(new DOMSource(soapMessage.getSOAPPart()), new StreamResult(sw));
            return sw.toString();
        } catch (SOAPException | JAXBException | ParserConfigurationException | TransformerException e) {
            throw new InternalServerErrorException("Could not create request", "N/A", e);
        }
    }


}
