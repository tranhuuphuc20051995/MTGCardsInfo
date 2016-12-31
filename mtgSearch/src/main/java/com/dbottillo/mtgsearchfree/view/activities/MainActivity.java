package com.dbottillo.mtgsearchfree.view.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dbottillo.mtgsearchfree.BuildConfig;
import com.dbottillo.mtgsearchfree.R;
import com.dbottillo.mtgsearchfree.model.CardFilter;
import com.dbottillo.mtgsearchfree.model.database.CardsInfoDbHelper;
import com.dbottillo.mtgsearchfree.model.helper.AddFavouritesAsyncTask;
import com.dbottillo.mtgsearchfree.model.helper.CreateDBAsyncTask;
import com.dbottillo.mtgsearchfree.model.helper.CreateDecksAsyncTask;
import com.dbottillo.mtgsearchfree.model.storage.GeneralData;
import com.dbottillo.mtgsearchfree.presenter.CardFilterPresenter;
import com.dbottillo.mtgsearchfree.presenter.MainActivityPresenter;
import com.dbottillo.mtgsearchfree.util.CardMigratorService;
import com.dbottillo.mtgsearchfree.util.FileUtil;
import com.dbottillo.mtgsearchfree.util.LOG;
import com.dbottillo.mtgsearchfree.util.PermissionUtil;
import com.dbottillo.mtgsearchfree.util.TrackingManager;
import com.dbottillo.mtgsearchfree.view.CardFilterView;
import com.dbottillo.mtgsearchfree.view.MainView;
import com.dbottillo.mtgsearchfree.view.fragments.AboutFragment;
import com.dbottillo.mtgsearchfree.view.fragments.BasicFragment;
import com.dbottillo.mtgsearchfree.view.fragments.DecksFragment;
import com.dbottillo.mtgsearchfree.view.fragments.JoinBetaFragment;
import com.dbottillo.mtgsearchfree.view.fragments.LifeCounterFragment;
import com.dbottillo.mtgsearchfree.view.fragments.MainFragment;
import com.dbottillo.mtgsearchfree.view.fragments.NoticeDialogFragment;
import com.dbottillo.mtgsearchfree.view.fragments.ReleaseNoteFragment;
import com.dbottillo.mtgsearchfree.view.fragments.SavedFragment;
import com.dbottillo.mtgsearchfree.view.helpers.DialogHelper;
import com.dbottillo.mtgsearchfree.view.helpers.NavDrawerHelper;
import com.dbottillo.mtgsearchfree.view.helpers.SlidingPanelHelper;
import com.dbottillo.mtgsearchfree.view.views.FilterPickerView;
import com.dbottillo.mtgsearchfree.view.views.SlidingUpPanelLayout;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BasicActivity implements MainView, CardFilterView,
        NavigationView.OnNavigationItemSelectedListener, FilterPickerView.OnFilterPickerListener,
        SlidingPanelHelper.SlidingPanelHelperListener {

    public interface MainActivityListener {
        void updateContent();
    }

    private static final String CURRENT_SELECTION = "currentSelection";

    private MainActivityPresenter mainPresenter;
    private SlidingPanelHelper slidingPanelHelper;
    private NavDrawerHelper navDrawerHelper;
    private MainActivityListener listener;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @BindView(R.id.filter)
    FilterPickerView filterView;

    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;

    private boolean filterOn;
    private Bundle initialBundle;

    public CardFilter getCurrentFilter() {
        return currentFilter;
    }

    CardFilter currentFilter;

    @Inject
    CardFilterPresenter filterPresenter;

    @Inject
    GeneralData generalData;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        getMTGApp().getUiGraph().inject(this);

        setupToolbar();
        slidingPanelHelper = new SlidingPanelHelper(slidingUpPanelLayout, getResources(), this);
        slidingPanelHelper.init(filterView.findViewById(R.id.filter_draggable));
        navDrawerHelper = new NavDrawerHelper(this, navigationView, toolbar, this, generalData);

        initialBundle = bundle;

        filterPresenter.init(this);

        if (bundle == null) {
            filterPresenter.loadFilter();
        } else {
            currentFilter = bundle.getParcelable("currentFilter");
            filterView.refresh(currentFilter);
            filterOn = true;
        }

        mainPresenter = new MainActivityPresenter(this);
        mainPresenter.checkReleaseNote(getIntent());

        filterView.setFilterPickerListener(this);

        if (bundle != null && bundle.getInt(CURRENT_SELECTION) > 0) {
            slidingPanelHelper.hidePanel(true);
        }


        if (bundle == null && !generalData.isFreshInstall() && BuildConfig.VERSION_CODE == 58) {
            Intent intent = new Intent(this, CardMigratorService.class);
            startService(intent);
            DialogHelper.open(this, "notice", NoticeDialogFragment.newInstance(R.string.card_migrator_title, R.string.card_migrator_text));
        }

    }

    public void filterLoaded(CardFilter filter) {
        LOG.d();
        currentFilter = filter;
        if (!filterOn) {
            if (initialBundle == null) {
                changeFragment(new MainFragment(), "main", false);
                filterOn = true;
            }
        } else {
            listener.updateContent();
        }
        filterView.refresh(filter);
    }

    public String getPageTrack() {
        return "/main";
    }

    public void onNewIntent(Intent intent) {
        LOG.d();
        super.onNewIntent(intent);
        mainPresenter.checkReleaseNote(intent);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_SELECTION, navDrawerHelper.getCurrentSelection());
        outState.putParcelable("currentFilter", currentFilter);
    }

    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navDrawerHelper.syncState();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        navDrawerHelper.onConfigurationChanged(newConfig);
    }

    public void showReleaseNote() {
        LOG.d();
        changeFragment(new ReleaseNoteFragment(), "release_note_fragment", true);
        slidingPanelHelper.hidePanel(true);
        navDrawerHelper.select(6);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (navDrawerHelper.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            navDrawerHelper.openDrawer();
            return true;
        }
        if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_lucky) {
            startActivity(new Intent(this, CardLuckyActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(MenuItem menuItem) {
        BasicFragment currentFragment = (BasicFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (menuItem.getItemId() == R.id.drawer_home && !(currentFragment instanceof MainFragment)) {
            changeFragment(new MainFragment(), "main", false);
            slidingPanelHelper.showPanel();
        } else if (menuItem.getItemId() == R.id.drawer_saved && !(currentFragment instanceof SavedFragment)) {
            changeFragment(new SavedFragment(), "saved_fragment", true);
            slidingPanelHelper.showPanel();

        } else if (menuItem.getItemId() == R.id.drawer_life_counter && !(currentFragment instanceof LifeCounterFragment)) {
            changeFragment(LifeCounterFragment.newInstance(), "life_counter", true);
            slidingPanelHelper.hidePanel(true);

        } else if (menuItem.getItemId() == R.id.drawer_decks && !(currentFragment instanceof DecksFragment)) {
            changeFragment(new DecksFragment(), "decks", true);
            slidingPanelHelper.hidePanel(true);

        } else if (menuItem.getItemId() == R.id.drawer_rate) {
            openRateTheApp();

        } else if (menuItem.getItemId() == R.id.drawer_beta && !(currentFragment instanceof JoinBetaFragment)) {
            changeFragment(new JoinBetaFragment(), "joinbeta_fragment", true);
            slidingPanelHelper.hidePanel(true);

        } else if (menuItem.getItemId() == R.id.drawer_about && !(currentFragment instanceof AboutFragment)) {
            changeFragment(new AboutFragment(), "about_fragment", true);
            slidingPanelHelper.hidePanel(true);

        } else if (menuItem.getItemId() == R.id.drawer_release_note) {
            showReleaseNote();

        } else if (menuItem.getItemId() == 100) {
            // NB: WARNING, FOR RECREATE DATABASE
            recreateDb();

        } else if (menuItem.getItemId() == 101) {
            new CreateDecksAsyncTask(getApplicationContext()).execute();

        } else if (menuItem.getItemId() == 102) {
            new AddFavouritesAsyncTask(getApplicationContext()).execute();

        } else if (menuItem.getItemId() == 103) {
            throw new RuntimeException("This is a crash");

        } else if (menuItem.getItemId() == 104) {
            copyDBToSdCard();

        } else if (menuItem.getItemId() == 105) {
            boolean copied = FileUtil.copyDbFromSdCard(getApplicationContext(), CardsInfoDbHelper.DATABASE_NAME);
            Toast.makeText(this, (copied) ? "database copied" : "database not copied", Toast.LENGTH_LONG).show();
        }

        navDrawerHelper.closeDrawer();
        return true;
    }

    private void recreateDb() {
        requestPermission(PermissionUtil.TYPE.WRITE_STORAGE, new PermissionUtil.PermissionListener() {
            @Override
            public void permissionGranted() {
                new CreateDBAsyncTask(MainActivity.this, getApplication().getPackageName()).execute();
            }

            @Override
            public void permissionNotGranted() {
                Toast.makeText(MainActivity.this, getString(R.string.error_export_db), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void copyDBToSdCard() {
        requestPermission(PermissionUtil.TYPE.WRITE_STORAGE, new PermissionUtil.PermissionListener() {
            @Override
            public void permissionGranted() {
                final File file = FileUtil.copyDbToSdCard(getApplicationContext(), CardsInfoDbHelper.DATABASE_NAME);
                if (file != null) {
                    Snackbar snackbar = Snackbar
                            .make(slidingUpPanelLayout, getString(R.string.db_exported), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.share), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"help@mtgcardsinfo.com"});
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "[MTGCardsInfo] Database status");
                                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                                    startActivity(Intent.createChooser(intent, "Send mail...."));
                                    TrackingManager.trackDeckExport();
                                }
                            });
                    snackbar.show();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.error_export_db), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void permissionNotGranted() {
                Toast.makeText(MainActivity.this, getString(R.string.error_export_db), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtil.isGranted(grantResults)) {
            copyDBToSdCard();
        } else {
            Toast.makeText(this, getString(R.string.error_export_db), Toast.LENGTH_SHORT).show();
        }
    }

    public void onBackPressed() {
        LOG.d();
        if (slidingPanelHelper.onBackPressed()) {
            return;
        }
        navDrawerHelper.onBackPressed();
        BasicFragment current = (BasicFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (current.onBackPressed()) {
            return;
        }
        if (!(current instanceof MainFragment)) {
            changeFragment(new MainFragment(), "main", false);
            navDrawerHelper.resetSelection();
            slidingPanelHelper.showPanel();
        } else {
            finish();
        }
    }

    public void setMainActivityListener(MainActivityListener list) {
        listener = list;
    }

    public void filterUpdated(CardFilter.TYPE type, boolean on) {
        LOG.d();
        filterPresenter.update(type, on);
    }

    public void onPanelChangeOffset(float offset) {
        filterView.onPanelSlide(offset);
    }

    public boolean isFilterOpen() {
        return slidingPanelHelper.isPanelOpen();
    }

    public void closePanel() {
        slidingPanelHelper.closePanel();
    }
}