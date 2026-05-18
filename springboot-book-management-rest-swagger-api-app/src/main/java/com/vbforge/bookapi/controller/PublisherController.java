package com.vbforge.bookapi.controller;

import com.vbforge.bookapi.dto.PublisherDTO;
import com.vbforge.bookapi.dto.response.ApiResponse;
import com.vbforge.bookapi.service.PublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Publisher management
 * Base path: /api/publishers
 */
@RestController
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Publishers", description = "Publisher management APIs")
public class PublisherController {

    private final PublisherService publisherService;

    @PostMapping
    @Operation(summary = "Create a new publisher", description = "Creates a new publisher in the system")
    public ResponseEntity<ApiResponse<PublisherDTO>> createPublisher(
            @Valid @RequestBody PublisherDTO publisherDTO) {
        log.info("REST request to create publisher: {}", publisherDTO.getName());

        PublisherDTO createdPublisher = publisherService.createPublisher(publisherDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Publisher created successfully", createdPublisher));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get publisher by ID", description = "Returns a single publisher by its ID")
    public ResponseEntity<ApiResponse<PublisherDTO>> getPublisherById(
            @Parameter(description = "Publisher ID") @PathVariable Long id) {
        log.info("REST request to get publisher by ID: {}", id);

        PublisherDTO publisher = publisherService.getPublisherById(id);

        return ResponseEntity.ok(ApiResponse.success(publisher));
    }

    @GetMapping
    @Operation(summary = "Get all publishers", description = "Returns all publishers")
    public ResponseEntity<ApiResponse<List<PublisherDTO>>> getAllPublishers() {
        log.info("REST request to get all publishers");

        List<PublisherDTO> publishers = publisherService.getAllPublishers();

        return ResponseEntity.ok(ApiResponse.success(publishers));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update publisher", description = "Updates an existing publisher")
    public ResponseEntity<ApiResponse<PublisherDTO>> updatePublisher(
            @Parameter(description = "Publisher ID") @PathVariable Long id,
            @Valid @RequestBody PublisherDTO publisherDTO) {
        log.info("REST request to update publisher with ID: {}", id);

        PublisherDTO updatedPublisher = publisherService.updatePublisher(id, publisherDTO);

        return ResponseEntity.ok(ApiResponse.success("Publisher updated successfully", updatedPublisher));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete publisher", description = "Deletes a publisher from the system")
    public ResponseEntity<ApiResponse<Void>> deletePublisher(
            @Parameter(description = "Publisher ID") @PathVariable Long id) {
        log.info("REST request to delete publisher with ID: {}", id);

        publisherService.deletePublisher(id);

        return ResponseEntity.ok(ApiResponse.success("Publisher deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search publishers", description = "Search publishers by name")
    public ResponseEntity<ApiResponse<List<PublisherDTO>>> searchPublishers(
            @Parameter(description = "Publisher name") @RequestParam String name) {
        log.info("REST request to search publishers with name: {}", name);

        List<PublisherDTO> publishers = publisherService.searchPublishersByName(name);

        return ResponseEntity.ok(ApiResponse.success(publishers));
    }
}
