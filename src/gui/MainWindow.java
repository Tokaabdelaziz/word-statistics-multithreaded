package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainWindow extends JFrame {

    private final DirectoryChooserPanel directoryChooserPanel = new DirectoryChooserPanel();
    private final ResultsTable resultsTable = new ResultsTable();
    private final OptionsPanel optionsPanel = new OptionsPanel();

    public MainWindow() {
        super("Word Statistics");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setLayout(new BorderLayout(0, 8));

        add(directoryChooserPanel, BorderLayout.NORTH);
        add(resultsTable, BorderLayout.CENTER);
        add(optionsPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    public DirectoryChooserPanel getDirectoryChooserPanel() {
        return directoryChooserPanel;
    }

    public ResultsTable getResultsTable() {
        return resultsTable;
    }

    public OptionsPanel getOptionsPanel() {
        return optionsPanel;
    }
}
