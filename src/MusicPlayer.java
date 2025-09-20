package src;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.Buffer;
import java.util.ArrayList;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

public class MusicPlayer extends PlaybackListener {

    // update isPaused more atomically
    private static final Object playSignal = new Object();
    private Song currentSong;

    public Song getCurrentSong() {
        return currentSong;
    }

    private ArrayList<Song> playlist;
    private int currentPlaylistIndex;
    private musicPlayerGUI musicPlayerGUI;
    private AdvancedPlayer advancedPlayer;

    public void setCurrentFrame(int frame) {
        currentFrame = frame;
    }

    private boolean isPaused;
    // boolean used to determine when song finishes
    private boolean songFinished;
    // boolean flag for when next or previous buttons are pressed
    private boolean pressedNext, pressedPrev;
    // to keep track of the current frame when pausing
    private int currentFrame;

    // track how many milliseconds have played
    private int millisecondsPlayed;

    public void setCurrentTimeinMs(int ms) {
        millisecondsPlayed = ms;
    }

    // constructor
    public MusicPlayer(musicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;

    }

    public void loadSong(Song song) {
        currentSong = song;
        playlist = null;
        if (!songFinished)
            stopSong();
        if (currentSong != null) {
            currentFrame = 0;
            millisecondsPlayed = 0;

            // update GUI
            musicPlayerGUI.setPlayBackSliderValue(0);
            playCurrentSong();
        }
    }

    public void loadPlaylist(File playlistFile) {
        // read the playlist file and load the songs into the playlist
        playlist = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(playlistFile);
            try (BufferedReader bufferReader = new BufferedReader(fileReader)) {
                // read each line from the text file and store the text into the songPath var
                String songPath;
                while ((songPath = bufferReader.readLine()) != null) {
                    Song song = new Song(songPath);
                    // add to the playlist array
                    playlist.add(song);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        if (playlist.size() > 0) {
            // load the first song in the playlist
            musicPlayerGUI.setPlayBackSliderValue(0);
            millisecondsPlayed = 0;
            // update current song to the first song in the playlist
            currentSong = playlist.get(0);
            // start from the first frame
            currentFrame = 0;
            // update GUI
            musicPlayerGUI.enablePauseDisablePlayButtons();
            musicPlayerGUI.updateSongDetails(currentSong);
            musicPlayerGUI.updatePlaybackSlider(currentSong);

            // start song
            playCurrentSong();

        }
    }

    public void pauseSong() {
        if (advancedPlayer != null) {
            try {
                isPaused = true;
                stopSong();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopSong() {
        if (advancedPlayer != null) {
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;
        }
    }

    public void nextSong() {
        if (playlist == null)
            return;
        if (currentPlaylistIndex + 1 > playlist.size() - 1)
            return;
        pressedNext = true;
        // stop current song
        if (!songFinished)
            stopSong();
        currentPlaylistIndex++;
        // update current song
        currentSong = playlist.get(currentPlaylistIndex);
        // reset playback variables
        millisecondsPlayed = 0;
        currentFrame = 0;
        // update GUI
        musicPlayerGUI.enablePauseDisablePlayButtons();
        musicPlayerGUI.updateSongDetails(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);
        playCurrentSong();
    }

    public void previousSong() {
        if (playlist == null)
            return;
        if (currentPlaylistIndex - 1 < 0)
            return;

        pressedPrev = true;
        if (!songFinished)
            stopSong();
        currentPlaylistIndex--;
        // update current song
        currentSong = playlist.get(currentPlaylistIndex);
        // reset playback variables
        millisecondsPlayed = 0;
        currentFrame = 0;
        // update GUI
        musicPlayerGUI.enablePauseDisablePlayButtons();
        musicPlayerGUI.updateSongDetails(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);
        playCurrentSong();
    }

    public void playCurrentSong() {
        if (currentSong == null)
            return;
        try {
            FileInputStream fileInpitStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInpitStream);

            // create a new advanced player
            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);

            startMusicThread();
            // start the playback slider thread
            startPlayBackSliderThread();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // create a thread that will handle playing music
    private void startMusicThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isPaused) {
                        synchronized (playSignal) {
                            isPaused = false;
                            // notify the playback thread to resume
                            playSignal.notify();
                        }
                        // resume music from last frame
                        isPaused = false;
                        advancedPlayer.play(currentFrame, Integer.MAX_VALUE);

                    } else {
                        advancedPlayer.play();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    // create another thread that will update the playback slider
    public void startPlayBackSliderThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    // if paused, wait until notified to resume
                    synchronized (playSignal) {
                        try {
                            playSignal.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                while (!isPaused) {
                    try {
                        // increment milliseconds played
                        millisecondsPlayed++;
                        // calculate the current frame based on milliseconds played
                        int calculatedFrame = (int) ((double) millisecondsPlayed * 2.08
                                * currentSong.getFrameRatePerMs());
                        // update the playback slider in the GUI
                        musicPlayerGUI.setPlayBackSliderValue(calculatedFrame);

                        // mimic 1 ms delay
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        // this method is called when playback starts
        songFinished = false;
        System.out.println("Playback started");
        pressedNext = false;
        pressedPrev = false;

    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        // this method is called when playback finishes or os stopped
        System.out.println("Playback finished");
        System.out.println("Stopped at frame: " + evt.getFrame());
        if (isPaused) {
            currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMs());

        } else {
            // hard stop if user presses next or previous
            if (pressedNext || pressedPrev)
                return;
            songFinished = true;
            if (playlist == null) {
                musicPlayerGUI.enablePlayDisablePauseButtons();
            } else {
                // last song in playlist
                if (currentPlaylistIndex == playlist.size() - 1) {
                    musicPlayerGUI.enablePlayDisablePauseButtons();

                } else {

                    nextSong();
                }
            }
        }
    }
}
