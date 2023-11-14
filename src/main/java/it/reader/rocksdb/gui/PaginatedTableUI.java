package it.reader.rocksdb.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PaginatedTableUI extends JPanel {
    private int pageSize = 29;
    private final List<Map.Entry<String, String>> data;
    private int currentPage = 0;

    private final DefaultTableModel model;
    private final JTable table;
    private final JButton nextButton;
    private final JButton prevButton;

    public PaginatedTableUI(Map<String, String> keyValueMap) {
        setLayout(new BorderLayout());


        data = new ArrayList<>(keyValueMap.entrySet());
        model = new DefaultTableModel();
        model.addColumn("Chiave");
        model.addColumn("Valore");

        table = new JTable(model);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (table.hasFocus() && table.isFocusOwner() && evt.isControlDown()) {
                int row = table.rowAtPoint(evt.getPoint());
                int col = table.columnAtPoint(evt.getPoint());

                if (row >= 0 && col >= 0) {
                    Object cellValue = table.getValueAt(row, col);

                    
                    if (cellValue != null) {
                        String textToCopy = cellValue.toString();
                        copyToClipboard(textToCopy);

                        
                        showTooltip(table, evt.getX(), evt.getY(), "Value copied to clipboard: " + textToCopy);
                    }
                }
                }
            }
        });
        nextButton = new JButton("Next");
        prevButton = new JButton("Previous");

        
        JTextField pageSizeInput = new JTextField(5); 
        JButton setPageSizeButton = new JButton("Set page size");

        JPanel pageSizePanel = new JPanel(); 
        pageSizePanel.add(new JLabel("Page size: "));
        pageSizePanel.add(pageSizeInput);
        pageSizePanel.add(setPageSizeButton);

        JPanel searchValuePanel = new JPanel(); 
        searchValuePanel.add(new JLabel("Search: "));
        JTextField searchValueInput = new JTextField(15);
        searchValuePanel.add(searchValueInput);
        JButton searchValueInputButton = new JButton("Search key");
        searchValuePanel.add(searchValueInputButton);

        JPanel resetPanel = new JPanel(); 
        JButton resetButton = new JButton("Reset search");
        resetPanel.add(resetButton);

        resetButton.addActionListener(e -> {
            try {
               currentPage = 0;
                updateTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ricerca del valore non riuscita");
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel navigationPanel = new JPanel();
        navigationPanel.add(prevButton);
        navigationPanel.add(nextButton);
        buttonPanel.add(navigationPanel);
        buttonPanel.add(resetPanel);
        buttonPanel.add(searchValuePanel);
        buttonPanel.add(pageSizePanel);
        nextButton.addActionListener(e -> nextPage());
        prevButton.addActionListener(e -> prevPage());
        
        setPageSizeButton.addActionListener(e -> {
            try {
                pageSize = Integer.parseInt(pageSizeInput.getText());
                currentPage = 0;
                updateTable(); 
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Inserire un numero valido per la dimensione della pagina.");
            }
        });

        searchValueInputButton.addActionListener(e -> {
            try {
                String valueToSearch = searchValueInput.getText();
                updateTable(valueToSearch); 
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ricerca del valore non riuscita");
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        updateTable();

    }

    private void updateTable() {
        model.setRowCount(0);
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, data.size());

        for (int i = start; i < end; i++) {
            Map.Entry<String, String> entry = data.get(i);
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }

    private void updateTable(String searchValue) {
        model.setRowCount(0);
        List<Map.Entry<String, String>> results = new ArrayList<>();
         for (Map.Entry<String, String> datum : data) {
            if(datum.getKey().contains(searchValue)) {
                results.add(datum);
            }
        }
        for (Map.Entry<String, String> result : results) {
            model.addRow(new Object[]{result.getKey(), result.getValue()});
        }


    }

    private void nextPage() {
        currentPage++;
        if (currentPage >= (data.size() + pageSize - 1) / pageSize) {
            currentPage = (data.size() + pageSize - 1) / pageSize - 1;
        }
        updateTable();
    }

    private void prevPage() {
        currentPage--;
        if (currentPage < 0) {
            currentPage = 0;
        }
        updateTable();
    }
    private static void copyToClipboard(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferableText = new StringSelection(text);
        clipboard.setContents(transferableText, null);
    }

    private static void showTooltip(JComponent parent, int x, int y, String text) {

        JToolTip tooltip = new JToolTip() {
            @Override
            public Color getBackground() {
                return Color.GREEN;
            }
        };
        tooltip.setComponent(parent);
        tooltip.setLayout(new BorderLayout());
        JLabel label = new JLabel(text);
        label.setFont( new Font("Arial", Font.BOLD, 16));
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tooltip.add(label, BorderLayout.CENTER);
        tooltip.setPreferredSize(new Dimension(text.length()*8, 25)); 
        tooltip.setLocation(parent.getLocationOnScreen().x + x + 500, parent.getLocationOnScreen().y + y + 500);
        tooltip.setVisible(true);

       
        JWindow window = new JWindow();
        window.getContentPane().add(tooltip);
        window.pack();
        window.setVisible(true);

        Timer timer = new Timer(3500, e -> window.dispose());
        timer.setRepeats(false);
        timer.start();
    }
}
