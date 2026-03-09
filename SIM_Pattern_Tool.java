import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.frame.*;

import java.awt.*;
import java.awt.event.*;

public class SIM_Pattern_Tool extends PlugInFrame implements ActionListener {

    ImagePlus imp;
    ImageProcessor original;

    TextField angleField;
    TextField phaseField;
    TextField freqField;
    TextField modField;

    Button updateButton;
    Button exportCurrentButton;
    Button exportSIMButton;

    double pixelSize = 0.1;

    public SIM_Pattern_Tool() {

        super("SIM Pattern Tool");

        imp = IJ.getImage();
        original = imp.getProcessor().duplicate();

        setLayout(new GridLayout(7,2));

        add(new Label("Angle (0–180)"));
        angleField = new TextField("0");
        add(angleField);

        add(new Label("Phase (0–360)"));
        phaseField = new TextField("0");
        add(phaseField);

        add(new Label("Spatial Frequency (0.1–10)"));
        freqField = new TextField("3.5");
        add(freqField);

        add(new Label("Modulation (0–1)"));
        modField = new TextField("0.3");
        add(modField);

        updateButton = new Button("Update Pattern");
        exportCurrentButton = new Button("Export Current Pattern");
        exportSIMButton = new Button("SIM Test Patterns (3 angles x 3 phases)");

        add(updateButton);
        add(exportCurrentButton);
        add(exportSIMButton);

        updateButton.addActionListener(this);
        exportCurrentButton.addActionListener(this);
        exportSIMButton.addActionListener(this);

        pack();
        setVisible(true);

        updatePattern();
    }

    double clamp(double val,double min,double max){
        return Math.max(min,Math.min(max,val));
    }

    void updatePattern(){

        int width = imp.getWidth();
        int height = imp.getHeight();

        double angle = clamp(parse(angleField.getText()),0,180);
        double phase = clamp(parse(phaseField.getText()),0,360);
        double spatialFreq = clamp(parse(freqField.getText()),0.1,10);
        double modulation = clamp(parse(modField.getText()),0,1);

        double theta = Math.toRadians(angle);
        double phi = Math.toRadians(phase);

        double kx = spatialFreq*Math.cos(theta);
        double ky = spatialFreq*Math.sin(theta);

        FloatProcessor pattern = new FloatProcessor(width,height);

        for(int y=0;y<height;y++){
            for(int x=0;x<width;x++){

                double X = (x-width/2.0)*pixelSize;
                double Y = (y-height/2.0)*pixelSize;

                double val = 0.5*(1 + modulation*Math.cos(
                        2*Math.PI*(kx*X + ky*Y) + phi));

                pattern.setf(x,y,(float)val);
            }
        }

        ImageProcessor overlay = original.duplicate();
        overlay.copyBits(pattern,0,0,Blitter.ADD);

        imp.setProcessor(overlay);
        imp.updateAndDraw();
    }

    double parse(String s){
        try{
            return Double.parseDouble(s);
        }catch(Exception e){
            return 0;
        }
    }

    public void actionPerformed(ActionEvent e){

        if(e.getSource()==updateButton){
            updatePattern();
            return;
        }

        int width = imp.getWidth();
        int height = imp.getHeight();

        double angle = parse(angleField.getText());
        double phase = parse(phaseField.getText());
        double spatialFreq = parse(freqField.getText());
        double modulation = parse(modField.getText());

        if(e.getSource()==exportCurrentButton){

            double theta = Math.toRadians(angle);
            double phi = Math.toRadians(phase);

            double kx = spatialFreq*Math.cos(theta);
            double ky = spatialFreq*Math.sin(theta);

            FloatProcessor pattern = new FloatProcessor(width,height);

            for(int y=0;y<height;y++){
                for(int x=0;x<width;x++){

                    double X = (x-width/2.0)*pixelSize;
                    double Y = (y-height/2.0)*pixelSize;

                    double val = 0.5*(1 + modulation*Math.cos(
                            2*Math.PI*(kx*X + ky*Y) + phi));

                    pattern.setf(x,y,(float)val);
                }
            }

            new ImagePlus("Current SIM Pattern",pattern).show();
        }

        if(e.getSource()==exportSIMButton){

            double[] angles = {0,60,120};
            double[] phases = {0,120,240};

            ImageStack stack = new ImageStack(width,height);

            for(double a:angles){

                double theta = Math.toRadians(a);

                for(double p:phases){

                    double phi = Math.toRadians(p);

                    FloatProcessor pattern = new FloatProcessor(width,height);

                    double kx = spatialFreq*Math.cos(theta);
                    double ky = spatialFreq*Math.sin(theta);

                    for(int y=0;y<height;y++){
                        for(int x=0;x<width;x++){

                            double X = (x-width/2.0)*pixelSize;
                            double Y = (y-height/2.0)*pixelSize;

                            double val = 0.5*(1 + modulation*Math.cos(
                                    2*Math.PI*(kx*X + ky*Y) + phi));

                            pattern.setf(x,y,(float)val);
                        }
                    }

                    stack.addSlice("angle"+a+"_phase"+p,pattern);
                }
            }

            new ImagePlus("SIM Test Patterns (3 angles x 3 phases)",stack).show();
        }
    }

    public void run(String arg){}
}
