package org.example.riskwarningsystembackend.config;

import org.example.riskwarningsystembackend.service.CompanyRelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class CompanyRelationDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CompanyRelationDataLoader.class);

    private final CompanyRelationService companyRelationService;

    public CompanyRelationDataLoader(CompanyRelationService companyRelationService) {
        this.companyRelationService = companyRelationService;
    }

    @Override
    public void run(String... args) {
        try {
            logger.info("Application startup: Triggering initial company relation build...");
            companyRelationService.rebuildCompanyRelations();
        } catch (Exception e) {
            logger.error("Initial company relation build failed on startup.", e);
        }
    }
}
