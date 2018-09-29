package com.dbottillo.mtgsearchfree.model.database

import android.database.sqlite.SQLiteDatabase
import com.dbottillo.mtgsearchfree.model.MTGCard
import com.dbottillo.mtgsearchfree.model.MTGSet
import com.dbottillo.mtgsearchfree.model.SearchParams
import com.dbottillo.mtgsearchfree.util.LOG
import java.util.*

class MTGCardDataSource(private val database: SQLiteDatabase,
                        private val cardDataSource: CardDataSource) {

    internal enum class STANDARD(var setId: Int, var set: String) {
        GUILDS_OF_RAVNICA(1, "Guilds of Ravnica"),
        CORE_19(3, "Core Set 2019"),
        DOMINARIA(8, "Dominaria"),
        RIVALS_OF_IXALAN(10, "Rivals of Ixalan"),
        IXALAN(15, "Ixalan");

        companion object {

            val setIds: Array<String>
                get() {
                    return STANDARD.values().map { it.setId.toString() }.toTypedArray()
                }
        }
    }

    fun getSet(set: MTGSet): List<MTGCard> {
        LOG.d("get set  " + set.toString())
        val query = "SELECT * FROM " + CardDataSource.TABLE + " WHERE " + CardDataSource.COLUMNS.SET_CODE.noun + " = '" + set.code + "';"
        LOG.query(query, set.code!!)

        val cards = ArrayList<MTGCard>()
        val cursor = database.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val card = cardDataSource.fromCursor(cursor)
                card.belongsTo(set)
                cards.add(card)
                cursor.moveToNext()
            }
        }
        cursor.close()
        return cards
    }

    fun searchCards(searchParams: SearchParams): List<MTGCard> {
        LOG.d("search cards  " + searchParams.toString())
        val queryComposer = QueryComposer("SELECT * FROM " + CardDataSource.TABLE)
        queryComposer.addLikeParam(CardDataSource.COLUMNS.NAME.noun, searchParams.name.trim { it <= ' ' }.toLowerCase(Locale.getDefault()))
        if (searchParams.types.isNotEmpty()) {
            val types = searchParams.types.split(" ").toTypedArray()
            queryComposer.addMultipleParam(CardDataSource.COLUMNS.TYPE.noun, "LIKE", "AND", *types)
        }
        queryComposer.addLikeParam(CardDataSource.COLUMNS.TEXT.noun, searchParams.text.trim { it <= ' ' })
        queryComposer.addCMCParam(searchParams.cmc)
        queryComposer.addPTParam(CardDataSource.COLUMNS.POWER.noun, searchParams.power)
        queryComposer.addPTParam(CardDataSource.COLUMNS.TOUGHNESS.noun, searchParams.tough)
        var colorsOperator = "OR"
        if (searchParams.isNoMulti) {
            queryComposer.addParam(CardDataSource.COLUMNS.MULTICOLOR.noun, "==", "0")
            //colorsOperator = "OR";
        }
        if (searchParams.onlyMulti() || searchParams.isOnlyMultiNoOthers) {
            queryComposer.addParam(CardDataSource.COLUMNS.MULTICOLOR.noun, "==", "1")
        }
        if (searchParams.isOnlyMultiNoOthers) {
            colorsOperator = "AND"
        }
        if (searchParams.setId > 0) {
            queryComposer.addParam(CardDataSource.COLUMNS.SET_ID.noun, "==", searchParams.setId)
        }
        if (searchParams.setId == -2) {
            queryComposer.addMultipleParam(CardDataSource.COLUMNS.SET_ID.noun, "==", "OR", *STANDARD.setIds)
        }
        if (searchParams.atLeastOneColor()) {
            val colors = ArrayList<String>()
            if (searchParams.isWhite) {
                colors.add("W")
            }
            if (searchParams.isBlue) {
                colors.add("U")
            }
            if (searchParams.isBlack) {
                colors.add("B")
            }
            if (searchParams.isRed) {
                colors.add("R")
            }
            if (searchParams.isGreen) {
                colors.add("G")
            }
            queryComposer.addMultipleParam(CardDataSource.COLUMNS.MANA_COST.noun, "LIKE", colorsOperator, *Arrays.copyOf<String, Any>(colors.toTypedArray(), colors.size, Array<String>::class.java))
        }
        if (searchParams.atLeastOneRarity()) {
            val rarities = ArrayList<String>()
            if (searchParams.isCommon) {
                rarities.add("Common")
            }
            if (searchParams.isUncommon) {
                rarities.add("Uncommon")
            }
            if (searchParams.isRare) {
                rarities.add("Rare")
            }
            if (searchParams.isMythic) {
                rarities.add("Mythic Rare")
            }
            queryComposer.addMultipleParam(CardDataSource.COLUMNS.RARITY.noun, "==", "OR", *Arrays.copyOf<String, Any>(rarities.toTypedArray(), rarities.size, Array<String>::class.java))
        }
        if (searchParams.isLand) {
            queryComposer.addParam(CardDataSource.COLUMNS.LAND.noun, "==", 1)
        }
        queryComposer.append("ORDER BY " + CardDataSource.COLUMNS.MULTIVERSE_ID.noun + " DESC LIMIT " + LIMIT)

        val output = queryComposer.build()
        val sel = Arrays.copyOf<String, Any>(output.selection.toTypedArray(), output.selection.size, Array<String>::class.java)
        LOG.query(output.query, *sel)

        val cursor = database.rawQuery(output.query, sel)

        val cards = ArrayList<MTGCard>()
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val card = cardDataSource.fromCursor(cursor)
                cards.add(card)
                cursor.moveToNext()
            }
        }
        cursor.close()
        return cards
    }


    fun getRandomCard(number: Int): List<MTGCard> {
        LOG.d("get random card  $number")
        val query = "SELECT * FROM " + CardDataSource.TABLE + " ORDER BY RANDOM() LIMIT " + number
        LOG.query(query)
        val cards = ArrayList<MTGCard>(number)
        val cursor = database.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                cards.add(cardDataSource.fromCursor(cursor))
                cursor.moveToNext()
            }
        }
        cursor.close()
        return cards
    }

    fun searchCard(name: String): MTGCard? {
        LOG.d("search card <$name>")
        val query = ("SELECT * FROM " + CardDataSource.TABLE + " WHERE " + CardDataSource.COLUMNS.NAME.noun + "=?")
        val selection = arrayOf(name)
        LOG.query(query)
        val cursor = database.rawQuery(query, selection)
        var card: MTGCard? = null
        if (cursor.moveToFirst()) {
            card = cardDataSource.fromCursor(cursor)
        }
        cursor.close()
        return card
    }

    fun searchCard(multiverseid: Int): MTGCard? {
        LOG.d("search card <$multiverseid>")
        val query = ("SELECT * FROM " + CardDataSource.TABLE + " WHERE " + CardDataSource.COLUMNS.MULTIVERSE_ID.noun + "=?")
        val selection = arrayOf(multiverseid.toString())
        LOG.query(query)
        val cursor = database.rawQuery(query, selection)
        var card: MTGCard? = null
        if (cursor.moveToFirst()) {
            card = cardDataSource.fromCursor(cursor)
        }
        cursor.close()
        return card
    }

    fun searchCardById(id: Int): MTGCard? {
        LOG.d("search card <$id>")
        val query = "SELECT * FROM " + CardDataSource.TABLE + " WHERE " + "_id=?"
        val selection = arrayOf(id.toString())
        LOG.query(query)
        val cursor = database.rawQuery(query, selection)
        var card: MTGCard? = null
        if (cursor.moveToFirst()) {
            card = cardDataSource.fromCursor(cursor)
        }
        cursor.close()
        return card
    }
}

private const val LIMIT = 400
