package com.dbottillo.mtgsearchfree.model

import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit

class CardsBucketTest {

    @Rule @JvmField var mockitoRule = MockitoJUnit.rule()

    @Mock lateinit var set: MTGSet

    private lateinit var cardsSetBucket: CardsBucket
    private lateinit var genericBucket: CardsBucket

    private val setCards = listOf(MTGCard(5), MTGCard(6))
    private val genericCards = listOf(MTGCard(8), MTGCard(9))

    @Before
    fun setup() {
        whenever(set.name).thenReturn("Zendikar")
        cardsSetBucket = CardsBucket(set, setCards)
        genericBucket = CardsBucket("fav", genericCards)
    }

    @Test
    @Throws(Exception::class)
    fun testGetCards() {
        var cards = cardsSetBucket.cards
        assertThat(cards, `is`(setCards))
        cards = genericBucket.cards
        assertThat(cards, `is`(genericCards))
    }

    @Test
    @Throws(Exception::class)
    fun testIsValid() {
        assertTrue(cardsSetBucket.isValid(set.name))
        assertFalse(cardsSetBucket.isValid("fav"))
        assertFalse(genericBucket.isValid(set.name))
        assertTrue(genericBucket.isValid("fav"))
    }

    @Test
    @Throws(Exception::class)
    fun testSetCards() {
        cardsSetBucket.cards = genericCards
        assertThat(cardsSetBucket.cards, `is`(genericCards))
    }

    @Test
    @Throws(Exception::class)
    fun testGetKey() {
        assertThat(cardsSetBucket.key, `is`(set.name))
        assertThat(genericBucket.key, `is`("fav"))
    }
}