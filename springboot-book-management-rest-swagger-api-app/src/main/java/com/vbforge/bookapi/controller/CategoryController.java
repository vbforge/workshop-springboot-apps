package com.vbforge.bookapi.controller;

import com.vbforge.bookapi.dto.CategoryDTO;
import com.vbforge.bookapi.dto.response.ApiResponse;
import com.vbforge.bookapi.service.CategoryService;
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
 * REST Controller for Category management
 * Base path: /api/categories
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category", description = "Creates a new category in the system")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("REST request to create category: {}", categoryDTO.getName());

        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", createdCategory));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Returns a single category by its ID")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.info("REST request to get category by ID: {}", id);

        CategoryDTO category = categoryService.getCategoryById(id);

        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Returns all categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        log.info("REST request to get all categories");

        List<CategoryDTO> categories = categoryService.getAllCategories();

        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Updates an existing category")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("REST request to update category with ID: {}", id);

        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);

        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updatedCategory));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Deletes a category from the system")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.info("REST request to delete category with ID: {}", id);

        categoryService.deleteCategory(id);

        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories", description = "Search categories by name")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> searchCategories(
            @Parameter(description = "Category name") @RequestParam String name) {
        log.info("REST request to search categories with name: {}", name);

        List<CategoryDTO> categories = categoryService.searchCategoriesByName(name);

        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/with-books")
    @Operation(summary = "Get categories with books", description = "Returns categories that have books")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategoriesWithBooks() {
        log.info("REST request to get categories with books");

        List<CategoryDTO> categories = categoryService.getCategoriesWithBooks();

        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
