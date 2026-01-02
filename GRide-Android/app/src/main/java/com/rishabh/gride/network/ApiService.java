package com.rishabh.gride.network;

import com.rishabh.gride.models.Ride;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;


public interface ApiService {

    @POST("api/auth/login")
    Call<Map<String, Object>> login(@Body Map<String, String> body);

    @POST("api/rides/create")
    Call<Map<String, Object>> createRide(
            @Header("authorization") String token,
            @Body Map<String, Object> body
    );

    @GET("api/rides/my")
    Call<List<Ride>> getMyRides(@Header("authorization") String token);

    @PATCH("api/rides/{id}/status")
    Call<Map<String, String>> updateRideStatus(
            @Header("authorization") String token,
            @Path("id") int rideId,
            @Body Map<String, String> body
    );

    @POST("api/auth/register")
    Call<Map<String, String>> register(@Body Map<String, String> body);

    @GET("api/rides/available")
    Call<List<Map<String, Object>>> getAvailableRides(
            @Header("authorization") String token
    );

    @PATCH("api/rides/{id}/accept")
    Call<Map<String, String>> acceptRide(
            @Header("authorization") String token,
            @Path("id") int rideId
    );

}
