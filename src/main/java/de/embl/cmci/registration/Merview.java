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
		dialog();
		
		
		msPlugin.setTwoStackAlign(true);      
		msPlugin.setSrcImg(srcImg);
		msPlugin.setTgtImg(tgtImg);
		msPlugin.setSrcAction("Use as Reference");
		msPlugin.setTgtAction("Align to First Stack");
		msPlugin.setSaveTransform(false);
		msPlugin.setTransformation(Action);

		
		ImagePlus FOM1 = null;
		ImagePlus FOM2 = null;
		FOM1 = ic.run("Difference create stack", srcImg, tgtImg);
		for (int i = Iterations;i > 0; i--) {
			//register top

			msPlugin.setSrcImg(srcImg);
			msPlugin.setTgtImg(tgtImg);
			registerdirectionforloop(Iterations);
			srcImg = msPlugin.getSrcImg();
			tgtImg = msPlugin.getTgtImg();
			rotatebothdown();
			
			//register front
			msPlugin.setSrcImg(srcImg);
			msPlugin.setTgtImg(tgtImg);
			registerdirectionforloop(Iterations);
			srcImg = msPlugin.getSrcImg();
			tgtImg = msPlugin.getTgtImg();
			rotatebothdown();
			rotatebothleft();
			rotatebothdown();
	
			//register side
			msPlugin.setSrcImg(srcImg);
			msPlugin.setTgtImg(tgtImg);
			registerdirectionforloop(Iterations);
			srcImg = msPlugin.getSrcImg();
			tgtImg = msPlugin.getTgtImg();
			rotatebothdown();
			rotatebothleft();
			rotatebothleft();
			rotatebothleft();
			//rotate back to top for next loop
			FOM2 = FOM1;
			FOM1 = ic.run("Difference create stack", srcImg, tgtImg);
			if (areequal(FOM1, FOM2)) {
				break;
			}
		}
		
		srcImg.show();
		srcImg.draw();
		tgtImg.show();
		tgtImg.draw();
		
		/*
		ImagePlus Aeoi1 = null;
		ImagePlus Aeoi2 = null;
		Aeoi1 = ic.run("Difference create", srcImg, tgtImg);
		do {
			registerdirection();
			rotatebothstacks(1);
			
			registerdirection();
			rotatebothstacks(1);
						
			registerdirection();
			rotatebothstacks(1);

			//Calculate the difference between the images
			Aeoi2 = Aeoi1;
			Aeoi1 = ic.run("Difference create", srcImg, tgtImg);
		} while (!Aeoi1.equals(Aeoi2));
		//If the difference between two images over two iterations then stop
		 */
	
	}

	private boolean areequal(ImagePlus img1, ImagePlus img2) {
		int maxequalness = 2;
		double imDoub1 = valueofimage(img1);
		double imDoub2 = valueofimage(img2);
		if (imDoub1 == imDoub2) {
			return true;
		}
		//else {
		//	IJ.error("Are not equal", String.valueOf(valueofimage(img2)) + " <Img 1:Img 2> " + String.valueOf(valueofimage(img1)));
		//}
		if (imDoub1 <= maxequalness && imDoub2 <= maxequalness) {
			//if close to equal
			return true;
		}
		return false;
	}
	
	private double valueofimage(ImagePlus img1) {
		double value = 0.0;
		ZProjector Zproj = new ZProjector(img1);
		Zproj.setMethod(ZProjector.AVG_METHOD);
		Zproj.doProjection();
		ImagePlus img2D = Zproj.getProjection();
		ImageProcessor img2Dprocessor = img2D.getProcessor();
		
		for (int y = 0; y < img2Dprocessor.getHeight() -1; y++) {
			for (int x = 0; x < img2Dprocessor.getWidth() - 1; x++) {
				value += img2Dprocessor.getPixelValue(x, y);
			}
		}
		value = value / img2Dprocessor.getPixelCount();
		return value;
	}

	//get side view
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
	//Temp Method
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
			IJ.wait(1000);
		}
	}
	
	private void registerdirection() {
		ImagePlus Beoi1 = null;
		ImagePlus Beoi2 = null;
		Beoi1 = ic.run("Difference create stack", srcImg, tgtImg);
		do {
			msPlugin.processDirectives(srcImg,false); //Align the current stack
			Beoi2 = Beoi1;
			Beoi1 = ic.run("Difference create stack", srcImg, tgtImg);
		} while (!Beoi1.equals(Beoi2));
	}
	
	private void dialog() {
		final ImagePlus[] admissibleImageList = msPlugin.createAdmissibleImageList();
		final String[] sourceNames = new String[1+admissibleImageList.length];
		sourceNames[0]="None";
		for (int k = 0; (k < admissibleImageList.length); k++) {
			sourceNames[k+1]=admissibleImageList[k].getTitle();
		}
		final String[] transformationItem = {
				"Translation",
				"Rigid Body",
				"Scaled Rotation",
				"Affine"
					//"Load Transformation File"
			};
		
		GenericDialog gd = new GenericDialog("MerviewReg");
		gd.addChoice("Stack_1: Use as Reference", sourceNames, admissibleImageList[0].getTitle());
		gd.addChoice("Stack_2: Align to", sourceNames, admissibleImageList[1].getTitle());
		gd.addChoice("Transformation:", transformationItem, "Rigid Body");
		gd.addNumericField("Iterations:", 5, 0);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		
		
		//Assume both images are top view
		
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
	}
}