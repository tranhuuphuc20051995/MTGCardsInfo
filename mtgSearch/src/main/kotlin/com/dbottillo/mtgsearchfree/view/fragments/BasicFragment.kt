package com.dbottillo.mtgsearchfree.view.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import com.dbottillo.mtgsearchfree.base.MTGApp
import com.dbottillo.mtgsearchfree.communication.events.BaseEvent
import com.dbottillo.mtgsearchfree.helper.TrackingHelper
import com.dbottillo.mtgsearchfree.view.activities.BasicActivity
import de.greenrobot.event.EventBus

abstract class BasicFragment : DialogFragment() {

    val PREFS_NAME = "Filter"
    val PREF_SHOW_IMAGE = "show_image"
    val PREF_SCREEN_ON = "screen_on"
    val PREF_TWO_HG_ENABLED = "two_hg"
    val PREF_SORT_WUBRG = "sort_wubrg"

    private var activity: BasicActivity? = null
    protected var isPortrait: Boolean = false

    protected var bus = EventBus.getDefault()

    var sharedPreferences: SharedPreferences? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.activity = context as BasicActivity?
        sharedPreferences = context?.getSharedPreferences(PREFS_NAME, 0)
        val res = activity!!.resources
        isPortrait = res.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onPause() {
        super.onPause()
        bus.unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        val refWatcher = MTGApp.refWatcher
        refWatcher?.watch(this)
    }

    protected fun setActionBarTitle(title: String) {
        if (activity!!.supportActionBar != null) {
            activity!!.supportActionBar!!.title = title
        }
    }

    protected fun openPlayStore() {
        TrackingHelper.getInstance(activity!!.applicationContext).trackEvent(TrackingHelper.UA_CATEGORY_POPUP, TrackingHelper.UA_ACTION_OPEN, "play_store")
        val appPackageName = "com.dbottillo.mtgsearch"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)))
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)))
        }

    }

    override fun onResume() {
        super.onResume()
        if (getPageTrack() != null) {
            TrackingHelper.getInstance(activity!!.applicationContext).trackPage(getPageTrack())
        }
        bus.registerSticky(this)
    }

    abstract fun getPageTrack(): String?

    fun getApp(): MTGApp {
        return activity!!.application as MTGApp
    }

    fun onBackPressed(): Boolean {
        return false
    }

    fun onEvent(event: BaseEvent<Any>) {

    }
}