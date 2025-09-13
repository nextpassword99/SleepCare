package com.example.sleepcare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sleepcare.R
import com.example.sleepcare.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            homeViewModel.toggleMonitoring()
        } else {
            Toast.makeText(requireContext(), "Permissions not granted to start monitoring", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(HomeViewModel::class.java)

        binding.startStopButton.setOnClickListener {
            checkAndRequestPermissions()
        }

        homeViewModel.isMonitoring.observe(viewLifecycleOwner) { isMonitoring ->
            if (isMonitoring) {
                binding.statusText.text = getString(R.string.monitoring_active)
                binding.startStopButton.text = getString(R.string.stop_monitoring)
                binding.startStopButton.setIconResource(R.drawable.ic_stop)
            } else {
                binding.statusText.text = getString(R.string.monitoring_inactive)
                binding.startStopButton.text = getString(R.string.start_monitoring)
                binding.startStopButton.setIconResource(R.drawable.ic_play)
            }
        }

        return root
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BODY_SENSORS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            homeViewModel.toggleMonitoring()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}