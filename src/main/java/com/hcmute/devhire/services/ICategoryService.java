package com.hcmute.devhire.services;


import com.hcmute.devhire.entities.Category;

import java.util.List;

public interface ICategoryService {
    Category findById(Long id);
    List<Category> getAllCategory();
}
