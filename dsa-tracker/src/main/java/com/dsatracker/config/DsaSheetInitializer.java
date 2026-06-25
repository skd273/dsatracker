package com.dsatracker.config;

import com.dsatracker.service.DsaSheetMigrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DsaSheetInitializer {

    @Bean
    CommandLineRunner initDsaSheet(DsaSheetMigrationService migrationService) {
        return args -> {
            if (migrationService.needsReseed()) {
                migrationService.reseed();
            } else {
                migrationService.clearSeededNotes();
                migrationService.syncLinksFromSheet();
            }
        };
    }
}
