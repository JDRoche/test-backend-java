package com.dev.mscuentas.presentation.request;

import com.dev.mscuentas.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateRangeTest {

    @Test
    @DisplayName("Parses the range of the statement example: 2022-02-08,2022-02-10")
    void parsesRange() {
        DateRange range = DateRange.parse("2022-02-08,2022-02-10");

        assertEquals(LocalDate.of(2022, 2, 8), range.from());
        assertEquals(LocalDate.of(2022, 2, 10), range.to());
    }

    @Test
    @DisplayName("A single date is a range of one day")
    void parsesSingleDay() {
        DateRange range = DateRange.parse("2022-02-10");

        assertEquals(range.from(), range.to());
    }

    @Test
    @DisplayName("Spaces around the dates are tolerated")
    void toleratesSpaces() {
        assertEquals(LocalDate.of(2022, 2, 10), DateRange.parse(" 2022-02-08 , 2022-02-10 ").to());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"10/2/2022", "2022-02-08,", "2022-02-08,2022-02-09,2022-02-10", "2022-13-01", "abc"})
    @DisplayName("Anything that is not a date or a range of dates is rejected")
    void rejectsInvalidValues(String value) {
        assertThrows(DomainValidationException.class, () -> DateRange.parse(value));
    }
}
