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

import java.lang.String;

public class Merview implements PlugIn  {
	private MultiStackReg_ msPlugin = new MultiStackReg_();
	private Slicer reslice = new Slicer();
	private ImagePlus srcImg;
	private ImagePlus tgtImg;	
	private int Action;
	private int Iterations;
	

	public void run (final String arg) {
		dialog();
		
		msPlugin.setTwoStackAlign(true);      
		msPlugin.setSrcImg(srcImg);
		msPlugin.setTgtImg(tgtImg);
		msPlugin.setSrcAction("Use as Reference");
		msPlugin.setTgtAction("Align to First Stack");
		msPlugin.setSaveTransform(false);
		msPlugin.setTransformation(Action);
		
		
		ImagePlus srcImg2 = null;
		ImagePlus tgtImg2 = null;
		ImagePlus srcImg3 = null;
		ImagePlus tgtImg3 = null;
		
		//iterate over all angles
		for (int j = Iterations;j >= 0; j--) {
			//need to apply changes made in lower loop to original image stack.
			if (srcImg3 != null) {
				srcImg = srcImg3;
			}
			if (tgtImg3 != null) {
				tgtImg = tgtImg3;
			}
			for (int i = Iterations;i >= 0; i--) {
				msPlugin.processDirectives(srcImg,false); //Align the current stack
			}
	
			//rotate once
			
			srcImg2 = reslice.reslice(srcImg);
			tgtImg2 = reslice.reslice(tgtImg);
			msPlugin.setSrcImg(srcImg2);
			msPlugin.setTgtImg(tgtImg2);
			//register side
			for (int i = Iterations;i >= 0; i--) {
				msPlugin.processDirectives(srcImg,false); //Align the current stack
			}
			
			//rotate second time
			
			srcImg3 = reslice.reslice(srcImg2);
			tgtImg3 = reslice.reslice(tgtImg2);
			msPlugin.setSrcImg(srcImg3);
			msPlugin.setTgtImg(tgtImg3);
			//register second side
			for (int i = Iterations;i >= 0; i--) {
				msPlugin.processDirectives(srcImg,false); //Align the current stack
			}
		}
				
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