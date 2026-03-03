package com.rekor.file_processing.model;

import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.rekor.file_processing.model.repository.DocumentRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
public class DocumentConsumer {

    private final SqsClient sqsClient;
    private final DocumentRepository repository;
    private final S3Client s3Client;

    private final String queueUrl = "http://localhost:4566/000000000000/document-queue";
    private final String bucketName = "document-results";

    public DocumentConsumer(SqsClient sqsClient,
                            DocumentRepository repository,
                            S3Client s3Client) {
        this.sqsClient = sqsClient;
        this.repository = repository;
        this.s3Client = s3Client;
    }

    @Scheduled(fixedDelay = 5000)
    public void pollQueue() {

        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(1)
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();

        for (Message message : messages) {

            Long documentId = Long.valueOf(message.body());

            processDocument(documentId);

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());
        }
    }

    private void processDocument(Long documentId) {

        Optional<Document> optional = repository.findById(documentId);
        if (optional.isEmpty()) return;

        Document document = optional.get();

        document.setStatus(DocumentStatus.PROCESSING);
        repository.save(document);

        try {
            Thread.sleep(3000); // simula processamento
        } catch (InterruptedException ignored) {}

        saveResultToS3(document);

        document.setStatus(DocumentStatus.DONE);
        repository.save(document);
    }

    private void saveResultToS3(Document document) {

        String content = "Documento processado: " + document.getFilename();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key("result-" + document.getId() + ".txt")
                        .build(),
                RequestBody.fromString(content)
        );
    }
}