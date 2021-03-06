package com.haerul.popularnews

import android.support.v7.app.AppCompatActivity
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener
import android.support.design.widget.AppBarLayout
import android.os.Bundle
import com.haerul.popularnews.R
import android.support.design.widget.CollapsingToolbarLayout
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import kotlinx.android.synthetic.main.activity_news_detail.*
import java.lang.Exception

class NewsDetailActivity : AppCompatActivity(), OnOffsetChangedListener {
    private var imageView: ImageView? = null
    private var appbar_title: TextView? = null
    private var appbar_subtitle: TextView? = null
    private var date: TextView? = null
    private var time: TextView? = null
    private var title: TextView? = null
    private var isHideToolbarView = false
    private var date_behavior: FrameLayout? = null
    private var titleAppbar: LinearLayout? = null
    private var appBarLayout: AppBarLayout? = null
    private var toolbar: Toolbar? = null
    private var mUrl: String? = null
    private var mImg: String? = null
    private var mTitle: String? = null
    private var mDate: String? = null
    private var mSource: String? = null
    private var mAuthor: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val collapsingToolbarLayout = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        collapsingToolbarLayout.title = ""
        appBarLayout = findViewById(R.id.appbar)
        appbar.addOnOffsetChangedListener(this)
        date_behavior = findViewById(R.id.date_behavior)
        titleAppbar = findViewById(R.id.title_appbar)
        imageView = findViewById(R.id.backdrop)
        appbar_title = findViewById(R.id.title_on_appbar)
        appbar_subtitle = findViewById(R.id.subtitle_on_appbar)
        date = findViewById(R.id.date)
        time = findViewById(R.id.time)
        title = findViewById(R.id.title)
        val intent = intent
        mUrl = intent.getStringExtra("url")
        mImg = intent.getStringExtra("img")
        mTitle = intent.getStringExtra("title")
        mDate = intent.getStringExtra("date")
        mSource = intent.getStringExtra("source")
        mAuthor = intent.getStringExtra("author")
        val requestOptions = RequestOptions()
        requestOptions.error(Utils.randomDrawbleColor)
        Glide.with(this)
            .load(mImg)
            .apply(requestOptions)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(backdrop)
        title_on_appbar.setText(mSource)
        subtitle_on_appbar.setText(mUrl)
        date?.setText(Utils.DateFormat(mDate))
        title?.setText(mTitle)
        val author: String
        author = if (mAuthor != null) {
            " \u2022 $mAuthor"
        } else {
            ""
        }
        time?.setText(mSource + author + " \u2022 " + Utils.DateToTimeFormat(mDate))
        initWebView(mUrl)
    }

    private fun initWebView(url: String?) {
        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.loadsImagesAutomatically = true
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url!!)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        supportFinishAfterTransition()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val maxScroll = appBarLayout.totalScrollRange
        val percentage = Math.abs(verticalOffset).toFloat() / maxScroll.toFloat()
        if (percentage == 1f && isHideToolbarView) {
            date_behavior!!.visibility = View.GONE
            titleAppbar!!.visibility = View.VISIBLE
            isHideToolbarView = !isHideToolbarView
        } else if (percentage < 1f && !isHideToolbarView) {
            date_behavior!!.visibility = View.VISIBLE
            titleAppbar!!.visibility = View.GONE
            isHideToolbarView = !isHideToolbarView
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_news, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.view_web) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(mUrl)
            startActivity(i)
            return true
        } else if (id == R.id.share) {
            try {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plan"
                i.putExtra(Intent.EXTRA_SUBJECT, mSource)
                val body = "$mTitle\n$mUrl\nShare from the News App\n"
                i.putExtra(Intent.EXTRA_TEXT, body)
                startActivity(Intent.createChooser(i, "Share with :"))
            } catch (e: Exception) {
                Toast.makeText(this, "Hmm.. Sorry, \nCannot be share", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}