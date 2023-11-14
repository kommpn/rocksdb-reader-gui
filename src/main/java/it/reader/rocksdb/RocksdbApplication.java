package it.reader.rocksdb;


import it.reader.rocksdb.gui.FileChooserUI;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
public class RocksdbApplication extends JFrame {


	public RocksdbApplication() {
		InstructionDialog id = new InstructionDialog(this, "Welcome to RocksDB reader", true);
		id.setVisible(true);
		FileChooserUI.initFileChooser();
	}


	public static void main(String[] args) {

		var ctx = new SpringApplicationBuilder(RocksdbApplication.class)
				.headless(false).web(WebApplicationType.NONE).run(args);


	}

	static class InstructionDialog extends JDialog {

		public InstructionDialog(JFrame parent, String title, boolean modal) {
			super(parent, title, modal);
			setSize(350, 200);
			setLocationRelativeTo(parent);

			JPanel modalPanel = new JPanel();
			modalPanel.setLayout(new BorderLayout());
			JLabel label = new JLabel("<html>Welcome to RocksDB reader." +
					"<br>Select your Rocks directory to read its content." +
					"<br>If the content is a json that has been serialized, <br>select yes on the dialog." +
					"<br> Otherwise, select no.<br>By pressing 'control'/'ctrl' and clicking on a cell you" +
					"<br>will copy its value. <br>Have fun</html>");
			label.setHorizontalAlignment(SwingConstants.CENTER);
			modalPanel.add(label, BorderLayout.CENTER);
			JButton okButton = new JButton("Ok");

			okButton.addActionListener(e -> {
				dispose();
			});

			modalPanel.add(okButton, BorderLayout.AFTER_LAST_LINE);

			add(modalPanel);
		}

	}

}