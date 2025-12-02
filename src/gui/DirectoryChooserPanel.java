package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

/**
 * Top section of the UI that lets the user pick a directory
 * and control whether subdirectories should be considered.
 * This panel is presentation-only and does not invoke any
 * backend logic.
 */
public class DirectoryChooserPanel extends JPanel {

    private final JTextField directoryField = new JTextField(30);
    private final JButton browseButton = new JButton("Browse…");
    private final JCheckBox includeSubdirectoriesBox =
            new JCheckBox("Include subdirectories");
    private final List<DirectorySelectionListener> listeners = new ArrayList<>();

    public DirectoryChooserPanel() {
        setBorder(BorderFactory.createTitledBorder("Input Directory"));
        setLayout(new BorderLayout(8, 0));

        JLabel directoryLabel = new JLabel("Directory path:");
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 4));
        leftPanel.add(directoryLabel);
        leftPanel.add(directoryField);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 4));
        rightPanel.add(browseButton);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.add(leftPanel, BorderLayout.CENTER);
        topRow.add(rightPanel, BorderLayout.EAST);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEADING));
        bottomRow.add(includeSubdirectoriesBox);

        add(topRow, BorderLayout.CENTER);
        add(bottomRow, BorderLayout.SOUTH);

        directoryField.addActionListener(e -> notifyDirectorySelection());
        browseButton.addActionListener(e -> openFileChooser());
    }

    public void setDirectoryPath(String path) {
        directoryField.setText(path);
    }

    public String getDirectoryPath() {
        return directoryField.getText().trim();
    }

    public boolean isIncludeSubdirectoriesSelected() {
        return includeSubdirectoriesBox.isSelected();
    }

    public void setIncludeSubdirectories(boolean selected) {
        includeSubdirectoriesBox.setSelected(selected);
    }

    public void addDirectorySelectionListener(DirectorySelectionListener listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    private void openFileChooser() {
        JFileChooser chooser = new JFileChooser(
                FileSystemView.getFileSystemView().getHomeDirectory());
        chooser.setDialogTitle("Choose project directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            directoryField.setText(selected.getAbsolutePath());
            notifyDirectorySelection();
        }
    }

    private void notifyDirectorySelection() {
        String path = getDirectoryPath();
        listeners.forEach(listener -> listener.onDirectorySelected(path));
    }

    public interface DirectorySelectionListener {
        void onDirectorySelected(String directoryPath);
    }
}
