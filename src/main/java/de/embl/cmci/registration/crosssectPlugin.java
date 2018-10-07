package de.embl.cmci.registration;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;


public class crosssectPlugin implements PlugIn   {
	
	private MultiStackReg_ msPlugin = new MultiStackReg_();
	private ImagePlus imp1;
	private ImagePlus imp2;
	private int min;
	private int max;
	private ImagePlus ResultImage;
	private ImageCalculator ic = new ImageCalculator();

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		dialog();
		ResultImage = function();
		ResultImage.draw();
		ResultImage.show();
	}
	
	private ImageProcessor Threshold() {
		//needs to be temp and revert once returned value
		//still needs to display so that it can be used for preview
		ImageProcessor temp = imp1.getProcessor();
		temp.setThreshold(min, max, 0);
		ResultImage.draw();
		/*
		 	val = "code=[if(v<=" + minval + "){v=0;}else if(v>="+ maxval +"){v=0;}else{v=v;}] stack";
			run("Macro...", val);
			run("Make Binary", "method=Huang background=Default calculate black");
			run("Divide...", "value=255 stack");
		 */
		return temp;
	}
	
	private ImagePlus function() {
		ImagePlus tempres = new ImagePlus("Binary image convert", Threshold());
		IJ.run(tempres, "Divide...", "value=255 stack");
		ImagePlus result = ic.run("Multiply create 32-bit stack", imp2, tempres);
		return result;
	}
	private void dialog() {
		//list is using an object of the registration class, should not
		final ImagePlus[] admissibleImageList = msPlugin.createAdmissibleImageList();
		final String[] sourceNames = new String[1+admissibleImageList.length];
		sourceNames[0]="None";
		for (int k = 0; (k < admissibleImageList.length); k++) {
			sourceNames[k+1]=admissibleImageList[k].getTitle();
		}
	
		GenericDialog gd = new GenericDialog("Create Cross-Sect");
		gd.addChoice("Image 1: Apply Range to", sourceNames, admissibleImageList[0].getTitle());
		gd.addNumericField("Min:", 0, 0);
		gd.addNumericField("Max:", 255, 0);
		gd.addChoice("Image 2: Get Data From", sourceNames, admissibleImageList[1].getTitle());
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		
		int tmpIndex=gd.getNextChoiceIndex();
		imp1=null;
		if (tmpIndex > 0){
			imp1 = admissibleImageList[tmpIndex-1];
		}
		tmpIndex=gd.getNextChoiceIndex();
		imp2=null;
		if (tmpIndex > 0){
			imp2 = admissibleImageList[tmpIndex-1];
		}
		min = (int) gd.getNextNumber();
		max = (int) gd.getNextNumber();

	
	}
}
