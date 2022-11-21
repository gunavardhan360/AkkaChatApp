package Ask;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import akka.dispatch.OnSuccess;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

public class TrailAsk {
    public static void main(String[] args) throws InterruptedException, TimeoutException {
        ActorSystem actorSystem = ActorSystem.create("TrailSystem");
        ActorRef actorRef = actorSystem.actorOf(Props.create(AskActor.class), "askActor");

        Timeout timeout = new Timeout(Duration.create(3, "seconds"));
        Queue<Future<Object>> futures = new LinkedList<>();
        futures.add(Patterns.ask(actorRef, "question", timeout));
        futures.add(Patterns.ask(actorRef, "questions", timeout));
        futures.add(Patterns.ask(actorRef, "questionss", timeout));
        futures.add(Patterns.ask(actorRef, "question", timeout));
        futures.add(Patterns.ask(actorRef, "question", timeout));
        futures.add(Patterns.ask(actorRef, "question", timeout));
        futures.add(Patterns.ask(actorRef, "questions", timeout));
        futures.add(Patterns.ask(actorRef, "questionss", timeout));

        System.out.println("asking all...");

        for(int i = 0; i < 8; i++){
            if(i%2 == 0)
                futures.remove();
            else {
                try {
                    System.out.println(Await.result(futures.remove(), timeout.duration()));
                } catch (TimeoutException t) {
                    t.printStackTrace();
                }
            }
        }
        System.out.println(futures.size());
        actorSystem.stop(actorRef);
        actorSystem.terminate();
    }
}
