package examples.autonomous_rescue_coordination_system;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class DroneAgent extends Agent {

    private final Random random = new Random();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": started.");

        // scan every 5 seconds
        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID( "commander", AID.ISLOCALNAME ));

                if (random.nextBoolean()) {
                    // simulate a victim
                    String[] statuses = {"injured", "hungry", "critical", "injured and hungry"};
                    String status = statuses[random.nextInt(statuses.length)];
                    String victimReport = "VICTIM at (" + random.nextInt(100) + "," + random.nextInt(100) + ") - " + status;
                    msg.setContent(victimReport);
                } else {
                    // simulate danger zone
                    String dangerZone = "DANGER ZONE at (" + random.nextInt(100) + "," + random.nextInt(100) + ")";
                    msg.setContent(dangerZone);
                }

                send(msg);

                System.out.println(getLocalName() + " sent info: " + msg.getContent());
            }
        });
    }
}
