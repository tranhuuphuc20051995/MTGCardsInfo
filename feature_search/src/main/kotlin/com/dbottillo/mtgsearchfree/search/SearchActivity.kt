package com.dbottillo.mtgsearchfree.search

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Toast
import com.dbottillo.mtgsearchfree.model.CardsCollection
import com.dbottillo.mtgsearchfree.model.MTGCard
import com.dbottillo.mtgsearchfree.model.MTGSet
import com.dbottillo.mtgsearchfree.model.SearchParams
import com.dbottillo.mtgsearchfree.ui.BasicActivity
import com.dbottillo.mtgsearchfree.ui.cards.OnCardListener
import com.dbottillo.mtgsearchfree.ui.cardsConfigurator.CARDS_CONFIGURATION_SHOW_FILTER
import com.dbottillo.mtgsearchfree.ui.cardsConfigurator.CARDS_CONFIGURATION_SHOW_ORDER
import com.dbottillo.mtgsearchfree.ui.cardsConfigurator.CardsConfiguratorFragment
import com.dbottillo.mtgsearchfree.ui.views.MTGCardsView
import com.dbottillo.mtgsearchfree.util.AnimationUtil
import com.dbottillo.mtgsearchfree.util.LOG
import com.dbottillo.mtgsearchfree.util.TrackingManager
import com.dbottillo.mtgsearchfree.util.bind
import com.dbottillo.mtgsearchfree.util.setHeight
import com.dbottillo.mtgsearchfree.util.setMarginTop
import com.dbottillo.mtgsearchfree.util.show
import dagger.android.AndroidInjection
import javax.inject.Inject

@Suppress("EmptyFunctionBlock")
class SearchActivity : BasicActivity(), View.OnClickListener, SearchActivityView, OnCardListener {

    private val newSearch: ImageButton by bind(R.id.action_search)
    private val scrollView: ScrollView by bind(R.id.search_scroll_view)
    private val mtgCardsView: MTGCardsView by bind(R.id.cards_list_view)
    private val searchView: MTGSearchView by bind(R.id.search_view)
    private val closeButton: ImageButton by bind(R.id.close_button)

    private var newSearchAnimation: AnimationDrawable? = null
    private var sizeBig = 0
    private var searchOpen = false

    @Inject
    lateinit var presenter: SearchPresenter

    override fun onCreate(bundle: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(bundle)
        setContentView(R.layout.activity_search)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.setTitle(R.string.action_search)
        mtgCardsView.setEmptyString(R.string.empty_search)
        closeButton.setOnClickListener {
            newSearch.callOnClick()
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (bundle != null) {
            searchOpen = bundle.getBoolean(SEARCH_OPEN)
            closeButton.imageAlpha = if (searchOpen) 1 else 0
        }

        newSearch.setOnClickListener(this)
        newSearch.setBackgroundResource(R.drawable.anim_search_icon)
        newSearch.elevation = 6.0f

        if (bundle != null) {
            val searchParams = bundle.getParcelable<SearchParams>(SEARCH_PARAMS)
            if (searchParams != null) {
                doSearch(searchParams)
            }
        }

        scrollView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                sizeBig = scrollView.height
                if (searchOpen) {
                    scrollView.setHeight(0)
                    mtgCardsView.setMarginTop(0)
                    mtgCardsView.visibility = View.VISIBLE
                }
                scrollView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        presenter.init(this)
        presenter.loadSet()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SEARCH_OPEN, searchOpen)
        outState.putParcelable(SEARCH_PARAMS, searchView.searchParams)
    }

    override fun getPageTrack(): String {
        return "/search_main"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun doSearch(searchParams: SearchParams) {
        LOG.d()
        trackingManager.trackSearch(searchParams.toString())
        presenter.doSearch(searchParams)
        hideIme()
    }

    override fun onClick(v: View) {
        LOG.d()
        var searchParams: SearchParams? = null
        if (!searchOpen) {
            searchParams = searchView.searchParams
            if (!searchParams.isValid) {
                Toast.makeText(this, getString(R.string.minimum_search), Toast.LENGTH_SHORT).show()
                return
            }
        }
        val backgroundInterpolator = AnimationUtil.createLinearInterpolator()
        if (!searchOpen) {
            newSearch.setBackgroundResource(R.drawable.anim_search_icon)
            backgroundInterpolator.fromValue(sizeBig.toFloat()).toValue(0f)
        } else {
            newSearch.setBackgroundResource(R.drawable.anim_search_icon_reverse)
            backgroundInterpolator.fromValue(0f).toValue(sizeBig.toFloat())
        }
        newSearchAnimation = newSearch.background as AnimationDrawable
        newSearchAnimation?.start()
        val anim = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                super.applyTransformation(interpolatedTime, t)
                val `val` = backgroundInterpolator.getInterpolation(interpolatedTime).toInt()
                scrollView.setHeight(`val`)
            }
        }
        val finalSearchParams = searchParams
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                closeButton.visibility = View.VISIBLE
                if (!searchOpen) {
                    mtgCardsView.visibility = View.VISIBLE
                    closeButton.animate().setDuration(100).alpha(1f).start()
                } else {
                    closeButton.animate().setDuration(100).alpha(0f).start()
                }
            }

            override fun onAnimationEnd(animation: Animation) {
                if (searchOpen) {
                    closeButton.visibility = View.GONE
                    mtgCardsView.visibility = View.GONE
                } else {
                    closeButton.visibility = View.VISIBLE
                    finalSearchParams?.let {
                        doSearch(it)
                    }
                }
                searchOpen = !searchOpen
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
        anim.duration = 200
        scrollView.startAnimation(anim)
    }

    override fun onBackPressed() {
        if (searchOpen) {
            newSearch.callOnClick()
        } else {
            super.onBackPressed()
        }
    }

    override fun setLoaded(data: List<MTGSet>) {
        LOG.d()
        searchView.refreshSets(data)
    }

    override fun showSearch(data: CardsCollection) {
        mtgCardsView.loadCards(data.list, this, R.string.search_result)
    }

    override fun showCardsGrid() {
        mtgCardsView.setGridOn()
    }

    override fun showCardsList() {
        mtgCardsView.setListOn()
    }

    override fun onTitleHeaderSelected() {
    }

    override fun onCardsViewTypeSelected() {
        presenter.toggleCardTypeViewPreference()
    }

    override fun onCardsSettingSelected() {
        val fragment = CardsConfiguratorFragment()
        fragment.arguments = Bundle().apply {
            putBoolean(CARDS_CONFIGURATION_SHOW_FILTER, false)
            putBoolean(CARDS_CONFIGURATION_SHOW_ORDER, true)
        }
        fragment.listener = object : CardsConfiguratorFragment.CardsConfiguratorListener {
            override fun onConfigurationChange() {
                presenter.doSearch(searchView.searchParams)
            }
        }
        fragment.show(supportFragmentManager, "cards_configurator")
    }

    override fun onCardSelected(card: MTGCard, position: Int) {
        LOG.d()
        navigator.openCardsScreen(this, searchView.searchParams, position)
    }

    override fun onOptionSelected(menuItem: MenuItem, card: MTGCard, position: Int) {
        if (menuItem.itemId == R.id.action_add_to_deck) {
            this.show("add_to_deck", navigator.newAddToDeckFragment(card))
        } else if (menuItem.itemId == R.id.action_add_to_favourites) {
            presenter.saveAsFavourite(card)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}

private const val SEARCH_OPEN = "searchOpen"
private const val SEARCH_PARAMS = "searchParams"