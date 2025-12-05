package com.example.najwa_belajarnavigationdrawer.fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.najwa_belajarnavigationdrawer.R
import com.example.najwa_belajarnavigationdrawer.databinding.FragmentPendahuluanBinding
class FragmentPendahuluan : Fragment() {
    private var _binding: FragmentPendahuluanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPendahuluanBinding.inflate(inflater, container, false)

        binding.judulPendahuluan.text = getString(R.string.judul_pendahuluan)
        binding.textPendahuluan.text = getString(R.string.isi_pendahuluan)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
