package bdv.jogl.VolumeRenderer.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.IsoSurfaceVolumeInterpreter;
import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.TransparentVolumeinterpreter;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.TransferFunctions.sampler.PreIntegrationSampler;
import bdv.jogl.VolumeRenderer.TransferFunctions.sampler.RegularSampler;
import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;
import bdv.jogl.VolumeRenderer.gui.TFDataPanel.TransferFunctionDataPanel;
import bdv.jogl.VolumeRenderer.gui.TFDrawPanel.TransferFunctionDrawPanel;
import bdv.jogl.VolumeRenderer.gui.VDataAggregationPanel.AggregatorManager;
import bdv.jogl.VolumeRenderer.gui.VDataAggregationPanel.VolumeDataAggregationPanel;
import bdv.jogl.VolumeRenderer.utils.IVolumeDataManagerListener;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManagerAdapter;

/**
 * Class for providing tf scene controls
 * @author michael
 *
 */
public class SceneControlsWindow extends JFrame {
	
	/**
	 * default version
	 */
	private static final long serialVersionUID = 1L;

	private TransferFunctionDrawPanel tfpanel = null;
	
	private final JPanel mainPanel  = new JPanel();
	
	private TransferFunctionDataPanel tfDataPanel = null;
	
	private TransferFunction1D transferFunction;
	
	private VolumeDataAggregationPanel aggregationPanel;
	
	private JCheckBox usePreIntegration = new JCheckBox("Use pre-integration",false);
	
	private JCheckBox advancedCheck = new JCheckBox("Advanced configurations",false);
	
	private JCheckBox showIsoSurface = new JCheckBox("Show iso surface", false);
	
	private JSpinner isoValueSpinner = new JSpinner();
	
	private final VolumeDataManager dataManager;
	
	private final MultiVolumeRenderer renderer;
	
	private final GLWindow drawWindow;
	
	public SceneControlsWindow(final TransferFunction1D tf,final AggregatorManager agm, final VolumeDataManager dataManager, final MultiVolumeRenderer mvr, final GLWindow win){
		this.drawWindow = win;
		this.renderer = mvr;
		transferFunction = tf;
		this.dataManager = dataManager;
		createTFWindow(tf,agm,dataManager);
	}
	
	private void createTFWindow(final TransferFunction1D tf,final AggregatorManager agm,final VolumeDataManager dataManager){
		tfpanel = new TransferFunctionDrawPanel(tf,dataManager);
		tfDataPanel = new TransferFunctionDataPanel(tf);
		aggregationPanel = new VolumeDataAggregationPanel(agm);


		setTitle("Transfer function configurations");
		setSize(640, 100);
		initAdvancedBox();
		initUsePreIntegration();
		initShowIsoSurface();
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(tfpanel);
		mainPanel.add(advancedCheck);
		mainPanel.add(tfDataPanel);
		mainPanel.add(usePreIntegration);
		mainPanel.add(showIsoSurface);
		mainPanel.add(isoValueSpinner);
		
		mainPanel.add(aggregationPanel);
		tfDataPanel.setVisible(advancedCheck.isSelected());
		
		getContentPane().add(mainPanel);
		pack();
	}

	private void changeVolumeInterpreter(){
		if(showIsoSurface.isSelected()){
			renderer.getSource().setVolumeInterpreter(new IsoSurfaceVolumeInterpreter());
		}else{
			renderer.getSource().setVolumeInterpreter(new TransparentVolumeinterpreter());
		}
	}
	private void updateIsoSurface(){
		renderer.setIsoSurface(((Number)isoValueSpinner.getValue()).floatValue());
	}
	
	private void initShowIsoSurface() {
		dataManager.addVolumeDataManagerListener(new VolumeDataManagerAdapter() {
			
			@Override
			public void dataUpdated(Integer i) {
				float maxVolume=dataManager.getGlobalMaxVolumeValue();
				isoValueSpinner.setModel(new SpinnerNumberModel(0.0,0.0, maxVolume, (maxVolume< 1.0)?0.1f:1.0f));
			}
		});	
		
		changeVolumeInterpreter();
		showIsoSurface.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				changeVolumeInterpreter();
				drawWindow.getGlCanvas().repaint();
			}
		});
		
		updateIsoSurface();
		isoValueSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				updateIsoSurface();
				drawWindow.getGlCanvas().repaint();
			}
		});
		
	}

	private void changeTransferfuntionSampler(){
		if(usePreIntegration.isSelected()){
			transferFunction.setSampler(new PreIntegrationSampler());
		}else{
			transferFunction.setSampler(new RegularSampler());
		}
	}
	
	private void initUsePreIntegration() {
		changeTransferfuntionSampler();
		usePreIntegration.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				changeTransferfuntionSampler();
			}
		});
	}

	private void initAdvancedBox() {
		advancedCheck.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				tfDataPanel.setVisible(advancedCheck.isSelected());
				pack();
				
			}
		});
		
	}

	public void destroyTFWindow() {
		dispose();
		tfpanel = null;
	}
}