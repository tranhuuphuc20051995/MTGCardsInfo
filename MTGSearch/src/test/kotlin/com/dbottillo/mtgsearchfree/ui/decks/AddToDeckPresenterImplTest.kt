package com.dbottillo.mtgsearchfree.ui.decks

import android.os.Bundle
import com.dbottillo.mtgsearchfree.exceptions.MTGException
import com.dbottillo.mtgsearchfree.model.Deck
import com.dbottillo.mtgsearchfree.model.MTGCard
import com.dbottillo.mtgsearchfree.util.Logger
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit

class AddToDeckPresenterImplTest {

    @Rule
    @JvmField
    val mockitoRule = MockitoJUnit.rule()

    lateinit var underTest: AddToDeckPresenter

    @Mock lateinit var interactor: AddToDeckInteractor
    @Mock lateinit var logger: Logger
    @Mock lateinit var view: AddToDeckView
    @Mock lateinit var decks: List<Deck>
    @Mock lateinit var throwable: Throwable
    @Mock lateinit var deck: Deck
    @Mock lateinit var card: MTGCard
    @Mock lateinit var bundle: Bundle
    @Mock lateinit var mtgException: MTGException
    @Mock lateinit var addToDeckData: AddToDeckData

    @Before
    fun setUp() {
        `when`(bundle.getInt("card", -1)).thenReturn(4)
        `when`(addToDeckData.card).thenReturn(card)
        `when`(addToDeckData.decks).thenReturn(decks)
        `when`(card.name).thenReturn("Counterspell")
        `when`(interactor.init(4)).thenReturn(Single.just(addToDeckData))
        underTest = AddToDeckPresenterImpl(interactor, logger)
    }

    @Test
    fun `load decks should call interactor and update view`() {
        underTest.init(view, bundle)

        verify(interactor).init(4)
        verify(view).decksLoaded(decks)
        verify(view).setCardTitle("Counterspell")
        verifyNoMoreInteractions(view, interactor)
    }

    @Test
    fun `load decks should call interactor and show error it if fails`() {
        `when`(throwable.localizedMessage).thenReturn("error message")
        `when`(interactor.init(4)).thenReturn(Single.error(throwable))

        underTest.init(view, bundle)

        verify(interactor).init(4)
        verify(view).showError("error message")
        verifyNoMoreInteractions(view, interactor)
    }

    @Test
    fun `load decks should call interactor and show error it if fails with a mtg exception`() {
        `when`(mtgException.message).thenReturn("error message")
        `when`(interactor.init(4)).thenReturn(Single.error(mtgException))

        underTest.init(view, bundle)

        verify(interactor).init(4)
        verify(view).showError("error message")
        verifyNoMoreInteractions(view, interactor)
    }

    @Test
    fun `add card to existing deck should call interactor`() {
        underTest.init(view, bundle)
        reset(view, interactor)

        underTest.addCardToDeck(deck, 5, true)

        verify(interactor).addCard(deck, card, 5)
        verifyNoMoreInteractions(view, interactor)
    }

    @Test
    fun `add card to new deck should call interactor`() {
        underTest.init(view, bundle)
        reset(view, interactor)

        underTest.addCardToDeck("new deck", 5, false)

        verify(interactor).addCard("new deck", card, 5)
        verifyNoMoreInteractions(view, interactor)
    }

}