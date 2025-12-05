package com.example.najwa_belajarnavigationdrawer.fragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.najwa_belajarnavigationdrawer.HalamanUtama
import com.example.najwa_belajarnavigationdrawer.R
import com.example.najwa_belajarnavigationdrawer.databinding.FragmentRegresiBinding
class FragmentRegresi : Fragment() {
    private var _binding: FragmentRegresiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegresiBinding.inflate(inflater, container, false)

        binding.judulRegresi.text = getString(R.string.judul_regresi)
        binding.textRegresi.text = getString(R.string.isi_regresi)

        binding.btnLanjut.setOnClickListener {
            val intent = Intent(requireContext(), HalamanUtama::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
