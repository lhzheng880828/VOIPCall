package com.sun.media.vfw;

import com.lti.utils.UnsignedUtils;
import com.sun.media.format.AviVideoFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import org.jitsi.android.util.java.awt.Dimension;

public class BitMapInfo {
    public int biBitCount;
    public int biClrImportant;
    public int biClrUsed;
    public int biHeight;
    public int biPlanes;
    public int biSizeImage;
    public int biWidth;
    public int biXPelsPerMeter;
    public int biYPelsPerMeter;
    public byte[] extraBytes;
    public int extraSize;
    public String fourcc;

    public BitMapInfo() {
        this.fourcc = "";
        this.biPlanes = 1;
        this.biBitCount = 24;
    }

    public BitMapInfo(String fourcc, int width, int height) {
        this.fourcc = fourcc;
        this.biPlanes = 1;
        this.biBitCount = 24;
        this.biWidth = width;
        this.biHeight = height;
        if (fourcc.equals("RGB")) {
            this.biSizeImage = (this.biWidth * this.biHeight) * (this.biBitCount / 8);
        }
    }

    public BitMapInfo(String fourcc, int width, int height, int planes, int bitcount, int sizeImage, int clrused, int clrimportant) {
        this.fourcc = fourcc;
        this.biPlanes = planes;
        this.biBitCount = bitcount;
        this.biWidth = width;
        this.biHeight = height;
        this.biSizeImage = sizeImage;
        this.biClrUsed = clrused;
        this.biClrImportant = clrimportant;
    }

    public BitMapInfo(VideoFormat format) {
        if (format instanceof RGBFormat) {
            RGBFormat fCast = (RGBFormat) format;
            this.fourcc = fCast.getEncoding().toUpperCase();
            this.biPlanes = 1;
            this.biBitCount = fCast.getBitsPerPixel();
            if (fCast.getSize() == null) {
                this.biWidth = 320;
                this.biHeight = 240;
            } else {
                this.biWidth = fCast.getSize().width;
                this.biHeight = fCast.getSize().height;
            }
            if (this.biBitCount == -1) {
                this.biSizeImage = -2;
            } else {
                this.biSizeImage = (this.biWidth * this.biHeight) * (this.biBitCount / 8);
            }
            this.biClrUsed = 0;
            this.biClrImportant = 0;
        } else if (format instanceof AviVideoFormat) {
            AviVideoFormat fCast2 = (AviVideoFormat) format;
            this.fourcc = fCast2.getEncoding();
            this.biPlanes = fCast2.getPlanes();
            this.biBitCount = fCast2.getBitsPerPixel();
            this.biWidth = fCast2.getSize().width;
            this.biHeight = fCast2.getSize().height;
            this.biSizeImage = fCast2.getImageSize();
            this.biClrUsed = fCast2.getClrUsed();
            this.biClrImportant = fCast2.getClrImportant();
        } else if (format instanceof YUVFormat) {
            if (((YUVFormat) format).getYuvType() == 2) {
                this.fourcc = "YV12";
                this.biBitCount = 12;
            } else {
                this.fourcc = format.getEncoding();
                this.biBitCount = 24;
            }
            this.biWidth = 320;
            this.biHeight = 240;
            this.biPlanes = 1;
            this.biSizeImage = -1;
        } else {
            this.fourcc = format.getEncoding();
            this.biBitCount = 24;
            this.biWidth = 320;
            this.biHeight = 240;
            this.biPlanes = 1;
            this.biSizeImage = -1;
        }
    }

    public VideoFormat createVideoFormat(Class<?> arrayType) {
        return createVideoFormat(arrayType, -1.0f);
    }

    public VideoFormat createVideoFormat(Class<?> arrayType, float frameRate) {
        if (this.fourcc.equals("RGB")) {
            int red;
            int green;
            int blue;
            int pixelStride;
            int maxDataLength;
            if (this.biBitCount == 32) {
                if (arrayType == int[].class) {
                    red = 16711680;
                    green = 65280;
                    blue = UnsignedUtils.MAX_UBYTE;
                } else {
                    red = 3;
                    green = 2;
                    blue = 1;
                }
            } else if (this.biBitCount == 24) {
                red = 3;
                green = 2;
                blue = 1;
            } else if (this.biBitCount == 16) {
                red = 31744;
                green = 992;
                blue = 31;
            } else {
                blue = -1;
                green = -1;
                red = -1;
            }
            if (arrayType == int[].class) {
                pixelStride = this.biBitCount / 32;
                maxDataLength = this.biSizeImage / 4;
            } else if (arrayType == byte[].class) {
                pixelStride = this.biBitCount / 8;
                maxDataLength = this.biSizeImage;
            } else if (arrayType == short[].class) {
                pixelStride = this.biBitCount / 16;
                maxDataLength = this.biSizeImage / 2;
            } else {
                throw new IllegalArgumentException();
            }
            return new RGBFormat(new Dimension(this.biWidth, this.biHeight), maxDataLength, arrayType, frameRate, this.biBitCount, red, green, blue, pixelStride, pixelStride * this.biWidth, 1, 1);
        } else if (this.fourcc.equals("YV12")) {
            return new YUVFormat(new Dimension(this.biWidth, this.biHeight), this.biSizeImage, byte[].class, frameRate, 2, this.biWidth, this.biWidth / 2, 0, (this.biWidth * this.biHeight) + ((this.biWidth * this.biHeight) / 4), this.biWidth * this.biHeight);
        } else if (this.fourcc.equals("I420")) {
            return new YUVFormat(new Dimension(this.biWidth, this.biHeight), this.biSizeImage, byte[].class, frameRate, 2, this.biWidth, this.biWidth / 2, 0, this.biWidth * this.biHeight, (this.biWidth * this.biHeight) + ((this.biWidth * this.biHeight) / 4));
        } else {
            return new AviVideoFormat(this.fourcc, new Dimension(this.biWidth, this.biHeight), this.biSizeImage, arrayType, frameRate, this.biPlanes, this.biBitCount, this.biSizeImage, this.biXPelsPerMeter, this.biYPelsPerMeter, this.biClrUsed, this.biClrImportant, this.extraBytes);
        }
    }

    public String toString() {
        return "Size = " + this.biWidth + " x " + this.biHeight + "\tPlanes = " + this.biPlanes + "\tBitCount = " + this.biBitCount + "\tFourCC = " + this.fourcc + "\tSizeImage = " + this.biSizeImage + "\nClrUsed = " + this.biClrUsed + "\nClrImportant = " + this.biClrImportant + "\nExtraSize = " + this.extraSize + "\n";
    }
}
