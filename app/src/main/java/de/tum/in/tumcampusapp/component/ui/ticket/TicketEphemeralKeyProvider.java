package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.stripe.android.EphemeralKeyProvider;
import com.stripe.android.EphemeralKeyUpdateListener;

import java.util.HashMap;

import de.tum.in.tumcampusapp.api.app.TumCabeClient;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketEphemeralKeyProvider implements EphemeralKeyProvider {

    private ProgressListener mProgressListener;
    private Context mContext;

    public TicketEphemeralKeyProvider(@NonNull ProgressListener progressListener,
                                      Context context) {
        mProgressListener = progressListener;
        mContext = context;
    }

    @Override
    public void createEphemeralKey(@NonNull @Size(min = 4) String apiVersion,
                                   @NonNull final EphemeralKeyUpdateListener keyUpdateListener) {
        TumCabeClient.getInstance(mContext).retrieveEphemeralKey(apiVersion,
                new Callback<HashMap<String, Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<HashMap<String, Object>> call,
                                           @NonNull Response<HashMap<String, Object>> response) {
                        HashMap<String, Object> responseBody = response.body();
                        if (responseBody != null) {
                            String id = responseBody.toString();
                            keyUpdateListener.onKeyUpdate(id);
                            mProgressListener.onStringResponse(id);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<HashMap<String, Object>> call,
                                          @NonNull Throwable t) {
                        Utils.log(t);
                    }
                });
    }

    public interface ProgressListener {
        void onStringResponse(String string);
    }

}
