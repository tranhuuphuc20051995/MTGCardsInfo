package com.dbottillo.mtgsearchfree.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import com.dbottillo.mtgsearchfree.core.BuildConfig
import com.dbottillo.mtgsearchfree.model.CardsBucket
import com.dbottillo.mtgsearchfree.model.MTGCard
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader

class FileUtil(private val fileManager: FileManagerI) {

    @Throws(Exception::class)
    fun readFileContent(uri: Uri): CardsBucket {
        val inputStream = fileManager.loadUri(uri)
        val bucket: CardsBucket
        try {
            bucket = inputStream.readFileStream(uri.lastPathSegment)
        } catch (e: Exception) {
            inputStream.close()
            throw e
        }
        return bucket
    }

    private fun InputStream.readFileStream(deckName: String?): CardsBucket {
        val cards = ArrayList<MTGCard>()
        val br = BufferedReader(InputStreamReader(this, "UTF-8"))
        var name: String? = null
        var side = false
        var numberOfEmptyLines = 0
        var line = br.readLine()
        while (line != null) {
            if (line.startsWith("//")) {
                // title
                if (name == null) {
                    name = line.replace("//", "")
                }
            } else if (line.isEmpty()) {
                numberOfEmptyLines++
                // from here on all the cards belong to side
                if (numberOfEmptyLines >= 2) {
                    side = true
                }
            } else {
                val card = generateCard(line.replace("SB: ", ""))
                if (line.startsWith("SB: ")) {
                    card.isSideboard = true
                } else {
                    card.isSideboard = side
                }
                cards.add(card)
            }
            line = br.readLine()
        }
        br.close()
        return CardsBucket(name ?: deckName ?: "", cards)
    }

    private fun generateCard(line: String): MTGCard {
        val items = ArrayList(listOf(*line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        val first = items.removeAt(0)
        val rest = TextUtils.join(" ", items)
        val card = MTGCard()
        card.quantity = Integer.parseInt(first)
        card.setCardName(rest)
        return card
    }
}

private val Context.mtgSearchDirectory: File?
    get() {
        val root = File(this.getExternalFilesDir(null)!!.path)
        if (!root.exists()) {
            val created = root.mkdirs()
            if (!created) {
                return null
            }
        }
        return root
    }

fun Context.copyDbToSdCard(name: String): File? {
    LOG.e("copy db to sd card")
    try {
        val root = mtgSearchDirectory
        if (root == null) {
            LOG.e("mtg search director null")
        }
        if (root?.canWrite() == true) {
            val currentDB = this.getDatabasePath(name)
            val backupDB = File(root, name)

            if (currentDB.exists()) {
                val src = FileInputStream(currentDB)
                        .channel
                val dst = FileOutputStream(backupDB)
                        .channel
                dst.transferFrom(src, 0, src.size())
                src.close()
                dst.close()
                return backupDB
            } else {
                LOG.e("current db dont exist")
            }
        } else {
            LOG.e("sd card cannot be write")
        }
    } catch (e: Exception) {
        LOG.e("exception copy db: " + e.localizedMessage)
    }

    return null
}

fun Context.copyDbFromSdCard(name: String): Boolean {
    LOG.e("copy db to sd card")
    try {
        val root = mtgSearchDirectory ?: return false
        if (root.canWrite()) {
            val currentDB = getDatabasePath(name)
            val backupDB = File(root, name)

            if (backupDB.exists()) {
                val src = FileInputStream(backupDB)
                        .channel
                val dst = FileOutputStream(currentDB)
                        .channel
                dst.transferFrom(src, 0, src.size())
                src.close()
                dst.close()
                return true
            } else {
                LOG.e("backup db dont exist")
            }
        } else {
            LOG.e("sd card cannot be write")
        }
    } catch (e: Exception) {
        LOG.e("exception copy db: " + e.localizedMessage)
    }

    return false
}
