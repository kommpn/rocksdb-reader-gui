package it.reader.rocksdb.gui;

import it.reader.rocksdb.domain.KeyType;
import it.reader.rocksdb.service.RocksDBRepo;
import lombok.NoArgsConstructor;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;

@NoArgsConstructor
public class FileChooserUI extends JFrame implements ActionListener {


    static JLabel l;
    static JFrame f = new JFrame("RocksDB reader");
    RocksDBRepo rocksdb = new RocksDBRepo();
    static  JPanel p = new JPanel();

    JPanel panel = new JPanel(new GridLayout(10, 2, 5, 5));

    public static void initFileChooser()
    {
        f.setSize(500, 700);

        f.setVisible(true);

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton button2 = new JButton("Browse");

        FileChooserUI f1 = new FileChooserUI();

        button2.addActionListener(f1);

        p.add(button2);
        l = new JLabel("No file selected");

        p.add(l);
        f.add(p);

        f.show();
    }
    public void actionPerformed(ActionEvent evt)
    {


            JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int r = j.showOpenDialog(null);

            if (r == JFileChooser.APPROVE_OPTION)
            {

                try {
                    rocksdb.initDbReadOnly(j.getSelectedFile().getAbsolutePath());
                } catch (Exception e) {
                    l.setText("You did not select a rocksdb");
                    return;
                }
                        CustomDialog modal = new CustomDialog(f, "Value serialized", true);
                        modal.setVisible(true);

                        boolean result = modal.getUserChoice();
                        f.getContentPane().removeAll();
                        f.add(createRocksGrid(result));
                        f.revalidate();
                        f.setVisible(true);
            }

        }

    public JPanel createRocksGrid(boolean isJson) {
        JPanel container = new JPanel(new BorderLayout());

        JButton backButt = new JButton("Choose another rocks");
        backButt.addActionListener(it -> {
            panel.removeAll();
            container.remove(panel);
            f.getContentPane().removeAll();
            p.removeAll();
            initFileChooser();
            rocksdb.close();

            f.revalidate();
            f.repaint();
        });
        container.add(backButt, BorderLayout.NORTH);

        try {


            populateGrid(new LinkedHashMap<>(rocksdb.getWholeDb(CustomDialog.keyType, isJson)));

        } catch (Exception e) {
            JPanel errorPanel = new JPanel(new BorderLayout());
            JLabel errorLabel = new JLabel("Errore: " + e.getMessage());
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            errorPanel.add(errorLabel, BorderLayout.CENTER);
            container.add(errorPanel, BorderLayout.CENTER);

            rocksdb.close();
            return container;
        }



        container.add(panel, BorderLayout.CENTER);



        return container;
    }

    public void handleException(JPanel container, String errorMessage) {
        JPanel errorPanel = new JPanel(new BorderLayout());
        JLabel errorLabel = new JLabel("Errore: " + errorMessage);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorPanel.add(errorLabel, BorderLayout.CENTER);

        container.removeAll();
        container.add(errorPanel, BorderLayout.CENTER);

        container.revalidate();
        container.repaint();
    }



    public void populateGrid(LinkedHashMap<String, String> resultsMap) {

        panel.setLayout(new BorderLayout());
        panel.add(new PaginatedTableUI(resultsMap));
        panel.setVisible(true);
    }



    static class CustomDialog extends JDialog {
        private boolean userChoice = false;

        static public KeyType keyType = new KeyType();

        public CustomDialog(JFrame parent, String title, boolean modal) {
            super(parent, title, modal);
            setSize(500, 200);
            setLocationRelativeTo(parent);

            JPanel modalPanel = new JPanel();
            JLabel label = new JLabel("Does this rocksdb contain JSON serialized values?");

            JButton yesButton = new JButton("Yes");
            JButton noButton = new JButton("No");

            yesButton.addActionListener(e -> {
                userChoice = true;
                setKeyType(KeyTypeSelectionUI.selectedOption);
                dispose();
            });

            noButton.addActionListener(e -> {
                userChoice = false;
                setKeyType(KeyTypeSelectionUI.selectedOption);
                dispose();
            });

            modalPanel.add(label);
            KeyTypeSelectionUI selectionModal = new KeyTypeSelectionUI(f, "Key format selection", true);
            selectionModal.setVisible(true);
            modalPanel.add(yesButton);
            modalPanel.add(noButton);
            add(modalPanel);
        }

        public void setKeyType(String selectedKeyType) {
            if (selectedKeyType.equals("Is IP")) {
                keyType.setIp(true);
            } else if(selectedKeyType.equals("Is IP Range")) {
                keyType.setIpRange(true);
            } else {
                keyType.setString(true);
            }
        }

        public boolean getUserChoice() {
            return userChoice;
        }
    }




//     p.add(new JLabel("<html>Welcome to RocksDB reader.<br>Select your Rocks directory to read its content." +
//                "<br>If the content is a json that has been serialized, select yes on the dialog. Otherwhise, select no.<br>Have fun</html>"),
//                BorderLayout.NORTH);
}

