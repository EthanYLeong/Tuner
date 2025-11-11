import java.io.ByteArrayOutputStream;
import java.util.HashMap;

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

    HashMap<Integer, String> notesMap = new HashMap<Integer, String>();

    Tuner(Gui gui) {
        notesMap.put(0, "A");
        notesMap.put(-11, "A#");
        notesMap.put(1, "A#");
        notesMap.put(-10, "B");
        notesMap.put(2, "B");
        notesMap.put(-9, "C");
        notesMap.put(3, "C");
        notesMap.put(-8, "C#");
        notesMap.put(4, "C#");
        notesMap.put(-7, "D");
        notesMap.put(5, "D");
        notesMap.put(-6, "D#");
        notesMap.put(6, "D#");
        notesMap.put(-5, "E");
        notesMap.put(7, "E");
        notesMap.put(-4, "F");
        notesMap.put(8, "F");
        notesMap.put(-3, "F#");
        notesMap.put(9, "F#");
        notesMap.put(-2, "G");
        notesMap.put(10, "G");
        notesMap.put(-1, "G#");
        notesMap.put(11, "G#");

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
            if (largestAmplitude < 750){
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
            double frequency = (mostFrequentBin * 44100.0)/doubleArray.length;
            double numOfSemitones = 12 * (Math.log((frequency/440))/Math.log(2));
            System.out.println(frequency + " semit " + numOfSemitones);
            double lower = Math.floor(numOfSemitones);
            double upper = Math.ceil(numOfSemitones);
            double closest = 0;
            String noteName = "";
            if (numOfSemitones - lower > upper - numOfSemitones){
                closest = upper;
            } else {
                closest = lower;
            }

            int semitoneDifference = (int) closest % 12;

            noteName = notesMap.get(semitoneDifference);

            double correctPitchFrequency = 440 * Math.pow(1.05946, closest);
            double cents = 1200 * (Math.log(frequency/correctPitchFrequency)/Math.log(2));
            cents = Math.floor(cents * 10)/10;
            double octave = 4;
            // had to artificially lower C4 in order for C6 to be caputred
            double octaveDifference = Math.log(frequency/261)/Math.log(2);
            if (octaveDifference >= 0){
                octaveDifference = Math.floor(octaveDifference);
                octave += octaveDifference;
            } else {
                octaveDifference = 1/octaveDifference;
                octaveDifference = Math.ceil(octaveDifference);
                octave -= octaveDifference;
            }

            System.out.println(octave);

            String noteAndOctave = "<html>" + noteName + "<sub>" + (int)(octave) + "</sub></html>";

            gui.updateFrequency(frequency, noteAndOctave, cents);
            // out.write(data, 0, numBytesRead); 
            // System.out.println(out);
        }
    }

}
