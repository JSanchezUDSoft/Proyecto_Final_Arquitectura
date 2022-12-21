
package presentacion.controladores;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import presentacion.vistas.VistaPanelCPU;
import logica.SistemaSAP;
import logica.HiloReloj;
import presentacion.Modelo;


public class ControlPanelCPU implements ActionListener, ChangeListener, 
        interfaces.ILogObserver, interfaces.IClockObserver{

    private final VistaPanelCPU vistaCPU;
    private final SistemaSAP sistema;
    private final Modelo modelo;

    public ControlPanelCPU(VistaPanelCPU aThis) {
        vistaCPU = aThis;
        modelo = vistaCPU.getModelo();        
        sistema = modelo.getSistema();
    }
    
    @Override
    // Manejador clicks de los botones
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("resetButtonClicked")) {
            modelo.reset();
        } else if (e.getActionCommand().contentEquals("clockButton")) {
            // Si el programa se está ejecutando, primero hay que detenerlo
            if (modelo.isEjecutandoPrograma()) {
                modelo.setEjecutandoPrograma(false);
                modelo.getHiloReloj().terminar();
                modelo.setHiloReloj(null);
                vistaCPU.getBtnEjecutar().setText("Ejecutar");
            }

            logica.Clock.getClock().toggleClock();
        } else if (e.getActionCommand().contentEquals("autoplay")) {
            if (modelo.isEjecutandoPrograma()) {
                modelo.setEjecutandoPrograma(false);
                modelo.getHiloReloj().terminar();
                modelo.setHiloReloj(null);
                vistaCPU.getBtnEjecutar().setText("Ejecutar");
            } else {
                modelo.setEjecutandoPrograma(true);
                modelo.setHiloReloj(new HiloReloj(vistaCPU.getSliVelocidad().getMaximum() - vistaCPU.getSliVelocidad().getValue() + 25));                
                modelo.getHiloReloj().start();
                vistaCPU.getBtnEjecutar().setText("Detener");
            }
        }
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {        
        modelo.modificarVelocidadEjecucion();               
    }
    
    // Implementación log observer
    @Override
    public void agregaEntradaLog(String entry) {
        vistaCPU.getTxaLogArea().append(entry + "\n");
    }

    @Override
    // Si el reloj cambia, actualizar el Label
    public void cambioReloj() {
        vistaCPU.getLblClockStatus().setText("Reloj: " + (logica.Clock.getClock().getEstado() ? "ALTO" : "BAJO"));
    }
    
}
