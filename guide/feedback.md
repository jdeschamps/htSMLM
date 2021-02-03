# Feedback

The first iterations of htSMLM happened in 2015 and the first real version was finished in 2016. Since then improvements and new features have been added together with patches, for instance to port htSMLM to Micro-Manager 2 .0-gamma. As such, some part of the code are probably a bit messy.

In addition, Micro-Manager is rapidly changing. In the future, the Micro-Manager acquisition engine will be modified and this will without a doubt create some bugs in htSMLM. 

htSMLM acquisitions were last tested with the 20201203 Micro-Manager gamma nightly build.

### Feedback on Github

To provide feedback on potential bugs, please [open a Github issue](https://github.com/jdeschamps/htSMLM/issues) and don't forget to follow the check list beforehand:

- Have you tried the latest htSMLM version with the latest Micro-Manager 2 nightly build?
- Do you have "enable debug logging" in Micro-Manager 2 preferences? (This is important before sharing the CoreLog).
- In the Github issue, state the following:
  - Micro-Manager 2 nightly build version
  - Steps required in order to reproduce the bug
- Finally, please provide the following files:
  - Micro-Manager configuration 
  - EMU configuration (found in the EMU folder)
  - CoreLog corresponding to a session of Micro-Manager where you observed the bug. (found in the CoreLog folder)
