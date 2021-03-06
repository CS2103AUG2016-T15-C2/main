package seedu.scheduler.model.entry;

import seedu.scheduler.commons.exceptions.IllegalValueException;

/**
 * Represents a Entry's startTime number in the scheduler.
 * Guarantees: immutable; is valid as declared in {@link #isValidStartTime(String)}
 */
public class StartTime {

    public static final String MESSAGE_START_TIME_CONSTRAINTS = "Item's start time should be in the 24-Hr format hh-mm in numbers separated by a ':'";
    public static final String START_TIME_VALIDATION_REGEX = "(([01]?[0-9]|2[0-3]):[0-5][0-9]|empty)";

    public final String value;

    /**
     * Validates given startTime number.
     *
     * @throws IllegalValueException if given startTime string is invalid.
     */
    public StartTime(String startTime) throws IllegalValueException {
        if (startTime==null) {
            startTime = "empty";
        }
        startTime = startTime.trim();
        if (!isValidStartTime(startTime)) {
            throw new IllegalValueException(MESSAGE_START_TIME_CONSTRAINTS);
        }
        this.value = startTime;
    }

    /**
     * Returns true if a given string is a valid entry startTime number.
     */
    public static boolean isValidStartTime(String test) {
        return test.matches(START_TIME_VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof StartTime // instanceof handles nulls
                && this.value.equals(((StartTime) other).value)); // state check
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
