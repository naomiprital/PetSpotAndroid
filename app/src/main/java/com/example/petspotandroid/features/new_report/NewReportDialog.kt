package com.example.petspotandroid.features.new_report

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.petspotandroid.R
import com.example.petspotandroid.data.models.FirebaseStorageModel
import com.example.petspotandroid.model.Post
import com.example.petspotandroid.features.authentication.auth.AuthViewModel
import com.example.petspotandroid.features.posts_list.PostsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class NewReportDialog : DialogFragment() {
    private var selectedImageUri: Uri? = null
    private var tempCameraUri: Uri? = null

    private lateinit var postsViewModel: PostsViewModel
    private lateinit var authViewModel: AuthViewModel

    private var editingPost: Post? = null

    companion object {
        private const val ARG_POST = "arg_post"

        fun newInstance(post: Post? = null): NewReportDialog {
            val fragment = NewReportDialog()
            post?.let {
                val args = Bundle()
                args.putSerializable(ARG_POST, it)
                fragment.arguments = args
            }
            return fragment
        }
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            val uploadText = view?.findViewById<TextView>(R.id.uploadText)
            val cameraIcon = view?.findViewById<ImageView>(R.id.cameraIcon)

            if (uri != null) {
                selectedImageUri = uri
                uploadText?.text = getString(R.string.photo_selected)
                uploadText?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.status_found
                    )
                )
                cameraIcon?.imageTintList = ColorStateList.valueOf(Color.BLACK)
            }
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val uploadText = view?.findViewById<TextView>(R.id.uploadText)
            val cameraIcon = view?.findViewById<ImageView>(R.id.cameraIcon)

            if (success && tempCameraUri != null) {
                selectedImageUri = tempCameraUri
                uploadText?.text = getString(R.string.photo_selected)
                uploadText?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.status_found
                    )
                )
                cameraIcon?.imageTintList = ColorStateList.valueOf(Color.BLACK)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editingPost = arguments?.getSerializable(ARG_POST) as? Post
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_report, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postsViewModel = ViewModelProvider(requireActivity())[PostsViewModel::class.java]
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)
        val closeButton = view.findViewById<ImageButton>(R.id.closeButton)
        closeButton.setOnClickListener { dismiss() }

        val animalTypes =
            resources.getStringArray(R.array.filter_animals_array).drop(1).toTypedArray()
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, animalTypes)
        val dropdownAnimalType = view.findViewById<AutoCompleteTextView>(R.id.dropdownAnimalType)
        dropdownAnimalType.setAdapter(adapter)

        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroupListingType)
        val lostButton = view.findViewById<MaterialButton>(R.id.lostButton)
        val foundButton = view.findViewById<MaterialButton>(R.id.foundButton)

        val lostColor =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.status_lost))
        val foundColor =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.status_found))
        val grayTextColor =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.gray_text))

        val contactInput = view.findViewById<TextInputEditText>(R.id.contactNumber)
        val locationInput = view.findViewById<TextInputEditText>(R.id.location)
        val dateTime = view.findViewById<TextInputEditText>(R.id.dateTime)
        val descriptionInput = view.findViewById<TextInputEditText>(R.id.description)
        val publishButton = view.findViewById<MaterialButton>(R.id.publishButton)
        val uploadText = view.findViewById<TextView>(R.id.uploadText)
        val cameraIcon = view.findViewById<ImageView>(R.id.cameraIcon)

        val dateTimeFormat = getString(R.string.date_format_with_at)

        if (editingPost != null) {
            val post = editingPost!!
            dialogTitle.text = getString(R.string.edit_report)
            publishButton.text = getString(R.string.save_changes)

            toggleGroup.check(if (post.isLost) R.id.lostButton else R.id.foundButton)
            dropdownAnimalType.setText(post.petType, false)
            contactInput.setText(post.contactNumber)
            locationInput.setText(post.lastSeenLocation)
            dateTime.setText(post.eventDate)
            descriptionInput.setText(post.description)

            if (post.imageUrl.isNotEmpty()) {
                uploadText.text = getString(R.string.photo_selected)
                uploadText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.status_found
                    )
                )
                cameraIcon.imageTintList = ColorStateList.valueOf(Color.BLACK)
            }
        } else {
            toggleGroup.check(R.id.lostButton)
            dropdownAnimalType.setText(animalTypes[0], false)

            val currentCalendar = Calendar.getInstance()
            val defaultFormat = SimpleDateFormat(dateTimeFormat, Locale.getDefault())
            dateTime.setText(defaultFormat.format(currentCalendar.time))

            authViewModel.userData.observe(viewLifecycleOwner) { user ->
                if (user != null && contactInput.text.isNullOrBlank()) {
                    contactInput.setText(user.phone)
                }
            }
        }

        updateToggleColors(
            toggleGroup.checkedButtonId,
            lostButton,
            foundButton,
            lostColor,
            foundColor,
            grayTextColor
        )

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                updateToggleColors(
                    checkedId,
                    lostButton,
                    foundButton,
                    lostColor,
                    foundColor,
                    grayTextColor
                )
            }
        }

        val uploadImageButton = view.findViewById<LinearLayout>(R.id.uploadImageButton)
        uploadImageButton.setOnClickListener {
            showImageSourceDialog()
        }

        dateTime.setOnClickListener {
            showDateTimePicker(dateTime, dateTimeFormat)
        }

        publishButton.setOnClickListener {
            val isLost = toggleGroup.checkedButtonId == R.id.lostButton
            val animalType = dropdownAnimalType.text.toString()
            val contact = contactInput.text.toString()
            val locationString = locationInput.text.toString()
            val dateTimeString = dateTime.text.toString()
            val descriptionString = descriptionInput.text.toString()
            val imageString = selectedImageUri?.toString() ?: editingPost?.imageUrl ?: ""

            if (locationString.isBlank() || descriptionString.isBlank() || contact.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_missing_fields),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val currentUserData = authViewModel.userData.value
            val currentUserId = authViewModel.user.value?.uid
            val profilePicUrl = currentUserData?.avatarUrl ?: ""

            if (currentUserId == null || currentUserData == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_user_not_logged_in),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            publishButton.isEnabled = false
            publishButton.text = if (editingPost != null) getString(R.string.saving) else getString(
                R.string.publishing)

            val postId = editingPost?.id ?: UUID.randomUUID().toString()
            val createdAt = editingPost?.createdAt ?: System.currentTimeMillis()
            val authorName = "${currentUserData.firstName} ${currentUserData.lastName}"

            val savePostAction = { finalImageUrl: String ->
                val post = Post(
                    id = postId,
                    authorId = currentUserId,
                    userName = authorName,
                    authorProfileImageUrl = profilePicUrl,
                    isLost = isLost,
                    petType = animalType,
                    contactNumber = contact,
                    lastSeenLocation = locationString,
                    eventDate = dateTimeString,
                    createdAt = createdAt,
                    imageUrl = finalImageUrl,
                    description = descriptionString
                )

                if (editingPost != null) {
                    postsViewModel.updatePost(post) { success, messageRes ->
                        if (success) {
                            Toast.makeText(requireContext(), getString(R.string.report_updated), Toast.LENGTH_SHORT).show()
                            dismiss()
                        } else {
                            publishButton.isEnabled = true
                            publishButton.text = getString(R.string.save_changes)
                            Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    postsViewModel.addPost(post) { success, messageRes ->
                        if (success) {
                            Toast.makeText(requireContext(), getString(R.string.report_published), Toast.LENGTH_SHORT).show()
                            dismiss()
                        } else {
                            publishButton.isEnabled = true
                            publishButton.text = getString(R.string.publish_report)
                            Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            if (selectedImageUri != null) {
                val storageModel = FirebaseStorageModel()
                storageModel.uploadPostImage(selectedImageUri!!, postId) { uploadedUrl ->
                    if (uploadedUrl != null) {
                        savePostAction(uploadedUrl)
                    } else {
                        publishButton.isEnabled = true
                        publishButton.text = if (editingPost != null) getString(R.string.save_changes) else getString(
                            R.string.publish_report)
                        Toast.makeText(
                            requireContext(),
                            "Failed to upload image. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                savePostAction(editingPost?.imageUrl ?: "")
            }
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                if (which == 0) {
                    launchCamera()
                } else {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
            .show()
    }

    private fun launchCamera() {
        val photoFile =
            File(requireContext().cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")

        tempCameraUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )

        takePicture.launch(tempCameraUri)
    }

    private fun showDateTimePicker(editText: TextInputEditText, formatString: String) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .build()

        datePicker.addOnPositiveButtonClickListener { dateSelection ->
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setTitleText(getString(R.string.select_time))
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = dateSelection
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)

                val format = SimpleDateFormat(formatString, Locale.getDefault())
                editText.setText(format.format(calendar.time))
            }
            timePicker.show(parentFragmentManager, "TimePicker")
        }
        datePicker.show(parentFragmentManager, "DatePicker")
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            val displayMetrics = resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.90).toInt()
            val height = (displayMetrics.heightPixels * 0.90).toInt()
            dialog.window?.setLayout(width, height)
        }
    }

    private fun updateToggleColors(
        checkedId: Int,
        lostButton: MaterialButton,
        foundButton: MaterialButton,
        lostColor: ColorStateList,
        foundColor: ColorStateList,
        grayTextColor: ColorStateList
    ) {
        when (checkedId) {
            R.id.lostButton -> {
                lostButton.strokeColor = lostColor
                lostButton.setTextColor(lostColor)
                foundButton.strokeColor = grayTextColor
                foundButton.setTextColor(grayTextColor)
            }

            R.id.foundButton -> {
                foundButton.strokeColor = foundColor
                foundButton.setTextColor(foundColor)
                lostButton.strokeColor = grayTextColor
                lostButton.setTextColor(grayTextColor)
            }
        }
    }
}