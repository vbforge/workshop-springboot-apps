package com.vbforge.bookapi.service;

import com.vbforge.bookapi.dto.CategoryDTO;

import java.util.List;

/**
 * Service interface for Category business logic
 */
public interface CategoryService {

    /**
     * Create a new category
     */
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    /**
     * Get category by ID
     */
    CategoryDTO getCategoryById(Long id);

    /**
     * Get all categories
     */
    List<CategoryDTO> getAllCategories();

    /**
     * Update category
     */
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);

    /**
     * Delete category
     */
    void deleteCategory(Long id);

    /**
     * Search categories by name
     */
    List<CategoryDTO> searchCategoriesByName(String name);

    /**
     * Get categories with books
     */
    List<CategoryDTO> getCategoriesWithBooks();

}
