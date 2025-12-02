package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Bottom section that holds action buttons and status indicators.
 * Purely cosmetic; it exposes callbacks without triggering backend work.
 */
public class OptionsPanel extends JPanel {

    private final JButton startButton = new JButton("Start Preview");
    private final JButton stopButton = new JButton("Stop");
    private final JButton clearButton = new JButton("Clear Table");
    private final JLabel statusLabel = new JLabel("Waiting for input…");
    private final JProgressBar progressBar = new JProgressBar(0, 100);

    private final List<Runnable> startListeners = new ArrayList<>();
    private final List<Runnable> stopListeners = new ArrayList<>();
    private final List<Runnable> clearListeners = new ArrayList<>();

    public OptionsPanel() {
        setBorder(BorderFactory.createTitledBorder("Actions"));
        setLayout(new BorderLayout(8, 8));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 4));
        buttonRow.add(startButton);
        buttonRow.add(stopButton);
        buttonRow.add(clearButton);

        JPanel statusRow = new JPanel(new BorderLayout(8, 0));
        statusRow.add(statusLabel, BorderLayout.CENTER);
        statusRow.add(progressBar, BorderLayout.EAST);

        add(buttonRow, BorderLayout.NORTH);
        add(statusRow, BorderLayout.SOUTH);

        progressBar.setStringPainted(true);
        stopButton.setEnabled(false);

        startButton.addActionListener(e -> startListeners.forEach(Runnable::run));
        stopButton.addActionListener(e -> stopListeners.forEach(Runnable::run));
        clearButton.addActionListener(e -> clearListeners.forEach(Runnable::run));
    }

    public void addStartListener(Runnable listener) {
        startListeners.add(Objects.requireNonNull(listener));
    }

    public void addStopListener(Runnable listener) {
        stopListeners.add(Objects.requireNonNull(listener));
    }

    public void addClearListener(Runnable listener) {
        clearListeners.add(Objects.requireNonNull(listener));
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void setProgress(int progress) {
        progressBar.setValue(Math.max(0, Math.min(progress, 100)));
    }

    public void setBusy(boolean busy) {
        startButton.setEnabled(!busy);
        stopButton.setEnabled(busy);
    }

    public void reset() {
        setBusy(false);
        setProgress(0);
        setStatus("Waiting for input…");
    }
}
