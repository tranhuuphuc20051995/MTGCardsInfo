package com.dbottillo.common;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.dbottillo.BuildConfig;
import com.dbottillo.adapters.GameSetAdapter;
import com.dbottillo.adapters.LeftMenuAdapter;
import com.dbottillo.base.DBActivity;
import com.dbottillo.base.MTGApp;
import com.dbottillo.database.CardDatabaseHelper;
import com.dbottillo.database.DatabaseHelper;
import com.dbottillo.helper.CreateDBAsyncTask;
import com.dbottillo.helper.DBAsyncTask;
import com.dbottillo.R;
import com.dbottillo.lifecounter.LifeCounterActivity;
import com.dbottillo.resources.GameSet;
import com.dbottillo.saved.SavedActivity;
import com.dbottillo.view.SlidingUpPanelLayout;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends DBActivity implements DBAsyncTask.DBAsyncTaskListener, SlidingUpPanelLayout.PanelSlideListener, AdapterView.OnItemClickListener {

    private static final String PREFERENCE_DATABASE_VERSION = "databaseVersion";

    private ArrayList<GameSet> sets;
    private GameSetAdapter setAdapter;

    private SlidingUpPanelLayout slidingPanel;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private LeftMenuAdapter leftMenuAdapter;
    private ImageView setArrow;
    private View setListBg;
    private ListView setList;
    private View container;

    private FilterFragment filterFragment;

    SearchView searchView;
    ImageView arrow;

    private int currentSetPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupDrawerLayout();

        slidingPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingPanel.setPanelSlideListener(this);
        container = findViewById(R.id.container);
        setListBg = findViewById(R.id.set_list_bg);
        setList = (ListView) findViewById(R.id.set_list);
        setArrow = (ImageView) findViewById(R.id.set_arrow);

        setListBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setList.getHeight() > 0){
                    showHideSetList(false);
                }
            }
        });

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getActionBar();
        getActionBar().setTitle(R.string.app_name);

        if (getSharedPreferences().getInt(PREFERENCE_DATABASE_VERSION, -1) != BuildConfig.DATABASE_VERSION){
            Log.e("MTG", getSharedPreferences().getInt(PREFERENCE_DATABASE_VERSION, -1)+" <-- wrong database version --> "+BuildConfig.DATABASE_VERSION);
            File file = new File(getApplicationInfo().dataDir + "/databases/"+ DatabaseHelper.DATABASE_NAME);
            file.delete();
            CardDatabaseHelper dbHelper = CardDatabaseHelper.getDatabaseHelper(this);
            Toast.makeText(this, getString(R.string.set_loaded, dbHelper.getSets().getCount()), Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putInt(PREFERENCE_DATABASE_VERSION, BuildConfig.DATABASE_VERSION);
            editor.apply();
        }

        if (savedInstanceState == null){
            sets = new ArrayList<GameSet>();

            showLoadingInActionBar();
            new DBAsyncTask(this, this, DBAsyncTask.TASK_SET_LIST).execute();
        }else{
            sets = savedInstanceState.getParcelableArrayList("SET");
            currentSetPosition = savedInstanceState.getInt("currentSetPosition");
            loadSet();
        }

        setAdapter = new GameSetAdapter(this, sets);
        setAdapter.setCurrent(currentSetPosition);
        setList.setAdapter(setAdapter);
        setList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentSetPosition != position) {
                    currentSetPosition = position;
                    showHideSetList(true);
                } else {
                    showHideSetList(false);
                }
            }
        });

        if (BuildConfig.magic) {
            filterFragment = new FilterFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.filter, filterFragment)
                    .commit();
        } else {
            slidingPanel.setPanelHeight(0);
        }

        findViewById(R.id.set_chooser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHideSetList(false);
            }
        });

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            handleIntent(intent);
        }
    }

    private void showHideSetList(final boolean loadSet) {
        final int startHeight = setList.getHeight();
        final int targetHeight = (startHeight == 0) ? container.getHeight() : 0;
        final float startRotation = setArrow.getRotation();
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                super.applyTransformation(interpolatedTime, t);
                if (targetHeight > startHeight){
                    int newHeight = (int) (startHeight + (interpolatedTime * targetHeight));
                    setHeightView(setList, newHeight);
                    setHeightView(setListBg, newHeight);
                    setArrow.setRotation(startRotation + (180 * interpolatedTime));
                } else {
                    int newHeight = (int) (startHeight - startHeight*interpolatedTime);
                    setHeightView(setList, newHeight);
                    setHeightView(setListBg, newHeight);
                    setArrow.setRotation( startRotation - (180 * interpolatedTime));
                }
            }
        };
        animation.setDuration(200);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (loadSet){
                    /*if (!getApp().isPremium() && position > 2){
                        showGoToPremium();
                        return false;
                    }*/
                    getApp().trackEvent(MTGApp.UA_CATEGORY_UI, "spinner_selected", sets.get(currentSetPosition).getCode());
                    SharedPreferences.Editor editor = getSharedPreferences().edit();
                    editor.putInt("setPosition", currentSetPosition);
                    editor.apply();
                    setAdapter.setCurrent(currentSetPosition);
                    setAdapter.notifyDataSetChanged();
                    loadSet();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slidingPanel.startAnimation(animation);

    }

    private void setHeightView(View view, int value){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.height = value;
        view.setLayoutParams(params);
    }

    private void setupDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_navigation_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerSlide(final View view, final float slideOffset) {
                super.onDrawerSlide(view, slideOffset);
                float value = 0.9f + 0.1f * (1.0f - slideOffset);
                findViewById(R.id.sliding_layout).setScaleX(value);
                findViewById(R.id.sliding_layout).setScaleY(value);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        final ArrayList<LeftMenuAdapter.LeftMenuItem> items = new ArrayList<LeftMenuAdapter.LeftMenuItem>();
        for (LeftMenuAdapter.LeftMenuItem leftMenuItem : LeftMenuAdapter.LeftMenuItem.values()){
            boolean skip = false;
            if (leftMenuItem == LeftMenuAdapter.LeftMenuItem.CREATE_DB && !BuildConfig.DEBUG) skip = true;
            if (leftMenuItem == LeftMenuAdapter.LeftMenuItem.LIFE_COUNTER && !BuildConfig.magic) skip = true;
            if (!skip) {
                items.add(leftMenuItem);
            }
        }

        leftMenuAdapter = new LeftMenuAdapter(this, items);
        mDrawerList.setAdapter(leftMenuAdapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public String getPageTrack() {
        return "/main";
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            setIntent(intent);
            handleIntent(intent);
        }
    }

    private void handleIntent(Intent intent){
        String query = intent.getStringExtra(SearchManager.QUERY);
        getApp().trackEvent(MTGApp.UA_CATEGORY_SEARCH, "done", query);
        if (query.length() < 3){
            Toast.makeText(this, getString(R.string.minimum_search), Toast.LENGTH_SHORT).show();
            return;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, MTGSetFragment.newInstance(query))
                .commit();
        slidingPanel.collapsePane();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("currentSetPosition",currentSetPosition);
        outState.putParcelableArrayList("SET", sets);
    }

    private void loadSet(){
        slidingPanel.collapsePane();
        ((TextView)findViewById(R.id.set_chooser_name)).setText(sets.get(currentSetPosition).getName());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, MTGSetFragment.newInstance(sets.get(currentSetPosition)))
                .commit();
    }

    @Override
    public void onTaskFinished(ArrayList<?> result) {
        currentSetPosition = getSharedPreferences().getInt("setPosition",0);
        setAdapter.setCurrent(currentSetPosition);

        sets.clear();
        for (Object set : result){
            sets.add((GameSet) set);
        }
        setAdapter.notifyDataSetChanged();
        result.clear();

        hideLoadingFromActionBar();
        loadSet();
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
        setRotationArrow(180 - (180*slideOffset));
    }

    @Override
    public void onPanelCollapsed(View panel) {
        getApp().trackEvent(MTGApp.UA_CATEGORY_UI, "panel", "collapsed");
        MTGSetFragment setFragment = (MTGSetFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        setFragment.refreshUI();

        setRotationArrow(0);
    }

    @Override
    public void onPanelExpanded(View panel) {
        getApp().trackEvent(MTGApp.UA_CATEGORY_UI, "panel", "expanded");
        setRotationArrow(180);

    }

    @Override
    public void onPanelAnchored(View panel) {

    }

    private void setRotationArrow(float angle){
        if (arrow == null) arrow = (ImageView) findViewById(R.id.arrow_filter);
        else arrow.setRotation(angle);
    }

    public void onToggleClicked(View view) {
        filterFragment.onToggleClicked(view);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (slidingPanel.isExpanded()){
            slidingPanel.collapsePane();
            return false;
        }
        return super.onKeyDown(keyCode, event);
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

        try {
            int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView textView = (TextView) searchView.findViewById(id);
            textView.setHintTextColor(getResources().getColor(R.color.light_grey));
        } catch (Exception e){

        }

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

        searchItem.setVisible(!mDrawerLayout.isDrawerOpen(mDrawerList));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showGoToPremium() {
        openDialog(DBDialog.PREMIUM);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LeftMenuAdapter.LeftMenuItem item = leftMenuAdapter.getItem(position);
        if (item == LeftMenuAdapter.LeftMenuItem.FAVOURITE){
            startActivity(new Intent(this, SavedActivity.class));

        } else if (item == LeftMenuAdapter.LeftMenuItem.LIFE_COUNTER){
            startActivity(new Intent(this, LifeCounterActivity.class));

        }else if (item == LeftMenuAdapter.LeftMenuItem.ABOUT){
            openDialog(DBDialog.ABOUT);

        }else if (item == LeftMenuAdapter.LeftMenuItem.FORCE_UPDATE){
            // /data/data/com.dbottillo.mtgsearch/databases/mtgsearch.db
            getApp().trackEvent(MTGApp.UA_CATEGORY_SEARCH, "reset_db", "");
            sets.clear();
            setAdapter.notifyDataSetChanged();
            File file = new File(getApplicationInfo().dataDir + "/databases/"+ DatabaseHelper.DATABASE_NAME);
            file.delete();
            CardDatabaseHelper dbHelper = CardDatabaseHelper.getDatabaseHelper(this);
            Toast.makeText(this, getString(R.string.set_loaded, dbHelper.getSets().getCount()), Toast.LENGTH_SHORT).show();
            new DBAsyncTask(this, this, DBAsyncTask.TASK_SET_LIST).execute();

        }else if (item == LeftMenuAdapter.LeftMenuItem.CREATE_DB){
            // NB: WARNING, FOR RECREATE DATABASE
            String packageName = getApplication().getPackageName();
            new CreateDBAsyncTask(this,packageName).execute();
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void onBackPressed() {
        if (setList.getHeight() > 0){
            showHideSetList(false);
        }else {
            super.onBackPressed();
        }
    }
}
