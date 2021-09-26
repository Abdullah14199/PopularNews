package com.haerul.popularnews.models

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import com.haerul.popularnews.models.Article

class Source {
    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null
}