package dslabs.clientserver;

import dslabs.atmostonce.AMOApplication;
import dslabs.atmostonce.AMOCommand;
import dslabs.atmostonce.AMOResult;
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
    private AMOApplication<KVStore> amoApplication;

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleServer(Address address, Application app) {
        super(address);

        //TODO: record the passed application
        //Hints: wrap app inside your implementation for AMOApplication (Part 3)
        amoApplication = new AMOApplication<KVStore>((KVStore) app);

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
        AMOCommand command = m.amoCommand();
        AMOResult result = amoApplication.execute(command);
        this.send(new Reply(result), sender);
    }
}
