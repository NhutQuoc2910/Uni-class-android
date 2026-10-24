package com.ppquang06.uniclassapp.data.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ppquang06.uniclassapp.utils.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static volatile Retrofit retrofit = null;
    private static AuthApiService authApiService = null;
    private static UserClassApiService userClassApiService = null;

    private ApiClient() {}

    public static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .addInterceptor(new AuthInterceptor(context))
                            .addInterceptor(loggingInterceptor)
                            .build();

                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(Constants.BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static synchronized AuthApiService getAuthApiService(Context context) {
        if (authApiService == null) {
            authApiService = getRetrofit(context).create(AuthApiService.class);
        }
        return authApiService;
    }

    public static synchronized UserClassApiService getUserClassApiService(Context context) {
        if (userClassApiService == null) {
            userClassApiService = getRetrofit(context).create(UserClassApiService.class);
        }
        return userClassApiService;
    }
}
