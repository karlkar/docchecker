package pl.kksionek.docchecker.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.ArraySet;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputFilter;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pl.kksionek.docchecker.DocApplication;
import pl.kksionek.docchecker.R;
import pl.kksionek.docchecker.data.Doc;
import pl.kksionek.docchecker.model.DocRecyclerAdapter;

public class MainActivity extends AppCompatActivity {

    public static final String PREF_ID_REQUESTS = "ID_REQUESTS";
    public static final String PREF_PAS_REQUESTS = "PAS_REQUESTS";
    public static final int LENGTH_OF_ID_CASE_NUM = 23;
    public static final int LENGTH_OF_PAS_CASE_NUM = -1; // passports currently not enabled
    private static final String TAG = "MainActivity";
    private DocRecyclerAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SharedPreferences mSharedPreferences;
    private Gson mGson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGson = new Gson();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activity_main_recycler);

        mSwipeRefreshLayout.setOnRefreshListener(() -> getData(false));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_main_fab);
        fab.setOnClickListener(v -> {
            final EditText editText = new EditText(MainActivity.this);
            editText.setSingleLine();
            editText.setMaxLines(1);
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(23)});
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.activity_main_alert_input_number)
                    .setView(editText)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        addNewRequest(editText.getText().toString());
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        mAdapter = new DocRecyclerAdapter();
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView,
                    RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                    RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String reqNum = mAdapter.remove(viewHolder.getAdapterPosition());
                if (reqNum != null) {
                    removeDocWithReqNum(reqNum);
                }
            }
        });
        helper.attachToRecyclerView(recyclerView);

        mSwipeRefreshLayout.setRefreshing(true);
        getData(true);
    }

    private void addNewRequest(String reqNum) {
        if (reqNum.length() == LENGTH_OF_ID_CASE_NUM) {
            addNewRequestInner(PREF_ID_REQUESTS, reqNum);
        } else if (reqNum.length() == LENGTH_OF_PAS_CASE_NUM) {
            addNewRequestInner(PREF_PAS_REQUESTS, reqNum);
        } else {
            Toast.makeText(MainActivity.this,
                    getString(R.string.activity_main_request_number_incorrect, reqNum),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        getData(false);
    }

    private void addNewRequestInner(String docPref, String reqNum) {
        Set<String> newSet = new ArraySet<>();
        Set<String> requests = mSharedPreferences.getStringSet(docPref, newSet);
        for (String str : requests) {
            newSet.add(str);
        }
        newSet.add(reqNum);
        mSharedPreferences.edit()
                .putStringSet(docPref, newSet)
                .apply();
    }

    private void removeDocWithReqNum(String reqNum) {
        if (reqNum.length() == LENGTH_OF_ID_CASE_NUM) {
            removeDocWithReqNum(PREF_ID_REQUESTS, reqNum);
        } else if (reqNum.length() == LENGTH_OF_PAS_CASE_NUM) {
            removeDocWithReqNum(PREF_PAS_REQUESTS, reqNum);
        }
    }

    private void removeDocWithReqNum(String docPref, String reqNum) {
        Set<String> newSet = new ArraySet<>();
        Set<String> requests = mSharedPreferences.getStringSet(docPref, new ArraySet<>());
        for (String str : requests) {
            if (!str.equals(reqNum)) {
                newSet.add(str);
            }
        }

        mSharedPreferences.edit()
                .putStringSet(docPref, newSet).remove(reqNum)
                .apply();
    }

    private void saveDocToCache(Doc document) {
        if (document.getThrowable() == null) {
            mSharedPreferences.edit()
                    .putString(document.getNumber(), mGson.toJson(document))
                    .apply();
        }
    }

    private Observable<String> getRequests(String docPref) {
        Log.d(TAG, "getRequests: " + Thread.currentThread().getName());
        return Observable.fromCallable(
                () -> mSharedPreferences.getStringSet(docPref, new ArraySet<>()))
                .filter(strings -> strings.size() > 0)
                .flatMapIterable(strings -> strings);
    }

    private Observable<Doc> getDocStatusFromCache(String reqNum) {
        Log.d(TAG, "getIdStatusFromCache: " + Thread.currentThread().getName());
        return Observable.fromCallable(() -> {
            String str = mSharedPreferences.getString(reqNum, null);
            if (str == null) {
                return Doc.EMPTY;
            }
            return mGson.fromJson(str, Doc.class);
        });
    }

    private Observable<Doc> getIdStatusFromNetwork(String reqNum) {
        Log.d(TAG, "getIdStatusFromNetwork: " + Thread.currentThread().getName());
        return DocApplication.getObywatelGovInterface().getDocIdStatus(reqNum)
                .cast(Doc.class)
                .onErrorResumeNext(throwable -> {
                    return Observable.just(Doc.error(reqNum, throwable));
                });
    }

    private Observable<Doc> getPasStatusFromNetwork(String reqNum) {
        Log.d(TAG, "getPasStatusFromNetwork: " + Thread.currentThread().getName());
        return DocApplication.getObywatelGovInterface().getDocPasStatus(reqNum, null)
                .cast(Doc.class)
                .onErrorResumeNext(throwable -> {
                    return Observable.just(Doc.error(reqNum, throwable));
                });
    }

    private Observable<Doc> getIdData(boolean useCache) {
        Log.d(TAG, "getIdData: " + Thread.currentThread().getName());
        return getRequests(PREF_ID_REQUESTS)
                .flatMap(s -> Observable.concat(
                        useCache ? getDocStatusFromCache(s) : Observable.empty(),
                        getIdStatusFromNetwork(s))
                        .subscribeOn(Schedulers.io()));
    }

    private Observable<Doc> getPasData(boolean useCache) {
        Log.d(TAG, "getPasData: " + Thread.currentThread().getName());
        return getRequests(PREF_PAS_REQUESTS)
                .flatMap(s -> Observable.concat(
                        useCache ? getDocStatusFromCache(s) : Observable.empty(),
                        getPasStatusFromNetwork(s))
                        .subscribeOn(Schedulers.io()));
    }

    private void getData(boolean useCache) {
        Log.d(TAG, "getData: " + Thread.currentThread().getName());
        Observable.merge(
                getIdData(useCache),
                getPasData(useCache))
                .filter(Doc::isNotEmpty)
                .doOnNext(doc -> doc.setTimestamp(System.currentTimeMillis()))
                .doOnNext(this::saveDocToCache)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleDocument,
                        throwable -> {
                            throwable.printStackTrace();
                            mSwipeRefreshLayout.setRefreshing(false);
                        },
                        () -> mSwipeRefreshLayout.setRefreshing(false)
                );
        // "1465038/2017/9847487/01"
    }

    private void handleDocument(Doc doc) {
        String reqNum = doc.getNumber();
        if (doc.getErrorMessage() != null) {
            Toast.makeText(
                    MainActivity.this,
                    "[" + reqNum + "] " + doc.getErrorMessage(),
                    Toast.LENGTH_LONG).show();
            FirebaseCrash.log("Error returned from server: " + doc.getErrorMessage());
            removeDocWithReqNum(doc.getNumber());
            return;
        }
        Throwable throwable = doc.getThrowable();
        if (throwable != null) {
            if (throwable instanceof HttpException) {
                HttpException ex = (HttpException) throwable;
                if (ex.code() == 500) {
                    Toast.makeText(
                            MainActivity.this,
                            getString(R.string.activity_main_request_number_incorrect, reqNum),
                            Toast.LENGTH_SHORT).show();
                    removeDocWithReqNum(reqNum);
                } else {
                    FirebaseCrash.log("HttpException took place.");
                    FirebaseCrash.report(throwable);
                    Log.e(TAG, "getIdStatusFromNetwork: Req num [" + reqNum
                            + "] caused HttpException [" + ex.code() + "] " + ex.getMessage());
                }
                return;
            } else if (throwable instanceof UnknownHostException) {
                Toast.makeText(
                        MainActivity.this,
                        R.string.activity_main_no_internet,
                        Toast.LENGTH_SHORT).show();
                return;
            } else if (throwable instanceof IOException) {
                // network problem
                FirebaseCrash.log("IOException took place.");
                FirebaseCrash.report(throwable);
            } else {
                FirebaseCrash.log("Exception took place.");
                FirebaseCrash.report(throwable);
            }
            Log.d(TAG, "getIdStatusFromNetwork: Req num [" + reqNum + "] failed with error "
                    + throwable.getMessage());
            return;
        }
        mAdapter.update(doc);
    }
}
