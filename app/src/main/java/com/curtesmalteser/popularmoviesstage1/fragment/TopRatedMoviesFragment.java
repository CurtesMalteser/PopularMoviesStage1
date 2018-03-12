package com.curtesmalteser.popularmoviesstage1.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.curtesmalteser.popularmoviesstage1.BuildConfig;
import com.curtesmalteser.popularmoviesstage1.R;
import com.curtesmalteser.popularmoviesstage1.activity.MovieDetailsActivity;
import com.curtesmalteser.popularmoviesstage1.adapter.EndlessScrollListener;
import com.curtesmalteser.popularmoviesstage1.adapter.MoviesAdapter;
import com.curtesmalteser.popularmoviesstage1.utils.MoviesAPIClient;
import com.curtesmalteser.popularmoviesstage1.utils.MoviesAPIInterface;
import com.curtesmalteser.popularmoviesstage1.utils.MoviesModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopRatedMoviesFragment extends Fragment
        implements MoviesAdapter.ListItemClickListener {

    private static final String TAG = TopRatedMoviesFragment.class.getSimpleName();


    private static final String SAVED_STATE_MOVIES_LIST = "moviesListSaved";

    private ArrayList<MoviesModel> mMoviesList = new ArrayList<>();

    private Parcelable stateRecyclerView;

    private static final String PAGE_NUMBER_KEY = "pageNumber";

    private int pageNumber = 1;

    private ConnectivityManager cm;

    private MoviesAdapter moviesAdapter;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    public TopRatedMoviesFragment() {
        // Required empty public constructor
    }

    public static TopRatedMoviesFragment newInstance() {
        return new TopRatedMoviesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies_layout, container, false);
        ButterKnife.bind(this, view);

        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.number_of_columns));
        mRecyclerView.setLayoutManager(layoutManager);
        if (savedInstanceState == null) {
            makeMoviesQuery(pageNumber);
        } else {
            mMoviesList = savedInstanceState.getParcelableArrayList(SAVED_STATE_MOVIES_LIST);
            pageNumber = savedInstanceState.getInt(PAGE_NUMBER_KEY);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(stateRecyclerView);

        }
        moviesAdapter = new MoviesAdapter(getContext(), mMoviesList, TopRatedMoviesFragment.this);
        mRecyclerView.setAdapter(moviesAdapter);
        mRecyclerView.addOnScrollListener(new EndlessScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                makeMoviesQuery(current_page);
                pageNumber = current_page;
            }
        });

        return view;
    }

    private void makeMoviesQuery(int page) {
        MoviesAPIInterface apiInterface = MoviesAPIClient.getClient().create(MoviesAPIInterface.class);
        Call<MoviesModel> call;

        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {

            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("api_key", BuildConfig.API_KEY);
            queryParams.put("language", "en-US");
            queryParams.put("page", String.valueOf(page));

            call = apiInterface.getTopRated(queryParams);
            call.enqueue(new Callback<MoviesModel>() {
                @Override
                public void onResponse(@NonNull Call<MoviesModel> call, @NonNull Response<MoviesModel> response) {

                    for (MoviesModel moviesModel : response.body().getMoviesModels()) {
                        mMoviesList.add(moviesModel);
                        moviesAdapter.notifyDataSetChanged();
                    }
                    Log.d("LOL", "onResponse: " + page);
                }

                @Override
                public void onFailure(@NonNull Call<MoviesModel> call, @NonNull Throwable t) {
                    Log.d(TAG, "onFailure:" + t.getMessage());
                }
            });
        } else
            Toast.makeText(getContext(), R.string.check_internet_connection, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onListItemClick(MoviesModel moviesModel) {
        Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
        intent.putExtra(getResources().getString(R.string.string_extra), moviesModel);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        stateRecyclerView = mRecyclerView.getLayoutManager().onSaveInstanceState();
        outState.putInt(PAGE_NUMBER_KEY, pageNumber);
        outState.putParcelableArrayList(SAVED_STATE_MOVIES_LIST, mMoviesList);
    }
}
