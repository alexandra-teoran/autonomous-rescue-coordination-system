package examples.autonomous_rescue_coordination_system;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

public class SupplyAgent extends Agent {

    private final Random random = new Random();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": started.");

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative( ACLMessage.REQUEST ));

                if (msg != null) {
                    System.out.println(getLocalName() + " received supply request: " + msg.getContent());
                    String location = msg.getContent().replace("Deliver to ", "").replace("(RETRY)", "").trim();
                    // there is a 20% chance of failure to simulate real-world scenarios
                    boolean fail = random.nextInt(100) < 20;

                    // send message to commander with the result of the delivery
                    if (fail) {
                        System.out.println(getLocalName() + " FAILED to deliver at: " + location);
                        ACLMessage failMsg = new ACLMessage(ACLMessage.INFORM);
                        failMsg.addReceiver(new AID( "commander", AID.ISLOCALNAME ));
                        failMsg.setContent("FAILURE at " + location);
                        send(failMsg);
                    } else {
                        System.out.println(getLocalName() + " SUCCESS delivering to: " + location);
                        ACLMessage successMsg = new ACLMessage(ACLMessage.INFORM);
                        successMsg.addReceiver(new AID( "commander", AID.ISLOCALNAME ));
                        successMsg.setContent("SUCCESS at " + location);
                        send(successMsg);
                    }
                } else {
                    block();
                }
            }
        });
    }
}
