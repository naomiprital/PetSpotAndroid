package com.example.petspotandroid.features.posts_list

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
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petspotandroid.R
import com.example.petspotandroid.databinding.FragmentPostsListBinding
import com.example.petspotandroid.features.post_details.PostDetailsDialog
import com.example.petspotandroid.features.new_report.NewReportDialog

class PostsListFragment : Fragment() {

    private var _binding: FragmentPostsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostsViewModel by viewModels()
    private lateinit var adapter: PostsAdapter

    private var currentTypeFilter: FilterType = FilterType.ALL
    private var currentAnimalFilter: String? = null
    private var currentSortFilter: SortOrder = SortOrder.NEWEST_FIRST

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterUI()
        setupObservers()
        setupFab()
        fetchPetFact()
    }

    private fun setupRecyclerView() {
        adapter = PostsAdapter(emptyList()) { clickedPost ->
            PostDetailsDialog.newInstance(clickedPost.id)
                .show(parentFragmentManager, "PostDetailsDialog")
        }

        binding.postsRecyclerView.adapter = adapter
        binding.postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupObservers() {
        viewModel.filteredPosts.observe(viewLifecycleOwner) { posts ->
            adapter.setPosts(posts)
            binding.emptyStateText.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.dailyFact.observe(viewLifecycleOwner) { fact ->
            if (fact != null) {
                showFactAnimation(fact)
            } else {
                binding.factCardInclude.root.visibility = View.GONE
            }
        }
    }

    private fun setupFab() {
        binding.addPostFab.setOnClickListener {
            NewReportDialog.newInstance()
                .show(parentFragmentManager, "NewReportDialog")
        }
    }

    private fun setupFilterUI() {
        val typesArray = resources.getStringArray(R.array.filter_types_array)
        val animalsArray = resources.getStringArray(R.array.filter_animals_array)
        val sortArray = resources.getStringArray(R.array.filter_sort_array)

        binding.dropdownType.text = typesArray[0]
        binding.dropdownAnimal.text = animalsArray[0]
        binding.dropdownSort.text = sortArray[0]

        setupDropdown(binding.dropdownType, typesArray)
        setupDropdown(binding.dropdownAnimal, animalsArray)
        setupDropdown(binding.dropdownSort, sortArray)

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateSearchQuery(s.toString())
            }
        })
    }

    private fun setupDropdown(textView: TextView, items: Array<String>) {
        var selectedIndex = 0
        var itemWasClicked = false

        val popup = ListPopupWindow(requireContext()).apply {
            anchorView = textView
            verticalOffset = 10
            isModal = true
            setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_rounded_box))
        }

        val adapter = object : ArrayAdapter<String>(requireContext(), R.layout.item_dropdown, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val checkedTextView = super.getView(position, convertView, parent) as CheckedTextView
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
                textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                textView.setTypeface(null, Typeface.NORMAL)
            } else {
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.selected))
                textView.setTypeface(null, Typeface.BOLD)
            }

            when (textView.id) {
                binding.dropdownType.id -> {
                    currentTypeFilter = when (position) {
                        1 -> FilterType.LOST
                        2 -> FilterType.FOUND
                        else -> FilterType.ALL
                    }
                }
                binding.dropdownAnimal.id -> {
                    currentAnimalFilter = if (position == 0) null else items[position]
                }
                binding.dropdownSort.id -> {
                    currentSortFilter = if (position == 1) SortOrder.OLDEST_FIRST else SortOrder.NEWEST_FIRST
                }
            }

            viewModel.updateFilters(currentTypeFilter, currentAnimalFilter, currentSortFilter)
            popup.dismiss()
        }

        var isDismissing = false
        popup.setOnDismissListener {
            isDismissing = true
            textView.postDelayed({ isDismissing = false }, 100)
            if (!itemWasClicked) textView.playSoundEffect(SoundEffectConstants.CLICK)
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

    private fun fetchPetFact() {
        binding.factCardInclude.closeFactButton.setOnClickListener {
            binding.factCardInclude.root.animate().alpha(0f).setDuration(200).withEndAction {
                binding.factCardInclude.root.visibility = View.GONE
            }.start()
        }

        val supportedAnimals = resources.getStringArray(R.array.supported_api_animals).toList()
        viewModel.loadDailyFact(supportedAnimals)
    }

    private fun showFactAnimation(factText: String) {
        val factBinding = binding.factCardInclude
        factBinding.skeletonView.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                factBinding.skeletonView.visibility = View.GONE
                factBinding.factTextView.text = factText
                factBinding.factTextView.alpha = 0f
                factBinding.factTextView.visibility = View.VISIBLE
                factBinding.factTextView.animate().alpha(1f).setDuration(300).start()
            }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}