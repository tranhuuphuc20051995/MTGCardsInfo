package com.dbottillo.mtgsearchfree.ui.saved

import com.dbottillo.mtgsearchfree.interactors.SavedCardsInteractor
import com.dbottillo.mtgsearchfree.model.MTGCard
import com.dbottillo.mtgsearchfree.model.SavedCards
import com.dbottillo.mtgsearchfree.model.storage.GeneralPreferences
import com.dbottillo.mtgsearchfree.presenter.TestRunnerFactory
import com.dbottillo.mtgsearchfree.util.Logger
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnit

class SavedCardsPresenterImplTest {

    @Rule @JvmField
    var mockitoRule = MockitoJUnit.rule()

    @Mock lateinit var view: SavedCardsView
    @Mock lateinit var interactor: SavedCardsInteractor
    @Mock lateinit var logger: Logger
    @Mock lateinit var generalData: GeneralPreferences
    @Mock lateinit var cards: SavedCards
    @Mock lateinit var card: MTGCard

    lateinit var underTest: SavedCardsPresenter

    @Before
    fun setUp() {
        Mockito.`when`(interactor.load()).thenReturn(Observable.just(cards))
        Mockito.`when`(interactor.remove(card)).thenReturn(Observable.just(cards))
        underTest = SavedCardsPresenterImpl(interactor, TestRunnerFactory(), generalData, logger)
        underTest.init(view)
    }

    @Test
    fun load_shouldLaunchInteractor() {
        underTest.load()

        verify(interactor).load()
        verify(view).showCards(cards)
        verifyNoMoreInteractions(interactor, view)
    }

    @Test
    fun removeFromFavourite() {
        underTest.removeFromFavourite(card)

        verify(interactor).remove(card)
        verify(view).showCards(cards)
        verifyNoMoreInteractions(interactor, view)
    }

    @Test
    fun toggleCardTypeViewPreference_shouldSetGridIfIsList() {
        Mockito.`when`(generalData.isCardsShowTypeGrid).thenReturn(false)

        underTest.toggleCardTypeViewPreference()

        verify(generalData).setCardsShowTypeGrid()
        verify(view).showCardsGrid()
    }

    @Test
    fun toggleCardTypeViewPreference_shouldSetListIfIsGrid() {
        Mockito.`when`(generalData.isCardsShowTypeGrid).thenReturn(true)

        underTest.toggleCardTypeViewPreference()

        verify(generalData).setCardsShowTypeList()
        verify(view).showCardsList()
    }

}