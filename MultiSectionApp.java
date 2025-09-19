import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;

public class MultiSectionApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JLabel clockLabel;

    @SuppressWarnings("unused")
    public MultiSectionApp() {
        setTitle("Habit Clock");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Load custom font (fallback if fails)
        Font defaultFont;
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

        setIconImage(Toolkit.getDefaultToolkit().getImage("resources\\images\\habit0clock.png")); // Set your icon path
            
        // CardLayout for switching sections
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // --- Create sections ---
        JPanel homeSection = createHomeSection(defaultFont);

        JPanel habitSection = new JPanel();
        habitSection.setBackground(Color.black);
        habitSection.setLayout(new BorderLayout());
        JLabel habit_desc = new JLabel();
        habit_desc.setText("sadfghjkl");
        habit_desc.setForeground(Color.white);
        habit_desc.setFont(defaultFont.deriveFont(Font.PLAIN, 14f));
        habit_desc.setHorizontalAlignment(SwingConstants.LEFT);
        habitSection.add(habit_desc, BorderLayout.NORTH);
        Point p = habit_desc.getLocation();
        JButton habit_add = new JButton();
        habit_add.setHorizontalAlignment(SwingConstants.LEFT);
        // habit_add.setLocation(p.x , p.y - 16);
        habit_add.setText("add habit");
        habit_add.setFont(defaultFont.deriveFont(Font.BOLD, 24f));
        habit_add.setForeground(Color.white);
        habit_add.setBackground(Color.black);
        habit_add.setSize(20, 20);
        habitSection.add(habit_add);
        
        JPanel aboutSection = new JPanel();
        aboutSection.setBackground(Color.black);
        JLabel aboutLabel = new JLabel("This is the About Section.", SwingConstants.CENTER);
        aboutLabel.setForeground(Color.white);
        aboutLabel.setFont(defaultFont.deriveFont(Font.BOLD, 24f));
        aboutSection.setLayout(new BorderLayout());
        aboutSection.add(aboutLabel, BorderLayout.CENTER);

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

        // Styling buttons
        
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

        // Layout
        setLayout(new BorderLayout());
        add(navPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);


        // --- Set default page ---
        cardLayout.show(mainPanel, "Home"); // Home is default

        setVisible(true);
    }

    // ---------------- Home Section ----------------
    @SuppressWarnings("unused")
    private JPanel createHomeSection(Font font) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.black);

        clockLabel = new JLabel("", SwingConstants.CENTER);
        clockLabel.setFont(font.deriveFont(Font.BOLD, 60f));
        clockLabel.setForeground(Color.white);
        panel.add(clockLabel, BorderLayout.CENTER);

        // Timer to update clock every second
        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();

        updateClock(); // show immediately
        return panel;
    }
    

    private void updateClock() {
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        clockLabel.setText(now.format(formatter));
    }

    // ---------------- Pomodoro Section ----------------
    @SuppressWarnings("unused")
    private JPanel createPomodoroSection(Font font) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.black);

    // Timer label
    JLabel timerLabel = new JLabel("25:00", SwingConstants.CENTER);
    timerLabel.setFont(font.deriveFont(Font.BOLD, 48f));
    timerLabel.setForeground(Color.white);
    panel.add(timerLabel, BorderLayout.CENTER);

    // Controls panel
    JPanel controlsPanel = new JPanel();

    JButton startBtn = new JButton("Start");
    JButton pauseBtn = new JButton("Pause");
    JButton stopBtn = new JButton("Stop");
    controlsPanel.add(startBtn);
    controlsPanel.add(pauseBtn);
    controlsPanel.add(stopBtn);
    // Styling controls panel

    JButton[] controlbuttons = {startBtn, pauseBtn,stopBtn};
        for (JButton cb : controlbuttons) {
            cb.setForeground(Color.white);
            cb.setBackground(Color.black);
            cb.setBorder(null);
        }

    // Dropdown menus for session and break durations
    Integer[] workTimes = new Integer[120];
    for (int i = 1; i <= 120; i++) workTimes[i - 1] = i; // 1 to 120 minutes
    JComboBox<Integer> workDropdown = new JComboBox<>(workTimes);
    workDropdown.setSelectedItem(25);

    Integer[] breakTimes = new Integer[60];
    for (int i = 1; i <= 60; i++) breakTimes[i - 1] = i; // 1 to 60 minutes
    JComboBox<Integer> breakDropdown = new JComboBox<>(breakTimes);
    breakDropdown.setSelectedItem(5);

    //display labels and buttons
    JLabel workLabel = new JLabel("Work (min):");
    JLabel breakLabel = new JLabel("Break (min):");
    JLabel[] optLabels = {workLabel, breakLabel};
    for(JLabel opt : optLabels){
        opt.setForeground(Color.white);
        opt.setBackground(Color.black);
    }
    controlsPanel.add(workLabel);
    controlsPanel.add(workDropdown);
    controlsPanel.add(breakLabel);
    controlsPanel.add(breakDropdown);


    panel.add(controlsPanel, BorderLayout.SOUTH);
    controlsPanel.setBackground(Color.BLACK);
    controlsPanel.setForeground(Color.white);
    controlsPanel.setBorder(null);
    

    // Timer logic
    final Timer[] pomodoroTimer = {null};
    final int[] remainingSeconds = {(int) workDropdown.getSelectedItem() * 60};
    final boolean[] isPaused = {false};
    final boolean[] onBreak = {false};

    Runnable updateTimerLabel = () -> {
        int mins = remainingSeconds[0] / 60;
        int secs = remainingSeconds[0] % 60;
        timerLabel.setText(String.format("%02d:%02d", mins, secs));
    };
    //Stylise dropdown menus
    workDropdown.setForeground(Color.WHITE);
    workDropdown.setBackground(Color.BLACK);
    breakDropdown.setBackground(Color.BLACK);
    breakDropdown.setForeground(Color.white);
   
    // Dropdown change listeners reset timer
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

    // Start button
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
                // Switch between work and break automatically
                if (!onBreak[0]) {
                    onBreak[0] = true;
                    remainingSeconds[0] = (int) breakDropdown.getSelectedItem() * 60;
                    timerLabel.setForeground(Color.GREEN); // break color
                } else {
                    onBreak[0] = false;
                    remainingSeconds[0] = (int) workDropdown.getSelectedItem() * 60;
                    timerLabel.setForeground(Color.WHITE); // work color
                }
                updateTimerLabel.run();
            }
        });
        pomodoroTimer[0].start();
    });

    pauseBtn.addActionListener(e -> isPaused[0] = true);

    stopBtn.addActionListener(e -> {
        if (pomodoroTimer[0] != null) pomodoroTimer[0].stop();
        onBreak[0] = false;
        remainingSeconds[0] = (int) workDropdown.getSelectedItem() * 60;
        timerLabel.setForeground(Color.WHITE);
        updateTimerLabel.run();
        isPaused[0] = false;
    });

    return panel;
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(MultiSectionApp::new);
    }
}
