/*
 * Copyright (c) 2018. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pti.tipapers.ui.adapters

import android.view.ViewGroup
import ca.allanwang.kau.utils.inflate
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.pti.tipapers.R
import com.pti.tipapers.data.models.Wallpaper
import com.pti.tipapers.helpers.extensions.jfilter
import com.pti.tipapers.helpers.glide.loadPicture
import com.pti.tipapers.helpers.utils.MAX_WALLPAPERS_LOAD
import com.pti.tipapers.ui.adapters.viewholders.TiPapersViewClickListener
import com.pti.tipapers.ui.adapters.viewholders.WallpaperHolder
import jahirfiquitiva.libs.kext.ui.adapters.RecyclerViewListAdapter
import java.util.Collections

class WallpapersAdapter(
    private val manager: RequestManager?,
    private val provider: ViewPreloadSizeProvider<Wallpaper>,
    private val fromFavorites: Boolean,
    private val showFavIcon: Boolean,
    private val listener: TiPapersViewClickListener<Wallpaper, WallpaperHolder>
                       ) :
    RecyclerViewListAdapter<Wallpaper, WallpaperHolder>(
        if (fromFavorites) -1 else MAX_WALLPAPERS_LOAD),
    ListPreloader.PreloadModelProvider<Wallpaper> {
    
    var favorites = ArrayList<Wallpaper>()
        private set(value) {
            field.clear()
            field.addAll(value)
        }
    
    fun updateFavorites(newFavs: ArrayList<Wallpaper>) {
        if (fromFavorites) {
            favorites = newFavs
            notifyDataSetChanged()
        } else {
            val modified = getModifiedItems(favorites, newFavs)
            favorites = newFavs
            modified.forEach { notifyItemChanged(list.indexOf(it)) }
        }
    }
    
    override fun doBind(holder: WallpaperHolder, position: Int, shouldAnimate: Boolean) {
        val item = list[position]
        holder.setItem(
            manager, provider, item, fromFavorites || favorites.contains(item),
            listener)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperHolder =
        WallpaperHolder(parent.inflate(R.layout.item_wallpaper), showFavIcon)
    
    override fun getPreloadItems(position: Int): MutableList<Wallpaper> =
        Collections.singletonList(list[position])
    
    override fun getPreloadRequestBuilder(item: Wallpaper): RequestBuilder<*>? =
        manager?.loadPicture(item.thumbUrl, item.thumbUrl, withTransition = false)
    
    private fun getModifiedItems(
        oldList: ArrayList<Wallpaper>,
        newList: ArrayList<Wallpaper>
                                ): ArrayList<Wallpaper> {
        val modified = ArrayList<Wallpaper>()
        modified.addAll(oldList.jfilter { !newList.contains(it) && !modified.contains(it) })
        modified.addAll(newList.jfilter { !oldList.contains(it) && !modified.contains(it) })
        return ArrayList(modified.distinct())
    }
    
    override fun onViewRecycled(holder: WallpaperHolder) {
        holder.unbind()
        super.onViewRecycled(holder)
    }
    
    override fun getItemId(position: Int) = position.toLong()
}