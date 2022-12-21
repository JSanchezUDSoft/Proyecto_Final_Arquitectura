
package presentacion;

import logica.SistemaSAP;
import java.awt.Dimension;
import javax.swing.JFrame;
import logica.HiloReloj;
import presentacion.vistas.VistaPanelCPU;


public class Modelo {
    
    private JFrame ventanaPrincipal;
    private VistaPanelCPU panelCPU;
    private SistemaSAP sistema;    
    
    private boolean ejecutandoPrograma;
    private HiloReloj hiloReloj;
  
    public Modelo() {
        
    }
    
    public void iniciar(){               
        getVentanaPrincipal().pack();
        getVentanaPrincipal().setVisible(true);
    }

    // genera la ventana principal, que contendrá a los diferentes paneles que 
    // mostrarán el resultado de la ejecución del simulador
    public JFrame getVentanaPrincipal() {
        if(ventanaPrincipal == null){              
            ventanaPrincipal = new JFrame();
            ventanaPrincipal.setTitle("Simulador SAP-1");
            ventanaPrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ventanaPrincipal.setContentPane(getPanelCPU());
            ventanaPrincipal.setPreferredSize(new Dimension(1200, 768));
            ventanaPrincipal.setResizable(true);
        }
        return ventanaPrincipal;
    }

    public VistaPanelCPU getPanelCPU(){
        if(panelCPU == null){
            panelCPU = new VistaPanelCPU(this);   
        }
        return panelCPU;
    }
    
    public SistemaSAP getSistema() {
        if(sistema == null){
            sistema = new SistemaSAP();
        }
        return sistema;
    }
    
    public boolean isEjecutandoPrograma() {
        return ejecutandoPrograma;
    }

    public void setEjecutandoPrograma(boolean auto) {
        this.ejecutandoPrograma = auto;
    }

    public HiloReloj getHiloReloj() {
        return hiloReloj;
    }

    public void setHiloReloj(HiloReloj runBG) {
        this.hiloReloj = runBG;
    }

    public void modificarVelocidadEjecucion() {
        // Implementación ChangeListener
        if (this.isEjecutandoPrograma()) {
            this.setEjecutandoPrograma(false);
            this.getHiloReloj().terminar();
            this.setHiloReloj(null);

            this.setEjecutandoPrograma(true);
            double tiempo = getPanelCPU().getSliVelocidad().getMaximum() - getPanelCPU().getSliVelocidad().getValue();
            this.setHiloReloj(new HiloReloj(tiempo));
            this.getHiloReloj().start();
        }
    }

    public void reset() {
        this.getSistema().reset();                
    }            
    
    
}
