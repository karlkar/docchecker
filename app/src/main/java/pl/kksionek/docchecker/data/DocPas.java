package pl.kksionek.docchecker.data;

public class DocPas extends Doc {

    private long mTimestamp;

    public DocPas() {
    }

    @Override
    public String getNumber() {
        return null;
    }

    @Override
    public String getReqStatus() {
        return null;
    }

    @Override
    public String getDocStatus() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }
}
