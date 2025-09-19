import java.awt.*;
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class MultiSectionApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JLabel clockLabel;

    // ✅ Habit storage
    private JPanel habitList;
    private final File habitFile = new File("habits.txt");
    private Font defaultFont;

    @SuppressWarnings("unused")
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
        } catch (FontFormatException | IOException e) {
            defaultFont = new Font("Monospaced", Font.BOLD, 48);
            UIManager.put("Label.font", defaultFont);
            UIManager.put("Button.font", defaultFont.deriveFont(Font.BOLD, 18f));
            UIManager.put("Spinner.font", defaultFont.deriveFont(Font.PLAIN, 10f));
            System.err.println("Failed to load custom font: " + e.getMessage());
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

        mainPanel.add(homeSection, "Home");
        mainPanel.add(aboutSection, "About");
        mainPanel.add(pomodoroPanel, "Pomodoro");
        mainPanel.add(habitSection, "Habit Section");

        // --- Navigation buttons ---
        JPanel navPanel = new JPanel();
        navPanel.setBackground(Color.black);

        JButton homeBtn = new JButton("Home");
        JButton pomodoroBtn = new JButton("Pomodoro");
        JButton aboutBtn = new JButton("About");
        JButton habitButton = new JButton("Habit");

        habitButton.addActionListener(e -> cardLayout.show(mainPanel, "Habit Section"));
        homeBtn.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
        pomodoroBtn.addActionListener(e -> cardLayout.show(mainPanel, "Pomodoro"));
        aboutBtn.addActionListener(e -> cardLayout.show(mainPanel, "About"));

        JButton[] buttons = {homeBtn, pomodoroBtn, aboutBtn, habitButton};
        for (JButton b : buttons) {
            b.setForeground(Color.white);
            b.setBackground(Color.black);
            b.setBorder(null);
        }

        navPanel.add(homeBtn);
        navPanel.add(pomodoroBtn);
        navPanel.add(aboutBtn);
        navPanel.add(habitButton);

        setLayout(new BorderLayout());
        add(navPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        cardLayout.show(mainPanel, "Home"); // default page

        // ✅ Load saved habits
        loadHabits();

        setVisible(true);
    }

    // ---------------- Home Section ----------------
    private JPanel createHomeSection(Font font) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.black);

        clockLabel = new JLabel("", SwingConstants.CENTER);
        clockLabel.setFont(font.deriveFont(Font.BOLD, 60f));
        clockLabel.setForeground(Color.white);
        panel.add(clockLabel, BorderLayout.CENTER);

        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();
        updateClock();
        return panel;
    }

    private void updateClock() {
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        clockLabel.setText(now.format(formatter));
    }

    // ---------------- Habit Section ----------------
    private JPanel createHabitSection() {
        JPanel habitSection = new JPanel(new BorderLayout());
        habitSection.setBackground(Color.black);

        // Top panel
        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.black);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JLabel habit_desc = new JLabel("Your habits:");
        habit_desc.setForeground(Color.white);
        habit_desc.setFont(defaultFont.deriveFont(Font.PLAIN, 14f));
        habit_desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton habit_add = new JButton("add habit");
        habit_add.setFont(defaultFont.deriveFont(Font.BOLD, 24f));
        habit_add.setForeground(Color.white);
        habit_add.setBackground(Color.black);
        habit_add.setAlignmentX(Component.LEFT_ALIGNMENT);

        topPanel.add(habit_desc);
        topPanel.add(Box.createVerticalStrut(4));
        topPanel.add(habit_add);

        habitSection.add(topPanel, BorderLayout.NORTH);

        // ✅ Container for checkboxes
        habitList = new JPanel();
        habitList.setLayout(new BoxLayout(habitList, BoxLayout.Y_AXIS));
        habitList.setBackground(Color.black);

        habitSection.add(new JScrollPane(habitList), BorderLayout.CENTER);

        // ✅ Add habit function (custom dialog)
        habit_add.addActionListener(e -> {
            String habitName = showCustomInputDialog(habitSection, "Add Habit", "");
            if (habitName != null && !habitName.trim().isEmpty()) {
                addHabit(habitName.trim(), false);
                saveHabits();
            }
        });

        return habitSection;
    }

    // ---------------- Custom Dialogs ----------------
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

        int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            return textField.getText().trim();
        }
        return null;
    }

    private int showCustomConfirmDialog(Component parent, String title, String message) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.black);

        JLabel label = new JLabel(message);
        label.setForeground(Color.white);
        label.setFont(defaultFont.deriveFont(Font.PLAIN, 16f));
        panel.add(label, BorderLayout.CENTER);

        return JOptionPane.showConfirmDialog(
                parent,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    // ---------------- Add/Edit/Delete Habits ----------------
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

        // Edit button
        editBtn.addActionListener(e -> {
            String newName = showCustomInputDialog(habitList, "Edit Habit", newHabit.getText());
            if (newName != null && !newName.trim().isEmpty()) {
                newHabit.setText(newName.trim());
                saveHabits();
            }
        });

        // Delete button with confirmation
        delBtn.addActionListener(e -> {
            int confirm = showCustomConfirmDialog(habitList, "Delete Habit",
                    "Are you sure you want to delete \"" + newHabit.getText() + "\"?");
            if (confirm == JOptionPane.OK_OPTION) {
                habitList.remove(habitRow);
                habitList.revalidate();
                habitList.repaint();
                saveHabits();
            }
        });

        // Save when checkbox toggled
        newHabit.addActionListener(e -> saveHabits());

        habitRow.add(newHabit);
        habitRow.add(editBtn);
        habitRow.add(delBtn);

        habitList.add(habitRow);
        habitList.revalidate();
        habitList.repaint();
    }

    // ---------------- Load/Save Habits ----------------
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------- About Section ----------------
    private JPanel createAboutSection() {
        JPanel aboutSection = new JPanel();
        aboutSection.setBackground(Color.black);

        JTextArea aboutText = new JTextArea(
                "This is a Project, intended to help you keep track of your habits and assist you to become more consistent. " +
                        "It is designed with productivity in mind, combining habit tracking with a built-in Pomodoro timer."
        );
        aboutText.setEditable(false);
        aboutText.setWrapStyleWord(true);
        aboutText.setLineWrap(true);
        aboutText.setForeground(Color.white);
        aboutText.setBackground(Color.black);
        aboutText.setFont(defaultFont.deriveFont(Font.PLAIN, 20f));

        JLabel aboutLabel2 = new JLabel("Created at: PU.", SwingConstants.CENTER);
        aboutLabel2.setForeground(Color.white);
        aboutLabel2.setFont(defaultFont.deriveFont(Font.PLAIN, 24f));

        aboutSection.setLayout(new BorderLayout());
        aboutSection.add(new JScrollPane(aboutText), BorderLayout.CENTER);
        aboutSection.add(aboutLabel2, BorderLayout.SOUTH);

        return aboutSection;
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

        JButton startBtn = new JButton("Start");
        JButton pauseBtn = new JButton("Pause");
        JButton stopBtn = new JButton("Stop");
        controlsPanel.add(startBtn);
        controlsPanel.add(pauseBtn);
        controlsPanel.add(stopBtn);

        JButton[] controlbuttons = {startBtn, pauseBtn, stopBtn};
        for (JButton cb : controlbuttons) {
            cb.setForeground(Color.white);
            cb.setBackground(Color.black);
            cb.setBorder(null);
        }

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
        workLabel.setBackground(Color.black);
        breakLabel.setBackground(Color.black);

        controlsPanel.add(workLabel);
        controlsPanel.add(workDropdown);
        controlsPanel.add(breakLabel);
        controlsPanel.add(breakDropdown);

        panel.add(controlsPanel, BorderLayout.SOUTH);
        controlsPanel.setBackground(Color.BLACK);
        controlsPanel.setBorder(null);

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
            int confirm = showCustomConfirmDialog(panel, "Stop Timer", "Are you sure you want to stop the timer?");
            if (confirm == JOptionPane.OK_OPTION) {
                if (pomodoroTimer[0] != null) pomodoroTimer[0].stop();
                onBreak[0] = false;
                remainingSeconds[0] = (int) workDropdown.getSelectedItem() * 60;
                timerLabel.setForeground(Color.WHITE);
                updateTimerLabel.run();
                isPaused[0] = false;
            }
        });

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MultiSectionApp::new);
    }
}
