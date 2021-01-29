package heizi.heizi;

import retrofit2.Call;
import retrofit2.http.GET;

public interface HeiziRequest {

    @GET("/latest")
    public Call<DataSet> latest();

    @GET("/ping")
    public Call<String> ping();
}
