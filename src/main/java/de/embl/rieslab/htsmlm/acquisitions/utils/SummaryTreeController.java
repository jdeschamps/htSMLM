package de.embl.rieslab.htsmlm.acquisitions.utils;

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.embl.rieslab.emu.controller.SystemController;
import de.embl.rieslab.htsmlm.AcquisitionPanel;
import de.embl.rieslab.htsmlm.MainFramehtSMLM;
import de.embl.rieslab.htsmlm.acquisitions.AcquisitionController;

public class SummaryTreeController {
	
	private final SystemController systemController_;
	private final AcquisitionController acquisitionController_;
	private final MainFramehtSMLM mainFrame_;
	private final AcquisitionPanel acquisitionPanel_;
	private final JToggleButton button_;
	private boolean showSummaryTree_;
	private JFrame summaryFrame_;
	
	public SummaryTreeController(MainFramehtSMLM mainFrame, 
								 SystemController systemController, 
								 AcquisitionController acquisitionController, 
								 AcquisitionPanel acquisitionPanel,
								 JToggleButton button) {
		systemController_ = systemController;
		acquisitionController_ = acquisitionController;
		mainFrame_ = mainFrame;		
		acquisitionPanel_ = acquisitionPanel;
		button_ = button;
		showSummaryTree_ = false;
		
		// add listener to main window in order to sync position
		addMainFrameListener();
		
		// add listener to the tabs to hide summary panel when acquisition is not selected
		addTabListener();
	}
	
	private void addMainFrameListener() {
		/*
		 * Sync the position of the summary frame with that of the main window.
		 */
		mainFrame_.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent evt) {
				Point newLoc = getSummaryButtonLocation();
				if (summaryFrame_ != null) {
					summaryFrame_.setLocation(newLoc);
					summaryFrame_.toFront();
					summaryFrame_.repaint();
				}
            }
          });
		
		mainFrame_.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
				if (summaryFrame_ != null) {
					summaryFrame_.setAlwaysOnTop(false);
				}
            }

            @Override
            public void windowActivated(WindowEvent e) {
				if (summaryFrame_ != null) {
					summaryFrame_.setAlwaysOnTop(true);
				}
            }
            
            @Override
            public void windowIconified(WindowEvent e) {
            	if ((mainFrame_.getExtendedState() & Frame.ICONIFIED) != 0) {
					if (summaryFrame_ != null) {
						summaryFrame_.setAlwaysOnTop(false);
						summaryFrame_.toBack();
					}
                  }
             }
        });
	}
	
	private void addTabListener() {
		mainFrame_.getTab().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				// get selected index
	            int selectedIndex = mainFrame_.getTab().getSelectedIndex();
	            
	            // if the selected index is different from the acquisition tab
	            if(selectedIndex != mainFrame_.getTab().indexOfComponent(acquisitionPanel_)) {
	            	hideSummary();
	            }
			}
	    });
	}
	
	private JPanel getExperimentPanel() {
		return ExperimentTreeSummary.getExperimentPanel(systemController_, acquisitionController_.getExperiment());
	}
	
	public Point getSummaryButtonLocation(){
		Point newLoc = button_.getLocation();

		newLoc.x += mainFrame_.getAcquisitionPanelLocation().getX()+100;
		newLoc.y += mainFrame_.getAcquisitionPanelLocation().getY()+48;
		
		return newLoc;
	}
	
	/**
	 * Show or hide experiment summary.
	 * 
	 * @param b True if should show, false otherwise.
	 */
	public void showSummary(boolean b){
		if(b){
			showSummaryTree_ = true;
			summaryFrame_ = new JFrame("Acquisitions summary");
			summaryFrame_.setLocation(getSummaryButtonLocation());
			summaryFrame_.setUndecorated(true);
			summaryFrame_.setContentPane(getExperimentPanel());
			summaryFrame_.pack();
			summaryFrame_.setVisible(true);
			button_.setText("<<");
		} else {
			showSummaryTree_ = false;
			if(summaryFrame_ != null){
				summaryFrame_.dispose();
			}
			button_.setText(">>");
			
			if(button_.isSelected()) {
				button_.setSelected(false);
			}
		}
	}
	
	/**
	 * Update the summary panel.
	 */
	public void updateSummary() {
		if (showSummaryTree_) {
			showSummary(false); // this is a quick fix, surely there is a better way
			showSummary(true);
		}
	}
	
	/**
	 * Hide the summary panel. Should be called if the tab is unselected.
	 */
	public void hideSummary() {
		showSummary(false);
	}

	/**
	 * Dispose of the summary tree window.
	 */
	public void shutDown() {
		summaryFrame_.dispose();		
	}

}
