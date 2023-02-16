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

For AQI identify your nearest Purple Air Sensor ID and configure the virtual sensor.  Then select that sensor as the input to the AQI color mapper.

In the future I may add a feature to dim the light brightness after sunset or between set hours or when the light is turned off.