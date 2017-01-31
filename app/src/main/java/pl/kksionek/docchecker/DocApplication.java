package pl.kksionek.docchecker;

import android.app.Application;

import pl.kksionek.docchecker.model.ObywatelGovInterface;

public class DocApplication extends Application {

    private static ObywatelGovInterface mObywatelGovInterface = null;

    public static ObywatelGovInterface getObywatelGovInterface() {
        return mObywatelGovInterface;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mObywatelGovInterface = ObywatelGovInterface.Factory.create();
    }

}
