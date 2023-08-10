package com.hanium.rideornot.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.annotation.UiThread
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.hanium.rideornot.databinding.FragmentSearchBinding
import com.hanium.rideornot.domain.SearchHistory
import com.hanium.rideornot.domain.Station
import com.hanium.rideornot.domain.StationDatabase
import com.hanium.rideornot.ui.SearchViewModel
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import kotlinx.coroutines.*

class SearchFragment : Fragment(),
    OnMapReadyCallback,
    ISearchHistoryRV,
    ISearchResultRV,
    SearchResultRVAdapter.OnItemClickListener {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchHistoryRVAdapter: SearchHistoryRVAdapter
    private lateinit var searchResultRVAdapter: SearchResultRVAdapter
    private lateinit var searchViewModel: SearchViewModel
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private fun setBackBtnHandling() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        setBackBtnHandling()

        searchViewModel = SearchViewModel(requireContext())
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

        binding.recyclerView.setHasFixedSize(true)

        binding.editTextSearch.setOnKeyListener { _, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // 엔터가 눌릴 때 동작
                // TODO: 일치하는 Station 객체를 직접 추가하도록 변경
                coroutineScope.launch {
                    searchViewModel.insertSearchHistory(
                        SearchHistory(
                            stationId = -1,
                            stationName = binding.editTextSearch.text.toString()
                        )
                    ) // TODO: ID값 수정\
                    hideKeyboard()
                    handleSearch()
                    binding.editTextSearch.text.clear()
                }
                true
            } else {
                false
            }
        }
        // TODO: 검색창 바깥부분 터치 시 키보드 내려가는 기능 동작 안하는 문제 수정
        binding.constraintLayout.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
            }
            false
        }
//        binding.fcvMap.setOnTouchListener { view, event ->
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                hideKeyboard()
//            }
//            false
//        }

        initView()

        return binding.root
    }

    private fun initView() {
//        val fm = childFragmentManager
//        val mapFragment = fm.findFragmentById(R.id.fcv_map) as MapFragment?
//            ?: MapFragment.newInstance().also {
//                fm.beginTransaction().add(R.id.fcv_map, it).commit()
//            }
//
//        // 비동기로 NaverMap 객체 얻기
//        mapFragment.getMapAsync(this)
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        // ...
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }

    override fun onPause() {
        super.onPause()
//        // Fragment 전환 시 입력했던 텍스트가 사라지는 기능
//        binding.editTextSearch.text.clear()

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchViewModel.searchHistoryList.observe(this, Observer {
            initSearchHistoryRecycler(searchViewModel.searchHistoryList.value!!)
            binding.recyclerView.adapter = searchHistoryRVAdapter
            searchHistoryRVAdapter.notifyDataSetChanged()
        })

        // 검색어 입력을 실시간으로 탐지하여 검색 결과에 반영
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val searchText = s?.toString() ?: ""
                if (searchText.isNotEmpty()) {
                    handleSearch()
                } else {
                    searchViewModel.searchHistoryList.observe(viewLifecycleOwner, Observer {
                        initSearchHistoryRecycler(it)
                        binding.recyclerView.adapter = searchHistoryRVAdapter
                    })
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun initSearchHistoryRecycler(searchHistoryList: List<SearchHistory>) {
        val emptyList = listOf<SearchHistory>()
        searchHistoryRVAdapter =
            SearchHistoryRVAdapter(searchHistoryList, this)
        searchHistoryRVAdapter.notifyDataSetChanged()
    }

    override fun onSearchResultItemClick(station: Station) {
        switchToStationDetailFragment(station.stationName)
    }

    override fun onSearchHistoryItemClick(stationName: String) {
        switchToStationDetailFragment(stationName)
    }

    override fun onSearchHistoryItemDeleteClick(position: Int) {
        coroutineScope.launch {
            val searchHistoryToDelete = searchHistoryRVAdapter.itemList[position]
            searchViewModel.deleteSearchHistory(searchHistoryToDelete)
        }
    }


    private fun hideKeyboard() {
        if (activity != null && requireActivity().currentFocus != null) {
            val inputManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                requireActivity().currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    private fun handleSearch() {
        val searchQuery: String = binding.editTextSearch.text.toString()
        if (searchQuery.isNotEmpty()) {
            val stationDao = StationDatabase.getInstance(requireContext())!!.stationDao()
            lifecycleScope.launch {
                val searchResult = stationDao.findStationsByName(searchQuery).distinctBy { it.stationName }
                searchResultRVAdapter = SearchResultRVAdapter(
                    requireContext(),
                    searchResult,
                    searchViewModel,
                    this@SearchFragment
                )
                binding.recyclerView.adapter = searchResultRVAdapter
            }
        }
    }

    override fun onItemClick(position: Int) {
    }

    private fun switchToStationDetailFragment(stationName: String) {
        findNavController().navigate(
            SearchFragmentDirections.actionFragmentSearchToActivityStationDetail(stationName)
        )
    }
}