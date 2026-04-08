package com.example.customproject.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Reusable RecyclerView scroll listener that triggers [onLoadMore] when the user
 * scrolls within [threshold] items of the end of the list.
 *
 * Memory-leak note: remove this listener in onDestroyView via
 *   recyclerView.removeOnScrollListener(listener)
 */
class InfiniteScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val threshold: Int = Constants.SCROLL_THRESHOLD,
    private val onLoadMore: () -> Unit
) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy <= 0) return
        val totalItems = layoutManager.itemCount
        val lastVisible = layoutManager.findLastVisibleItemPosition()
        if (totalItems > 0 && lastVisible >= totalItems - threshold) {
            onLoadMore()
        }
    }
}
