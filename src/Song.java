package src;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import com.mpatric.mp3agic.Mp3File;

public class Song {
    private String songTitle;
    private String songArtist;
    private String songLength;
    private String filePath;
    private Mp3File mp3file;
    private double frameRatePerMs;

    public Song(String filePath) {
        this.filePath = filePath;
        try {
            mp3file = new Mp3File(filePath);
            frameRatePerMs = (double) mp3file.getFrameCount() / mp3file.getLengthInMilliseconds();
            songLength = convertToSongLengthFormat();
            // use the jaudiotagger library to extract metadata
            AudioFile audioFile = AudioFileIO.read(new File(filePath));

            // read the meta data of the file
            Tag tag = audioFile.getTag();
            if (tag != null) {
                songTitle = tag.getFirst(FieldKey.TITLE);
                songArtist = tag.getFirst(FieldKey.ARTIST);
            } else {
                songTitle = "Unknown Title";
                songArtist = "Unknown Artist";
            }
        } catch (Exception e) {
            songTitle = "Unknown Title";
            songArtist = "Unknown Artist";
            songLength = "0:00";
            e.printStackTrace();
        }
    }

    private String convertToSongLengthFormat() {
        long minutes = mp3file.getLengthInSeconds() / 60;
        long seconds = mp3file.getLengthInSeconds() % 60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);
        return formattedTime;
    }

    // getters
    public String getSongTitle() {
        return songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getSongLength() {
        return songLength;
    }

    public double getFrameRatePerMs() {
        return frameRatePerMs;
    }

    public Mp3File getMp3file() {
        return mp3file;
    }

}
