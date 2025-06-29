#version 330 core

in vec2 TexCoords;
out vec4 FragColor;

uniform sampler2D text;    // atlas de caracteres (un solo canal R)
uniform vec3 textColor;    // color del texto (RGB)

void main() {
    // La textura 'text' tiene sólo el canal R con la información de la glyph
    float alpha = texture(text, TexCoords).r;
    // Usamos textColor para RGB, y alpha para transparencia
    FragColor = vec4(textColor, alpha);
}
