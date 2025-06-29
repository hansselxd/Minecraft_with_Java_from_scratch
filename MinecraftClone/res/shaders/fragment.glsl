#version 330 core

in vec2 fragTexCoord;
in vec3 fragPos;
in vec3 normal;
in float shadeFactor;
in float vAO;
in float vLight;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform vec3 lightPos; // ej: -0.5, -1.0, -0.5

void main() {
    vec3 norm = normalize(normal);
    vec3 texColor = texture(textureSampler, fragTexCoord).rgb;

    // Cálculo de iluminación difusa según dirección de luz
    float diff = max(dot(norm, -lightPos), 0.0);
    diff *= shadeFactor;

    // Luz ambiental escalada según la cantidad de luz ambiental (vLight)
    vec3 ambient = vec3(0.1 + 0.3 * vLight); // luz mínima de 0.1

    // Difusa escalada con intensidad de luz (vLight)
    vec3 diffuse = vec3(0.7) * diff * vLight;

    // Combinamos todo (más AO y color de textura)
    vec3 lighting = (ambient + diffuse) * texColor * vAO;
    

    // Fragment shader temporal
    //fragColor = vec4(vec3(vLight), 1.0);
    fragColor = vec4(lighting, 1.0);
}
