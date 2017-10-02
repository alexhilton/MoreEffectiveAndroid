package net.toughcoder.opengl.sharedcontext;

import android.graphics.SurfaceTexture;

/**
 * Created by alex on 17-9-30.
 */

public class FilterRenderer extends SurfaceTextureRenderer {
    public static final String SPHERE_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "varying highp vec2 textureCoords;\n" +
                    "\n" +
                    "uniform samplerExternalOES uTextureSampler;\n" +
                    "\n" +
                    "highp vec2 center = vec2(.5, .5);\n" +
                    "highp float radius = .42;\n" +
                    "highp float aspectRatio = 1.;\n" +
                    "highp float refractiveIndex = 0.71;\n" +
                    "const highp vec3 lightPosition = vec3(-0.5, 0.5, 1.0);\n" +
                    "const highp vec3 ambientLightPosition = vec3(0.0, 0.0, 1.0);\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "  highp vec2 textureCoordinateToUse = vec2(textureCoords.x, (textureCoords.y * aspectRatio + 0.5 - 0.5 * aspectRatio));\n" +
                    "  highp float distanceFromCenter = distance(center, textureCoordinateToUse);\n" +
                    "  lowp float checkForPresenceWithinSphere = step(distanceFromCenter, radius);\n" +
                    "\n" +
                    "  distanceFromCenter = distanceFromCenter / radius;\n" +
                    "\n" +
                    "  highp float normalizedDepth = radius * sqrt(1.0 - distanceFromCenter * distanceFromCenter);\n" +
                    "  highp vec3 sphereNormal = normalize(vec3(textureCoordinateToUse - center, normalizedDepth));\n" +
                    "\n" +
                    "  highp vec3 refractedVector = 2.0 * refract(vec3(0.0, 0.0, -1.0), sphereNormal, refractiveIndex);\n" +
                    "  refractedVector.xy = -refractedVector.xy;\n" +
                    "\n" +
                    "  highp vec3 finalSphereColor = texture2D(uTextureSampler, (refractedVector.xy + 1.0) * 0.5).rgb;\n" +
                    "\n" +
                    "  // Grazing angle lighting\n" +
                    "  highp float lightingIntensity = 2.5 * (1.0 - pow(clamp(dot(ambientLightPosition, sphereNormal), 0.0, 1.0), 0.25));\n" +
                    "  finalSphereColor += lightingIntensity;\n" +
                    "\n" +
                    "  // Specular lighting\n" +
                    "  lightingIntensity  = clamp(dot(normalize(lightPosition), sphereNormal), 0.0, 1.0);\n" +
                    "  lightingIntensity  = pow(lightingIntensity, 15.0);\n" +
                    "  finalSphereColor += vec3(0.8, 0.8, 0.8) * lightingIntensity;\n" +
                    "\n" +
                    "  gl_FragColor = vec4(finalSphereColor, 1.0) * checkForPresenceWithinSphere;\n" +
                    "}\n";

    public static final String SWIRL_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision highp float;\n" +
                    "uniform samplerExternalOES uTextureSampler;\n" +
                    "varying highp vec2 textureCoords;\n" +
                    "\n" +
                    "highp vec2 center = vec2(.5, .5);\n" +
                    "highp float radius = .42;\n" +
                    "highp float angle = 1.;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "  highp vec2 textureCoordinateToUse = textureCoords;\n" +
                    "  highp float dist = distance(center, textureCoords);\n" +
                    "  if (dist < radius)\n" +
                    "  {\n" +
                    "    textureCoordinateToUse -= center;\n" +
                    "    highp float percent = (radius - dist) / radius;\n" +
                    "    highp float theta = percent * percent * angle * 8.0;\n" +
                    "    highp float s = sin(theta);\n" +
                    "    highp float c = cos(theta);\n" +
                    "    textureCoordinateToUse = vec2(dot(textureCoordinateToUse, vec2(c, -s)), dot(textureCoordinateToUse, vec2(s, c)));\n" +
                    "    textureCoordinateToUse += center;\n" +
                    "  }\n" +
                    "  gl_FragColor = texture2D(uTextureSampler, textureCoordinateToUse);\n" +
                    "}\n";

    public static final String GRAYSCALE_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision highp float;\n" +
                    "uniform samplerExternalOES uTextureSampler;\n" +
                    "varying vec2 textureCoords;\n" +
                    "void main () {\n" +
                    "  vec4 tex = texture2D(uTextureSampler, textureCoords);\n" +
                    "  vec3 factor = vec3(0.2125, 0.7154, 0.0721); \n" +
                    "  float luma = dot(tex.rgb, factor); \n" +
                    "  gl_FragColor = vec4(vec3(luma), tex.a);\n" +
                    "}";
    private final SurfaceTexture mSurfaceTexture;
    private final int mPreviewTexture;
    private final String mFragmentShader;

    public FilterRenderer(SurfaceTexture surfaceTexture, int texture, String fragShader) {
        mSurfaceTexture = surfaceTexture;
        mPreviewTexture = texture;
        mFragmentShader = fragShader;
    }

    @Override
    protected SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    protected int getPreviewTexture() {
        return mPreviewTexture;
    }

    @Override
    protected String getFragmentShader() {
        return mFragmentShader;
    }
}
