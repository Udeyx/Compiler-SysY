package analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ErrorRecorder {
    private static final ErrorRecorder ERROR_RECORDER = new ErrorRecorder();
    private final ArrayList<Error> errors;

    private ErrorRecorder() {
        this.errors = new ArrayList<>();
    }

    public static ErrorRecorder getInstance() {
        return ERROR_RECORDER;
    }

    @Override
    public String toString() {
        errors.sort(Comparator.comparingInt(Error::lineNum));
        return errors.stream().map(Error::toString).collect(Collectors.joining("\n"));
    }
}
