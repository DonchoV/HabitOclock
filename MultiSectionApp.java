import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class MultiSectionApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JLabel clockLabel;

    private JPanel habitList;
    private final File habitFile = new File("habits.txt");
    private Font defaultFont;

    // ---------------- Streaks ----------------
    private final File streakFile = new File("habits_meta.txt");
    private final java.util.Map<String, Integer> streakMap = new java.util.HashMap<>();
    private final java.util.Map<String, LocalDate> lastCompletedMap = new java.util.HashMap<>();
    private LocalDate lastCheckedDate = LocalDate.now();

    // ---------------- Statistics ----------------
    private JPanel statisticsPanel;
    private JTable statsTable;

    public MultiSectionApp() {
        setTitle("Habit Clock");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Load custom font (fallback if fails)
        try {
            defaultFont = Font.createFont(Font.TRUETYPE_FONT, new File("resources/fonts/PixelifySans-VariableFont_wght.ttf"))
                    .deriveFont(Font.BOLD, 48f);
            UIManager.put("Label.font", defaultFont.deriveFont(Font.BOLD, 14f));
            UIManager.put("Button.font", defaultFont.deriveFont(Font.BOLD, 12f));
            UIManager.put("Spinner.font", defaultFont.deriveFont(Font.PLAIN, 12f));
        } catch (Exception e) {
            defaultFont = new Font("Monospaced", Font.BOLD, 48);
        }

        setIconImage(Toolkit.getDefaultToolkit().getImage("resources\\images\\habit0clock.png"));

        // CardLayout for switching sections
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // --- Create sections ---
        JPanel homeSection = createHomeSection(defaultFont);
        JPanel habitSection = createHabitSection();
        JPanel aboutSection = createAboutSection();
        JPanel pomodoroPanel = createPomodoroSection(defaultFont);
        JPanel statsSection = createStatisticsSection();

        mainPanel.add(homeSection, "Home");
        mainPanel.add(habitSection, "Habit Section");
        mainPanel.add(aboutSection, "About");
        mainPanel.add(pomodoroPanel, "Pomodoro");
        mainPanel.add(statsSection, "Statistics");

        // --- Navigation ---
        JPanel navPanel = new JPanel();
        navPanel.setBackground(Color.black);

        JButton homeBtn = new JButton("Home");
        JButton pomodoroBtn = new JButton("Pomodoro");
        JButton aboutBtn = new JButton("About");
        JButton habitBtn = new JButton("Habit");
        JButton statsBtn = new JButton("Statistics");

        homeBtn.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
        pomodoroBtn.addActionListener(e -> cardLayout.show(mainPanel, "Pomodoro"));
        aboutBtn.addActionListener(e -> cardLayout.show(mainPanel, "About"));
        habitBtn.addActionListener(e -> cardLayout.show(mainPanel, "Habit Section"));
        statsBtn.addActionListener(e -> {
            updateStatisticsTable();
            cardLayout.show(mainPanel, "Statistics");
        });

        JButton[] buttons = {homeBtn, pomodoroBtn, aboutBtn, habitBtn, statsBtn};
        for (JButton b : buttons) {
            b.setForeground(Color.white);
            b.setBackground(Color.black);
            b.setBorder(null);
            navPanel.add(b);
        }

        setLayout(new BorderLayout());
        add(navPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        cardLayout.show(mainPanel, "Home"); // default page

        // Load habits & streaks
        loadHabits();
        attachHabitListListener();
        loadStreaks();
        updateAllTooltips();
        initStreakDisplay();
        startDailyResetTimer();

        setVisible(true);
    }

    // ---------------- Home Section ----------------
    private JPanel createHomeSection(Font font) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.black);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        clockLabel = new JLabel("", SwingConstants.CENTER);
        clockLabel.setFont(font.deriveFont(Font.BOLD, 60f));
        clockLabel.setForeground(Color.white);
        clockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel resetLabel = new JLabel("", SwingConstants.CENTER);
        resetLabel.setFont(font.deriveFont(Font.PLAIN, 18f));
        resetLabel.setForeground(Color.LIGHT_GRAY);
        resetLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(clockLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(resetLabel);
        panel.add(Box.createVerticalGlue());

        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();
        updateClock();

        Timer resetTimer = new Timer(60 * 1000, e -> updateResetCountdown(resetLabel));
        resetTimer.start();
        updateResetCountdown(resetLabel);

        return panel;
    }

    private void updateClock() {
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        clockLabel.setText(now.format(formatter));
    }

    private void updateResetCountdown(JLabel label) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReset = now.withHour(3).withMinute(0).withSecond(0).withNano(0);
        if (!now.isBefore(nextReset)) nextReset = nextReset.plusDays(1);

        java.time.Duration duration = java.time.Duration.between(now, nextReset);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        label.setText(String.format("🌙 Next reset in %dh %02dm", hours, minutes));
        label.setForeground(duration.toMinutes() <= 60 ? Color.ORANGE : Color.LIGHT_GRAY);
    }

    // ---------------- Habit Section ----------------
    private JPanel createHabitSection() {
        JPanel habitSection = new JPanel(new BorderLayout());
        habitSection.setBackground(Color.black);

        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.black);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JLabel habit_desc = new JLabel("Your habits:");
        habit_desc.setForeground(Color.white);
        habit_desc.setFont(defaultFont.deriveFont(Font.PLAIN, 14f));
        habit_desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton habit_add = new JButton("Add Habit");
        habit_add.setFont(defaultFont.deriveFont(Font.BOLD, 24f));
        habit_add.setForeground(Color.white);
        habit_add.setBackground(Color.black);
        habit_add.setAlignmentX(Component.LEFT_ALIGNMENT);

        topPanel.add(habit_desc);
        topPanel.add(Box.createVerticalStrut(4));
        topPanel.add(habit_add);

        habitSection.add(topPanel, BorderLayout.NORTH);

        habitList = new JPanel();
        habitList.setLayout(new BoxLayout(habitList, BoxLayout.Y_AXIS));
        habitList.setBackground(Color.black);
        habitSection.add(new JScrollPane(habitList), BorderLayout.CENTER);

        habit_add.addActionListener(e -> {
            String habitName = showCustomInputDialog(habitSection, "Add Habit", "");
            if (habitName != null && !habitName.trim().isEmpty()) {
                addHabit(habitName.trim(), false);
                saveHabits();
            }
        });

        return habitSection;
    }

    private String showCustomInputDialog(Component parent, String title, String defaultText) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.black);

        JLabel label = new JLabel("Enter a habit:");
        label.setForeground(Color.white);
        label.setFont(defaultFont.deriveFont(Font.PLAIN, 16f));
        panel.add(label, BorderLayout.NORTH);

        JTextField textField = new JTextField(defaultText != null ? defaultText : "");
        textField.setFont(defaultFont.deriveFont(Font.PLAIN, 16f));
        textField.setBackground(Color.darkGray);
        textField.setForeground(Color.white);
        textField.setCaretColor(Color.white);
        panel.add(textField, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return result == JOptionPane.OK_OPTION ? textField.getText().trim() : null;
    }

    private int showCustomConfirmDialog(Component parent, String title, String message) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.black);

        JLabel label = new JLabel(message);
        label.setForeground(Color.white);
        label.setFont(defaultFont.deriveFont(Font.PLAIN, 16f));
        panel.add(label, BorderLayout.CENTER);

        return JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    // ---------------- Habit Add/Edit/Delete ----------------
    private void addHabit(String habitName, boolean checked) {
        JPanel habitRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        habitRow.setBackground(Color.black);

        JCheckBox newHabit = new JCheckBox(habitName, checked);
        newHabit.setFont(defaultFont.deriveFont(Font.PLAIN, 16f));
        newHabit.setForeground(Color.white);
        newHabit.setBackground(Color.black);

        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");

        JButton[] btns = {editBtn, delBtn};
        for (JButton b : btns) {
            b.setForeground(Color.white);
            b.setBackground(Color.darkGray);
            b.setBorder(null);
            b.setFont(defaultFont.deriveFont(Font.PLAIN, 12f));
        }

        editBtn.addActionListener(e -> {
            String newName = showCustomInputDialog(habitList, "Edit Habit", newHabit.getText());
            if (newName != null && !newName.trim().isEmpty()) {
                newHabit.setText(newName.trim());
                saveHabits();
            }
        });

        delBtn.addActionListener(e -> {
            int confirm = showCustomConfirmDialog(habitList, "Delete Habit", "Are you sure you want to delete \"" + newHabit.getText() + "\"?");
            if (confirm == JOptionPane.OK_OPTION) {
                habitList.remove(habitRow);
                habitList.revalidate();
                habitList.repaint();
                saveHabits();

                String key = (String) newHabit.getClientProperty("habitKey");
                if (key != null) {
                    streakMap.remove(key);
                    lastCompletedMap.remove(key);
                    saveStreaks();
                    refreshStreakLabels();
                }
            }
        });

        newHabit.addActionListener(e -> saveHabits());

        habitRow.add(newHabit);
        habitRow.add(editBtn);
        habitRow.add(delBtn);

        habitList.add(habitRow);
        habitList.revalidate();
        habitList.repaint();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void loadHabits() {
        if (!habitFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(habitFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2) {
                    boolean checked = parts[0].equals("1");
                    String name = parts[1];
                    addHabit(name, checked);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void saveHabits() {
        List<String> habits = new ArrayList<>();
        for (Component row : habitList.getComponents()) {
            if (row instanceof JPanel) {
                for (Component c : ((JPanel) row).getComponents()) {
                    if (c instanceof JCheckBox) {
                        JCheckBox cb = (JCheckBox) c;
                        String state = cb.isSelected() ? "1" : "0";
                        habits.add(state + "|" + cb.getText());
                    }
                }
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(habitFile))) {
            for (String h : habits) {
                writer.write(h);
                writer.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ---------------- Streak Logic ----------------
    private void attachHabitListListener() {
        habitList.addContainerListener(new java.awt.event.ContainerAdapter() {
            @Override
            public void componentAdded(java.awt.event.ContainerEvent e) {
                Component child = e.getChild();
                if (!(child instanceof JPanel)) return;
                JPanel row = (JPanel) child;

                final JCheckBox[] cbHolder = {null};
                JButton delBtn = null;
                for (Component c : row.getComponents()) {
                    if (c instanceof JCheckBox) cbHolder[0] = (JCheckBox) c;
                    if (c instanceof JButton && "Delete".equals(((JButton) c).getText())) delBtn = (JButton) c;
                }

                final JCheckBox cb = cbHolder[0];
                if (cb != null) {
                    String name = cb.getText();
                    cb.putClientProperty("habitKey", name);

                    int s = streakMap.getOrDefault(name, 0);
                    cb.setToolTipText("Streak: " + s);

                    for (java.awt.event.ActionListener al : cb.getActionListeners()) cb.removeActionListener(al);
                    cb.addActionListener(ae -> checkAllAndUpdateStreaks());

                    cb.addPropertyChangeListener("text", evt -> {
                        String oldName = (String) evt.getOldValue();
                        String newName = (String) evt.getNewValue();
                        if (oldName == null || newName == null || oldName.equals(newName)) return;
                        Integer v = streakMap.remove(oldName);
                        LocalDate d = lastCompletedMap.remove(oldName);
                        if (v != null) streakMap.put(newName, v);
                        if (d != null) lastCompletedMap.put(newName, d);
                        cb.putClientProperty("habitKey", newName);
                        saveStreaks();
                        refreshStreakLabels();
                    });
                }

                if (delBtn != null) {
                    final JCheckBox finalCb = cb;
                    delBtn.addActionListener(ae -> {
                        String key = finalCb == null ? null : (String) finalCb.getClientProperty("habitKey");
                        if (key != null) {
                            streakMap.remove(key);
                            lastCompletedMap.remove(key);
                            saveStreaks();
                            refreshStreakLabels();
                        }
                    });
                }
            }
        });
    }

    private void checkAllAndUpdateStreaks() {
        if (habitList == null) return;
        Component[] rows = habitList.getComponents();
        if (rows.length == 0) return;

        boolean allChecked = true;
        List<JCheckBox> boxes = new ArrayList<>();
        for (Component row : rows) {
            if (!(row instanceof JPanel)) continue;
            for (Component c : ((JPanel) row).getComponents()) {
                if (c instanceof JCheckBox) {
                    JCheckBox cb = (JCheckBox) c;
                    boxes.add(cb);
                    if (!cb.isSelected()) { allChecked = false; break; }
                }
            }
            if (!allChecked) break;
        }

        LocalDate today = LocalDate.now();
        if (!allChecked) return;

        boolean alreadyCountedToday = true;
        for (JCheckBox cb : boxes) {
            String key = (String) cb.getClientProperty("habitKey");
            LocalDate last = lastCompletedMap.get(key);
            if (last == null || !last.equals(today)) { alreadyCountedToday = false; break; }
        }
        if (alreadyCountedToday) return;

        for (JCheckBox cb : boxes) {
            String key = (String) cb.getClientProperty("habitKey");
            int newStreak = streakMap.getOrDefault(key, 0) + 1;
            streakMap.put(key, newStreak);
            lastCompletedMap.put(key, today);
            cb.setToolTipText("Streak: " + newStreak);
        }
        saveStreaks();
        refreshStreakLabels();
    }

    private void loadStreaks() {
        if (!streakFile.exists()) return;
        try (BufferedReader r = new BufferedReader(new FileReader(streakFile))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split("\\|", 3);
                if (parts.length >= 2) {
                    String name = parts[0];
                    int streak = 0;
                    try { streak = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {}
                    streakMap.put(name, streak);
                    if (parts.length == 3 && parts[2] != null && !parts[2].isEmpty()) {
                        try { LocalDate d = LocalDate.parse(parts[2]); lastCompletedMap.put(name, d); } catch (Exception ignored) {}
                    }
                }
            }
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void saveStreaks() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(streakFile))) {
            for (java.util.Map.Entry<String, Integer> e : streakMap.entrySet()) {
                String name = e.getKey();
                String streak = String.valueOf(e.getValue());
                LocalDate d = lastCompletedMap.get(name);
                String dateStr = d == null ? "" : d.toString();
                w.write(name + "|" + streak + "|" + dateStr);
                w.newLine();
            }
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void refreshStreakLabels() {
        if (habitList == null) return;
        for (Component rowComp : habitList.getComponents()) {
            if (!(rowComp instanceof JPanel)) continue;
            JPanel row = (JPanel) rowComp;

            JCheckBox cb = null;
            for (Component c : row.getComponents()) if (c instanceof JCheckBox) cb = (JCheckBox) c;
            if (cb == null) continue;

            String key = (String) cb.getClientProperty("habitKey");
            int streak = streakMap.getOrDefault(key, 0);

            cb.setToolTipText("Streak: " + streak);
        }
    }

    private void updateAllTooltips() { refreshStreakLabels(); }

    private void initStreakDisplay() { refreshStreakLabels(); }

    private void startDailyResetTimer() {
        Timer dailyReset = new Timer(60 * 1000, e -> {
            LocalDate today = LocalDate.now();
            if (!today.equals(lastCheckedDate)) {
                lastCheckedDate = today;
                for (Component rowComp : habitList.getComponents()) {
                    if (!(rowComp instanceof JPanel)) continue;
                    JPanel row = (JPanel) rowComp;

                    for (Component c : row.getComponents()) {
                        if (c instanceof JCheckBox) ((JCheckBox) c).setSelected(false);
                    }
                }
                saveHabits();
            }
        });
        dailyReset.start();
    }

    // ---------------- Pomodoro Section ----------------
    private JPanel createPomodoroSection(Font font) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.black);

        JLabel timerLabel = new JLabel("25:00", SwingConstants.CENTER);
        timerLabel.setFont(font.deriveFont(Font.BOLD, 48f));
        timerLabel.setForeground(Color.white);
        panel.add(timerLabel, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setBackground(Color.BLACK);
        controlsPanel.setBorder(null);

        JButton startBtn = new JButton("Start");
        JButton pauseBtn = new JButton("Pause");
        JButton stopBtn = new JButton("Stop");

        Integer[] workTimes = new Integer[120];
        for (int i = 1; i <= 120; i++) workTimes[i - 1] = i;
        JComboBox<Integer> workDropdown = new JComboBox<>(workTimes);
        workDropdown.setSelectedItem(25);

        Integer[] breakTimes = new Integer[60];
        for (int i = 1; i <= 60; i++) breakTimes[i - 1] = i;
        JComboBox<Integer> breakDropdown = new JComboBox<>(breakTimes);
        breakDropdown.setSelectedItem(5);

        JLabel workLabel = new JLabel("Work (min):");
        JLabel breakLabel = new JLabel("Break (min):");
        workLabel.setForeground(Color.white);
        breakLabel.setForeground(Color.white);

        controlsPanel.add(startBtn);
        controlsPanel.add(pauseBtn);
        controlsPanel.add(stopBtn);
        controlsPanel.add(workLabel);
        controlsPanel.add(workDropdown);
        controlsPanel.add(breakLabel);
        controlsPanel.add(breakDropdown);

        JButton[] controlButtons = {startBtn, pauseBtn, stopBtn};
        for (JButton cb : controlButtons) {
            cb.setForeground(Color.white);
            cb.setBackground(Color.black);
            cb.setBorder(null);
        }

        final Timer[] pomodoroTimer = {null};
        final int[] remainingSeconds = {(int) workDropdown.getSelectedItem() * 60};
        final boolean[] isPaused = {false};
        final boolean[] onBreak = {false};

        Runnable updateTimerLabel = () -> {
            int mins = remainingSeconds[0] / 60;
            int secs = remainingSeconds[0] % 60;
            timerLabel.setText(String.format("%02d:%02d", mins, secs));
        };

        workDropdown.addActionListener(e -> {
            if (!onBreak[0]) {
                remainingSeconds[0] = (int) workDropdown.getSelectedItem() * 60;
                updateTimerLabel.run();
            }
        });

        breakDropdown.addActionListener(e -> {
            if (onBreak[0]) {
                remainingSeconds[0] = (int) breakDropdown.getSelectedItem() * 60;
                updateTimerLabel.run();
            }
        });

        startBtn.addActionListener(e -> {
            if (pomodoroTimer[0] != null && pomodoroTimer[0].isRunning()) return;
            isPaused[0] = false;
            pomodoroTimer[0] = new Timer(1000, evt -> {
                if (!isPaused[0] && remainingSeconds[0] > 0) {
                    remainingSeconds[0]--;
                    updateTimerLabel.run();
                }
                if (remainingSeconds[0] == 0) {
                    Toolkit.getDefaultToolkit().beep();
                    if (!onBreak[0]) {
                        onBreak[0] = true;
                        remainingSeconds[0] = (int) breakDropdown.getSelectedItem() * 60;
                        timerLabel.setForeground(Color.GREEN);
                    } else {
                        onBreak[0] = false;
                        remainingSeconds[0] = (int) workDropdown.getSelectedItem() * 60;
                        timerLabel.setForeground(Color.WHITE);
                    }
                    updateTimerLabel.run();
                }
            });
            pomodoroTimer[0].start();
        });

        pauseBtn.addActionListener(e -> isPaused[0] = true);

        stopBtn.addActionListener(e -> {
            if (pomodoroTimer[0] != null) pomodoroTimer[0].stop();
            isPaused[0] = false;
            onBreak[0] = false;
            remainingSeconds[0] = (int) workDropdown.getSelectedItem() * 60;
            timerLabel.setForeground(Color.WHITE);
            updateTimerLabel.run();
        });

        panel.add(controlsPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ---------------- About Section ----------------
    private JPanel createAboutSection() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.black);
        JLabel about = new JLabel("developed at PU", SwingConstants.CENTER);
        about.setForeground(Color.white);
        about.setFont(defaultFont.deriveFont(Font.PLAIN, 18f));
        panel.add(about);
        return panel;
    }

    // ---------------- Statistics Section ----------------
    private JPanel createStatisticsSection() {
        statisticsPanel = new JPanel(new BorderLayout());
        statisticsPanel.setBackground(Color.black);

        statsTable = new JTable();
        statsTable.setBackground(Color.darkGray);
        statsTable.setForeground(Color.white);
        statsTable.setFont(defaultFont.deriveFont(Font.PLAIN, 14f));
        statsTable.setFillsViewportHeight(true);

        statisticsPanel.add(new JScrollPane(statsTable), BorderLayout.CENTER);
        return statisticsPanel;
    }

    private void updateStatisticsTable() {
        String[] cols = {"Habit", "Streak"};
        Object[][] data = new Object[streakMap.size()][2];
        int i = 0;
        for (String k : streakMap.keySet()) {
            data[i][0] = k;
            data[i][1] = streakMap.get(k);
            i++;
        }
        statsTable.setModel(new javax.swing.table.DefaultTableModel(data, cols));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MultiSectionApp::new);
    }
}
