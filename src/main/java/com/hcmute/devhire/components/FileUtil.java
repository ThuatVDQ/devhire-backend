package com.hcmute.devhire.components;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
@Component
public class FileUtil {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    // Kiểm tra kích thước file
    public boolean isFileSizeValid(MultipartFile file) {
        return file.getSize() <= MAX_FILE_SIZE;
    }

    // Kiểm tra định dạng file (chỉ cho ảnh)
    public boolean isImageFormatValid(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    // Kiểm tra định dạng file (ảnh hoặc pdf)
    public boolean isImageOrPdfFormatValid(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null &&
                (contentType.startsWith("image/") || contentType.equals("application/pdf"));
    }
}
