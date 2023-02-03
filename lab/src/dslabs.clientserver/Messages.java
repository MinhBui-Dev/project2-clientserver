package dslabs.clientserver;

import dslabs.atmostonce.AMOCommand;
import dslabs.atmostonce.AMOResult;
import dslabs.framework.Command;
import dslabs.framework.Message;
import dslabs.framework.Result;
import lombok.Data;

@Data
class Request implements Message {
    //TODO: message for client request
    private final AMOCommand amoCommand;
}

@Data
class Reply implements Message {
    //TODO: message for server reply
    private final AMOResult amoResult;
}
