package akhmedoff.usman.data.repository.source.videos

import akhmedoff.usman.data.api.VkApi
import akhmedoff.usman.data.model.ResponseVideo
import akhmedoff.usman.data.model.Video
import android.arch.paging.PositionalDataSource
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchDataSource(
    private val vkApi: VkApi,
    private val query: String,
    private val sort: Int?,
    private val hd: Int?,
    private val adult: Int?,
    private val filters: String?,
    private val searchOwn: Boolean?,
    private val longer: Long?,
    private val shorter: Long?
) : PositionalDataSource<Video>() {
    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Video>) {
        vkApi.searchVideos(
            query,
            sort,
            hd,
            adult,
            filters,
            searchOwn,
            params.startPosition.toLong(),
            longer,
            shorter
        ).enqueue(object : Callback<ResponseVideo> {

            override fun onFailure(call: Call<ResponseVideo>?, t: Throwable?) {
                Log.e(javaClass.simpleName, "ERROR: " + t.toString())
            }

            override fun onResponse(
                call: Call<ResponseVideo>?,
                response: Response<ResponseVideo>?
            ) {
                response?.body()?.let { responseVideo ->
                    callback.onResult(responseVideo.items)
                }
            }

        })
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
        val apiSource = vkApi.searchVideos(
            query,
            sort,
            hd,
            adult,
            filters,
            searchOwn,
            0,
            longer,
            shorter
        )
        try {
            val response = apiSource.execute()

            val items = response.body()?.items ?: emptyList()

            callback.onResult(items, 0)
        } catch (exception: Exception) {
            Log.e(javaClass.simpleName, exception.toString())
        }
    }

}