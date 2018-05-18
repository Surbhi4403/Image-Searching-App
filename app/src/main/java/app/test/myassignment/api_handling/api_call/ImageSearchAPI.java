package app.test.myassignment.api_handling.api_call;

import android.content.Context;

import app.test.myassignment.R;
import app.test.myassignment.api_handling.pojo.ImageData;
import app.test.myassignment.api_handling.retrofit.APIInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* API Calling intermediate Handler */
public class ImageSearchAPI implements ImageApiInterface {

    Call<ImageData> call;

    @Override
    public void cancelRequest(Context mContext, ImageApiInterface.onApiFinishedListener listener) {
        if (call != null && !call.isCanceled()) {
            call.cancel();
            if (listener != null)
                listener.onApiFailure(mContext.getString(R.string.call_cancelled));
        }
    }

    @Override
    public void imageSearchAPI(Context mContext, String searchTerm, int offset, int count,
                               final ImageApiInterface.onApiFinishedListener listener, APIInterface api, Call<ImageData> callParam) {

        call = callParam;
        call = api.getListOfImages(searchTerm, offset + "", count + "");
        call.enqueue(new Callback<ImageData>() {
            @Override
            public void onResponse(Call<ImageData> call, Response<ImageData> response) {
                try {
                    if (response.body().get_type().equalsIgnoreCase(mContext.getString(R.string.got_response))) {
                        if (listener != null)
                            listener.onApiSuccess(response.body(), searchTerm);
                    } else {
                        if (listener != null)
                            listener.onApiFailure(mContext.getString(R.string.no_data));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null)
                        listener.onApiFailure(mContext.getString(R.string.response_error));
                }
            }

            @Override
            public void onFailure(Call<ImageData> call, Throwable t) {
                if (listener != null)
                    listener.onApiFailure(mContext.getString(R.string.api_failure));
            }
        });
    }
}