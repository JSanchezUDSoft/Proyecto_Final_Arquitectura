package presentacion.vistas;

import presentacion.controladores.ControladorWindgetSAP;
import presentacion.vistas.VistaDisplaySieteSeg;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import presentacion.Modelo;

@SuppressWarnings("serial")
public class VistaWidgetSAP extends JPanel{
    
    private logica.SistemaSAP sistema;
    private GridBagConstraints c;

    // Contenido del widget
    private JLabel lblRegA;
    private JLabel lblRegB;
    private JLabel lblALU;
    private JLabel lblIR;
    private JLabel lblOut;
    private JLabel lblPC;
    private JLabel lblMAR;
    private JLabel lblControles;
    private JLabel lblStepCount;
    private JLabel lblBus;
    private JLabel lblStepCt;
    private JLabel lblFlags;

    private JLabel[] btns_bitsA;
    private JLabel[] btns_bitsB;
    private JLabel[] btns_bitsALU;
    private JLabel[] btns_bitsIR;
    private JLabel[] btns_bitsOUT;
    private JLabel[] btns_bitsPC;
    private JLabel[] btns_bitsMAR;
    private JLabel[] btns_bitsControl;
    private JLabel[] btns_bistBUS;
    
    private JLabel btnCarry;
    private JLabel btnZero;
    private VistaDisplaySieteSeg vistaDisplay7Seg;
    private VistaWidgetRAM ramWidget;

    private ControladorWindgetSAP control;
    
    // Constantes
    public static final Dimension BUTTON_SIZE = new Dimension(22, 22);
    public static final Dimension WIDGET_SIZE = new Dimension(625, 500);
    public static final Color BUTTON_UNSELECTED_BG = new Color(238, 238, 238);
    public static final Color BUTTON_SELECTED_BG = new Color(255, 85, 85);
    public static final Color COLOR_BACKGROUND = new Color(203, 246, 245);
    public static final Color WIDGET_BORDER_COLOR = Color.BLACK;
    
    private final Modelo modelo;    

    public VistaWidgetSAP(Modelo m, VistaDisplaySieteSeg display, VistaWidgetRAM ramWidget) {
        // Encapsula el modelo
        this.modelo = m;        
        this.sistema = m.getSistema();
        this.vistaDisplay7Seg = display;
        this.ramWidget = ramWidget;
        this.setBorder(BorderFactory.createLineBorder(WIDGET_BORDER_COLOR));

        this.sistema.addObserver(getControl());  // Agrega el manejador del observador

        // Tamaño preferido
        this.setPreferredSize(WIDGET_SIZE);
        this.setBackground(COLOR_BACKGROUND);

        // Layout
        this.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        c.insets = new Insets(10, 10, 10, 0);

        // Encabezado
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;
        this.lblBus = new JLabel("BUS");
        this.add(this.lblBus, c);

        // Etiquetas de registros
        c.gridwidth = 1;
        c.gridy = 2;
        c.gridx = 0;
        this.lblRegA = new JLabel("Registro A");
        this.add(lblRegA, c);

        //  B
        c.gridy = 4;
        c.gridx = 0;
        this.lblRegB = new JLabel("Registro B");
        this.add(lblRegB, c);

        // ALU 
        c.gridy = 6;
        c.gridx = 0;
        this.lblALU = new JLabel("ALU");
        this.add(lblALU, c);

        // Line Break
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 12;
        this.add(new JLabel("=================================================="), c);
        c.gridwidth = 1;

        // IR 
        c.gridy = 8;
        c.gridx = 0;
        this.lblPC = new JLabel("Registro de Instrucción");
        this.add(lblPC, c);

        // PC 
        c.gridy = 10;
        c.gridx = 0;
        this.lblIR = new JLabel("Contador de Programa");
        this.add(lblIR, c);

        // Line Break
        c.gridx = 0;
        c.gridy = 11;
        c.gridwidth = 12;
        this.add(new JLabel("=================================================="), c);
        c.gridwidth = 1;

        // OUT 
        c.gridy = 12;
        c.gridx = 0;
        this.lblOut = new JLabel("Salida");
        this.add(lblOut, c);

        // Memory Address Register 
        c.gridy = 14;
        c.gridx = 0;
        this.lblMAR = new JLabel("MAR");
        this.add(lblMAR, c);

        // Step Count 
        c.gridy = 16;
        c.gridx = 0;
        this.lblStepCount = new JLabel("Conteo de Ciclos");
        this.add(this.lblStepCount, c);

        // Valor step count
        c.gridy = 16;
        c.gridx = 1;
        this.lblStepCt = new JLabel("" + this.sistema.getStepCount());
        this.add(this.lblStepCt, c);

        // Line Break
        c.gridx = 0;
        c.gridwidth = 12;
        c.gridy = 17;
        this.add(new JLabel("=================================================="), c);

        // Lineas de Control 
        c.gridwidth = 1;
        c.gridy = 18;
        c.gridx = 0;
        this.lblControles = new JLabel("Lineas de Control ");
        this.add(this.lblControles, c);

        // Flags 
        c.gridy = 20;
        c.gridx = 0;
        this.lblFlags = new JLabel("Flags");
        this.add(this.lblFlags, c);

        // Prepara espacio display 
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);

        // Estados de las líneas de control
        this.btns_bitsControl = new JLabel[16];
        this.btns_bitsControl[0] = crearLabel("HLT"); 
        this.btns_bitsControl[1] = crearLabel("MI");
        this.btns_bitsControl[2] = crearLabel("RI");
        this.btns_bitsControl[3] = crearLabel("RO");
        this.btns_bitsControl[4] = crearLabel("IO");
        this.btns_bitsControl[5] = crearLabel("II");
        this.btns_bitsControl[6] = crearLabel("AI");
        this.btns_bitsControl[7] = crearLabel("AO");
        this.btns_bitsControl[8] = crearLabel("ΣO");
        this.btns_bitsControl[9] = crearLabel("SU");
        this.btns_bitsControl[10] = crearLabel("BI");
        this.btns_bitsControl[11] = crearLabel("OI");
        this.btns_bitsControl[12] = crearLabel("CE");
        this.btns_bitsControl[13] = crearLabel("CO");
        this.btns_bitsControl[14] = crearLabel("J");
        this.btns_bitsControl[15] = crearLabel("FI");

        c.gridy = 18;
        for (int i = 0; i < 16; i++) {
            btns_bitsControl[i].setBackground(BUTTON_UNSELECTED_BG);
            if (i == 8) {
                c.gridy++;
                c.gridx = 1;
            } else if (i >= 8) {
                c.gridx = i - 7;
            } else {
                c.gridx = i + 1;
            }
            this.add(this.btns_bitsControl[i], c);
        }

        //  BUS 
        c.gridy = 0;
        c.gridx = 1;
        btns_bistBUS = new JLabel[8];
        for (int i = 0; i <= 7; i++) {
            c.gridx = i + 1;
            JLabel b = crearLabel("0"); 
            this.add(b, c);
            btns_bistBUS[i] = b;
        }

        //  A 
        c.gridy = 2;
        c.gridx = 1;
        btns_bitsA = new JLabel[8];
        for (int i = 0; i <= 7; i++) {
            c.gridx = i + 1;
            JLabel b = crearLabel("0"); 
            this.add(b, c);
            btns_bitsA[i] = b;
        }

        // B 
        c.gridy = 4;
        c.gridx = 1;
        btns_bitsB = new JLabel[8];
        for (int i = 0; i <= 7; i++) {
            c.gridx = i + 1;
            JLabel b = crearLabel("0"); 
            this.add(b, c);
            btns_bitsB[i] = b;
        }

        //  ALU 
        c.gridy = 6;
        c.gridx = 1;
        btns_bitsALU = new JLabel[8];
        for (int i = 0; i <= 7; i++) {
            c.gridx = i + 1;
            JLabel b = crearLabel("0"); 
            this.add(b, c);
            btns_bitsALU[i] = b;
        }

        //  IR 
        c.gridy = 8;
        c.gridx = 1;
        btns_bitsIR = new JLabel[8];
        for (int i = 0; i <= 7; i++) {
            c.gridx = i + 1;
            JLabel b = crearLabel("0"); 
            this.add(b, c);
            btns_bitsIR[i] = b;
        }

        //  out 
        c.gridy = 12;
        c.gridx = 1;
        btns_bitsOUT = new JLabel[8];
        for (int i = 0; i <= 7; i++) {
            c.gridx = i + 1;
            JLabel b = crearLabel("0"); 
            this.add(b, c);
            btns_bitsOUT[i] = b;
        }

        //  PC
        c.gridy = 10;
        c.gridx = 1;
        btns_bitsPC = new JLabel[4];
        for (int i = 0; i <= 3; i++) {
            c.gridx = i + 1;
            JLabel b = crearLabel("0"); 
            this.add(b, c);
            btns_bitsPC[i] = b;
        }

        //  MAR
        c.gridy = 14;
        c.gridx = 1;
        btns_bitsMAR = new JLabel[4];
        for (int i = 0; i <= 3; i++) {
            c.gridx = i + 1;
            JLabel b = crearLabel("0"); 
            this.add(b, c);
            btns_bitsMAR[i] = b;
        }

        //  Flags
        c.gridy = 20;
        c.gridx = 1;
        this.btnCarry = crearLabel("C");
        this.add(this.btnCarry, c);
        c.gridx = 2;
        this.btnZero = crearLabel("Z");
        this.add(this.btnZero, c);

        getControl().cambioLineasControl();
        repaint();
    }

    protected JLabel crearLabel(String str){
        JLabel b = new JLabel(str);
        b.setPreferredSize(BUTTON_SIZE);
        b.setBackground(BUTTON_UNSELECTED_BG);
        b.setFont(new java.awt.Font("Arial", 0, 15)); 
        b.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        b.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        b.setOpaque(true);
        return b;
    }

    public Modelo getModelo() {
        return modelo;
    }
        
    public ControladorWindgetSAP getControl() {
        if(control == null){
            control = new ControladorWindgetSAP(this);
        }
        return control;
    }

    public JLabel[] getBtns_bitsA() {
        return btns_bitsA;
    }

    public JLabel[] getBtns_bitsB() {
        return btns_bitsB;
    }

    public JLabel[] getBtns_bitsALU() {
        return btns_bitsALU;
    }

    public JLabel[] getBtns_bitsIR() {
        return btns_bitsIR;
    }

    public JLabel[] getBtns_bitsOUT() {
        return btns_bitsOUT;
    }

    public JLabel[] getBtns_bitsPC() {
        return btns_bitsPC;
    }

    public JLabel[] getBtns_bitsMAR() {
        return btns_bitsMAR;
    }

    public JLabel[] getBtns_bitsControl() {
        return btns_bitsControl;
    }

    public JLabel[] getBtns_bistBUS() {
        return btns_bistBUS;
    }

    public JLabel getBtnCarry() {
        return btnCarry;
    }

    public JLabel getBtnZero() {
        return btnZero;
    }

    public JLabel getLblStepCt() {
        return lblStepCt;
    }

    public VistaDisplaySieteSeg getVistaDisplay() {
        return vistaDisplay7Seg;
    }

    public VistaWidgetRAM getRamWidget() {
        return ramWidget;
    }
    
    
    
}
