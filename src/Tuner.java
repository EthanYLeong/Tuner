import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;

import org.jtransforms.fft.DoubleFFT_1D;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Tuner {

    TargetDataLine line;
    AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int numBytesRead;
    boolean stopped = false;
    DoubleFFT_1D FFT = new DoubleFFT_1D(2048);

    Tuner() {

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
        byte[] data = new byte[line.getBufferSize()/10];
        // System.out.println(data.length);
        line.start();
        while (!stopped){
            numBytesRead = line.read(data, 0, data.length);
            double[] doubleArray = new double[data.length/2];
            for (int i = 0, counter = 0; i < data.length - 1; i += 2, counter++){
                int low  = data[i] & 0xFF;
                int high = data[i+1] & 0xFF;
                int combined = (high << 8) | low;
                double sample = (double) combined;
                doubleArray[counter] = sample;
            }
            FFT.realForward(doubleArray);
            double lastMagnitude = 0;
            int mostFrequentBin = 1;
            for (int i = 2; i < doubleArray.length - 1; i += 2){
                double re = doubleArray[i];
                double im = doubleArray[i+1];
                int frequencyBin = i/2; 
                double magnitude = Math.sqrt((re * re) + (im * im));
                if (magnitude > lastMagnitude){
                    lastMagnitude = magnitude;
                    mostFrequentBin = frequencyBin;
                }

            }
            System.out.println((mostFrequentBin * 44100)/doubleArray.length);
            // out.write(data, 0, numBytesRead);
            // System.out.println(out);
        }
    }

}
