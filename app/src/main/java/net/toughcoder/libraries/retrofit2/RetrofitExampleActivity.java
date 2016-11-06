package net.toughcoder.libraries.retrofit2;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by alexhilton on 16/11/3.
 */

public class RetrofitExampleActivity extends Activity {
    private static final String TAG = "Retrofit example";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
    }

    interface GitHubService {
//        @GET("users/{user}/repos")
//        Call<List<Repo>> listRepos(@Path("user") String user);
    }
}
