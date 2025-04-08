package com.example.watchshop.network;

import com.example.watchshop.model.Watch;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("watches")
    Call<List<Watch>> getWatches();
}