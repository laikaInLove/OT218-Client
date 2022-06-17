package com.melvin.ongandroid.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.melvin.ongandroid.R
import com.melvin.ongandroid.databinding.FragmentMembersBinding
import com.melvin.ongandroid.databinding.FragmentMembersDetailBinding
import com.melvin.ongandroid.model.data.MembersList
import com.melvin.ongandroid.view.adapters.MembersAdapter
import com.melvin.ongandroid.viewmodel.MembersViewModel
import com.melvin.ongandroid.viewmodel.State
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MembersFragment : Fragment() {

    private val viewModel by viewModels<MembersViewModel>()
    private lateinit var binding: FragmentMembersBinding
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMembersBinding.inflate(inflater, container, false)
        analytics = FirebaseAnalytics.getInstance(binding.root.context)


        //Loads data
        viewModel.getMembers()
        viewModel.membersList.observe(viewLifecycleOwner, Observer {
            val bundle = Bundle()
            when (it) {
                is State.Success -> {
                    setMembers(it.data)
                    showSpinnerLoading(false)
                    //Success Analytics Event
                    bundle.putString("message", "members_retrieve_success")
                    analytics.logEvent("members_retrieve_success", bundle)

                }
                is State.Failure -> {
                    showErrorDialog(callback = { viewModel.getMembers() })
                    binding.rvMembers.isVisible = false
                    binding.btnWantToJoin.isVisible = false
                    binding.rvMembers.isVisible = false
                    //Success Analytics Event
                    bundle.putString("message", "members_retrieve_error")
                    analytics.logEvent("members_retrieve_error", bundle)

                }
                is State.Loading -> showSpinnerLoading(true)
            }
        })

        return binding.root
    }

    private fun setMembers(membersList: MembersList) {
        if (!membersList.data.isNullOrEmpty()) {
            showSpinnerLoading(false)
            binding.rvMembers.adapter = MembersAdapter(membersList.data)
        }
    }

    private fun showSpinnerLoading(loading: Boolean) {
        binding.rvMembers.isVisible = !loading
        binding.btnWantToJoin.isVisible = !loading
        binding.rvMembers.isVisible = !loading
        binding.progressBarMembers.isVisible = loading
    }

    // show error message and try again
    private fun showErrorDialog(
        callback: (() -> Unit)? = null
    ) {
        showSpinnerLoading(false)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.error_dialog))
            .setMessage(getString(R.string.error_dialog_detail))
            .setPositiveButton(getString(R.string.try_again)) { _, _ -> callback?.invoke() }
            .show()
    }


}