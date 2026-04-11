package com.example.petspotandroid.features.posts

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petspotandroid.R
import com.example.petspotandroid.adapter.PostsAdapter
import com.example.petspotandroid.api.RetrofitInstance
import com.example.petspotandroid.ui.NewReportDialog
import com.example.petspotandroid.viewmodel.FilterType
import com.example.petspotandroid.viewmodel.PostsViewModel
import com.example.petspotandroid.viewmodel.SortOrder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostsListFragment : Fragment() {

    private lateinit var viewModel: PostsViewModel
    private lateinit var adapter: PostsAdapter

    private var currentTypeFilter: FilterType = FilterType.ALL
    private var currentAnimalFilter: String? = null
    private var currentSortFilter: SortOrder = SortOrder.NEWEST_FIRST

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_posts_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.posts_recycler_view)
        val emptyStateText = view.findViewById<TextView>(R.id.empty_state_text)

        adapter = PostsAdapter(emptyList()) { clickedPost ->
            val dialog = PostDetailsDialog(clickedPost)
            dialog.show(parentFragmentManager, "PostDetailsDialog")
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel = ViewModelProvider(requireActivity())[PostsViewModel::class.java]

        viewModel.filteredPosts.observe(viewLifecycleOwner) { posts ->
            adapter.setPosts(posts)

            if (posts.isEmpty()) {
                emptyStateText.visibility = View.VISIBLE
            } else {
                emptyStateText.visibility = View.GONE
            }
        }

        setupFilterUI(view)

        val floatingActionButton = view.findViewById<FloatingActionButton>(R.id.add_post_fab)
        floatingActionButton.setOnClickListener {
            val dialog = NewReportDialog()
            dialog.show(parentFragmentManager, "NewReportDialog")
        }

        fetchPetFact(view)
    }

    private fun setupFilterUI(view: View) {
        val typesArray = resources.getStringArray(R.array.filter_types_array)
        val animalsArray = resources.getStringArray(R.array.filter_animals_array)
        val sortArray = resources.getStringArray(R.array.filter_sort_array)

        view.findViewById<TextView>(R.id.dropdown_type).text = typesArray[0]
        view.findViewById<TextView>(R.id.dropdown_animal).text = animalsArray[0]
        view.findViewById<TextView>(R.id.dropdown_sort).text = sortArray[0]

        setupDropdown(view, R.id.dropdown_type, typesArray)
        setupDropdown(view, R.id.dropdown_animal, animalsArray)
        setupDropdown(view, R.id.dropdown_sort, sortArray)

        val searchEditText = view.findViewById<EditText>(R.id.search_edit_text)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateSearchQuery(s.toString())
            }
        })
    }

    private fun setupDropdown(rootView: View, textViewId: Int, items: Array<String>) {
        val textView = rootView.findViewById<TextView>(textViewId)
        var selectedIndex = 0
        var itemWasClicked = false

        val popup = ListPopupWindow(requireContext())
        popup.anchorView = textView
        popup.verticalOffset = 10
        popup.isModal = true

        val grayBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_rounded_box)
        popup.setBackgroundDrawable(grayBackground)

        val adapter =
            object : ArrayAdapter<String>(requireContext(), R.layout.item_dropdown, items) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val checkedTextView =
                        super.getView(position, convertView, parent) as CheckedTextView
                    checkedTextView.isChecked = (position == selectedIndex)
                    return checkedTextView
                }
            }

        popup.setAdapter(adapter)

        popup.setOnItemClickListener { _, _, position, _ ->
            itemWasClicked = true
            selectedIndex = position
            textView.text = items[position]

            if (position == 0) {
                textView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        android.R.color.black
                    )
                )
                textView.setTypeface(null, Typeface.NORMAL)
            } else {
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.selected))
                textView.setTypeface(null, Typeface.BOLD)
            }

            when (textViewId) {
                R.id.dropdown_type -> {
                    currentTypeFilter = when (position) {
                        1 -> FilterType.LOST
                        2 -> FilterType.FOUND
                        else -> FilterType.ALL
                    }
                }

                R.id.dropdown_animal -> currentAnimalFilter =
                    if (position == 0) null else items[position]

                R.id.dropdown_sort -> {
                    currentSortFilter = when (position) {
                        1 -> SortOrder.OLDEST_FIRST
                        else -> SortOrder.NEWEST_FIRST
                    }
                }
            }
            viewModel.updateFilters(currentTypeFilter, currentAnimalFilter, currentSortFilter)
            popup.dismiss()
        }

        var isDismissing = false
        popup.setOnDismissListener {
            isDismissing = true
            textView.postDelayed({ isDismissing = false }, 100)
            if (!itemWasClicked) {
                textView.playSoundEffect(SoundEffectConstants.CLICK)
            }
        }

        textView.setOnClickListener {
            if (isDismissing) return@setOnClickListener
            if (popup.isShowing) popup.dismiss() else {
                itemWasClicked = false
                adapter.notifyDataSetChanged()
                popup.show()
            }
        }
    }

    private fun fetchPetFact(view: View) {
        val skeletonView = view.findViewById<View>(R.id.skeleton_view)
        val factTextView = view.findViewById<TextView>(R.id.fact_text_view)
        val factCardWrapper = view.findViewById<View>(R.id.fact_card_include)
        val closeButton = view.findViewById<ImageView>(R.id.close_fact_button)

        closeButton.setOnClickListener {
            factCardWrapper.animate().alpha(0f).setDuration(200).withEndAction {
                factCardWrapper.visibility = View.GONE
            }.start()
        }

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val db = FirebaseFirestore.getInstance()
        val dailyFactRef = db.collection("system_data").document("daily_fact")

        dailyFactRef.get().addOnSuccessListener { document ->
            val savedDate = document.getString("date")
            val savedFact = document.getString("fact")

            if (savedDate == todayDate && savedFact != null) {
                showFactAnimation(skeletonView, factTextView, savedFact)
            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val supportedAnimals = resources.getStringArray(R.array.supported_api_animals).toList()
                        val randomAnimal = supportedAnimals.random()

                        val response = RetrofitInstance.api.getFact(randomAnimal)

                        val newFactData = mapOf(
                            "date" to todayDate,
                            "fact" to response.fact
                        )
                        dailyFactRef.set(newFactData)

                        showFactAnimation(skeletonView, factTextView, response.fact)

                    } catch (e: Exception) {
                        factCardWrapper.visibility = View.GONE
                    }
                }
            }
        }.addOnFailureListener {
            factCardWrapper.visibility = View.GONE
        }
    }

    private fun showFactAnimation(skeletonView: View, factTextView: TextView, factText: String) {
        skeletonView.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                skeletonView.visibility = View.GONE
                factTextView.text = factText
                factTextView.alpha = 0f
                factTextView.visibility = View.VISIBLE
                factTextView.animate().alpha(1f).setDuration(300).start()
            }.start()
    }
}