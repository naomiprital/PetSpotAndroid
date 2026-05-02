package com.example.petspotandroid.features.new_report

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.petspotandroid.R
import com.example.petspotandroid.databinding.FragmentNewReportBinding
import com.example.petspotandroid.features.authentication.auth.AuthViewModel
import com.example.petspotandroid.model.Post
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NewReportDialog : DialogFragment() {

    private var _binding: FragmentNewReportBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var tempCameraUri: Uri? = null
    private var editingPost: Post? = null

    private val authViewModel: AuthViewModel by viewModels()
    private val newReportViewModel: NewReportViewModel by viewModels()

    companion object {
        private const val ARG_POST_ID = "arg_post_id"

        fun newInstance(postId: String? = null): NewReportDialog {
            return NewReportDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_POST_ID, postId)
                }
            }
        }
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { handleImageSelection(it) }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempCameraUri?.let { handleImageSelection(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postId = arguments?.getString(ARG_POST_ID)

        setupAnimalDropdown()

        if (postId != null) {
            newReportViewModel.getPost(postId).observe(viewLifecycleOwner) { post ->
                post?.let {
                    editingPost = it
                    setupUIWithPost(it)
                }
            }
        } else {
            setupUIForNewReport()
        }

        setupListeners()
        setupObservers()
    }

    private fun setupAnimalDropdown() {
        val animalTypes = resources.getStringArray(R.array.filter_animals_array).drop(1).toTypedArray()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, animalTypes)
        binding.dropdownAnimalType.setAdapter(adapter)

        binding.dropdownAnimalType.setOnClickListener {
            binding.dropdownAnimalType.showDropDown()
        }
    }

    private fun setupListeners() {
        binding.closeButton.setOnClickListener { dismiss() }
        binding.toggleGroupListingType.addOnButtonCheckedListener { _, id, isChecked ->
            if (isChecked) updateToggleColors(id)
        }
        binding.uploadImageButton.setOnClickListener { showImageSourceDialog() }
        binding.dateTime.setOnClickListener { showDateTimePicker() }
        binding.publishButton.setOnClickListener { handlePublish() }
    }

    private fun setupObservers() {
        newReportViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.publishButton.isEnabled = !isLoading
            binding.publishButton.text = when {
                isLoading && editingPost != null -> getString(R.string.saving)
                isLoading -> getString(R.string.publishing)
                editingPost != null -> getString(R.string.save_changes)
                else -> getString(R.string.publish_report)
            }
        }

        if (editingPost == null) {
            authViewModel.userData.observe(viewLifecycleOwner) { user ->
                if (user != null && binding.contactNumber.text.isNullOrBlank()) {
                    binding.contactNumber.setText(user.phone)
                }
            }
        }
    }

    private fun handlePublish() {
        val location = binding.location.text.toString()
        val description = binding.description.text.toString()
        val contact = binding.contactNumber.text.toString()

        if (location.isBlank() || description.isBlank() || contact.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.error_missing_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val userData = authViewModel.userData.value ?: return

        val post = Post(
            id = editingPost?.id ?: UUID.randomUUID().toString(),
            authorId = userData.id,
            userName = "${userData.firstName} ${userData.lastName}",
            authorProfileImageUrl = userData.avatarUrl,
            description = description,
            imageUrl = editingPost?.imageUrl,
            createdAt = editingPost?.createdAt ?: System.currentTimeMillis(),
            isLost = binding.toggleGroupListingType.checkedButtonId == R.id.lostButton,
            isResolved = editingPost?.isResolved ?: false,
            petType = binding.dropdownAnimalType.text.toString(),
            lastSeenLocation = location,
            contactNumber = contact,
            eventDate = binding.dateTime.text.toString(),
            lastUpdated = editingPost?.lastUpdated
        )

        val imageBytes = selectedImageUri?.let { getCompressedImageBytes(it) }

        if (editingPost != null) {
            newReportViewModel.updatePost(post, imageBytes) { success ->
                if (success) handleSuccess(R.string.report_updated) else handleError()
            }
        } else {
            newReportViewModel.addPost(post, imageBytes) { success ->
                if (success) handleSuccess(R.string.report_published) else handleError()
            }
        }
    }

    private fun handleSuccess(messageRes: Int) {
        if (!isAdded) return
        Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_SHORT).show()
        dismiss()
    }

    private fun handleError() {
        if (!isAdded) return
        Toast.makeText(requireContext(), "Operation failed. Please try again.", Toast.LENGTH_SHORT).show()
    }

    private fun handleImageSelection(uri: Uri) {
        selectedImageUri = uri
        updatePhotoSelectionUI()
    }

    private fun updatePhotoSelectionUI() {
        binding.uploadText.text = getString(R.string.photo_selected)
        binding.uploadText.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_found))
        binding.cameraIcon.imageTintList = ColorStateList.valueOf(Color.BLACK)
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                if (which == 0) launchCamera()
                else pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }.show()
    }

    private fun launchCamera() {
        val photoFile = File(requireContext().cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
        tempCameraUri = androidx.core.content.FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
        takePicture.launch(tempCameraUri)
    }

    private fun showDateTimePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText(getString(R.string.select_date)).build()
        datePicker.addOnPositiveButtonClickListener { dateSelection ->
            val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).setTitleText(getString(R.string.select_time)).build()
            timePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = dateSelection
                    set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    set(Calendar.MINUTE, timePicker.minute)
                }
                val format = SimpleDateFormat(getString(R.string.date_format_with_at), Locale.getDefault())
                binding.dateTime.setText(format.format(calendar.time))
            }
            timePicker.show(parentFragmentManager, "TimePicker")
        }
        datePicker.show(parentFragmentManager, "DatePicker")
    }

    private fun updateToggleColors(checkedId: Int) {
        val lostColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.status_lost))
        val foundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.status_found))
        val grayColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.gray_text))

        if (checkedId == R.id.lostButton) {
            binding.lostButton.strokeColor = lostColor
            binding.lostButton.setTextColor(lostColor)
            binding.foundButton.strokeColor = grayColor
            binding.foundButton.setTextColor(grayColor)
        } else {
            binding.foundButton.strokeColor = foundColor
            binding.foundButton.setTextColor(foundColor)
            binding.lostButton.strokeColor = grayColor
            binding.lostButton.setTextColor(grayColor)
        }
    }

    private fun setupUIWithPost(post: Post) {
        binding.dialogTitle.text = getString(R.string.edit_report)
        binding.publishButton.text = getString(R.string.save_changes)
        binding.toggleGroupListingType.check(if (post.isLost) R.id.lostButton else R.id.foundButton)
        updateToggleColors(binding.toggleGroupListingType.checkedButtonId)

        binding.dropdownAnimalType.setText(post.petType, false)
        binding.contactNumber.setText(post.contactNumber)
        binding.location.setText(post.lastSeenLocation)
        binding.dateTime.setText(post.eventDate)
        binding.description.setText(post.description)

        if (post.imageUrl?.isNotEmpty() == true) {
            updatePhotoSelectionUI()
        }
    }

    private fun setupUIForNewReport() {
        val animalTypes = resources.getStringArray(R.array.filter_animals_array).drop(1).toTypedArray()
        binding.dialogTitle.text = getString(R.string.publish_report)
        binding.publishButton.text = getString(R.string.publish_report)
        binding.toggleGroupListingType.check(R.id.lostButton)
        updateToggleColors(R.id.lostButton)

        binding.dropdownAnimalType.setText(animalTypes[0], false)

        val format = SimpleDateFormat(getString(R.string.date_format_with_at), Locale.getDefault())
        binding.dateTime.setText(format.format(Date()))
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.90).toInt()
            setLayout(width, height)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCompressedImageBytes(uri: android.net.Uri): ByteArray? {
        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                java.io.ByteArrayOutputStream().use { outputStream ->
                    originalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                    outputStream.toByteArray()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}