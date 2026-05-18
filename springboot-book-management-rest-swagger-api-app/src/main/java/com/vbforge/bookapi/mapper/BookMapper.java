package com.vbforge.bookapi.mapper;

import com.vbforge.bookapi.dto.*;
import com.vbforge.bookapi.entity.*;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

/**
 * MapStruct mapper interface for:
 *  - Book Mappings;
 *  - Author Mappings;
 *  - Category Mappings;
 *  - Publisher Mappings;
 * MapStruct will automatically generate implementation at compile time
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

    // ========================================
    // Book Mappings
    // ========================================

    /**
     * Convert Book entity to BookDTO
     */
    @Mapping(target = "authors", source = "authors")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "publisher", source = "publisher")
    BookDTO toDTO(Book book);

    /**
     * Convert list of Book entities to list of BookDTOs
     */
    List<BookDTO> toDTOList(List<Book> books);

    /**
     * Convert BookCreateDTO to Book entity
     * Ignores relationships (they need to be set separately)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "publisher", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Book toEntity(BookCreateDTO dto);

    /**
     * Update Book entity from BookUpdateDTO
     * Only updates non-null fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isbn", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "publisher", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(BookUpdateDTO dto, @MappingTarget Book entity);

    // ========================================
    // Author Mappings
    // ========================================

    /**
     * Convert Author entity to AuthorDTO
     */
    @Mapping(target = "bookCount", expression = "java(author.getBooks() != null ? author.getBooks().size() : 0)")
    AuthorDTO toDTO(Author author);

    /**
     * Convert list of Author entities to list of AuthorDTOs
     */
    List<AuthorDTO> authorsToDTOList(List<Author> authors);

    /**
     * Convert set of Author entities to set of AuthorDTOs
     */
    Set<AuthorDTO> authorsToDTOSet(Set<Author> authors);

    /**
     * Convert AuthorDTO to Author entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Author toEntity(AuthorDTO dto);

    // ========================================
    // Category Mappings
    // ========================================

    /**
     * Convert Category entity to CategoryDTO
     */
    @Mapping(target = "bookCount", expression = "java(category.getBooks() != null ? category.getBooks().size() : 0)")
    CategoryDTO toDTO(Category category);

    /**
     * Convert list of Category entities to list of CategoryDTOs
     */
    List<CategoryDTO> categoriesToDTOList(List<Category> categories);

    /**
     * Convert CategoryDTO to Category entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategoryDTO dto);

    // ========================================
    // Publisher Mappings
    // ========================================

    /**
     * Convert Publisher entity to PublisherDTO
     */
    @Mapping(target = "bookCount", expression = "java(publisher.getBooks() != null ? publisher.getBooks().size() : 0)")
    PublisherDTO toDTO(Publisher publisher);

    /**
     * Convert list of Publisher entities to list of PublisherDTOs
     */
    List<PublisherDTO> publishersToDTOList(List<Publisher> publishers);

    /**
     * Convert PublisherDTO to Publisher entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Publisher toEntity(PublisherDTO dto);

}
