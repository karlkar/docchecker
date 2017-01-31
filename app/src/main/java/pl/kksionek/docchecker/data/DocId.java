package pl.kksionek.docchecker.data;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class DocId extends Doc {

    private static final String TAG = "DocId";

    @SerializedName("numerWniosku")
    private String mReqNumber;

    @SerializedName("statusWniosku")
    private String mReqStatus;

    @SerializedName("statusDowodu")
    private String mDocStatus;

    @SerializedName("additionalInformation")
    private String mAdditionalInfo;

    @SerializedName("status")
    private Status mStatus;

    private long mTimestamp;

    public DocId() {
        mReqNumber = null;
        mReqStatus = null;
        mDocStatus = null;
        mAdditionalInfo = null;
        mStatus = null;
        mTimestamp = 0;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    @Override
    public String getNumber() {
        return mReqNumber;
    }

    @Override
    public String getReqStatus() {
        return mReqStatus;
    }

    @Override
    public String getDocStatus() {
        switch (mDocStatus) {
            case "WYSLANY":
                return "Wysłany";
            case "PRZYJETY_PRZEZ_URZAD":
                return "Przyjęty przez urząd";
            default:
                Log.d(TAG, "getDocStatus: Unknown status " + mDocStatus);
                return mDocStatus;
        }
    }

    @Nullable
    @Override
    public String getMessage() {
        if (mStatus != null) {
            return mStatus.mMessage.replace("&#160;", " ").replace("<br/>", "\n");
        }
        return null;
    }

    @Override
    public long getTimestamp() {
        return mTimestamp;
    }

    @Nullable
    public String getErrorMessage() {
        if (isError()) {
            return getMessage();
        }
        return null;
    }

    private boolean isError() {
        return mDocStatus == null || mReqStatus == null;
    }

    private class Status {

        @SerializedName("message")
        private String mMessage;

        @SerializedName("level")
        private String mLevel;
    }
}
