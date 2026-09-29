package com.awesome.awesomepizza.config;

import com.awesome.awesomepizza.domain.ChefStation;
import com.awesome.awesomepizza.repository.ChefStationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChefStationInitializer {
    @Bean
    CommandLineRunner initializeChefStation(ChefStationRepository repository) {
        return args -> {
            if (!repository.existsById(1L)) {
                try {
                    repository.saveAndFlush(new ChefStation(1L));
                } catch (org.springframework.dao.DataIntegrityViolationException exception) {
                    // Another instance may have inserted the same singleton row.
                    if (!repository.existsById(1L)) {
                        throw exception;
                    }
                }
            }
        };
    }
}
