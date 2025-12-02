package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Assembles the placeholder GUI described in Projektdetails.txt.
 */
public class MainWindow extends JFrame {

    private final DirectoryChooserPanel directoryChooserPanel = new DirectoryChooserPanel();
    private final ResultsTable resultsTable = new ResultsTable();
    private final OptionsPanel optionsPanel = new OptionsPanel();
    private final LiveUpdateHandler liveUpdateHandler =
            new LiveUpdateHandler(resultsTable, optionsPanel);

    public MainWindow() {
        super("Word Statistics (Preview Only)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setLayout(new BorderLayout(0, 8));

        add(directoryChooserPanel, BorderLayout.NORTH);
        add(resultsTable, BorderLayout.CENTER);
        add(optionsPanel, BorderLayout.SOUTH);

        registerCallbacks();
        setLocationRelativeTo(null);
    }

    private void registerCallbacks() {
        optionsPanel.addStartListener(() -> liveUpdateHandler.startDemo(
                directoryChooserPanel.getDirectoryPath(),
                directoryChooserPanel.isIncludeSubdirectoriesSelected()));
        optionsPanel.addStopListener(() -> liveUpdateHandler.stopDemo("Preview stopped"));
        optionsPanel.addClearListener(() -> {
            liveUpdateHandler.stopDemo("Cleared results");
            resultsTable.clearRows();
            optionsPanel.reset();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
