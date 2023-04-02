# htSMLM quick test configuration

The files in this folder allow to quickly test htSMLM with Micro-Manager. Here is where to place them:
1. **config.uicfg**: Create a folder named "EMU" in your Micro-Manager installation folder and copy this EMU Configuration in there. This configuration tells EMU to load htSMLM with specific parameters.
2. **MMStartup.bsh**: Place this script in your Micro-Manager installation folder. This script loads EMU upon start-up.
3. Follow the [installation guidelines](https://github.com/jdeschamps/htSMLM/blob/main/guide/installation.md) to set-up htSMLM. You should end up with htSMLM.jar placed in the EMU folder of your Micro-Manager installation.
4. Use **Micro-Manager-2.0.0.cfg** as your Micro-Manager configuration. This should start Micro-Manager with fake devices.

Now you should be able to interact with htSMLM and explore how clicking on certain element change properties in the device property browser. Try to understand the configuration wizard, the activation script and the automated acquisition feature.

Don't forget to check the [guide](https://github.com/jdeschamps/htSMLM/tree/main/guide) and to use the **Help** button in the configuration wizard.
