package cluster;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

class ClusterAwareActor extends AbstractLoggingActor {
    private final Cluster cluster = Cluster.get(context().system());
    private final Timeout timeout = new Timeout(Duration.create(3, "seconds"));
    public static chatApplication.userName user = new chatApplication.userName();

    public ClusterAwareActor(String name) { this.user.setUserName(name); }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(chatApplication.userName.class, s -> getSender().tell(this.user, self()))
                .match(chatApplication.onlineUserList.class, s -> onlineUserList())
                .match(chatApplication.messageAll.class, this::sendMessage)
                .match(chatApplication.messageFew.class, this::sendMessage)
                .match(chatApplication.messageTo.class, this::sendMessage)
                .match(chatApplication.messageFrom.class, this::handleMessage)
                .matchAny(this::printError)
                .build();
    }

    private void onlineUserList() {
        Member me = cluster.selfMember();
        log().info("I {} am querying for list", me);
        AtomicInteger count = new AtomicInteger();

        cluster.state().getMembers().forEach(member -> {
            if (!me.equals(member) && member.status().equals(MemberStatus.up()) && !member.address().toString().contains("2551")) {
                String path = member.address().toString() + self().path().toStringWithoutAddress();
                ActorSelection actorSelection = context().actorSelection(path);
                System.out.println("@" + getUserName(actorSelection));
                count.getAndIncrement();
            }
        });
        if(count.get() <= 0) System.out.println("No Online Users are present right now!!!");
    }

    private void sendMessage(chatApplication.messageAll msgAll){
        List<ActorSelection> actorSelections = getActorWithUserName(null);
        sendViaChild(actorSelections, new chatApplication.messageFrom(this.user.getFirstName(), msgAll.getMessage()));

    }
    private void sendMessage(chatApplication.messageFew msgFew){
        List<ActorSelection> actorSelections = getActorWithUserName(msgFew.getUserNames());
        sendViaChild(actorSelections, new chatApplication.messageFrom(this.user.getFirstName(), msgFew.getMessage()));
    }

    private void sendViaChild(List<ActorSelection> actorSelectionList, chatApplication.messageFrom msgFrom){
        for(ActorSelection actorSelection: actorSelectionList){
            ActorRef childActor = context().actorOf(ChildClusterAwareActor.props(actorSelection, this.user.getFirstName()));
            childActor.tell(msgFrom, self());
        }
    }

    private void sendMessage(chatApplication.messageTo msgTo){
        List<ActorSelection> actorSelection = getActorWithUserName(List.of(new String[]{msgTo.getMessageTo()}));
        if(actorSelection != null){
            actorSelection.get(0).tell(new chatApplication.messageFrom(this.user.getFirstName(), msgTo.getMessage()), self());
        } else {
            System.out.println("No user found with First Name as " + msgTo.getMessageTo());
        }
    }

    private List<ActorSelection> getActorWithUserName(List<String> userNames) {
        Member me = cluster.selfMember();
        log().info("I {} am querying for list", me);
        List<ActorSelection> actorSelections = new ArrayList<>();
        for(Member member: cluster.state().getMembers()){
            if (!me.equals(member) && member.status().equals(MemberStatus.up()) && !member.address().toString().contains("2551")) {
                String path = member.address().toString() + self().path().toStringWithoutAddress();
                ActorSelection temp = context().actorSelection(path);
                String tempName = getUserName(temp).split(" ")[0];
                if(userNames == null || userNames.contains(tempName))
                    actorSelections.add(temp);
            }
        }
        if(actorSelections.isEmpty()) return null;
        return actorSelections;
    }

    private String getUserName(ActorSelection actorSelection){
        chatApplication.userName response = new chatApplication.userName();
        Future<Object> f = Patterns.ask(actorSelection, new chatApplication.userName(), timeout);
        try {
            response = (chatApplication.userName) Await.result(f, timeout.duration());
        } catch (TimeoutException | InterruptedException e){
            e.printStackTrace();
        }
        if(response.getLastName() == null) return response.getFirstName();
        return response.getFirstName() + " " + response.getLastName();
    }

    private void handleMessage(chatApplication.messageFrom msgFrom){
        System.out.println("Message From User " + msgFrom.getMessageFrom() + " -> " + msgFrom.getMessage() + "\n-> ");
    }

    private void printError(Object o) { System.out.println("Not a valid Functionality"); }

    @Override
    public void preStart() {
        log().debug("Start");
    }

    @Override
    public void postStop() {
        log().info("Stop");
    }

    static Props props(String name) {
        return Props.create(ClusterAwareActor.class, () -> new ClusterAwareActor(name));
    }
}


class ChildClusterAwareActor extends AbstractLoggingActor {
    public static chatApplication.userName user = new chatApplication.userName();
    public ActorSelection actorSelection;

    public ChildClusterAwareActor(ActorSelection actorSelection, String name) {
        this.user.setUserName(name);
        this.actorSelection = actorSelection;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(chatApplication.messageFrom.class, this::handleMessage)
                .match(chatApplication.stopActor.class, s -> getContext().stop(getSelf()))
                .matchAny(this::printError)
                .build();
    }

    private void handleMessage(chatApplication.messageFrom msgFrom) {
        this.actorSelection.tell(msgFrom, ActorRef.noSender());
        getSelf().tell(new chatApplication.stopActor(), ActorRef.noSender());
    }

    private void printError(Object o) { System.out.println("Not a valid Functionality"); }

    @Override
    public void preStart() {
        log().debug("Start");
    }

    @Override
    public void postStop() {
        log().info("Stop");
    }

    static Props props(ActorSelection actorSelection, String name) {
        return Props.create(ChildClusterAwareActor.class, () -> new ChildClusterAwareActor(actorSelection, name));
    }
}