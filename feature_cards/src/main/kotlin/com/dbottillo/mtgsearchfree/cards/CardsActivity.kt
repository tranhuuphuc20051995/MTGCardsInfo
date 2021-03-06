package com.dbottillo.mtgsearchfree.cards

import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import android.widget.RelativeLayout
import android.widget.Toast
import com.dbottillo.mtgsearchfree.model.CardsCollection
import com.dbottillo.mtgsearchfree.model.MTGCard
import com.dbottillo.mtgsearchfree.ui.CommonCardsActivity
import com.dbottillo.mtgsearchfree.ui.views.CardPresenter
import com.dbottillo.mtgsearchfree.ui.views.MTGLoader
import com.dbottillo.mtgsearchfree.util.LOG
import com.dbottillo.mtgsearchfree.util.dpToPx
import com.dbottillo.mtgsearchfree.util.gone
import com.dbottillo.mtgsearchfree.util.setIcon
import com.dbottillo.mtgsearchfree.util.show
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.AndroidInjection
import javax.inject.Inject

class CardsActivity : CommonCardsActivity(), ViewPager.OnPageChangeListener, CardsActivityView {

    private val viewPager by lazy { findViewById<ViewPager>(R.id.cards_view_pager) }
    private val tabLayout by lazy { findViewById<TabLayout>(R.id.cards_tab_layout) }
    private val fabButton by lazy { findViewById<FloatingActionButton>(R.id.card_add_to_deck) }
    private val loader by lazy { findViewById<MTGLoader>(R.id.loader) }
    private val previewBanner by lazy(LazyThreadSafetyMode.NONE) { findViewById<View>(R.id.preview_banner) }

    private var adapter: CardsPagerAdapter? = null

    @Inject lateinit var cardsPresenter: CardsActivityPresenter
    @Inject lateinit var cardPresenter: CardPresenter

    public override val currentCard: MTGCard?
        get() {
            LOG.d()
            return adapter?.getItem(viewPager.currentItem)
        }

    override fun onCreate(bundle: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(bundle)
        setContentView(R.layout.activity_cards)

        fabButton.setOnClickListener {
            LOG.d()
            currentCard?.let { openDialog("add_to_deck", navigator.newAddToDeckFragment(it)) }
        }
        setupView()

        cardsPresenter.init(this, intent)
    }

    private fun setupView() {
        setupToolbar(R.id.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        toolbar.elevation = 0f

        tabLayout.setupWithViewPager(viewPager)
        val par = fabButton.layoutParams as RelativeLayout.LayoutParams
        if (isPortrait) {
            par.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
        } else {
            par.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
            par.rightMargin = this.dpToPx(16)
        }
        fabButton.layoutParams = par
        viewPager.addOnPageChangeListener(this)

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        previewBanner.setOnClickListener {
            val dialogBuilder = MaterialAlertDialogBuilder(this)
            dialogBuilder.setMessage(getString(R.string.set_preview_banner_text))
                    .setCancelable(true)
                    .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            val alert = dialogBuilder.create()
            alert.setTitle(R.string.set_preview_banner)
            alert.show()
        }
    }

    override fun getPageTrack(): String? {
        return if (cardsPresenter.isDeck()) {
            "/deck"
        } else "/cards"
    }

    override fun updateAdapter(cards: CardsCollection, showImage: Boolean, startPosition: Int) {
        LOG.d()
        adapter = CardsPagerAdapter(context = this, showImage = showImage,
                cards = cards, cardPresenter = cardPresenter, trackingManager = trackingManager)
        viewPager.adapter = adapter
        viewPager.currentItem = startPosition
        syncMenu()
    }

    public override fun favClicked() {
        LOG.d()
        cardsPresenter.favClicked(currentCard)
    }

    public override fun toggleImage(show: Boolean) {
        LOG.d()
        cardsPresenter.toggleImage(show)
    }

    override fun syncMenu() {
        cardsPresenter.updateMenu(currentCard)
    }

    override fun showError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onPageScrollStateChanged(state: Int) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            setFabScale(1.0f)
            syncMenu()
        }
    }

    override fun onPageSelected(position: Int) {
        syncMenu()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (positionOffset.toDouble() != 0.0) {
            if (positionOffset < 0.5) {
                setFabScale(1.0f - positionOffset)
            } else {
                setFabScale(positionOffset)
            }
        }
    }

    private fun setFabScale(value: Float) {
        fabButton.scaleX = value
        fabButton.scaleY = value
    }

    override fun updateTitle(name: String) {
        title = name
    }

    override fun updateTitle(resource: Int) {
        title = getString(resource)
    }

    override fun showFavMenuItem() {
        favMenuItem?.let { it.isVisible = true }
    }

    override fun updateFavMenuItem(text: Int, icon: Int) {
        favMenuItem?.let { fav ->
            fav.title = getString(text)
            fav.setIcon(this, icon, R.attr.colorPrimary)
        }
    }

    override fun hideFavMenuItem() {
        favMenuItem?.let { it.isVisible = false }
    }

    override fun setImageMenuItemChecked(checked: Boolean) {
        imageMenuItem?.isChecked = checked
    }

    override fun showLoading() {
        loader.show()
    }

    override fun hideLoading() {
        loader.gone()
    }

    override fun shareImage(bitmap: Bitmap) {
        currentCard?.let { cardsPresenter.shareImage(bitmap) }
    }

    override fun shareUri(uri: Uri) {
        shareUriArtwork(currentCard?.name ?: "", uri)
    }

    override fun onDestroy() {
        super.onDestroy()
        cardsPresenter.onDestroy()
    }

    override fun renderPreview(preview: Boolean) {
        if (preview) {
            fabButton.hide()
            previewBanner.show()
        } else {
            fabButton.show()
            previewBanner.gone()
        }
    }
}

const val KEY_SEARCH = "Search"
const val KEY_SET = "Set"
const val KEY_FAV = "Fav"
const val KEY_DECK = "Deck"
const val POSITION = "Position"
