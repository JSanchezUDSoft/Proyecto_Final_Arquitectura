package presentacion.controladores;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import presentacion.vistas.VistaDisplaySieteSeg;

public class ControlDisplaySieteSeg implements ActionListener {

    private final VistaDisplaySieteSeg vista7Seg;

    public ControlDisplaySieteSeg(VistaDisplaySieteSeg aThis) {
        vista7Seg = aThis;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().contentEquals("toggle")) {
            vista7Seg.setConSigno(!vista7Seg.isConSigno());
            vista7Seg.setValor(vista7Seg.getByteValor());

            // Cambia etiqueta
            vista7Seg.getBtnCambioModo().setText(vista7Seg.isConSigno() ? vista7Seg.TWOS_COMP_LABEL : vista7Seg.UNSIGNED_LABEL);
        }
    }
}
