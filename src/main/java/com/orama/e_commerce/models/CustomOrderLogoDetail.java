package com.orama.e_commerce.models;

import com.orama.e_commerce.enums.LogoPosition;
import com.orama.e_commerce.enums.LogoTechnique;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "tb_custom_order_logo_detail")
public class CustomOrderLogoDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "custom_order_id", nullable = false)
  private CustomOrder customOrder;

  @Enumerated(EnumType.STRING)
  @Column(length = 25, nullable = false)
  private LogoPosition position;

  @Enumerated(EnumType.STRING)
  @Column(length = 25, nullable = false)
  private LogoTechnique technique;

  public CustomOrderLogoDetail() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public CustomOrder getCustomOrder() {
    return customOrder;
  }

  public void setCustomOrder(CustomOrder customOrder) {
    this.customOrder = customOrder;
  }

  public LogoPosition getPosition() {
    return position;
  }

  public void setPosition(LogoPosition position) {
    this.position = position;
  }

  public LogoTechnique getTechnique() {
    return technique;
  }

  public void setTechnique(LogoTechnique technique) {
    this.technique = technique;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof CustomOrderLogoDetail d && Objects.equals(id, d.id));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
