package com.namdq.example.spring.boot.aws.s3;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@Slf4j
public class SampleSpringBootAwsS3Application implements CommandLineRunner {

  @Autowired
  private AmazonS3Service amazonS3Service;

  public static void main(String[] args) {
    SpringApplication.run(SampleSpringBootAwsS3Application.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    ObjectListing objectListing = amazonS3Service.getObjectList();
    List<String> objects = objectListing.getObjectSummaries().stream().map(S3ObjectSummary::getKey)
        .collect(Collectors.toList());
    log.info("objects = {}", objects);

    String url = amazonS3Service.uploadFile(
        "5.jpg",
        new File("D:\\Downloads\\color-design-flora-1166644.jpg"), true, true);

//        amazonS3Service.removeObject("videos/3.jpg");

    log.info("objectUrl = {}", url);
  }
}
