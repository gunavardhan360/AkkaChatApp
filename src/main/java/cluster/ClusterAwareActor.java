package cluster;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorSelection;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.Member;
import akka.cluster.MemberStatus;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class ClusterAwareActor extends AbstractLoggingActor {
    private final Cluster cluster = Cluster.get(context().system());
//    private final FiniteDuration tickInterval = Duration.create(10, TimeUnit.SECONDS);
    private Cancellable ticker;

    public static String userName;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("getUserName", s -> getUserName())
                .matchEquals("onlineUserList", s -> onlineUserList())
                .match(String.class, s -> s.startsWith("@@"), this::handleMessage)
                .match(String.class, s -> s.startsWith("@"), this::sendMessage)
                .match(chatApplication.userName.class, this::displayName)
                .build();
    }

    private void handleMessage(String msg){
        List<String> words = List.of(msg.split(" "));
        String Message = String.join(" ", words.subList(2, words.size()));
        if(userName.equals(words.get(1).substring(1))){
            System.out.println(words.get(0).substring(1) + " -> " + Message);
        }
    }

    private void sendMessage(String msg){
        Member me = cluster.selfMember();
        log().info("I {} am querying for list", me);

        cluster.state().getMembers().forEach(member -> {
            if (!me.equals(member) && member.status().equals(MemberStatus.up())) {
                String path = member.address().toString() + self().path().toStringWithoutAddress();
                ActorSelection actorSelection = context().actorSelection(path);
                actorSelection.tell("@@" + userName + " " + msg, self());
            }
        });
    }

    private void displayName(chatApplication.userName user) {
        System.out.println("@" + user.userName);
    }

    private void getUserName() {
        chatApplication.userName name = new chatApplication.userName(userName);
        sender().tell(name, self());
    }

    private void onlineUserList() {
        Member me = cluster.selfMember();
        log().info("I {} am querying for list", me);
        AtomicInteger count = new AtomicInteger();

        cluster.state().getMembers().forEach(member -> {
            if (!me.equals(member) && member.status().equals(MemberStatus.up())) {
                String path = member.address().toString() + self().path().toStringWithoutAddress();
                ActorSelection actorSelection = context().actorSelection(path);
                actorSelection.tell("getUserName", self());
                count.getAndIncrement();
            }
        });
        if(count.get() <= 1) System.out.println("No Online Users are present right now!!!");
    }

    @Override
    public void preStart() {
        log().debug("Start");
//        ticker = context().system().scheduler()
//                .schedule(Duration.Zero(),
//                        tickInterval,
//                        self(),
//                        "tick",
//                        context().system().dispatcher(),
//                        null);
    }

    @Override
    public void postStop() {
        ticker.cancel();
        log().info("Stop");
    }

    static Props props(String name) {
        userName = name;
        return Props.create(ClusterAwareActor.class);
    }

    interface chatApplication{
        class userName implements Serializable{
            final String userName;

            userName(String name) {
                userName = name;
            }
        }
    }
//
//    interface Message {
//        class Ping implements Serializable {
//            final long time;
//
//            Ping() {
//                time = System.nanoTime();
//            }
//
//            @Override
//            public String toString() {
//                return String.format("%s[%dus]", getClass().getSimpleName(), time);
//            }
//        }
//
//        class Pong implements Serializable {
//            final long pingTime;
//
//            private Pong(long pingTime) {
//                this.pingTime = pingTime;
//            }
//
//            static Pong from(Ping ping) {
//                return new Pong(ping.time);
//            }
//
//            @Override
//            public String toString() {
//                final double elapsed = (System.nanoTime() - pingTime) / 1000000000.0;
//                return String.format("%s[elapsed %.9fs, %dus]", getClass().getSimpleName(), elapsed, pingTime);
//            }
//        }
//    }
}