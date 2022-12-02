## Some Bugs Noticed till Now

### 1. Messages that may not be serializable
Desc - Objects in chatApplication interface do not implement serializable
    
    [chatApplication LNO 22] messageAll 
    [chatApplication LNO 74] onlineUserList 
    [chatApplication LNO 76] stopActor 

### 2. Blocking Calls within an actor
Desc - Using a count variable to keep track of users using atomic Integer

    [ClusterAwareActor LNO 48] Operation on Atomic Integer
Desc - Major Blocking call effects every operation of the chat application

    [ClusterAwareActor LNO 98] Ask for user name and await the result with timeout duration of 3 sec

### 3. Detect a state shared b/w actors
Desc - Using a count variable to keep track of users using atomic Integer

    [ClusterAwareActor LNO 48] Operation of Atomic Integer
Desc - Child Actors created from parent user actor to send messages

    [ClusterAwareActor LNO 66] Child actor created with parameters as sender actorref and parent userName

### 4. Messages that are mutated
Desc - messageFrom object mutates the message and adds quotes

    [chatApplication LNO 69] setMessage() function 

### 5. Null Pointer Exceptions
Desc - when user sends a message with wrong/invalid userName

    [ClusterAwareActor LNO 71] getActorWithUserName() returns null on which we do null.get(0) NPE occurs

Desc - when user sends a message to all tho there are no online users

    [ClusterAwareActor LNO 64] Looping on a null object

Desc - when user does not input any message [e.g. @all ]

    [chatApplication LNO 28] Assigning null object to message
    [chatApplication LNO 69] setMessage() function edits the null message

### 6. Replies sent to Actor.noSender()
Desc - when user asks for online user list

    [ClusterAwareActor LNO 38] reply is sent to Actor.noSender() that the actor is querying
Desc - when users message is sent

    [ClusterAwareActor LNO 111] reply is sent to Actor.noSender() that the message is sent
