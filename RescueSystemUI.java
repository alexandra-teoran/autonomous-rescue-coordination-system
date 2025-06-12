package examples.autonomous_rescue_coordination_system;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Runtime;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class RescueSystemUI extends JFrame {
    private JTextArea logArea;
    private JLabel currentActionLabel;
    private JLabel helpedVictimsLabel;
    private JLabel awaitingSuppliesLabel;
    private JButton startButton;
    private ContainerController containerController;
    private AgentController commanderAgent, droneAgent, medicAgent, supplyAgent, victimAgent;
    private JButton stopButton;

    private int helpedVictims = 0;
    private int awaitingSupplies = 0;

    public RescueSystemUI() {
        setTitle("Autonomous Rescue Coordination System");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // top panel with current action and progress report
        JPanel topPanel = new JPanel(new GridLayout(2, 1));

        currentActionLabel = new JLabel("Current action: waiting to start...");
        currentActionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        currentActionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(currentActionLabel);

        // progress pannel
        JPanel progressPanel = new JPanel(new GridLayout(2, 1));
        progressPanel.setBorder(BorderFactory.createTitledBorder("Progress Report"));

        helpedVictimsLabel = new JLabel("Helped Victims: 0");
        helpedVictimsLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        progressPanel.add(helpedVictimsLabel);

        awaitingSuppliesLabel = new JLabel("Victims awaiting Supplies: 0");
        awaitingSuppliesLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        progressPanel.add(awaitingSuppliesLabel);

        topPanel.add(progressPanel);

        add(topPanel, BorderLayout.NORTH);

        // log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // panel with start and stop simulation buttons
        startButton = new JButton("Start Simulation");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(startButton);

        stopButton = new JButton("Stop Simulation");
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopAgents());
        buttonPanel.add(stopButton);

        add(buttonPanel, BorderLayout.SOUTH);


        // redirect sout to log area to display the logs
        System.setOut(new PrintStream(new OutputStream() {
            private StringBuilder buffer = new StringBuilder();

            public void write(int b) {
                char c = (char) b;
                buffer.append(c);

                if (c == '\n') {
                    String line = buffer.toString();
                    buffer.setLength(0);

                    SwingUtilities.invokeLater(() -> processLogLine(line.trim()));
                }
            }
        }));

        startButton.addActionListener(e -> startAgents());
    }

    private void processLogLine(String line) {
        logArea.append(line + "\n");

        // if the log is an action from an agent, add it to the current action label
        if (line.contains("sent") || line.contains("treating") || line.contains("delivering")) {
            currentActionLabel.setText("Current action: " + line);
        }

        // if the log is a progress report with the number of helped victims, update the value label
        if (line.startsWith("Helped victims: ")) {
            try {
                helpedVictims = Integer.parseInt(line.replace("Helped victims: ", "").trim());
                helpedVictimsLabel.setText("Helped Victims: " + helpedVictims);
            } catch (NumberFormatException ignored) {}
        }

        // if the log is a progress report with the number of victims that wait for help, update the value label
        if (line.startsWith("Victims that await help: ")) {
            try {
                awaitingSupplies = Integer.parseInt(line.replace("Victims that await help: ", "").trim());
                awaitingSuppliesLabel.setText("Victims that await help: " + awaitingSupplies);
            } catch (NumberFormatException ignored) {}
        }
    }

    // start the agents while keeping the references for stopping the agents. also enable/disable the buttons
    private void startAgents() {
        try {
            Runtime rt = Runtime.instance();
            Profile p = new ProfileImpl();
            containerController = rt.createMainContainer(p);

            commanderAgent = containerController.createNewAgent("commander", "examples.autonomous_rescue_coordination_system.CommanderAgent", null);
            droneAgent = containerController.createNewAgent("drone", "examples.autonomous_rescue_coordination_system.DroneAgent", null);
            medicAgent = containerController.createNewAgent("medic", "examples.autonomous_rescue_coordination_system.MedicAgent", null);
            supplyAgent =containerController.createNewAgent("supplier", "examples.autonomous_rescue_coordination_system.SupplyAgent", null);
            victimAgent = containerController.createNewAgent("victim", "examples.autonomous_rescue_coordination_system.VictimAgent", null);

            commanderAgent.start();
            droneAgent.start();
            medicAgent.start();
            supplyAgent.start();
            victimAgent.start();

            startButton.setEnabled(false);
            stopButton.setEnabled(true);

            logArea.append("Agents started.\n");
        } catch (Exception ex) {
            logArea.append("Error starting agents: " + ex.getMessage() + "\n");
        }
    }

    // stop the agents and disable the stop button
    private void stopAgents() {
        try {
            if (commanderAgent != null) commanderAgent.kill();
            if (droneAgent != null) droneAgent.kill();
            if (medicAgent != null) medicAgent.kill();
            if (supplyAgent != null) supplyAgent.kill();
            if (victimAgent != null) victimAgent.kill();

            logArea.append("Agents stopped.\n");

            stopButton.setEnabled(false);
        } catch (Exception ex) {
            logArea.append("Error stopping agents: " + ex.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RescueSystemUI().setVisible(true));
    }
}
