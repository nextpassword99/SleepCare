package com.example.sleepcare.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sleepcare.R
import com.example.sleepcare.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private val TAG = "HomeFragment"
    private var _binding: FragmentHomeBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    private val requestPermissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                    permissions ->
                Log.d(TAG, "Permissions request results: $permissions")
                val allGranted = permissions.entries.all { it.value }
                if (allGranted) {
                    Log.d(TAG, "All permissions granted, toggling monitoring.")
                    homeViewModel.toggleMonitoring()
                } else {
                    Log.w(TAG, "Not all permissions granted.")
                    Toast.makeText(
                                    requireContext(),
                                    "Se requieren permisos para iniciar el monitoreo.",
                                    Toast.LENGTH_LONG
                            )
                            .show()
                }
            }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel =
                ViewModelProvider(
                                this,
                                ViewModelProvider.AndroidViewModelFactory.getInstance(
                                        requireActivity().application
                                )
                        )
                        .get(HomeViewModel::class.java)

        binding.startStopButton.setOnClickListener { checkAndRequestPermissions() }

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

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) !=
                        PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
            Log.d(TAG, "RECORD_AUDIO not granted, adding to request list.")
        } else {
            Log.d(TAG, "RECORD_AUDIO already granted.")
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BODY_SENSORS) !=
                        PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.BODY_SENSORS)
            Log.d(TAG, "BODY_SENSORS not granted, adding to request list.")
        } else {
            Log.d(TAG, "BODY_SENSORS already granted.")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            Log.d(TAG, "POST_NOTIFICATIONS not granted (Android 13+), adding to request list.")
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "POST_NOTIFICATIONS not required for this Android version.")
        } else {
            Log.d(TAG, "POST_NOTIFICATIONS already granted.")
        }

        if (permissionsToRequest.isNotEmpty()) {
            Log.d(TAG, "Requesting permissions: $permissionsToRequest")
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Log.d(TAG, "All required permissions already granted, toggling monitoring.")
            homeViewModel.toggleMonitoring()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
