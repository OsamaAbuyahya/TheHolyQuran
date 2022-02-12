package os.abuyahya.theholyquran.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import os.abuyahya.theholyquran.R
import os.abuyahya.theholyquran.adapters.SurahAdapter
import os.abuyahya.theholyquran.databinding.FragmentHomeBinding
import os.abuyahya.theholyquran.other.Status
import os.abuyahya.theholyquran.ui.viewmodel.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var surahAdapter: SurahAdapter

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentHomeBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        setupRecyclerView()
        subscribeToObservers()

        surahAdapter.setItemClickListener {
            mainViewModel.playOrToggleSurah(it)
        }

        return binding.root
    }

    private fun setupRecyclerView() = binding.rvAllSurah.apply {
        adapter = surahAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeToObservers() {
        mainViewModel.surahItems.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    binding.allSurahProgressBar.isVisible = false
                    result.data?.let { surahs ->
                        surahAdapter.surahs = surahs
                    }
                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), "${result.message}", Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    binding.allSurahProgressBar.isVisible = true
                }
            }
        }
    }
}
