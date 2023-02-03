package dslabs.clientserver;

import dslabs.atmostonce.AMOCommand;
import dslabs.atmostonce.AMOResult;
import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static dslabs.clientserver.ClientTimer.CLIENT_RETRY_MILLIS;

/**
 * Simple client that sends requests to a single server and returns responses.
 *
 * See the documentation of {@link Client} and {@link Node} for important
 * implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleClient extends Node implements Client {
    private final Address serverAddress;

    //TODO: declare fields for your implementation
    private AMOCommand command;
    private AMOResult result;
    private int sequenceNumber;

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleClient(Address address, Address serverAddress) {
        super(address);
        this.serverAddress = serverAddress;
        //TODO: Initialize declared fields if necessary
        sequenceNumber = 0;
    }

    @Override
    public synchronized void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Client Methods
       -----------------------------------------------------------------------*/
    @Override
    public synchronized void sendCommand(Command command) {
        //TODO: send command to server
        sequenceNumber++;
        AMOCommand c = new AMOCommand(command, sequenceNumber,
                this.address().toString());
        this.command = c;
        this.result = null;
        this.send(new Request(c), serverAddress);
        this.set(new ClientTimer(c, sequenceNumber), CLIENT_RETRY_MILLIS);
    }

    @Override
    public synchronized boolean hasResult() {
        //TODO: check whether there is result
        return result != null;
    }

    @Override
    public synchronized Result getResult() throws InterruptedException {
        //TODO: wait to get result
        while (result == null) {
            wait();
        }
        return result.result();
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private synchronized void handleReply(Reply m, Address sender) {
        //TODO: check desired reply arrive
        if (result == null && m.amoResult().sequenceNumber() == sequenceNumber) {
            command = null;
            result = m.amoResult();
            notifyAll();
        }

    }

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private synchronized void onClientTimer(ClientTimer t) {
        //TODO: perform action when timer reach timeout
        if (command != null && t.sequenceNumber() == sequenceNumber) {
            this.send(new Request(command), serverAddress);
            this.set(t, CLIENT_RETRY_MILLIS);
        }
    }
}
