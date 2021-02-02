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

    private final static String PROTOCOL = "http://";
    private final static String PORT = ":4321";
    private final static int LAST_BYTE_BEGIN = 101;
    private final static int LAST_BYTE_END = 200;



    public HeiziClient(final String serviceHost) {
        this.request = createRequest(serviceHost, 3000);
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

    public void locateService(String baseIp, HostNameConsumer hostConsumer) {
        pingIp(baseIp, LAST_BYTE_BEGIN, hostConsumer);
    }

    private void pingIp(final String baseIp, final int lastByte, final HostNameConsumer hostConsumer) {
        final String ip = baseIp + lastByte;
        hostConsumer.message("ping " + ip);
        createRequest(ip, 1000).ping().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if("pong".equals(response.body())){
                    hostConsumer.consume(ip);
                }
                else {
                    onFailure(call, null);
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (lastByte == LAST_BYTE_END) {
                    hostConsumer.fail();
                }
                else {
                    pingIp(baseIp, lastByte + 1, hostConsumer);
                }
            }
        });
    }

    public static interface HostNameConsumer {
        public void consume(final String host);
        public void fail();
        public void message(final String message);
    }

}
