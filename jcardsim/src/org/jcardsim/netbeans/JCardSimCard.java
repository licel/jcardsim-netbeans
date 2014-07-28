package org.jcardsim.netbeans;

import java.awt.Image;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javacard.api.AntClasspathClosureProvider;
import org.netbeans.modules.javacard.api.RunMode;
import org.netbeans.modules.javacard.spi.AbstractCard;
import org.netbeans.modules.javacard.spi.CardState;
import static org.netbeans.modules.javacard.spi.CardState.BEFORE_STARTING;
import static org.netbeans.modules.javacard.spi.CardState.NOT_RUNNING;
import static org.netbeans.modules.javacard.spi.CardState.STARTING;
import org.netbeans.modules.javacard.spi.ICardCapability;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.capabilities.CapabilitiesProvider;
import org.netbeans.modules.javacard.spi.capabilities.CardInfo;
import org.netbeans.modules.javacard.spi.capabilities.ContactedProtocol;
import org.netbeans.modules.javacard.spi.capabilities.PortKind;
import org.netbeans.modules.javacard.spi.capabilities.PortProvider;
import org.netbeans.modules.javacard.spi.capabilities.StartCapability;
import org.netbeans.modules.javacard.spi.capabilities.UrlCapability;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * 
 * @author Vit
 */
public  class JCardSimCard extends AbstractCard {

    public static final String SINGLE_CARD_ID = "card"; //NOI18N

    private static final String POLL_URL = "http://smartcard:80/SysInfo/getData"; //NOI18N

    private static final String HOST = "smartcard"; //NOI18N

    private volatile boolean connected;
    
    private final RequestProcessor rp;

    public JCardSimCard(JavacardPlatform pform) {
        super(pform, SINGLE_CARD_ID);
        System.out.println("new JCardSimCard()");
        rp = new RequestProcessor("jCardSim"); //NOI18N
    }

    URL getPollUrl() throws MalformedURLException {
        return new URL(POLL_URL);
    }

    @Override
    protected void onBeforeFirstLookup() {
        initCapabilities(new Ports(), new Info(), new Caps(), new Urls(), new Start());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    void setConnected(boolean connected) {
        if (this.connected != connected) {
            this.connected = connected;
            connected &= super.isValid();
            setState(connected ? CardState.RUNNING : CardState.NOT_RUNNING);
        }
    }

    void refreshStatus() {
//        watchdog.refreshNow();
    }

    private final class Ports implements PortProvider {

        public Set<Integer> getClaimedPorts() {
            return Collections.singleton(80);
        }

        public Set<Integer> getPortsInUse() {
            return connected ? getPortsInUse() : Collections.<Integer>emptySet();
        }

        public String getHost() {
            return HOST;
        }

        public int getPort(PortKind role) {
            switch (role) {
                case HTTP:
                    return 80;
                default:
                    return -1;
            }
        }
    }

    public void log(String toLog) {
        System.out.println(">>>" + toLog);
    }

    private final class Info implements CardInfo {

        public String getSystemId() {
            return SINGLE_CARD_ID;
        }

        public String getDisplayName() {
            return NbBundle.getMessage(Info.class, "CARD_DISPLAY_NAME");  //NOI18N
        }

        public Image getIcon() {
            return ImageUtilities.loadImage("org/jcardsim/netbeans/otcard.png"); //NOI18N
        }

        public String getDescription() {
            return "Super Card jCard Sim";
//            PortProvider p = getCapability(PortProvider.class);
//            return NbBundle.getMessage(Info.class, "CARD_DESCRIPTION", //NOI18N
//                    p.getHost(), p.getClaimedPorts().iterator().next(),
//                    getPlatform().getDisplayName());
        }
    }

    private final class Caps implements CapabilitiesProvider {

        public Set<Class<? extends ICardCapability>> getSupportedCapabilityTypes() {
            Set<Class<? extends ICardCapability>> result = new HashSet<Class<? extends ICardCapability>>();
            result.add(CardInfo.class);
            result.add(PortProvider.class);
            result.add(CapabilitiesProvider.class);
            result.add(UrlCapability.class);
            result.add(Start.class);
            return result;
        }
    }

    private final class Urls implements UrlCapability {

        public ContactedProtocol getContactedProtocol() {
            return null;
        }

        public String getURL() {
            return "http://smartcard/";
        }

        public String getManagerURL() {
            return POLL_URL;
        }

        public String getListURL() {
            return null;
        }
    }

    class Start implements ICardCapability, StartCapability {

        private int DEBUG = 1;

        @Override
        public Condition start(RunMode mode, Project project) {
            if (project == null && mode != RunMode.RUN) {
                throw new NullPointerException("Project parameter required for DEBUG/PROFILE run modes"); //NOI18N
            }
            final boolean debug = mode == RunMode.DEBUG;
            if (!getState().isNotRunning()) {
                log("Already running, return dummy condition"); //NOI18N
                return new ConditionImpl();
            }

            final ConditionImpl c = new ConditionImpl(DEBUG, JCardSimCard.this);

            setState(BEFORE_STARTING);
            rp.post(new Starter(c, mode, project));
            return c;

//            File file = AntClasspathClosureProvider.getTargetArtifact(project);
//            log("JCardSimStartCapability.start:" + file.getAbsolutePath());
//            return new ConditionImpl(DEBUG, this);
        }
    }

    private final class Starter implements Runnable {

        private final ConditionImpl c;
        private final RunMode mode;
        private final Project project;

        private Starter(ConditionImpl c, RunMode mode, Project project) {
            this.c = c;
            this.mode = mode;
            this.project = project;
        }

        public void run() {
            log("Starter.start()");

            setState(STARTING);
            try {
                boolean debug = mode.isDebug();
//                if (debug) {
//                    final String[] cmdLine = getDebugProxyCommandLine(project);
//                    log("Will start debug process " + Arrays.asList(cmdLine)); //NOI18N
//                    if (cls.length <= 0) {
//                        throw new IllegalStateException("Debug command line empty"); //NOI18N
//                    }
//                    ed = new ExecutionDescriptor().controllable(true).frontWindow(true);
//                    RICard.ProcessLaunch debugLaunch = new RICard.ProcessLaunch(cmdLine, RUNNING_IN_DEBUG_MODE, c, false);
//                    ExecutionService debugService = ExecutionService.newService(debugLaunch,
//                            ed, NbBundle.getMessage(RICard.class, "DEBUG_TAB_NAME", getDisplayName()));   //NOI18N
//                    log("Starting debug process"); //NOI18N
//                    debugService.run();
//                }

                File file = AntClasspathClosureProvider.getTargetArtifact(project);
                log("JCardSimStartCapability.start:" + file.getAbsolutePath());
                log("JCardSimStartCapability.exists:" + file.exists());
                c.countdown();
            } catch (Exception e) {
                Logger.getLogger(JCardSimCard.class.getName()).log(Level.SEVERE,
                        "Problem starting " + getSystemId(), e); //NOI18N
//                killProcesses();
                setState(NOT_RUNNING);
                c.signalAll();
            }
            log("Starter.end()");
        }
    }
}
