# Installation using a compiled version

1. Download the latest [release](https://github.com/jdeschamps/htSMLM/releases).
2. Place it in a folder called "EMU" in your Micro-Manager installation folder.
3. Start Micro-Manager and select **Plugins->Interface->EMU**.

# Installation from source

1. Make sure you have the most recent Micro-Manager 2 installation (nightly build), to ensure that you have the correct EMU version.

2. If not present, install git (for windows users: [git bash](https://gitforwindows.org/)).

3. Using the console, go to the folder you wish to install htSMLM in and type (omitting the $):

   ```bash
   $ git clone https://github.com/jdeschamps/htSMLM.git
   ```

4. Download and install [Maven](https://maven.apache.org/install.html).

5. Finally, compile htSMLM using Maven.

   ```bash
   $ cd htsmlm
   $ ./compileAndDeploy.sh "C:\Path\to\MicroManager2gamma"
   ```

   > **Note**: the compilation script has been written for Windows. For unix-like systems, replace "\\" by "/" in the path variables.

6. Start Micro-Manager and select **Plugins->Interface->EMU**.
