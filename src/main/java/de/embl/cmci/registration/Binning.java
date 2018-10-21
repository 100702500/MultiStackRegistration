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

public class Binning implements PlugIn  {
	private MultiStackReg_ msPlugin = new MultiStackReg_();
	private ImagePlus srcImg;
	private ImagePlus tgtImg;
	
	//Catherine Nguyen
	//100042293
	//9/10/18

	public void run (final String arg) {
		dialog();
		
		msPlugin.setSrcImg(srcImg);
		msPlugin.setTgtImg(tgtImg);
	}
	
		private void dialog() {
			final ImagePlus[] admissibleImageList = msPlugin.createAdmissibleImageList();
			final String[] sourceNames = new String[1+admissibleImageList.length];
			sourceNames[0]="None";
			for (int k = 0; (k < admissibleImageList.length); k++) {
				sourceNames[k+1]=admissibleImageList[k].getTitle();
			}
			
			GenericDialog gd = new GenericDialog("Binning - increase pixel dimensions");
			gd.addChoice("Stack_1: Target image", sourceNames, admissibleImageList[0].getTitle());
			gd.addChoice("Stack_2: Bin to", sourceNames, admissibleImageList[1].getTitle());
			gd.showDialog();
			if (gd.wasCanceled()) {
				return;
			}
			
			
	}
}