package com.dev.mscuentas.presentation.request;

import com.dev.mscuentas.domain.exception.DomainValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Value of the {@code fecha} query parameter: either a range {@code yyyy-MM-dd,yyyy-MM-dd}
 * or a single day {@code yyyy-MM-dd}, which is treated as a range of one day.
 */
public record DateRange(LocalDate from, LocalDate to) {

    private static final String FORMAT_HELP =
            "El parámetro fecha debe tener el formato yyyy-MM-dd o un rango yyyy-MM-dd,yyyy-MM-dd";

    public static DateRange parse(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(FORMAT_HELP);
        }
        String[] parts = value.split(",", -1);
        if (parts.length > 2) {
            throw new DomainValidationException(FORMAT_HELP);
        }
        try {
            LocalDate from = LocalDate.parse(parts[0].trim());
            LocalDate to = parts.length == 2 ? LocalDate.parse(parts[1].trim()) : from;
            return new DateRange(from, to);
        } catch (DateTimeParseException ex) {
            throw new DomainValidationException(FORMAT_HELP);
        }
    }
}
