package akhmedoff.usman.thevt.ui.album

import akhmedoff.usman.data.model.Album
import akhmedoff.usman.data.model.CatalogItem
import akhmedoff.usman.data.model.Video
import akhmedoff.usman.data.utils.getAlbumRepository
import akhmedoff.usman.thevt.R
import akhmedoff.usman.thevt.Router
import akhmedoff.usman.thevt.ui.video.VideoActivity
import akhmedoff.usman.thevt.ui.view.MarginItemDecorator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_album.*

private const val ALBUM_ID = "album_id"
private const val ALBUM_OWNER_ID = "album_owner_id"
private const val ALBUM_NAME = "album_name"
private const val TRANSITION_NAME = "transition_name"

class AlbumFragment : Fragment(), AlbumContract.View {

    companion object {
        fun getFragment(item: CatalogItem, transitionName: String) =
                getFragment(item.id.toString(), item.ownerId.toString(), item.title, transitionName)

        fun getFragment(item: Album, transitionName: String) =
                getFragment(item.id.toString(), item.ownerId.toString(), item.title, transitionName)

        private fun getFragment(id: String,
                                ownerId: String,
                                title: String,
                                transitionName: String): AlbumFragment {
            val arguments = Bundle()

            arguments.putString(ALBUM_ID, id)
            arguments.putString(ALBUM_OWNER_ID, ownerId)
            arguments.putString(ALBUM_NAME, title)
            arguments.putString(TRANSITION_NAME, transitionName)

            val fragment = AlbumFragment()
            fragment.arguments = arguments

            return fragment
        }
    }

    private val adapter: AlbumRecyclerAdapter by lazy(mode = LazyThreadSafetyMode.NONE) {
        val adapter = AlbumRecyclerAdapter { video, view -> showVideo(video, view) }

        adapter.setHasStableIds(true)
        return@lazy adapter
    }

    override lateinit var albumPresenter: AlbumContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        albumPresenter = AlbumPresenter(this, getAlbumRepository(context!!))
        albumPresenter.onCreated()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.activity_album, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appbar.transitionName = arguments?.getString(TRANSITION_NAME)
        album_videos_recycler.addItemDecoration(
                MarginItemDecorator(
                        1,
                        resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin)
                )
        )
        album_videos_recycler.adapter = adapter

    }

    override fun showVideos(items: PagedList<Video>) = adapter.submitList(items)

    override fun showAlbumTitle(title: String) {
        album_title.text = title
    }

    override fun showAlbumImage(poster: String) {}

    override fun showVideo(video: Video, view: View) {
        val intent = VideoActivity.getInstance(video, ViewCompat.getTransitionName(view), context!!)

        Router.startActivityWithTransition(activity!!, intent, view)
    }

    override fun setAdded(isAdded: Boolean) {
    }

    override fun getAlbumId(): String? = arguments?.getString(ALBUM_ID)

    override fun getAlbumOwnerId(): String? = arguments?.getString(ALBUM_OWNER_ID)

    override fun getAlbumTitle(): String? = arguments?.getString(ALBUM_NAME)
}