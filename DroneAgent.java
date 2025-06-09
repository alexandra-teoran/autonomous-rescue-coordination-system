package examples.autonomous_rescue_coordination_system;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class DroneAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": started.");

        // scan every 3 seconds
        addBehaviour(new TickerBehaviour(this, 3000) {
            @Override
            protected void onTick() {
                // simulate finding a victim
                String victimLocation = "Victim at (x=" + Math.random()*100 + ", y=" + Math.random()*100 + ")";
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(getAID("CommanderAgent"));
                msg.setContent(victimLocation);
                send(msg);

                System.out.println(getLocalName() + " sent victim info: " + victimLocation);
            }
        });
    }
}
