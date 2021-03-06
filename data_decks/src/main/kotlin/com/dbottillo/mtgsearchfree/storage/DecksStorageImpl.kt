package com.dbottillo.mtgsearchfree.storage

import android.net.Uri
import com.dbottillo.mtgsearchfree.exceptions.ExceptionCode
import com.dbottillo.mtgsearchfree.exceptions.MTGException
import com.dbottillo.mtgsearchfree.model.CardsBucket
import com.dbottillo.mtgsearchfree.model.Deck
import com.dbottillo.mtgsearchfree.model.DeckCollection
import com.dbottillo.mtgsearchfree.model.MTGCard
import com.dbottillo.mtgsearchfree.database.DeckDataSource
import com.dbottillo.mtgsearchfree.util.FileUtil
import com.dbottillo.mtgsearchfree.util.Logger
import javax.inject.Inject

class DecksStorageImpl @Inject constructor(
    private val fileUtil: FileUtil,
    private val deckDataSource: DeckDataSource,
    private val generalData: GeneralData,
    private val logger: Logger
) : DecksStorage {
    init {
        logger.d("created")
    }

    override fun load(): List<Deck> {
        logger.d()
        return deckDataSource.decks
    }

    override fun addDeck(name: String): List<Deck> {
        logger.d("add $name")
        deckDataSource.addDeck(name)
        return load()
    }

    override fun copy(deck: Deck): List<Deck> {
        logger.d("copy $deck")
        deckDataSource.copy(deck)
        return load()
    }

    override fun deleteDeck(deck: Deck): List<Deck> {
        logger.d("delete $deck")
        deckDataSource.deleteDeck(deck)
        return load()
    }

    override fun loadDeck(deckId: Long): DeckCollection {
        logger.d("loadDeck $deckId")
        val cards = deckDataSource.getCards(deckId)
        return DeckCollection().addCards(cards)
    }

    override fun loadDeckById(deckId: Long): Deck {
        logger.d("loadDeckById $deckId")
        return deckDataSource.getDeck(deckId)
    }

    override fun editDeck(deck: Deck, name: String): Deck {
        logger.d("edit $deck with $name")
        deckDataSource.renameDeck(deck.id, name)
        return deckDataSource.getDeck(deck.id)
    }

    override fun addCard(deck: Deck, card: MTGCard, quantity: Int): DeckCollection {
        logger.d("add $quantity $card to $deck")
        deckDataSource.addCardToDeck(deck.id, card, quantity)
        generalData.lastDeckSelected = deck.id
        return loadDeck(deck.id)
    }

    override fun addCard(name: String, card: MTGCard, quantity: Int): DeckCollection {
        logger.d("add $quantity $card with new deck name $name")
        val deckId = deckDataSource.addDeck(name)
        deckDataSource.addCardToDeck(deckId, card, quantity)
        generalData.lastDeckSelected = deckId
        return DeckCollection().addCards(deckDataSource.getCards(deckId))
    }

    override fun removeCard(deck: Deck, card: MTGCard): DeckCollection {
        logger.d("remove $card from $deck")
        deckDataSource.addCardToDeck(deck.id, card, -1)
        return loadDeck(deck.id)
    }

    override fun removeAllCard(deck: Deck, card: MTGCard): DeckCollection {
        logger.d("remove all $card from $deck")
        deckDataSource.removeCardFromDeck(deck.id, card)
        return loadDeck(deck.id)
    }

    @Throws(MTGException::class)
    override fun importDeck(uri: Uri): List<Deck> {
        val bucket: CardsBucket?
        try {
            bucket = fileUtil.readFileContent(uri)
        } catch (e: Exception) {
            throw MTGException(ExceptionCode.DECK_NOT_IMPORTED, "file not valid")
        }
        deckDataSource.addDeck(bucket)
        return deckDataSource.decks
    }

    override fun moveCardFromSideboard(deck: Deck, card: MTGCard, quantity: Int): DeckCollection {
        logger.d("move [$quantity]$card from sideboard of$deck")
        deckDataSource.moveCardFromSideBoard(deck.id, card, quantity)
        return loadDeck(deck.id)
    }

    override fun moveCardToSideboard(deck: Deck, card: MTGCard, quantity: Int): DeckCollection {
        logger.d("move [$quantity]$card to sideboard of$deck")
        deckDataSource.moveCardToSideBoard(deck.id, card, quantity)
        return loadDeck(deck.id)
    }
}
