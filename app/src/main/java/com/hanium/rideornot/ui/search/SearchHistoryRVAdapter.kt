package com.hanium.rideornot.ui.search

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hanium.rideornot.R
import com.hanium.rideornot.domain.SearchHistory
import com.hanium.rideornot.ui.SearchViewModel

class SearchHistoryRVAdapter(
    var itemList: List<SearchHistory>,
    private val searchRecyclerViewInterface: ISearchHistoryRV,
) :
    RecyclerView.Adapter<SearchHistoryRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_search_history_recycler_ex, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
        Log.d("onBindViewHolder", "is called")
    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private val searchResult: TextView = itemView.findViewById(R.id.tv_search_result)
        private val deleteSearchBtn: ImageView = itemView.findViewById(R.id.iv_delete_search_btn)
        private val constraintSearchItem: ConstraintLayout =
            itemView.findViewById(R.id.cl_search_item)

        init {
            // 리스너 연결
            deleteSearchBtn.setOnClickListener(this)
            constraintSearchItem.setOnClickListener(this)
        }

        fun bind(item: SearchHistory) {
            searchResult.text = item.stationName
            constraintSearchItem.setOnClickListener {
                searchRecyclerViewInterface.onSearchHistoryItemClick(item.stationName)
            }
        }

        override fun onClick(view: View?) {
            when (view) {
                deleteSearchBtn -> {
                    searchRecyclerViewInterface.onSearchHistoryItemDeleteClick(adapterPosition)
                }
            }
        }


    }

}