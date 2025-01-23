package org.jfm.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jfm.domain.entities.Video;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

@Path("/sync/shield")
public class VideoShieldSyncResource {
  
  @Inject
  SqsClient sqs;

  @ConfigProperty(name = "queue.url")
  String queueUrl;

  static ObjectReader VIDEO_READER = new ObjectMapper().readerFor(Video.class);

  @GET
  public List<Video> receive() {
    List<Message> messages = sqs.receiveMessage(m -> m.maxNumberOfMessages(10).queueUrl(queueUrl)).messages();

    return messages.stream()
      .map(Message::body)
      .map(this::toVideo)
      .collect(Collectors.toList());
  }

  private Video toVideo(String message) {
    Video video = null;
    try {
      video = VIDEO_READER.readValue(message);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return video;
  }

}
