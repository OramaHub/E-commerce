package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.custom_order.CreateCustomOrderRequestDto;
import com.orama.e_commerce.dtos.custom_order.CustomOrderResponseDto;
import com.orama.e_commerce.dtos.custom_order.LogoDetailDto;
import com.orama.e_commerce.enums.CustomOrderStatus;
import com.orama.e_commerce.exceptions.custom_order.CustomOrderNotFoundException;
import com.orama.e_commerce.mapper.CustomOrderMapper;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.models.CustomOrder;
import com.orama.e_commerce.models.CustomOrderLogoDetail;
import com.orama.e_commerce.repository.ClientRepository;
import com.orama.e_commerce.repository.CustomOrderRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CustomOrderService {

  private final CustomOrderRepository customOrderRepository;
  private final ClientRepository clientRepository;
  private final CustomOrderMapper customOrderMapper;
  private final R2StorageService r2StorageService;

  public CustomOrderService(
      CustomOrderRepository customOrderRepository,
      ClientRepository clientRepository,
      CustomOrderMapper customOrderMapper,
      R2StorageService r2StorageService) {
    this.customOrderRepository = customOrderRepository;
    this.clientRepository = clientRepository;
    this.customOrderMapper = customOrderMapper;
    this.r2StorageService = r2StorageService;
  }

  @Transactional
  public CustomOrderResponseDto createCustomOrder(CreateCustomOrderRequestDto dto, Long clientId) {
    Client client =
        clientRepository
            .findById(clientId)
            .orElseThrow(
                () -> new IllegalArgumentException("Cliente não encontrado com id: " + clientId));

    CustomOrder customOrder = customOrderMapper.toEntity(dto);
    customOrder.setClient(client);
    customOrder.setOrderNumber(generateOrderNumber());
    customOrder.setStatus(CustomOrderStatus.DRAFT);
    customOrder.setLaserCut(dto.laserCut() != null ? dto.laserCut() : false);
    customOrder.setFullLaserCut(dto.fullLaserCut() != null ? dto.fullLaserCut() : false);

    if (dto.logoDetails() != null && !dto.logoDetails().isEmpty()) {
      List<CustomOrderLogoDetail> details = createLogoDetails(dto.logoDetails(), customOrder);
      customOrder.setLogoDetails(details);
    } else {
      customOrder.setLogoDetails(new ArrayList<>());
    }

    CustomOrder saved = customOrderRepository.save(customOrder);
    return customOrderMapper.toResponseDto(saved);
  }

  public CustomOrderResponseDto getById(Long id) {
    CustomOrder customOrder =
        customOrderRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new CustomOrderNotFoundException(
                        "Pedido personalizado não encontrado com id: " + id));
    return customOrderMapper.toResponseDto(customOrder);
  }

  public List<CustomOrderResponseDto> getByClient(Long clientId) {
    return customOrderRepository.findByClientId(clientId).stream()
        .map(customOrderMapper::toResponseDto)
        .toList();
  }

  public List<CustomOrderResponseDto> getAllOrders() {
    return customOrderRepository.findAll().stream().map(customOrderMapper::toResponseDto).toList();
  }

  @Transactional
  public CustomOrderResponseDto updateStatus(Long id, CustomOrderStatus newStatus) {
    CustomOrder customOrder =
        customOrderRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new CustomOrderNotFoundException(
                        "Pedido personalizado não encontrado com id: " + id));

    customOrder.setStatus(newStatus);
    CustomOrder updated = customOrderRepository.save(customOrder);
    return customOrderMapper.toResponseDto(updated);
  }

  @Transactional
  public CustomOrderResponseDto uploadLogo(Long id, MultipartFile file) {
    CustomOrder customOrder =
        customOrderRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new CustomOrderNotFoundException(
                        "Pedido personalizado não encontrado com id: " + id));

    String filename = r2StorageService.upload(file);
    String url = r2StorageService.getPublicUrl(filename);
    customOrder.setLogoUrl(url);

    CustomOrder updated = customOrderRepository.save(customOrder);
    return customOrderMapper.toResponseDto(updated);
  }

  @Transactional
  public CustomOrderResponseDto uploadPreview(Long id, MultipartFile file) {
    CustomOrder customOrder =
        customOrderRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new CustomOrderNotFoundException(
                        "Pedido personalizado não encontrado com id: " + id));

    String filename = r2StorageService.upload(file);
    String url = r2StorageService.getPublicUrl(filename);
    customOrder.setPreviewImageUrl(url);

    CustomOrder updated = customOrderRepository.save(customOrder);
    return customOrderMapper.toResponseDto(updated);
  }

  private String generateOrderNumber() {
    String prefix = "CUST-" + Instant.now() + "-";
    String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    String orderNumber = prefix + uniqueId;

    while (customOrderRepository.existsByOrderNumber(orderNumber)) {
      uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
      orderNumber = prefix + uniqueId;
    }

    return orderNumber;
  }

  private List<CustomOrderLogoDetail> createLogoDetails(
      List<LogoDetailDto> dtos, CustomOrder customOrder) {
    return dtos.stream()
        .map(
            dto -> {
              CustomOrderLogoDetail detail = new CustomOrderLogoDetail();
              detail.setCustomOrder(customOrder);
              detail.setPosition(dto.position());
              detail.setTechnique(dto.technique());
              return detail;
            })
        .toList();
  }
}
