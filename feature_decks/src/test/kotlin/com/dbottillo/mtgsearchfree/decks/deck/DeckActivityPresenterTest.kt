package com.dbottillo.mtgsearchfree.decks.deck

import android.net.Uri
import com.dbottillo.mtgsearchfree.interactors.DecksInteractor
import com.dbottillo.mtgsearchfree.model.Deck
import com.dbottillo.mtgsearchfree.model.DeckCollection
import com.dbottillo.mtgsearchfree.model.MTGCard
import com.dbottillo.mtgsearchfree.util.Logger
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit

class DeckActivityPresenterTest {

    @Rule @JvmField val mockitoRule = MockitoJUnit.rule()!!

    @Mock lateinit var interactor: DecksInteractor
    @Mock internal lateinit var logger: Logger
    @Mock lateinit var view: DeckActivityView
    @Mock lateinit var deck: Deck
    @Mock lateinit var editedDeck: Deck
    @Mock lateinit var card: MTGCard
    @Mock lateinit var cards: DeckCollection
    @Mock lateinit var decks: List<Deck>
    @Mock lateinit var uri: Uri

    lateinit var underTest: DeckActivityPresenter

    @Before
    fun setup() {
        whenever(interactor.loadDeckById(2L)).thenReturn(Single.just(deck))
        underTest = DeckActivityPresenter(interactor, logger)
    }

    @Test
    fun `load should show empty screen if deck is empty`() {
        whenever(deck.name).thenReturn("name")
        whenever(deck.numberOfCards).thenReturn(0)

        underTest.init(view, 2L)

        verify(view).showEmptyScreen()
        verify(view).showTitle("name")
        verify(interactor).loadDeckById(2L)
        verifyNoMoreInteractions(view, interactor)
    }

    @Test
    fun `load should show deck if deck is not empty`() {
        whenever(deck.name).thenReturn("name")
        whenever(deck.numberOfCards).thenReturn(75)
        whenever(deck.sizeOfSideboard).thenReturn(15)

        underTest.init(view, 2L)

        verify(view).showDeck(deck)
        verify(view).showTitle("name (60/15)")
        verify(interactor).loadDeckById(2L)
        verifyNoMoreInteractions(view, interactor)
    }

    @Test
    fun `edit deck name, should call interactor and update view`() {
        underTest.init(view, 2L)
        Mockito.reset(view, interactor)
        whenever(editedDeck.name).thenReturn("new name")
        whenever(editedDeck.numberOfCards).thenReturn(75)
        whenever(editedDeck.sizeOfSideboard).thenReturn(15)
        whenever(interactor.editDeck(deck, "new name")).thenReturn(Single.just(editedDeck))

        underTest.editDeck("new name")

        verify(view).showTitle("new name (60/15)")
        verify(interactor).editDeck(deck, "new name")
        verifyNoMoreInteractions(view, interactor)
    }

    @Test
    fun `export deck, should call interactor and update view`() {
        underTest.init(view, 2L)
        Mockito.reset(view, interactor)
        whenever(interactor.exportDeck(deck)).thenReturn(Single.just(uri))

        underTest.exportDeck()

        verify(view).deckExported(uri)
        verify(interactor).exportDeck(deck)
        verifyNoMoreInteractions(view, interactor)
    }

    @Test
    fun `export deck, should react to a failure in the interactor`() {
        underTest.init(view, 2L)
        Mockito.reset(view, interactor)
        whenever(interactor.exportDeck(deck)).thenReturn(Single.error(Throwable("error")))

        underTest.exportDeck()

        verify(view).deckNotExported()
        verify(interactor).exportDeck(deck)
        verifyNoMoreInteractions(view, interactor)
    }

    @Test
    fun `copy deck, should call interactor and update view`() {
        underTest.init(view, 2L)
        Mockito.reset(view, interactor)
        whenever(interactor.copy(deck)).thenReturn(Single.just(decks))

        underTest.copyDeck()

        verify(view).deckCopied()
        verify(interactor).copy(deck)
        verifyNoMoreInteractions(view, interactor)
    }
}