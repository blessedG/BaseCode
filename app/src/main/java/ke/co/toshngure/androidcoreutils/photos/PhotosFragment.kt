package ke.co.toshngure.androidcoreutils.photos

import android.os.Bundle
import android.view.View
import ke.co.toshngure.androidcoreutils.util.Extras
import ke.co.toshngure.androidcoreutils.R
import ke.co.toshngure.basecode.app.GlideApp
import ke.co.toshngure.basecode.paging.PagingConfig
import ke.co.toshngure.basecode.paging.PagingFragment
import ke.co.toshngure.basecode.paging.adapter.BaseItemViewHolder
import ke.co.toshngure.basecode.logging.BeeLog

class PhotosFragment : PagingFragment<Photo, Photo,Any>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BeeLog.i(TAG, "onCreate, arguments -> $arguments")
    }


    override fun getPagingConfig(): PagingConfig<Photo, Photo> {
        return PagingConfig(
            layoutRes = R.layout.item_photo,
            withDivider = false,
            diffUtilItemCallback = Photo.DIFF_UTIL_ITEM_CALLBACK,
            repository = PhotoRepository(arguments?.getLong(Extras.ALBUM_ID) ?: 1)
        )
    }


    override fun createItemViewHolder(itemView: View): BaseItemViewHolder<Photo> {
        return PhotoViewHolder(itemView, GlideApp.with(itemView.context))
    }

    companion object {
        private const val TAG = "PhotosFragment"
    }
}
