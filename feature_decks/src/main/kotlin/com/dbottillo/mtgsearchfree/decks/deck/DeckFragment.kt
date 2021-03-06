package com.dbottillo.mtgsearchfree.decks.deck

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.dbottillo.mtgsearchfree.Navigator
import com.dbottillo.mtgsearchfree.decks.R
import com.dbottillo.mtgsearchfree.model.DeckCollection
import com.dbottillo.mtgsearchfree.model.MTGCard
import com.dbottillo.mtgsearchfree.ui.BasicFragment
import com.dbottillo.mtgsearchfree.util.LOG
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class DeckFragment : BasicFragment(), DeckView {

    @Inject lateinit var presenter: DeckPresenter
    @Inject lateinit var navigator: Navigator

    private lateinit var cardList: RecyclerView
    private val deckAdapter = DeckAdapter()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_deck, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardList = view.findViewById(R.id.card_list)

        deckAdapter.cardListener = object : OnDeckCardListener {
            override fun onCardSelected(card: MTGCard) {
                navigator.openCardsScreen(requireActivity(), presenter.deck, deckAdapter.getCards().indexOfFirst {
                    if (it.uuid.isNotEmpty() && card.uuid.isNotEmpty()) {
                        it.uuid == card.uuid
                    } else {
                        it.multiVerseId == card.multiVerseId
                    }
                })
            }

            override fun onOptionSelected(menuItem: MenuItem, card: MTGCard) {
                if (menuItem.itemId == R.id.action_add_one_more) {
                    trackingManager.trackAddCardToDeck()
                    presenter.addCardToDeck(card, 1)
                } else if (menuItem.itemId == R.id.action_remove_one) {
                    trackingManager.trackRemoveCardFromDeck()
                    presenter.removeCardFromDeck(card)
                } else if (menuItem.itemId == R.id.action_remove_all) {
                    trackingManager.trackRemoveAllCardsFromDeck()
                    presenter.removeAllCardFromDeck(card)
                } else if (menuItem.itemId == R.id.action_move_one) {
                    trackingManager.trackMoveOneCardFromDeck()
                    if (card.isSideboard) {
                        presenter.moveCardFromSideBoard(card, 1)
                    } else {
                        presenter.moveCardToSideBoard(card, 1)
                    }
                } else if (menuItem.itemId == R.id.action_move_all) {
                    trackingManager.trackMoveAllCardFromDeck()
                    if (card.isSideboard) {
                        presenter.moveCardFromSideBoard(card, card.quantity)
                    } else {
                        presenter.moveCardToSideBoard(card, card.quantity)
                    }
                }
            }
        }
        cardList.adapter = deckAdapter

        cardList.setHasFixedSize(true)
        cardList.layoutManager = LinearLayoutManager(view.context)

        val deckId = arguments?.getLong(DECK_KEY) ?: 0
        presenter.init(this, deckId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroyView()
    }

    override fun deckLoaded(title: String, collection: DeckCollection) {
        LOG.d(collection.toCardsCollection().list.toString())
        val sections = ArrayList<DeckSection>()
        sections.add(DeckSection(getString(R.string.deck_header_creatures) + " (" + collection.getNumberOfCreatures() + ")", collection.creatures))
        sections.add(DeckSection(getString(R.string.deck_header_instant_sorceries) + " (" + collection.getNumberOfInstantAndSorceries() + ")", collection.instantAndSorceries))
        sections.add(DeckSection(getString(R.string.deck_header_other) + " (" + collection.getNumberOfOther() + ")", collection.other))
        sections.add(DeckSection(getString(R.string.deck_header_lands) + " (" + collection.getNumberOfLands() + ")", collection.lands))
        sections.add(DeckSection(getString(R.string.deck_header_sideboard) + " (" + collection.numberOfCardsInSideboard() + ")", collection.side))

        deckAdapter.setSections(sections)
        activity?.title = title
    }

    override fun getPageTrack(): String = "/deck"

    override fun getTitle(): String = presenter.deck.name
}

const val DECK_KEY = "deck"