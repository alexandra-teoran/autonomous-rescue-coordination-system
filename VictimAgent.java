package examples.autonomous_rescue_coordination_system;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class VictimAgent extends Agent {

    private int health = 100; // good health at the start, decreases over time

    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": started at location (x=" + Math.random()*100 + ", y=" + Math.random()*100 + ").");

        // every 5 seconds
        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                // simulate health degradation if no help is provided
                health -= 10;
                System.out.println(getLocalName() + ": health = " + health);

                // inform Commander if health gets critical
                if (health <= 50) {
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(getAID("CommanderAgent"));
                    msg.setContent("Victim critical at my location. Health = " + health);
                    send(msg);

                    System.out.println(getLocalName() + " sent critical health update.");
                }
            }
        });
    }
}
