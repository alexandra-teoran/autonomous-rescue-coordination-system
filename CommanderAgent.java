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
    private Set<String> helpedVictims = new HashSet<>();
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

                    // if the message contains info about a victim, it is added to the queue and a medic or supplier is assigned
                    if (content.contains("VICTIM")) {
                        VictimInfo info = VictimInfo.fromMessage(content);
                        if (info != null && !helpedVictims.contains(info.location)) {
                            victimQueue.offer(info);
                            System.out.println("in commander: victim added to the queue: " + info.description());
                            printProgress();
                            assignAgents();
                        }
                        return;

                    }
                    // if the message reports a failure when distributing supplies, a new supplier is sent to try again
                    if (content.contains("FAILURE at")) {
                        String failedLocation = content.replace("FAILURE at ", "").trim();
                        System.out.println("in commander: failure while sending supplies to " + failedLocation + ". trying to resend...");
                        resendSupply(failedLocation);
                        printProgress();
                        return;
                    }
                    // if the message reports a success when distributing supplies, the victim is removed from the pending supplies and added to the helped victims
                    if (content.contains("SUCCESS at")) {
                        String successLocation = content.replace("SUCCESS at ", "").trim();
                        helpedVictims.add(successLocation);
                        pendingSupplies.remove(successLocation);
                        System.out.println("in commander: successfully helped the victim at " + successLocation);
                        printProgress();
                        return;
                    }
                    // if the message contains a treated victim, it is added to the helped victims count
                    if (content.contains("TREATED at")) {
                        String treatedLocation = content.replace("TREATED at ", "").trim();
                        helpedVictims.add(treatedLocation);
                        System.out.println("in commander: successfully treated the victim at " + treatedLocation);
                        printProgress();
                    }
                    // if the message contains a danger zone, it is added to the danger zones list
                    if (content.contains("ZONE DANGER")) {
                        dangerZones.add(content);
                        System.out.println("in commander: danger zone reported: " + content);
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

            // send a medic if the victim is injured or in critical condition
            if (v.injured || v.critical) {
                ACLMessage medicMsg = new ACLMessage(ACLMessage.REQUEST);
                medicMsg.addReceiver(new AID( "medic", AID.ISLOCALNAME ));
                medicMsg.setContent("Assist " + v.location);
                send(medicMsg);
            }

            // send supplies if the victim is hungry
            if (v.hungry) {
                ACLMessage supplyMsg = new ACLMessage(ACLMessage.REQUEST);
                supplyMsg.setContent("Deliver to " + v.location);
                supplyMsg.addReceiver(new AID( "supplier", AID.ISLOCALNAME ));
                send(supplyMsg);
            }

            System.out.println("in commander: agents allocated for " + v.description());
        }
    }

    // resend supplies if the previous attempt failed
    private void resendSupply(String location) {
        ACLMessage supplyMsg = new ACLMessage(ACLMessage.REQUEST);
        supplyMsg.setContent("Deliver to " + location + " (RETRY)");
        supplyMsg.addReceiver(new AID( "supplier", AID.ISLOCALNAME ));
        send(supplyMsg);

        System.out.println("in commander: resend supplier at " + location);
    }

    private void printProgress() {
        System.out.println("=== Progress report ===");
        System.out.println("Helped victims: " + helpedVictims.size());
        System.out.println("Victims that await help: " + (pendingSupplies.size() + victimQueue.size()));
        System.out.println("=======================");
    }

    static class VictimInfo {
        String location;
        boolean injured, hungry, critical;
        int urgencyScore;

        public String description() {
            StringBuilder sb = new StringBuilder();
            sb.append("Victim at ").append(location).append(" is ");

            boolean first = true;
            if (injured) {
                sb.append("injured");
                first = false;
            }
            if (hungry) {
                if (!first) sb.append(", ");
                sb.append("hungry");
                first = false;
            }
            if (critical) {
                if (!first) sb.append(" and ");
                sb.append("critical");
            }

            return sb.toString();
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
                System.out.println("Error when parsing the victim info: " + msg);
                return null;
            }
        }
    }
}
