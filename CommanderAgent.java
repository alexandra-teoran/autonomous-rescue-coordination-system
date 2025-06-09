package examples.autonomous_rescue_coordination_system;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CommanderAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": started.");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                // listen for incoming messages
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = receive(mt);

                if (msg != null) {
                    System.out.println(getLocalName() + " received: " + msg.getContent() + " from " + msg.getSender().getLocalName());


                } else {
                    block();
                }
            }
        });
    }
}
