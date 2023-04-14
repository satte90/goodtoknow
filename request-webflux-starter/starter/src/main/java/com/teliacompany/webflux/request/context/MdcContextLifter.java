package com.teliacompany.webflux.request.context;

import com.teliacompany.webflux.request.utils.Constants;
import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

/**
 * Helper that copies the state of Reactor [Context] to MDC on the #onNext function.
 * Based on: https://github.com/archie-swif/webflux-mdc/blob/master/src/main/java/com/example/webfluxmdc/MdcContextLifter.java
 */
public final class MdcContextLifter<T> implements CoreSubscriber<T> {
    CoreSubscriber<T> coreSubscriber;

    public MdcContextLifter(CoreSubscriber<T> coreSubscriber) {
        this.coreSubscriber = coreSubscriber;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        coreSubscriber.onSubscribe(subscription);
    }

    @Override
    public void onNext(T obj) {
        copyToMdc(coreSubscriber.currentContext());
        coreSubscriber.onNext(obj);
    }

    @Override
    public void onError(Throwable t) {
        coreSubscriber.onError(t);
    }

    @Override
    public void onComplete() {
        copyToMdc(coreSubscriber.currentContext());
        coreSubscriber.onComplete();
    }

    @Override
    public Context currentContext() {
        return coreSubscriber.currentContext();
    }

    /**
     * Extension function for the Reactor [Context]. Copies the current context to the MDC, if context is empty clears the MDC.
     * State of the MDC after calling this method should be same as Reactor [Context] state.
     * One thread-local access only.
     */
    private void copyToMdc(Context subscriberContext) {

        if(!subscriberContext.isEmpty() && subscriberContext.hasKey(Constants.TRANSACTION_CONTEXT_KEY)) {
            TransactionContext ctx = subscriberContext.get(Constants.TRANSACTION_CONTEXT_KEY);
            MDC.put(Constants.MDC_TRANSACTION_ID_KEY, ctx.getTid());
            MDC.put(Constants.MDC_TCAD_KEY, ctx.getTcad());
            MDC.put(Constants.MDC_TSCID_KEY, ctx.getTscid());
        }
        // else dont do MDC.clear here, it will cause error logging to lose MDC data as it is done outside the subscriber context
    }

}
