package examples.autonomous_rescue_coordination_system;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SupplyAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": started.");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage msg = receive(mt);

                if (msg != null) {
                    System.out.println(getLocalName() + " received supply request: " + msg.getContent());

                    // simulate successful delivery
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Supplies delivered at " + msg.getContent());
                    send(reply);

                    System.out.println(getLocalName() + " sent delivery report.");
                } else {
                    block();
                }
            }
        });
    }
}
