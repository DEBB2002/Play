package com.example.play.audio_mixer.resample;

import androidx.annotation.NonNull;

import java.nio.ShortBuffer;

//import zeroonezero.android.audio_mixer.resample.AudioResampler;

public class DefaultAudioResampler implements AudioResampler {

    @Override
    public void resample(@NonNull ShortBuffer inputBuffer, int inputSampleRate, @NonNull ShortBuffer outputBuffer, int outputSampleRate, int channels) {
        if (inputSampleRate < outputSampleRate) {
            UPSAMPLE.resample(inputBuffer, inputSampleRate, outputBuffer, outputSampleRate, channels);
        } else if (inputSampleRate > outputSampleRate) {
            DOWNSAMPLE.resample(inputBuffer, inputSampleRate, outputBuffer, outputSampleRate, channels);
        } else {
            PASSTHROUGH.resample(inputBuffer, inputSampleRate, outputBuffer, outputSampleRate, channels);
        }
    }
}
