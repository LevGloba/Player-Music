package com.example.playermusic
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.playermusic.domain.ContentResolverMusic
import com.example.playermusic.data.InterfaceTransmitControllerMediaPlayer
import com.example.playermusic.domain.PathMusic
import com.example.playermusic.data.model.ObserverWhatIsPlaying
import com.example.playermusic.data.model.PlayingMusic
import com.example.playermusic.databinding.ActivityMainBinding
import com.example.playermusic.domain.AlertWhatIsPlaying
import com.example.playermusic.extensions.doOnApplyWindowInsets
import com.example.playermusic.ui.MainViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var textTitle: TextView
    private lateinit var textAuthor: TextView
    private lateinit var seek: SeekBar
    private lateinit var playAndPauseButton: MaterialButton
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private lateinit var headerMainBinding: View
    private lateinit var preview: ImageView
    private lateinit var drawerLayout: DrawerLayout


    private var isTrackTouch: Boolean = false
    private lateinit var mps: Intent
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        mps = Intent(this, MediaPlayerService::class.java)
        startService(mps)


        binding = ActivityMainBinding.inflate(layoutInflater).also { binding ->
            binding.run {
                setContentView(root)

                setSupportActionBar(binding.appBarMain.toolbar)
                        appBarMain.fab.setOnClickListener { _ ->
                            mainViewModel.mode?.value.let {
                                println("$it")
                                val res = when (it) {
                                    Player.REPEAT_MODE_ALL -> {
                                        mainViewModel.repeatOne()
                                        R.drawable.round_replay_24
                                    }

                                    Player.REPEAT_MODE_ONE -> {
                                        mainViewModel.mix()
                                        R.drawable.baseline_shuffle_24
                                    }

                                    else -> {
                                        mainViewModel.repeatAll()
                                        R.drawable.baseline_repeat_24
                                    }
                                }
                                appBarMain.fab.setImageResource(res)
                            }
                        }

                val drawerLayout: DrawerLayout = drawerLayout
                val navHostFragment =  supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
                val navController = navHostFragment.navController

                appBarMain.toolbar.doOnApplyWindowInsets { view1, insets, _ ->
                    view1.updateLayoutParams<MarginLayoutParams> {
                        topMargin = insets.top
                    }
                    WindowInsetsCompat.CONSUMED
                }
                navView.doOnApplyWindowInsets { _, insets, _ ->
                    navView.getHeaderView(0).run {
                        updatePadding(top = insets.top)
                    }
                    WindowInsetsCompat.CONSUMED
                }
                appBarMain.fab.doOnApplyWindowInsets { view1, insets, _ ->
                    view1.updateLayoutParams<MarginLayoutParams> {
                        bottomMargin = insets.bottom
                    }
                    WindowInsetsCompat.CONSUMED
                }
                // Passing each menu ID as a set of Ids because each
                // menu should be considered as top level destinations.
                appBarConfiguration = AppBarConfiguration(
                    setOf(
                        R.id.nav_home, R.id.nav_library, R.id.nav_equalizer
                    ), drawerLayout
                )
                setupActionBarWithNavController(navController, appBarConfiguration)
                navView.setupWithNavController(navController)
            }
        }

        val locationPermissionRequest =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                if (it) {
                    mainViewModel.update()
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissionRequest.launch(android.Manifest.permission.READ_MEDIA_AUDIO)
                return
            } else
                mainViewModel.update()
        }

        initNavHead()
        listener()
        controllerHead()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @UiThread
    private fun initNavHead() {
        headerMainBinding = binding.navView.getHeaderView(0)
        textTitle = returnResource(R.id.textView_song) as TextView
        textAuthor = returnResource(R.id.textView_author) as TextView
        seek = returnResource(R.id.seekBar_music) as SeekBar
        playAndPauseButton = returnResource(R.id.imageView_icon_play_and_pause) as MaterialButton
        nextButton = returnResource(R.id.imageView_icon_next) as Button
        previousButton = returnResource(R.id.imageView_icon_back) as Button
        preview = returnResource(R.id.imageView_icon_music) as ImageView
    }

    private fun returnResource(@IdRes int: Int): View = headerMainBinding.findViewById(int)

    private fun listener() {
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let { mainViewModel.setPosition(it.toLong()) }
                isTrackTouch = false
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTrackTouch = true
            }

            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
            }
        })
        nextButton.setOnClickListener { mainViewModel.next() }
        previousButton.setOnClickListener { mainViewModel.previous() }

        playAndPauseButton.setOnClickListener {
            if (mainViewModel.playing?.value == false) {
                mainViewModel.pause()
                playAndPauseButton.setIconResource(R.drawable.baseline_play_arrow_24)
            } else {
                mainViewModel.play()
                playAndPauseButton.setIconResource(R.drawable.baseline_pause_24)
            }
        }

    }

    @MainThread
    private fun controllerHead() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mainViewModel.playMusic.collect {
                    if (this@MainActivity::textTitle.isInitialized) {
                        textTitle.text = it.title
                        textAuthor.text = it.author
                        if (!isTrackTouch) {
                            seek.max = (it.duration / 100).toInt()
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mainViewModel.position?.collect {
                    seek.progress = (it).toInt() / 100
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(mps)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Изменить путь")
                .setMessage("Вы хотите изменить путь считывания музыкальных файлов?")
                .setNegativeButton("Отменить") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Изменить") { dialog, _ ->
                    createChangePath()
                    dialog.dismiss()
                }
                .show()
        } else
            binding.drawerLayout.openDrawer(binding.navView)
        return true
    }

    private fun createChangePath() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val editText = EditText(this@MainActivity).apply {
                    setText(mainViewModel.path.value)
                }
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("Путь")
                    .setView(editText)
                    .setNegativeButton("Отменить") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Сохранить") { dialog, _ ->
                        if (!editText.text.isNullOrEmpty()) {
                            mainViewModel.changePathMusic(editText.text.toString().trim())
                            dialog.dismiss()
                        }
                    }
                    .setOnDismissListener {
                        Snackbar.make(binding.root,"Путь изменен", Snackbar.LENGTH_LONG).apply {
                            anchorView =  binding.appBarMain.fab
                            show()
                        }
                    }
                    .show()
            }
        }
    }
}