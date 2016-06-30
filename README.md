---
services: virtual-machines
platforms: java
author: selvasingh
---

# Azure Virtual Machines Management Samples - Java

These samples (for 1.0.0-beta2) demonstrate how to perform common tasks with Microsoft Azure Virtual Machines. Code examples show how to do the following:

- Create a virtual machine
- Start a virtual machine
- Stop a virtual machine
- Restart a virtual machine
- Update a virtual machine
	- Expand a drive
	- Tag a virtual machine
	- Attach data disks
	- Detach data disks
- List virtual machines
- Delete a virtual machine.

## Running this sample

To run this sample first set the environment variable **AZURE_AUTH_LOCATION** to the full path for an auth file. See [how to create an auth file](https://github.com/Azure/azure-sdk-for-java/blob/master/AUTH.md).

Next, clone (or download) the sample and compile it.

    git clone https://github.com/Azure-Samples/compute-java-manage-vm.git

    cd compute-java-manage-vm

    mvn clean compile exec:java

## More information

Here are some helpful links:

- [Azure Java Development Center] (http://azure.com/java)
- [Azure Virtual Machines documentation](https://azure.microsoft.com/services/virtual-machines/)
- [Learning Path for Virtual Machines](https://azure.microsoft.com/documentation/learning-paths/virtual-machines/)

If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212).

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
