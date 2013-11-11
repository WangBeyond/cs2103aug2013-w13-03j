import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

//@author A0098077N
/**
 * Swing "integration" on top of this JavaFX node
 * This is an original class from Arnaud Nouard with lots of modifications to be fit for the currently working project
 * The purpose of this class is to create a JavaFX node that can display a Swing node
 */
public class SwingNode extends Region {
	/*
     * Windows offset (Frame decoration bounds)
     * Should be dynamically determined
     */
    private static int STAGE_BORDER_X = 8;
    private static int STAGE_BORDER_Y = 30;
    // The dialog that wraps around the Swing component
    private JDialog jDialog;
    // The parent stage
    private Stage stage;
    private boolean isKeyEvent;
    // The listeners
    private ChangeListener<Number> changeListenerH;
    private ChangeListener<Bounds> changeListenerBIL;
    private ChangeListener<Number> changeListenerW;
    private ChangeListener<Number> changeListenerStageX;
    private ChangeListener<Number> changeListenerStageY;
    // The frame for the component
    private JFrame jFrameParent;
    
	/**
	 * Constructor of this class
	 * 
	 * @param orgStage
	 *            the original stage i.e main window
	 * @param jcomponent
	 *            the component in Swing
	 */
    public SwingNode(Stage orgStage, final Component jcomponent) {
    	this(orgStage, jcomponent, STAGE_BORDER_X, STAGE_BORDER_X);
    }
    
    /**
     * The main constructor of this class
     * @param orgStage the parent stage
     * @param jcomponent the Swing component
     * @param offsetX offset in X-direction
     * @param offsetY offset in Y-direction
     */
    public SwingNode(Stage orgStage, final Component jcomponent, int offsetX, int offsetY) {
        this.stage = orgStage;
        wrapSwingComponent(jcomponent);
        setupListeners();
    }
    
    /**
     * Setup listeners for the stage
     */
	private void setupListeners() {
		setupListenerToBounds();
        setupListenerToStageX();
        setupListenerToStageY();
        setupListenerToShowProperty();
        setupListenerToFocusProperty();
        setupListenerToVisibleProperty();
	}
	
	// Listener to the visible property
	private void setupListenerToVisibleProperty() {
		super.
        visibleProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                jDialog.setVisible(t1.booleanValue());
            }
        });
	}
	
	// Listener to the focus property 
	private void setupListenerToFocusProperty() {
		stage.focusedProperty()
                .addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, final Boolean newValue) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setSwingComponentAlwaysOnTop(newValue.booleanValue());
                       if(newValue.booleanValue() == false){
                    	   if(!isKeyEvent){
                    		   checkMousePosition();
                    	   }
                       }
                       isKeyEvent = false;
                    }

					private void checkMousePosition() {
						PointerInfo mouseInfo = MouseInfo.getPointerInfo();
						Point mousePosition = mouseInfo.getLocation();
						double x = mousePosition.getX();
						double y = mousePosition.getY();
						boolean check1 = x < stage.getX();
						boolean check2 = x > stage.getX() + stage.getWidth();
						boolean check3 = y < stage.getY();
						boolean check4 = y > stage.getY() + stage.getHeight();
						if (check1 || check2 || check3 || check4) {
							jDialog.toBack();
							stage.toBack();
						}
					}
                });
               
            }
        });
	}
	
	// Listener to the show property
	private void setupListenerToShowProperty() {
		stage.showingProperty()
                .addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, final Boolean t1) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        jDialog.setVisible(t1.booleanValue());
                    }
                });
            }
        });
	}
	
	// Listener to the Stage Y-Position
	private void setupListenerToStageY() {
		changeListenerStageY = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, final Number t1) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Rectangle bounds = jDialog.getBounds();
                        bounds.y = t1.intValue();
                        Point2D jCoord = new Point2D(bounds.y, bounds.y);
                        Point2D p = localToScene(jCoord);

                        bounds.y = (int)( p.getY() + stage.getHeight())+ STAGE_BORDER_Y;
                        jDialog.setBounds(bounds);
                    }
                });
            }
        };
        stage.yProperty().addListener(changeListenerStageY);
	}
	
	// Listener to the Stage X-Position
	private void setupListenerToStageX() {
		changeListenerStageX = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, final Number t1) {

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Rectangle bounds = jDialog.getBounds();
                        bounds.x = t1.intValue();
                        Point2D jCoord = new Point2D(bounds.x, bounds.x);
                        Point2D p = localToScene(jCoord);

                        bounds.x = (int) p.getX() + STAGE_BORDER_X;
                        jDialog.setBounds(bounds);
                    }
                });
            }
        };
        stage.xProperty().addListener(changeListenerStageX);
	}
	
	// Listener to the bounds of the command line
	private void setupListenerToBounds() {
		changeListenerBIL = new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> ov, Bounds t, Bounds bounds) {

                final Bounds boundsJFX = localToScene(bounds);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Rectangle bounds = jDialog.getBounds();
                        bounds.x = (int) (boundsJFX.getMinX() + stage.getX()) + STAGE_BORDER_X;
                        bounds.y = (int) (boundsJFX.getMinY() + stage.getY() + stage.getHeight()) + STAGE_BORDER_Y;
                        bounds.width = (int) 634;
                        bounds.height = (int) 35;

                        jDialog.setBounds(bounds);
                    }
                });
            }
        };
        boundsInLocalProperty().addListener(changeListenerBIL);
	}
	
	// Wrap the Swing component in the JavaFX node
	private void wrapSwingComponent(final Component jcomponent) {
		try {
            /*
             * Wrap the Swing component with an invisible window
             */
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    toDialog(jcomponent);
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            Logger.getLogger(SwingNode.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
    
    // Set the key Event to notify the listener
    public void setKeyEvent(boolean isKeyEvent){
    	this.isKeyEvent = isKeyEvent;
    }
    
    // Set the command line be on top
    public void setCommandLineOnTop(){
    	SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
			jDialog.toFront();
			}
		});
    }
    
    // Remove all listeners
    public void removeAllListeners() {
        boundsInLocalProperty().removeListener(changeListenerBIL);
        heightProperty().removeListener(changeListenerH);
        widthProperty().removeListener(changeListenerW);
        stage.xProperty().removeListener(changeListenerStageX);
        stage.yProperty().removeListener(changeListenerStageY);
    }

    public Container toDialog(Component comp) {
        jFrameParent = new JFrame();
        jDialog = new JDialog(jFrameParent);
       
        setSwingComponentAlwaysOnTop(false);
        setupJDialog(comp);
        jDialog.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                // To ensure Stage to be set to front at the same time as Swing
            	setSwingComponentAlwaysOnTop(true);
            	Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        stage.toFront(); 
                    }
                });
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
             
            }
        });
      
        /* Window Focus */
        jDialog.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                setSwingComponentAlwaysOnTop(true);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                       stage.toFront();
                    }
                });
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
            	setSwingComponentAlwaysOnTop(false);
            	jDialog.toBack();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                    	if(stage.isFocused() == false)
                        stage.toBack();
                    }
                });
            }
        }); 
        return jDialog;
    }
    
    // Setup the settings for the JDialog that will wrap around the component
	private void setupJDialog(Component comp) {
		jDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Avoid ALT+F4
        jDialog.setUndecorated(true);
        jDialog.getContentPane().add(comp);
        jDialog.setType(Window.Type.UTILITY);
        jDialog.setModalExclusionType(Dialog.ModalExclusionType.NO_EXCLUDE);
        jDialog.setResizable(false);
        jDialog.setFocusable(true);
        jDialog.setAutoRequestFocus(true);
	}
    
    // Set that the Swing component is always above all applications
    public void setSwingComponentAlwaysOnTop(boolean value) {
        jDialog.setAlwaysOnTop(value);
    }
    
    // Set stage to front most of the window
    public void setStageToFront() {
        stage.toFront();
    }
    
    // Dispose the SwingNode
    public void dispose() {
        removeAllListeners();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jFrameParent.dispose();
                jDialog.setAlwaysOnTop(false);
                jDialog.setVisible(false);
                jDialog.dispose();
            }
        });
    }
}