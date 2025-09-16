package com.project_management.config;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DroolsConfig {

    private static final String RULES_DRL_PATH = "rules/validation/task-validation.drl";

    @Bean
    public KieContainer kieContainer() {
        KieServices kieServices = KieServices.Factory.get();

        try {
            KieContainer kieContainer = kieServices.getKieClasspathContainer();

            if (kieContainer.getKieBase("validationKB") != null) {
                log.info("Successfully loaded KieContainer from classpath");
                return kieContainer;
            }
        } catch (Exception e) {
            log.warn("Failed to load from classpath, building manually: {}", e.getMessage());
        }

        return buildKieContainerManually(kieServices);
    }

    private KieContainer buildKieContainerManually(KieServices kieServices) {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_DRL_PATH));

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kieBuilder.getResults().toString());
        }

        KieModule kieModule = kieBuilder.getKieModule();
        KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());

        log.info("Successfully built KieContainer manually");
        return kieContainer;
    }

    @Bean
    public StatelessKieSession statelessKieSession(KieContainer kieContainer) {
        try {
            StatelessKieSession session = kieContainer.newStatelessKieSession("validationSession");
            if (session != null) {
                log.info("Successfully created StatelessKieSession: validationSession");
                return session;
            }
        } catch (Exception e) {
            log.error("Failed to create named session, trying default: {}", e.getMessage());
        }

        try {
            StatelessKieSession defaultSession = kieContainer.newStatelessKieSession();
            if (defaultSession != null) {
                log.info("Successfully created default StatelessKieSession");
                return defaultSession;
            }
        } catch (Exception e) {
            log.error("Failed to create default session: {}", e.getMessage());
        }

        throw new RuntimeException("Unable to create StatelessKieSession. Check kmodule.xml and DRL files.");
    }
}
