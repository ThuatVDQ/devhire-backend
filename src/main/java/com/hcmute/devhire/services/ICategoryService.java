package com.hcmute.devhire.services;


import com.hcmute.devhire.entities.Category;

public interface ICategoryService {
    Category findById(Long id);
}
