package edu.cg;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProcessor extends FunctioalForEachLoops {

	// MARK: fields
	public final Logger logger;
	public final BufferedImage workingImage;
	public final RGBWeights rgbWeights;
	public final int inWidth;
	public final int inHeight;
	public final int workingImageType;
	public final int outWidth;
	public final int outHeight;

	// MARK: constructors
	public ImageProcessor(Logger logger, BufferedImage workingImage, RGBWeights rgbWeights, int outWidth,
			int outHeight) {
		super(); // initializing for each loops...

		this.logger = logger;
		this.workingImage = workingImage;
		this.rgbWeights = rgbWeights;
		inWidth = workingImage.getWidth();
		inHeight = workingImage.getHeight();
		workingImageType = workingImage.getType();
		this.outWidth = outWidth;
		this.outHeight = outHeight;
		setForEachInputParameters();
	}

	public ImageProcessor(Logger logger, BufferedImage workingImage, RGBWeights rgbWeights) {
		this(logger, workingImage, rgbWeights, workingImage.getWidth(), workingImage.getHeight());
	}

	// MARK: change picture hue - example
	public BufferedImage changeHue() {
		logger.log("Prepareing for hue changing...");

		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int max = rgbWeights.maxWeight;

		BufferedImage ans = newEmptyInputSizedImage();

		forEach((y, x) -> {
			Color c = new Color(workingImage.getRGB(x, y));
			int red = r * c.getRed() / max;
			int green = g * c.getGreen() / max;
			int blue = b * c.getBlue() / max;
			Color color = new Color(red, green, blue);
			ans.setRGB(x, y, color.getRGB());
		});

		logger.log("Changing hue done!");

		return ans;
	}

	public final void setForEachInputParameters() {
		setForEachParameters(inWidth, inHeight);
	}

	public final void setForEachOutputParameters() {
		setForEachParameters(outWidth, outHeight);
	}

	public final BufferedImage newEmptyInputSizedImage() {
		return newEmptyImage(inWidth, inHeight);
	}

	public final BufferedImage newEmptyOutputSizedImage() {
		return newEmptyImage(outWidth, outHeight);
	}

	public final BufferedImage newEmptyImage(int width, int height) {
		return new BufferedImage(width, height, workingImageType);
	}

	// A helper method that deep copies the current working image.
	public final BufferedImage duplicateWorkingImage() {
		BufferedImage output = newEmptyInputSizedImage();
		setForEachInputParameters();
		forEach((y, x) -> output.setRGB(x, y, workingImage.getRGB(x, y)));

		return output;
	}

	public BufferedImage greyscale() {
		// TODO: Implement this method, remove the exception.
        logger.log("Preparing for greyscale changing...");
        int r = rgbWeights.redWeight;
        int g = rgbWeights.greenWeight;
        int b = rgbWeights.blueWeight;
        int max = rgbWeights.maxWeight;

        BufferedImage ans = newEmptyInputSizedImage();

        forEach((y, x) -> {
            Color c = new Color(workingImage.getRGB(x, y));
            int red = r * c.getRed() / max;
            int green = g * c.getGreen() / max;
            int blue = b * c.getBlue() / max;
            int avg = (red+green+blue)/3;
            Color newColor = new Color(avg,avg,avg);
            ans.setRGB(x, y, newColor.getRGB());
        });

        logger.log("Changing greyscale done!");
        return ans;
	}

	public BufferedImage nearestNeighbor() {
		// TODO: Implement this method, remove the exception.
        logger.log("Preparing for Nearest neighbor changing...");

        // The new image (to be) rescaled
        BufferedImage ans = newEmptyImage(outWidth, outHeight);

        // Calculate the height factor and the width factor
        double factHeight = (double) inHeight / (double) outHeight,
                factWidth = (double) inWidth / (double) outWidth;

        // Iterate over all the pixels in the image
        for (int i = 0; i < outWidth; i++) {
            // Current nearest neighbor in the x's
            int pWidth = (int)(i * factWidth);
            int red, green, blue;

            for (int j = 0; j < outHeight; j++) {
                // Current nearest neighbor in the y's
                int pHeight = (int)(j * factHeight);
                Color c = new Color(workingImage.getRGB(pWidth, pHeight));
                red = c.getRed();
                green = c.getGreen();
                blue = c.getBlue();

                // Set pixel in the new image same as the NN found
                Color newColor = new Color(red, green, blue);
                ans.setRGB(i, j, newColor.getRGB());
            }
        }

        logger.log("Changing using Nearest neighbor done!");
        return ans;
	}
}
