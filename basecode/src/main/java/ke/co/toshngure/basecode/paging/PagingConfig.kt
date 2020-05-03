package ke.co.toshngure.basecode.paging

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import ke.co.toshngure.basecode.paging.adapter.ItemsAdapter
import ke.co.toshngure.basecode.paging.data.ItemRepository

data class PagingConfig<Model, FetchedDatabaseModel>(
        @LayoutRes val layoutRes: Int,
        val withDivider: Boolean = true,
        val diffUtilItemCallback: DiffUtil.ItemCallback<FetchedDatabaseModel>,
        val repository: ItemRepository<Model, FetchedDatabaseModel>,
        val noDataLayoutClickLister: View.OnClickListener? = null,
        val errorLayoutClickLister: View.OnClickListener? = null,
        val itemClickListener: ItemsAdapter.OnItemClickListener<FetchedDatabaseModel>? = null,
        val arguments: Bundle? = null,
        val itemAnimator: RecyclerView.ItemAnimator? = SlideInUpAnimator())