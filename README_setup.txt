README file for Setup

These programs (DKVRunServers.java,DKVServer.java, SNApp.java, SNAppSetup.java) must be executed on an Ubuntu system with zookeeper installed in standalone mode. After installing zookeeper, extract the zookeeper-<version>.jar file and copy the org folder from it into the folder containing these programs (the current directory).
Start zookeeper before executing the programs.

To set up the Distributed Key-Value Store:
This does not require any explicit setup. Compile and run the DKVRunServers.java file from the terminal. The program prints output to the standard output.

To set up the Social Network Application:
Compile and run the SNAppSetup.java file the first time this application is started. This need not be done again. Compile and run the SNApp.java file from the terminal. The program reads input from the standard input and prints output to the standard output.
