package com.smarthas.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textview.MaterialTextView
import com.smarthas.R

class CreditsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_credits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize text views
        val developerName = view.findViewById<MaterialTextView>(R.id.txt_developer_name)
        val rm = view.findViewById<MaterialTextView>(R.id.txt_rm)
        val projectName = view.findViewById<MaterialTextView>(R.id.txt_project_name)
        val version = view.findViewById<MaterialTextView>(R.id.txt_version)

        // Set text
        developerName.text = getString(R.string.credits_developer_name)
        rm.text = getString(R.string.credits_rm)
        projectName.text = getString(R.string.credits_project)
        version.text = "${getString(R.string.credits_version)} - ${getString(R.string.credits_year)}"
    }
}
