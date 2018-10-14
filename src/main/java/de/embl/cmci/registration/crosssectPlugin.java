package de.embl.cmci.registration;

import java.awt.AWTEvent;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.Duplicator;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilterRunner;


public class crosssectPlugin implements PlugIn   {
	
	private MultiStackReg_ msPlugin = new MultiStackReg_();
	private ImagePlus imp1;
	private ImagePlus imp2;
	private int min;
	private int max;
	private ImagePlus ResultImage;
	private ImageCalculator ic = new ImageCalculator();
	private ImagePlus[] admissibleImageList;
	private String[] sourceNames;

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		showdialog();
		function();
		ResultImage.draw();
		ResultImage.show();
	}
	
	private void Threshold() {
		
		String macro = "code=[if(v<=" + min + "){v=0;}else if(v>="+ max +"){v=0;}else{v=v;}] stack";
		IJ.run(ResultImage, "Macro...", macro);
		ResultImage.draw();
		/* OLD implementations Kept incase need of use
		
		//needs to be temp and revert once returned value
		//still needs to display so that it can be used for preview
		//String macro = "if(v<=" + min + "){v=0;}else if(v>="+ max +"){v=0;}else{v=v;}";
		//for (int i=1 ; i<=imp1.getImageStackSize() ; i++) {
		//	ImageMath.applyMacro(imp1.getImageStack().getProcessor(i), ( macro ), false);
		//}
		//ImageMath.applyMacro(imp1.getProcessor(), macro, true);
		//ImageProcessor temp = imp1.getProcessor();
		//temp.applyMacro(macro);
		//temp.setThreshold(min, max, 0);
		//ResultImage.draw();
		 	val = "code=[if(v<=" + minval + "){v=0;}else if(v>="+ maxval +"){v=0;}else{v=v;}] stack";
			run("Macro...", val);
			run("Make Binary", "method=Huang background=Default calculate black");
			run("Divide...", "value=255 stack");
			
			
		 */
	}
	
	private void function() {
		Threshold();
		ImagePlus temp = new Duplicator().run(ResultImage);
		
		IJ.run(temp, "Make Binary", "method=Huang background=Default calculate black");
		
		IJ.run(temp, "Divide...", "value=255 stack");
		temp.draw();
		temp.show();
		ResultImage.draw();
		ResultImage.show();
		ResultImage.changes = false;
		ResultImage.close();
		ResultImage = ic.run("Multiply create 32-bit stack", imp2, temp);
		temp.setTitle("SHOULD BE DELETED");
		temp.changes = false;
		temp.close();
		
		//ImagePlus tempres = new ImagePlus("Binary image convert", Threshold());
		//IJ.run(tempres, "Divide...", "value=255 stack");
		//ImagePlus result = ic.run("Multiply create 32-bit stack", imp2, tempres);
		//return result;
	}
	/*
	private void dialog() {
		//list is using an object of the registration class, should not
		admissibleImageList = msPlugin.createAdmissibleImageList();
		sourceNames = new String[1+admissibleImageList.length];
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
	*/
	private void showdialog() {
		//list is using an object of the registration class, should not
		admissibleImageList = msPlugin.createAdmissibleImageList();
		sourceNames = new String[1+admissibleImageList.length];
		sourceNames[0]="None";
		for (int k = 0; (k < admissibleImageList.length); k++) {
			sourceNames[k+1]=admissibleImageList[k].getTitle();
		}
	
		GenericDialog gd = new GenericDialog("Create Cross-Sect");
		gd.addChoice("Image 1: Apply Range to", sourceNames, admissibleImageList[0].getTitle());
		Choice imChoice = (Choice)(gd.getChoices().get(0));
		imChoice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int tmpIndex=imChoice.getSelectedIndex();
				imp1=null;
				if (tmpIndex > 0){
					imp1 = admissibleImageList[tmpIndex-1];
				}
			}
		});
		gd.addSlider("Min", 0, 1000, 0);
		Scrollbar minslider = (Scrollbar)(gd.getSliders().get(0));
		minslider.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent ae) {
				min = ae.getValue();
			}
		});
		gd.addSlider("Max", 0, 1000, 0);
		Scrollbar maxslider = (Scrollbar)(gd.getSliders().get(1));
		maxslider.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent ae) {
				max = ae.getValue();
			}
		});
		gd.addChoice("Image 2: Get Data From", sourceNames, admissibleImageList[1].getTitle());
		Choice im2Choice = (Choice)(gd.getChoices().get(1));
		im2Choice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int tmpIndex=im2Choice.getSelectedIndex();
				imp2=null;
				if (tmpIndex > 0){
					imp2 = admissibleImageList[tmpIndex-1];
				}
			}
		});
		gd.addCheckbox("Change (ACT AS BUTTON)", false);
		Checkbox c = (Checkbox) gd.getCheckboxes().get(0);
        c.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				IJ.error("Action Fired");
				int tmpIndex=imChoice.getSelectedIndex();
				imp1=null;
				if (tmpIndex > 0){
					imp1 = admissibleImageList[tmpIndex-1];
				}
				tmpIndex=im2Choice.getSelectedIndex();
				imp2=null;
				if (tmpIndex > 0){
					imp2 = admissibleImageList[tmpIndex-1];
				}
				
				
				
				min = minslider.getValue();
				max = maxslider.getValue();
				cpyimp1toresults();
				Threshold();
			}
        });
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
		
		cpyimp1toresults();
	
	}
	
	private void cpyimp1toresults() {
		if (ResultImage != null) {
			ResultImage.changes = false;
			ResultImage.close();
		}
		ResultImage = new Duplicator().run(imp1);
		ResultImage.show();
		ResultImage.setTitle("Result of " + imp1.getTitle());
		IJ.selectWindow(ResultImage.getTitle());
	}
}
