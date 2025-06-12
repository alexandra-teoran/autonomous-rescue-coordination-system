package examples.autonomous_rescue_coordination_system;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class CommanderAgent extends Agent {

    private List<String> dangerZones = new ArrayList<>();
    private PriorityQueue<VictimInfo> victimQueue;
    private Set<String> treatedVictims = new HashSet<>();
    private Set<String> pendingSupplies = new HashSet<>();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": started.");

        // sort the victim queue by the priority score
        victimQueue = new PriorityQueue<>(Comparator.comparingInt(v -> v.urgencyScore));

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                // listen for incoming messages
                ACLMessage msg = receive(MessageTemplate.MatchPerformative( ACLMessage.INFORM ));

                if (msg != null) {
                    String content = msg.getContent();
                    System.out.println(getLocalName() + " received: " + content + " from " + msg.getSender().getLocalName());

                    if (content.contains("VICTIM")) {
                        VictimInfo info = VictimInfo.fromMessage(content);
                        if (info != null && !treatedVictims.contains(info.location)) {
                            victimQueue.offer(info);
                            System.out.println("Commander: victim added to the queue: " + info.description());
                            assignAgents();
                        }
                        return;

                    }
                    if (content.contains("FAILURE at")) {
                        String failedLocation = content.replace("FAILURE at ", "").trim();
                        System.out.println("Commander: failure while sending supplies to " + failedLocation + ". Retry...");
                        resendSupply(failedLocation);
                        return;
                    }

                    if (content.contains("SUCCESS at")) {
                        String successLocation = content.replace("SUCCESS at ", "").trim();
                        treatedVictims.add(successLocation);
                        pendingSupplies.remove(successLocation);
                        System.out.println("Commander: success at " + successLocation);
                        printProgress();
                        return;
                    }

                    if (content.contains("TREATED at")) {
                        String treatedLocation = content.replace("TREATED at ", "").trim();
                        treatedVictims.add(treatedLocation);
                        System.out.println("Commander: victim treated at " + treatedLocation);
                        printProgress();
                    }
                    if (content.contains("ZONE DANGER")) {
                        dangerZones.add(content);
                        System.out.println("Commander: danger zone reported → " + content);
                    }

                } else {
                    block();
                }
            }
        });
    }

    private void assignAgents() {
        while (!victimQueue.isEmpty()) {
            VictimInfo v = victimQueue.poll();

            // Trimite Medic dacă e rănit
            if (v.injured || v.critical) {
                ACLMessage medicMsg = new ACLMessage(ACLMessage.REQUEST);
                medicMsg.addReceiver(new AID( "medic", AID.ISLOCALNAME ));
                medicMsg.setContent("Assist " + v.location);
                send(medicMsg);
            }

            // Trimite Supply dacă e înfometat
            if (v.hungry) {
                ACLMessage supplyMsg = new ACLMessage(ACLMessage.REQUEST);
                supplyMsg.setContent("Deliver to " + v.location);
                supplyMsg.addReceiver(new AID( "supplier", AID.ISLOCALNAME ));
                send(supplyMsg);
            }

            System.out.println("Commander: agents allocated for " + v.description());
        }
    }

    private void resendSupply(String location) {
        ACLMessage supplyMsg = new ACLMessage(ACLMessage.REQUEST);
        supplyMsg.setContent("Deliver to " + location + " (RETRY)");
        supplyMsg.addReceiver(new AID( "supplier", AID.ISLOCALNAME ));
        send(supplyMsg);

        System.out.println("Commander: resend supplier at " + location);
    }

    private void printProgress() {
        System.out.println("=== Progress report ===");
        System.out.println("Treated victims: " + treatedVictims.size());
        System.out.println("Victims that away supplies: " + pendingSupplies.size());
        System.out.println("=======================");
    }

    static class VictimInfo {
        String location;
        boolean injured, hungry, critical;
        int urgencyScore;

        public String description() {
            return "Victim@" + location + " [injured=" + injured + ", hungry=" + hungry + ", critical=" + critical + "]";
        }

        static VictimInfo fromMessage(String msg) {
            try {
                VictimInfo info = new VictimInfo();
                String[] parts = msg.split(" - ");
                info.location = parts[0].replace("VICTIM at ", "").trim();

                String status = parts[1].toLowerCase();
                info.injured = status.contains("injured");
                info.hungry = status.contains("hungry");
                info.critical = status.contains("critical");

                info.urgencyScore = (info.critical ? 1 : 0) + (info.injured ? 2 : 0) + (info.hungry ? 3 : 0);
                return info;
            } catch (Exception e) {
                System.out.println("Error at parsing the victim info: " + msg);
                return null;
            }
        }
    }
}
