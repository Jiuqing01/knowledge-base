package edu.ngd.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileUploadConfig {

    private String uploadDir = "uploads";
    
    private Long maxFileSize = 524288000L;
    
    private List<String> allowedExtensions = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "jpg", "jpeg", "png", "gif", "bmp", "webp",
            "txt", "md", "zip", "rar"
    );
}
