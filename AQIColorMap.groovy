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
    name: "InovelliBlueColorMap",
    namespace: "Jamesan",
    author: "James Anderson",
    singleInstance: false,
    description: "Map device values to a LED strip",
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
            avg = averageAQI()
            
			input "thisName", "text", title: "Name this Colormap", submitOnChange: true, required: true
			if(thisName) app.updateLabel("$thisName")
			input "inputSensor", "capability.sensor", title: "Select AQI Input", submitOnChange: true, required: true, multiple: false
            if(inputSensor) {
                paragraph "Current average AQI is ${averageAQI()}"
                paragraph "Current AQI color is ${AQIToLedSettings(avg)}"
            }
            input "outputLights", "device.InovelliDimmer2-in-1BlueSeriesVZM31-SN", title: "Select Output", submitOnChange: true, required: true, multiple: true 
		    if(outputLights)
            {
                log.info "Initial start"
                def result = AQIToLedSettings(avg)
                paragraph "Current AQI result is ${result}"
                UpdateLights(result)
            }
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
    //def averageDev = getChildDevice("AQIColorMap_${app.id}")
	//if(!averageDev) averageDev = addChildDevice("hubitat", "AQI Colormap", "AQIColorMap_${app.id}", null, [label: thisName, name: thisName])
    //averageDev.setAirQuality(averageAQI())  
    if(inputSensor) {
        subscribe(inputSensor, "aqi", handler)
        runEvery5Minutes(handler)
    }
}


@Field int MaxBrightness = 66
@Field int MinBrightness = 10

@Field static Random random = new Random()

def averageAQI() {
    //return 0
    //return 28
    //int value = random.nextInt(5) * 10
    //log.debug("Random AQI ${value}")
    //return value
    return inputSensor.currentValue("aqi")
    /*
	def total = 0.0
	def n = inputSensors.size()
    if(n <= 0) return -1
	inputSensors.each {total += it.airQuality}
	return (total / n).toDouble().round(1)
*/
}

@Field static int numLEDs = 7
  
// From https://www.airnow.gov/aqi/aqi-basics/
@Field static def getCaqiRange = [
        [min: 0, max: 50, name: "Good", color: 'green', show_background: false],
        [min: 50, max: 100, name: "Moderate", color: 'yellow', show_background: true],
        [min: 100, max: 150, name: "Unhealthy for Sensitive Groups", color: 'orange', show_background: true],
        [min: 150, max: 200, name: "Unhealthy", color: 'red', show_background: true],
        [min: 200, max: 300, name: "Very Unhealthy", color: 'purple', show_background: true],
        [min: 300, max: 400, name: "Hazardous", color: 'magenta', show_background: true],
    ]

@Field static Map colorNameMap = [
   "red": 1,
   "red-orange": 4,
   "orange": 25,
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


def handler(evt) {
	//def averageDev = getChildDevice("AQIColorMap_${app.id}")
	def avg = averageAQI() 
	//averageDev.setAirQuality(avg)
	log.info "Average AQI = $avg"
    //def c = AQIToColor(avg)
    //log.info "AQI Color = $c"
    def result = AQIToLedSettings(avg) 
    UpdateLights(result)
}

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
            log.debug("LED ${result.aboveLightsParam} set to 1, ${result.background}, Type: ${result.show_background ? 1 : 0} Brightness: ${result.show_background ? MinBrightness : 0}")
            light.ledEffectOne(result.aboveLightsParam,  result.show_background ? 1 : 0, map_color(result.background), result.show_background ? MinBrightness : 0) 
        }
    }
}

def getBackgroundForRange(i){
    if( i == 0)
    {
        return 'white'
    }
    else{
        return getCaqiRange[i-1].color
    }
}
  
def AQIToRange(v) 
{  
    //getCaqiColors().each { log.info "$it.value $it.color" }
    def range = getCaqiRange
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
    log.debug "AQI range is ${range[i]}"
    
    result = range[i]
    result['background'] = getBackgroundForRange(i)
    
    return result
}

def map_color(color){
    int result = 0
    if( colorNameMap.containsKey(color) ) {
        result = colorNameMap[color]
    }
    else{
        result = color
    }
    
    return result
}


def AQIToLedSettings(v){
    def range = AQIToRange(v)
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
    
    result = [belowLightsParam: nBelowLightsParam, nLevelLight: nLevelLight, aboveLightsParam: nAboveLightsParam,
              finalLightBrightness: finalLightBrightness, color: range.color, background: range.background,
             show_background:range.show_background] 
    log.debug "Resulting settings: ${result}"
    return result
}