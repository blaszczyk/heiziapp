package heizi.heizi.data;

import java.util.Map;

import heizi.heizi.data.DataSet;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HeiziRequest {

    @GET("/latest")
    public Call<DataSet> latest();

    @GET("/range")
    public Call<DataRange> range(@Query("mintime") long mintime, @Query("maxtime") long maxtime);

    @GET("/ping")
    public Call<String> ping();
}
