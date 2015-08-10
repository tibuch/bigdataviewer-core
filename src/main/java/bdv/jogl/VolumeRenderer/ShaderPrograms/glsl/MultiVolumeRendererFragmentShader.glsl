#version 130
//http://www.visualizationlibrary.org/documentation/pag_guide_raycast_volume.html
const int maxNumberOfData = 6;
const int maxInt = 9999;
const float val_threshold =1;

uniform bool inActiveVolume[maxNumberOfData];
uniform float inMaxVolumeValue;
uniform float inMinVolumeValue;
uniform vec3 inEyePosition[maxNumberOfData];
uniform sampler3D inVolumeTexture[maxNumberOfData];
uniform sampler1D inColorTexture;

in vec3 textureCoord[maxNumberOfData];
out vec4 fragmentColor;

int getStepsInVolume(float stepsSize, vec3 position, vec3 direction){
	//infinite steps ;)
	int steps = maxInt;
	
	vec3 targetPoint = max(sign(direction),vec3(0,0,0));
	vec3 differenceVector = targetPoint - position;
	vec3 stepsInDirections = differenceVector / (direction * stepsSize);
	for(int i =0; i< 3; i++){
		if(stepsInDirections[i] < steps){
			steps = int(stepsInDirections[i])+1;
		}
	
	}
	return steps;
}

void main(void)
{	
	const int samples = 512;//256;
	float sample_step =sqrt(3f)/float(samples);
	const float brightness = 150.0f;
	 
    vec3 ray_dir = normalize(textureCoord - inEyePosition );
    vec3 ray_pos = textureCoord; // the current ray position

    fragmentColor = vec4(0.0, 0.0, 0.0, 0.0);
    vec4 color;
    float volumeNormalizeFactor = 1.f/ (inMaxVolumeValue-inMinVolumeValue+0.01);
    
    int steps =  getStepsInVolume(sample_step,ray_pos,ray_dir);
    if(steps > samples){
    	steps = samples;
    }
   	float density;
   	for(int i = 0; i< steps; i++){

        // note: 
        // - ray_dir * sample_step can be precomputed
        // - we assume the volume has a cube-like shape
        
        // break out if ray reached the end of the cube.
        density = (texture(inVolumeTexture, ray_pos).r-inMinVolumeValue) *volumeNormalizeFactor;


        color.rgb = texture(inColorTexture, density).rgb;
        color.a   = texture(inColorTexture, density).a /*density*/ * sample_step * val_threshold * brightness;
        fragmentColor.rgb = fragmentColor.rgb * (1.0 - color.a) + color.rgb * color.a;
		ray_pos += ray_dir * sample_step;  		
    }
	fragmentColor = vec4 (fragmentColor.rgb,0.1); 
}