package com.vbforge.bookapi.service;

import com.vbforge.bookapi.dto.PublisherDTO;

import java.util.List;

/**
 * Service interface for Publisher business logic
 */
public interface PublisherService {

    /**
     * Create a new publisher
     */
    PublisherDTO createPublisher(PublisherDTO publisherDTO);

    /**
     * Get publisher by ID
     */
    PublisherDTO getPublisherById(Long id);

    /**
     * Get all publishers
     */
    List<PublisherDTO> getAllPublishers();

    /**
     * Update publisher
     */
    PublisherDTO updatePublisher(Long id, PublisherDTO publisherDTO);

    /**
     * Delete publisher
     */
    void deletePublisher(Long id);

    /**
     * Search publishers by name
     */
    List<PublisherDTO> searchPublishersByName(String name);

}
