package org.mshare.picture;

public class ColorComputer {

	private static final int MASK_ALPHA = 0xff000000;
	private static final int MASK_RED = 0x00ff0000;
	private static final int MASK_GREEN = 0x0000ff00;
	private static final int MASK_BLUE = 0x000000ff;
	
	public static int computeGradientColor(int startColor, int endColor, float ratio) {
		int color = 0x00000000;
		int startAlpha = (MASK_ALPHA & startColor), endAlpha = (MASK_ALPHA & endColor);
		int startRed = (MASK_RED & startColor), endRed = (MASK_RED & endColor);
		int startGreen = (MASK_GREEN & startColor), endGreen = (MASK_GREEN & endColor);
		int startBlue = (MASK_BLUE & startColor), endBlue = (MASK_BLUE & endColor);
		
		color |= (startAlpha + ((int)((endAlpha - startAlpha) * ratio) & MASK_ALPHA));
		color |= (startRed + ((int)((endRed - startRed) * ratio) & MASK_RED));
		color |= (startGreen + ((int)((endGreen - startGreen) * ratio) & MASK_GREEN));
		color |= (startBlue + ((int)((endBlue - startBlue) * ratio) & MASK_BLUE));
		
		return color;
	}
	
}
