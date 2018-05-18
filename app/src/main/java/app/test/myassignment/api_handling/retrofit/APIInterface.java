package app.test.myassignment.api_handling.retrofit;

import app.test.myassignment.CommonFunctions;
import app.test.myassignment.api_handling.pojo.ImageData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("search?mkt=en-in&safeSearch=Strict")
    @Headers("Ocp-Apim-Subscription-Key:"+ CommonFunctions.BingKey)
    Call<ImageData> getListOfImages(@Query("q") String searchTerm, @Query("offset") String offset, @Query("count") String count);

}
