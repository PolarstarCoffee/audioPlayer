package src;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.Desktop.Action;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.MouseEvent;

// This class creates a GUI window for an audio player application
public class musicPlayerGUI extends JFrame {
    // Global variables
    public static final Color FRAME_COLOR = Color.darkGray;
    public static final Color TEXT_COLOR = Color.WHITE;
    private MusicPlayer musicPlayer;
    // file explorer
    private JFileChooser fileChooser;
    private JLabel songTitle, songArtist;
    private JPanel playbackBtns;
    private JSlider playbackSlider;

    public musicPlayerGUI() {
        // calls the JFrame constructor to configure the window and set its properties
        super("NVS");
        // Set the size of the window
        setSize(400, 600);
        // Ensure the application exits when the window is closed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Make the window visible
        setVisible(true);
        // Center the window on the screen
        setLocationRelativeTo(null);
        // Prevent resizing to maintain layout integrity
        setResizable(false);
        // Use absolute positioning for custom layout
        setLayout(null);
        // Set the background color of the window
        getContentPane().setBackground(FRAME_COLOR);
        // initialize music player
        musicPlayer = new MusicPlayer(this);
        // set default path for file explorer
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("src/assets"));

        // filter to see only mp3 files
        fileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));

        addGuiComponents();
    }

    private void addGuiComponents() {
        addToolbar();
        // load image
        JLabel songImage = new JLabel(loadImage("src/assets/record.png"));
        songImage.setBounds(0, 50, getWidth() - 20, 225);
        add(songImage);

        // song title
        songTitle = new JLabel("Title");
        songTitle.setBounds(0, 285, getWidth() - 10, 30);
        songTitle.setFont(new Font("Dialog", Font.BOLD, 20));
        songTitle.setForeground(TEXT_COLOR);
        songTitle.setHorizontalAlignment(JLabel.CENTER);
        add(songTitle);

        // song artist
        songArtist = new JLabel("Artist");
        songArtist.setBounds(0, 315, getWidth() - 10, 20);
        songArtist.setFont(new Font("Dialog", Font.PLAIN, 16));
        songArtist.setForeground(TEXT_COLOR);
        songArtist.setHorizontalAlignment(JLabel.CENTER);
        add(songArtist);

        // playback controls
        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBounds(getWidth() / 2 - 300 / 2, 365, 300, 40);
        playbackSlider.setBackground(null);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // when user clicks on the slider, pause the song
                musicPlayer.pauseSong();

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // when user releases the mouse, resume playing the song from the selected
                // position
                // when the user drops the tick
                JSlider source = (JSlider) e.getSource();
                // get the selected frame from where the user dropped the tick

                int frame = source.getValue();

                musicPlayer.setCurrentFrame(frame);

                // update in ms
                musicPlayer.setCurrentTimeinMs(
                        (int) (double) (frame / (2.08 * musicPlayer.getCurrentSong().getFrameRatePerMs())));

                // resume the song
                musicPlayer.playCurrentSong();

                // toggle pause button
                enablePauseDisablePlayButtons();
            }
        });

        add(playbackSlider);

        // volume controls
        AddPlaybackCtrls();

    }

    // toolbar methods
    private void addToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 30);
        toolBar.setFloatable(false);
        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        JMenu songMenu = new JMenu("Song");
        menuBar.add(songMenu);

        JMenuItem loadSong = new JMenuItem("Load Song(s)");
        loadSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // int is returned to check if a file was selected or the dialog was canceled
                int result = fileChooser.showOpenDialog(musicPlayerGUI.this);
                File selectedFile = fileChooser.getSelectedFile();
                // checking if the user selected a file or closed the dialog
                if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    Song song = new Song(selectedFile.getPath());
                    musicPlayer.loadSong(song);

                    // update song details
                    updateSongDetails(song);
                    enablePauseDisablePlayButtons();
                    updatePlaybackSlider(song);
                }

            }
        });
        songMenu.add(loadSong);

        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        createPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // load music playlist GUI
                new musicPlaylistPrompt(musicPlayerGUI.this).setVisible(true);

            }
        });
        playlistMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
        loadPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // open file explorer to load a playlist
                JFileChooser jFileChooser = new JFileChooser();
                // set file filter to only show text files (we save playlists as text files)
                jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist", "txt"));
                jFileChooser.setCurrentDirectory(new File("src/assets"));

                int result = jFileChooser.showOpenDialog(musicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    // stop music
                    musicPlayer.stopSong();
                    // load the playlist
                    musicPlayer.loadPlaylist(selectedFile);

                }

            }

        });
        playlistMenu.add(loadPlaylist);
        add(toolBar);
    }

    private void AddPlaybackCtrls() {
        // TODO: Add customization to playback controls
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0, 435, getWidth() - 10, 80);
        playbackBtns.setBackground(null);

        // previous button
        JButton previousBtn = new JButton(loadImage("src/assets/previous.png"));
        previousBtn.setBorderPainted(false);
        previousBtn.setBackground(null);
        previousBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // go to the previous song in the playlist
                musicPlayer.previousSong();
            }
        });
        playbackBtns.add(previousBtn);

        // play
        JButton playBtn = new JButton(loadImage("src/assets/play.png"));
        playBtn.setBorderPainted(false);
        playBtn.setBackground(null);
        playBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePauseDisablePlayButtons();

                // play the song
                musicPlayer.playCurrentSong();
            }
        });
        playbackBtns.add(playBtn);

        // pause
        JButton pauseBtn = new JButton(loadImage("src/assets/pause.png"));
        pauseBtn.setBorderPainted(false);
        pauseBtn.setBackground(null);
        pauseBtn.setVisible(false);
        pauseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePlayDisablePauseButtons();

                // pause the song
                musicPlayer.pauseSong();
            }
        });
        playbackBtns.add(pauseBtn);

        // next
        JButton nextBtn = new JButton(loadImage("src/assets/next.png"));
        nextBtn.setBorderPainted(false);
        nextBtn.setBackground(null);
        nextBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // go to the next song in the playlist
                musicPlayer.nextSong();
            }
        });
        playbackBtns.add(nextBtn);

        add(playbackBtns);

    }

    public void enablePauseDisablePlayButtons() {
        // retrive reference to play and pause buttons
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        // turn off button
        playButton.setVisible(false);
        playButton.setEnabled(false);

        // turn on pause button
        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);

    }

    public void enablePlayDisablePauseButtons() {
        // retrive reference to play and pause buttons
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        // turn off button
        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);

        // turn on play button
        playButton.setVisible(true);
        playButton.setEnabled(true);

    }

    public void updateSongDetails(Song song) {
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }

    public void updatePlaybackSlider(Song song) {
        // TODO: update the playback slider based on the song's current position
        playbackSlider.setMaximum(song.getMp3file().getFrameCount());

        // create the song length label
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

        // beginning will be 0:00
        JLabel beginningLabel = new JLabel("0:00");
        beginningLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
        beginningLabel.setForeground(TEXT_COLOR);

        // end will vary based on the song length
        JLabel labelEnd = new JLabel(song.getSongLength());
        labelEnd.setFont(new Font("Dialog", Font.PLAIN, 10));
        labelEnd.setForeground(TEXT_COLOR);
        labelTable.put(0, beginningLabel);
        labelTable.put(song.getMp3file().getFrameCount(), labelEnd);

        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);

    }

    void setPlayBackSliderValue(int frame) {
        playbackSlider.setValue(frame);

    }

    private ImageIcon loadImage(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            return new ImageIcon(image);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // could not load image
        return null;
    }
}
