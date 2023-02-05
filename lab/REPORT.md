# Project 2: Report
## Intro (Your understanding)
The goal of this project is to build a key-value storing application (KV Store).
This application would sit on a server that is capable of processing commands from multiple clients.
The server and client need to be implemented such that the commands from client are executed exactly once by the server.

## Flow of Control & Code Design

### KV Store
KV Store processes the commands `GET`, `PUT`, `APPEND` and returns the result to the client.
The KV Store is implemented as a `HashMap<String, String>` where the key is the key of the key-value pair and the value is the value of the key-value pair.
`GET` command returns the value of the key in `GetResult` if it exists , otherwise returns `KeyNotFound`.
`PUT` command inserts the key-value pair into the store regardless of whether the key exists or not, and returns `PutOk`.
`APPEND` command appends the value to the existing value of the key if the key exists, otherwise inserts the key-value pair into the store. Afterward, it returns the value in `AppendResult`.

### SimpleClient
SimpleClient contains a Lamport Clock to keep track of its tasks, declared as `sequenceNumber` and initialized as 0.
It sends messages to the server with the `SendCommand` method. Every time `SendCommand` is called, `sequenceNumber` is incremented by 1.
The client creates a unique `AMOCommand` using the `sequenceNumber`, client address, and the command sent by the user. 
AMOResult is set to `null` and `AMOCommand` is sent to the server. At this time, client will also set a timer for the command with 1000 milliseconds retry interval.

#### Timers and Retries
The client will set a timer for every command sent to the server at 1000 milliseconds retry interval.
When the timer goes off, the client will check if result is `null` and if `sequenceNumber` of the timer is the same as the `sequenceNumber` of the client.
If both conditions are met, the client will resend the command to the server and reset the timer. Otherwise, the timer will be discarded.

#### HandleReply
When the client receives a reply from the server, it will check if the `sequenceNumber` of the reply is the same as the `sequenceNumber` of the client.
Since the client will only send one command at a time, the `sequenceNumber` of the awaited reply should be the same as the `sequenceNumber` of the client. 
Otherwise, the reply could be for a previous command and the client will discard the reply.

### SimpleServer
The job of server is to receive client's requests, pass the request to `AMOApplication`, and send the result of the command to the client.
SimpleServer wraps the KV Store inside `AMOApplication` in its constructor. `AMOApplication` will actually check for duplicate commands, execute the command only once, and return the result to server afterward.

### Messages
Messages between client and server are defined as `Request` and `Reply`. 
`Request` contains the `AMOCommand` sent by the client and `Reply` contains the `AMOResult` returned by the server.

```java
    class Request implements Message {
        //TODO: message for client request
        private final AMOCommand amoCommand;
    }
    
    class Reply implements Message {
        //TODO: message for server reply
        private final AMOResult amoResult;
    }
```

### AMOApplication
AMOApplication is a management layer that sits on top of the KV Store. 
It is responsible for checking for duplicate commands, keeping track of the commands and corresponding results that have been executed, and executing the command only once.

AMOApplication has a data structure called `ClientSequenceNumber` that keeps track of the `sequenceNumber` of the clients, essentially acting as a vector clock. 
It also has a data structure called `ClientResult` that keeps track of the `AMOResult` of already executed commands and the corresponding client address.
When the AMOApplication receives a command, it will check if the command is a duplicate command.If the `sequenceNumber` of the command is the same or less than the `sequenceNumber` inside the `ClientSequenceNumber`, 
the command is a duplicate and the stored result is returned to the server. Otherwise, the command is not a duplicate and the command is executed by the KV Store.
The result will be stored in `ClientResult` and the `sequenceNumber` will be stored in `ClientSequenceNumber`. 

```Java
    public final class AMOCommand implements Command {
    //TODO: implement your wrapper for command
    //Hints: think carefully about what information is required for server to check duplication
    private final Command command;
    private final int sequenceNumber;
    private final String clientAddress;
    }

    public final class AMOResult implements Result {
        //TODO: implement your wrapper for result
        //Hints: think carefully about what information is required for client to check duplication
        private final Result result;
        private final int sequenceNumber;
    }
```

## Design Decisions

### Messages
Originally, I was using `Command` and `Result` as data types for messages between client and server, and added `sequenceNumber` to keep track of the commands in part2 of the lab.
However, in part3, where I need to implement `AMOApplication` as management layer, I decided that the metadata such as `sequenceNumber` and `clientAddress` should be wrapped inside `AMOCommand` and `AMOResult` instead of `Reply` and `Request`.
This is because `AMOApplication` is the only layer that needs to know about the metadata, and it is easier to implement `AMOApplication` if the metadata is wrapped inside `AMOCommand` and `AMOResult`.

### SimpleClient/SimpleServer
The changes from part2 to part3 are minimal for `SimpleClient` and `SimpleServer`.
The logic still remains the same, except that `SimpleClient` and `SimpleServer` now send and receive `AMOCommand` and `AMOResult` instead of `Command` and `Result`.
As such, the metadata is stored inside `AMOCommand` and `AMOResult` instead of `Request` and `Reply`.

#### SimpleClient
In handleReply, I only check if the `sequenceNumber` of the reply is the same as the `sequenceNumber` of the client because the client will only send one command at a time.
The same condition is also used to check if the timer should be reset or discarded.

#### SimpleServer
Since management layer is implemented in `AMOApplication`, the server only needs to pass the command to `AMOApplication` and send the result back to the client.
Thus, the logic is very simple, server could work as either exactly-once or at least-once.

### AMOApplication
The `ClientResult` data structure only stores on previous result for each client. This is designed on that fact that each client can only send one command at a time.
Thus, if server receives a new command from the client, and it is not duplicated, then previous result is no longer needed and will be overwritten by the new result.
The `ClientSequenceNumber` vector clock is used for this purpose as well as garbage collection. As If we know the sequenceNumber is increased, then we know the previous result is no longer needed.

## Missing Components

### Client
The client is not implemented to send multiple commands at the same time as this is not required for the lab.
However, to implement this feature, the client will need to keep track of the `sequenceNumber` of each command sent to the server as well as the `sequenceNumber` of the reply received from the server.

### Server
The requests to the server are handled on first-come first-served basis. In real life scenario, most likely the KV Store will need to in consistent state across the clients. 
Thus, we would need to establish an ordering of the requests and execute them in order to ensure consistency and accuracy of the results.

## References
- [DSLab Framework](https://ellismichael.com/dslabs/javadoc/dslabs/framework/package-summary.html)
- [Prof Ellis's Lecture Slides](https://ellismichael.com/dslabs/452%20Discussion%20Section%20Slides.pdf)
- [Prof ADA's Video Lecture on Lesson 3](https://gatech.instructure.com/)
