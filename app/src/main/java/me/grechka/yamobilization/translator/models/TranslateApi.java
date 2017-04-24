package me.grechka.yamobilization.translator.models;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TranslateApi {

    @GET("api/v1.5/tr.json/translate")
    Call<ResultFromHttp> getResult(@Query("key") String key,
                                   @Query("text") String text,
                                   @Query("lang") String lang);
}
