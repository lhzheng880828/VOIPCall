package org.jitsi.impl.neomedia.codec.audio.g729;

class TabLd8k {
    static final float[] a100 = new float[]{1.0f, 1.9330735f, -0.935892f};
    static final float[] a140 = new float[]{1.0f, 1.9059465f, -0.9114024f};
    static final float[] b100 = new float[]{0.9398058f, -1.8795834f, 0.9398058f};
    static final float[] b140 = new float[]{0.92727435f, -1.8544941f, 0.92727435f};
    static final int[] bitsno = new int[]{8, 10, 8, 1, 13, 4, 7, 5, 13, 4, 7};
    static final float[][] coef = new float[][]{new float[]{31.134575f, 1.612322f}, new float[]{0.481389f, 0.053056f}};
    static final float[][][] fg;
    static final float[][] fg_sum = new float[][]{new float[]{0.238f, 0.2578f, 0.2504f, 0.25310004f, 0.24800001f, 0.25870004f, 0.25779995f, 0.26560003f, 0.27600002f, 0.26259995f}, new float[]{0.4451f, 0.55950004f, 0.60340005f, 0.5293f, 0.5012999f, 0.5023f, 0.46250004f, 0.4645f, 0.48959997f, 0.47939998f}};
    static final float[][] fg_sum_inv = new float[][]{new float[]{4.2016807f, 3.8789759f, 3.9936101f, 3.951007f, 4.032258f, 3.8654807f, 3.8789766f, 3.76506f, 3.623188f, 3.808074f}, new float[]{2.246686f, 1.78731f, 1.6572753f, 1.8892878f, 1.9948138f, 1.990842f, 2.162162f, 2.1528525f, 2.0424838f, 2.0859408f}};
    static final float[][] gbk1 = new float[][]{new float[]{1.0E-5f, 0.185084f}, new float[]{0.094719f, 0.296035f}, new float[]{0.111779f, 0.613122f}, new float[]{0.003516f, 0.65978f}, new float[]{0.117258f, 1.134277f}, new float[]{0.197901f, 1.214512f}, new float[]{0.021772f, 1.801288f}, new float[]{0.163457f, 3.3157f}};
    static final float[][] gbk2 = new float[][]{new float[]{0.050466f, 0.244769f}, new float[]{0.121711f, 1.0E-5f}, new float[]{0.313871f, 0.072357f}, new float[]{0.375977f, 0.292399f}, new float[]{0.49387f, 0.59341f}, new float[]{0.556641f, 0.064087f}, new float[]{0.645363f, 0.362118f}, new float[]{0.706138f, 0.14611f}, new float[]{0.809357f, 0.397579f}, new float[]{0.866379f, 0.199087f}, new float[]{0.923602f, 0.599938f}, new float[]{0.925376f, 1.742757f}, new float[]{0.942028f, 0.029027f}, new float[]{0.983459f, 0.414166f}, new float[]{1.055892f, 0.227186f}, new float[]{1.158039f, 0.724592f}};
    static final float[] grid = new float[]{0.9997559f, 0.9986295f, 0.9945219f, 0.9876884f, 0.9781476f, 0.9659258f, 0.9510565f, 0.9335804f, 0.9135454f, 0.8910065f, 0.8660254f, 0.8386706f, 0.809017f, 0.777146f, 0.7431448f, 0.7071068f, 0.6691306f, 0.6293204f, 0.5877852f, 0.5446391f, 0.5f, 0.4539905f, 0.4067366f, 0.3583679f, 0.309017f, 0.258819f, 0.2079117f, 0.1564345f, 0.1045285f, 0.052336f, 0.0f, -0.052336f, -0.1045285f, -0.1564345f, -0.2079117f, -0.258819f, -0.309017f, -0.3583679f, -0.4067366f, -0.4539905f, -0.5f, -0.5446391f, -0.5877852f, -0.6293204f, -0.6691306f, -0.7071068f, -0.7431448f, -0.777146f, -0.809017f, -0.8386706f, -0.8660254f, -0.8910065f, -0.9135454f, -0.9335804f, -0.9510565f, -0.9659258f, -0.9781476f, -0.9876884f, -0.9945219f, -0.9986295f, -0.9997559f};
    static final float[] hamwindow = new float[]{0.08f, 0.08005703f, 0.08022812f, 0.08051321f, 0.08091225f, 0.08142514f, 0.08205172f, 0.08279188f, 0.0836454f, 0.08461212f, 0.08569173f, 0.08688401f, 0.08818865f, 0.08960532f, 0.09113365f, 0.09277334f, 0.09452391f, 0.09638494f, 0.09835598f, 0.10043652f, 0.10262608f, 0.10492408f, 0.10732999f, 0.10984316f, 0.11246302f, 0.1151889f, 0.1180201f, 0.12095598f, 0.12399574f, 0.12713866f, 0.13038395f, 0.13373083f, 0.13717847f, 0.14072597f, 0.14437246f, 0.1481171f, 0.1519589f, 0.15589692f, 0.15993017f, 0.16405767f, 0.16827843f, 0.17259133f, 0.17699537f, 0.18148938f, 0.18607232f, 0.190743f, 0.19550033f, 0.20034306f, 0.20527f, 0.21027996f, 0.2153717f, 0.22054392f, 0.22579536f, 0.23112471f, 0.23653066f, 0.24201185f, 0.24756692f, 0.25319457f, 0.25889328f, 0.2646617f, 0.27049842f, 0.27640197f, 0.28237087f, 0.28840363f, 0.29449883f, 0.3006549f, 0.3068703f, 0.31314352f, 0.31947297f, 0.32585713f, 0.33229437f, 0.33878314f, 0.3453218f, 0.35190874f, 0.35854232f, 0.36522087f, 0.3719428f, 0.3787064f, 0.38550997f, 0.39235184f, 0.39923036f, 0.40614375f, 0.41309035f, 0.42006844f, 0.42707625f, 0.43411207f, 0.44117412f, 0.4482607f, 0.45537004f, 0.46250033f, 0.46964988f, 0.47681686f, 0.4839995f, 0.49119604f, 0.49840465f, 0.5056236f, 0.51285106f, 0.5200853f, 0.52732444f, 0.5345667f, 0.54181033f, 0.5490535f, 0.55629444f, 0.56353134f, 0.5707624f, 0.5779858f, 0.58519983f, 0.59240264f, 0.59959245f, 0.6067675f, 0.613926f, 0.6210661f, 0.62818617f, 0.63528436f, 0.6423589f, 0.64940804f, 0.65643007f, 0.66342324f, 0.67038584f, 0.677316f, 0.6842122f, 0.69107264f, 0.6978956f, 0.70467937f, 0.7114223f, 0.7181228f, 0.72477907f, 0.7313895f, 0.73795253f, 0.7444665f, 0.7509297f, 0.7573406f, 0.7636976f, 0.76999915f, 0.7762437f, 0.7824296f, 0.78855544f, 0.7946196f, 0.8006207f, 0.80655706f, 0.8124274f, 0.81823015f, 0.8239639f, 0.8296273f, 0.8352188f, 0.8407371f, 0.84618086f, 0.85154873f, 0.8568393f, 0.8620513f, 0.86718345f, 0.87223446f, 0.8772031f, 0.88208807f, 0.88688827f, 0.8916024f, 0.8962294f, 0.90076804f, 0.90521723f, 0.9095758f, 0.9138428f, 0.9180171f, 0.9220976f, 0.9260834f, 0.9299734f, 0.9337668f, 0.93746245f, 0.9410596f, 0.9445573f, 0.94795465f, 0.95125085f, 0.9544451f, 0.9575365f, 0.96052444f, 0.9634081f, 0.96618676f, 0.96885973f, 0.9714263f, 0.97388595f, 0.976238f, 0.9784819f, 0.980617f, 0.9826429f, 0.984559f, 0.98636484f, 0.98806006f, 0.98964417f, 0.9911167f, 0.9924774f, 0.99372596f, 0.99486196f, 0.9958852f, 0.9967953f, 0.9975922f, 0.99827564f, 0.9988454f, 0.99930143f, 0.9996435f, 0.9998716f, 0.9999857f, 1.0f, 0.9992193f, 0.99687845f, 0.9929811f, 0.98753333f, 0.9805436f, 0.9720229f, 0.9619845f, 0.9504441f, 0.93741965f, 0.92293155f, 0.9070024f, 0.8896571f, 0.8709226f, 0.8508284f, 0.82940567f, 0.80668795f, 0.7827107f, 0.75751126f, 0.7311291f, 0.7036054f, 0.6749831f, 0.6453069f, 0.61462307f, 0.5829796f, 0.55042595f, 0.5170128f, 0.48279238f, 0.4478181f, 0.41214463f, 0.37582767f, 0.33892387f, 0.30149087f, 0.26358715f, 0.22527184f, 0.18660481f, 0.14764643f, 0.1084575f, 0.06909923f, 0.02963307f};
    static final int[] imap1 = new int[]{5, 1, 7, 4, 2, 0, 6, 3};
    static final int[] imap2 = new int[]{2, 14, 3, 13, 0, 15, 1, 12, 6, 10, 7, 9, 4, 11, 5, 8};
    static final float[] inter_3 = new float[]{0.900839f, 0.760084f, 0.424082f, 0.084078f, -0.10557f, -0.12112f, -0.047624f, 0.016285f, 0.031217f, 0.015738f, 0.0f, -0.005925f, 0.0f};
    static final float[] inter_3l = new float[]{0.898517f, 0.769271f, 0.448635f, 0.095915f, -0.134333f, -0.178528f, -0.084919f, 0.036952f, 0.095533f, 0.068936f, -0.0f, -0.050404f, -0.050835f, -0.014169f, 0.023083f, 0.033543f, 0.016774f, -0.007466f, -0.01934f, -0.013755f, 0.0f, 0.0094f, 0.009029f, 0.002381f, -0.003658f, -0.005027f, -0.002405f, 0.00105f, 0.00278f, 0.002145f, 0.0f};
    static final float[][] lspcb1 = new float[][]{new float[]{0.1814f, 0.2647f, 0.458f, 1.1077f, 1.4813f, 1.7022f, 2.1953f, 2.3405f, 2.5867f, 2.6636f}, new float[]{0.2113f, 0.3223f, 0.4212f, 0.5946f, 0.7479f, 0.9615f, 1.9097f, 2.175f, 2.4773f, 2.6737f}, new float[]{0.1915f, 0.2755f, 0.377f, 0.595f, 1.3505f, 1.6349f, 2.2348f, 2.3552f, 2.5768f, 2.654f}, new float[]{0.2116f, 0.3067f, 0.4099f, 0.5748f, 0.8518f, 1.2569f, 2.0782f, 2.192f, 2.3371f, 2.4842f}, new float[]{0.2129f, 0.2974f, 0.4039f, 1.0659f, 1.2735f, 1.4658f, 1.9061f, 2.0312f, 2.6074f, 2.675f}, new float[]{0.2181f, 0.2893f, 0.4117f, 0.5519f, 0.8295f, 1.5825f, 2.1575f, 2.3179f, 2.5458f, 2.6417f}, new float[]{0.1991f, 0.2971f, 0.4104f, 0.7725f, 1.3073f, 1.4665f, 1.6208f, 1.6973f, 2.3732f, 2.5743f}, new float[]{0.1818f, 0.2886f, 0.4018f, 0.763f, 1.1264f, 1.2699f, 1.6899f, 1.865f, 2.1633f, 2.6186f}, new float[]{0.2282f, 0.3093f, 0.4243f, 0.5329f, 1.1173f, 1.7717f, 1.942f, 2.078f, 2.516f, 2.6137f}, new float[]{0.2528f, 0.3693f, 0.529f, 0.7146f, 0.9528f, 1.1269f, 1.2936f, 1.9589f, 2.4548f, 2.6653f}, new float[]{0.2332f, 0.3263f, 0.4174f, 0.5202f, 1.3633f, 1.8447f, 2.0236f, 2.1474f, 2.3572f, 2.4738f}, new float[]{0.1393f, 0.2216f, 0.3204f, 0.5644f, 0.7929f, 1.1705f, 1.7051f, 2.0054f, 2.3623f, 2.5985f}, new float[]{0.2677f, 0.3871f, 0.5746f, 0.7091f, 1.3311f, 1.526f, 1.7288f, 1.9122f, 2.5787f, 2.6598f}, new float[]{0.157f, 0.2328f, 0.3111f, 0.4216f, 1.1688f, 1.4605f, 1.9505f, 2.1173f, 2.4038f, 2.746f}, new float[]{0.2346f, 0.3321f, 0.5621f, 0.816f, 1.4042f, 1.586f, 1.7518f, 1.8631f, 2.0749f, 2.538f}, new float[]{0.2505f, 0.3368f, 0.4758f, 0.6405f, 0.8104f, 1.2533f, 1.9329f, 2.0526f, 2.2155f, 2.6459f}, new float[]{0.2196f, 0.3049f, 0.6857f, 1.3976f, 1.61f, 1.7958f, 2.0813f, 2.2211f, 2.4789f, 2.5857f}, new float[]{0.1232f, 0.2011f, 0.3527f, 0.6969f, 1.1647f, 1.5081f, 1.8593f, 2.2576f, 2.5594f, 2.6896f}, new float[]{0.3682f, 0.4632f, 0.66f, 0.9118f, 1.5245f, 1.7071f, 1.8712f, 1.9939f, 2.4356f, 2.538f}, new float[]{0.269f, 0.3711f, 0.4635f, 0.6644f, 1.4633f, 1.6495f, 1.8227f, 1.9983f, 2.1797f, 2.2954f}, new float[]{0.3555f, 0.524f, 0.9751f, 1.1685f, 1.4114f, 1.6168f, 1.7769f, 2.0178f, 2.442f, 2.5724f}, new float[]{0.3493f, 0.4404f, 0.7231f, 0.8587f, 1.1272f, 1.4715f, 1.676f, 2.2042f, 2.4735f, 2.5604f}, new float[]{0.3747f, 0.5263f, 0.7284f, 0.8994f, 1.4017f, 1.5502f, 1.7468f, 1.9816f, 2.238f, 2.3404f}, new float[]{0.2972f, 0.447f, 0.5941f, 0.7078f, 1.2675f, 1.431f, 1.593f, 1.9126f, 2.3026f, 2.4208f}, new float[]{0.2467f, 0.318f, 0.4712f, 1.1281f, 1.6206f, 1.7876f, 1.9544f, 2.0873f, 2.3521f, 2.4721f}, new float[]{0.2292f, 0.343f, 0.4383f, 0.5747f, 1.3497f, 1.5187f, 1.907f, 2.0958f, 2.2902f, 2.4301f}, new float[]{0.2573f, 0.3508f, 0.4484f, 0.7079f, 1.6577f, 1.7929f, 1.9456f, 2.0847f, 2.306f, 2.4208f}, new float[]{0.1968f, 0.2789f, 0.3594f, 0.4361f, 1.0034f, 1.704f, 1.9439f, 2.1044f, 2.2696f, 2.4558f}, new float[]{0.2955f, 0.3853f, 0.7986f, 1.247f, 1.4723f, 1.6522f, 1.8684f, 2.0084f, 2.2849f, 2.4268f}, new float[]{0.2036f, 0.3189f, 0.4314f, 0.6393f, 1.2834f, 1.4278f, 1.5796f, 2.0506f, 2.2044f, 2.3656f}, new float[]{0.2916f, 0.3684f, 0.5907f, 1.1394f, 1.3933f, 1.554f, 1.8341f, 1.9835f, 2.1301f, 2.28f}, new float[]{0.2289f, 0.3402f, 0.5166f, 0.7716f, 1.0614f, 1.2389f, 1.4386f, 2.0769f, 2.2715f, 2.4366f}, new float[]{0.0829f, 0.1723f, 0.5682f, 0.9773f, 1.3973f, 1.6174f, 1.9242f, 2.2128f, 2.4855f, 2.6327f}, new float[]{0.2244f, 0.3169f, 0.4368f, 0.5625f, 0.6897f, 1.3763f, 1.7524f, 1.9393f, 2.5121f, 2.6556f}, new float[]{0.1591f, 0.2387f, 0.2924f, 0.4056f, 1.4677f, 1.6802f, 1.9389f, 2.2067f, 2.4635f, 2.5919f}, new float[]{0.1756f, 0.2566f, 0.3251f, 0.4227f, 1.0167f, 1.2649f, 1.6801f, 2.1055f, 2.4088f, 2.7276f}, new float[]{0.105f, 0.2325f, 0.7445f, 0.9491f, 1.1982f, 1.4658f, 1.8093f, 2.0397f, 2.4155f, 2.5797f}, new float[]{0.2043f, 0.3324f, 0.4522f, 0.7477f, 0.9361f, 1.1533f, 1.6703f, 1.7631f, 2.5071f, 2.6528f}, new float[]{0.1522f, 0.2258f, 0.3543f, 0.5504f, 0.8815f, 1.5516f, 1.811f, 1.9915f, 2.3603f, 2.7735f}, new float[]{0.1862f, 0.2759f, 0.4715f, 0.6908f, 0.8963f, 1.4341f, 1.6322f, 1.763f, 2.2027f, 2.6043f}, new float[]{0.146f, 0.2254f, 0.379f, 0.8622f, 1.3394f, 1.5754f, 1.8084f, 2.0798f, 2.4319f, 2.7632f}, new float[]{0.2621f, 0.3792f, 0.5463f, 0.7948f, 1.0043f, 1.1921f, 1.3409f, 1.4845f, 2.3159f, 2.6002f}, new float[]{0.1935f, 0.2937f, 0.3656f, 0.4927f, 1.4015f, 1.6086f, 1.7724f, 1.8837f, 2.4374f, 2.5971f}, new float[]{0.2171f, 0.3282f, 0.4412f, 0.5713f, 1.1554f, 1.3506f, 1.5227f, 1.9923f, 2.41f, 2.5391f}, new float[]{0.2274f, 0.3157f, 0.4263f, 0.8202f, 1.4293f, 1.5884f, 1.7535f, 1.9688f, 2.3939f, 2.4934f}, new float[]{0.1704f, 0.2633f, 0.3259f, 0.4134f, 1.2948f, 1.4802f, 1.6619f, 2.0393f, 2.3165f, 2.6083f}, new float[]{0.1763f, 0.2585f, 0.4012f, 0.7609f, 1.1503f, 1.5847f, 1.8309f, 1.9352f, 2.0982f, 2.6681f}, new float[]{0.2447f, 0.3535f, 0.4618f, 0.5979f, 0.753f, 0.8908f, 1.5393f, 2.0075f, 2.3557f, 2.6203f}, new float[]{0.1826f, 0.3496f, 0.7764f, 0.9888f, 1.3915f, 1.7421f, 1.9412f, 2.162f, 2.4999f, 2.6931f}, new float[]{0.3033f, 0.3802f, 0.6981f, 0.8664f, 1.0254f, 1.5401f, 1.718f, 1.8124f, 2.5068f, 2.6119f}, new float[]{0.296f, 0.4001f, 0.6465f, 0.7672f, 1.3782f, 1.5751f, 1.9559f, 2.1373f, 2.3601f, 2.476f}, new float[]{0.3132f, 0.4613f, 0.6544f, 0.8532f, 1.0721f, 1.273f, 1.7566f, 1.9217f, 2.1693f, 2.6531f}, new float[]{0.3329f, 0.4131f, 0.8073f, 1.1297f, 1.2869f, 1.4937f, 1.7885f, 1.915f, 2.4505f, 2.576f}, new float[]{0.234f, 0.3605f, 0.7659f, 0.9874f, 1.1854f, 1.3337f, 1.5128f, 2.0062f, 2.4427f, 2.5859f}, new float[]{0.4131f, 0.533f, 0.653f, 0.936f, 1.3648f, 1.5388f, 1.6994f, 1.8707f, 2.4294f, 2.5335f}, new float[]{0.3754f, 0.5229f, 0.7265f, 0.9301f, 1.1724f, 1.344f, 1.5118f, 1.7098f, 2.5218f, 2.6242f}, new float[]{0.2138f, 0.2998f, 0.6283f, 1.2166f, 1.4187f, 1.6084f, 1.7992f, 2.0106f, 2.5377f, 2.6558f}, new float[]{0.1761f, 0.2672f, 0.4065f, 0.8317f, 1.09f, 1.4814f, 1.7672f, 1.8685f, 2.3969f, 2.5079f}, new float[]{0.2801f, 0.3535f, 0.4969f, 0.9809f, 1.4934f, 1.6378f, 1.8021f, 2.12f, 2.3135f, 2.4034f}, new float[]{0.2365f, 0.3246f, 0.5618f, 0.8176f, 1.1073f, 1.5702f, 1.7331f, 1.8592f, 1.9589f, 2.3044f}, new float[]{0.2529f, 0.3251f, 0.5147f, 1.153f, 1.3291f, 1.5005f, 1.7028f, 1.82f, 2.3482f, 2.4831f}, new float[]{0.2125f, 0.3041f, 0.4259f, 0.9935f, 1.1788f, 1.3615f, 1.6121f, 1.793f, 2.5509f, 2.6742f}, new float[]{0.2685f, 0.3518f, 0.5707f, 1.041f, 1.227f, 1.3927f, 1.7622f, 1.8876f, 2.0985f, 2.5144f}, new float[]{0.2373f, 0.3648f, 0.5099f, 0.7373f, 0.9129f, 1.0421f, 1.7312f, 1.8984f, 2.1512f, 2.6342f}, new float[]{0.2229f, 0.3876f, 0.8621f, 1.1986f, 1.5655f, 1.8861f, 2.2376f, 2.4239f, 2.6648f, 2.7359f}, new float[]{0.3009f, 0.3719f, 0.5887f, 0.7297f, 0.9395f, 1.8797f, 2.0423f, 2.1541f, 2.5132f, 2.6026f}, new float[]{0.3114f, 0.4142f, 0.6476f, 0.8448f, 1.2495f, 1.7192f, 2.2148f, 2.3432f, 2.5246f, 2.6046f}, new float[]{0.3666f, 0.4638f, 0.6496f, 0.7858f, 0.9667f, 1.4213f, 1.93f, 2.0564f, 2.2119f, 2.317f}, new float[]{0.4218f, 0.5075f, 0.8348f, 1.0009f, 1.2057f, 1.5032f, 1.9416f, 2.054f, 2.4352f, 2.5504f}, new float[]{0.3726f, 0.4602f, 0.5971f, 0.7093f, 0.8517f, 1.2361f, 1.8052f, 1.952f, 2.4137f, 2.5518f}, new float[]{0.4482f, 0.5318f, 0.7114f, 0.8542f, 1.0328f, 1.4751f, 1.7278f, 1.8237f, 2.3496f, 2.4931f}, new float[]{0.3316f, 0.4498f, 0.6404f, 0.8162f, 1.0332f, 1.2209f, 1.513f, 1.725f, 1.9715f, 2.4141f}, new float[]{0.2375f, 0.3221f, 0.5042f, 0.976f, 1.7503f, 1.9014f, 2.0822f, 2.2225f, 2.4689f, 2.5632f}, new float[]{0.2813f, 0.3575f, 0.5032f, 0.5889f, 0.6885f, 1.604f, 1.9318f, 2.0677f, 2.4546f, 2.5701f}, new float[]{0.2198f, 0.3072f, 0.409f, 0.6371f, 1.6365f, 1.9468f, 2.1507f, 2.2633f, 2.5063f, 2.5943f}, new float[]{0.1754f, 0.2716f, 0.3361f, 0.555f, 1.1789f, 1.3728f, 1.8527f, 1.9919f, 2.1349f, 2.3359f}, new float[]{0.2832f, 0.354f, 0.608f, 0.8467f, 1.0259f, 1.6467f, 1.8987f, 1.9875f, 2.4744f, 2.5527f}, new float[]{0.267f, 0.3564f, 0.5628f, 0.7172f, 0.9021f, 1.5328f, 1.7131f, 2.0501f, 2.5633f, 2.6574f}, new float[]{0.2729f, 0.3569f, 0.6252f, 0.7641f, 0.9887f, 1.6589f, 1.8726f, 1.9947f, 2.1884f, 2.4609f}, new float[]{0.2155f, 0.3221f, 0.458f, 0.6995f, 0.9623f, 1.2339f, 1.6642f, 1.8823f, 2.0518f, 2.2674f}, new float[]{0.4224f, 0.7009f, 1.1714f, 1.4334f, 1.7595f, 1.9629f, 2.2185f, 2.3304f, 2.5446f, 2.6369f}, new float[]{0.456f, 0.5403f, 0.7568f, 0.8989f, 1.1292f, 1.7687f, 1.9575f, 2.0784f, 2.426f, 2.5484f}, new float[]{0.4299f, 0.5833f, 0.8408f, 1.0596f, 1.5524f, 1.7484f, 1.9471f, 2.2034f, 2.4617f, 2.5812f}, new float[]{0.2614f, 0.3624f, 0.8381f, 0.9829f, 1.222f, 1.6064f, 1.8083f, 1.9362f, 2.1397f, 2.2773f}, new float[]{0.5064f, 0.7481f, 1.1021f, 1.3271f, 1.5486f, 1.7096f, 1.9503f, 2.1006f, 2.3911f, 2.5141f}, new float[]{0.5375f, 0.6552f, 0.8099f, 1.0219f, 1.2407f, 1.416f, 1.8266f, 1.9936f, 2.1951f, 2.2911f}, new float[]{0.4994f, 0.6575f, 0.8365f, 1.0706f, 1.4116f, 1.6224f, 1.92f, 2.0667f, 2.3262f, 2.4539f}, new float[]{0.3353f, 0.4426f, 0.6469f, 0.9161f, 1.2528f, 1.3956f, 1.608f, 1.8909f, 2.06f, 2.138f}, new float[]{0.2745f, 0.4341f, 1.0424f, 1.2928f, 1.5461f, 1.794f, 2.0161f, 2.1758f, 2.4742f, 2.5937f}, new float[]{0.1562f, 0.2393f, 0.4786f, 0.9513f, 1.2395f, 1.801f, 2.032f, 2.2143f, 2.5243f, 2.6204f}, new float[]{0.2979f, 0.4242f, 0.8224f, 1.0564f, 1.4881f, 1.7808f, 2.0898f, 2.1882f, 2.3328f, 2.4389f}, new float[]{0.2294f, 0.307f, 0.549f, 0.9244f, 1.2229f, 1.8248f, 1.9704f, 2.0627f, 2.2458f, 2.3653f}, new float[]{0.3423f, 0.4502f, 0.9144f, 1.2313f, 1.3694f, 1.5517f, 1.9907f, 2.1326f, 2.4509f, 2.5789f}, new float[]{0.247f, 0.3275f, 0.4729f, 1.0093f, 1.2519f, 1.4216f, 1.854f, 2.0877f, 2.3151f, 2.4156f}, new float[]{0.3447f, 0.4401f, 0.7099f, 1.0493f, 1.2312f, 1.4001f, 2.0225f, 2.1317f, 2.2894f, 2.4263f}, new float[]{0.3481f, 0.4494f, 0.6446f, 0.9336f, 1.1198f, 1.262f, 1.8264f, 1.9712f, 2.1435f, 2.2552f}, new float[]{0.1646f, 0.3229f, 0.7112f, 1.0725f, 1.2964f, 1.5663f, 1.9843f, 2.2363f, 2.5798f, 2.7572f}, new float[]{0.2614f, 0.3707f, 0.5241f, 0.7425f, 0.9269f, 1.2976f, 2.0945f, 2.2014f, 2.6204f, 2.6959f}, new float[]{0.1963f, 0.29f, 0.4131f, 0.8397f, 1.2171f, 1.3705f, 2.0665f, 2.1546f, 2.464f, 2.5782f}, new float[]{0.3387f, 0.4415f, 0.6121f, 0.8005f, 0.9507f, 1.0937f, 2.0836f, 2.2342f, 2.3849f, 2.5076f}, new float[]{0.2362f, 0.5876f, 0.7574f, 0.8804f, 1.0961f, 1.424f, 1.9519f, 2.1742f, 2.4935f, 2.6493f}, new float[]{0.2793f, 0.4282f, 0.6149f, 0.8352f, 1.0106f, 1.1766f, 1.8392f, 2.0119f, 2.6433f, 2.7117f}, new float[]{0.3603f, 0.4604f, 0.5955f, 0.9251f, 1.1006f, 1.2572f, 1.7688f, 1.8607f, 2.4687f, 2.5623f}, new float[]{0.3975f, 0.5849f, 0.8059f, 0.9182f, 1.0552f, 1.185f, 1.6356f, 1.9627f, 2.3318f, 2.4719f}, new float[]{0.2231f, 0.3192f, 0.4256f, 0.7373f, 1.4831f, 1.6874f, 1.9765f, 2.1097f, 2.6152f, 2.6906f}, new float[]{0.1221f, 0.2081f, 0.3665f, 0.7734f, 1.0341f, 1.2818f, 1.8162f, 2.0727f, 2.4446f, 2.7377f}, new float[]{0.201f, 0.2791f, 0.3796f, 0.8845f, 1.403f, 1.5615f, 2.0538f, 2.1567f, 2.3171f, 2.4686f}, new float[]{0.2086f, 0.3053f, 0.4047f, 0.8224f, 1.0656f, 1.2115f, 1.9641f, 2.0871f, 2.243f, 2.4313f}, new float[]{0.3203f, 0.4285f, 0.5467f, 0.6891f, 1.2039f, 1.3569f, 1.8578f, 2.2055f, 2.3906f, 2.4881f}, new float[]{0.3074f, 0.4192f, 0.5772f, 0.7799f, 0.9866f, 1.1335f, 1.6068f, 2.2441f, 2.4194f, 2.5089f}, new float[]{0.2108f, 0.291f, 0.4993f, 0.7695f, 0.9528f, 1.5681f, 1.7838f, 2.1495f, 2.3522f, 2.4636f}, new float[]{0.3492f, 0.456f, 0.5906f, 0.7379f, 0.8855f, 1.0257f, 1.7128f, 1.9997f, 2.2019f, 2.3694f}, new float[]{0.5185f, 0.7316f, 0.9708f, 1.1954f, 1.5066f, 1.7887f, 2.1396f, 2.2918f, 2.5429f, 2.6489f}, new float[]{0.4276f, 0.4946f, 0.6934f, 0.8308f, 0.9944f, 1.4582f, 2.0324f, 2.1294f, 2.4891f, 2.6324f}, new float[]{0.3847f, 0.5973f, 0.7202f, 0.8787f, 1.3938f, 1.5959f, 1.8463f, 2.1574f, 2.505f, 2.6687f}, new float[]{0.4835f, 0.5919f, 0.7235f, 0.8862f, 1.0756f, 1.2853f, 1.9118f, 2.0215f, 2.2213f, 2.4638f}, new float[]{0.5492f, 0.8062f, 0.981f, 1.1293f, 1.3189f, 1.5415f, 1.9385f, 2.1378f, 2.4439f, 2.5691f}, new float[]{0.519f, 0.6764f, 0.8123f, 1.0154f, 1.2085f, 1.4266f, 1.8433f, 2.0866f, 2.5113f, 2.6474f}, new float[]{0.4602f, 0.6503f, 0.9602f, 1.1427f, 1.3043f, 1.4427f, 1.6676f, 1.8758f, 2.2868f, 2.4271f}, new float[]{0.3764f, 0.4845f, 0.7627f, 0.9914f, 1.1961f, 1.3421f, 1.5129f, 1.6707f, 2.1836f, 2.3322f}, new float[]{0.3334f, 0.5701f, 0.8622f, 1.1232f, 1.3851f, 1.6767f, 2.06f, 2.2946f, 2.5375f, 2.7295f}, new float[]{0.1449f, 0.2719f, 0.5783f, 0.8807f, 1.1746f, 1.5422f, 1.8804f, 2.1934f, 2.4734f, 2.8728f}, new float[]{0.2333f, 0.3024f, 0.478f, 1.2327f, 1.418f, 1.5815f, 1.9804f, 2.0921f, 2.3524f, 2.5304f}, new float[]{0.2154f, 0.3075f, 0.4746f, 0.8477f, 1.117f, 1.5369f, 1.9847f, 2.0733f, 2.188f, 2.2504f}, new float[]{0.1709f, 0.4486f, 0.8705f, 1.0643f, 1.3047f, 1.5269f, 1.9175f, 2.1621f, 2.4073f, 2.5718f}, new float[]{0.2835f, 0.3752f, 0.5234f, 0.9898f, 1.1484f, 1.2974f, 1.9363f, 2.0378f, 2.4065f, 2.6214f}, new float[]{0.3211f, 0.4077f, 0.5809f, 1.0206f, 1.2542f, 1.3835f, 1.5723f, 2.1209f, 2.3464f, 2.4336f}, new float[]{0.2101f, 0.3146f, 0.6779f, 0.8783f, 1.0561f, 1.3045f, 1.8395f, 2.0695f, 2.2831f, 2.4328f}};
    static final float[][] lspcb2 = new float[][]{new float[]{-0.0532f, -0.0995f, -0.0906f, 0.1261f, -0.0633f, 0.0711f, -0.1467f, 0.1012f, 0.0106f, 0.047f}, new float[]{-0.1017f, -0.1088f, 0.0566f, -0.001f, -0.1528f, 0.1771f, 0.0089f, -0.0282f, 0.1055f, 0.0808f}, new float[]{-0.1247f, 0.0283f, -0.0374f, 0.0393f, -0.0269f, -0.02f, -0.0643f, -0.0921f, -0.1994f, 0.0327f}, new float[]{0.007f, -0.0242f, -0.0415f, -0.0041f, -0.1793f, 0.07f, 0.0972f, -0.0207f, -0.0771f, 0.0997f}, new float[]{0.0209f, -0.0428f, 0.0359f, 0.2027f, 0.0554f, 0.0634f, 0.0356f, 0.0195f, -0.0782f, -0.1583f}, new float[]{-0.0856f, -0.1028f, -0.0071f, 0.116f, 0.1089f, 0.1892f, 0.0874f, 0.0644f, -0.0872f, -0.0236f}, new float[]{0.0713f, 0.0039f, -0.0353f, 0.0435f, -0.0407f, -0.0558f, 0.0748f, -0.0346f, -0.1686f, -0.0905f}, new float[]{-0.0134f, -0.0987f, 0.0283f, 0.0095f, -0.0107f, -0.042f, 0.1638f, 0.1328f, -0.0799f, -0.0695f}, new float[]{-0.1049f, 0.151f, 0.0672f, 0.1043f, 0.0872f, -0.0663f, -0.2139f, -0.0239f, -0.012f, -0.0338f}, new float[]{-0.1071f, -0.1165f, -0.1524f, -0.0365f, 0.026f, -0.0288f, -0.0889f, 0.1159f, 0.1852f, 0.1093f}, new float[]{-0.0094f, 0.042f, -0.0758f, 0.0932f, 0.0505f, 0.0614f, -0.0443f, -0.1172f, -0.059f, 0.1693f}, new float[]{-0.0384f, -0.0375f, -0.0313f, -0.1539f, -0.0524f, 0.055f, -0.0569f, -0.0133f, 0.1233f, 0.2714f}, new float[]{0.0869f, 0.0847f, 0.0637f, 0.0794f, 0.1594f, -0.0035f, -0.0462f, 0.0909f, -0.1227f, 0.0294f}, new float[]{-0.0137f, -0.0332f, -0.0611f, 0.1156f, 0.2116f, 0.0332f, -0.0019f, 0.111f, -0.0317f, 0.2061f}, new float[]{0.0703f, -0.0013f, -0.0572f, -0.0243f, 0.1345f, -0.1235f, 0.071f, -0.0065f, -0.0912f, 0.1072f}, new float[]{0.0178f, -0.0349f, -0.1563f, -0.0487f, 0.0044f, -0.0609f, -0.1682f, 0.0023f, -0.0542f, 0.1811f}, new float[]{-0.1384f, -0.102f, 0.1649f, 0.1568f, -0.0116f, 0.124f, -0.0271f, 0.0541f, 0.0455f, -0.0433f}, new float[]{-0.1782f, -0.1511f, 0.0509f, -0.0261f, 0.057f, 0.0817f, 0.0805f, 0.2003f, 0.1138f, 0.0653f}, new float[]{-0.0019f, 0.0081f, 0.0572f, 0.1245f, -0.0914f, 0.1691f, -0.0223f, -0.1108f, -0.0881f, -0.032f}, new float[]{-0.0413f, 0.0181f, 0.1764f, 0.0092f, -0.0928f, 0.0695f, 0.1523f, 0.0412f, 0.0508f, -0.0148f}, new float[]{0.0476f, 0.0292f, 0.1915f, 0.1198f, 0.0139f, 0.0451f, -0.1225f, -0.0619f, -0.0717f, -0.1104f}, new float[]{-0.0382f, -0.012f, 0.1159f, 0.0039f, 0.1348f, 0.0088f, -0.0173f, 0.1789f, 0.0078f, -0.0959f}, new float[]{0.1376f, 0.0713f, 0.102f, 0.0339f, -0.1415f, 0.0254f, 0.0368f, -0.1077f, 0.0143f, -0.0494f}, new float[]{0.0658f, -0.014f, 0.1046f, -0.0603f, 0.0273f, -0.1114f, 0.0761f, -0.0093f, 0.0338f, -0.0538f}, new float[]{0.2683f, 0.2853f, 0.1549f, 0.0819f, 0.0372f, -0.0327f, -0.0642f, 0.0172f, 0.1077f, -0.017f}, new float[]{-0.1949f, 0.0672f, 0.0978f, -0.0557f, -0.0069f, -0.0851f, 0.1057f, 0.1294f, 0.0505f, 0.0545f}, new float[]{0.1409f, 0.0724f, -0.0094f, 0.1511f, -0.0039f, 0.071f, -0.1266f, -0.1093f, 0.0817f, 0.0363f}, new float[]{0.0485f, 0.0682f, 0.0248f, -0.0974f, -0.1122f, 4.0E-4f, 0.0845f, -0.0357f, 0.1282f, 0.0955f}, new float[]{0.0408f, 0.1801f, 0.0772f, -0.0098f, 0.0059f, -0.1296f, -0.0591f, 0.0443f, -0.0729f, -0.1041f}, new float[]{-0.0666f, -0.0403f, -0.0524f, -0.0831f, 0.1384f, -0.1443f, -0.0909f, 0.1636f, 0.032f, 0.0077f}, new float[]{0.1612f, 0.101f, -0.0486f, -0.0704f, 0.0417f, -0.0945f, -0.059f, -0.1523f, -0.0086f, 0.012f}, new float[]{-0.0199f, 0.0823f, -0.0014f, -0.1082f, 0.0649f, -0.1374f, -0.0324f, -0.0296f, 0.0885f, 0.1141f}};
    static final float[] lwindow = new float[]{0.9987904f, 0.9954689f, 0.9899578f, 0.98229337f, 0.9725262f, 0.96072036f, 0.94695264f, 0.9313118f, 0.9138975f, 0.8948196f};
    static final int[] map1 = new int[]{5, 1, 4, 7, 3, 0, 6, 2};
    static final int[] map2 = new int[]{4, 6, 0, 2, 12, 14, 8, 10, 15, 11, 9, 13, 7, 3, 1, 5};
    static final float[] pred = new float[]{0.68f, 0.58f, 0.34f, 0.19f};
    static final float[] tab_hup_l = new float[]{-0.001246f, 0.0022f, -0.004791f, 0.009621f, -0.017685f, 0.031212f, -0.057225f, 0.13547f, 0.973955f, -0.103495f, 0.048663f, -0.02709f, 0.01528f, -0.00816f, 0.003961f, -0.001827f, -0.002388f, 0.004479f, -0.009715f, 0.019261f, -0.035118f, 0.061945f, -0.115187f, 0.294161f, 0.898322f, -0.170283f, 0.083211f, -0.046645f, 0.02621f, -0.013854f, 0.006641f, -0.003099f, -0.003277f, 0.006456f, -0.013906f, 0.027229f, -0.049283f, 0.08699f, -0.16459f, 0.464041f, 0.780309f, -0.199879f, 0.100795f, -0.056792f, 0.031761f, -0.016606f, 0.007866f, -0.00374f, -0.00377f, 0.007714f, -0.016462f, 0.031849f, -0.057272f, 0.101294f, -0.195755f, 0.630993f, 0.630993f, -0.195755f, 0.101294f, -0.057272f, 0.031849f, -0.016462f, 0.007714f, -0.00377f, -0.00374f, 0.007866f, -0.016606f, 0.031761f, -0.056792f, 0.100795f, -0.199879f, 0.780309f, 0.464041f, -0.16459f, 0.08699f, -0.049283f, 0.027229f, -0.013906f, 0.006456f, -0.003277f, -0.003099f, 0.006641f, -0.013854f, 0.02621f, -0.046645f, 0.083211f, -0.170283f, 0.898322f, 0.294161f, -0.115187f, 0.061945f, -0.035118f, 0.019261f, -0.009715f, 0.004479f, -0.002388f, -0.001827f, 0.003961f, -0.00816f, 0.01528f, -0.02709f, 0.048663f, -0.103495f, 0.973955f, 0.13547f, -0.057225f, 0.031212f, -0.017685f, 0.009621f, -0.004791f, 0.0022f, -0.001246f};
    static final float[] tab_hup_s = new float[]{-0.005772f, 0.087669f, 0.965882f, -0.048753f, -0.014793f, 0.214886f, 0.868791f, -0.065537f, -0.028507f, 0.374334f, 0.723418f, -0.060834f, -0.045567f, 0.550847f, 0.550847f, -0.045567f, -0.060834f, 0.723418f, 0.374334f, -0.028507f, -0.065537f, 0.868791f, 0.214886f, -0.014793f, -0.048753f, 0.965882f, 0.087669f, -0.005772f};
    static final float[] thr1 = new float[]{0.659681f, 0.755274f, 1.207205f, 1.98774f};
    static final float[] thr2 = new float[]{0.429912f, 0.494045f, 0.618737f, 0.650676f, 0.717949f, 0.77005f, 0.850628f, 0.932089f};

    TabLd8k() {
    }

    static {
        r0 = new float[2][][];
        r0[0] = new float[][]{new float[]{0.257f, 0.278f, 0.28f, 0.2736f, 0.2757f, 0.2764f, 0.2675f, 0.2678f, 0.2779f, 0.2647f}, new float[]{0.2142f, 0.2194f, 0.2331f, 0.223f, 0.2272f, 0.2252f, 0.2148f, 0.2123f, 0.2115f, 0.2096f}, new float[]{0.167f, 0.1523f, 0.1567f, 0.158f, 0.1601f, 0.1569f, 0.1589f, 0.1555f, 0.1474f, 0.1571f}, new float[]{0.1238f, 0.0925f, 0.0798f, 0.0923f, 0.089f, 0.0828f, 0.101f, 0.0988f, 0.0872f, 0.106f}};
        r0[1] = new float[][]{new float[]{0.236f, 0.2405f, 0.2499f, 0.2495f, 0.2517f, 0.2591f, 0.2636f, 0.2625f, 0.2551f, 0.231f}, new float[]{0.1285f, 0.0925f, 0.0779f, 0.106f, 0.1183f, 0.1176f, 0.1277f, 0.1268f, 0.1193f, 0.1211f}, new float[]{0.0981f, 0.0589f, 0.0401f, 0.0654f, 0.0761f, 0.0728f, 0.0841f, 0.0826f, 0.0776f, 0.0891f}, new float[]{0.0923f, 0.0486f, 0.0287f, 0.0498f, 0.0526f, 0.0482f, 0.0621f, 0.0636f, 0.0584f, 0.0794f}};
        fg = r0;
    }
}
