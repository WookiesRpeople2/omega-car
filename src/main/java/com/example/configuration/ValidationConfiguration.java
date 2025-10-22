package com.example.configuration;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.model.validations.EnumMessageInterpolator;

@Configuration
public class ValidationConfiguration {

    @Bean
    public Validator validator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.usingContext()
            .messageInterpolator(new EnumMessageInterpolator(factory.getMessageInterpolator()))
            .getValidator();
    }
}

