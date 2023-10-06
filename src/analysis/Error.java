package analysis;

public record Error(int lineNum, ErrorType type) {
    @Override
    public String toString() {
        return lineNum + " " + type;
    }
}
