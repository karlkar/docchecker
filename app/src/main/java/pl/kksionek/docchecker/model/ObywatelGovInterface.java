package pl.kksionek.docchecker.model;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import pl.kksionek.docchecker.data.DocId;
import pl.kksionek.docchecker.data.DocPas;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ObywatelGovInterface {

    @POST("/esrvProxy/proxy/sprawdzStatusWniosku")
    Observable<DocId> getDocIdStatus(@Query("numerWniosku") String reqNum);

    // ?p_p_id=Gotowoscpaszportu_WAR_Gotowoscpaszportuportlet&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_cacheability=cacheLevelPage&p_p_col_id=column-5&p_p_col_count=1
    @FormUrlEncoded
    @POST("/dokumenty-i-dane-osobowe/sprawdz-czy-twoj-paszport-jest-gotowy?p_p_id=Gotowoscpaszportu_WAR_Gotowoscpaszportuportlet&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_cacheability=cacheLevelPage&p_p_col_id=column-5&p_p_col_count=1")
    Observable<DocPas> getDocPasStatus(
            @Field("_Gotowoscpaszportu_WAR_Gotowoscpaszportuportlet_nrSprawy") String reqNum,
            @Field("_Gotowoscpaszportu_WAR_Gotowoscpaszportuportlet_p_auth") String auth);

    class Factory {
        public static ObywatelGovInterface create() {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(httpLoggingInterceptor)
                    .build();
            return new Retrofit.Builder()
                    .baseUrl("https://obywatel.gov.pl")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
                    .create(ObywatelGovInterface.class);
        }
    }
}
