package it.reader.rocksdb.gui;

import javax.swing.*;
import java.awt.*;

public class KeyTypeSelectionUI extends JDialog  {
    static String selectedOption;
    public KeyTypeSelectionUI(JFrame parent, String title, boolean modal) {
        super(parent, title, modal);
        setSize(300, 400);
        setLocationRelativeTo(parent);
        JPanel panel = new JPanel(new GridLayout(0, 1));

        ButtonGroup buttonGroup = new ButtonGroup();
        JLabel label = new JLabel("Select key type:");
        JRadioButton isStringRadioButton = new JRadioButton("Is String");
       JRadioButton isIpRangeButton = new JRadioButton("Is IP Range");
        JRadioButton isIPRadioButton = new JRadioButton("Is IP");

        buttonGroup.add(isStringRadioButton);
        buttonGroup.add(isIpRangeButton);
        buttonGroup.add(isIPRadioButton);

        JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> {
            String selectedOption = "";
            if (isStringRadioButton.isSelected()) {
                selectedOption = "Is String";
            } else if (isIpRangeButton.isSelected()) {
                selectedOption = "Is IP Range";
            } else if (isIPRadioButton.isSelected()) {
                selectedOption = "Is IP";
            }

            if (!selectedOption.isEmpty()) {



                KeyTypeSelectionUI.selectedOption = selectedOption;
                dispose();
            } else {

                KeyTypeSelectionUI.selectedOption = "Is String";
                dispose();
            }
        });
        panel.add(label);
        panel.add(isStringRadioButton);
        panel.add(isIpRangeButton);
        panel.add(isIPRadioButton);
        panel.add(okButton);
        add(panel);
    }
}


