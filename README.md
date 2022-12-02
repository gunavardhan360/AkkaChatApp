# Akka Chat Application

## Introduction

This is a Java, Gradle project that demonstrates basic Chat Application using akka actors.

This Gradle project runs on JDK >= 11

## Chat Application Details

This Akka Chat application has mainly two unique actors - ClusterListenerActor and - ClusterAwareActor

### ClusterListenerActor 
This actor is set up to receive messages about cluster events. As nodes join and leave the cluster, this actor receives messages about these events. Theses received messages are then written to a logger.

### ClusterAwareActor
This actor is set up to handle/redirect the messages to users in a specific format.

## How to Run the chat Application
After building the Gradle Project on JDK >= 11

    Step - 1 : Run the Runner Class in a JVM which starts the ClusterListenerActor. After the setup
    Step - 2 : Run the instance of NodeRunner Class in differet JVMs as per the number of users, who want to use the application

All required files are in Cluster Folder

Then each user will be presented with a menu as shown below -

    -> Choose 1 to discover online users   
    -> Start with (@userFirstName message) to send a message
    -> Start with (@all message) to send a message to everyone
    -> Start with (@few user1.user2 message) to send a message  to few users
    -> Type "exit" to exit

### Rest the menu is self-explanatory [@pattern message] (Example below)

Assume there are 4 online users with userNames A B C D

Then A can send message to B via
    
    @B Hi, Whats Up?

or

    @few B Hi, Whats Up?

and B will receive message in this format
    
    Message From User A -> "Hi, Whats Up?"

To send message to all the format is -
    
    @all Hi, Whats Up?

To send message to B & C the format is -

    @few B.C Hi, Whats Up?

Finally to exit the application or log off, type

    exit

## Project by Gunavardhan Reddy