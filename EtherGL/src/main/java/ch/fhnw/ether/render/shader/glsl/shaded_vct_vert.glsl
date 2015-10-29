#version 330

#include <view_block.glsl>

struct VertexData {
	vec4 position;				// vertex position in eye space
	vec3 normal;				// vertex normal in eye space
	vec4 color;					// vertex diffuse color
	vec2 texCoord;				// texture coordinate of color map
};

uniform bool useVertexColors;
uniform bool useColorMap;

in vec4 vertexPosition;
in vec4 vertexNormal;
in vec4 vertexColor;
in vec2 vertexTexCoord;

out VertexData vd;

void main() {
	vd.position = view.viewMatrix * vertexPosition;
	vd.normal = normalize(view.normalMatrix * vertexNormal.xyz);
	vd.color = useVertexColors ? vertexColor : vec4(1);

	if (useColorMap)
		vd.texCoord = vertexTexCoord;

	gl_Position = view.viewProjMatrix * vertexPosition;
}
