package com.orama.e_commerce.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "tb_order_shipping_address")
public class OrderShippingAddress {

  @Id
  @Column(name = "order_id")
  private Long orderId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "order_id")
  private Order order;

  @Column(name = "recipient_name", length = 120)
  private String recipientName;

  @Column(name = "recipient_phone", length = 20)
  private String recipientPhone;

  @Column(name = "zip_code", nullable = false, length = 20)
  private String zipCode;

  @Column(nullable = false, length = 255)
  private String street;

  @Column(nullable = false, length = 20)
  private String number;

  @Column(length = 100)
  private String complement;

  @Column(nullable = false, length = 100)
  private String district;

  @Column(length = 255)
  private String reference;

  @Column(name = "city_name", nullable = false, length = 150)
  private String cityName;

  @Column(name = "state_uf", nullable = false, length = 10)
  private String stateUf;

  @Column(name = "country_code", nullable = false, length = 10)
  private String countryCode = "BR";

  @Column(name = "ibge_code", length = 7)
  private String ibgeCode;

  @Column(precision = 10, scale = 7)
  private BigDecimal latitude;

  @Column(precision = 10, scale = 7)
  private BigDecimal longitude;

  @Column(name = "original_address_id")
  private Long originalAddressId;

  @CreationTimestamp
  @Column(name = "snapshot_at", nullable = false, updatable = false)
  private Instant snapshotAt;

  public static OrderShippingAddress fromAddress(Order order, Address address) {
    OrderShippingAddress snapshot = new OrderShippingAddress();
    snapshot.setOrder(order);
    snapshot.setRecipientName(address.getRecipientName());
    snapshot.setRecipientPhone(address.getRecipientPhone());
    snapshot.setZipCode(address.getZipCode());
    snapshot.setStreet(address.getStreet());
    snapshot.setNumber(hasText(address.getNumber()) ? address.getNumber() : "S/N");
    snapshot.setComplement(address.getComplement());
    snapshot.setDistrict(address.getDistrict());
    snapshot.setReference(address.getReference());
    snapshot.setCityName(resolveCityName(address));
    snapshot.setStateUf(resolveStateUf(address));
    snapshot.setCountryCode(hasText(address.getCountryCode()) ? address.getCountryCode() : "BR");
    snapshot.setIbgeCode(address.getIbgeCode());
    snapshot.setLatitude(address.getLatitude());
    snapshot.setLongitude(address.getLongitude());
    snapshot.setOriginalAddressId(address.getId());
    return snapshot;
  }

  private static String resolveCityName(Address address) {
    if (hasText(address.getCityName())) {
      return address.getCityName();
    }
    return address.getCity() != null ? address.getCity().getName() : null;
  }

  private static String resolveStateUf(Address address) {
    if (hasText(address.getStateUf())) {
      return address.getStateUf();
    }
    return address.getCity() != null && address.getCity().getState() != null
        ? address.getCity().getState().getAbbreviation()
        : null;
  }

  private static boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public String getRecipientName() {
    return recipientName;
  }

  public void setRecipientName(String recipientName) {
    this.recipientName = recipientName;
  }

  public String getRecipientPhone() {
    return recipientPhone;
  }

  public void setRecipientPhone(String recipientPhone) {
    this.recipientPhone = recipientPhone;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public String getComplement() {
    return complement;
  }

  public void setComplement(String complement) {
    this.complement = complement;
  }

  public String getDistrict() {
    return district;
  }

  public void setDistrict(String district) {
    this.district = district;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getCityName() {
    return cityName;
  }

  public void setCityName(String cityName) {
    this.cityName = cityName;
  }

  public String getStateUf() {
    return stateUf;
  }

  public void setStateUf(String stateUf) {
    this.stateUf = stateUf;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getIbgeCode() {
    return ibgeCode;
  }

  public void setIbgeCode(String ibgeCode) {
    this.ibgeCode = ibgeCode;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public void setLatitude(BigDecimal latitude) {
    this.latitude = latitude;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
  }

  public Long getOriginalAddressId() {
    return originalAddressId;
  }

  public void setOriginalAddressId(Long originalAddressId) {
    this.originalAddressId = originalAddressId;
  }

  public Instant getSnapshotAt() {
    return snapshotAt;
  }

  public void setSnapshotAt(Instant snapshotAt) {
    this.snapshotAt = snapshotAt;
  }
}
