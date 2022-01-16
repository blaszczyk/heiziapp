package heizi.heizi.data;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HeiziClient {

    private final HeiziRequest request;
    private final static String SERVICE_URL = "http://heizi.fritz.box:4321";
    private final static long TIMEOUT_MILLIS = 3000L;

    public HeiziClient() {
        this.request = createRequest();
    }

    private static HeiziRequest createRequest() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .build();
        return new Retrofit.Builder()
                .baseUrl(SERVICE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(HeiziRequest.class);
    }

    public HeiziRequest request() {
        return request;
    }

}
