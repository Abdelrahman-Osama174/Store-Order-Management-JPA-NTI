package com.store.embeddables;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    private String street;
    private String city;
    private String country;

    public Address() {}

    public Address(String street, String city, String country) {
        this.street = street;
        this.city = city;
        this.country = country;
    }

    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
}