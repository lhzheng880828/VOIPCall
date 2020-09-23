package net.java.sip.communicator.plugin.provisioning;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.java.sip.communicator.plugin.desktoputil.SIPCommCheckBox;
import net.java.sip.communicator.plugin.desktoputil.SIPCommRadioButton;
import net.java.sip.communicator.plugin.desktoputil.SIPCommTextField;
import net.java.sip.communicator.plugin.desktoputil.TransparentPanel;
import net.java.sip.communicator.service.gui.ExportedWindow;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.resources.ResourceManagementService;

public class ProvisioningForm extends TransparentPanel {
    private static final long serialVersionUID = 0;
    /* access modifiers changed from: private|final */
    public final JRadioButton bonjourButton;
    /* access modifiers changed from: private|final */
    public final JRadioButton dhcpButton;
    /* access modifiers changed from: private|final */
    public final JRadioButton dnsButton;
    /* access modifiers changed from: private|final */
    public final JCheckBox enableCheckBox;
    /* access modifiers changed from: private|final */
    public final JButton forgetPasswordButton;
    /* access modifiers changed from: private|final */
    public final JRadioButton manualButton;
    /* access modifiers changed from: private|final */
    public final JPasswordField passwordField;
    /* access modifiers changed from: private|final */
    public final SIPCommTextField uriField;
    /* access modifiers changed from: private|final */
    public final JTextField usernameField;

    public ProvisioningForm() {
        super(new BorderLayout());
        final ResourceManagementService resources = ProvisioningActivator.getResourceService();
        ConfigurationService config = ProvisioningActivator.getConfigurationService();
        this.enableCheckBox = new SIPCommCheckBox(resources.getI18NString("plugin.provisioning.ENABLE_DISABLE"));
        this.dhcpButton = new SIPCommRadioButton(resources.getI18NString("plugin.provisioning.DHCP"));
        this.dnsButton = new SIPCommRadioButton(resources.getI18NString("plugin.provisioning.DNS"));
        this.bonjourButton = new SIPCommRadioButton(resources.getI18NString("plugin.provisioning.BONJOUR"));
        this.manualButton = new SIPCommRadioButton(resources.getI18NString("plugin.provisioning.MANUAL"));
        this.uriField = new SIPCommTextField(resources.getI18NString("plugin.provisioning.URI"));
        JPanel mainPanel = new TransparentPanel();
        add(mainPanel, "North");
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        this.enableCheckBox.setAlignmentX(0.0f);
        c.fill = 2;
        c.weightx = 1.0d;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 10, 0);
        mainPanel.add(this.enableCheckBox, c);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(this.dhcpButton);
        buttonGroup.add(this.bonjourButton);
        buttonGroup.add(this.dnsButton);
        buttonGroup.add(this.manualButton);
        JPanel radioButtonPanel = new TransparentPanel(new GridLayout(0, 1));
        radioButtonPanel.setBorder(BorderFactory.createTitledBorder(resources.getI18NString("plugin.provisioning.AUTO")));
        radioButtonPanel.add(this.dhcpButton);
        radioButtonPanel.add(this.bonjourButton);
        radioButtonPanel.add(this.dnsButton);
        c.fill = 2;
        c.weightx = 1.0d;
        c.insets = new Insets(0, 20, 0, 0);
        c.gridx = 0;
        c.gridy = 1;
        mainPanel.add(radioButtonPanel, c);
        c.fill = 2;
        c.weightx = 1.0d;
        c.insets = new Insets(0, 26, 0, 0);
        c.gridx = 0;
        c.gridy = 2;
        mainPanel.add(this.manualButton, c);
        c.fill = 2;
        c.weightx = 1.0d;
        c.insets = new Insets(0, 51, 0, 0);
        c.gridx = 0;
        c.gridy = 3;
        mainPanel.add(this.uriField, c);
        JPanel uuidPanel = new TransparentPanel(new FlowLayout(0));
        final JTextField uuidPane = new JTextField();
        uuidPane.setEditable(false);
        uuidPane.setOpaque(false);
        uuidPane.setText(config.getString(ProvisioningServiceImpl.PROVISIONING_UUID_PROP));
        uuidPanel.add(new JLabel(resources.getI18NString("plugin.provisioning.UUID")));
        uuidPanel.add(uuidPane);
        c.fill = 2;
        c.weightx = 0.0d;
        c.insets = new Insets(10, 10, 0, 0);
        c.gridwidth = 0;
        c.gridx = 0;
        c.gridy = 4;
        mainPanel.add(uuidPanel, c);
        JButton clipboardBtn = new JButton(resources.getI18NString("plugin.provisioning.COPYTOCLIPBOARD"));
        clipboardBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                if (clipboard != null) {
                    StringSelection data = new StringSelection(uuidPane.getText());
                    clipboard.setContents(data, data);
                    return;
                }
                JOptionPane.showMessageDialog(ProvisioningForm.this, resources.getI18NString("plugin.provisioning.CLIPBOARD_FAILED"), resources.getI18NString("plugin.provisioning.CLIPBOARD_FAILED"), 0);
            }
        });
        c.fill = 2;
        c.weightx = 1.0d;
        c.insets = new Insets(5, 10, 0, 0);
        c.gridwidth = 0;
        c.gridx = 0;
        c.gridy = 5;
        mainPanel.add(clipboardBtn, c);
        JPanel userPassPanel = new TransparentPanel(new BorderLayout());
        userPassPanel.setBorder(BorderFactory.createTitledBorder(ProvisioningActivator.getResourceService().getI18NString("plugin.provisioning.CREDENTIALS")));
        JPanel labelPanel = new TransparentPanel(new GridLayout(0, 1));
        labelPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 0));
        JPanel valuesPanel = new TransparentPanel(new GridLayout(0, 1));
        valuesPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 0));
        labelPanel.add(new JLabel(ProvisioningActivator.getResourceService().getI18NString("plugin.simpleaccregwizz.LOGIN_USERNAME")));
        labelPanel.add(new JLabel(ProvisioningActivator.getResourceService().getI18NString("service.gui.PASSWORD")));
        this.usernameField = new JTextField();
        this.usernameField.setEditable(false);
        this.passwordField = new JPasswordField();
        this.passwordField.setEditable(false);
        valuesPanel.add(this.usernameField);
        valuesPanel.add(this.passwordField);
        userPassPanel.add(labelPanel, "West");
        userPassPanel.add(valuesPanel, "Center");
        JPanel buttonPanel = new TransparentPanel(new FlowLayout(2));
        this.forgetPasswordButton = new JButton(resources.getI18NString("plugin.provisioning.FORGET_PASSWORD"));
        buttonPanel.add(this.forgetPasswordButton);
        userPassPanel.add(buttonPanel, "South");
        c.fill = 2;
        c.weightx = 1.0d;
        c.insets = new Insets(5, 10, 0, 0);
        c.gridwidth = 0;
        c.gridx = 0;
        c.gridy = 6;
        mainPanel.add(userPassPanel, c);
        JTextPane pane = new JTextPane();
        pane.setForeground(Color.RED);
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.setText(ProvisioningActivator.getResourceService().getI18NString("plugin.provisioning.RESTART_WARNING", new String[]{ProvisioningActivator.getResourceService().getSettingsString("service.gui.APPLICATION_NAME")}));
        c.gridy = 7;
        mainPanel.add(pane, c);
        initButtonStates();
        initListeners();
    }

    private void initButtonStates() {
        boolean isProvEnabled;
        String provMethod = ProvisioningActivator.getProvisioningService().getProvisioningMethod();
        if (provMethod == null || provMethod.length() <= 0 || provMethod.equals("NONE")) {
            isProvEnabled = false;
        } else {
            isProvEnabled = true;
        }
        this.enableCheckBox.setSelected(isProvEnabled);
        if (isProvEnabled) {
            if (provMethod.equals("DHCP")) {
                this.dhcpButton.setSelected(true);
            } else if (provMethod.equals("DNS")) {
                this.dnsButton.setSelected(true);
            } else if (provMethod.equals("Bonjour")) {
                this.bonjourButton.setSelected(true);
            } else if (provMethod.equals("Manual")) {
                this.manualButton.setSelected(true);
                String uri = ProvisioningActivator.getProvisioningService().getProvisioningUri();
                if (uri != null) {
                    this.uriField.setText(uri);
                }
            }
        }
        this.dhcpButton.setEnabled(isProvEnabled);
        this.manualButton.setEnabled(isProvEnabled);
        this.uriField.setEnabled(this.manualButton.isSelected());
        this.bonjourButton.setEnabled(isProvEnabled);
        this.dnsButton.setEnabled(false);
        this.forgetPasswordButton.setEnabled(isProvEnabled);
        this.usernameField.setText(ProvisioningActivator.getConfigurationService().getString("net.java.sip.communicator.plugin.provisioning.auth.USERNAME"));
        if (ProvisioningActivator.getCredentialsStorageService().isStoredEncrypted("net.java.sip.communicator.plugin.provisioning.auth")) {
            this.passwordField.setText(ProvisioningActivator.getCredentialsStorageService().loadPassword("net.java.sip.communicator.plugin.provisioning.auth"));
        }
    }

    private void initListeners() {
        this.enableCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                boolean isSelected = ProvisioningForm.this.enableCheckBox.isSelected();
                ProvisioningForm.this.dhcpButton.setEnabled(isSelected);
                ProvisioningForm.this.bonjourButton.setEnabled(isSelected);
                ProvisioningForm.this.manualButton.setEnabled(isSelected);
                ProvisioningForm.this.forgetPasswordButton.setEnabled(isSelected);
                String provisioningMethod = null;
                if (isSelected) {
                    if (ProvisioningForm.this.dhcpButton.isSelected()) {
                        provisioningMethod = "DHCP";
                    } else if (ProvisioningForm.this.dnsButton.isSelected()) {
                        provisioningMethod = "DNS";
                    } else if (ProvisioningForm.this.bonjourButton.isSelected()) {
                        provisioningMethod = "Bonjour";
                    } else if (ProvisioningForm.this.manualButton.isSelected()) {
                        provisioningMethod = "Manual";
                    } else {
                        ProvisioningForm.this.dhcpButton.setSelected(true);
                        provisioningMethod = "DHCP";
                    }
                }
                ProvisioningActivator.getProvisioningService().setProvisioningMethod(provisioningMethod);
            }
        });
        this.dhcpButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (ProvisioningForm.this.dhcpButton.isSelected()) {
                    ProvisioningActivator.getProvisioningService().setProvisioningMethod("DHCP");
                }
            }
        });
        this.dnsButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (ProvisioningForm.this.dnsButton.isSelected()) {
                    ProvisioningActivator.getProvisioningService().setProvisioningMethod("DNS");
                }
            }
        });
        this.bonjourButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (ProvisioningForm.this.bonjourButton.isSelected()) {
                    ProvisioningActivator.getProvisioningService().setProvisioningMethod("Bonjour");
                }
            }
        });
        this.manualButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                boolean isSelected = ProvisioningForm.this.manualButton.isSelected();
                ProvisioningForm.this.uriField.setEnabled(isSelected);
                if (isSelected) {
                    ProvisioningActivator.getProvisioningService().setProvisioningMethod("Manual");
                    String uriText = ProvisioningForm.this.uriField.getText();
                    if (uriText != null && uriText.length() > 0) {
                        ProvisioningActivator.getProvisioningService().setProvisioningUri(uriText);
                        return;
                    }
                    return;
                }
                ProvisioningActivator.getProvisioningService().setProvisioningUri(null);
            }
        });
        this.uriField.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                if (ProvisioningForm.this.manualButton.isSelected()) {
                    String uriText = ProvisioningForm.this.uriField.getText();
                    if (uriText != null && uriText.length() > 0) {
                        ProvisioningActivator.getProvisioningService().setProvisioningUri(uriText);
                    }
                }
            }

            public void focusGained(FocusEvent e) {
            }
        });
        this.forgetPasswordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (ProvisioningForm.this.passwordField.getPassword() != null && ProvisioningForm.this.passwordField.getPassword().length != 0 && JOptionPane.showConfirmDialog((Component) ProvisioningActivator.getUIService().getExportedWindow(ExportedWindow.MAIN_WINDOW).getSource(), ProvisioningActivator.getResourceService().getI18NString("plugin.provisioning.REMOVE_CREDENTIALS_MESSAGE"), ProvisioningActivator.getResourceService().getI18NString("service.gui.REMOVE"), 0) == 0) {
                    ProvisioningActivator.getCredentialsStorageService().removePassword("net.java.sip.communicator.plugin.provisioning.auth");
                    ProvisioningActivator.getConfigurationService().removeProperty("net.java.sip.communicator.plugin.provisioning.auth.USERNAME");
                    ProvisioningForm.this.usernameField.setText("");
                    ProvisioningForm.this.passwordField.setText("");
                }
            }
        });
    }
}
