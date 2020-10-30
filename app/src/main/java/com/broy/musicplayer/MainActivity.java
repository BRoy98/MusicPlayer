package com.broy.musicplayer;

import android.Manifest.permission;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.broy.musicplayer.databinding.ActivityMainBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements DownloadProgressListener {

    private ActivityMainBinding binding;
    private String songUrl = "https://geexec.s3.ap-south-1.amazonaws.com/SlowGrenade.mp3";
    private Boolean isPlaying = false;
    private MediaPlayer mPlayer;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        checkPermissions();

        mPlayer = new MediaPlayer();
        handler = new Handler();

        binding.progressBar.setVisibility(View.INVISIBLE);

        binding.download.setOnClickListener(view1 -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.progressBar.setIndeterminate(true);
            Downloader downloader = new Downloader(songUrl, this);
            downloader.execute();
        });

        binding.playBtn.setOnClickListener(view2 -> {
            if (!isPlaying) {
                mPlayer.start();
                startTimer();
                binding.playBtn.setText("Pause");
            } else {
                mPlayer.pause();
                stopTimer();
                binding.playBtn.setText("Play");
            }
            isPlaying = !isPlaying;
        });
    }

    @Override
    public void onDownloadProgress(final int progress) {
        binding.progressBar.setIndeterminate(false);
        binding.progressBar.setProgress(progress);
        binding.downloadText.setText(progress + "%");

        if (progress == 100) {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.downloadText.setText("Downloaded");
            setupSong();
        }
    }

    void checkPermissions() {
        MultiplePermissionsListener dialogMultiplePermissionsListener =
                DialogOnAnyDeniedMultiplePermissionsListener.Builder
                        .withContext(this)
                        .withTitle("Allow Permission")
                        .withMessage("All the permissions are required. Please allow permissions.")
                        .withButtonText(android.R.string.ok, this::finish)
                        .build();

        Dexter.withContext(this)
                .withPermissions(
                        permission.READ_EXTERNAL_STORAGE,
                        permission.WRITE_EXTERNAL_STORAGE,
                        permission.RECORD_AUDIO
                ).withListener(dialogMultiplePermissionsListener).check();

    }

    void setupSong() {
        binding.download.setEnabled(false);
        binding.playBtn.setEnabled(true);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(getApplicationContext(), Uri.fromFile(new File(
                    Environment.getExternalStorageDirectory().getPath() + "/ca3")));
            mPlayer.prepare();
            binding.playTimeTotal.setText(millisToMin(mPlayer.getDuration()));

            int audioSessionId = mPlayer.getAudioSessionId();
            if (audioSessionId != -1) {
                binding.blast.setAudioSessionId(mPlayer.getAudioSessionId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void startTimer() {
        handler.postDelayed(runnable = () -> {
            handler.postDelayed(runnable, 1000);
            binding.playText.setText(millisToMin(mPlayer.getCurrentPosition()));
        }, 1000);
    }

    void stopTimer() {
        handler.removeCallbacks(runnable);
    }

    String millisToMin(long milliseconds) {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        return (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }
}