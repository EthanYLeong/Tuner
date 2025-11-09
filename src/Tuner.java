import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;

import org.jtransforms.fft.DoubleFFT_1D;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Tuner {

    Gui gui;

    TargetDataLine line;
    AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int numBytesRead;
    boolean stopped = false;
    DoubleFFT_1D FFT = new DoubleFFT_1D(22050);
    boolean loudEnough = false;

    Tuner(Gui gui) {
        this.gui = gui;

        if (!AudioSystem.isLineSupported(info)) {
            // Handle the error.
        }
        // Obtain and open the line.
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (LineUnavailableException ex) {
            // Handle the error.
            // ...
        }

        start();
    }

    void start(){
        // buffer size is half a second worth of samples
        // so data is a quarter of a second of sample
        byte[] data = new byte[line.getBufferSize()/2];
        line.start();
        while (!stopped){
            numBytesRead = line.read(data, 0, data.length);
            double[] doubleArray = new double[data.length];
            for (int i = 0, counter = 0; i < data.length - 1; i += 2, counter++){
                int low  = data[i] & 0xFF;
                int high = data[i+1] & 0xFF;
                short combined = (short) ((high << 8) | low);
                double sample = (double) combined;
                doubleArray[counter] = sample;
            }

            double largestAmplitude = 0;
            for (int i = 0; i < doubleArray.length; i++){
                if (Math.abs(doubleArray[i]) > largestAmplitude){
                    largestAmplitude = Math.abs(doubleArray[i]);
                }
            }
            if (largestAmplitude < 1000){
                loudEnough = false;
                gui.loudEnoughColor(loudEnough);
                continue;
            }
            loudEnough = true;
            gui.loudEnoughColor(loudEnough);
            FFT.realForward(doubleArray);
            double strongestMagnitude = 0;
            int mostFrequentBin = 1;
            // start at index 2, as 1 and 0 contain info about mean, frequency bins start
            // at index 2
            for (int i = 2; i < doubleArray.length/2; i += 2){
                double re = doubleArray[i];
                double im = doubleArray[i+1];
                int frequencyBin = i/2; 
                double magnitude = Math.sqrt((re * re) + (im * im));
                if (magnitude > strongestMagnitude){
                    strongestMagnitude = magnitude;
                    mostFrequentBin = frequencyBin;
                }

            }
            // num of cycles over time period / (seconds of time period)
            double frequency =(mostFrequentBin * 44100.0)/doubleArray.length;
            double numOfSemitones = 12 * (Math.log((frequency/440))/Math.log(2));
            System.out.println(frequency + " semit " + numOfSemitones);
            double lower = Math.floor(numOfSemitones);
            double upper = Math.ceil(numOfSemitones);
            if (numOfSemitones - lower > upper - numOfSemitones){
                double closest = upper;
            } else {
                double closest = lower;
            }

             

            gui.updateFrequency(frequency, loudEnough);
            // out.write(data, 0, numBytesRead); 
            // System.out.println(out);
        }
    }

}
