library (
 author: "James Anderson",
 category: "Utility",
 description: "Set Inovelli Blue Switch LED according to a colormap",
 name: "blue",
 namespace: "colormap.inovelli",
 documentationLink: ""
)
  
//Set the Inovelli Blue light switch individual LEDs for each switch
def UpdateLights(result, outputLights, MinBrightness, MaxBrightness){
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
            log.debug("LED ${result.aboveLightsParam} set to 1, ${result.background_color}, Type: ${result.show_background ? 1 : 0} Brightness: ${result.show_background ? MinBrightness : 0}")
            light.ledEffectOne(result.aboveLightsParam,  result.show_background ? 1 : 0, map_color(result.background_color), result.show_background ? MinBrightness : 0) 
        }
    }
}
 

def GetLEDSettings(float v, Map range, MinBrightness, MaxBrightness){
    if(MinBrightness == null)
        MinBrightness = 0
    
    if(MaxBrightness == null)
        MaxBrightness = 100
    
    //log.info "Get LED settings for Value ${v} in range ${range.name}"
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
    def hide_background = range['no_background'] ?: false
    
    result = [belowLightsParam: nBelowLightsParam, nLevelLight: nLevelLight, aboveLightsParam: nAboveLightsParam,
              finalLightBrightness: finalLightBrightness, color: range.color, background_color: range.background_color,
             show_background:!hide_background] 
    log.debug "Resulting settings: ${result}"
    return result
}