package net.java.sip.communicator.impl.dns;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.java.sip.communicator.plugin.desktoputil.SIPCommDialog;
import net.java.sip.communicator.plugin.desktoputil.TransparentPanel;
import net.java.sip.communicator.service.dns.DnssecRuntimeException;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.resources.ResourceManagementService;
import org.jitsi.util.StringUtils;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Type;

public class ConfigurableDnssecResolver extends UnboundResolver {
    static final String EVENT_TYPE = "DNSSEC_NOTIFICATION";
    public static final String PNAME_BASE_DNSSEC_PIN = "net.java.sip.communicator.util.dns.pin";
    public static final String PNAME_DNSSEC_VALIDATION_MODE = "net.java.sip.communicator.util.dns.DNSSEC_VALIDATION_MODE";
    private static final Logger logger = Logger.getLogger(ConfigurableDnssecResolver.class);
    /* access modifiers changed from: private */
    public ResourceManagementService R = DnsUtilActivator.getResources();
    private ConfigurationService config = DnsUtilActivator.getConfigurationService();
    private Map<String, Date> lastNotifications = new HashMap();

    private class DnssecDialog extends SIPCommDialog implements ActionListener {
        private static final long serialVersionUID = 0;
        private JButton cmdAck;
        private JButton cmdShowDetails;
        private final String domain;
        private JPanel pnlAdvanced;
        private JPanel pnlStandard;
        private final String reason;
        private DnssecDialogResult result = DnssecDialogResult.Deny;

        public DnssecDialog(String domain, String reason) {
            super(false);
            setModal(true);
            this.domain = domain;
            this.reason = reason;
            initComponents();
        }

        private void initComponents() {
            setLayout(new BorderLayout(15, 15));
            setTitle(ConfigurableDnssecResolver.this.R.getI18NString("util.dns.INSECURE_ANSWER_TITLE"));
            JLabel imgWarning = new JLabel(ConfigurableDnssecResolver.this.R.getImage("service.gui.icons.WARNING_ICON"));
            imgWarning.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(imgWarning, "West");
            add(new JLabel(ConfigurableDnssecResolver.this.R.getI18NString("util.dns.DNSSEC_WARNING", new String[]{ConfigurableDnssecResolver.this.R.getSettingsString("service.gui.APPLICATION_NAME"), this.domain})), "Center");
            this.cmdAck = new JButton(ConfigurableDnssecResolver.this.R.getI18NString("service.gui.OK"));
            this.cmdAck.addActionListener(this);
            this.cmdShowDetails = new JButton(ConfigurableDnssecResolver.this.R.getI18NString("util.dns.DNSSEC_ADVANCED_OPTIONS"));
            this.cmdShowDetails.addActionListener(this);
            this.pnlStandard = new TransparentPanel(new BorderLayout());
            this.pnlStandard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            this.pnlStandard.add(this.cmdShowDetails, "West");
            this.pnlStandard.add(this.cmdAck, "East");
            add(this.pnlStandard, "South");
            this.pnlAdvanced = new TransparentPanel(new BorderLayout());
            JPanel pnlAdvancedButtons = new TransparentPanel(new FlowLayout(2));
            pnlAdvancedButtons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            this.pnlAdvanced.add(pnlAdvancedButtons, "East");
            for (DnssecDialogResult r : DnssecDialogResult.values()) {
                JButton cmd = new JButton(ConfigurableDnssecResolver.this.R.getI18NString("net.java.sip.communicator.util.dns.ConfigurableDnssecResolver$DnssecDialogResult." + r.name()));
                cmd.setActionCommand(r.name());
                cmd.addActionListener(this);
                pnlAdvancedButtons.add(cmd);
            }
            JLabel lblReason = new JLabel(this.reason);
            lblReason.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            this.pnlAdvanced.add(lblReason, "North");
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == this.cmdAck) {
                this.result = DnssecDialogResult.Deny;
                dispose();
            } else if (e.getSource() == this.cmdShowDetails) {
                getContentPane().remove(this.pnlStandard);
                add(this.pnlAdvanced, "South");
                pack();
            } else {
                this.result = (DnssecDialogResult) Enum.valueOf(DnssecDialogResult.class, e.getActionCommand());
                dispose();
            }
        }

        public DnssecDialogResult getResult() {
            return this.result;
        }
    }

    private enum DnssecDialogResult {
        Accept,
        Deny,
        AlwaysAccept,
        AlwaysDeny
    }

    public ConfigurableDnssecResolver() {
        reset();
        Lookup.setDefaultResolver(this);
        DnsUtilActivator.getNotificationService().registerDefaultNotificationForEvent(EVENT_TYPE, "PopupMessageAction", null, null);
    }

    /* access modifiers changed from: protected */
    public void validateMessage(SecureMessage msg) throws DnssecRuntimeException {
        String fqdn = msg.getQuestion().getName().toString();
        String propName = createPropNameUnsigned(fqdn, Type.string(msg.getQuestion().getType()));
        SecureResolveMode defaultAction = (SecureResolveMode) Enum.valueOf(SecureResolveMode.class, this.config.getString(PNAME_DNSSEC_VALIDATION_MODE, SecureResolveMode.WarnIfBogus.name()));
        SecureResolveMode pinned = (SecureResolveMode) Enum.valueOf(SecureResolveMode.class, this.config.getString(propName, defaultAction.name()));
        if (pinned == defaultAction) {
            this.config.setProperty(propName, pinned.name());
        }
        if (pinned != SecureResolveMode.IgnoreDnssec && !msg.isSecure()) {
            if ((pinned == SecureResolveMode.SecureOnly && !msg.isSecure()) || (pinned == SecureResolveMode.SecureOrUnsigned && msg.isBogus())) {
                String text = getExceptionMessage(msg);
                Date last = (Date) this.lastNotifications.get(text);
                if (last == null || last.before(new Date(new Date().getTime() - 300000))) {
                    DnsUtilActivator.getNotificationService().fireNotification(EVENT_TYPE, this.R.getI18NString("util.dns.INSECURE_ANSWER_TITLE"), text, null);
                    this.lastNotifications.put(text, new Date());
                }
                throw new DnssecRuntimeException(text);
            } else if (pinned == SecureResolveMode.SecureOrUnsigned && !msg.isBogus()) {
            } else {
                if (pinned != SecureResolveMode.WarnIfBogus || msg.isBogus()) {
                    String reason;
                    if (msg.isBogus()) {
                        reason = this.R.getI18NString("util.dns.DNSSEC_ADVANCED_REASON_BOGUS", new String[]{fqdn, msg.getBogusReason()});
                    } else {
                        reason = this.R.getI18NString("util.dns.DNSSEC_ADVANCED_REASON_UNSIGNED", new String[]{type, fqdn});
                    }
                    DnssecDialog dlg = new DnssecDialog(fqdn, reason);
                    dlg.setVisible(true);
                    switch (dlg.getResult()) {
                        case Deny:
                            throw new DnssecRuntimeException(getExceptionMessage(msg));
                        case AlwaysAccept:
                            if (msg.isBogus()) {
                                this.config.setProperty(propName, SecureResolveMode.IgnoreDnssec.name());
                                return;
                            } else {
                                this.config.setProperty(propName, SecureResolveMode.WarnIfBogus.name());
                                return;
                            }
                        case AlwaysDeny:
                            this.config.setProperty(propName, SecureResolveMode.SecureOnly);
                            throw new DnssecRuntimeException(getExceptionMessage(msg));
                        default:
                            return;
                    }
                }
            }
        }
    }

    private String getExceptionMessage(SecureMessage msg) {
        if (msg.getBogusReason() == null) {
            return this.R.getI18NString("util.dns.INSECURE_ANSWER_MESSAGE_NO_REASON", new String[]{msg.getQuestion().getName().toString()});
        }
        return this.R.getI18NString("util.dns.INSECURE_ANSWER_MESSAGE_REASON", new String[]{msg.getQuestion().getName().toString(), msg.getBogusReason()});
    }

    private String createPropNameUnsigned(String fqdn, String type) {
        return "net.java.sip.communicator.util.dns.pin." + fqdn.replace(Separators.DOT, "__");
    }

    public void reset() {
        String forwarders = DnsUtilActivator.getConfigurationService().getString(DnsUtilActivator.PNAME_DNSSEC_NAMESERVERS);
        if (!StringUtils.isNullOrEmpty(forwarders, true)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Setting DNSSEC forwarders to: " + Arrays.toString(forwarders.split(Separators.COMMA)));
            }
            super.setForwarders(forwarders.split(Separators.COMMA));
        }
        int i = 1;
        while (true) {
            String anchor = DnsUtilActivator.getResources().getSettingsString("net.java.sip.communicator.util.dns.DS_ROOT." + i);
            if (anchor != null) {
                clearTrustAnchors();
                addTrustAnchor(anchor);
                if (logger.isTraceEnabled()) {
                    logger.trace("Loaded trust anchor " + anchor);
                }
                i++;
            } else {
                return;
            }
        }
    }
}
