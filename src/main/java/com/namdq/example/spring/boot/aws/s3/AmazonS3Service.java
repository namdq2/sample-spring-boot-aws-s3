package com.namdq.example.spring.boot.aws.s3;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URL;

@Service
@Slf4j
public class AmazonS3Service {

  private AmazonS3 amazonS3;

  @Value("${cloud.aws.credentials.accessKey}")
  private String accessKey;

  @Value("${cloud.aws.credentials.secretKey}")
  private String secretKey;

  @Value("${cloud.aws.region.static}")
  private String region;

  @Value("${app.awsServices.bucketName}")
  private String bucketName;

  @Value("${app.awsServices.defaultPath}")
  private String defaultPath;

  @Value("${app.awsServices.endPointUrl}")
  private String endPointUrl;

  @PostConstruct
  private void initializeAmazon() {
    log.info("(initializeAmazon) accessKey = {}, secretKey = {}", accessKey, secretKey);

    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
    amazonS3 = AmazonS3ClientBuilder
        .standard()
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withRegion(Regions.fromName(region))
        .build();
  }

  public String uploadFile(String targetFileName, File filePath, boolean isPublic,
      boolean isReplace) {
    try {
      if (doesObjectExist(targetFileName) && !isReplace) {
        log.info("(uploadFile) targetFileName = {}, filePath = {}, isPublic = {}, object exist",
            targetFileName, filePath, isPublic);
        return null;
      }

      String objectKey = defaultPath + targetFileName;
      if (isPublic) {
        amazonS3.putObject(new PutObjectRequest(bucketName, objectKey, filePath)
            .withCannedAcl(CannedAccessControlList.PublicRead));

        log.info("(uploadFile) targetFileName = {}, filePath = {}, isPublic = {}, uploaded",
            targetFileName, filePath, isPublic);

        return endPointUrl + objectKey;
      }

      amazonS3.putObject(new PutObjectRequest(bucketName, objectKey, filePath));

      log.info("(uploadFile) targetFileName = {}, filePath = {}, isPublic = {}, uploaded",
          targetFileName, filePath, isPublic);

      return getObjectUrl(targetFileName);
    } catch (SdkClientException e) {
      log.info("(uploadFile) targetFileName = {}, filePath = {}, isPublic = {}, error = {}",
          targetFileName, filePath, isPublic, e.getMessage());
    }
    return null;
  }

  public String getObjectUrl(String objectName) {
    try {
      GeneratePresignedUrlRequest generatePresignedUrlRequest =
          new GeneratePresignedUrlRequest(bucketName, defaultPath + objectName)
              .withMethod(HttpMethod.GET);

      URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

      log.info("(getObjectUrl) objectName = {}, objectUrl = {}", objectName, url.toString());
      return url.toString();
    } catch (SdkClientException e) {
      log.error("(getObjectUrl) objectName = {}, error = {} ", objectName, e.getMessage());
    }
    return null;
  }

  public Boolean removeObject(String objectName) {
    try {
      if (objectName == null) {
        return false;
      }

      objectName = objectName.trim();

      if (objectName.length() < 1) {
        return false;
      }

      if (!doesObjectExist(objectName)) {
        return false;
      }

      amazonS3.deleteObject(bucketName, defaultPath + objectName);

      log.info("(removeObject) objectName = {}, removed", objectName);
      return true;
    } catch (SdkClientException e) {
      log.info("(removeObject) objectName = {}, error = {}", objectName, e.getMessage());
    }
    return false;
  }

  private Boolean doesObjectExist(String objectName) {
    log.info("(doesObjectExist) objectName = {}", objectName);
    return amazonS3.doesObjectExist(bucketName, defaultPath + objectName);
  }

  public ObjectListing getObjectList() {
    log.info("(getObjectList)");
    return amazonS3.listObjects(bucketName);
  }
}
