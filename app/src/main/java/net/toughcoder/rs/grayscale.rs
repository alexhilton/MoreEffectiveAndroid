#pragma version(1)
#pragma rs java_package_name(net.toughcoder.effectiveandroid)

static const float3 channelWeights = {0.299f, 0.587f, 0.114f};

uchar4 __attribute__((kernel)) root(const uchar4 in, uint32_t x, uint32_t y) {
    float4 pixel = rsUnpackColor8888(in);
    float3 outPixel = dot(pixel.rgb, channelWeights);
    uchar4 out;
    out = rsPackColorTo8888(outPixel);
    return out;
}