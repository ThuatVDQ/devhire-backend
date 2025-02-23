package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CategoryDTO;
import com.hcmute.devhire.entities.Category;
import com.hcmute.devhire.repositories.CategoryRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class CategoryService implements ICategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public List<CategoryDTO> getAllCategory() {
        return categoryRepository.findAll().stream().map(this::convertDTO).toList();
    }

    @Override
    public CategoryDTO saveCategory(String category) {
        Optional<Category> categoryOptional = Optional.ofNullable(categoryRepository.findByName(category));
        if (categoryOptional.isPresent()) {
            throw new RuntimeException("Category already exists");
        }
        Category newCategory = Category.builder()
                .name(category)
                .build();
        categoryRepository.save(newCategory);

        return convertDTO(newCategory);
    }

    @Override
    public CategoryDTO updateCategory(Long id, String category) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        if (categoryOptional.isPresent()) {
            Category updateCategory = categoryOptional.get();
            updateCategory.setName(category);
            categoryRepository.save(updateCategory);
            return convertDTO(updateCategory);
        } else {
            throw new RuntimeException("Category not found");
        }
    }

    public CategoryDTO convertDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
