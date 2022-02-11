package com.coolightman.note.presentation.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coolightman.note.NoteApp
import com.coolightman.note.R
import com.coolightman.note.databinding.FragmentEditTaskBinding
import com.coolightman.note.di.ViewModelFactory
import com.coolightman.note.domain.entity.Note
import com.coolightman.note.domain.entity.Task
import com.coolightman.note.domain.entity.TaskColor
import com.coolightman.note.presentation.MainActivity
import com.coolightman.note.presentation.viewmodel.EditTaskViewModel
import com.coolightman.note.util.*
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import javax.inject.Inject

class EditTaskFragment : Fragment() {

    private val component by lazy {
        (requireActivity().application as NoteApp).component
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[EditTaskViewModel::class.java]
    }

    private val preferences by lazy {
        (requireActivity() as MainActivity).preferences
    }

    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<EditTaskFragmentArgs>()
    private var taskId: Long = 0
    private lateinit var timePicker: MaterialTimePicker
    private lateinit var datePicker: MaterialDatePicker<Long>

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taskId = args.taskId

        prepareTask()
        showKeyboard()
        setListeners()
    }

    private fun setListeners() {
        binding.apply {
            btSaveBottom.setOnClickListener {
                saveTask()
            }

            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            rgColors.setOnCheckedChangeListener { group, checkedId ->
                setTaskColor()
            }

            switchImportantTask.setOnCheckedChangeListener { button, checked ->
                when {
                    checked -> imgImportance.visibility = VISIBLE
                    else -> imgImportance.visibility = GONE
                }
            }

            switchAddNotification.setOnCheckedChangeListener { button, checked ->
                when {
                    checked -> {
                        imgNotification.visibility = VISIBLE
                        layoutTaskDate.visibility = VISIBLE
                    }
                    else -> {
                        imgNotification.visibility = GONE
                        layoutTaskDate.visibility = GONE
                    }
                }
            }

            tvTaskDate.setOnClickListener {
                datePicker.show(childFragmentManager, datePicker.toString())
            }

            tvTaskTime.setOnClickListener {
                timePicker.show(childFragmentManager, timePicker.toString())
            }
        }

        datePicker.addOnPositiveButtonClickListener {
            binding.tvTaskDate.text = datePicker.headerText
        }

        timePicker.addOnPositiveButtonClickListener {
            val time = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            binding.tvTaskTime.text = time
        }
    }

    private fun saveTask() {
        if (isTaskValid()) {
            val task: Task = scanTaskDate()
            viewModel.saveTask(task)
            launchToMainTasks()
        } else {
            binding.apply {
                makeSnackbarWithAnchor(
                    root, getString(R.string.snackbar_empty_description_t), btSaveBottom
                )
            }
        }
    }

    private fun scanTaskDate(): Task {
        return Task(
            taskId = taskId,
            description = binding.etTaskDescription.text.toString().trim(),
            color = getTaskColor(),
            dateRemind = 0,
            isImportant = binding.switchImportantTask.isChecked,
            isReminding = false
        )
    }

    private fun launchToMainTasks() {
        findNavController().popBackStack()
    }

    private fun isTaskValid(): Boolean {
        return binding.etTaskDescription.text.toString().trim().isNotEmpty()
    }

    private fun getChosenDateMillis(): Long {
        val dateMillis = datePicker.selection
        dateMillis?.let {
            val timeMillis = ((timePicker.hour * 60) + (timePicker.minute)) * 60 * 1000
            return dateMillis + timeMillis - DATE_MILLIS_CORRECTION
        }
        return 0
    }

    private fun prepareTask() {
        setDefaultTaskColor()
        createDatePicker()
        createTimePicker()
    }

    private fun createTimePicker() {
        timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText(getString(R.string.time_picker_text))
            .build()
    }

    private fun createDatePicker() {
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())

        datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.date_picker_text))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build()
    }

    private fun setDefaultTaskColor() {
        val colorIndex = preferences.getInt(PrefConstants.PREF_TASK_DEFAULT_COLOR, 4)
        val taskColor = TaskColor.values()[colorIndex]
        binding.rgColors.setCheckedByIndex(taskColor.ordinal)
        setTaskColor()
    }

    private fun setTaskColor() {
        val taskColor = getTaskColor()
        binding.cvEditTask.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), taskColor.colorResId)
        )
    }

    private fun getTaskColor(): TaskColor {
        val colorIndex = binding.rgColors.getCheckedIndex()
        return TaskColor.values()[colorIndex]
    }

    private fun showKeyboard() {
        val editText = binding.etTaskDescription
        editText.requestFocus()
        val inputMethManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showSnackBar(message: String) {
        binding.apply {
            makeSnackbarWithAnchor(root, message, btSaveBottom)
        }
    }

    companion object {
        private const val DATE_MILLIS_CORRECTION = 3 * 60 * 60 * 1000
    }
}