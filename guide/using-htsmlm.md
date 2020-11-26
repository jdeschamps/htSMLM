# Using htSMLM

1. [General tips](#general-tips)
2. [Default panels](#default-panels)
3. [Optional panels](#optional-panels)
4. [Activation script](#activation-script)
5. [Acquisitions with htSMLM](#acquisitions-with-htsmlm)

------

## General tips

- To validate what one enters in a text field, either press **enter** or click away from the field.
- All monitoring threads can be turned on/off. During imaging, and depending on your workstation, the updating of graphical components in Java and Micro-Manager can be stalling. This includes the refreshment of the acquisition window. In order to lift the number of actions the main graphical thread has to perform, turn off the htSMLM monitoring. (Except the activation or acquisition if your are using them). 



------

## Default panels

#### Activation laser panel

<p align="center">
  <img height="200" src="img/pulse.png">
</p>
The activation laser panel offers a logarithmic scale with a user-defined maximum value. It corresponds to a single GUI property: **UV pulse duration (main frame)**. While it is intended for a pulse duration in us, it can be used with an absolute laser power or power percentage, albeit with reduced dynamical range.


- Slider: change the laser pulse / power up to the maximum value.
- Top text field (gray background): change the maximum value allowed for the laser pulse / power. The value must be an integer.
- Bottom text field: change the laser pulse / power up to the maximum value. Decimals are accepted.

The laser name and colors are the same as the first laser (Laser 0, see parameters).

#### Laser control panel

<p align="center">
  <img height="200" src="img/lasers.png">
</p>
htSMLM has four main laser control panels. Each laser control panel interacts with two GUI properties: **Laser # power percentage** and "**Laser # enable**" (where # is 0, 1, 2 or 3). Additionally, two modes of power percentage control are available: slider or buttons.


- On/Off button: switches the "**Laser # enable**" property between its ON and OFF state.
- Power percentage: change the "**Laser # power percentage**" value.
  - Slider mode:
    - Slider: changes the GUI property to the set value and display the new value in the text field.
    - Text field: changes the GUI property to the set value and updates the slider. The text field only accepts integer values.
  - Buttons mode:
    - 1%, 20% and 100% buttons:  set the GUI property to the corresponding power percentage.
    - User-defined percentage button: selecting the button sets the GUI property to the displayed percentage.
    - Text field: choose the user-defined percentage. Note that by changing the user-defined percentage, the GUI property is only change if the button is currently selected.

The laser name and the colors are GUI parameters. Additionally, Laser 0 name and color are linked to the activation laser panel.

Note that the laser percentage GUI property can be mapped to an absolute laser power, as long as the slope parameter (properties tab in the EMU configuration wizard) rescales the power to the range 0-100.

#### Filters panel

<p align="center">
  <img height="80" src="img/filters.png">
</p>
The filter panel offer controls for one or two filter wheels. Only one filter can be selected in each row, setting the GUI property "**Filter wheel position**" (or **Filter wheel 2 position**) to the corresponding state.

All texts and colors, as well as the panel title, can be set in the properties tab of the EMU configuration wizard.


#### Focus panel

<p align="center">
  <img height="150" src="img/focus.png">
</p>
The focus panel allows moving and monitoring the position of your focusing device ("**Z stage position**" GUI property). It also includes a button to enable or disable focus stabilization, if this feature is present on your microscope (see examples such as [pgFocus]( https://github.com/ries-lab/RiesPieces/tree/master/Microscopy/Focus-locking ) or the [Ries lab solution]( https://github.com/ries-lab/RiesPieces/tree/master/Microscopy/Focus-locking )).


- Position: set the GUI property to the requested value (up to two decimals). 
- Monitor: toggle the monitoring of the focus position, updating the graph.
- Lock: switch the "**Z stage focus-locking**" GUI property between it's ON and OFF states.
- "^^" and "vv": move the stage up or down by the value indicated in the ">>" text field.
- "^", "v" and ">":  move the stage up or down by the value indicated in the ">" text field.

Two parameters can be set: the number of points shown in the graph and the graph update frequency. Finally, the large and small steps default values are parameters.

#### Controls panel

<p align="center">
  <img height="200" src="img/controls.png">
</p>
The controls panel contain a few toggle buttons that can be used to switch certain devices between two states. This can for instance be in/out elements (lenses, single filters, laser stops) or on/off (bright-field light). Each button corresponds to a "**Two-state device #**" GUI property, and their text can be customized. Finally, unused buttons can be disabled in the parameters. 



------

## Optional panels

#### Additional filters panel

<p align="center">
  <img height="150" src="img/add_filters.png">
</p>

- 

#### QPD tab

<p align="center">
  <img height="250" src="img/qpd.png">
</p>

- 

#### Laser trigger tab

<p align="center">
  <img height="250" src="img/trigger.png">
</p>

- 

#### Powermeter tab

<p align="center">
  <img height="250" src="img/powermeter.png">
</p>

- 

#### iBeamSmart laser tab

<p align="center">
  <img height="230" src="img/ibeam.png">
</p>

- 

------

## Activation script

Principle of the algorithm

<p align="center">
  <img height="280" src="img/activation.png">
</p>

- Sd coeff:
- Feedback:
- Average:
- Get N:
- N text field:
- Activate:
- Run:
- Cut-off field:
- Auto:
- Clear:
- NMS:

Number of points, frequency of updates in parameters

------

## Acquisitions with htSMLM

<p align="center">
  <img height="300" src="img/acq.png">
</p>

- On/Off button

<p align="center">
  <img height="200" src="img/summary.png">
</p>

- On/Off button

#### Acquisition wizard

<p align="center">
  <img height="300" src="img/acq-wizard.png">
</p>

- On/Off button

#### Acquisition types

##### Localization

<p align="center">
  <img height="120" src="img/loc.png">
</p>

- On/Off button

##### BFP or Bright-field or Snap

<p align="center">
  <img height="100" src="img/bfp.png">
</p>

- On/Off button

##### Z-stack

<p align="center">
  <img height="120" src="img/z.png">
</p>

- On/Off button

##### Time

<p align="center">
  <img height="120" src="img/time.png">
</p>

- On/Off button

##### Multislice localization

<p align="center">
  <img height="250" src="img/multislice.png">
</p>

- On/Off button

