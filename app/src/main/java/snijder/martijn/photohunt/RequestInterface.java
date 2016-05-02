package snijder.martijn.photohunt;

import snijder.martijn.photohunt.models.ServerRequest;
import snijder.martijn.photohunt.models.ServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RequestInterface {

    @POST("login-register/")
    Call<ServerResponse> operation(@Body ServerRequest request);

}
