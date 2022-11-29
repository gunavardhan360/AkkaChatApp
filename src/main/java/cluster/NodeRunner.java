package cluster;

import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.CoordinatedShutdown;
import akka.management.javadsl.AkkaManagement;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

class NodeRunner {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            startupClusterNodes(Arrays.asList("0"));
        } else {
            startupClusterNodes(Arrays.asList(args));
        }
    }

    private static void startupClusterNodes(List<String> ports) throws Exception {
        System.out.println("Please enter your Full Name before entering the chat room: [e.g. Sajal Goel]");
        String userName = getUserInput();
        System.out.printf("Start cluster on port(s) %s%n", ports);
        String port = ports.get(0);

        ActorSystem actorSystem = ActorSystem.create("cluster", setupClusterNodeConfig(port));
        AkkaManagement.get(actorSystem).start();

        ActorRef userHandler = actorSystem.actorOf(ClusterAwareActor.props(userName), "clusterAware");

        addCoordinatedShutdownTask(actorSystem, CoordinatedShutdown.PhaseClusterShutdown());
        actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());

        showMenu(userHandler);

        actorSystem.stop(userHandler);
        AkkaManagement.get(actorSystem).stop();
        actorSystem.terminate();
    }

    private static String getUserInput(){
        Scanner sc = new Scanner(System.in);
        String res = "";
        if(sc.hasNextLine())
            res = sc.nextLine();
        return res;
    }

    private static void showMenuContents(){
        System.out.println("Menu\n\t -> Choose 1 to discover online users\n\t -> Start with (@userFirstName message) to send a message\n\t -> Start with (@all message) to send a message to everyone" +
                "\n\t -> Start with (@few user1.user2 message) to send a message  to few users\n\t -> Type \"exit\" to exit");
    }

    private static void showMenu(ActorRef userHandler) throws Exception {
        String response;
        showMenuContents();
        do {
            System.out.print("-> ");
            response = getUserInput();
            if(response.equals("1")){
                userHandler.tell(new chatApplication.onlineUserList(), ActorRef.noSender());
            } else if (response.startsWith("@all")) {
                userHandler.tell(new chatApplication.messageAll(response), userHandler);
            } else if (response.startsWith("@few")) {
                userHandler.tell(new chatApplication.messageFew(response), userHandler);
            } else if (response.startsWith("@")) {
                userHandler.tell(new chatApplication.messageTo(response), userHandler);
            } else if (!response.equals("exit")) {
                System.out.println("Please enter a valid Response");
                showMenuContents();
            }
            TimeUnit.SECONDS.sleep(1);
        } while (!response.equals("exit"));

    }

    private static Config setupClusterNodeConfig(String port) {
        return ConfigFactory.parseString(
                String.format("akka.remote.netty.tcp.port=%s%n", port) +
                        String.format("akka.remote.artery.canonical.port=%s%n", port))
                .withFallback(ConfigFactory.load());
    }

    private static void addCoordinatedShutdownTask(ActorSystem actorSystem, String coordindateShutdownPhase) {
        CoordinatedShutdown.get(actorSystem).addTask(
                coordindateShutdownPhase,
                coordindateShutdownPhase,
                () -> {
                    actorSystem.log().warning("Coordinated shutdown phase {}", coordindateShutdownPhase);
                    return CompletableFuture.completedFuture(Done.getInstance());
                });
    }
}
