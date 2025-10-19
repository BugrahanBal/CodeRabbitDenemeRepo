package com.balbugrahan.chatbot2024.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.balbugrahan.chatbot2024.R
import com.balbugrahan.chatbot2024.databinding.FragmentMainBinding
import com.balbugrahan.chatbot2024.ui.StepAdapter
import com.balbugrahan.chatbot2024.util.DialogHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: StepAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = StepAdapter(requireContext()) { action ->
            when (action) {
                "end_conversation" -> {
                    popUpMessage()
                }
                else -> {
                    viewModel.sendAction(action)
                }
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.currentStep.observe(viewLifecycleOwner) { step ->
            adapter.addStep(step)
            scrollToBottom()
        }
    }

    private fun scrollToBottom() {
        binding.recyclerView.post {
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun popUpMessage() {
        context?.let {
            DialogHelper.showAlertDialog(
                context = requireContext(),
                title = it.getString(R.string.would_like_end_conversation),
                message = it.getString(R.string.are_you_sure),
                positiveText = it.getString(R.string.yes_button),
                negativeText = it.getString(R.string.no_button),
                positiveAction = {
                    viewModel.onFinishRequested()
                    activity?.finish()
                },
                negativeAction = {
                    Toast.makeText(requireContext(), context?.getString(R.string.not_leave), Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
