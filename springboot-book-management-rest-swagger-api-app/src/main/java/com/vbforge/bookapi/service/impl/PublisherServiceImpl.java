package com.vbforge.bookapi.service.impl;

import com.vbforge.bookapi.dto.PublisherDTO;
import com.vbforge.bookapi.entity.Publisher;
import com.vbforge.bookapi.exception.DuplicateResourceException;
import com.vbforge.bookapi.exception.ResourceNotFoundException;
import com.vbforge.bookapi.mapper.BookMapper;
import com.vbforge.bookapi.repository.PublisherRepository;
import com.vbforge.bookapi.service.PublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of PublisherService
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional
    public PublisherDTO createPublisher(PublisherDTO publisherDTO) {
        log.info("Creating new publisher: {}", publisherDTO.getName());

        //check name is valid (uniqueness)
        if(publisherRepository.existsByName(publisherDTO.getName())){
            throw new DuplicateResourceException("Publisher", "name ", publisherDTO.getName());
        }

        Publisher publisher = bookMapper.toEntity(publisherDTO);
        Publisher savedPublisher = publisherRepository.save(publisher);

        log.info("Publisher created successfully with ID: {}", savedPublisher.getId());

        return bookMapper.toDTO(savedPublisher);
    }

    @Override
    public PublisherDTO getPublisherById(Long id) {
        log.debug("Fetching publisher with ID: {}", id);

        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publisher not found with ID: " + id));

        return bookMapper.toDTO(publisher);
    }

    @Override
    public List<PublisherDTO> getAllPublishers() {
        log.debug("Fetching all publishers");

        List<Publisher> publishers = publisherRepository.findAll();
        return bookMapper.publishersToDTOList(publishers);
    }

    @Override
    @Transactional
    public PublisherDTO updatePublisher(Long id, PublisherDTO publisherDTO) {
        log.info("Updating publisher with ID: {}", id);

        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publisher not found with ID: " + id));

        // Check name uniqueness if name is being changed
        if (!publisher.getName().equals(publisherDTO.getName()) && publisherRepository.existsByName(publisherDTO.getName())) {
            throw new DuplicateResourceException("Publisher", "name ", publisherDTO.getName());
        }

        // Update fields
        publisher.setName(publisherDTO.getName());
        publisher.setAddress(publisherDTO.getAddress());
        publisher.setWebsite(publisherDTO.getWebsite());
        publisher.setContactEmail(publisherDTO.getContactEmail());

        Publisher updatedPublisher = publisherRepository.save(publisher);
        log.info("Publisher updated successfully with ID: {}", updatedPublisher.getId());

        return bookMapper.toDTO(updatedPublisher);
    }

    @Override
    @Transactional
    public void deletePublisher(Long id) {
        log.info("Deleting publisher with ID: {}", id);

        if (!publisherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Publisher not found with ID: " + id);
        }

        publisherRepository.deleteById(id);
        log.info("Publisher deleted successfully with ID: {}", id);
    }

    @Override
    public List<PublisherDTO> searchPublishersByName(String name) {
        log.debug("Searching publishers by name: {}", name);

        List<Publisher> publishers = publisherRepository.findByNameContainingIgnoreCase(name);
        return bookMapper.publishersToDTOList(publishers);
    }
}
