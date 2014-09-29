package org.pmazzoncini.blackjack.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pmazzoncini.blackjack.osgi.api.InstantiateDealer;

import java.util.Hashtable;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(InstantiateDealer.class, new InstantiateDealerImpl(), new Hashtable<>());
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }

}
