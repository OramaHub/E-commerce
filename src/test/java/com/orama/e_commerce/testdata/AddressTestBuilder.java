package com.orama.e_commerce.testdata;

import com.orama.e_commerce.models.Address;

public class AddressTestBuilder {

  private Long id = 10L;
  private String street = "Rua das Flores";
  private String number = "123";
  private String district = "Centro";
  private String zipCode = "01234-567";
  private String cityName = "Sao Paulo";
  private String stateAbbreviation = "SP";

  public static AddressTestBuilder anAddress() {
    return new AddressTestBuilder();
  }

  public AddressTestBuilder withId(Long id) {
    this.id = id;
    return this;
  }

  public AddressTestBuilder withStreet(String street) {
    this.street = street;
    return this;
  }

  public AddressTestBuilder withNumber(String number) {
    this.number = number;
    return this;
  }

  public AddressTestBuilder withDistrict(String district) {
    this.district = district;
    return this;
  }

  public AddressTestBuilder withZipCode(String zipCode) {
    this.zipCode = zipCode;
    return this;
  }

  public AddressTestBuilder withCityName(String cityName) {
    this.cityName = cityName;
    return this;
  }

  public AddressTestBuilder withStateAbbreviation(String stateAbbreviation) {
    this.stateAbbreviation = stateAbbreviation;
    return this;
  }

  public Address build() {
    Address address = new Address(id);
    address.setStreet(street);
    address.setNumber(number);
    address.setDistrict(district);
    address.setZipCode(zipCode);
    address.setCityName(cityName);
    address.setStateUf(stateAbbreviation);
    address.setCountryCode("BR");
    return address;
  }
}
