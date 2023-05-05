package com.example.play;

import static com.example.play.AudioMediaOperation.writeInt;
import static com.example.play.AudioMediaOperation.writeShort;
import static com.example.play.AudioMediaOperation.writeString;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.play.audio_mixer.AudioMixer;
import com.example.play.audio_mixer.input.AudioInput;
import com.example.play.audio_mixer.input.GeneralAudioInput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;


public class MainActivity3 extends AppCompatActivity {
    // Storage Permissions
    MediaPlayer mMediaPlayer;
    Button play, stop;
    boolean issaved = false;
    String files[] = {"found.wav", "eight.wav", "contacts_for.wav", "deb_biswas.txt", "please_select_one_from_the_below_list.wav"};

    int storecount = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        mMediaPlayer = new MediaPlayer();
        play = findViewById(R.id.play);
        stop = findViewById(R.id.stop);
        for (int i = 0; i < files.length; i++) {
            int index = files[i].lastIndexOf('.');
            String extension = "";
            if (index > 0) {
                extension = files[i].substring(index + 1);
            }
            Log.e("ext", extension);
            if (extension.contentEquals("txt")) {
                try {//Log.e("Load","audio");
                    extract_double(files[i]);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


            }
        }
        save();
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (issaved) {
                    play.setClickable(false);
                    play();
                } else {
                    Toast.makeText(getApplicationContext(), "Processing Audio", Toast.LENGTH_SHORT).show();


                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mMediaPlayer.stop();
                }
                mMediaPlayer = new MediaPlayer();
                play.setClickable(true);
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mMediaPlayer = new MediaPlayer();
                play.setClickable(true);
            }
        });
        //   save();
    }

    public void save() {


        try {
            FileDescriptor fileDescriptors[] = new FileDescriptor[files.length];
            ArrayList<AudioInput> audioInputs = new ArrayList<>();
            for (int i = 0; i < fileDescriptors.length; i++) {
                int index = files[i].lastIndexOf('.');
                String extension = "";
                if (index > 0) {
                    extension = files[i].substring(index + 1);
                    // System.out.println("File extension is " + extension);
                }
                Log.e("ext", extension);
                if (extension.contentEquals("txt")) {
                    try {//Log.e("Load","audio");
                        String temp = files[i].replace("txt", "wav");
                        FileInputStream fis = openFileInput(temp);

                        audioInputs.add(new GeneralAudioInput(fis.getFD()));


                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


                } else {
                    AssetFileDescriptor afd = getAssets().openFd(files[i]);
                    audioInputs.add(new GeneralAudioInput(afd));

                }


            }


            FileOutputStream f2 = openFileOutput("final2.wav", Context.MODE_PRIVATE);
            final AudioMixer audioMixer = new AudioMixer(f2.getFD());

            for (int i = 0; i < audioInputs.size(); i++) {
                audioMixer.addDataSource(audioInputs.get(i));
            }
            audioMixer.setSampleRate(44100); // Optional
            audioMixer.setBitRate(128000); // Optional
            audioMixer.setChannelCount(1); // Optional //1(mono) or 2(stereo)

            Log.e("prog", String.valueOf(100));
            audioMixer.setMixingType(AudioMixer.MixingType.SEQUENTIAL);
            audioMixer.setProcessingListener(new AudioMixer.ProcessingListener() {
                @Override
                public void onProgress(final double progress) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("prog", String.valueOf(progress * 100));
                        }
                    });
                }

                @Override
                public void onEnd() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Success!!!", Toast.LENGTH_SHORT).show();
                            audioMixer.release();

                            issaved = true;

                        }
                    });
                }
            });

            audioMixer.start();
            audioMixer.processAsync();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void play() {
        try {
            FileInputStream fis = openFileInput("final2.wav");


            Log.e("here", fis.getFD().toString());

            mMediaPlayer = new MediaPlayer();
            // mPlayer.prepare();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(fis.getFD());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mMediaPlayer = new MediaPlayer();
                    play.setClickable(true);
                }
            });
        } catch (Exception e) {

            Log.e("Playing", e.toString());

        }
    }


    private void extract_double(String file) {

        try {
            char start = '[';
            char end = ']';
            InputStream inputStream = getAssets().open(file);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            String string = new String(buffer);
            string = string.replace(start, ' ');
            string = string.replace(end, ' ');
            string = string.trim();
            String[] tokens = string.split(",");
            // final FileInputStream file = (FileInputStream) getAssets().open("ac.txt");
//sc=new Scanner(file);


            //  sc.useDelimiter(",");   //sets the delimiter pattern
            ArrayList<Double> data = new ArrayList<>();
            for (int i = 0; i < tokens.length; i++)//returns a boolean value
            {
                data.add(Double.parseDouble(tokens[i].trim()));

            }
            //sc.close();


            Double[] arr = new Double[data.size()];
            arr = data.toArray(arr);
            double dat[] = new double[data.size()];
            for (int i = 0; i < arr.length; i++) {
                dat[i] = arr[i];
            }

            convert_to_wav(dat, file);
        } catch (Exception e) {


        }


    }

    private void convert_to_wav(double[] input, String file) {
        try {
            // AudioFormat format = new AudioFormat(22050, 16, 1, true,false);
            byte[] data = new byte[2 * input.length];
            Byte[] data2 = new Byte[2 * input.length];
            for (int i = 0; i < input.length; i++) {
                int temp = (short) (input[i] * Short.MAX_VALUE);
                data[2 * i + 0] = (byte) temp;
                data2[2 * i + 0] = (byte) temp;
                data[2 * i + 1] = (byte) (temp >> 8);
                data2[2 * i + 1] = (byte) (temp >> 8);
            }


            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                //  AudioInputStream ais = new AudioInputStream(bais, format, input.length);
                Log.e("here", "doing");
                DataOutputStream output = null;
                try {
                    file = file.replaceAll("txt", "wav");
                    //  File myFile = new File("final" + ".xls");
                    FileOutputStream fos = openFileOutput(file, Context.MODE_PRIVATE);
                    output = new DataOutputStream(fos);

                    writeString(output, "RIFF"); // chunk id
                    writeInt(output, 36 + data.length); // chunk size
                    writeString(output, "WAVE"); // format
                    writeString(output, "fmt "); // subchunk 1 id
                    writeInt(output, 16); // subchunk 1 size
                    writeShort(output, (short) 1); // audio format (1 = PCM)
                    writeShort(output, (short) 1); // number of channels
                    writeInt(output, 22050); // sample rate
                    writeInt(output, 22050 * 2); // byte rate
                    writeShort(output, (short) 1); // block align
                    writeShort(output, (short) 16); // bits per sample
                    writeString(output, "data"); // subchunk 2 id
                    writeInt(output, data.length); // subchunk 2 size
                    output.write(data);

                } finally {
                    if (output != null) {
                        output.close();
                    }
                }

            } catch (Exception e) {
                Log.e("here", e.toString());
                System.out.println(e);
                System.exit(1);
            }

        } catch (Exception e) {
            Log.e("exception", e.toString());

        }

    }

    public void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    public void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    public void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }
}