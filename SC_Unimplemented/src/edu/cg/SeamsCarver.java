package edu.cg;

import java.awt.image.BufferedImage;

public class SeamsCarver extends ImageProcessor {

	// MARK: An inner interface for functional programming.
	@FunctionalInterface
	interface ResizeOperation {
		BufferedImage resize();
	}

	// MARK: Fields
	private int numOfSeams;
	private ResizeOperation resizeOp;
	boolean[][] imageMask;
	BufferedImage greyscaleImage;
	// TODO: Add some additional fields
    int currentWidth;
    int[][] indexMapping;

	public SeamsCarver(Logger logger, BufferedImage workingImage, int outWidth, RGBWeights rgbWeights,
			boolean[][] imageMask) {
		super((s) -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, workingImage.getHeight());

		numOfSeams = Math.abs(outWidth - inWidth);
		this.imageMask = imageMask;
		if (inWidth < 2 | inHeight < 2)
			throw new RuntimeException("Can not apply seam carving: workingImage is too small");

		if (numOfSeams > inWidth / 2)
			throw new RuntimeException("Can not apply seam carving: too many seams...");

		// Setting resizeOp by with the appropriate method reference
		if (outWidth > inWidth)
			resizeOp = this::increaseImageWidth;
		else if (outWidth < inWidth)
			resizeOp = this::reduceImageWidth;
		else
			resizeOp = this::duplicateWorkingImage;

		// TODO: You may initialize your additional fields and apply some preliminary
		// calculations.
        greyscaleImage = greyscale();
        currentWidth = inWidth;
        System.out.println("current Width" + currentWidth + " current Heigth: " + inHeight);

        indexMapping = new int[inHeight][inWidth];
        initIndexMapping();

		this.logger.log("preliminary calculations were ended.");
	}

	public BufferedImage resize() {
		return resizeOp.resize();
	}

	private BufferedImage reduceImageWidth() {
		// TODO: Implement this method, remove the exception.
        for (int i = 0; i < numOfSeams; i++) {
            removeOneSeam();
            currentWidth--;
        }

        return imageFromIndexMapping();
	}

	private BufferedImage increaseImageWidth() {
		// TODO: Implement this method, remove the exception.
		throw new UnimplementedMethodException("increaseImageWidth");
	}

	public BufferedImage showSeams(int seamColorRGB) {
		// TODO: Implement this method (bonus), remove the exception.
		throw new UnimplementedMethodException("showSeams");
	}

	private void initIndexMapping(){
        // TODO
        for (int i = 0; i < inHeight; i++) {
            for (int j = 0; j < inWidth; j++) {
                indexMapping[i][j] = j;
            }
        }
    }

    private long[][] calculateEnergyMatrix(){
	    long[][] energyMatrix = new long[inHeight][currentWidth];
        //System.out.println("energy1");
        int e1,e2,e3;
        for (int i = 0; i < inHeight; i++) {
            //System.out.println("energy11");
            for (int j = 0; j < currentWidth; j++) {
                //System.out.println("i : "+ i + "j: " + j);

                if(j < currentWidth - 1){
                    //System.out.println("heyy "+ indexMapping[i][j]);
                    e1 = Math.abs(greyscaleImage.getRGB(indexMapping[i][j], i)- greyscaleImage.getRGB(indexMapping[i][j]+1, i));
                }else{
                    //System.out.println("heyy lol "+ indexMapping[i][j]);
                    e1 = Math.abs(greyscaleImage.getRGB(indexMapping[i][j], i)- greyscaleImage.getRGB(indexMapping[i][j]-1, i));
                }

                if(i < inHeight - 1){
                    e2 = Math.abs(greyscaleImage.getRGB(indexMapping[i][j], i)- greyscaleImage.getRGB(indexMapping[i][j], i+1));
                }else{
                    e2 = Math.abs(greyscaleImage.getRGB(indexMapping[i][j], i)- greyscaleImage.getRGB(indexMapping[i][j], i-1));
                }

                if(imageMask[i][j]){
                    e3 = Integer.MAX_VALUE;
                }else {
                    e3 = 0;
                }
                energyMatrix[i][j] = e1 + e2 + e3;
            }
        }
        System.out.println("energy2");
        return energyMatrix;
    }

    private long[][] calculateCostMatrix(long[][] energyMatrix){
	    // TODO
        long[][] costMatrix = new long[inHeight][currentWidth];

        // initialize the first row
        for (int j = 0; j < currentWidth; j++) {
            costMatrix[0][j] = energyMatrix[0][j];
        }

        if(!(costMatrix.length > 1))
            return costMatrix;


        for (int i = 1; i < inHeight; i++) {
            for (int j = 1; j < currentWidth; j++) {
                    costMatrix[i][j] = energyMatrix[i][j] +
                            Math.min(
                                    Math.min(costMatrix[i - 1][j - 1] + CL(i, j),
                                            costMatrix[i - 1][j] + CV(i, j))
                                    ,
                                    costMatrix[i - 1][j + 1] + CR(i, j));
                }
        }

        return costMatrix;
    }

    private int CL(int i, int j){
        if(indexMapping[i][j] == 0 || indexMapping[i][j] == currentWidth-1){
            return 0;
        }
	    return Math.abs(greyscaleImage.getRGB(indexMapping[i][j]+1, i) - greyscaleImage.getRGB(indexMapping[i][j]-1, i)) +
                Math.abs(greyscaleImage.getRGB(indexMapping[i][j], i-1) - greyscaleImage.getRGB(indexMapping[i][j]-1, i));
    }

    private int CV(int i, int j) {
	    if(indexMapping[i][j] == 0 || indexMapping[i][j] == currentWidth-1){
	        return 0;
        }

        if (indexMapping[i][j] < currentWidth - 1) {
            //System.out.println("h : " + indexMapping[i][j] + "\n " + i + " " + j + "\n"+(currentWidth - 1));
            //System.out.println("chiennasse");
            return Math.abs(greyscaleImage.getRGB(indexMapping[i][j] + 1, i) - greyscaleImage.getRGB(indexMapping[i][j] - 1, i));
        }else{
            int a = greyscaleImage.getRGB(indexMapping[i][j] - 1, i);
            return a;
        }
    }

    private int CR(int i, int j) {
        if(indexMapping[i][j] == 0 || indexMapping[i][j] == currentWidth-1){
            return 0;
        }
        if (indexMapping[i][j] < currentWidth - 1) {
            return Math.abs(greyscaleImage.getRGB(indexMapping[i][j] + 1, i) - greyscaleImage.getRGB(indexMapping[i][j] - 1, i)) +
                    Math.abs(greyscaleImage.getRGB(indexMapping[i][j], i - 1) - greyscaleImage.getRGB(indexMapping[i][j] + 1, i));
        }else{
            return Math.abs(greyscaleImage.getRGB(indexMapping[i][j] - 1, i)) +
                    Math.abs(greyscaleImage.getRGB(indexMapping[i][j], i - 1));
        }
    }

    private void updateIndexMappingRemove(int i, int j){
	    for (int l = 0; l < currentWidth - 1; l++) {
	        if(l == j){
                for (int k = j; k < currentWidth - 1; k++) {
                    indexMapping[i][k] = indexMapping[i][k+1];
                }
                break;
	        }
	    }
    }

    private void updateIndexMappingAdd(int i, int j){

    }



    /**
     * Helper static method that returns the index of the minimum entry
     * in the array given as argument
     *
     * @param arr
     * @return
     */
    private static int findMinIndex(long[] arr){
        long min = arr[0];
        int j = 0;
        for (int i = 1; i < arr.length; i++) {
            if (min > arr[i]) {
                min = arr[i];
                j = i;
            }
        }
        return j;
    }

    private void backTrack(long[][] costMatrix, long[][] energyMatrix){
	    int minIndex = findMinIndex(costMatrix[costMatrix.length-1]);
	    updateIndexMappingRemove(0, minIndex);

        for (int i = 1; i < inHeight; i++) {
            if(costMatrix[i][minIndex] == energyMatrix[i][minIndex] + costMatrix[i-1][minIndex] + CV(i,minIndex)){

            }else if(costMatrix[i][minIndex] == energyMatrix[i][minIndex] + costMatrix[i-1][minIndex-1] + CL(i,minIndex)){
                minIndex = minIndex - 1;
            }else {
                minIndex = minIndex + 1;
            }
            updateIndexMappingRemove(i, minIndex);
        }
    }


    private void removeOneSeam(){
        long[][] energyMatrix = calculateEnergyMatrix();
        long[][] costMatrix = calculateCostMatrix(energyMatrix);

        backTrack(costMatrix, energyMatrix);
    }

    private void addOneSeam(){
        // TODO
    }

	public boolean[][] getMaskAfterSeamCarving() {
		// TODO: Implement this method, remove the exception.
		// This method should return the mask of the resize image after seam carving. Meaning,
		// after applying Seam Carving on the input image, getMaskAfterSeamCarving() will return
		// a mask, with the same dimensions as the resized image, where the mask values match the
		// original mask values for the corresponding pixels.
		// HINT:
		// Once you remove (replicate) the chosen seams from the input image, you need to also
		// remove (replicate) the matching entries from the mask as well.
        boolean[][] output = new boolean[outHeight][outWidth];
        for (int i = 0; i < outHeight; i++) {
            for (int j = 0; j < outWidth; j++) {
                System.out.println("index mapping["+i+"]["+j+"] = " + indexMapping[i][j]);
                output[i][j] = imageMask[i][indexMapping[i][j]];
            }
        }
        return output;
	}

	private BufferedImage imageFromIndexMapping(){
        BufferedImage ans = newEmptyImage(outWidth, outHeight);

        for (int i = 0; i < outWidth; i++) {
            for (int j = 0; j < outHeight; j++) {
                ans.setRGB(i, j, workingImage.getRGB(i, indexMapping[i][j]));
            }
        }
        return ans;
    }

}
