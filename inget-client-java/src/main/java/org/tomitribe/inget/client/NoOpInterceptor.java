package org.tomitribe.inget.client;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.logging.NoOpFaultListener;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class NoOpInterceptor extends AbstractPhaseInterceptor<Message> {
    public NoOpInterceptor() {
        super(Phase.PRE_LOGICAL);
    }

    public void handleMessage(final Message message) throws Fault {
        message.put(FaultListener.class.getName(), new NoOpFaultListener());
    }

    public void handleFault(final Message message) {
        // no-op
    }
}
