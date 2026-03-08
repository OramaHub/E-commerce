package com.orama.e_commerce.models;

import com.orama.e_commerce.enums.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "tb_custom_order")
public class CustomOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_number", length = 50, unique = true, nullable = false)
  private String orderNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id", nullable = false)
  private Client client;

  @Enumerated(EnumType.STRING)
  @Column(name = "cap_line", length = 20, nullable = false)
  private CapLine capLine;

  @Enumerated(EnumType.STRING)
  @Column(name = "cap_model", length = 30, nullable = false)
  private CapModel capModel;

  @Enumerated(EnumType.STRING)
  @Column(name = "cap_material", length = 20, nullable = false)
  private CapMaterial capMaterial;

  @Column(name = "laser_cut", nullable = false)
  private Boolean laserCut;

  @Column(name = "full_laser_cut", nullable = false)
  private Boolean fullLaserCut;

  @Enumerated(EnumType.STRING)
  @Column(name = "strap_type", length = 20, nullable = false)
  private StrapType strapType;

  @Column(name = "color_front", length = 7, nullable = false)
  private String colorFront;

  @Column(name = "color_mesh", length = 7)
  private String colorMesh;

  @Column(name = "color_brim", length = 7, nullable = false)
  private String colorBrim;

  @Column(name = "color_brim_lining", length = 7)
  private String colorBrimLining;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "logo_url", length = 1000)
  private String logoUrl;

  @Column(name = "preview_image_url", length = 1000)
  private String previewImageUrl;

  @Column(name = "layout_image_url", length = 1000)
  private String layoutImageUrl;

  @Column(length = 2000)
  private String observations;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  private CustomOrderStatus status;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @OneToMany(
      mappedBy = "customOrder",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<CustomOrderLogoDetail> logoDetails;

  public CustomOrder() {}

  public CustomOrder(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(String orderNumber) {
    this.orderNumber = orderNumber;
  }

  public Client getClient() {
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public CapLine getCapLine() {
    return capLine;
  }

  public void setCapLine(CapLine capLine) {
    this.capLine = capLine;
  }

  public CapModel getCapModel() {
    return capModel;
  }

  public void setCapModel(CapModel capModel) {
    this.capModel = capModel;
  }

  public CapMaterial getCapMaterial() {
    return capMaterial;
  }

  public void setCapMaterial(CapMaterial capMaterial) {
    this.capMaterial = capMaterial;
  }

  public Boolean getLaserCut() {
    return laserCut;
  }

  public void setLaserCut(Boolean laserCut) {
    this.laserCut = laserCut;
  }

  public Boolean getFullLaserCut() {
    return fullLaserCut;
  }

  public void setFullLaserCut(Boolean fullLaserCut) {
    this.fullLaserCut = fullLaserCut;
  }

  public StrapType getStrapType() {
    return strapType;
  }

  public void setStrapType(StrapType strapType) {
    this.strapType = strapType;
  }

  public String getColorFront() {
    return colorFront;
  }

  public void setColorFront(String colorFront) {
    this.colorFront = colorFront;
  }

  public String getColorMesh() {
    return colorMesh;
  }

  public void setColorMesh(String colorMesh) {
    this.colorMesh = colorMesh;
  }

  public String getColorBrim() {
    return colorBrim;
  }

  public void setColorBrim(String colorBrim) {
    this.colorBrim = colorBrim;
  }

  public String getColorBrimLining() {
    return colorBrimLining;
  }

  public void setColorBrimLining(String colorBrimLining) {
    this.colorBrimLining = colorBrimLining;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public String getLogoUrl() {
    return logoUrl;
  }

  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  public String getPreviewImageUrl() {
    return previewImageUrl;
  }

  public void setPreviewImageUrl(String previewImageUrl) {
    this.previewImageUrl = previewImageUrl;
  }

  public String getLayoutImageUrl() {
    return layoutImageUrl;
  }

  public void setLayoutImageUrl(String layoutImageUrl) {
    this.layoutImageUrl = layoutImageUrl;
  }

  public String getObservations() {
    return observations;
  }

  public void setObservations(String observations) {
    this.observations = observations;
  }

  public CustomOrderStatus getStatus() {
    return status;
  }

  public void setStatus(CustomOrderStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public List<CustomOrderLogoDetail> getLogoDetails() {
    return logoDetails;
  }

  public void setLogoDetails(List<CustomOrderLogoDetail> logoDetails) {
    this.logoDetails = logoDetails;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof CustomOrder co && Objects.equals(id, co.id));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "CustomOrder{id=" + id + ", number='" + orderNumber + "'}";
  }
}
