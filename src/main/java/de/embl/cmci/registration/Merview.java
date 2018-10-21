package de.embl.cmci.registration;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Label;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Comparable;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Iterator;
import ij.WindowManager;
import ij.plugin.Slicer;
import ij.process.ImageProcessor;
import ij.plugin.ImageCalculator;
import ij.plugin.ZProjector;

import java.lang.String;

public class Merview implements PlugIn  {
	private MultiStackReg_ msPlugin = new MultiStackReg_();
	private Slicer reslice = new Slicer();
	private ImagePlus srcImg;
	private ImagePlus tgtImg;	
	private int Action;
	private int Iterations;
	private ImageCalculator ic = new ImageCalculator();
	
	//Zac Agius
	//100702500
	//7/10/18

	public void run (final String arg) {	
		if (dialog() == -1) {
			IJ.error("Error selecting files");
			return;
		}
		
		//MSR perameters
		//we wan to register one stack to the other
		msPlugin.setTwoStackAlign(true); 
		//set the images to register
		msPlugin.setSrcImg(srcImg);
		msPlugin.setTgtImg(tgtImg);
		//what to do with the iamges
		msPlugin.setSrcAction("Use as Reference");
		msPlugin.setTgtAction("Align to First Stack");
		//dont need to save the transformations as it would be overwritten anyway through the iterations
		//this should be setup as a seperate function to allow adding up and saving it as a total tranformation
		msPlugin.setSaveTransform(false);
		msPlugin.setTransformation(Action);

		//set up FOM
		//to check if aligned
		ImagePlus FOM1 = null;
		ImagePlus FOM2 = null;
		//get the difference between the two stacks at the start
		FOM1 = ic.run("Difference create stack", srcImg, tgtImg);
		//Iteration will limit the amount of times that the function will go through,
		//this can just be set to a high amount when wanting full registation
		//however is included for the purposes of testing
		for (int i = Iterations;i > 0; i--) {
			//register top
			//As the way that the images are rotated to allow them to be aligned in different axis
			//alters them, it is required to re-set them as the images that are to be aligned.
			msPlugin.setSrcImg(srcImg);
			msPlugin.setTgtImg(tgtImg);
			//actual register the images
			registerdirectionforloop(Iterations);
			//make sure that the image that is rotated are the images that the plugin is using
			//sometimes they are not
			srcImg = msPlugin.getSrcImg();
			tgtImg = msPlugin.getTgtImg();
			//rotates both images to get the side view
			rotatebothdown();
			
			//register front
			//set images after they have been rotated
			msPlugin.setSrcImg(srcImg);
			msPlugin.setTgtImg(tgtImg);
			//register
			registerdirectionforloop(Iterations);
			// set as register images
			srcImg = msPlugin.getSrcImg();
			tgtImg = msPlugin.getTgtImg();
			//rotate to the other side of the image, done the top, done the front now do the right
			//move it back to the top view
			rotatebothdown();
			//rotate 90 left
			rotatebothleft();
			//rotate down again
			rotatebothdown();
	
			//register side
			msPlugin.setSrcImg(srcImg);
			msPlugin.setTgtImg(tgtImg);
			registerdirectionforloop(Iterations);
			srcImg = msPlugin.getSrcImg();
			tgtImg = msPlugin.getTgtImg();
			//back to top view
			rotatebothdown();
			//go back to the original position
			//this could just use a single rotate 90 right
			rotatebothleft();
			rotatebothleft();
			rotatebothleft();
			//rotate back to top for next loop
			
			//make the previosly created FOM the second and create a new FOM
			FOM2 = FOM1;
			FOM1 = ic.run("Difference create stack", srcImg, tgtImg);
			//if both FOM are equal then can stop
			if (areequal(FOM1, FOM2)) {
				break;
			}
		}
		
		//show the final images
		srcImg.show();
		srcImg.draw();
		tgtImg.show();
		tgtImg.draw();
	
	}

	//created by Zac Agius 100702500
	//checks if both images are equal
	private boolean areequal(ImagePlus img1, ImagePlus img2) {
		//sometimes the images are slightly unequal and will always try to slightly rotate causing to alway be unequal
		//max equalness is the tolerance of the image to be equal
		int maxequalness = 2;
		//get the value of the image as a double
		double imDoub1 = valueofimage(img1);
		double imDoub2 = valueofimage(img2);
		//if the values are actual equal then return true
		if (imDoub1 == imDoub2) {
			return true;
		}
		//A test to see the value of an image 
		//else {
		//	IJ.error("Are not equal", String.valueOf(valueofimage(img2)) + " <Img 1:Img 2> " + String.valueOf(valueofimage(img1)));
		//}
		//if they are both within the maxequalness tolerance then also return true
		if (imDoub1 <= maxequalness && imDoub2 <= maxequalness) {
			//if close to equal
			return true;
		}
		return false;
	}
	
	private double valueofimage(ImagePlus img1) {
		double value = 0.0;
		//zproject will get the average value of in the Z-direction
		ZProjector Zproj = new ZProjector(img1);
		Zproj.setMethod(ZProjector.AVG_METHOD);
		Zproj.doProjection();
		ImagePlus img2D = Zproj.getProjection();
		ImageProcessor img2Dprocessor = img2D.getProcessor();
		
		//image after zproject is 2d
		//get each x value in a row
		//add to value
		for (int y = 0; y < img2Dprocessor.getHeight() -1; y++) {
			for (int x = 0; x < img2Dprocessor.getWidth() - 1; x++) {
				value += img2Dprocessor.getPixelValue(x, y);
			}
		}
		//div by total pixel count
		value = value / img2Dprocessor.getPixelCount();
		return value;
	}

	//rotates to a front view from a top view
	private void rotatebothdown() {
		
		srcImg = reslice.reslice(srcImg);
		tgtImg = reslice.reslice(tgtImg);
		//IJ.run(srcImg, "Reslice [/]...", "output=4.500 start=Top avoid");
		//IJ.run(tgtImg, "Reslice [/]...", "output=4.500 start=Top avoid");
		IJ.wait(1000);
	}
	//rotate the top of the image
	private void rotatebothleft() {
		IJ.doCommand(srcImg, "Rotate 90 Degrees Left");
		IJ.doCommand(tgtImg, "Rotate 90 Degrees Left");
		IJ.wait(1000);
	}
	
	//register using the maxiterations loop
	//same as main loop
	private void registerdirectionforloop(int iter) {
		ImagePlus FOM1 = null;
		ImagePlus FOM2 = null;
		FOM1 = ic.run("Difference create stack", srcImg, tgtImg);
		for (int i = iter;i > 0; i--) {
			msPlugin.processDirectives(srcImg,false); //Align the current stack
			srcImg = msPlugin.getSrcImg();
			tgtImg = msPlugin.getTgtImg();
			FOM2 = FOM1;
			FOM1 = ic.run("Difference create stack", srcImg, tgtImg);
			if (areequal(FOM1, FOM2)) {
				break;
			}
			//were some errors if there was not wait
			//could be lowered
			IJ.wait(1000);
		}
	}
	
	private int dialog() {
		//image list of registraion valid files
		final ImagePlus[] admissibleImageList = msPlugin.createAdmissibleImageList();
		final String[] sourceNames = new String[1+admissibleImageList.length];
		sourceNames[0]="None";
		for (int k = 0; (k < admissibleImageList.length); k++) {
			sourceNames[k+1]=admissibleImageList[k].getTitle();
		}
		//type of registraion avialble
		final String[] transformationItem = {
				"Translation",
				"Rigid Body",
				"Scaled Rotation",
				"Affine"
					//"Load Transformation File"
			};
		
		//start dilog box
		GenericDialog gd = new GenericDialog("MerviewReg");
		gd.addChoice("Stack_1: Use as Reference", sourceNames, admissibleImageList[0].getTitle());
		gd.addChoice("Stack_2: Align to", sourceNames, admissibleImageList[1].getTitle());
		gd.addChoice("Transformation:", transformationItem, "Rigid Body");
		gd.addNumericField("Iterations:", 5, 0);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return -1;
		}
		
		
		//Assume both images are top view
		
		//set choices as values
		
		int tmpIndex=gd.getNextChoiceIndex();
		srcImg=null;
		if (tmpIndex > 0){
			srcImg = admissibleImageList[tmpIndex-1];
		}
		
		tmpIndex=gd.getNextChoiceIndex();
		tgtImg=null;
		if (tmpIndex > 0){
			tgtImg = admissibleImageList[tmpIndex-1];
		}
		Action=gd.getNextChoiceIndex();
		Iterations = (int) gd.getNextNumber();
		
		
		
		return 0;
	}
}