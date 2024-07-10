package com.example.playermusic.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playermusic.domain.AlertWhatIsPlaying
import com.example.playermusic.databinding.FragmentHomeBinding
import com.example.playermusic.extensions.doOnApplyWindowInsets
import com.example.playermusic.ui.home.adapter.AllMusicAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val homeViewModel: HomeViewModel by viewModels()
    private val binding get() = _binding!!
    @Inject
    lateinit var alertWhatIsPlaying: AlertWhatIsPlaying

    private lateinit var allMusicAdapter: AllMusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allMusicAdapter = AllMusicAdapter({
            v -> alertWhatIsPlaying.playThis(v)
        } )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.allMusicRecyclerView.doOnApplyWindowInsets { view1, insets, padding ->
            view1.updatePadding(
                bottom = insets.bottom + padding.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
        createRecyclerView()
        observer()
    }

    private fun createRecyclerView() {
        binding.allMusicRecyclerView.run {
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter = allMusicAdapter
            setItemViewCacheSize(20)
            setHasFixedSize(true)
        }
    }

    private fun observer() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                homeViewModel.musicFlow.collect { listMusic ->
                    binding.progressIndicatorHomeFragment.isVisible = listMusic.isEmpty()
                    allMusicAdapter.submitList(listMusic.values.toList())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}