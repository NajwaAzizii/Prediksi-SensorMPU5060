package com.example.najwa_belajarnavigationdrawer.fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.najwa_belajarnavigationdrawer.R
import com.example.najwa_belajarnavigationdrawer.databinding.FragmentSensorBinding
class FragmentSensor : Fragment() {
    private var _binding: FragmentSensorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorBinding.inflate(inflater, container, false)

        binding.judulSensor.text = getString(R.string.judul_sensor)
        binding.textSensor.text = getString(R.string.isi_sensor)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
