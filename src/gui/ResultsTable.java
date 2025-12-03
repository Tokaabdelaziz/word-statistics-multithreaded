package gui;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Read-only table that shows the latest statistics per file along with
 * aggregated directory highlights.
 */
public class ResultsTable extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel directoryLongestLabel = new JLabel("Longest word: —");
    private final JLabel directoryShortestLabel = new JLabel("Shortest word: —");

    public ResultsTable() {
        setBorder(BorderFactory.createTitledBorder("Live Statistics"));
        setLayout(new BorderLayout(0, 8));

        tableModel = new DefaultTableModel(new Object[]{
                "File", "#words", "#is", "#are", "#you",
                "Longest word", "Shortest word"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);

        // ⬇️ NEW: render only file name, but keep full path in the model
        DefaultTableCellRenderer fileNameRenderer = new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                if (value instanceof String) {
                    String path = (String) value;
                    String name = new File(path).getName(); // just "file.txt"
                    super.setValue(name);
                } else {
                    super.setValue(value);
                }
            }
        };
        table.getColumnModel().getColumn(0).setCellRenderer(fileNameRenderer);
        // ⬆️ NEW

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel directorySummary = new JPanel(new BorderLayout(8, 0));
        directorySummary.add(directoryLongestLabel, BorderLayout.WEST);
        directorySummary.add(directoryShortestLabel, BorderLayout.CENTER);

        add(scrollPane, BorderLayout.CENTER);
        add(directorySummary, BorderLayout.SOUTH);
    }

    public void clearRows() {
        tableModel.setRowCount(0);
        updateDirectorySummary("—", "—");
    }

    public void addResultRow(ResultRow row) {
        Objects.requireNonNull(row, "row");
        tableModel.addRow(new Object[]{
                row.fileName(),
                row.wordCount(),
                row.isCount(),
                row.areCount(),
                row.youCount(),
                row.longestWord(),
                row.shortestWord()
        });
    }

    public void addOrUpdateRow(ResultRow row) {
        Objects.requireNonNull(row, "row");

        // Check if file already exists in table (by full path)
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String existingFileName = (String) tableModel.getValueAt(i, 0);
            if (existingFileName.equals(row.fileName())) {
                // Update existing row
                tableModel.setValueAt(row.wordCount(), i, 1);
                tableModel.setValueAt(row.isCount(), i, 2);
                tableModel.setValueAt(row.areCount(), i, 3);
                tableModel.setValueAt(row.youCount(), i, 4);
                tableModel.setValueAt(row.longestWord(), i, 5);
                tableModel.setValueAt(row.shortestWord(), i, 6);
                return;
            }
        }

        // Add new row if not found
        addResultRow(row);
    }

    public void removeRow(String fileName) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String existingFileName = (String) tableModel.getValueAt(i, 0);
            if (existingFileName.equals(fileName)) {
                tableModel.removeRow(i);
                return;
            }
        }
    }

    public void updateDirectorySummary(String longest, String shortest) {
        directoryLongestLabel.setText("Longest word: " + longest);
        directoryShortestLabel.setText("Shortest word: " + shortest);
    }

    public record ResultRow(
            String fileName,
            int wordCount,
            int isCount,
            int areCount,
            int youCount,
            String longestWord,
            String shortestWord
    ) {
    }
}
