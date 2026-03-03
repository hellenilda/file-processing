package com.rekor.file_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rekor.file_processing.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

}