package com.rekor.file_processing.config.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rekor.file_processing.config.service.DocumentService;
import com.rekor.file_processing.model.Document;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/documents")
public class DocumentController {
    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Document> create(@RequestBody Map<String, String> body) {
        Document document = service.create(body.get("fileName"));

        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }
    
}
