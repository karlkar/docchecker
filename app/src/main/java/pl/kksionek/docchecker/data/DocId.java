package pl.kksionek.docchecker.data;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("FieldCanBeLocal")
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

    @Override
    public String getNumber() {
        return mReqNumber;
    }

    @Override
    public String getReqStatus() {
        switch (mReqStatus) {
            case "SPERSONALIZOWANY":
                return "Spersonalizowany";
        }
        Log.d(TAG, "getReqStatus: Unknown status " + mReqStatus);
        return mReqStatus;
    }

    @Override
    public String getDocStatus() {
        switch (mDocStatus) {
            case "WYSLANY":
                return "Wysłany";
            case "PRZYJETY_PRZEZ_URZAD":
                return "Przyjęty przez urząd";
            case "WYDANY":
                return "Wydany";
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

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
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

    @Override
    public boolean isReady() {
        if (mStatus == null || mStatus.mMessage == null) {
            return false;
        }
        return mStatus.mMessage.contains("jest gotowy do odbioru")
                || mStatus.mMessage.contains("został odebrany");
    }

    private class Status {

        @SerializedName("message")
        private String mMessage;

        @SerializedName("level")
        private String mLevel;
    }
}
