package com.dbottillo.mtgsearchfree.decks.deck

import com.dbottillo.mtgsearchfree.interactors.DecksInteractor
import com.dbottillo.mtgsearchfree.model.Deck
import com.dbottillo.mtgsearchfree.util.Logger
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class DeckActivityPresenter @Inject constructor(
    private val interactor: DecksInteractor,
    private val logger: Logger
) {

    lateinit var view: DeckActivityView
    lateinit var deck: Deck

    private var disposable: CompositeDisposable = CompositeDisposable()

    init {
        logger.d("created")
    }

    fun init(view: DeckActivityView, deckId: Long) {
        logger.d()
        this.view = view
        disposable.add(
            interactor.loadDeckById(deckId).subscribe({
                deck = it
                load()
            }, {
                logger.logNonFatal(it)
            })
        )
    }

    private fun load() {
        if (deck.numberOfCards == 0) {
            view.showEmptyScreen()
            view.showTitle(deck.name)
        } else {
            view.showDeck(deck)
            deckLoaded()
        }
    }

    fun editDeck(name: String) {
        disposable.add(interactor.editDeck(deck, name).subscribe({
            this.deck = it
            deckLoaded()
        }, {
            logger.logNonFatal(it)
        }))
    }

    fun exportDeck() {
        disposable.add(interactor.exportDeck(deck).subscribe({
            view.deckExported(it)
        }, {
            view.deckNotExported()
            logger.logNonFatal(it)
        }))
    }

    fun copyDeck() {
        disposable.add(interactor.copy(deck).subscribe({
            view.deckCopied()
        }, {
            logger.logNonFatal(it)
        }))
    }

    fun onDestroy() {
        disposable.clear()
    }

    private fun deckLoaded() {
        view.showTitle("${deck.name} (${deck.numberOfCards - deck.sizeOfSideboard}/${deck.sizeOfSideboard})")
    }
}