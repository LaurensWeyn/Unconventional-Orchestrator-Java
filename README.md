# Unconventional Orchestrator-Java side
'Everything you need to play music on things never designed to play music'

## Crude build instructions
- Install Maven and JDK 1.8
- In the root of this project's source directory, run 'mvn clean compile assembly:single'
- Once the command finishes, a runnable jar file will be generated in target/. See the running instructions below for how to start this.

## Crude usage instructions
The first step to using Unconventional Orchestrator is to define the hardware it will be controlling.

mappings.csv and instruments.csv control this hardware<->software configuration.
### instruments.csv
This file contains lines in the format:

type:CODE,comPort,numChannels,numDrums

- the type is either 'cnc' (for G-code accepting devices) or 'uic' ('universal instrument controller' protocol, used for floppy drives and other devices).
- the code is a single, unique uppercase letter used to refer to this device and its channels. Anything from A to Z is fine.
- the comPort should match the name of the COM port connected to the Arduino/CNC machine. Check the device manager for what this should be.
- numChannels is how many note playing channels this device has. 3 for most CNC machines, and the number of floppies/stepper motors for UIC.
- numDrums is the number of percussion channels handled by this device. Unlike the above, drum channels cannot play specific tones, and only play on a percussion event. For cnc, this is always 0.

### mappings.csv 
This file controls the groups you can chart instruments to. This is useful to give meaning to the channel numbers of UIC devices, or classify instruments controlled by UICs based on their performance for different kinds of notes.

Note that channel groups can overlap without any problems.

This file contains lines in the format:

mappingName:Ax,By,Cz,.....

- The mappingName is the name for this set of mappings shown in the UI. Changing this later will break all previous song configurations.
- What follows is a comma separated list of instrument references.
- For instrument references, the first character is the CODE of the instrument (see instruments.csv above)
- What follows the instrument code is the channel number to use from that instrument, with 1 being the first channel (X axis for CNC machines, or the first stepper/floppy drive on UNCs).

### Starting the program

Make sure the 'songs' and 'config' folders exist before starting the program, and the configuration files above are complete.
 
Move the generated jar file from Target to the root of the project, and double click it (or run 'java -jar'). Alternatively, start the program with the working directory being the project root.

If your configuration files are correct (and your defined devices are plugged in), the program should start without errors. Otherwise, an error popup will show and the instruments won't work, but the editor should still be functional.

Press the 'Init' button to home any attached CNC machines.

### Mapping songs



Songs should go into the 'songs' directory of the root of the project. These should be MIDI files ending in '.mid'.

Start the Unconventional Orchestrator and select the song file at the top-left, and click 'load song'. You should now see a piano roll visualisation of the song loaded in the middle of the screen. If not, ensure 'Original' is selected in the image settings pane to the right.

Next, note the colors of the important instruments (or channels if you know them, or checked them with a MIDI editor). For each important channel: select it, click the 'mapping type' dropdown and select the mapping defined in 'mappings.csv' (or the reversed version, marked '[R]'). Click 'Save' to confirm.

When you are done with the channels and want to see the result, click 'Reload' on the Image pane on the right to update the 'Manipulated' and 'Final' visualisations.

Press 'Play' at the top left to playback what you have made (so far) on the instruments. Press Stop to stop playback, and Init to reset things after a song has finished playing to completion.

Once you are happy with your configuration, give it a name in the 'Config' text box, and click 'save config' to save it. You can also restore configurations by selecting it from the dropdown and clicking 'load config'. Note that configurations are specific to song files, and the list is updated on clicking 'load song'.


## More details on channel settings
TODO: document this

## Drum mappings
TODO: document this

## Advanced MIDI manipulation
TODO: document this
TODO: make sure this even still works
