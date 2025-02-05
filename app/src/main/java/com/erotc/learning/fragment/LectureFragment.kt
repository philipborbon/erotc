package com.erotc.learning.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.erotc.learning.R
import com.erotc.learning.activity.ViewLectureActivity
import com.erotc.learning.adapter.LectureAdapter
import com.erotc.learning.contract.Searchable
import com.erotc.learning.data.Lecture
import com.erotc.learning.repository.LearnRepository
import kotlinx.android.synthetic.main.fragment_lecture.*

/**
 * A simple [Fragment] subclass.
 */
class LectureFragment : Fragment(), Searchable {
    private lateinit var learnRepository: LearnRepository

    private var resultList: List<Lecture>? = null
    private var lectureAdapter: LectureAdapter? = null
    private var previousTask: AsyncTask<Void, Void, Void>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lecture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_search.setOnClickListener { onSearchClick() }
        input_search_key.setOnEditorActionListener(OnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                onSearchClick()
                return@OnEditorActionListener true
            }
            false
        })
        input_search_key.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                search()
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        val activity = activity ?: return

        learnRepository = LearnRepository.getInstance(activity)

        initRecyclerView()
        hideNoResults()
    }

    private fun initRecyclerView() {
        lectureAdapter = LectureAdapter {
            show(it)
        }

        recycler_result_list.adapter = lectureAdapter
        recycler_result_list.layoutManager = LinearLayoutManager(activity)
    }

    fun onSearchClick() {
        hideKeyboard()
        input_search_key.clearFocus()
    }

    private fun hideKeyboard() {
        val activity = activity ?: return
        val view = activity.currentFocus ?: return

        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("StaticFieldLeak")
    fun search() {
        previousTask?.cancel(true)

        val keyword = input_search_key.text.toString()
        if (keyword.isEmpty()) {
            showAll()
            return
        }

        val task: AsyncTask<Void, Void, Void> = object : AsyncTask<Void, Void, Void>() {
            override fun onPreExecute() {
                hideNoResults()
            }

            override fun doInBackground(vararg p0: Void?): Void? {
                resultList = learnRepository.searchLecture(keyword)

                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                lectureAdapter?.setDataList(resultList ?: arrayListOf())
                lectureAdapter?.notifyDataSetChanged()

                if (resultList?.size ?: 0 == 0) {
                    showNoResults()
                } else {
                    recycler_result_list.scrollToPosition(0)
                }
            }
        }
        task.execute()
        previousTask = task
    }

    private fun showNoResults() {
        label_no_results.visibility = View.VISIBLE
    }

    private fun hideNoResults() {
        label_no_results.visibility = View.GONE
    }

    private fun show(lecture: Lecture) {
        val intent = Intent(activity, ViewLectureActivity::class.java)
        intent.putExtra(ViewLectureActivity.DATA_LECTURE, lecture)

        startActivity(intent)
    }

    override fun isSearchEmpty(): Boolean {
        return input_search_key.text.toString().isEmpty()
    }

    override fun clearInputSearch() {
        input_search_key.setText("")
        input_search_key.clearFocus()
    }

    @SuppressLint("StaticFieldLeak")
    override fun showAll() {
        val task: AsyncTask<Void, Void, Void> = object : AsyncTask<Void, Void, Void>() {
            override fun onPreExecute() {
                hideNoResults()
            }

            override fun doInBackground(vararg p0: Void?): Void? {
                resultList = learnRepository.getAllLecture()

                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                lectureAdapter?.setDataList(resultList ?: arrayListOf())
                lectureAdapter?.notifyDataSetChanged()

                if (resultList?.size ?: 0 == 0) {
                    showNoResults()
                } else {
                    recycler_result_list.scrollToPosition(0)
                }
            }
        }
        task.execute()
        previousTask = task
    }

    override fun onResume() {
        super.onResume()
        val keyword = input_search_key.text.toString()
        if (keyword.isEmpty()) {
            showAll()
        }
    }

    companion object {
        fun newInstance(): LectureFragment {
            return LectureFragment()
        }
    }
}