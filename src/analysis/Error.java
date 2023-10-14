package analysis;

import util.ErrorType;

public class Error {
    private final int lineNum;
    private final ErrorType errorType;

    public Error(int lineNum, ErrorType errorType) {
        this.lineNum = lineNum;
        this.errorType = errorType;
    }

    @Override
    public String toString() {
        return (lineNum + 1) + " " + errorType;
    }

    public int getLineNum() {
        return lineNum;
    }
}
