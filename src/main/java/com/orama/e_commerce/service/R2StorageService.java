package com.orama.e_commerce.service;

import com.orama.e_commerce.exceptions.StorageException;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class R2StorageService {

  @Value("${cloudflare.r2.account-id}")
  private String accountId;

  @Value("${cloudflare.r2.access-key-id}")
  private String accessKeyId;

  @Value("${cloudflare.r2.secret-access-key}")
  private String secretAccessKey;

  @Value("${cloudflare.r2.bucket-name}")
  private String bucketName;

  @Value("${cloudflare.r2.public-url}")
  private String publicUrl;

  private S3Client s3Client;

  @PostConstruct
  private void init() {
    s3Client =
        S3Client.builder()
            .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
            .region(Region.of("auto"))
            .build();
  }

  public String upload(MultipartFile file) {
    String filename = UUID.randomUUID() + getExtension(file.getOriginalFilename());
    try {
      PutObjectRequest request =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(filename)
              .contentType(file.getContentType())
              .build();
      s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
      return filename;
    } catch (SdkException e) {
      throw new StorageException("Falha ao fazer upload para o R2: " + e.getMessage());
    } catch (Exception e) {
      throw new StorageException("Falha ao processar o arquivo: " + e.getMessage());
    }
  }

  public void delete(String filename) {
    try {
      DeleteObjectRequest request =
          DeleteObjectRequest.builder().bucket(bucketName).key(filename).build();
      s3Client.deleteObject(request);
    } catch (SdkException e) {
      throw new StorageException("Falha ao deletar objeto do R2: " + e.getMessage());
    }
  }

  public String getPublicUrl(String filename) {
    return publicUrl + "/" + filename;
  }

  private String getExtension(String originalFilename) {
    if (originalFilename == null || !originalFilename.contains(".")) return "";
    return originalFilename.substring(originalFilename.lastIndexOf("."));
  }
}
