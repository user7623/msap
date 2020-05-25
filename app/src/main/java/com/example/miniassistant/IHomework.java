package com.example.miniassistant;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IHomework {
//TODO: smeni url zavrsetok
    @GET("getJobs/emulator")
    Call<List<Homework>> getHomeworks();
}
