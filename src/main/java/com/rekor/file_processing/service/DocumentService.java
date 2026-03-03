package com.rekor.file_processing.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.rekor.file_processing.model.Document;
import com.rekor.file_processing.model.DocumentStatus;
import com.rekor.file_processing.model.repository.DocumentRepository;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class DocumentService {
    private final DocumentRepository repository;
    private final SqsClient sqsClient;

    public DocumentService(DocumentRepository repository, SqsClient sqsClient) {
        this.repository = repository;
        this.sqsClient = sqsClient;
    }
    
    public Document create(String fileName) {
        Document document = new Document();
        document.setFilename(fileName);
        document.setStatus(DocumentStatus.RECEIVED);
        document.setCreatedAt(LocalDateTime.now());

        Document saved = repository.save(document);

        sendMessage(saved.getId());

        return saved;
    }

    private void sendMessage(Long documentId) {

        String queueUrl = "http://localhost:4566/000000000000/document-queue"; // 000000000000 é padrão do LocalStack

        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(documentId.toString())
                .build());
    }
}
