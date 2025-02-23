package com.hcmute.devhire.services;


import com.hcmute.devhire.DTOs.CategoryDTO;
import com.hcmute.devhire.entities.Category;

import java.util.List;

public interface ICategoryService {
    Category findById(Long id);
    List<CategoryDTO> getAllCategory();
    CategoryDTO saveCategory(String category);
    CategoryDTO updateCategory(Long id, String category);
}
