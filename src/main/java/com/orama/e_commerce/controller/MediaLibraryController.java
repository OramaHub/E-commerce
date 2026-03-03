package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.media.MediaLibraryResponseDto;
import com.orama.e_commerce.service.MediaLibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@Tag(name = "Biblioteca de Imagens")
public class MediaLibraryController {

  private final MediaLibraryService mediaLibraryService;

  public MediaLibraryController(MediaLibraryService mediaLibraryService) {
    this.mediaLibraryService = mediaLibraryService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  @Operation(summary = "Lista imagens da biblioteca")
  public ResponseEntity<Page<MediaLibraryResponseDto>> getAll(Pageable pageable) {
    return ResponseEntity.ok(mediaLibraryService.getAll(pageable));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Faz upload de uma imagem para o R2")
  public ResponseEntity<MediaLibraryResponseDto> upload(@RequestParam("file") MultipartFile file) {
    return ResponseEntity.status(HttpStatus.CREATED).body(mediaLibraryService.upload(file));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  @Operation(summary = "Remove uma imagem da biblioteca e do R2")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    mediaLibraryService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
