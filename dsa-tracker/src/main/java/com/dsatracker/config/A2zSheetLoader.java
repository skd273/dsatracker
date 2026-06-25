package com.dsatracker.config;

import com.dsatracker.model.Difficulty;
import com.dsatracker.model.Platform;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class A2zSheetLoader {

    public static final String SHEET_VERSION = "A2Z_SHEET_V5";

    private final SheetData sheetData;

    public A2zSheetLoader(ObjectMapper objectMapper) {
        try (InputStream in = new ClassPathResource("data/a2z-sheet.json").getInputStream()) {
            this.sheetData = objectMapper.readValue(in, SheetData.class);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load A2Z sheet data", ex);
        }
    }

    public SheetData getSheetData() {
        return sheetData;
    }

    public int topicCount() {
        return sheetData.topics().size();
    }

    public int problemCount() {
        return sheetData.topics().stream().mapToInt(t -> t.problems().size()).sum();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SheetData(String version, String category, List<SheetTopic> topics) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SheetTopic(String name, String description, String keyPatterns, List<SheetProblem> problems) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SheetProblem(
            String title,
            String slug,
            String difficulty,
            String keyPatterns,
            String url,
            String platform) {

        public Difficulty toDifficulty() {
            return Difficulty.valueOf(difficulty.toUpperCase());
        }

        public Platform toPlatform() {
            if (platform == null || platform.isBlank()) {
                return null;
            }
            return Platform.valueOf(platform.toUpperCase());
        }
    }
}
