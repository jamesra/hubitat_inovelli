library (
 author: "James Anderson",
 category: "Utility",
 description: "Map a value into a colormap range",
 name: "shared",
 namespace: "colormap",
 documentationLink: ""
)
 
import groovy.transform.Field

//I'd like to use a RecordType, but Hubitat apparently doesn't support it:
//
// import groovy.transform.RecordType
//
//@groovy.transform.RecordType
//class ValueRange  {
//    float min
//    float max
//    String name
//    int color
//    int background_color
//}
//
//Instead, colormaps are defined as an array of anonymous objects, like so:
//
//@Field static def temp_range_map = [
//        [min: 20, max: 32, name: "Freezing", color: 'white', no_background=True],
//        [min: 32, max: 46, name: "Cold", color: 'blue'],
//        [min: 46, max: 60, name: "Chilly", color: 135],
//        [min: 60, max: 68, name: "Mild chill", color: 110],
//        [min: 68, max: 75, name: "Comfortable", color: 'green'],
//        [min: 75, max: 82, name: "Warm", color: 'yellow'],
//        [min: 82, max: 89, name: "Too Warm", color: 'orange'],
//        [min: 89, max: 102, name: "Hot", color: 'red'],
//    ]
 

//Maps strings to Inovelli hue integer values
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

//Maps a color, either an integer or a string, to a hue integer 0-255.
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


def getBackgroundForRange(int i, colormap){
    if( i <= 0)
    {
        return colormap.default_background
    }
    else{
        return colormap.colormap[i-1].color
    }
}

//Map a value to the range in a colormap
def ValueToRange(float v, Map map) 
{  
    colormap = map.colormap
    def result = null
     
    if(!colormap.any { it -> v >= it.min && v <= it.max}){
        log.info "Could not find a range mapping for value ${v}"
        result = v <= 0 ? colormap[0] : colormap[-1]
        if( v <= 0) {
            result = colormap[0]
            result['background_color'] = getBackgroundForRange(0)    
        }
        else {
            result = colormap[-1]
            result['background_color'] = result.color
        }
        
        return result
    }
    
    def i = (colormap.findIndexOf {it -> v >= it.min && v <= it.max } )
    log.debug "Value ${v} falls in range index ${i}: ${colormap[i]}"
    if (i < 0) { i = 0 }
    log.debug "Range is ${colormap[i]}"
    
    result = colormap[i] 
    result['background_color'] = getBackgroundForRange(i, map)
    
    return result
}