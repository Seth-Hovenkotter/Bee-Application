package com.beeapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/*
Created by Seth Welch 2/6/2015-4/24/2015
 */
public class MainActivity extends ActionBarActivity {

    public static final int SAMPLE_RATE = 16000;

    public Boolean loop; //whether or not to loop initial recording for progress bar without saving
    public Boolean isPlaying = false;
    private Button playButton;
    private Button recordButton;

    private static TextView txtLat, txtLong;
    private static EditText notes;
    private double latitude, longitude;

    VisualizerView mVisualizerView; //used to visualize playback
    private Visualizer mVisualizer;
    private ProgressBar mProgressBar; //used to visualize while recording

    private static MediaPlayer mPlayer; //used for playing back the audio files

    private AudioRecord mRecorder;
    private File mRecording;
    private short[] mBuffer;
    private boolean mIsRecording = false;

    private static boolean gpsLock = false;

    GPS gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setColors();

        recordButton = (Button) findViewById(R.id.recordButton);
        playButton = (Button) findViewById(R.id.playButton);

        initRecorder(); //initializes the AudioRecord object and the buffer

        playButton.setEnabled(false);

        mVisualizerView = (VisualizerView) findViewById(R.id.myvisualizerview); //reference to visualizer in xml, visualizes during playback
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar); //reference to the progress bar in xml, used to visualize during recording

        loop = true;
        recordLoop();//sets up the recording and progressbar visualizer called onCreate to provide immediate feedback that recording is workins

        mVisualizerView.setVisibility(View.INVISIBLE); //makes the Visualizer view invisible
    }

    //resets the recording every 30 seconds so memory is not overloaded
    private void recordLoop() {

        mIsRecording = false;
        mRecorder.stop();

        //starts recording
        try {
            mIsRecording = true;
            mRecorder.startRecording();
            mRecording = getFile("raw");
            startBufferedWrite(mRecording);
        } catch (Exception e) {
            Log.e("Record Reset", Log.getStackTraceString(e));
        }

        //sets the countdown timer.
        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                if (loop) {
                    //stops the recording and then calls this function again
                    mIsRecording = false;
                    mRecorder.stop();
                    recordLoop(); //recursion is tyte!
                }
            }
        }.start();
    }

    //click event used to get the gps coordinates
    public void getLocation(View view) {
        gps = new GPS(MainActivity.this);

        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            txtLat = (TextView) findViewById(R.id.txtLat);
            txtLong = (TextView) findViewById(R.id.txtLong);

            txtLat.setText("Latitude: " + latitude);
            txtLong.setText("Longitude: " + longitude);
        } else {
            //Allow user to enable gps if disabled
            gps.showSettingsAlert();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), ThemeActivity.class);
            startActivity(i);
            loop = false;
            mRecorder.stop();
            mIsRecording = false;
            finish();
        }
        else if (id == R.id.action_files) {
            Intent i = new Intent(getApplicationContext(), FileChooser.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    //Save the project
    public void saving(View view) {
        //Checks if it can save
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dialogBox("Error", "Must Have SD Card Inserted to Save!");
        } else {
            notes = (EditText) findViewById(R.id.Notebox);
            final SaveData save = new SaveData(this);

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Create Folder");
            dialog.setMessage("Enter A Name For This Project");

            final EditText input = new EditText(this);
            dialog.setView(input);

            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = input.getText().toString();

                    //Set the directory to save in
                    File dir = new File(Environment.getExternalStorageDirectory() + "/BeeApp/" + value + "/");

                    //Check if the directory already exists
                    if (dir.exists() && dir.isDirectory()) {
                        dialogBox("Error", "Project Name Already Exists");
                    } else {
                        //Create the folder
                        String filepath = save.createFolder(value);

                        //make copy of wave  file and save it in the same spot
                        File waveFile = new File(filepath + "Audio File");

                        //Save gps data
                        if (isGpsLock()) {
                            String data = "Latitude: " + latitude + "\nLongitude: " + longitude;
                            save.createFile(data, "GPS", filepath);
                        }

                        //Save any written notes
                        if (notes.getText().toString().length() > 0) {
                            save.createFile(notes.getText().toString(), "Note", filepath);
                        }

                        //Zips the file with everything in it
                        save.zipFile(filepath);

                        //DELETE THE OLD FOLDER
                        //IF THIS IS NOT THE FOLDER THAT NEEDS TO BE DELETED PLEASE REMOVE THIS
                        deleteDir(dir);
                    }
                }
            });

            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                    dialog.cancel();
                }
            });

            dialog.show();
            //Log.e("Saving","Reached end of saving");
        }
    }

    //Create a generic dialog box
    public void dialogBox(String title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(title);
        dialog.setMessage(message);

        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public static boolean isGpsLock() {
        return gpsLock;
    }

    public static void setGpsLock(boolean gpsLock2) {
        gpsLock = gpsLock2;
    }

    public void recordAudio(View view) {

        recordButton.setEnabled(false);
        loop = false;
        mIsRecording = false;
        mRecorder.stop();

        new CountDownTimer(60000, 1000) {
            TextView txtTimer = (TextView) findViewById(R.id.txtTimer);

            public void onTick(long millisUntilFinished) {
                txtTimer.setText("Seconds Remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                TextView fft = (TextView) findViewById(R.id.Results);

                txtTimer.setText("Finished Recording");
                fft.setText("Press Check Button To Run FFT");
                //stops the recording and then saves the audio
                mIsRecording = false;
                mRecorder.stop();
                File waveFile = getFile("wav");
                playButton.setEnabled(true);
                try {
                    rawToWave(mRecording, waveFile); //converts to wav file
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(MainActivity.this, "Recorded to " + waveFile.getName(),
                        Toast.LENGTH_SHORT).show();
            }
        }.start();


        mVisualizerView.setVisibility(View.INVISIBLE); //makes it invisible again if it was made visible from playback
        mProgressBar.setVisibility(View.VISIBLE); //makes the progress bar visible
        mRecorder.stop();//stops the previous recording from when the app is first launched, not sure this is necessary

        try {
            mIsRecording = true;
            mRecorder.startRecording();
            mRecording = getFile("raw");
            startBufferedWrite(mRecording);
            Toast.makeText(MainActivity.this, "Recording", Toast.LENGTH_SHORT).show(); // just letting the users know the recording has started
        } catch (Exception e) {
            Log.e("record audio", Log.getStackTraceString(e));
        }

    }

    //stops AND saves the audio file that the user was just recording
    public void stopAudio(View view) {

        mIsRecording = false;
        mRecorder.stop();
        File waveFile = getFile("wav");
        try {
            rawToWave(mRecording, waveFile); //converts to wav file
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(MainActivity.this, "Recorded to " + waveFile.getName(),
                Toast.LENGTH_SHORT).show(); //displays a message to the user that the recording was successful

    }

    //plays the audio and visuals
    public void playAudio(View view) throws IOException {

        if (isPlaying) { //if the media player is already playing the audio file
            mPlayer.stop(); //then stop it
            playButton.setText("Play");
            isPlaying = false;
            mVisualizer.setEnabled(false);
        } else {

            isPlaying = true;
            playButton.setText("Stop");

            mVisualizerView.setVisibility(View.VISIBLE); //makes it visible again
            mProgressBar.setVisibility(View.INVISIBLE); //makes the progress bar invisible

            File waveFile = getFile("wav");

            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mPlayer = MediaPlayer.create(this, Uri.parse(waveFile.getAbsolutePath()));//plays back the file that was just recorded

            setupPlaybackVisualizer(); //method to attach the media player to the visualizer and setup the visualizer otherwise

            mVisualizer.setEnabled(true); //method used to sing show tunes from 1955

            ///used to disable the visualizer when the media player is done playing
            mPlayer
                    .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mVisualizer.setEnabled(false);
                            Toast.makeText(MainActivity.this, "Audio finished",
                                    Toast.LENGTH_SHORT).show();
                            playButton.setText("Play");
                            recordButton.setEnabled(true);
                        }
                    });

            mPlayer.start();

        }
    }

    private void setupPlaybackVisualizer() {

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(mPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {
                        mVisualizerView.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] bytes, int samplingRate) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }

    private void initRecorder() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mBuffer = new short[bufferSize];
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    }

    //visualizes the progress bar using the information in the buffer as it is recording
    private void startBufferedWrite(final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataOutputStream output = null;
                try {
                    output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
                    while (mIsRecording) {
                        double sum = 0;
                        int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
                        for (int i = 0; i < readSize; i++) {
                            output.writeShort(mBuffer[i]);
                            sum += mBuffer[i] * mBuffer[i];
                        }
                        if (readSize > 0) {
                            final double amplitude = sum / readSize;
                            mProgressBar.setProgress((int) Math.sqrt(amplitude));
                        }
                    }
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    mProgressBar.setProgress(0);
                    if (output != null) {
                        try {
                            output.flush();
                        } catch (IOException e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        } finally {
                            try {
                                output.close();
                            } catch (IOException e) {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    //returns the file that  is being recording and then played
    private File getFile(final String suffix) {
        return new File(Environment.getExternalStorageDirectory() + "/BeeApp/", "Bee." + suffix);
    }

    //raw audio file to wave
    private void rawToWave(final File rawFile, final File waveFile) throws IOException {
        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, SAMPLE_RATE); // sample rate
            writeInt(output, SAMPLE_RATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }
            output.write(bytes.array());
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    //called from rawToWave.  Used for converting to wav
    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    //called from rawToWave.  Used for converting to wav
    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    //called from rawToWave.  Used for converting to wav
    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

    //runs the algorithm on the wav file
    public void runAlgo(View view){
//        try {
//            javaMain.run(getFile("wav"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Toast.makeText(getApplicationContext(), "This is a placeholder for the algorithm", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        mRecorder.release();
        super.onDestroy();
    }

    //delete a file or directory method to be called in ItemLongClick
    //delete a directory or folder recursively in here
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    //Sets all of the colors and borders
    void setColors(){
        SaveData save = new SaveData(this);
        List<String> list = save.getSavedColors();

        //Background
        LinearLayout bg = (LinearLayout) findViewById(R.id.backgroundTop);
        LinearLayout bg2 = (LinearLayout) findViewById(R.id.linBackground);

        //Panel Backgrounds
        LinearLayout audioPanel = (LinearLayout) findViewById(R.id.AudioLayout);
        LinearLayout algoPanel = (LinearLayout) findViewById(R.id.AlgoLayout);
        LinearLayout gpsPanel = (LinearLayout) findViewById(R.id.GPSLayout);
        LinearLayout notePanel = (LinearLayout) findViewById(R.id.NoteLayout);

        //Titles
        TextView audioTitle = (TextView) findViewById(R.id.Record_Title);
        TextView algoTitle = (TextView) findViewById(R.id.Algo_Title);
        TextView gpsTitle = (TextView) findViewById(R.id.GPS_Title);
        TextView noteTitle = (TextView) findViewById(R.id.Notes_Title);

        //Buttons
        Button playBtn = (Button) findViewById(R.id.playButton);
        Button recordBtn = (Button) findViewById(R.id.recordButton);
        Button algoBtn = (Button) findViewById(R.id.algoBtn);
        Button gpsBtn = (Button) findViewById(R.id.gpsButton);
        Button saveAll = (Button) findViewById(R.id.btnSaveAll);

        String[] colors = new String[6];

        for(int i = 0; i < list.size(); i++){
            int color = Integer.parseInt(list.get(i));
            colors[i] = save.getColor(color);
        }

        //Set the background color
        bg.setBackgroundColor(Color.parseColor(colors[0]));
        bg2.setBackgroundColor(Color.parseColor(colors[0]));

        //Set the panels background color
        GradientDrawable panelBg = (GradientDrawable) audioPanel.getBackground();
        GradientDrawable panelBg2 = (GradientDrawable) algoPanel.getBackground();
        GradientDrawable panelBg3 = (GradientDrawable) gpsPanel.getBackground();
        GradientDrawable panelBg4 = (GradientDrawable) notePanel.getBackground();
        panelBg.setColor(Color.parseColor(colors[3]));
        panelBg2.setColor(Color.parseColor(colors[3]));
        panelBg3.setColor(Color.parseColor(colors[3]));
        panelBg4.setColor(Color.parseColor(colors[3]));

        //Set the buttons bg color and keep the outline
        GradientDrawable buttonBg = (GradientDrawable) playBtn.getBackground();
        GradientDrawable buttonBg2 = (GradientDrawable) recordBtn.getBackground();
        GradientDrawable buttonBg3 = (GradientDrawable) algoBtn.getBackground();
        GradientDrawable buttonBg4 = (GradientDrawable) gpsBtn.getBackground();
        GradientDrawable buttonBg5 = (GradientDrawable) saveAll.getBackground();
        buttonBg.setColor(Color.parseColor(colors[1]));
        buttonBg2.setColor(Color.parseColor(colors[1]));
        buttonBg3.setColor(Color.parseColor(colors[1]));
        buttonBg4.setColor(Color.parseColor(colors[1]));
        buttonBg5.setColor(Color.parseColor(colors[1]));
        buttonBg.setGradientType(2);

        //Set the buttons text color
        playBtn.setTextColor(Color.parseColor(colors[2]));
        recordBtn.setTextColor(Color.parseColor(colors[2]));
        algoBtn.setTextColor(Color.parseColor(colors[2]));
        gpsBtn.setTextColor(Color.parseColor(colors[2]));
        saveAll.setTextColor(Color.parseColor(colors[2]));

        //Set the titles background color
        audioTitle.setBackgroundColor(Color.parseColor(colors[4]));
        algoTitle.setBackgroundColor(Color.parseColor(colors[4]));
        gpsTitle.setBackgroundColor(Color.parseColor(colors[4]));
        noteTitle.setBackgroundColor(Color.parseColor(colors[4]));

        //Set the titles text color
        audioTitle.setTextColor(Color.parseColor(colors[5]));
        algoTitle.setTextColor(Color.parseColor(colors[5]));
        gpsTitle.setTextColor(Color.parseColor(colors[5]));
        noteTitle.setTextColor(Color.parseColor(colors[5]));
    }
}
