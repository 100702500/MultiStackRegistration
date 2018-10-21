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
		//if theres an error selecting the files show that and stop
		if (showdialoggetimages() == -1) {
			IJ.error("Error selecting file");
			return;
		}
		//if theres an error or cancled show that
		if (showdialog(imp1.getDisplayRangeMax()) == -1) {
			IJ.error("Error Cross secting");
			return;
		}
		
		//actual program function
		function();
		//show the result
		ResultImage.draw();
		ResultImage.show();
		IJ.selectWindow(ResultImage.getTitle());
	}
	
	
	//select the range of values to display
	private void Threshold() {
		//uses math>macro
		//if the pixel value <= inputted min then the pixel value is set to 0
		//same for the max
		//applys to whole stack
		String macro = "code=[if(v<=" + min + "){v=0;}else if(v>="+ max +"){v=0;}else{v=v;}] stack";
		//run the macro
		IJ.run(ResultImage, "Macro...", macro);
		//draw the result
		ResultImage.draw();
		IJ.selectWindow(ResultImage.getTitle());
	}
	
	private void function() {
		//apply the threshold range
		Threshold();
		//set the result of the threshold as a temp file
		ImagePlus temp = new Duplicator().run(ResultImage);
		//delete the result image
		ResultImage.draw();
		ResultImage.show();
		ResultImage.changes = false;
		ResultImage.close();
		
		//make the temp res image binary that is (0 or 255)
		//uses the Huang method
		IJ.run(temp, "Make Binary", "method=Huang background=Default calculate black");
		
		//makes the temp image either 1 or 0
		IJ.run(temp, "Divide...", "value=255 stack");
		//darw show temp
		//this shows that somthing has happened and is useful in testing
		temp.draw();
		temp.show();

		//get the value from the second image
		//by multipling the second with the temp
		//as the values are 0 or 1. 1 will keep the value from the second image, 0 will make it 0.
		//set this result as result image
		ResultImage = ic.run("Multiply create 32-bit stack", imp2, temp);
		//make sure that the temp is deleted
		temp.setTitle("SHOULD BE DELETED");
		temp.changes = false;
		temp.close();
		
	}
	
	private int showdialoggetimages() {
		//list is using an object of the registration class, should not
		//this gets a list of all images
			admissibleImageList = msPlugin.createAdmissibleImageList();
			sourceNames = new String[1+admissibleImageList.length];
			sourceNames[0]="None";
			for (int k = 0; (k < admissibleImageList.length); k++) {
				sourceNames[k+1]=admissibleImageList[k].getTitle();
			}
		//generate the dialog window
			GenericDialog gd = new GenericDialog("Select Images Cross-Sect");
			//name of choice, choice options, currently selected value
			gd.addChoice("Image 1: Apply Range to", sourceNames, admissibleImageList[0].getTitle());
			gd.addChoice("Image 2: Get Data From", sourceNames, admissibleImageList[1].getTitle());
			gd.showDialog();
			if (gd.wasCanceled()) {
				//if it failed or canceled then it should return somthing so that the main can stop
				return -1;
			}
			
			//sets the choices as variables
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
			return 0;
	}
	
	
	
	
	
	private int showdialog(double max1) {
		//list is using an object of the registration class, should not
		//same as above
		admissibleImageList = msPlugin.createAdmissibleImageList();
		sourceNames = new String[1+admissibleImageList.length];
		sourceNames[0]="None";
		for (int k = 0; (k < admissibleImageList.length); k++) {
			sourceNames[k+1]=admissibleImageList[k].getTitle();
		}
	
		GenericDialog gd = new GenericDialog("Create Cross-Sect");
		//the mix slider
		gd.addSlider("Min", 0, max1, 0);
		Scrollbar minslider = (Scrollbar)(gd.getSliders().get(0));
		//not actualy needed to have a listener
		minslider.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent ae) {
				min = ae.getValue();
			}
		});
		gd.addSlider("Max", 0, max1, 0);
		//not actualy needed to have a listener
		Scrollbar maxslider = (Scrollbar)(gd.getSliders().get(1));
		maxslider.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent ae) {
				max = ae.getValue();
			}
		});
		//the checkbox will activate the threshold using the current values
		//and display that as a preview
		//is possible to change to a button instead of a checkbox
		gd.addCheckbox("Change (ACT AS BUTTON)", false);
		Checkbox c = (Checkbox) gd.getCheckboxes().get(0);
        c.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				min = minslider.getValue();
				max = maxslider.getValue();
				cpyimp1toresults();
				Threshold();
				//makes the window come to the front so it is viewable
				IJ.selectWindow(ResultImage.getTitle());
			}
        });
        //if fail return it
		gd.showDialog();
		if (gd.wasCanceled()) {
			return -1;
		}
		
		//set choices as results
		min = (int) gd.getNextNumber();
		max = (int) gd.getNextNumber();
		
		//ResultImage is set to a copy of image1
		cpyimp1toresults();
		return 0;
	}
	
	//This copys image1 to a new resultvariable
	//this is done to make sure that the original images are not altered
	private void cpyimp1toresults() {
		if (ResultImage != null) {
			ResultImage.changes = false;
			ResultImage.close();
		}
		ResultImage = new Duplicator().run(imp1);
		ResultImage.show();
		ResultImage.setTitle("Preview of " + imp1.getTitle());
		IJ.selectWindow(ResultImage.getTitle());
	}
}
