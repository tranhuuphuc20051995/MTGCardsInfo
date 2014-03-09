package com.dbottillo.mtgsearch;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dbottillo.adapters.MTGSetSpinnerAdapter;
import com.dbottillo.database.CardContract;
import com.dbottillo.database.DatabaseHelper;
import com.dbottillo.database.MTGDatabaseHelper;
import com.dbottillo.database.SetContract;
import com.dbottillo.helper.CreateDBAsyncTask;
import com.dbottillo.helper.DBAsyncTask;
import com.dbottillo.resources.MTGSet;
import com.dbottillo.view.SlidingUpPanelLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Filter;

public class MainActivity extends DBActivity implements ActionBar.OnNavigationListener, DBAsyncTask.DBAsyncTaskListener, SlidingUpPanelLayout.PanelSlideListener{

    private static final int DATABASE_VERSION = 1;
    private static final String PREFERENCE_DATABASE_VERSION = "databaseVersion";

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private ArrayList<MTGSet> sets;
    private MTGSetSpinnerAdapter setAdapter;

    private SlidingUpPanelLayout slidingPanel;

    private FilterFragment filterFragment;

    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slidingPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingPanel.setPanelSlideListener(this);

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        if (savedInstanceState == null){
            sets = new ArrayList<MTGSet>();

            showLoadingInActionBar();
            new DBAsyncTask(this, this, DBAsyncTask.TASK_SET_LIST).execute();
        }else{
            sets = savedInstanceState.getParcelableArrayList("SET");
        }

        setAdapter = new MTGSetSpinnerAdapter(this, sets);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(setAdapter,  this);

        filterFragment = new FilterFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.filter, filterFragment)
                .commit();

        if (getSharedPreferences().getInt(PREFERENCE_DATABASE_VERSION, -1) != DATABASE_VERSION){
            Log.e("MTG", "wrong database version");
            File file = new File(getApplicationInfo().dataDir + "/databases/mtgsearch.db");
            file.delete();
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putInt(PREFERENCE_DATABASE_VERSION, DATABASE_VERSION);
            editor.commit();
        }

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            handleIntent(intent);
        }


        // NB: WARNING, FOR RECREATE DATABASE
        //String packageName = getApplication().getPackageName();
        //new CreateDBAsyncTask(this,packageName).execute();

        /*
        Danieles-MacBook-Pro:~ danielebottillo$ adb -d shell 'run-as com.dbottillo.mtgsearch cat /data/data/com.dbottillo.mtgsearch/databases/MTGCardsInfo.db > /sdcard/dbname.sqlite'
        Danieles-MacBook-Pro:~ danielebottillo$ adb pull /sdcard/dbname.sqlite
         */
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){
        String query = intent.getStringExtra(SearchManager.QUERY);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, MTGSetFragment.newInstance(query))
                .commit();
        slidingPanel.collapsePane();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
        outState.putParcelableArrayList("SET", sets);
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putInt("setPosition", position);
        editor.commit();
        loadSet();
        return true;
    }

    private void loadSet(){
        slidingPanel.collapsePane();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, MTGSetFragment.newInstance(sets.get(getSharedPreferences().getInt("setPosition",0))))
                .commit();
    }

    @Override
    public void onTaskFinished(ArrayList<?> result) {
        sets.clear();
        for (Object set : result){
            sets.add((MTGSet) set);
        }
        setAdapter.notifyDataSetChanged();
        result.clear();

        getActionBar().setSelectedNavigationItem(getSharedPreferences().getInt("setPosition", 0));
    }

    @Override
    public void onTaskEndWithError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    public SlidingUpPanelLayout getSlidingPanel(){
        return slidingPanel;
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {

    }

    @Override
    public void onPanelCollapsed(View panel) {
        MTGSetFragment setFragment = (MTGSetFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        setFragment.refreshUI();
    }

    @Override
    public void onPanelExpanded(View panel) {

    }

    @Override
    public void onPanelAnchored(View panel) {

    }

    public void onToggleClicked(View view) {
        filterFragment.onToggleClicked(view);
    }

    @Override
    public void onBackPressed(){
        if (slidingPanel.isExpanded()){
            slidingPanel.collapsePane();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) searchItem.getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        slidingPanel.collapsePane();
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        loadSet();
                        return true;
                    }
                });

        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.search_text_size));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_about:
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                AboutFragment newFragment = new AboutFragment();
                newFragment.show(ft, "dialog");
                return true;
            case R.id.action_update_database:
                // NB: WARNING, FOR DELETE DATABASE
                // /data/data/com.dbottillo.mtgsearch/databases/mtgsearch.db
                sets.clear();
                setAdapter.notifyDataSetChanged();
                File file = new File(getApplicationInfo().dataDir + "/databases/mtgsearch.db");
                file.delete();
                MTGDatabaseHelper dbHelper = new MTGDatabaseHelper(this);
                Toast.makeText(this, getString(R.string.set_loaded, dbHelper.getSets().getCount()), Toast.LENGTH_SHORT).show();
                new DBAsyncTask(this, this, DBAsyncTask.TASK_SET_LIST).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
