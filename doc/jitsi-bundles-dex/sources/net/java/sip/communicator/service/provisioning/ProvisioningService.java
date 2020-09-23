package net.java.sip.communicator.service.provisioning;

public interface ProvisioningService {
    String getProvisioningMethod();

    String getProvisioningPassword();

    String getProvisioningUri();

    String getProvisioningUsername();

    void setProvisioningMethod(String str);
}
