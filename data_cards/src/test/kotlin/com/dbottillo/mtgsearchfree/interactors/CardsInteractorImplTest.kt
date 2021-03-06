package com.dbottillo.mtgsearchfree.interactors

import android.graphics.Bitmap
import android.net.Uri
import com.dbottillo.mtgsearchfree.interactor.SchedulerProvider
import com.dbottillo.mtgsearchfree.model.CardPrice
import com.dbottillo.mtgsearchfree.model.CardsCollection
import com.dbottillo.mtgsearchfree.model.MKMCardPrice
import com.dbottillo.mtgsearchfree.model.MTGCard
import com.dbottillo.mtgsearchfree.model.MTGSet
import com.dbottillo.mtgsearchfree.model.PriceProvider
import com.dbottillo.mtgsearchfree.model.SearchParams
import com.dbottillo.mtgsearchfree.model.TCGCardPrice
import com.dbottillo.mtgsearchfree.repository.CardRepository
import com.dbottillo.mtgsearchfree.storage.CardsStorage
import com.dbottillo.mtgsearchfree.util.FileManager
import com.dbottillo.mtgsearchfree.util.Logger
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit

class CardsInteractorImplTest {

    @Rule @JvmField var mockitoRule = MockitoJUnit.rule()!!

    lateinit var underTest: CardsInteractor

    @Mock lateinit var cardsStorage: CardsStorage
    @Mock lateinit var set: MTGSet
    @Mock lateinit var searchParams: SearchParams
    @Mock lateinit var card: MTGCard
    @Mock lateinit var otherSideCard: MTGCard
    @Mock lateinit var logger: Logger
    @Mock lateinit var lukcyCardsCollection: CardsCollection
    @Mock lateinit var setCollection: CardsCollection
    @Mock lateinit var searchCardsCollection: CardsCollection
    @Mock lateinit var schedulerProvider: SchedulerProvider
    @Mock lateinit var fileManager: FileManager
    @Mock lateinit var cardRepository: CardRepository

    private val favCards = listOf(MTGCard(3), MTGCard(4))

    @Before
    fun setup() {
        whenever(schedulerProvider.io()).thenReturn(Schedulers.trampoline())
        whenever(schedulerProvider.ui()).thenReturn(Schedulers.trampoline())
        underTest = CardsInteractorImpl(cardsStorage, fileManager, schedulerProvider, logger, cardRepository)
    }

    @Test
    fun `get lucky cards should call storage and return observable`() {
        whenever(cardsStorage.getLuckyCards(2)).thenReturn(lukcyCardsCollection)
        val testSubscriber = TestObserver<CardsCollection>()

        underTest.getLuckyCards(2).subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(lukcyCardsCollection)
        verify(cardsStorage).getLuckyCards(2)
        verify(schedulerProvider).io()
        verify(schedulerProvider).ui()
        verifyNoMoreInteractions(cardsStorage, schedulerProvider, cardRepository)
    }

    @Test
    fun `get favourites should call storage and return observable`() {
        whenever(cardsStorage.getFavourites()).thenReturn(favCards)
        val testSubscriber = TestObserver<List<MTGCard>>()

        underTest.getFavourites().subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(favCards)
        verify(cardsStorage).getFavourites()
        verify(schedulerProvider).io()
        verify(schedulerProvider).ui()
        verifyNoMoreInteractions(cardsStorage, schedulerProvider, cardRepository)
    }

    @Test
    fun `save card as favourites should call storage and return observable`() {
        underTest.saveAsFavourite(card)

        verify(schedulerProvider).io()
        verify(schedulerProvider).ui()
        verify(cardsStorage).saveAsFavourite(card)
        verifyNoMoreInteractions(cardsStorage, schedulerProvider, cardRepository)
    }

    @Test
    fun `remove card as favourites should call storage and return observable`() {
        underTest.removeFromFavourite(card)

        verify(schedulerProvider).io()
        verify(schedulerProvider).ui()
        verify(cardsStorage).removeFromFavourite(card)
        verifyNoMoreInteractions(cardsStorage, schedulerProvider, cardRepository)
    }

    @Test
    fun `load set should call storage and return observable`() {
        whenever(cardsStorage.load(set)).thenReturn(setCollection)
        val testSubscriber = TestObserver<CardsCollection>()

        underTest.loadSet(set).subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(setCollection)
        verify(cardsStorage).load(set)
        verify(schedulerProvider).io()
        verify(schedulerProvider).ui()
        verifyNoMoreInteractions(cardsStorage, schedulerProvider, cardRepository)
    }

    @Test
    fun `load ifs of favourites should call storage and return observable`() {
        val idFavs = intArrayOf(6, 7, 8)
        whenever(cardsStorage.loadIdFav()).thenReturn(idFavs)
        val testSubscriber = TestObserver<IntArray>()

        underTest.loadIdFav().subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(idFavs)
        verify(cardsStorage).loadIdFav()
        verify(schedulerProvider).io()
        verify(schedulerProvider).ui()
        verifyNoMoreInteractions(cardsStorage, schedulerProvider, cardRepository)
    }

    @Test
    fun `do search should call storage and return observable`() {
        whenever(cardsStorage.doSearch(searchParams)).thenReturn(searchCardsCollection)
        val testSubscriber = TestObserver<CardsCollection>()

        underTest.doSearch(searchParams).subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(searchCardsCollection)
        verify(cardsStorage).doSearch(searchParams)
        verify(schedulerProvider).io()
        verify(schedulerProvider).ui()
        verifyNoMoreInteractions(cardsStorage, schedulerProvider, cardRepository)
    }

    @Test
    fun `load card with id should call storage and return observable`() {
        whenever(cardsStorage.loadCardById(5)).thenReturn(card)
        val testSubscriber = TestObserver<MTGCard>()

        underTest.loadCardById(5).subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(card)
        verify(cardsStorage).loadCardById(5)
        verify(schedulerProvider).io()
        verify(schedulerProvider).ui()
        verifyNoMoreInteractions(cardsStorage, schedulerProvider, cardRepository)
    }

    @Test
    fun `load other side of card should call storage and return observable`() {
        whenever(cardsStorage.loadOtherSide(card)).thenReturn(otherSideCard)
        val testSubscriber = TestObserver<MTGCard>()

        underTest.loadOtherSideCard(card).subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(otherSideCard)
        verify(cardsStorage).loadOtherSide(card)
        verify(schedulerProvider).io()
        verify(schedulerProvider).ui()
        verifyNoMoreInteractions(cardsStorage, schedulerProvider, cardRepository)
    }

    @Test
    fun `get artwork uri should defer to file loader`() {
        val bitmap = mock<Bitmap>()
        val uri = mock<Uri>()
        whenever(fileManager.saveBitmapToFile(bitmap)).thenReturn(uri)
        val testSubscriber = TestObserver<Uri>()

        underTest.getArtworkUri(bitmap).subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(uri)
        verify(fileManager).saveBitmapToFile(bitmap)
        verify(schedulerProvider).io()
        verify(schedulerProvider).ui()
        verifyNoMoreInteractions(cardsStorage, schedulerProvider, cardRepository, fileManager)
    }

    @Test
    fun `should fetch TCG price from repository`() {
        val price = mock<TCGCardPrice>()
        whenever(cardRepository.fetchPriceTCG(card)).thenReturn(Single.just(price))
        val testSubscriber = TestObserver<CardPrice>()

        underTest.fetchPrice(card, PriceProvider.TCG).subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(price)
        verify(cardRepository).fetchPriceTCG(card)
        verify(schedulerProvider).io()
        verify(schedulerProvider).ui()
        verifyNoMoreInteractions(cardsStorage, schedulerProvider, cardRepository)
    }

    @Test
    fun `should fetch MKM price from repository`() {
        val price = mock<MKMCardPrice>()
        whenever(cardRepository.fetchPriceMKM(card)).thenReturn(Single.just(price))
        val testSubscriber = TestObserver<CardPrice>()

        underTest.fetchPrice(card, PriceProvider.MKM).subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(price)
        verify(cardRepository).fetchPriceMKM(card)
        verify(schedulerProvider).io()
        verify(schedulerProvider).ui()
        verifyNoMoreInteractions(cardsStorage, schedulerProvider, cardRepository)
    }
}