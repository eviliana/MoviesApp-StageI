package com.project1.eviliana.popularmoviesapp;

import static com.project1.eviliana.popularmoviesapp.utils.JsonDataParser.getMovieDataFromJSON;
import com.project1.eviliana.popularmoviesapp.adapter.MovieRecyclerAdapter;
import com.project1.eviliana.popularmoviesapp.model.Movie;
import com.project1.eviliana.popularmoviesapp.utils.*;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.json.JSONException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MovieRecyclerAdapter.MoviePosterClickListener{

    ArrayList<Movie> moviesList;
    private MovieRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    protected final static String MOVIE_ITEM = "movie_item";
    private Context context = MainActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moviesList = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.posters_recycler);

        int numberOfColumns = 2; //2 columns for the GridLayoutManager
        GridLayoutManager layoutManager = new GridLayoutManager(context, numberOfColumns);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
    }

    /**
     * The reason i don't store hasNetworkAcces results in a var
     * is mainly because in this way, i always get real time connectivity check :P
     */
    @Override
    protected void onResume() {
        if(moviesList != null){
            populateAdapter();
        }
        if (moviesList.size() == 0){
            if (NetworkUtils.hasNetworkAcces(this)){
                loadPosters();
            } else {
                Toast.makeText(context,"Internet connection failed, please try again",Toast.LENGTH_SHORT).show();
            }
        }
        super.onResume();
    }
    private void loadPosters() {
        fetchMovieData("popular");
    }

    /**
     * This is a workaround to help me pass click listener in onPostExecute :)
     */
    private void populateAdapter(){
        mAdapter = new MovieRecyclerAdapter(context, moviesList, this);
        mRecyclerView.setAdapter(mAdapter);
    }
    /**
     * Get menu selection and build the url for the http call
     * Also, this where we pass user's api key for tmdb
     * We call AsyncTask to move our http call from the main thread
     * @param qParam - popular or top_rated for the api call
     */
    private void fetchMovieData(String qParam) {
        String apiKey = getString(R.string.myApiKey);
        URL movieQueryUrl = Queries.buildMovieUrl(qParam, apiKey);
        AsyncTaskCall at = new AsyncTaskCall();
        at.execute(movieQueryUrl);
    }

    /**
     * We inflate our options menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_popular).setChecked(true);
        return true;
    }

    /**
     * Choose a menu action and pass the parameter for the query
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_popular:
               if (NetworkUtils.hasNetworkAcces(this)){
                    fetchMovieData("popular");
                   if(!item.isChecked()){
                       item.setChecked(true);
                   }
                } else {
                    Toast.makeText(context,"Internet connection failed, please try again",Toast.LENGTH_SHORT).show();
                    //mRecyclerView.setAdapter(null); in case we want to clear the images from previous call
                }
                break;
            case R.id.action_topRated:
                if (NetworkUtils.hasNetworkAcces(this)){
                    fetchMovieData("top_rated");
                    if(!item.isChecked()){
                        item.setChecked(true);
                    }
                } else {
                    Toast.makeText(context,"Internet connection failed, please try again",Toast.LENGTH_SHORT).show();
                    //mRecyclerView.setAdapter(null); in case we want to clear the images from previous call
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMoviePosterClick(int clickedPosterId) {
        Class destinationClass = DetailsActivity.class;
        Intent intentToStartDetailsActivity = new Intent(context, destinationClass);
        intentToStartDetailsActivity.putExtra(MOVIE_ITEM, moviesList.get(clickedPosterId));
        startActivity(intentToStartDetailsActivity);
    }

    /**
     * The AsyncTaskCall handles the http call in a background thread
     * and calls getMovieDataFromJSON which parses the JSON data into
     * movie objects
     */
    public class AsyncTaskCall extends AsyncTask<URL, Void, ArrayList<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Toast.makeText(context, "The data is downloading", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected ArrayList<Movie> doInBackground(URL... urls) {
            URL tmdbURL = urls[0];
            String jsonResults = null;
            try {
                jsonResults = NetworkUtils.getResponseFromHttpUrl(tmdbURL);
                if (jsonResults != null) {
                    moviesList = getMovieDataFromJSON(jsonResults);
                }
                    return moviesList;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         *Get the movieList and popoulate the adapter
         * @param moviesList
         */
        @Override
        protected void onPostExecute(ArrayList<Movie> moviesList) {
            if (moviesList != null && !moviesList.equals("")) {
                populateAdapter();
            }
        }
    }
}