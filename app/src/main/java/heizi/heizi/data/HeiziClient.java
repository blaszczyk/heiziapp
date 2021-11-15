package heizi.heizi.data;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HeiziClient {

    private final HeiziRequest request;
    private final static String SERVICE_HOST = "heizi.fritz.box";

    private final static String PROTOCOL = "http://";
    private final static String PORT = ":4321";

    public HeiziClient() {
        this.request = createRequest(SERVICE_HOST, 3000);
    }

    private static HeiziRequest createRequest(final String host, final int connectionTimeout) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .build();
        return new Retrofit.Builder()
                .baseUrl(PROTOCOL + host + PORT)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(HeiziRequest.class);
    }

    public HeiziRequest request() {
        return request;
    }

}
