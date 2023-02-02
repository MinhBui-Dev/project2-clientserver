package dslabs.clientserver;

import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Node;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import dslabs.kvstore.KVStore;

/**
 * Simple server that receives requests and returns responses.
 *
 * See the documentation of {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleServer extends Node {
    //TODO: declare fields for your implementation
    private KVStore app = new KVStore();

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleServer(Address address, Application app) {
        super(address);

        //TODO: record the passed application
        //Hints: wrap app inside your implementation for AMOApplication (Part 3)
        //this.app = new KVStore((KVStore) app);
    }

    @Override
    public void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handleRequest(Request m, Address sender) {
        //TODO: handle client request
        //Hints: call interface provided by AMOApplication to hide the duplication check from server
        Result r = app.execute(m.command());
        this.send(new Reply(r, m.sequenceNumber()), sender);
    }
}
