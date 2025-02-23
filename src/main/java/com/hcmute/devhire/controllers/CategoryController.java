package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.CategoryDTO;
import com.hcmute.devhire.entities.Category;
import com.hcmute.devhire.services.CategoryService;
import com.hcmute.devhire.services.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping("")
    public ResponseEntity<?> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategory();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/add")
    public ResponseEntity<?> saveCategory(@RequestParam String category) {
        try {
            CategoryDTO newCategory = categoryService.saveCategory(category);
            return ResponseEntity.ok(newCategory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateCategory(@RequestParam Long id, @RequestParam String category) {
        try {
            CategoryDTO updatedCategory = categoryService.updateCategory(id, category);
            return ResponseEntity.ok(updatedCategory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
