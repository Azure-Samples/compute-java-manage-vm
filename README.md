---
services: compute
platforms: java
author: selvasingh
---

#Getting Started with Compute - Manage Virtual Machines - in Java #

Compute Manage Virtual Machine Sample (for 1.0.0-beta2) - demonstrates how to perform common tasks using the Microsoft Azure Compute service.

- Create a virtual machine
- Start a virtual machine
- Stop a virtual machine
- Restart a virtual machine
- Update a virtual machine
	- Expand the OS drive
	- Tag a virtual machine (there are many possible variations here)
	- Attach data disks
	- Detach data disks
- List virtual machines
- Delete a virtual machine.

## Running this Sample ##

To run this sample:

Set the environment variable `AZURE_AUTH_LOCATION` with the location of the auth file. See [how to create an auth file](https://github.com/Azure/azure-sdk-for-java/blob/master/AUTH.md).

    git clone https://github.com/Azure-Samples/compute-java-manage-vm.git

    cd compute-java-manage-vm

    mvn clean compile exec:java

## More information ##

[http://azure.com/java] (http://azure.com/java)

[Virtual Machines](https://azure.microsoft.com/en-us/services/virtual-machines/)

[Virtual Machines - Learning Path](https://azure.microsoft.com/en-us/documentation/learning-paths/virtual-machines/)

If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212)

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
