# EMU configuration

Start Micro-Manager and select **Plugins->Interface->EMU**. The EMU configuration wizard is launched automatically on the first start. It can be also be started from the menu in the EMU window, under "Configuration" and "Modify configuration". To get familiar with EMU, please consult the [EMU guide](https://jdeschamps.github.io/EMU-guide/). In particular, the [quick introduction](https://jdeschamps.github.io/EMU-guide/quickintro.html) gives an easy introduction to the configuration system.

In this section, we describe all the properties and parameters of htSMLM. Note that all descriptions are available in the interface during configuration by selecting the "**Help**" button. Have a look at the [htSMLM overview](overview.md) before starting.

Refer to [using htSMLM](using-htsmlm.md) to get details on what each button of the interface does.

1. [Plugin settings](#plugin-settings)
2. [Properties](#properties)
3. [Parameters](#parameters)
4. [Global settings](#global-settings)
5. [FAQ](#faq)

#### Plugin Settings

Selecting or unselecting certain plugin settings will modify the number of properties and parameters available in the other sections.

- **Additional FW tab**: Check to use the additional filters tab in the plugin. When selected, a new tab containing two sliders with four positions each. All slider position names and colors can be customized.
- **Additional FW tab title**: Title of the additional filters tab, if the "Additional FW tab" setting is selected.
- **Powermeter tab**: Check to use the powermeter tab in the plugin. Causes a new tab to appear, allowing users to read a Micro-manager property and linearly transform it to represent a power in mW. Users can choose a number of names for the channels (wavelengths), as well as the slopes and offsets for the linear transformation.  
- **QPD tab**: Check to use the QPD tab in the plugin. The QPD tab is aimed at plotting three signals (X, Y, Z) from a quadrant photo-diode in time. The tab contains one graph (X vs Y) and one progress bar (Z). This is part of a focus-locking system. 
- **Single FW panel**: Check to use a single filter wheel panel. When unchecked, the filter wheel panel contains two rows of filters, with customizable names and colors, corresponding to two different filter wheels.
- **Trigger tab**: Check to use the trigger tab in the plugin. The trigger tab can be used with MicroFPGA, an FPGA board aimed, among other things, at triggering lasers in a flexible manner.  MicroFPGA can be found on Github (jdeschamps/MicroFPGA).
- **iBeamSmart #1**: Check to use the iBeamSmart #1 tab. Originally aimed at controlling an iBeamSmart laser from Toptica, the Toptica specific aspects can be disabled to correspond to a simple laser. 
- **iBeamSmart #1 name**: Name of iBeamSmart #1 tab, if the "iBeamSmart #1" setting is selected. The corresponding properties and parameters will bear the same name.
- **iBeamSmart #2**: Check to use the iBeamSmart #2 tab. Originally aimed at controlling an iBeamSmart laser from Toptica, the Toptica specific aspects can be disabled to correspond to a simple laser. 
- **iBeamSmart #2 name**: Name of iBeamSmart #2 tab, if the "iBeamSmart #2" setting is selected. The corresponding properties and parameters will bear the same name.

#### Properties

In EMU, properties can be mapped to Micro-Manager device properties. In the Properties tab, this is done for a specific property by first selecting a device in the second column, then the relevant device property in the third column. Unmapped properties are simply for not doing anything. Note that the activation script requires certain properties to be mapped to function.

In addition to being mapped to a device property, [certain GUI properties](https://jdeschamps.github.io/EMU-guide/userguide.html) require states to be specified. That can be the case for GUI properties that have a finite number of states or properties that perform a linear scaling of the device property value.

Finally, some GUI properties are only available when the corresponding Plugin setting is selected (or unselected).

##### Properties that are always present:

- **Camera exposure**: Camera exposure in ms.
- **Filter wheel position**: Filter wheel position. Choose a device property that corresponds to an element with a finite number of states (e.g. a filter wheel). The filter wheel property has 6 positions. For each position, indicate in the "Filter wheel position state #" the corresponding device property value. In order to determine the value, use the Micro-Manager device property browser. All states must be set, but multiple states can have the same value. Each state name and color can be configured in the Parameters tab.
- **Laser # enable**: Laser On/Off property. Lasers are numbered from left to right in the main interface. Both on and off values must be set. Consult the Micro-Manager device property browser to determine them  (e.g. "1" and "0" or "On" and Off"). The On/Off button can be disabled in the Parameters tab.
- **Laser # power percentage**: Power percentage of the laser. This property allows rescaling the device property value. If the laser has only a power set point (mW) property instead of a percentage property, then use a slope value equal to (maximum power / 100) to turn to scale the device property to a power percentage. 
- **Two-state device #**: Map to this GUI property a device property with two positions (e.g. On/Off or In/Out). Consult the Micro-Manager device property browser to determine them  (e.g. "1" and "0" or "On" and Off"). The two-state device appears in the interface as a single toggle button. The name of the button can be set in the Parameters tab. 
- **UV pulse duration (activation)**: Pulse length, power or power percentage property of the activation laser. This property is required for the Activation script. Note that it should be mapped to the same device property as "UV pulse duration (main frame)".
- **UV pulse duration (main frame)**: Pulse length, power or power percentage property of the activation laser. This property is required for the Activation script. Note that it should be mapped to the same device property as "UV pulse duration (activation)".
- **Z stage focus-locking**: Property used to toggle focus stabilization.
- **Z stage position**: Position of the stage, used to move the stage and monitor its position.

##### Properties requiring a Plugin Setting to be selected:

###### Additional FW tab selected

- **Slider # position**: Slider position. Choose a device property that corresponds to an element with a finite number of states (e.g. a filter wheel). Each slider property has 4 positions. For each position, indicate in the "Slider # position state #" the corresponding device property value. In order to determine the value, use the Micro-Manager device property browser. All states must be set, but multiple states can have the same value. Each state name and color can be configured in the Parameters tab.

###### Powermeter tab selected

- **Laser powermeter**: Laser power signal value. The device property should be numerical. Slope and offset to convert it to mW can be set in the Parameters tab.

###### QPD tab selected

- **QPD #**: # signal of the quadrant photo-diode (QPD). Can alternatively be used to plot any device property (X vs Y and Z as a progress bar).

###### Trigger tab selected

- **Laser # trigger mode**: MicroFPGA (Github: jdeschamps/MicroFPGA) property dictating the behaviour of the laser trigger.
- **Laser # pulse duration**: MicroFPGA (Github: jdeschamps/MicroFPGA) duration of the laser pulses.
- **Laser # trigger sequence**: MicroFPGA (Github: jdeschamps/MicroFPGA) trigger sequence.

###### iBeamSmart #1/2 selected

The "iBeamSmart #1/2 name" plugin setting influences the name of the following properties.

- **[Name] enable fine**: iBeamSmart specific property. Leave unmapped to ignore. The corresponding panel can be disabled in the Parameters tab. Consult the Micro-Manager device property browser to determine them  (e.g. "1" and "0" or "On" and Off").
- **[Name] ext trigger**: iBeamSmart specific property. Leave unmapped to ignore. The corresponding panel can be disabled in the Parameters tab. Consult the Micro-Manager device property browser to determine them  (e.g. "1" and "0" or "On" and Off").
- **[Name] fine a (%)**: iBeamSmart specific property. Leave unmapped to ignore. The corresponding panel can be disabled in the Parameters tab.
- **[Name] fine b (%)**: iBeamSmart specific property. Leave unmapped to ignore. The corresponding panel can be disabled in the Parameters tab.
- **[Name] laser power**: Laser power in mW. This GUI property can also be used with a power percentage device property by setting the maximum power in the Properties tab to 100; however, the "mW" mention will remain.
- **[Name] operation**: Laser On/Off property. Lasers are numbered from left to right in the main interface. Both on and off values must be set. Consult the Micro-Manager device property browser to determine them  (e.g. "1" and "0" or "On" and Off").

#### Parameters

##### Parameters that are always present:

- **Acquisitions - BFP lens**: Choose among the mapped GUI properties that have two states. Originally aimed for a Bertrand lens, this parameter is used by a specific type of acquisition (BFP). Before a BFP acquisition, the selected GUI property is set to its on state, a single frame is recorded, and the property is finally set to its off state. 

- **Acquisitions - Bright field**: Choose among the mapped GUI properties that have two states. Originally aimed for a bright-field LED array, this parameter is used by a specific type of acquisition (Bright-field). Before a Bright-field acquisition, the selected GUI property is set to its on state, a single frame is recorded, and the property is finally set to its off state. 

- **Acquisitions - Bright field**: Select the "Z stage focus locking" property if it has been mapped in the Properties tab. This allows the acquisition controller to turn on or off the focus stabilization depending on the designed experiments.

- **Activation - Default feedback**: Default value of the parameter controlling the speed at which the pulse length (or power) of the activation laser is increased when the Activation script is running. A higher value leads to a faster increase.

- **Activation - Default sd coeff**: Default value of the parameter controlling the auto cut-off level when the Activation script is running. A high value leads to a high cut-off level, which in turns decreases the number of molecules detected.

- **Activation - Idle time (ms)**: Idle time (ms) between each iteration of the Activation script. 

- **Activation - Number of points**: Number of points on the x axis of the Activation script graph.

- **Controls - Enable two-state device #**: Select to enable the corresponding toggle button in the control panel of the GUI. When unselected, the button is greyed out.

- **Controls - Title**: Title appearing at the top of the control panel.

- **Controls - Two-state device # name**: Text appearing on the corresponding button.

- **Filters - Filter colors**: Colors of the filter names displayed on the GUI. The entry should be written as "color1,color2,color3,color4,color5,color6". The names should be separated by commas. The maximum number of filter colors is 6, beyond that the colors will be ignored. The available colors are: pink, violet, dark violet, dark blue, blue, pastel blue, dark green, green, yellow, orange, brown, dark red, red, black, gray, white.

- **Filters - Filter names**: Filter names displayed on the GUI. The entry should be written as "name1,name2,name3,name4,name5,name6". The names should be separated by commas. The maximum number of filter names is 6, beyond that the names will be ignored.

- **Filters - Panel title**: Title appearing at the top of the filter panel.

- **Focus - Idle time (ms)**: Idle time (ms) between two updates of the stage position.

- **Focus - Large step**: Default value for the large z stage step, shown in the GUI.

- **Focus -  Number of points**: Number of stage positions displayed in the chart (x axis).

- **Focus -  Small step**: Default value for the small z stage step, shown in the GUI.

- **Laser # - Color**: Color of the laser name as shown in the GUI.

- **Laser 0 - Default max pulse**: Default maximum value for the activation laser pulse length (or power). This default value appears in the grey box at the top-left corner of the GUI. 

- **Laser # - Name**: Laser name displayed on top of the laser control panel in the GUI.

- **Laser # - Use on/off**: Enable/disable the On/Off button.

- **Laser # - Use slider**: If selected, the laser power percentage control appears as a slider. If disabled the slider is replaced by three pre-defined buttons (1%, 20% and 100%), as well as a user-defined button whose corresponding percentage can be set in a text-field.

  

##### Parameters requiring a Plugin Setting to be selected:

###### Additional FW tab selected

- **Additional filters - Slider # colors**: Colors of the filter names displayed on the GUI. The entry should be written as "color1,color2,color3,grey,grey,grey". The names should be separated by commas. The maximum number of filters color is 4, beyond that the colors will be ignored. The available colors are: pink, violet, dark violet, dark blue, blue, pastel blue, dark green, green, yellow, orange, brown, dark red, red, black, gray, white
- **Additional filters - Slider # names**: Filter names displayed on the GUI. The entry should be written as "name1,name2,name3,None,None,None". The names should be separated by a comma. The maximum number of filters name is 4, beyond that the names will be ignored. If the commas are not present, then the entry will be set as the name of the first filter.
- **Additional filters - Slider # title**: Title of the # set of additional filters.

###### Powermeter tab selected

- **Powermeter - idle time (ms)**: Idle time (ms) between two updates of the powermeter value.
- **Powermeter - number of points**: Number of laser power measurements displayed in the chart (x axis).
- **Powermeter - offsets**: Comma-separated offsets to convert the measurements to mW. Make sure to input as many slopes as wavelengths, otherwise a default value of 1 will be applied.
- **Powermeter - slopes**: Comma-separated slopes to convert the measurements to Watts. Make sure to input as many slopes as there are wavelengths, otherwise, a default value of 1 will be applied.
- **Powermeter - wavelengths**: Comma-separated wavelengths of the different lasers measured by the powermeter property.

###### QPD tab selected

- **QPD - Idle time (ms)**: Idle time (ms) between two updates of the QPD signals value.
- **QPD - XY max**: Maximum X and Y signals value in the graph.
- **QPD - Z max**: Maximum Z value in the progress bar.

###### Trigger tab selected

- **Laser # trigger - Color**: Color of the laser name as displayed in the laser trigger panel.
- **Laser # trigger - Name**: Name of the laser as displayed on top of the laser trigger panel.

###### iBeamSmart#1/2 selected

The "iBeamSmart #1/2 name" plugin setting influences the name of the following parameters.

- **[Name] - external trigger available**: Unselect to hide the external trigger (iBeamSmart specific property) panel.
- **[Name] - fine available**: Unselect to hide the fine (iBeamSmart specific property) panel.
- **[Name] - max power**: Maximum laser power, sets the maximum of the slider in the GUI.

#### Global Settings

- **Enable unallocated warnings**: When enabled, a message will be prompted to the user if some UI properties are not allocated. Disable to prevent the message from being shown.



#### FAQ

##### My lasers do not have a power percentage device property

Not a problem, the GUI property corresponding to the power percentage has two parameters: slope and offset. You can set the slope to max_power/100, then setting 50% power will set your laser power to max_power/2. You effectively have now a power percentage!

##### I only have four filters in my filter wheel

Simply set the remaining two filters (in the main filter wheel of the interface) to the same value, for instance a position in between that blocks light, or the same value than one of the other filters. Choose gray or black color and an appropriate name.

##### I have more than four lasers and none of them are iBeamSmart lasers

Use the iBeamSmart laser options nonetheless and disable the iBeamSmart specific features in the parameters.

##### I don't understand what the controls panel do

These are simply several buttons that can toggle between two states. You can map to each button any device property of your choice, as long as you intend to switch them from one state to the other.

##### I don't have a focus stabilization system

In such case, the "Lock" button in the focus panel will just be useless, ignore it. In the same way, the QPD panel is intended to monitor the sensor in charge of the focus stabilization, you can just deactivate it. Note that the automated acquisitions were designed while having the focus stabilization in mind. You can still use them, but not as effectively.

##### Why can we deactivate the on/off button for the lasers

Some lasers do not have on/off properties and are simply off when at 0 power.



