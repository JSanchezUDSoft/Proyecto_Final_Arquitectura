package presentacion.vistas;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import logica.SistemaSAP;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import presentacion.Modelo;
import presentacion.controladores.ControlAssembler;

@SuppressWarnings("serial")
public class VistaPanelAssembler extends JPanel {

    // Define instance variables
    private GridBagConstraints c;
    private JTextArea txaEntrada;
    private JLabel lblSalida;
    private JButton btnEnsamblar;
    private JButton btnEnviarSAP;
    private JButton btnSalir;
    private JButton btnDecompilar;
    private Modelo modelo;
    private JPanel panelRetorno;

    private ControlAssembler control;
    private SistemaSAP sistema;

    // Define constants
    public static final int SCREEN_X = 225 + (2318 / 3);
    public static final int SCREEN_Y = 50 + (1600 / 3);
    public static final int BUTTON_PANEL_HEIGHT = 50;
    public static final char ASSEMBLER_COMMENT_SYMBOL = '#';
    public static final Dimension WIDGET_SIZE = new Dimension(SCREEN_X, SCREEN_Y);
    public static final String OUTPUT_PLACEHOLDER = "<html>Programa ensamblado aquí</html>";
    public static final String INPUT_PLACEHOLDER = "\n  Crear un programa aquí...";
    

    public VistaPanelAssembler(Modelo m, JPanel returnPanel) {
        // Encapsulate SAP Model and return view
        this.modelo = m;
        this.sistema = modelo.getSistema();        
        this.panelRetorno = returnPanel;

        // Set preferred size and color
        this.setPreferredSize(WIDGET_SIZE);
        this.setBackground(VistaPanelCPU.VIEW_BACKGROUND_COLOR);

        // Set the Layout
        this.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridwidth = 1;

        // Add the text input area
        this.txaEntrada = new JTextArea();
        this.txaEntrada.setPreferredSize(new Dimension(SCREEN_X / 2, SCREEN_Y - 50));
        this.txaEntrada.setMaximumSize(new Dimension(SCREEN_X / 2, SCREEN_Y - 50));
        this.txaEntrada.setMinimumSize(new Dimension(SCREEN_X / 2, SCREEN_Y - 50));
        this.txaEntrada.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.txaEntrada.setText(INPUT_PLACEHOLDER);
        this.txaEntrada.setBackground(VistaWidgetSAP.COLOR_BACKGROUND);

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 6;
        this.add(this.txaEntrada, c);
        c.insets = new Insets(0, 10, 0, 0);
        c.gridheight = 1;

        // Add the compiled program
        this.lblSalida = new JLabel(OUTPUT_PLACEHOLDER);
        this.lblSalida.setPreferredSize(new Dimension(SCREEN_X / 2, SCREEN_Y - 3 * BUTTON_PANEL_HEIGHT - 20));
        this.lblSalida.setMaximumSize(new Dimension(SCREEN_X / 2, SCREEN_Y - 3 * BUTTON_PANEL_HEIGHT - 20));
        this.lblSalida.setMinimumSize(new Dimension(SCREEN_X / 2, SCREEN_Y - 3 * BUTTON_PANEL_HEIGHT - 20));
        this.lblSalida.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        c.gridx = 2;
        c.gridy = 0;
        this.add(this.lblSalida, c);

        // Add the assembler button
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 1;
        this.btnEnsamblar = new JButton("Ensamblar");
        this.btnEnsamblar.setActionCommand("assemble");
        this.btnEnsamblar.addActionListener(getControl());
        this.add(this.btnEnsamblar, c);

        // Add the send to sap button
        c.gridx = 2;
        c.gridy = 2;
        this.btnEnviarSAP = new JButton("Enviar a SAP");
        this.btnEnviarSAP.setActionCommand("sendtosap");
        this.btnEnviarSAP.addActionListener(getControl());
        this.add(this.btnEnviarSAP, c);

        // Add the decompile button
        c.gridx = 2;
        c.gridy = 3;
        this.btnDecompilar = new JButton("Decompilar Programa actual");
        this.btnDecompilar.setActionCommand("decompile");
        this.btnDecompilar.addActionListener(getControl());
        this.add(this.btnDecompilar, c);

        // Add the exit button
        c.gridx = 2;
        c.gridy = 4;
        this.btnSalir = new JButton("Salir");
        this.btnSalir.setActionCommand("exit");
        this.btnSalir.addActionListener(getControl());
        this.add(this.btnSalir, c);
    }

    public Modelo getModelo() {
        return modelo;
    }
    
    public JButton getBtnEnsamblar() {
        return btnEnsamblar;
    }

    public JButton getBtnEnviarSAP() {
        return btnEnviarSAP;
    }

    public JButton getBtnSalir() {
        return btnSalir;
    }

    public JButton getBtnDecompilar() {
        return btnDecompilar;
    }

    public JTextArea getTxaEntrada() {
        return txaEntrada;
    }

    public JLabel getLblSalida() {
        return lblSalida;
    }

    public SistemaSAP getSistema() {
        return sistema;
    }

    public JPanel getPanelRetorno() {
        return panelRetorno;
    }
    
    public ControlAssembler getControl() {
        if (control == null) {
            control = new ControlAssembler(this);
        }
        return control;
    }

}
