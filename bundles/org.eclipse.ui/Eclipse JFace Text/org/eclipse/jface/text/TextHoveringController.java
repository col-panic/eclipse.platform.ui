package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.util.Assert;



/**
 * A text hovering controller. The controller registers with the 
 * text widget's control as a <code>MouseTrackListener<code>. When
 * receiving a mouse hover event, it opens a popup window using the 
 * appropriate <code>ITextHover</code>  to initialize the window's 
 * display information. The controller closes the window if the mouse 
 * pointer leaves the area for which the display information has been computed.<p>
 */
class TextHoveringController extends MouseTrackAdapter {		
	
	
	/**
	 * The  window closer.
	 */
	class WindowCloser extends MouseTrackAdapter implements
		MouseListener, MouseMoveListener, ControlListener, KeyListener, FocusListener {
		
		Rectangle fCoveredArea;
		
		/**
		 * Creates a new window closer for the given area.
		 */
		public WindowCloser(Rectangle coveredArea) {
			fCoveredArea= coveredArea;
		}
		
		/**
		 * Starts watching whether the popup window must be closed.
		 */
		public void start() {
			StyledText text= fTextViewer.getTextWidget();
			text.addMouseListener(this);
			text.addMouseMoveListener(this);
			text.addMouseTrackListener(this);
			text.addControlListener(this);
			text.addKeyListener(this);
			text.addFocusListener(this);
		}
		
		/**
		 * Closes the popup window and stops watching.
		 */
		private void stop() {
			
			fHoverShell.setVisible(false);
			
			StyledText text= fTextViewer.getTextWidget();
			text.removeMouseListener(this);
			text.removeMouseMoveListener(this);
			text.removeMouseTrackListener(this);
			text.removeControlListener(this);
			text.removeKeyListener(this);
			text.removeFocusListener(this);
			
			install();
		}
		
		/*
		 * @see MouseMoveListener#mouseMove
		 */
		public void mouseMove(MouseEvent event) {
			if (!fCoveredArea.contains(event.x, event.y))
				stop();
		}
				
		/*
		 * @see MouseListener#mouseUp(MouseEvent)
		 */
		public void mouseUp(MouseEvent event) {
		}
		
		/*
		 * @see MouseListener#mouseDown(MouseEvent)
		 */
		public void mouseDown(MouseEvent event) {
			stop();
		}
		
		/*
		 * @see MouseListener#mouseDoubleClick(MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent event) {
			stop();
		}
		
		/*
		 * @see MouseTrackAdapter#mouseExit(MouseEvent)
		 */
		public void mouseExit(MouseEvent event) {
			stop();
		}
		
		/*
		 * @see ControlListener#controlResized(ControlEvent)
		 */
		public void controlResized(ControlEvent event) {
			stop();
		}
		
		/*
		 * @see ControlListener#controlMoved(ControlEvent)
		 */
		public void controlMoved(ControlEvent event) {
			stop();
		}
		
		/*
		 * @see KeyListener#keyReleased(KeyEvent)
		 */
		public void keyReleased(KeyEvent event) {
		}
		
		/*
		 * @see KeyListener#keyPressed(KeyEvent)
		 */
		public void keyPressed(KeyEvent event) {
			stop();
		}
		
		/*
		 * @see FocusListener#focusLost(FocusEvent)
		 */
		public void focusLost(FocusEvent event) {
			if (fTextViewer.getTextWidget() == event.widget) {
				fHoverShell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						stop();
					}
				});
			}
		}
		
		/*
		 * @see FocusListener#focusGained(FocusEvent)
		 */
		public void focusGained(FocusEvent event) {
		}
	};
	
	/** The text viewer this controller is connected to */
	private TextViewer fTextViewer;
	
	/** The hover shell */
	private Shell fHoverShell;
	/** The hover text */
	private StyledText fHoverText;
	/** The hover text presentation */
	private TextPresentation fHoverPresentation= new TextPresentation();
	/** Remembers the previous mouse hover location */
	private Point fHoverLocation= new Point(-1, -1);
	
	/** The radius of the circle in which mouse hover locations are considered equal. */
	private final static int EPSILON= 5;
	
	
	/**
	 * Creates a new text hovering controller for the given text viewer. The
	 * controller registers as mouse track listener on the text viewer's text widget.
	 * Initially, the popup window is invisible.
	 *
	 * @param textViewer the viewer for which the controller is created
	 */
	public TextHoveringController(TextViewer textViewer) {
		Assert.isNotNull(textViewer);
		
		fTextViewer= textViewer;
		
		StyledText styledText= textViewer.getTextWidget();
		fHoverShell= new Shell(styledText.getShell(), SWT.NO_FOCUS | SWT.NO_TRIM | SWT.ON_TOP);
		fHoverText= new StyledText(fHoverShell, SWT.MULTI | SWT.READ_ONLY);
		
		Display display= styledText.getShell().getDisplay();
		fHoverShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		fHoverText.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
	}
	
	/**
	 * Determines graphical area covered by the given text region.
	 *
	 * @param region the region whose graphical extend must be computed
	 * @return the graphical extend of the given region
	 */
	private Rectangle computeCoveredArea(IRegion region) {
				
		StyledText styledText= fTextViewer.getTextWidget();
		
		int start= region.getOffset() - fTextViewer.getVisibleRegionOffset();
		int end= start + region.getLength();
		
		Point upperLeft= styledText.getLocationAtOffset(start);
		Point lowerRight= new Point(upperLeft.x, upperLeft.y);
		
		for (int i= start +1; i < end; i++) {
			
			Point p= styledText.getLocationAtOffset(i);
			
			if (upperLeft.x > p.x)
				upperLeft.x= p.x;
				
			if (upperLeft.y > p.y)
				upperLeft.y= p.y;
				
			if (lowerRight.x  < p.x)
				lowerRight.x= p.x;
				
			if (lowerRight.y < p.y)
				lowerRight.y= p.y;
		}

		lowerRight.x += fTextViewer.getAverageCharWidth();
		lowerRight.y += styledText.getLineHeight();
		
		int width= lowerRight.x - upperLeft.x;
		int height= lowerRight.y - upperLeft.y;
		return new Rectangle(upperLeft.x, upperLeft.y, width, height);
	}
	
	/**
	 * Computes the document offset underlying the given text widget coordinates.
	 * This method uses a linear search as it cannot make any assumption about
	 * how the document is actually presented in the widget. (Covers cases such
	 * as bidi text.)
	 *
	 * @param x the x coordinate inside the text widget
	 * @param y the y coordinate inside the text widget
	 * @return the document offset corresponding to the given point
	 */
	private int computeOffsetAtLocation(int x, int y) {
		
		IDocument document= fTextViewer.getVisibleDocument();
		StyledText styledText= fTextViewer.getTextWidget();
		
		
		int line= (y + styledText.getTopPixel()) / styledText.getLineHeight();
		int lineCount= document.getNumberOfLines();
		
		if (line > lineCount - 1)
			line= lineCount - 1;
		
		if (line < 0)
			line= 0;
		
		try {
			
			IRegion lineInfo= document.getLineInformation(line);
			int low= lineInfo.getOffset();
			int high= low + lineInfo.getLength();
			
			int lookup= styledText.getLocationAtOffset(low).x;
			int guess= low;
			int guessDelta= Math.abs(lookup - x);
			
			for (int i= low + 1; i < high; i++) {
				lookup= styledText.getLocationAtOffset(i).x;
				int delta= Math.abs(lookup - x);
				if (delta < guessDelta) {
					guess= i;
					guessDelta= delta;
				}
			}
			
			return guess + fTextViewer.getVisibleRegionOffset();
		
		} catch (BadLocationException e) {
		}
		
		return -1;
	}
	
	/**
	 * Determines the location of the popup window depending on
	 * the size of the covered area and the coordinates at which 
	 * the window has been requested.
	 * 
	 * @param x the x coordinate at which the window has been requested
	 * @param y the y coordinate at which the window has been requested
	 * @param coveredArea graphical area of the hover region
	 * @return the location of the hover popup window
	 */
	private Point computeWindowLocation(int x, int y, Rectangle coveredArea) {
		y= coveredArea.y + coveredArea.height + 5;
		return fTextViewer.getTextWidget().toDisplay(new Point(x, y));
	}
	
	/**
	 * Returns whether the given event ocurred within a cicle of <code>EPSILON</code>
	 * pixels of the previous mouse hover location. In addition, the location of
	 * the mouse event is remembered as the previous mouse hover location.
	 * 
	 * @param event the event to check
	 * @return <code>false</code> if the event occured too close to the previous location
	 */
	private boolean isPreviousMouseHoverLocation(MouseEvent event) {

		boolean tooClose= false;
				
		if (fHoverLocation.x != -1 && fHoverLocation.y != -1) {
			tooClose= Math.abs(fHoverLocation.x - event.x) <= EPSILON;
			tooClose= tooClose && (Math.abs(fHoverLocation.y - event.y) <= EPSILON);
		}
		
		fHoverLocation.x= event.x;
		fHoverLocation.y= event.y;
		return tooClose;
	}
		
	/*
	 * @see MouseTrackAdapter#mouseHover
	 */
	public void mouseHover(MouseEvent event) {
		
		if (isPreviousMouseHoverLocation(event))
			return;
		
		int offset= computeOffsetAtLocation(event.x, event.y);
		if (offset == -1)
			return;
				
		ITextHover hover= fTextViewer.getTextHover(offset);
		if (hover == null)
			return;
			
		IRegion region= hover.getHoverRegion(fTextViewer, offset);
		if (region == null)
			return;
			
		String info= hover.getHoverInfo(fTextViewer, region);
		if (info != null && info.trim().length() > 0) {
			Rectangle coveredArea= computeCoveredArea(region);
			if (fHoverShell != null && !fHoverShell.isDisposed()) {
				
				fHoverText.setText(info);
				
				if (hover instanceof IHoverInfoPresenter) {
					IHoverInfoPresenter presenter= (IHoverInfoPresenter) hover;
					fHoverPresentation.clear();
					if (presenter.updatePresentation(info, fHoverPresentation))
						TextPresentation.applyTextPresentation(fHoverPresentation, fHoverText);
				}
				
				showWindow(coveredArea, computeWindowLocation(event.x, event.y, coveredArea));
			}
		}
	}
	
	/**
	 * Opens the hover popup window at the specified location. The window closes if the
	 * mouse pointer leaves the specified area.
	 *
	 * @param coveredArea the area about which the hover popup window presents information
	 * @param location the location of the hover popup window will pop up
	 */
	private void showWindow(Rectangle coveredArea, Point location) {
		
		Point size= fHoverText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		fHoverText.setSize(size.x + 3, size.y);
		fHoverShell.setSize(size.x + 5, size.y + 2);
		
		fHoverText.setLocation(1,1);
		fHoverShell.setLocation(location);
		
		new WindowCloser(coveredArea).start();
		uninstall();
		
		fHoverShell.setVisible(true);
	}
	
	/**
	 * Installs this hovering controller on its text viewer.
	 */
	public void install() {
		fTextViewer.getTextWidget().addMouseTrackListener(this);
	}
	
	/**
	 * Uninstalls this hovering controller from its text viewer.
	 */
	public void uninstall() {
		fTextViewer.getTextWidget().removeMouseTrackListener(this);
	}
	
	/**
	 * Disposes this hovering controller	 
	 */
	public void dispose() {
		if (fHoverShell != null && !fHoverShell.isDisposed()) {
			fHoverShell.dispose();
			fHoverShell= null;
		}
	}
}