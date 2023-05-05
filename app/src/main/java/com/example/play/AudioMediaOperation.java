package com.example.play;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class AudioMediaOperation {
    public interface OperationCallbacks {
        public void onAudioOperationFinished();

        public void onAudioOperationError(Exception e);
    }

    public static void MergeAudios(AssetFileDescriptor fd[], String outpath,FileOutputStream fos) {
        int RECORDER_SAMPLERATE = 22050;
        try {
            Log.e("merge", "start");
          //  DataOutputStream amplifyOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outpath)));
            DataInputStream[] mergeFilesStream = new DataInputStream[fd.length];
            long[] sizes = new long[fd.length];
            for (int i = 0; i < fd.length; i++) {
                Long size = fd[i].getLength();

                sizes[i] = (fd[i].getLength() - 44) / 2;
            }


            for (int i = 0; i < fd.length; i++) {
                mergeFilesStream[i] = new DataInputStream(new BufferedInputStream(new FileInputStream(fd[i].getFileDescriptor())));

                if (i == fd.length - 1) {
                    mergeFilesStream[i].skip(24);
                    byte[] sampleRt = new byte[4];
                    mergeFilesStream[i].read(sampleRt);
                    ByteBuffer bbInt = ByteBuffer.wrap(sampleRt).order(ByteOrder.LITTLE_ENDIAN);
                    RECORDER_SAMPLERATE = bbInt.getInt();
                    mergeFilesStream[i].skip(16);
                } else {
                    mergeFilesStream[i].skip(44);
                }
            }
            ArrayList<Byte> b1 = new ArrayList<>();
            for (int b = 0; b < fd.length; b++) {
                for (int i = 0; i < (int) sizes[b]; i++) {
                    byte[] dataBytes = new byte[2];
                    try {
                        dataBytes[0] = mergeFilesStream[b].readByte();
                        dataBytes[1] = mergeFilesStream[b].readByte();
                    } catch (Exception e) {
                        //amplifyOutputStream.close();
                    }
                    short dataInShort = ByteBuffer.wrap(dataBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    float dataInFloat = (float) dataInShort / 37268.0f;

                    short outputSample = (short) (dataInFloat * 37268.0f);
                    byte[] dataFin = new byte[2];
                    dataFin[0] = (byte) (outputSample & 0xff);
                    dataFin[1] = (byte) ((outputSample >> 8) & 0xff);
                    b1.add(dataFin[0]);
                    b1.add(dataFin[1]);
//                    amplifyOutputStream.write(dataFin, 0 , 2);

                }
            }
            byte b2[] = new byte[b1.size()];
            for (int i = 0; i < b1.size(); i++) {
                b2[i] = b1.get(i);
            }


            DataOutputStream output = null;
            try {
                //  File myFile = new File("final" + ".xls");

                output = new DataOutputStream(fos);

                writeString(output, "RIFF"); // chunk id
                writeInt(output, 36 + b2.length); // chunk size
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
                writeInt(output, b2.length); // subchunk 2 size
                output.write(b2);

            } finally {
                if (output != null) {
                    Log.e("saving","here");
                    output.close();
                }
            }

        } catch (Exception e) {
            Log.e("here", e.toString());
            System.out.println(e);
            System.exit(1);
        }


        }


    public static byte[] convertStreamToByteArray(FileInputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[10240];
        int i = Integer.MAX_VALUE;
        while ((i = is.read(buff, 0, buff.length)) > 0) {
            baos.write(buff, 0, i);
        }

        return baos.toByteArray(); // be sure to close InputStream in calling function
    }

    static public byte[] fullyReadFileToBytes(int i, Context context) throws IOException {
        File outFile = null;
        try {
            outFile = new File(new URI("android.resource://" + context.getPackageName() + "//" + "apologies").getRawPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        int size = (int) outFile.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis = new FileInputStream(outFile);
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        } catch (Exception e) {
            Log.e("here", e.toString());
        } finally {
            fis.close();
        }

        return bytes;
    }

    static public void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    static public void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    static public void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

}