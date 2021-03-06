package com.dbottillo.mtgsearchfree.decks.deck

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.dbottillo.mtgsearchfree.decks.startingHand.DeckStartingHandFragment
import com.dbottillo.mtgsearchfree.decks.R
import com.dbottillo.mtgsearchfree.model.Deck
import com.dbottillo.mtgsearchfree.ui.BasicActivity
import com.dbottillo.mtgsearchfree.ui.views.MTGLoader
import com.dbottillo.mtgsearchfree.util.LOG
import com.dbottillo.mtgsearchfree.util.TrackingManager
import com.dbottillo.mtgsearchfree.util.gone
import com.dbottillo.mtgsearchfree.util.setTintColor
import com.dbottillo.mtgsearchfree.util.show
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.AndroidInjection
import javax.inject.Inject

class DeckActivity : BasicActivity(), DeckActivityView {

    @Inject lateinit var presenter: DeckActivityPresenter

    private val container: View by lazy(LazyThreadSafetyMode.NONE) { findViewById<ViewGroup>(R.id.container) }
    private val emptyView: TextView by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.empty_view) }
    private val loader: MTGLoader by lazy(LazyThreadSafetyMode.NONE) { findViewById<MTGLoader>(R.id.loader) }
    private val viewPager: ViewPager by lazy(LazyThreadSafetyMode.NONE) { findViewById<ViewPager>(R.id.deck_view_pager) }
    private val tabLayout: TabLayout by lazy(LazyThreadSafetyMode.NONE) { findViewById<TabLayout>(R.id.deck_cards_tab_layout) }

    override fun onCreate(bundle: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(bundle)
        setContentView(R.layout.activity_deck)

        setupToolbar(R.id.toolbar)

        val deckId = intent.getLongExtra("deck", 0)

        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }

        emptyView.setText(R.string.empty_deck)

        tabLayout.setupWithViewPager(viewPager)

        presenter.init(this, deckId)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.deck, menu)
        menu.findItem(R.id.action_copy).setTintColor(this, R.attr.colorPrimary)
        menu.findItem(R.id.action_export).setTintColor(this, R.attr.colorPrimary)
        menu.findItem(R.id.action_edit).setTintColor(this, R.attr.colorPrimary)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_export -> {
                exportDeck()
                return true
            }
            R.id.action_edit -> {
                editDeckName()
                return true
            }
            R.id.action_copy -> {
                presenter.copyDeck()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getPageTrack(): String? {
        return "/deck"
    }

    override fun showTitle(title: String) {
        this.title = title
    }

    override fun showEmptyScreen() {
        loader.gone()
        tabLayout.gone()
        emptyView.show()
    }

    override fun showDeck(deck: Deck) {
        loader.gone()
        tabLayout.show()
        viewPager.show()
        toolbar.elevation = 0f
        title = if (deck.name.isEmpty()) getString(R.string.deck_title) else deck.name
        viewPager.adapter = DeckPagerAdapter(supportFragmentManager, deck,
                listOf(getString(R.string.deck_list), getString(R.string.deck_starting_hand)))
    }

    override fun deckCopied() {
        Snackbar.make(container, getString(R.string.deck_copied), Snackbar.LENGTH_LONG).show()
    }

    override fun deckExported(uri: Uri) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.type = "text/plain"
        startActivity(Intent.createChooser(shareIntent, getString(R.string.deck_title)))
    }

    override fun deckNotExported() {
        Toast.makeText(this, getString(R.string.error_export_deck), Toast.LENGTH_SHORT).show()
        trackingManager.trackDeckExportError()
    }

    private fun exportDeck() {
        LOG.d()
        presenter.exportDeck()
    }

    private fun editDeckName() {
        LOG.d()
        val alert = MaterialAlertDialogBuilder(this)

        alert.setTitle(getString(R.string.edit_deck))

        val layoutInflater = LayoutInflater.from(this)
        @SuppressLint("InflateParams") val view = layoutInflater.inflate(R.layout.dialog_edit_deck, null)
        val editText = view.findViewById<EditText>(R.id.edit_text)
        editText.setText(presenter.deck.name)
        editText.setSelection(presenter.deck.name.length)
        alert.setView(view)

        alert.setPositiveButton(getString(R.string.save)) { dialog, _ ->
            val value = editText.text.toString()
            presenter.editDeck(value)
            trackingManager.trackEditDeck()
            dialog.dismiss()
        }

        alert.setNegativeButton(getString(R.string.cancel)) { _, _ ->
            // Canceled.
        }

        alert.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}

class DeckPagerAdapter(
    fragmentManager: FragmentManager,
    val deck: Deck,
    private val titleList: List<String>
) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                DeckFragment().apply { arguments = Bundle().apply { putLong(DECK_KEY, deck.id) } }
            }
            else -> DeckStartingHandFragment().apply { arguments = Bundle().apply { putLong(DECK_KEY, deck.id) } }
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return titleList[position]
    }
}