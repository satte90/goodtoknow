package com.teliacompany.springfield.addressmaster.model;

import se.telia.addressmaster.services.Address;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Manually added to match correct response object when fetching addresses
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addressResponse", propOrder = {
        "address"
})
public class AddressResponse {
    @XmlElement(name = "return", required = true)
    private Address address;

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address value) {
        this.address = value;
    }
}
