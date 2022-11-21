package cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.management.javadsl.AkkaManagement;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

class NodeRunner {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) {
            startupClusterNodes(Arrays.asList("0"));
        } else {
            startupClusterNodes(Arrays.asList(args));
        }
    }

    private static void startupClusterNodes(List<String> ports) throws IOException, InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter your user name before entering the chat room:");
        String userName = sc.next();
        System.out.printf("Start cluster on port(s) %s%n", ports);
        String port = ports.get(0);

        ActorSystem actorSystem = ActorSystem.create("cluster", setupClusterNodeConfig(port));

        AkkaManagement.get(actorSystem).start();

        ActorRef userHandler = actorSystem.actorOf(ClusterAwareActor.props(userName), "clusterAware");

//            addCoordinatedShutdownTask(actorSystem, CoordinatedShutdown.PhaseClusterShutdown());

        actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());

        showMenu(userHandler);
    }

    private static String getUserInput(){
        Scanner sc = new Scanner(System.in);
        String res = "";
        if(sc.hasNextLine())
            res = sc.nextLine();
        return res;
    }

    private static void showMenu(ActorRef userHandler) throws IOException, InterruptedException {
        String response;
        System.out.println("Menu \t -> Choose 1 to discover online users \t -> start with (@userName message) to send a message \t -> select anything else to exit");
        do {
            System.out.print("-> ");
            response = getUserInput();
            if(response.equals("1")){
                userHandler.tell("onlineUserList", ActorRef.noSender());
            } else if (response.startsWith("@")) {
                userHandler.tell(response, userHandler);
            }
            TimeUnit.SECONDS.sleep(1);
        } while (response.equals("1") | response.startsWith("@"));

    }

    private static Config setupClusterNodeConfig(String port) {
        return ConfigFactory.parseString(
                String.format("akka.remote.netty.tcp.port=%s%n", port) +
                        String.format("akka.remote.artery.canonical.port=%s%n", port))
                .withFallback(ConfigFactory.load());
    }

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
