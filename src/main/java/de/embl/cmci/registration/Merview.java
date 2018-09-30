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

import java.lang.String;

public class Merview implements PlugIn  {
	private MultiStackReg_ msPlugin = new MultiStackReg_();
	private Slicer reslice = new Slicer();
	private ImagePlus srcImg;
	private ImagePlus tgtImg;	
	private int Action;
	private int Iterations;
	private ImageCalculator ic = new ImageCalculator();
	

	public void run (final String arg) {
		dialog();
		
		msPlugin.setTwoStackAlign(true);      
		msPlugin.setSrcImg(srcImg);
		msPlugin.setTgtImg(tgtImg);
		msPlugin.setSrcAction("Use as Reference");
		msPlugin.setTgtAction("Align to First Stack");
		msPlugin.setSaveTransform(false);
		msPlugin.setTransformation(Action);
		
		for (int i = Iterations;i >= 0; i--) {
			msPlugin.processDirectives(srcImg,false); //Align the current stack
		}
		
		
		ImagePlus Aeoi1 = null;
		ImagePlus Aeoi2 = null;
		Aeoi1 = ic.run("Difference create", srcImg, tgtImg);
		do {
			registerdirection();
			rotatebothstacks();
			
			registerdirection();
			rotatebothstacks();
						
			registerdirection();
			rotatebothstacks();

			//Calculate the difference between the images
			Aeoi2 = Aeoi1;
			Aeoi1 = ic.run("Difference create", srcImg, tgtImg);
		} while (!Aeoi1.equals(Aeoi2));
		//If the difference between two images over two iterations then stop
	
	}
	private void rotatebothstacks() {
		srcImg = reslice.reslice(srcImg);
		tgtImg = reslice.reslice(tgtImg);
	}
	
	private void registerdirection() {
		ImagePlus Beoi1 = null;
		ImagePlus Beoi2 = null;
		Beoi1 = ic.run("Difference create", srcImg, tgtImg);
		do {
			msPlugin.processDirectives(srcImg,false); //Align the current stack
			Beoi2 = Beoi1;
			Beoi1 = ic.run("Difference create", srcImg, tgtImg);
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

	//check to stop the registration
	private void CheckIteration()
	{
		while(Aeoi2 != Aeoi1)
		{
			msPlugin.run(); //continue running the loop 
		}
		else if(Aeoi2 == Aeoi1)
		{
			; //output the image result
		}
	}
}
