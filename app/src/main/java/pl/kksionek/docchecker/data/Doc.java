package pl.kksionek.docchecker.data;

public class Doc {
    public static final Doc EMPTY = new Doc();
    private String mReqNum = null;
    private Throwable mThrowable = null;
    Doc() {
    }

    private Doc(String reqNum, Throwable throwable) {
        mReqNum = reqNum;
        mThrowable = throwable;
    }

    public static Doc error(String reqNum, Throwable throwable) {
        return new Doc(reqNum, throwable);
    }

    public String getNumber() {
        return mReqNum;
    }

    public String getReqStatus() {
        return null;
    }

    public String getDocStatus() {
        return null;
    }

    public String getMessage() {
        return null;
    }

    public String getErrorMessage() {
        return null;
    }

    public boolean isNotEmpty() {
        return getNumber() != null || getThrowable() != null;
    }

    public long getTimestamp() {
        return 0;
    }

    public void setTimestamp(long timestamp) {
    }

    public Throwable getThrowable() {
        return mThrowable;
    }

    public boolean isReady() {
        return false;
    }
}
