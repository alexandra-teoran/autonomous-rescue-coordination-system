package examples.autonomous_rescue_coordination_system;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MedicAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": started.");

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative( ACLMessage.REQUEST ));

                if (msg != null) {
                    String location = msg.getContent().replace("Assist ", "").trim();
                    System.out.println(getLocalName() + " treating victim at: " + location);

                    // simulate successful treatment
                    ACLMessage treatedMsg = new ACLMessage(ACLMessage.INFORM);
                    treatedMsg.addReceiver(new AID( "commander", AID.ISLOCALNAME ));
                    treatedMsg.setContent("TREATED at " + location);
                    send(treatedMsg);

                    System.out.println(getLocalName() + " reported TREATED at: " + location);
                } else {
                    block();
                }
            }
        });
    }
}
