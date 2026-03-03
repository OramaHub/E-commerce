package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.media.MediaLibraryResponseDto;
import com.orama.e_commerce.exceptions.media.MediaLibraryNotFoundException;
import com.orama.e_commerce.models.MediaLibrary;
import com.orama.e_commerce.repository.MediaLibraryRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaLibraryService {

  private final MediaLibraryRepository repository;
  private final R2StorageService r2StorageService;

  public MediaLibraryService(MediaLibraryRepository repository, R2StorageService r2StorageService) {
    this.repository = repository;
    this.r2StorageService = r2StorageService;
  }

  public Page<MediaLibraryResponseDto> getAll(Pageable pageable) {
    return repository.findAll(pageable).map(this::toDto);
  }

  @Transactional
  public MediaLibraryResponseDto upload(MultipartFile file) {
    String filename = r2StorageService.upload(file);
    String url = r2StorageService.getPublicUrl(filename);

    MediaLibrary media = new MediaLibrary();
    media.setFilename(filename);
    media.setUrl(url);
    repository.save(media);

    return toDto(media);
  }

  @Transactional
  public void delete(Long id) {
    MediaLibrary media =
        repository
            .findById(id)
            .orElseThrow(
                () -> new MediaLibraryNotFoundException("Mídia não encontrada com id: " + id));

    r2StorageService.delete(media.getFilename());
    repository.delete(media);
  }

  private MediaLibraryResponseDto toDto(MediaLibrary media) {
    return new MediaLibraryResponseDto(media.getId(), media.getUrl(), media.getFilename());
  }
}
