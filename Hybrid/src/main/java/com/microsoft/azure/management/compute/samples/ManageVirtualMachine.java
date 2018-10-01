/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.samples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.compute.v2017_03_30.CachingTypes;
import com.microsoft.azure.management.compute.v2017_03_30.DataDisk;
import com.microsoft.azure.management.compute.v2017_03_30.DiskCreateOptionTypes;
import com.microsoft.azure.management.compute.v2017_03_30.HardwareProfile;
import com.microsoft.azure.management.compute.v2017_03_30.ImageReference;
import com.microsoft.azure.management.compute.v2017_03_30.LinuxConfiguration;
import com.microsoft.azure.management.compute.v2017_03_30.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.v2017_03_30.NetworkProfile;
import com.microsoft.azure.management.compute.v2017_03_30.OSDisk;
import com.microsoft.azure.management.compute.v2017_03_30.OSProfile;
import com.microsoft.azure.management.compute.v2017_03_30.OperatingSystemTypes;
import com.microsoft.azure.management.compute.v2017_03_30.StorageProfile;
import com.microsoft.azure.management.compute.v2017_03_30.VirtualHardDisk;
import com.microsoft.azure.management.compute.v2017_03_30.VirtualMachine;
import com.microsoft.azure.management.compute.v2017_03_30.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.v2017_10_01.AddressSpace;
import com.microsoft.azure.management.network.v2017_10_01.IPAllocationMethod;
import com.microsoft.azure.management.network.v2017_10_01.IPVersion;
import com.microsoft.azure.management.network.v2017_10_01.NetworkInterface;
import com.microsoft.azure.management.network.v2017_10_01.PublicIPAddress;
import com.microsoft.azure.management.network.v2017_10_01.PublicIPAddressDnsSettings;
import com.microsoft.azure.management.network.v2017_10_01.Subnet;
import com.microsoft.azure.management.network.v2017_10_01.VirtualNetwork;
import com.microsoft.azure.management.network.v2017_10_01.implementation.NetworkInterfaceIPConfigurationInner;
import com.microsoft.azure.management.profile_2018_03_01_hybrid.Azure;
import com.microsoft.azure.management.resources.v2018_02_01.ResourceGroup;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.storage.v2016_01_01.Kind;
import com.microsoft.azure.management.storage.v2016_01_01.Sku;
import com.microsoft.azure.management.storage.v2016_01_01.SkuName;
import com.microsoft.azure.management.storage.v2016_01_01.StorageAccount;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * Azure Compute sample for managing virtual machines - - Create a virtual
 * machine with managed OS Disk - Start a virtual machine - Stop a virtual
 * machine - Restart a virtual machine - Update a virtual machine - Tag a
 * virtual machine (there are many possible variations here) - Attach data disks
 * - Detach data disks - List virtual machines - Delete a virtual machine.
 */
public final class ManageVirtualMachine {

    /**
     * Main function which runs the actual sample.
     * 
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean manageVm(Azure azure, String location) {
        final String windowsVMName = Utils.createRandomName("wVM");
        final String linuxVMName = Utils.createRandomName("lVM");
        final String rgName = Utils.createRandomName("rgCOMV");
        final String saName = Utils.createRandomName("sa");
        final String networkName = Utils.createRandomName("vnet");
        final String pipName = Utils.createRandomName("pip");
        final String domainNameLabel = Utils.createRandomName("dns");
        final String nicName = Utils.createRandomName("nic");
        final String subnetName = "subnet1";
        final String userName = "tirekicker";
        final String password = "12NewPA$$w0rd!";

        try {
            
            // ============================================================= 
            // Create a Linux virtual machine


            // Create a resource group
            //
            ResourceGroup resourceGroup = azure.resourceGroups().define(rgName).withExistingSubscription()
                    .withLocation(location).create();

            // Create a Storage Account
            //
            StorageAccount storageAccount = azure.storageAccounts().define(saName).withRegion(location)
                    .withExistingResourceGroup(rgName).withKind(Kind.STORAGE)
                    .withSku(new Sku().withName(SkuName.STANDARD_LRS)).create();

            // Create virtual network
            //
            List<String> addressPrefixes = new ArrayList<>();
            addressPrefixes.add("10.0.0.0/28");
            VirtualNetwork virtualNetwork = azure.virtualNetworks().define(networkName).withRegion(location)
                    .withExistingResourceGroup(rgName)
                    .withAddressSpace(new AddressSpace().withAddressPrefixes(addressPrefixes)).create();

            // Create subnet in the virtual network
            //
            Subnet subnet = azure.subnets().define(subnetName).withExistingVirtualNetwork(rgName, networkName)
                    .withAddressPrefix("10.0.0.0/28").create();

            // Create a public address
            //
            PublicIPAddress publicIPAddress = azure.publicIPAddresses().define(pipName).withRegion(location)
                    .withExistingResourceGroup(rgName).withPublicIPAllocationMethod(IPAllocationMethod.DYNAMIC)
                    .withPublicIPAddressVersion(IPVersion.IPV4)
                    .withDnsSettings(new PublicIPAddressDnsSettings().withDomainNameLabel(domainNameLabel)).create();

            // Create a network interface
            //
            List<NetworkInterfaceIPConfigurationInner> ipConfigs = new ArrayList<>();
            ipConfigs.add(new NetworkInterfaceIPConfigurationInner().withName("primary").withPrimary(true)
                    .withPrivateIPAddressVersion(IPVersion.IPV4)
                    .withPrivateIPAllocationMethod(IPAllocationMethod.DYNAMIC)
                    .withPublicIPAddress(publicIPAddress.inner()).withSubnet(subnet.inner()));

            NetworkInterface networkInterface = azure.networkInterfaces().defineNetworkInterface(nicName)
                    .withRegion(location).withExistingResourceGroup(rgName).withIpConfigurations(ipConfigs).create();

            // VM Hardware profile
            //
            HardwareProfile vmHardwareProfile = new HardwareProfile().withVmSize(VirtualMachineSizeTypes.STANDARD_A2);

            // VM OS Profile
            //
            OSProfile vmOsProfile = new OSProfile().withAdminUsername(userName).withAdminPassword(password)
                    .withLinuxConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(false))
                    .withComputerName("testlinux");

            // VM Network profile
            //
            final NetworkProfile vmNetworkProfile = new NetworkProfile()
                    .withNetworkInterfaces(new ArrayList<NetworkInterfaceReference>());
            NetworkInterfaceReference nicReference = new NetworkInterfaceReference();
            nicReference.withPrimary(true);
            nicReference.withId(networkInterface.id());
            vmNetworkProfile.networkInterfaces().add(nicReference);

            // VM Storage profile
            //
            StorageProfile vmStorageProfile = new StorageProfile();
            vmStorageProfile.withImageReference(new ImageReference().withPublisher("Canonical")
                    .withOffer("UbuntuServer").withSku("16.04-LTS").withVersion("1.0.0"));

            // OS disk
            //
            final String osDiskName = "osDisk1";
            final String osDiskVhdName = osDiskName + ".vhd"; 
            final String osDiskVhdUri = storageAccount.primaryEndpoints().blob() + "test" + "/" + osDiskVhdName;
            vmStorageProfile.withOsDisk(new OSDisk());
            vmStorageProfile.osDisk().withCaching(CachingTypes.READ_WRITE);
            vmStorageProfile.osDisk().withOsType(OperatingSystemTypes.LINUX); 
            vmStorageProfile.osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
            vmStorageProfile.osDisk().withName(osDiskName);
            vmStorageProfile.osDisk().withVhd(new VirtualHardDisk().withUri(osDiskVhdUri));

            // Data disks
            //
            final String dataDiskName = "datadisk1";
            final String dataDiskVhdName = dataDiskName + ".vhd"; 
            final String dataDiskVhdUri = storageAccount.primaryEndpoints().blob() + "test" + "/" + dataDiskVhdName;
            vmStorageProfile.withDataDisks(new ArrayList<DataDisk>());
            DataDisk lun1Disk = new DataDisk().withLun(1).withDiskSizeGB(1).withCaching(CachingTypes.READ_ONLY)
                    .withCreateOption(DiskCreateOptionTypes.EMPTY).withName(dataDiskName)
                    .withVhd(new VirtualHardDisk().withUri(dataDiskVhdUri));
            vmStorageProfile.dataDisks().add(lun1Disk);

            // Create Linux Vm
            //
            Date t1 = new Date();
            VirtualMachine virtualMachine = azure.virtualMachines().define(linuxVMName).withRegion(location)
                    .withExistingResourceGroup(rgName).withHardwareProfile(vmHardwareProfile).withOsProfile(vmOsProfile)
                    .withNetworkProfile(vmNetworkProfile).withStorageProfile(vmStorageProfile).create();

            Date t2 = new Date();
            System.out.println(
                    "Created VM: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + virtualMachine.id());
                    
            // Print virtual machine details
            Utils.print(virtualMachine);

            // ============================================================= 
            // Update - Tag the virtual machine

            virtualMachine.update().withTag("who-rocks", "java").withTag("where", "on azure").apply();
            System.out.println("Tagged VM: " + virtualMachine.id());


            // ============================================================= 
            // Update - Add data disk
            //
            String newdataDiskName = "dataDisk2";
            String newdataDiskVhdName = newdataDiskName + ".vhd";
            String newdataDiskVhdUri = storageAccount.primaryEndpoints().blob() + "test" + "/" + newdataDiskVhdName;
            DataDisk newDisk = new DataDisk().withLun(2).withDiskSizeGB(1).withCaching(CachingTypes.READ_ONLY)
                    .withCreateOption(DiskCreateOptionTypes.EMPTY).withName(newdataDiskName)
                    .withVhd(new VirtualHardDisk().withUri(newdataDiskVhdUri));
            
                    vmStorageProfile.dataDisks().add(newDisk);
            virtualMachine.update().withStorageProfile(vmStorageProfile).apply();

            System.out.println("Added a data disk to VM" + virtualMachine.id());
            Utils.print(virtualMachine);

            // ============================================================= 
            // Update - detach data disk

            vmStorageProfile.dataDisks().remove(0);
            virtualMachine.update().withStorageProfile(vmStorageProfile).apply();

            System.out.println("Detached data disk at lun 1 from VM " + virtualMachine.id());

            // ============================================================= 
            // Restart the virtual machine

            System.out.println("Restarting VM: " + virtualMachine.id());

            virtualMachine.manager().virtualMachines().inner().restart(rgName, linuxVMName);

            System.out.println("Restarted VM: " + virtualMachine.id() + "; state = " + virtualMachine.provisioningState());

            // ============================================================= //
            // Stop(powerOff) the virtual machine

            System.out.println("Powering OFF VM: " + virtualMachine.id());

            virtualMachine.manager().virtualMachines().inner().powerOff(rgName, linuxVMName);

            System.out.println("Powered OFF VM: " + virtualMachine.id() + "; state = " + virtualMachine.provisioningState());

            // ============================================================= //
            // Delete VM

            System.out.println("Delete VM: " + virtualMachine.id());
            azure.virtualMachines().deleteByIds(virtualMachine.id());

            System.out.println("Deleted VM: " + virtualMachine.id());
            return true;

        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {

            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().inner().delete(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
        return false;
    }

    public static HashMap<String, String> getActiveDirectorySettings(String armEndpoint) {
        HashMap<String, String> adSettings = new HashMap<String, String>();

        try {

            // create HTTP Client
            HttpClient httpClient = HttpClientBuilder.create().build();

            // Create new getRequest with below mentioned URL
            HttpGet getRequest = new HttpGet(String.format("%s/metadata/endpoints?api-version=1.0", armEndpoint));

            // Add additional header to getRequest which accepts application/xml data
            getRequest.addHeader("accept", "application/xml");

            // Execute request and catch response
            HttpResponse response = httpClient.execute(getRequest);

            // Check for HTTP response code: 200 = success
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }
            String responseStr = EntityUtils.toString(response.getEntity());
            JSONObject responseJson = new JSONObject(responseStr);
            adSettings.put("galleryEndpoint", responseJson.getString("galleryEndpoint"));
            JSONObject authentication = (JSONObject) responseJson.get("authentication");
            String audience = authentication.get("audiences").toString().split("\"")[1];
            adSettings.put("login_endpoint", authentication.getString("loginEndpoint"));
            adSettings.put("audience", audience);
            adSettings.put("graphEndpoint", responseJson.getString("graphEndpoint"));

        } catch (ClientProtocolException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return adSettings;
    }

    /**
     * Main entry point.
     * 
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            // =============================================================
            // Authenticate

            final String armEndpoint = System.getenv("ARM_ENDPOINT");
            final String location = System.getenv("RESOURCE_LOCATION");
            final String client = System.getenv("AZURE_CLIENT_ID");
            final String tenant = System.getenv("AZURE_TENANT_ID");
            final String key = System.getenv("AZURE_CLIENT_SECRET");
            final String subscriptionId = System.getenv("AZURE_SUBSCRIPTION_ID");

            // Get Azure Stack cloud endpoints
            final HashMap<String, String> settings = getActiveDirectorySettings(armEndpoint);

            // Register Azure Stack cloud environment
            AzureEnvironment AZURE_STACK = new AzureEnvironment(new HashMap<String, String>() {
                private static final long serialVersionUID = 1L;

                {
                    put("managementEndpointUrl", settings.get("audience"));
                    put("resourceManagerEndpointUrl", armEndpoint);
                    put("galleryEndpointUrl", settings.get("galleryEndpoint"));
                    put("activeDirectoryEndpointUrl", settings.get("login_endpoint"));
                    put("activeDirectoryResourceId", settings.get("audience"));
                    put("activeDirectoryGraphResourceId", settings.get("graphEndpoint"));
                    put("storageEndpointSuffix", armEndpoint.substring(armEndpoint.indexOf('.')));
                    put("keyVaultDnsSuffix", ".adminvault" + armEndpoint.substring(armEndpoint.indexOf('.')));
                }
            });

            // Authenticate to Azure Stack using Service Principal credentials
            AzureTokenCredentials credentials = new ApplicationTokenCredentials(client, tenant, key, AZURE_STACK)
                    .withDefaultSubscriptionId(subscriptionId);

            Azure azureStack = Azure.configure().withLogLevel(com.microsoft.rest.LogLevel.BASIC)
                    .authenticate(credentials, credentials.defaultSubscriptionId());

            // Run Manage Vm 
            manageVm(azureStack, location);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageVirtualMachine() {

    }
}