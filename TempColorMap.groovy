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
 
definition(
    name: "InovelliBlueTempMap",
    namespace: "Jamesan",
    author: "James Anderson",
    singleInstance: false,
    description: "Map temperature to a LED strip",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: ""
)   

preferences {
	page(name: "mainPage")
}

def mainPage() {
	dynamicPage(name: "mainPage", title: " ", install: true, uninstall: true) {
		section {
            avg = readValue()
            
			input "thisName", "text", title: "Name this Colormap", submitOnChange: true, required: true
            input "weather_url", "text", title: "Enter the weather.gov url for your local weather", required: true, defaultValue: "https://api.weather.gov/stations/KCVO/observations/latest"
            input "weather_email", "text", title: "E-mail to attach to weather.gov, per thier request, to contact folks who spam the service", required: true, defaultValue: "Jander42@hotmail.com"
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
    //def averageDev = getChildDevice("AQIColorMap_${app.id}")
	//if(!averageDev) averageDev = addChildDevice("hubitat", "AQI Colormap", "AQIColorMap_${app.id}", null, [label: thisName, name: thisName])
    //averageDev.setAirQuality(readValue())   
    runEvery5Minutes(every_five_minutes_handler)
    //runEvery1Minute(every_minute_handler)
    if(outputLights)
    {
        log.info "Initial start"
        def result = ValueToLEDSettings(avg)
        paragraph "Current Temp result is ${result}"
        UpdateLights(result)
    }  
}

def every_five_minutes_handler(evt) { 
    read_temperature()
}


def every_minute_handler(evt) {
	
}

def refresh(){
    //def averageDev = getChildDevice("AQIColorMap_${app.id}")
	def avg = readValue() 
	//averageDev.setAirQuality(avg)
	log.info "Cached Temperature = $avg"
    //def c = AQIToColor(avg)
    //log.info "AQI Color = $c"
    def result = ValueToLEDSettings(avg) 
    UpdateLights(result)
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
        [min: 20, max: 32, name: "Freezing", color: 'white'],
        [min: 32, max: 46, name: "Cold", color: 'blue'],
        [min: 46, max: 60, name: "Chilly", color: 135],
        [min: 60, max: 68, name: "Mild chill", color: 110],
        [min: 68, max: 75, name: "Comfortable", color: 'green'],
        [min: 75, max: 82, name: "Warm", color: 'yellow'],
        [min: 82, max: 89, name: "Too Warm", color: 'orange'],
        [min: 89, max: 102, name: "Hot", color: 'red'],
    ]

@Field static Map color_name_map = [
   "red": 1,
   "red-orange": 4,
   "orange": 12,
   "yellow": 45,
   "chartreuse": 60,
   "green": 85,
   "spring": 100,
   "cyan": 127,
   "azure": 155,
   "blue": 170,
   "purple": 190,
   "violet": 212,
   "magenta": 234,
   "rose": 254,
   "white": 255
] 

def UpdateLights(result){
    //log.debug("Updating LEDs")
    outputLights.each { light -> 
        if(result.belowLightsParam != null && result.belowLightsParam != "")
        {
            log.debug("LED ${result.belowLightsParam} set to 1, ${result.color}, ${MaxBrightness}")
            light.ledEffectOne(result.belowLightsParam, 1, map_color(result.color), MaxBrightness)
        }
        
        if(result.nLevelLight  != null)
        {
            log.debug("** LED ${result.nLevelLight} set to 1, ${result.color}, ${result.finalLightBrightness}")
            light.ledEffectOne("${result.nLevelLight}", 1, map_color(result.color), result.finalLightBrightness)
        }
        
        if(result.aboveLightsParam != null && result.aboveLightsParam != "")
        {
            log.debug("LED ${result.aboveLightsParam} set to 1, ${result.background}, ${MinBrightness}")
            light.ledEffectOne(result.aboveLightsParam, 1, map_color(result.background), MinBrightness) 
        }
    }
}

def getBackgroundForRange(i){
    if( i == 0)
    {
        return 'purple'
    }
    else{
        return temp_range_map[i-1].color
    }
}
  
def ValueToRange(v) 
{  
    //getCaqiColors().each { log.info "$it.value $it.color" }
    def range = temp_range_map
    def result = null
     
    if(!range.any { it -> v >= it.min && v <= it.max}){
        log.info "Could not find a range mapping for value ${v}"
        if( v <= 0) {
            result = range[0]
            result['background'] = getBackgroundForRange(0)    
        }
        else {
            result = range[-1]
            result['background'] = result.color
        }
        
        return result
    }
    
    def i = (range.findIndexOf {it -> v >= it.min && v <= it.max } )
    log.debug "Value ${v} falls in range index ${i}: ${range[i]}"
    if (i < 0) { i = 0 }
    log.debug "Temperature range is ${range[i]}"
    
    result = range[i]
    result['background'] = getBackgroundForRange(i)
    
    return result
}

def map_color(color){
    int result = 0
    if( color_name_map.containsKey(color) ) {
        result = color_name_map[color]
    }
    else{
        result = color
    }
    
    return result
}


def ValueToLEDSettings(v){
    def range = ValueToRange(v)
    //log.info "AQI in ${range.name} range"
    log.debug "Range = ${range}"
    float value_in_range = v - range.min
    float abs_range = range.max - range.min
    if (abs_range == 0) {
        return [lightsParam: '1', nLights: 1, finalLightBrightness: 0, color: 0] 
    }
    
    float scaled_value = value_in_range / abs_range
    log.debug "Scaled value ${scaled_value}"
    
    //Total number of lights that should be illuminated.  Whole numbers set the entire LED, fractions set a level LED above.
    float nLights = scaled_value * (numLEDs)
    if(nLights <= 0)
        nLights = 0
    else if (nLights >= numLEDs)
        nLights = numLEDs
    
    def nBelowLightsParam = ""
    def nAboveLightsParam = ""
    def LevelLightFraction = nLights - (nLights as Integer)
    log.debug "nLights: ${nLights} Fraction: ${LevelLightFraction}"
    def NoLevelLight = LevelLightFraction % 1 == 0
    
    def nUnderLights = nLights as Integer
    if(NoLevelLight){
        nUnderLights -= LevelLightFraction == 1 ? 1 : 0
    }
    nUnderLights = nUnderLights as Integer
    if(nUnderLights > 0)
        nBelowLightsParam = (0..nUnderLights-1).collect{ "${it + 1}" }.join()
    
    def nLevelLight = NoLevelLight ? null : nUnderLights + 1
    
    if(nUnderLights + 1 < numLEDs)
    {
        if(NoLevelLight)
            nAboveLightsParam = (nUnderLights..numLEDs-1).collect{ "${it+1}" }.join()
        else if(nLevelLight  < numLEDs)
            nAboveLightsParam = (nLevelLight..numLEDs-1).collect{ "${it+1}" }.join()
    } 
             
    log.debug "Lights Param for nLights ${nLights}: below: ${nBelowLightsParam} above: ${nAboveLightsParam} nUnderLights: ${nUnderLights} nLevelLight ${nLevelLight} nUnderLights ${nUnderLights}"
    int finalLightBrightness = ((nLights % 1) * MaxBrightness) as Integer
    
    result = [belowLightsParam: nBelowLightsParam, nLevelLight: nLevelLight, aboveLightsParam: nAboveLightsParam,  finalLightBrightness: finalLightBrightness, color: range.color, background: range.background] 
    log.debug "Resulting settings: ${result}"
    return result
}