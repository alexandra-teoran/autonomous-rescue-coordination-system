package examples.autonomous_rescue_coordination_system;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class VictimAgent extends Agent {

    private int health = 100; // good health at the start, decreases over time
    private boolean hungry = true;
    private boolean injured = true;
    private boolean critical = true;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": started at location (x=" + Math.random()*100 + ", y=" + Math.random()*100 + ").");

        // every 5 seconds
        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                // simulate health degradation if no help is provided
                if(health > 0) {
                    health -= 10;
                    System.out.println(getLocalName() + ": health = " + health);

                    // send message to commander if health is critical
                    if (health <= 50) {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addReceiver(new AID( "commander", AID.ISLOCALNAME ));
                        String status = (injured ? "injured" : "") +
                                (critical ? " critical" : "") +
                                (hungry ? " hungry" : "");
                        msg.setContent(status);
                        send(msg);
                        System.out.println(getLocalName() + " reported worsening status.");
                    }
                }
            }
        });
    }
}
