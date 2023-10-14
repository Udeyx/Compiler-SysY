package analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class Handler {
    private static final Handler HANDLER = new Handler();
    private final ArrayList<Error> errors;

    private Handler() {
        this.errors = new ArrayList<>();
    }

    public static Handler getInstance() {
        return HANDLER;
    }

    public void recordError(Error error) {
        errors.add(error);
    }

    @Override
    public String toString() {
        errors.sort(Comparator.comparingInt(Error::getLineNum));
        return errors.stream().map(Error::toString).collect(Collectors.joining("\n"));
    }
}
