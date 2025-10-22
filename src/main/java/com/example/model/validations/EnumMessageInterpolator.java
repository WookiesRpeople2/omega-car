package com.example.model.validations;

import jakarta.validation.MessageInterpolator;
import java.util.Locale;

import com.example.Errors;

public class EnumMessageInterpolator implements MessageInterpolator {

    private final MessageInterpolator defaultInterpolator;

    public EnumMessageInterpolator(MessageInterpolator defaultInterpolator) {
        this.defaultInterpolator = defaultInterpolator;
    }

    @Override
    public String interpolate(String messageTemplate, Context context) {
        return interpolate(messageTemplate, context, Locale.getDefault());
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        String key = messageTemplate.replaceAll("[{}]", "");
        for (Errors e : Errors.values()) {
            if (e.name().equals(key)) {
                return e.getMessage();
            }
        }
        return defaultInterpolator.interpolate(messageTemplate, context, locale);
    }
}

