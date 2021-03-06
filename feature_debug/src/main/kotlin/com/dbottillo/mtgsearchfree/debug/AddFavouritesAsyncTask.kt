package com.dbottillo.mtgsearchfree.debug

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import com.dbottillo.mtgsearchfree.database.CardDataSource
import com.dbottillo.mtgsearchfree.database.CardsInfoDbHelper
import com.dbottillo.mtgsearchfree.database.FavouritesDataSource
import com.dbottillo.mtgsearchfree.database.MTGCardDataSource
import com.dbottillo.mtgsearchfree.database.MTGDatabaseHelper
import com.google.gson.Gson
import java.lang.ref.WeakReference

/*
    This class is used only on debug to generate random favourites cards
 */
@Suppress("MagicNumber")
internal class AddFavouritesAsyncTask(context: Context) : AsyncTask<String, Void, ArrayList<Any>>() {

    private val context: WeakReference<Context> = WeakReference(context)

    override fun doInBackground(vararg params: String): ArrayList<Any> {
        val result = ArrayList<Any>()

        context.get()?.let {
            val databaseHelper = MTGDatabaseHelper(it)
            val cardsInfoDbHelper = CardsInfoDbHelper(it)
            val cardDataSource = CardDataSource(cardsInfoDbHelper.writableDatabase, Gson())
            val mtgCardDataSource = MTGCardDataSource(databaseHelper.readableDatabase, cardDataSource)

            val favouritesDataSource = FavouritesDataSource(cardsInfoDbHelper.writableDatabase, cardDataSource)
            favouritesDataSource.clear()

            mtgCardDataSource.getRandomCard(60).forEach {
                favouritesDataSource.saveFavourites(it)
            }

            cardsInfoDbHelper.close()
            databaseHelper.close()
            return result
        } ?: return arrayListOf()
    }

    override fun onPostExecute(result: ArrayList<Any>) {
        context.get()?.let {
            Toast.makeText(it, "finished", Toast.LENGTH_SHORT).show()
        }
    }
}
