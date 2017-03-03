package com.example.ojboba.newsapp;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Word>> {

private static final String GUARDIAN_API = "http://content.guardianapis.com/search?q=";
private String key = "&api-key=00cb316b-b7c5-42be-b7c7-7ba5a24aa2d6";
private static final String LOG_TAG = MainActivity.class.getName();
private TextView mEmptyStateTextView;
private String urlQuery = "";
private ListView listView;
private WordAdapter mAdapter;
private List savedNews;
    private static final int NEWS_LOADER_ID = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize listView
        listView = (ListView) findViewById(R.id.list);
        // Create a new adapter that takes an empty list of Word as input
        mAdapter = new WordAdapter(this, new ArrayList<Word>());
        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        listView.setAdapter(mAdapter);

        // USED TO INITIALIZE THE ERROR TEXT VIEW WHEN NO DATA OCCURS
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        listView.setEmptyView(mEmptyStateTextView);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // Put in text to tell user to search by clicking search icon on top right
        mEmptyStateTextView.setText(R.string.search_me);
        // Initialize and removes loading indicator since nothing is loading yet
        View loadingIndicator = findViewById(R.id.loading_indicator);
//        loadingIndicator.setVisibility(View.GONE);

        if(urlQuery != "") {
            // If there is a network connection, fetch data
            if (networkInfo != null && networkInfo.isConnected()) {
                mAdapter.clear();
                // Get a reference to the LoaderManager, in order to interact with loaders.
                LoaderManager loaderManager = getLoaderManager();

                // Initialize the loader. Pass in the int ID constant defined above and pass in null for
                // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
                // because this activity implements the LoaderCallbacks interface).
                loaderManager.initLoader(NEWS_LOADER_ID, null, this);
            } else {
                // Otherwise, display error
                // First, hide loading indicator so error message will be visible
                View LoadingIndicator = findViewById(R.id.loading_indicator);
                loadingIndicator.setVisibility(View.GONE);
            }
        }else{
            loadingIndicator.setVisibility(View.GONE);
        }

// -------------------------------------------------------------------------------------------------
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    // Find the current earthquake that was clicked on
                    Word currentNews = mAdapter.getItem(position);

                    // Convert the String URL into a URI object (to pass into the Intent constructor)
                    Uri newsUri = Uri.parse(currentNews.getmUrl());

                    // Create a new intent to view the earthquake URI
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                    // Send the intent to launch a new activity
                    startActivity(websiteIntent);
                }
            });

    }
    // ----------------------onSaveInstanceState & onRestoreInstanceState-------------------------------
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mAdapter.clear();
            savedNews = savedInstanceState.getParcelableArrayList("books");
            mAdapter.addAll(savedNews);
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outstate){
        outstate.putParcelableArrayList("books", (ArrayList<? extends Parcelable>) savedNews);
        super.onSaveInstanceState(outstate);
    }
// -----------------USED TO INITIALIZE AND CREATE THE SEARCH IMAGE AND FUNCTIONS--------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Full manual mode, can get callbacks and search to know exactly what happens
        // OnQueryTextListener occurs once the user submits a search
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        // This does not need to do anything since the user isn't really trying to get existing data
                        // Returning false is good so there is no change
                        return false;
                    }

                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        // Get a reference to the ConnectivityManager to check state of network connectivity
                        ConnectivityManager connMgr = (ConnectivityManager)
                                getSystemService(Context.CONNECTIVITY_SERVICE);

                        // Get details on the currently active default data network
                        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                        // If there is a network connection, fetch data
                        if (networkInfo != null && networkInfo.isConnected()) {
                            // Get a reference to the LoaderManager, in order to interact with loaders.
                            // Text submit already is saved into the query
                            String encodedQuery = "";
                            try {
                                encodedQuery = URLEncoder.encode(query, "utf-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            urlQuery = GUARDIAN_API + encodedQuery + key;

                            getLoaderManager().restartLoader(NEWS_LOADER_ID, null, MainActivity.this);

                            // Update empty state with no connection error message
                            mEmptyStateTextView.setText(R.string.no_internet_connection);

                            mEmptyStateTextView.setVisibility(View.GONE);

                            // If there is no data, display no connection gracefully
                        } else {
                            // Otherwise, display error
                            // First, hide loading indicator so error message will be visible
                            View loadingIndicator = findViewById(R.id.loading_indicator);
                            loadingIndicator.setVisibility(View.GONE);

                            // Update empty state with no connection error message
                            mAdapter.clear();
                            mEmptyStateTextView.setText(R.string.no_internet_connection);
                        }

                        return true;
                    }
                }

        );

        return true;
    }

// -----------------------------------UPDATE UI METHOD----------------------------------------------
    private void updateUi(List<Word> bookList) {
        mAdapter.clear();
        savedNews = bookList;
        mAdapter.addAll(bookList);
    }

    @Override
    public Loader<List<Word>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        Log.d(LOG_TAG, "This is the urlQuery: " + urlQuery);
        return new NewsLoader(this, urlQuery);
    }

    @Override
    public void onLoadFinished(Loader<List<Word>> loader, List<Word> news) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No earthquakes found."
        mEmptyStateTextView.setText(R.string.no_artciles);

        // Clear the adapter of previous news data
        mAdapter.clear();

        // If there is a valid list of {@link news}, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            Log.d(LOG_TAG, "This is the urlQuery: " + news.size());
            updateUi(news);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Word>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }


}
