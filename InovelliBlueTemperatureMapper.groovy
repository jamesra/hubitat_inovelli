/**
 * ==========================  Inovelli Blue Level Indicator ==========================
 *
 *  DESCRIPTION:
 *  Maps a single value to a colormap and sets a number of LEDs on the indicator bar
 *  
 
 *  TO INSTALL:
 *  Paste this code into a user app
 *
 *  Copyright 2021 James Anderson
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * =======================================================================================
 *
 *  Last modified: Feb-05-23
 * 
 *  Changelog:
 * 
 *  v1.0 - Initial Public Release
 *
 */ 

import groovy.transform.Field
#include colormap.shared
#include colormap.inovelli.blue
 
definition(
    name: "Inovelli Blue Temperature Mapper",
    namespace: "Jamesan",
    author: "James Anderson",
    singleInstance: false,
    description: "Map temperature to a LED strip",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: ""
)   

@Field static int numLEDs = 7

//18 - 32
//32 - 46,
//46 - 60,
//60 - 67,
//67 - 74
//74 - 81
//81 - 88,
//88 - 102
  
// From https://www.airnow.gov/aqi/aqi-basics/
@Field static def temp_range_map = [
        default_background: 'purple',
        colormap: [
        [min: 20, max: 32, name: "Freezing", color: 'white', no_background: true],
        [min: 32, max: 46, name: "Cold", color: 'blue'],
        [min: 46, max: 60, name: "Chilly", color: 135],
        [min: 60, max: 68, name: "Mild chill", color: 110],
        [min: 68, max: 75, name: "Comfortable", color: 'green'],
        [min: 75, max: 82, name: "Warm", color: 'yellow'],
        [min: 82, max: 89, name: "Too Warm", color: 'orange'],
        [min: 89, max: 102, name: "Hot", color: 'red'],
    ]
] 

preferences {
	page(name: "mainPage")
}

def mainPage() {
	dynamicPage(name: "mainPage", title: " ", install: true, uninstall: true) {
		section {
            avg = readValue()
            
			input "thisName", "text", title: "Name this Colormap", submitOnChange: true, required: true
            input "weather_url", "text", title: "Enter the weather.gov url for your local weather", required: true, defaultValue: "https://api.weather.gov/stations/KCVO/observations/latest"
            paragraph "Locate your local weather station at https://forecast.weather.gov/stations.php"
            input "weather_email", "text", title: "E-mail to attach to weather.gov, per thier request, to contact folks who spam the service", required: true, defaultValue: ""
            input "MaxBrightness", "number", title: "Maximum brightness of LED (0 - 100)", required: true, defaultValue: 66
            input "MinBrightness", "number", title: "Minimum brightness of LED (0 - 100)", required: true, defaultValue: 15
			if(thisName) app.updateLabel("$thisName")
			 if(weather_url && weather_email) {
                read_temperature()
                paragraph "Current outside temperature is ${currentTemperature}"
                //paragraph "Current AQI color is ${ValueToLEDSettings(avg)}"
            }
            input "outputLights", "device.InovelliDimmer2-in-1BlueSeriesVZM31-SN", title: "Select Output", submitOnChange: true, required: true, multiple: true  
        } 
	}
}


def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() { 
    read_temperature() 
    runEvery5Minutes(every_five_minutes_handler)
    //runEvery1Minute(every_minute_handler)
    if(outputLights)
    {
        log.info "Initial start"
        def result = ValueToRange(avg, temp_range_map)
        paragraph "Current Temp result is ${result}"
        UpdateLights(result)
    }  
}

def every_five_minutes_handler(evt) { 
    read_temperature()
}
 

def refresh(){
    //def averageDev = getChildDevice("AQIColorMap_${app.id}")
	def avg = readValue() 
    def range = ValueToRange(avg, temp_range_map)
    def result = GetLEDSettings(avg, range, MinBrightness, MaxBrightness)
	//averageDev.setAirQuality(avg)
	log.info "Cached Temperature = $avg"
    //def c = AQIToColor(avg)
    //log.info "AQI Color = $c"
    //def result = ValueToRange(avg, temp_range_map) 
    UpdateLights(result, outputLights, MinBrightness, MaxBrightness)
}

def asyncReadTemperatureCallback(response, data) {
    log.debug "Request was successful, $response.status"
    def json = response.json
    celsius = json.properties.temperature.value
    currentTemperature = celsiusToFahrenheit(celsius)
    log.debug "Current Outside Temperature is: ${currentTemperature}F"
    
    refresh()
}

def read_temperature(){
    log.debug("Reading temperature from ${weather_url}")
    headers = ['User-Agent': "(${weather_email})"]
    params = [headers : ['User-Agent': "(${weather_email})"],
              uri : weather_url,
              requestContentType: "application/json",
              contentType: "application/json"
             ]
    asynchttpGet("asyncReadTemperatureCallback", params)
    //results = httpGet params, response -> log.debug "Request was successful, ${response.status}"  
    //log.debug "${results}"
}


@Field static float currentTemperature = 72
@Field static Random random = new Random()

def readValue() { 
    return currentTemperature
    //return 100
    //int value = random.nextInt(78) + 17
    //log.debug("Random temp: ${value}")
    //return value
}



