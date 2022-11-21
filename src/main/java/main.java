//package cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import actor.MathActor;

public class main {
    public static void main(String[] args) {
        if (args.length == 0) {
            startupClusterNodes(Arrays.asList("2552"));
        } else {
            startupClusterNodes(Arrays.asList(args));
        }
    }

    private static void startupClusterNodes(List<String> ports) {
        System.out.printf("Start cluster on port(s) %s%n", ports);

        ports.forEach(port -> {
            ActorSystem actorSystem = ActorSystem.create("cluster");

//            AkkaManagement.get(actorSystem).start();

            ActorRef mathactor = actorSystem.actorOf(MathActor.props(), "MathActor");

            mathactor.tell(3, ActorRef.noSender());

            Scanner sc = new Scanner(System.in);
            int i;
            while(true) {
                System.out.println("Choose Number");
                i = sc.nextInt();
                mathactor.tell(i, ActorRef.noSender());
            }

//            addCoordinatedShutdownTask(actorSystem, CoordinatedShutdown.PhaseClusterShutdown());
//
//            actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());
        });
    }

//    private static Config setupClusterNodeConfig(String port) {
//        return ConfigFactory.parseString(
//                        String.format("akka.remote.netty.tcp.port=%s%n", port) +
//                                String.format("akka.remote.artery.canonical.port=%s%n", port))
//                .withFallback(ConfigFactory.load());
//    }
//
//    private static void addCoordinatedShutdownTask(ActorSystem actorSystem, String coordindateShutdownPhase) {
//        CoordinatedShutdown.get(actorSystem).addTask(
//                coordindateShutdownPhase,
//                coordindateShutdownPhase,
//                () -> {
//                    actorSystem.log().warning("Coordinated shutdown phase {}", coordindateShutdownPhase);
//                    return CompletableFuture.completedFuture(Done.getInstance());
//                });
//    }
}