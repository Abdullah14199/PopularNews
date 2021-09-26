package com.haerul.popularnews

import com.haerul.popularnews.api.ApiClient.apiClient
import android.support.v7.app.AppCompatActivity
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener
import android.support.v7.widget.RecyclerView
import com.haerul.popularnews.models.Article
import com.haerul.popularnews.MainActivity
import android.support.v4.widget.SwipeRefreshLayout
import android.os.Bundle
import com.haerul.popularnews.R
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.DefaultItemAnimator
import com.haerul.popularnews.api.ApiInterface
import com.haerul.popularnews.api.ApiClient
import com.haerul.popularnews.models.News
import android.content.Intent
import com.haerul.popularnews.NewsDetailActivity
import android.support.v4.view.ViewCompat
import android.support.v4.app.ActivityOptionsCompat
import android.os.Build
import android.view.MenuInflater
import android.app.SearchManager
import android.support.v4.util.Pair
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class MainActivity : AppCompatActivity(), OnRefreshListener {
    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var articles: List<Article>? = ArrayList()
    private var adapter: Adapter? = null
    private val TAG = MainActivity::class.java.simpleName
    private var topHeadline: TextView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var errorLayout: RelativeLayout? = null
    private var errorImage: ImageView? = null
    private var errorTitle: TextView? = null
    private var errorMessage: TextView? = null
    private var btnRetry: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipe_refresh_layout.setOnRefreshListener(this)
        swipe_refresh_layout.setColorSchemeResources(R.color.colorAccent)
        topHeadline = findViewById(R.id.topheadelines)
        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView?.setLayoutManager(layoutManager)
        recyclerView?.setItemAnimator(DefaultItemAnimator())
        recyclerView?.setNestedScrollingEnabled(false)
        onLoadingSwipeRefresh("")
        errorLayout = findViewById(R.id.errorLayout)
        errorImage = findViewById(R.id.errorImage)
        errorTitle = findViewById(R.id.errorTitle)
        errorMessage = findViewById(R.id.errorMessage)
        btnRetry = findViewById(R.id.btnRetry)
    }

    fun LoadJson(keyword: String) {
        errorLayout!!.visibility = View.GONE
        swipeRefreshLayout!!.isRefreshing = true
        val apiInterface = apiClient!!.create(ApiInterface::class.java)
        val country = Utils.country
        val language = Utils.language
        val call: Call<News?>?
        call = if (keyword.length > 0) {
            apiInterface.getNewsSearch(keyword, language, "publishedAt", API_KEY)
        } else {
            apiInterface.getNews(country, API_KEY)
        }
        call!!.enqueue(object : Callback<News?> {
            override fun onResponse(call: Call<News?>, response: Response<News?>) {
                if (response.isSuccessful && response.body()!!.article != null) {
                    if (!articles!!.isEmpty()) {
                        articles!!.clear()
                    }
                    articles = response.body()!!.article
                    adapter = Adapter(articles, this@MainActivity)
                    recyclerView!!.adapter = adapter
                    adapter!!.notifyDataSetChanged()
                    initListener()
                    topHeadline!!.visibility = View.VISIBLE
                    swipeRefreshLayout!!.isRefreshing = false
                } else {
                    topHeadline!!.visibility = View.INVISIBLE
                    swipeRefreshLayout!!.isRefreshing = false
                    val errorCode: String
                    errorCode = when (response.code()) {
                        404 -> "404 not found"
                        500 -> "500 server broken"
                        else -> "unknown error"
                    }
                    showErrorMessage(
                        R.drawable.no_result,
                        "No Result",
                        """
                            Please Try Again!
                            $errorCode
                            """.trimIndent()
                    )
                }
            }
            override fun onFailure(call: Call<News?>, t: Throwable) {
                topHeadline!!.visibility = View.INVISIBLE
                swipeRefreshLayout!!.isRefreshing = false
                showErrorMessage(
                    R.drawable.oops,
                    "Oops..",
                    """
                        Network failure, Please Try Again
                        $t
                        """.trimIndent()
                )
            }
        })
    }

    private fun initListener() {
        adapter!!.setOnItemClickListener { view, position ->
            val imageView = view.findViewById<ImageView>(R.id.img)
            val intent = Intent(this@MainActivity, NewsDetailActivity::class.java)
            val article = articles!![position]
            intent.putExtra("url", article.url)
            intent.putExtra("title", article.title)
            intent.putExtra("img", article.urlToImage)
            intent.putExtra("date", article.publishedAt)
            intent.putExtra("source", article.source!!.name)
            intent.putExtra("author", article.author)
            val pair = Pair.create(imageView as View, ViewCompat.getTransitionName(imageView))
            val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this@MainActivity,
                pair
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                startActivity(intent, optionsCompat.toBundle())
            } else {
                startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        val searchMenuItem = menu.findItem(R.id.action_search)
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = "Search Latest News..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.length > 2) {
                    onLoadingSwipeRefresh(query)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Type more than two letters!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        searchMenuItem.icon.setVisible(false, false)
        return true
    }

    override fun onRefresh() {
        LoadJson("")
    }

    private fun onLoadingSwipeRefresh(keyword: String) {
        swipeRefreshLayout!!.post { LoadJson(keyword) }
    }

    private fun showErrorMessage(imageView: Int, title: String, message: String) {
        if (errorLayout!!.visibility == View.GONE) {
            errorLayout!!.visibility = View.VISIBLE
        }
        errorImage!!.setImageResource(imageView)
        errorTitle!!.text = title
        errorMessage!!.text = message
        btnRetry!!.setOnClickListener { onLoadingSwipeRefresh("") }
    }

    companion object {
        const val API_KEY = "c2f552b48ac944429aee2dc3fcaf1bb7"

    }
}

private fun <E> List<E>.clear() {

}
