package com.example.petspotandroid.ui

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.petspotandroid.R
import com.example.petspotandroid.data.models.Post
import com.example.petspotandroid.viewmodel.PostsViewModel
import com.example.petspotandroid.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class NewReportDialog : DialogFragment() {

    private var selectedImageUri: Uri? = null

    private lateinit var postsViewModel: PostsViewModel
    private lateinit var authViewModel: AuthViewModel

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val uploadText = view?.findViewById<TextView>(R.id.uploadText)
        val cameraIcon = view?.findViewById<ImageView>(R.id.cameraIcon)

        if (uri != null) {
            selectedImageUri = uri
            uploadText?.text = getString(R.string.photo_selected)
            uploadText?.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_found))
            cameraIcon?.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.BLACK)
        } else {
            selectedImageUri = null
            uploadText?.text = getString(R.string.upload_photo)
            val defaultColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray_text)
            uploadText?.setTextColor(defaultColor)
            cameraIcon?.imageTintList = android.content.res.ColorStateList.valueOf(defaultColor)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postsViewModel = ViewModelProvider(requireActivity())[PostsViewModel::class.java]
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        val closeButton = view.findViewById<ImageButton>(R.id.closeButton)
        closeButton.setOnClickListener { dismiss() }

        val animalTypes = resources.getStringArray(R.array.filter_animals_array).drop(1).toTypedArray()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, animalTypes)
        val dropdownAnimalType = view.findViewById<AutoCompleteTextView>(R.id.dropdownAnimalType)
        dropdownAnimalType.setAdapter(adapter)
        dropdownAnimalType.setText(animalTypes[0], false)

        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroupListingType)
        val lostButton = view.findViewById<MaterialButton>(R.id.lostButton)
        val foundButton = view.findViewById<MaterialButton>(R.id.foundButton)

        val lostColor = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_lost))
        val foundColor = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_found))
        val grayTextColor = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray_text))

        toggleGroup.check(R.id.lostButton)
        lostButton.strokeColor = lostColor
        lostButton.setTextColor(lostColor)
        foundButton.strokeColor = grayTextColor
        foundButton.setTextColor(grayTextColor)

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
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

        val uploadImageButton = view.findViewById<LinearLayout>(R.id.uploadImageButton)
        uploadImageButton.setOnClickListener {
            pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val dateTime = view.findViewById<TextInputEditText>(R.id.dateTime)
        val currentCalendar = Calendar.getInstance()
        val defaultFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        dateTime.setText(defaultFormat.format(currentCalendar.time))

        dateTime.setOnClickListener {
            showDateTimePicker(dateTime)
        }

        val contactInput = view.findViewById<TextInputEditText>(R.id.contactNumber)
        authViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null && contactInput.text.isNullOrBlank()) {
                contactInput.setText(user.phone)
            }
        }

        val publishButton = view.findViewById<MaterialButton>(R.id.publishButton)
        publishButton.setOnClickListener {
            val isLost = toggleGroup.checkedButtonId == R.id.lostButton
            val animalType = dropdownAnimalType.text.toString()
            val contact = contactInput.text.toString()
            val locationString = view.findViewById<TextInputEditText>(R.id.location).text.toString()
            val dateTimeString = dateTime.text.toString()
            val descriptionString = view.findViewById<TextInputEditText>(R.id.description).text.toString()
            val imageString = selectedImageUri?.toString() ?: ""

            if (locationString.isBlank() || descriptionString.isBlank() || contact.isBlank()) {
                Toast.makeText(requireContext(), getString(R.string.error_missing_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUserData = authViewModel.userData.value
            val currentUserId = authViewModel.user.value?.uid
            val profilePicUrl = currentUserData?.avatarUrl ?: ""

            if (currentUserId == null || currentUserData == null) {
                Toast.makeText(requireContext(), "Error: User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val authorName = "${currentUserData.firstName} ${currentUserData.lastName}"

            val newPost = Post(
                id = UUID.randomUUID().toString(),
                authorId = currentUserId,
                userName = authorName,
                authorProfileImageUrl = profilePicUrl,
                isLost = isLost,
                petType = animalType,
                contactNumber = contact,
                lastSeenLocation = locationString,
                eventDate = dateTimeString,
                createdAt = System.currentTimeMillis(),
                imageUrl = imageString,
                description = descriptionString
            )

            postsViewModel.addPost(newPost)

            Toast.makeText(requireContext(), getString(R.string.report_published), Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun showDateTimePicker(editText: TextInputEditText) {
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

                val format = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
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
}