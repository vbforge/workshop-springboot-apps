package com.vbforge.bookapi.service.impl;

import com.vbforge.bookapi.dto.CategoryDTO;
import com.vbforge.bookapi.entity.Category;
import com.vbforge.bookapi.exception.DuplicateResourceException;
import com.vbforge.bookapi.exception.ResourceNotFoundException;
import com.vbforge.bookapi.mapper.BookMapper;
import com.vbforge.bookapi.repository.CategoryRepository;
import com.vbforge.bookapi.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Implementation of CategoryService
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        log.info("Creating new category: {}", categoryDTO.getName());

        //check name is valid (uniqueness)
        if(categoryRepository.existsByName(categoryDTO.getName())){
            throw new DuplicateResourceException("Category", "name", categoryDTO.getName());
        }

        Category category = bookMapper.toEntity(categoryDTO);
        Category saved = categoryRepository.save(category);

        log.info("Category created successfully with ID: {}", category.getId());

        return bookMapper.toDTO(saved);
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        log.debug("Fetching category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        return bookMapper.toDTO(category);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        log.debug("Fetching all categories");

        List<Category> categories = categoryRepository.findAll();
        return bookMapper.categoriesToDTOList(categories);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        log.info("Updating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Check name uniqueness if name is being changed
        if (!category.getName().equals(categoryDTO.getName()) && categoryRepository.existsByName(categoryDTO.getName())) {
            throw new DuplicateResourceException("Category", "name", categoryDTO.getName());
        }

        // Update fields
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", updatedCategory.getId());

        return bookMapper.toDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);

        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }

        categoryRepository.deleteById(id);
        log.info("Category deleted successfully with ID: {}", id);
    }

    @Override
    public List<CategoryDTO> searchCategoriesByName(String name) {
        log.debug("Searching categories by name: {}", name);

        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
        return bookMapper.categoriesToDTOList(categories);
    }

    @Override
    public List<CategoryDTO> getCategoriesWithBooks() {
        log.info("Fetching categories with books");

        List<Category> categoriesWithBooks = categoryRepository.findCategoriesWithBooks();

        return bookMapper.categoriesToDTOList(categoriesWithBooks);
    }
}
