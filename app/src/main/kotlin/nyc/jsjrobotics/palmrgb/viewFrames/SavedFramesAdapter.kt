package nyc.jsjrobotics.palmrgb.viewFrames

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import nyc.jsjrobotics.palmrgb.database.MutableRgbFrame
import javax.inject.Inject

class SavedFramesAdapter @Inject constructor(): RecyclerView.Adapter<SavedFrameViewHolder>(){

    private var data: List<MutableRgbFrame> = emptyList()
    private val frameSelected : PublishSubject<String> = PublishSubject.create()
    val onFrameSelected : Observable<String> = frameSelected

    fun setData(rgbFrames : List<MutableRgbFrame>) {
        data = rgbFrames
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SavedFrameViewHolder {
        return SavedFrameViewHolder(parent!!)
    }

    override fun onViewDetachedFromWindow(holder: SavedFrameViewHolder?) {
        super.onViewDetachedFromWindow(holder)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: SavedFrameViewHolder, position: Int) {
        val frameName = data[position].frameName
        holder.bind(data[position], {
            frameSelected.onNext(frameName)
        })
    }

}