## Autonomous Rescue Coordination System

The project implements an intelligent rescue coordination system using JADE. The system simulates a disaster scenario (e.g. earthquake, flood), where multiple autonomous agents collaborate to locate, assist, and evacuate victims efficiently. The goal is to model realistic decision-making and coordination in emergency situations using distributed intelligence.

### Agent Types and Roles

1. DroneAgent
- Patrols assigned zones, scans for victims, and sends coordinates to the Commander.
- Can detect inaccessible or dangerous areas.
2. MedicAgent
- Navigates to victim locations and provides medical support.
- Prioritizes victims based on urgency level.
3. SupplyAgent
- Delivers kits (food, water, medical supplies) to specified GPS coordinates.
- Checks and reports on delivery success.
4. CommanderAgent
- Central decision-maker. Assigns medics and supply agents based on data from drones and victim status.
- Manages global strategy, tracks mission progress, and handles reassignments if agents fail.
5. VictimAgent
- Represents individuals in need of help. Contains status information (injured, hungry, etc.) and location.
- Can update its condition (e.g., worsening health) if left unattended.

### How to run the application

Compile the source code

`javac -classpath "lib/jade.jar;classes" -d classes src/examples/autonomous-rescue-coordination-system/*.java -Xlint`

Start the GUI

`java -classpath "lib/jade.jar;classes" examples.autonomous_rescue_coordination_system.RescueSystemUI`