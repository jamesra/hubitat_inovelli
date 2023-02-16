/**
 * ==========================  ColorMap ==========================
 *
 *  DESCRIPTION:
 *  Maps a single value to a colormap and sets a light
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
 *  Last modified: 9-12-21
 * 
 *  Changelog:
 * 
 *  v1.0 - Initial Public Release
 *
 */ 
 
definition(
    name: "AQI Color Mapper",
    namespace: "Jamesan",
    author: "James Anderson",
    singleInstance: false,
    description: "Map device values to a color LED",
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
			input "thisName", "text", title: "Name this Colormap", submitOnChange: true, required: true
			if(thisName) app.updateLabel("$thisName")
			input "inputSensor", "capability.sensor", title: "Select AQI Input", submitOnChange: true, required: true, multiple: false
            if(inputSensor) {
                paragraph "Current average AQI is ${averageAQI()}"
                paragraph "Current AQI color is ${AQIToColor(avg)}"
            }
            input "outputLights", "capability.switch", title: "Select Output", submitOnChange: true, required: true, multiple: true 
		    if(outputLights)
            {
                avg = averageAQI()
                def c = AQIToColor(avg)
                paragraph "Current AQI color is ${c}"
                //outputLights.each { it.setIndicator(c.color, c.level, 'solid', 255) }
                outputLights.each { it.setIndicator(c.config) }
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
        runEvery1Minute(handler)
    }
}

def averageAQI() {
    return inputSensor.currentValue("aqi")
    /*
	def total = 0.0
	def n = inputSensors.size()
    if(n <= 0) return -1
	inputSensors.each {total += it.airQuality}
	return (total / n).toDouble().round(1)
*/
}

private def getCaqiColors() {
	// Common Air Quality Index
	return [
    	[value:   0, color: '#79bc6a'],		// Green - Very Low
        [value:  24, color: '#79bc6a'],
        [value:  25, color: '#bbcf4c'],		// Chartruese - Low
        [value:  49, color: '#bbcf4c'],
        [value:  50, color: '#eec20b'],		// Yellow - Medium
        [value:  74, color: '#eec20b'],
        [value:  75, color: '#f29305'],		// Orange - High
        [value:  99, color: '#f29305'],
        [value: 100, color: '#e8416f'],		// Red - Very High
        [value: 124, color: '#e8416f'],
        [value: 125, color: '#ff2bff'],
    ]
}

private def getCaqiColor() {
	// Common Air Quality Index
	return [
    	[value:   0, color: 'azure', level:  20, effect: 'off', config: 16711936],		// Green - Very Low
        [value:  10, color: 'azure', level:  20, effect: 'solid', config: 33489730],
        [value:  11, color: 'spring', level:  20, effect: 'solid', config: 33489996],
        [value:  24, color: 'spring', level:  20, effect: 'solid', config: 33491532],
        [value:  25, color: 'green', level:  20, effect: 'solid', config: 33491546],		// Chartruese - Low
        [value:  49, color: 'green', level:  20, effect: 'solid', config: 33491546],
        [value:  50, color: 'yellow', level: 50, effect: 'pulse', config: 100599085],		// Yellow - Medium
        [value:  74, color: 'yellow', level: 50, effect: 'pulse', config: 100599085],
        [value:  75, color: 'yellow', level: 100, effect: 'pulse', config: 100600365],		// Orange - High
        [value:  99, color: 'yellow', level: 100, effect: 'pulse', config: 100600365],
        [value: 100, color: 'red-orange', level: 50, effect: 'pulse', config: 100599065],		// Red - Very High
        [value: 124, color: 'red-orange', level: 50, effect: 'pulse', config: 100599065],
        [value: 125, color: 'red-orange', level: 100, effect: 'pulse', config: 100600345],		 
        [value: 150, color: 'red-orange', level: 100, effect: 'pulse', config: 100600345],
        [value: 151, color: 'red', level: 50, effect: 'pulse', config: 100599040],		// Red - Very High
        [value: 174, color: 'red', level: 50, effect: 'pulse', config: 100599040],
        [value: 175, color: 'red', level: 100, effect: 'pulse', config: 100600320],		 
        [value: 200, color: 'red', level: 100, effect: 'pulse', config: 100600320],
        [value: 201, color: 'purple', level: 50, effect: 'pulse', config: 100599240],		// Red - Very High
        [value: 249, color: 'purple', level: 50, effect: 'pulse', config: 100599240],
        [value: 250, color: 'purple', level: 100, effect: 'pulse', config: 100600520],		 
        [value: 299, color: 'purple', level: 100, effect: 'pulse', config: 100600520],
        [value: 301, color: 'magenta', level: 50, effect: 'pulse', config: 100600345],		// Red - Very High
        [value: 349, color: 'magenta', level: 50, effect: 'pulse', config: 100600345],
        [value: 350, color: 'magenta', level: 100, effect: 'pulse', config: 100600558],		 
        [value: 50000, color: 'magenta', level: 100, effect: 'pulse', config: 100600558] 
    ]
}


def handler(evt) {
	//def averageDev = getChildDevice("AQIColorMap_${app.id}")
	def avg = averageAQI() 
	//averageDev.setAirQuality(avg)
	log.info "Average AQI = $avg"
    def c = AQIToColor(avg)
    log.info "AQI Color = $c"
    
    //outputLights.each { it.setIndicator(c.color, c.level, c.effect, 255) }
    outputLights.each { it.setIndicator(c.config) }
}

def AQIToColor(v) 
{
    //getCaqiColors().each { log.info "$it.value $it.color" }
    
    if(!getCaqiColor().any { it.value <= v })
        return [color: 'blue', level: 10]
    
    def i = (getCaqiColor().findIndexOf  { it.value > v } )
    i = i - 1
    if (i < 0) { i = 0 };
    
    return getCaqiColor()[i]
}