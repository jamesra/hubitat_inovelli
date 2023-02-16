The inovelli Blue has a cool feature where each individual LED can be controlled which allows fairly fine-grained color maps on the switch. 

This repository constains a set of Hubitat apps and libraries that read inputs and map them to the color bar on an Inovelli Blue Switch.  Currently I support temperature and Air Quality, but it is easy to add more.

Installation
============

Step 1: Import both libraries into Hubitat as Libraries

 * [colormap.inovelli.blue.groovy](colormap.inovelli.blue.groovy)
 * [colormap.shared.groovy](colormap.shared.groovy)

Step 2: Import desired application into Hubitat as Applications

 * [InovelliBlueTemperatureMapper.groovy](InovelliBlueTemperatureMapper.groovy)
 * [InovelliBlueAQIMapper.groovy](InovelliBlueAQIMapper.groovy)

Step 3: For AQI you'll need to install a [Purple Air Sensor driver](https://github.com/pfmiller0/Hubitat/blob/main/PurpleAir%20AQI%20Virtual%20Sensor.groovy):

 * [virtual Purple Air Sensor](https://raw.githubusercontent.com/pfmiller0/Hubitat/main/PurpleAir%20AQI%20Virtual%20Sensor.groovy)

Setup
-----

*Temperature*

 When configuring the app you need to identify the nearest weather station.  This can be found by entering your location at [https://forecast.weather.gov/stations.php](https://forecast.weather.gov/stations.php).  The most recent time I checked searching by ZIP was broken, so I suggest sorting by State and finding your nearest station.  (My apologies to non-American users.  It should be possible to modify the code to your local weather service format.)

 Alternatively, the code could be modified by using an input sensor using the AQI temperature mapper as a guide if needed.

*AQI*

Configure the virtual sensor For AQI identify [using the nearest Purple Air Sensor ID](https://map.purpleair.com/).  If you select a station, then select "Get this Widget" the station ID number is currently in several locations after the "PurpleAirWidget_" string.  (There is probably a better way to find it I haven't identified.)  Once the virtual sensor is configured select that sensor as the input to the AQI color mapper.

In the future I may add a feature to dim the light brightness after sunset or between set hours or when the light is turned off. 

I've also debated adding barometer and indoor CO2 maps, but I don't own enough Blue switches. 

How colors are mapped
---------------------

Each app defines a color map that is passed to the libraries:

```
@Field static def AQI_range_map = [
        default_background: 'white',
        colormap: [
        [min: 0, max: 50, name: "Good", color: 'green', no_background: true],
        [min: 50, max: 100, name: "Moderate", color: 'yellow'],
        [min: 100, max: 150, name: "Unhealthy for Sensitive Groups", color: 'orange'],
        [min: 150, max: 200, name: "Unhealthy", color: 'red'],
        [min: 200, max: 300, name: "Very Unhealthy", color: 'purple'],
        [min: 300, max: 400, name: "Hazardous", color: 'magenta'],
    ]
] 
```

Each entry in the map is a min/max range of values that is assigned a specific color.  The LED display is painted by lighting the LED's from the bottom to top with the range color according to where the value falls within the range.  The background color is the range below the current range.  If "no_background" is set to true, the background LEDs will remain off.  For example, the "Green" AQI range is 0 to 50.  The "Yellow" AQI range is 50 to 100.  If the current AQI reading is 75, the bottom half of the display will be painted yellow.  The top half will be painted green, as that is the range below yellow.

The visual effect is that the light strip "fills up" as AQI worsens (gets higher).

Temperature works the same way.  For the mid temperature each 1F change maps directly to a single LED change.  For the cold and warmer extremes each 2F change maps to a single LED.

If you do not like the default choices of color or want to fine tune the ranges the app code can be editted to adjust the color map.