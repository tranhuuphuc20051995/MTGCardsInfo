package com.dbottillo.mtgsearchfree.releasenote

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dbottillo.mtgsearchfree.ui.BasicActivity
import com.dbottillo.mtgsearchfree.util.hide
import com.dbottillo.mtgsearchfree.util.show
import com.google.android.material.appbar.MaterialToolbar
import dagger.android.AndroidInjection
import javax.inject.Inject

class ReleaseNoteActivity : BasicActivity(), ReleaseNoteView {

    @Inject lateinit var presenter: ReleaseNotePresenter

    private val releaseNoteList: RecyclerView by lazy(LazyThreadSafetyMode.NONE) { findViewById<RecyclerView>(R.id.release_note_list) }
    private val emptyView: TextView by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.empty_view) }

    override fun onCreate(bundle: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(bundle)

        setContentView(R.layout.activity_release_note)

        findViewById<MaterialToolbar>(R.id.toolbar).also {
            setSupportActionBar(it)
        }
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }

        presenter.init(this)
        presenter.load()

        releaseNoteList.setHasFixedSize(true)
        releaseNoteList.layoutManager = LinearLayoutManager(this)
        releaseNoteList.addItemDecoration(ReleaseNoteFooter(resources.getDimensionPixelSize(R.dimen.footer_height)))
    }

    override fun getPageTrack(): String? {
        return "/release-note"
    }

    override fun showError(message: String) {
        releaseNoteList.hide()
        emptyView.show()
        emptyView.text = message
    }

    override fun showItems(list: List<ReleaseNoteItem>) {
        emptyView.hide()
        releaseNoteList.show()
        releaseNoteList.adapter = ReleaseNoteAdapter(list)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}

internal class ReleaseNoteAdapter(val items: List<ReleaseNoteItem>) : RecyclerView.Adapter<ReleaseNoteHolder>() {
    override fun onBindViewHolder(holder: ReleaseNoteHolder, position: Int) {
        holder.title.text = items[position].title
        holder.text.text = items[position].lines.joinToString(separator = "\n") { "- $it" }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReleaseNoteHolder {
        return ReleaseNoteHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_release_note, parent, false))
    }

    override fun getItemCount() = items.size
}

internal class ReleaseNoteHolder(view: View) : RecyclerView.ViewHolder(view) {
    var title: TextView = view.findViewById(R.id.release_note_title)
    var text: TextView = view.findViewById(R.id.release_note_text)
}

internal class ReleaseNoteFooter(val height: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) {
            outRect.set(0, 0, 0, height)
        } else {
            outRect.setEmpty()
        }
    }
}
