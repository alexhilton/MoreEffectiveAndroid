#pragma version(1)
#pragma rs java_package_name(net.toughcoder.effectiveandroid)

int radiusHi;
int radiusLo;
int xTouchApply;
int yTouchApply;

const float4 gWhite = {1.f, 1.f, 1.f, 1.f};
const float3 channelWeights = {0.299f, 0.587f, 0.114f};

uchar4 __attribute__((kernel)) root(const uchar4 in, uint32_t x, uint32_t y) {
    float4 f4 = rsUnpackColor8888(in);
    int xRel = x - xTouchApply;
    int yRel = y - yTouchApply;
    int polar = xRel * xRel + yRel * yRel;
    uchar4 out;

    if (polar > radiusHi || polar < radiusLo) {
        if (polar < radiusLo) {
            float3 outPixel = dot(f4.rgb, channelWeights);
            out = rsPackColorTo8888(outPixel);
        } else {
            out = in;
        }
    } else {
        out = rsPackColorTo8888(gWhite);
    }
    return out;
}