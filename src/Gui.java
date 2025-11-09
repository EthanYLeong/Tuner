import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class Gui {

    int frameWidth = 600;
    int frameHeight = 600;
    JFrame frame = new JFrame("goated tuner");
    JPanel textPanel = new JPanel();
    JPanel frequencyPanel = new JPanel();
    JLabel title = new JLabel();
    JLabel frequencyText = new JLabel();


    Gui () {
        frame.setVisible(true);
        frame.setSize(frameWidth, frameHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setBackground(Color.darkGray);
        frame.getContentPane().setBackground(Color.darkGray);

        title.setBackground(Color.gray);
        title.setForeground(Color.white);
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 50));
        title.setText("epic tuner");
        title.setOpaque(true);

        frequencyText.setBackground(Color.gray);
        frequencyText.setForeground(Color.white);
        frequencyText.setHorizontalAlignment(JLabel.CENTER);
        frequencyText.setFont(new Font("Arial", Font.BOLD, 100));
        frequencyText.setText("100");
        frequencyText.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(title);

        frequencyPanel.setLayout(new BorderLayout());
        frequencyPanel.add(frequencyText);
        

        frame.add(textPanel, BorderLayout.NORTH);
        frame.add(frequencyPanel);
    }

    void updateFrequency(double frequency, boolean loudEnough){
        frequencyText.setText(Double.toString(frequency));
    }

    void loudEnoughColor(boolean loudEnough){
        if (loudEnough){
            frequencyText.setBackground(Color.green);
        } else {
            frequencyText.setBackground(Color.red);
        }
    }

}
