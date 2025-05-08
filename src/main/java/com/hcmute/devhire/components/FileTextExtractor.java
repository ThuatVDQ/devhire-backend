package com.hcmute.devhire.components;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class FileTextExtractor {

    public String extractText(MultipartFile file) throws IOException {
        String fileName = file.getName();

        if (fileName.toLowerCase().endsWith(".pdf")) {
            return extractTextFromPdf(file.getInputStream());
        } else if (fileName.toLowerCase().endsWith(".docx")) {
            return extractTextFromDocx(file.getInputStream());
        } else if (fileName.toLowerCase().endsWith(".doc")) {
            return extractTextFromDoc(file.getInputStream());
        } else if (fileName.toLowerCase().endsWith(".txt")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } else {
            throw new UnsupportedOperationException("Unsupported file type: " + fileName);
        }
    }

    private String extractTextFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(InputStream inputStream) throws IOException {
        XWPFDocument doc = new XWPFDocument(inputStream);
        StringBuilder text = new StringBuilder();

        for (XWPFParagraph para : doc.getParagraphs()) {
            text.append(para.getText()).append("\n");
        }

        return text.toString();
    }

    private String extractTextFromDoc(InputStream inputStream) throws IOException {
        HWPFDocument doc = new HWPFDocument(inputStream);
        return doc.getDocumentText();
    }
}
