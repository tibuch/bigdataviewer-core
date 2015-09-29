package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.sgvRayPositions;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.suvVoxelCount;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

public class VoxelDistanceAccumulator extends AbstractVolumeAccumulator {

	public VoxelDistanceAccumulator() {
		super("voxeldistance");
	}
	
	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 33",
				"float["+scvMaxNumberOfVolumes+"] calcVoxelDiff(vec3 positionOnRay){",
				"	float differences["+scvMaxNumberOfVolumes+"];",
				"	for(int v = 0; v < "+scvMaxNumberOfVolumes+"; v++){",
				"		vec4 positionInVoxelSpace = "+suvTextureTransformationInverse+"[v] * vec4(positionOnRay,1.0);",
				"		positionInVoxelSpace/= positionInVoxelSpace.w;",
				"		vec3 voxelCounts = vec3("+suvVoxelCount+"[v]); ",
				"		vec3 posVoxelSpace = (positionInVoxelSpace.xyz/**"+sgvTexTScales+"[v]+"+sgvTexTOffsets+"[v]*/) * voxelCounts;",
				"		vec3 minDiffDim = abs(vec3(0.5)-fract(posVoxelSpace));",
				"		differences[v] = length(minDiffDim*1.0/vec3(voxelCounts));",
				"	}",
				"	return differences;",
				"}",
				"",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float density = 0.0;",		
				"	float sum = 0.0;",
				"	float diffs["+scvMaxNumberOfVolumes+"] = calcVoxelDiff("+sgvRayPositions+");",	
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"		if(densities[n] < 0.0){",
				"			continue;",	
				"		}",	
				"		float weight = 1.0/diffs[n];",
				"		sum+=weight;",
				"		density = weight * densities[n];",
				"	}",
				"	if(sum > 0.0){",
				"		density /= sum;",
				"	}",
				"	return density;",	
				"}"
		};
		
		addCodeArrayToList(dec, code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}
}
