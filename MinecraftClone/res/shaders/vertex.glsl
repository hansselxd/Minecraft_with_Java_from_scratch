#version 330 core

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in vec2 inTexCoord;
layout(location = 3) in float inShade;
layout(location = 4) in float inAO;
layout(location = 5) in float aLight;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec2 fragTexCoord;
out vec3 fragPos;
out vec3 normal;
out float shadeFactor;
out float vAO;
out float vLight;

void main() {
    fragTexCoord = inTexCoord;
    vec4 worldPos = model * vec4(inPosition, 1.0);
    fragPos = worldPos.xyz;
    normal = mat3(transpose(inverse(model))) * inNormal;
    shadeFactor = inShade;
    vAO = inAO;
    vLight = aLight;

    gl_Position = projection * view * worldPos;
}
