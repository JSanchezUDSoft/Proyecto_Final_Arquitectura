package presentacion.controladores;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JPanel;
import presentacion.Modelo;
import presentacion.vistas.VistaPanelAssembler;
import logica.SistemaSAP;

public class ControlAssembler implements ActionListener {

    private final VistaPanelAssembler vistaAssembler;
    private final SistemaSAP sistema;
    private final JPanel returnPanel;
    private final Modelo modelo;

    public ControlAssembler(VistaPanelAssembler v) {
        vistaAssembler = v;
        modelo = v.getModelo();
        sistema = modelo.getSistema();
        returnPanel = v.getPanelRetorno();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // El usuario quiere mostrar el panel del ensamblador
        if (e.getActionCommand().contentEquals("assemble")) {
            vistaAssembler.getLblSalida().setText(parseProgram(vistaAssembler.getTxaEntrada().getText()));
            return;
        }

        // El usuario quiere enviar el último programa compilado al SAP
        if (e.getActionCommand().contentEquals("sendtosap")) {
            this.sendToSap();
            return;
        }

        // El usuario quiere salir del ensamblador y volver a la vista principal
        if (e.getActionCommand().contentEquals("exit")) {
            modelo.getVentanaPrincipal().setContentPane(this.returnPanel);
            modelo.getVentanaPrincipal().pack();
            modelo.getVentanaPrincipal().setVisible(true);
            return;
        }

        // El usuario quiere descompilar el programa en SAP
        if (e.getActionCommand().contentEquals("decompile")) {
            this.decompilar();
            return;
        }
    }

    private void sendToSap() {
        // Caso extremo: el usuario acaba de descompilar el programa o no hay ningún 
        // programa compilado correctamente listo para enviar
        if (vistaAssembler.getLblSalida().getText().indexOf("[Dirección] Binario / Decimal") != -1) {
            vistaAssembler.getLblSalida().setText("[Error ensamblador] Programa ya está en el SAP!");
            return;
        } else if (vistaAssembler.getLblSalida().getText().indexOf("[Assembler Failed]") != -1) {
            vistaAssembler.getLblSalida().setText("[Error ensamblador] Debe tener un programa compilado con éxito para enviar a SAP");
            return;
        } else if (vistaAssembler.getLblSalida().getText().indexOf(vistaAssembler.OUTPUT_PLACEHOLDER) != -1) {
            vistaAssembler.getLblSalida().setText("[Error ensamblador] Debe tener un programa compilado con éxito para enviar a SAP");
            return;
        }

        // Obtenga la RAM de SAP
        byte[] ram = this.sistema.getRAM().getData();

        // Obtenga el último programa compilado, como una matriz de cadenas
        String[] program = vistaAssembler.getLblSalida().getText().split("<br>");

        // Eliminar la etiqueta HTML de la primera instrucción
        program[0] = program[0].substring(6);

        // Recorte cada cadena solo al código de la máquina
        for (int i = 0; i < 16; i++) {
            program[i] = program[i].substring(program[i].indexOf(": ") + 2);
        }

        // Mover cada cadena a la memoria
        for (int i = 0; i < ram.length; i++) {
            this.sistema.getRAM().cambiarValor(i, (byte) (0b11111111 & Integer.parseInt(program[i], 2)));
        }
    }

    private void decompilar() {
        // Obtener el contenido de ram
        byte[] ram = this.sistema.getRAM().getData();

        // Escribe el contenido de la RAM en el área de salida
        String out = "<html> [Dirección] Binario / Decimal<br>";

        for (int i = 0; i < ram.length; i++) {
            String binary = Integer.toBinaryString(0b11111111 & ram[i]);

            // Relleno de ceros a la izquierda
            for (int j = 0; j < 8 - binary.length(); j++) {
                binary = "0" + binary;
            }
            out += (i <= 9 ? "[ " : "[") + i + (i <= 9 ? " ] " : "] ") + binary + " / " + ram[i] + "<br>";
        }

        vistaAssembler.getLblSalida().setText(out + "</html>");

        // Decompilar el programa
        out = "";
        for (int i = 0; i < ram.length; i++) {
            byte opCode = (byte) (0b11110000 & ram[i]);
            byte arg = (byte) (0b00001111 & ram[i]);
            SistemaSAP.TipoInstruccion opType = sistema.decodificarInstruccion(opCode);
            switch (opType) {
                case LDA:
                    out += "LDA\t" + argTo4BitString(arg);
                    break;
                case NOP:
                    out += "NOP";
                    break;
                case ADD:
                    out += "ADD\t" + argTo4BitString(arg);
                    break;
                case SUB:
                    out += "SUB\t" + argTo4BitString(arg);
                    break;
                case STA:
                    out += "STA\t" + argTo4BitString(arg);
                    break;
                case LDI:
                    out += "LDI\t" + argTo4BitString(arg);
                    break;
                case JMP:
                    out += "JMP\t" + argTo4BitString(arg);
                    break;
                case JC:
                    out += "JC\t" + argTo4BitString(arg);
                    break;
                case JZ:
                    out += "JZ\t" + argTo4BitString(arg);
                    break;
                case OUT:
                    out += "OUT";
                    break;
                case HLT:
                    out += "HLT";
                    break;
                default:
                    break;
            }

            out += "\n";
        }
        vistaAssembler.getTxaEntrada().setText(out);
    }

    private String parseProgram(String text) {
        String rVal = "<html>" + text.replaceAll("\n", "<br>") + "</html>";

        // Caso extremo
        if (text.contentEquals("") || text.trim().length() == 0) {
            return "<html>[Error ensamblador] No hay un programa.</html>";
        }

        // Analizar la entrada en la matriz de cadenas
        String[] arr = text.split("\n");

        // Eliminar comentarios, forzar cadena a mayúsculas
        for (int i = 0; i < arr.length; i++) {
            // Toma la cadena actual
            String s = arr[i];

            // Si tiene un comentario, se quita.
            if (s.indexOf(vistaAssembler.ASSEMBLER_COMMENT_SYMBOL) != -1) {
                s = s.substring(0, s.indexOf(vistaAssembler.ASSEMBLER_COMMENT_SYMBOL));
                arr[i] = s.toUpperCase();
            } else {
                arr[i] = arr[i].toUpperCase();
            }
        }

        // Eliminar líneas en blanco
        List<String> result = new ArrayList<String>();

        for (String str : arr) {
            if (str != null && !str.isEmpty()) {
                result.add(str);
            }
        }

        // Eliminar espacios en blanco
        for (int i = 0; i < result.size(); i++) {
            result.set(i, result.get(i).replaceAll("\\s+", ""));
        }

        // Analizar variables
        Map<String, String> varValLookup = new TreeMap<String, String>();
        Map<String, Integer> varAddressLookup = new TreeMap<String, Integer>();
        Set<String> deletionIndices = new HashSet<String>();
        int nextAddress = 15;

        for (int i = 0; i < result.size(); i++) {
            // Toma la instrucción actual
            String curr = result.get(i);

            // ¿La instrucción actual es una declaración de variable?
            if (curr.indexOf("LET") == 0) {
                // Asegúrate de que sea una definición válida.
                if (curr.indexOf("=") == -1 || curr.indexOf("=") == 3 || curr.length() == 4) {
                    return "<html>[Error ensamblador] Declaración de variable no válida: " + curr + ".</html>";
                }
                String varName = curr.substring(3, curr.indexOf("="));
                String varVal = curr.substring(curr.indexOf("=") + 1);

                // Asegúrese de que el nombre esté estrictamente alfabético
                for (int j = 0; j < varName.length(); j++) {
                    if (varName.charAt(j) >= '0' && varName.charAt(j) <= '9') {
                        return "<html>[Error ensamblador] Los nombres de variables no pueden contener números.</html>";
                    }
                }

                // Asegúrese de que el valor de la variable sea válido
                if (!isValidBinaryString(varVal)) {
                    return "<html>[Error ensamblador] Valor '" + varVal + "' en la variable no válido.</html>";
                }

                if (varVal.length() == 0) {
                    return "<html>[Error ensamblador] Todas las variables deben definirse con un valor.</html>";
                }

                if (varVal.length() != 8) {
                    return "<html>[Error ensamblador] Todas las variables deben definirse con exactamente 8 bits</html>";
                }

                // Comprobar si hay una declaración de variable duplicada
                for (String s : varValLookup.keySet()) {
                    if (s.contentEquals(varName)) {
                        return "<html>[Error ensamblador] Declaración de variable duplicada  '" + s + "'.</html>";
                    }
                }
                // Añadir al mapa
                varValLookup.put(varName, varVal);
                varAddressLookup.put(varName, nextAddress--);
                deletionIndices.add(curr);
            }
        }

        // Eliminar declaraciones de variables del programa
        for (String i : deletionIndices) {
            result.remove(i);
        }

        // Analizar etiquetas
        Map<String, String> labelLookup = new TreeMap<String, String>();

        int pos = 0;
        for (int i = 0; i < result.size(); i++) {
            String curr = result.get(i);

            if (curr.length() > 0 && curr.charAt(0) == '.') {

                // Si la etiqueta actual no está definida
                if (curr.length() == 1 || curr.substring(1) == null || curr.substring(1).trim().length() == 0 || curr.substring(1).isEmpty()) {
                    return "<html>[Error ensamblador] La definición de la etiqueta está incompleta.</html>";
                }

                // Si la etiqueta actual ya se ha utilizado
                if (labelLookup.containsKey(curr.substring(1))) {
                    return "<html>[Error ensamblador] La etiqueta '" + curr + "' se ha definido más de una vez.</html>";
                }

                // Agregar etiqueta a nuestra tabla de búsqueda
                labelLookup.put(curr.substring(1), padBinaryString4Bits(Integer.toBinaryString(pos)));
            } else {
                pos++;
            }
        }

        // Eliminar todas las etiquetas del arreglo (List<String>)
        for (String s : labelLookup.keySet()) {
            result.remove("." + s);
        }

        // Validar que tenemos suficiente memoria para el programa
        if (result.size() + deletionIndices.size() > 16) {
            return "<html>[Error ensamblador] No se puede compilar el programa en los 16 bytes.</html>";
        }

        // Toma todas las etiquetas
        Set<String> labelsSet = labelLookup.keySet();
        List<String> labels = new ArrayList<String>();
        labels.addAll(labelsSet);

        // Ordenar etiquetas por longitud descendente
        labels.sort(Comparator.comparingInt(String::length));

        // Reemplazar etiquetas con ubicaciones numéricas
        for (int i = 0; i < result.size(); i++) {
            // Comprobar todas las claves para una coincidencia
            for (int j = labels.size() - 1; j >= 0; j--) {
                String currLabel = labels.get(j);
                if (result.get(i).indexOf(currLabel) != -1
                        && result.get(i).substring((result.get(i).indexOf(currLabel))).contentEquals(currLabel)) {
                    // ¡Coincide!
                    String s = result.get(i);

                    s = s.substring(0, s.indexOf(currLabel)) + labelLookup.get(currLabel);
                    // Reemplazar valor en la matriz
                    result.set(i, s);
                }
            }
        }

        // Asegúrese de que todas las instrucciones JMP/JC/JZ que usaban etiquetas fueron reemplazadas
        for (int i = 0; i < result.size(); i++) {
            // Toma la instrucción actual
            String curr = result.get(i);

            // Comprobar si es una sucursal
            if (curr.indexOf("JMP") != -1) {
                if (curr.substring(3).matches("[a-zA-Z]+")) {
                    return "<html>[Error ensamblador] No se puede compilar la siguiente instrucción: " + curr + "</html>";
                }
            } else if (curr.indexOf("JC") != -1) {
                if (curr.substring(2).matches("[a-zA-Z]+")) {
                    return "<html>[Error ensamblador] No se puede compilar la siguiente instrucción: " + curr + "</html>";
                }
            } else if (curr.indexOf("JZ") != -1) {
                if (curr.substring(2).matches("[a-zA-Z]+")) {
                    return "<html>[Error ensamblador] No se puede compilar la siguiente instrucción: " + curr + "</html>";
                }
            }
        }

        String[] rArr = new String[16];

        // Agregar variables a la memoria
        for (String v : varAddressLookup.keySet()) {
            rArr[varAddressLookup.get(v)] = varValLookup.get(v);
        }

        // convertir cada instrucción a código máquina
        for (int i = 0; i < result.size(); i++) {
            // Toma la instrucción actual
            String curr = result.get(i);

            // Agregar valor binario
            SistemaSAP.TipoInstruccion iVal = parseInstruction(curr);
            switch (iVal) {
                case NOP:
                    // Establecer el OP Code (instrucción)
                    rArr[i] = "00000000";
                    break;
                case LDA:
                    // Agregar el código OP
                    rArr[i] = "0001";

                    // Validar que se proporcionó un argumento
                    if (curr.length() == 3) {
                        return "<html>[Error ensamblador] Argumento faltante en LDA</html>";
                    }

                    // Convertir argumento a binario
                    if (argToBinary(curr.substring(3), varAddressLookup) == null) {
                        return "<html>[Error ensamblador] Variable faltante.</html>";
                    }
                    rArr[i] += argToBinary(curr.substring(3), varAddressLookup);

                    break;
                case ADD:
                    // OP Code
                    rArr[i] = "0010";

                    // Validar que se proporcionó un argumento
                    if (curr.length() == 3) {
                        return "<html>[Error ensamblador] Argumento faltante en ADD</html>";
                    }

                    // a binario
                    if (argToBinary(curr.substring(3), varAddressLookup) == null) {
                        return "<html>[Error ensamblador] Variable faltante.</html>";
                    }
                    rArr[i] += argToBinary(curr.substring(3), varAddressLookup);

                    break;
                case SUB:
                    // OP Code
                    rArr[i] = "0011";
                    
                    if (curr.length() == 3) {
                        return "<html>[Error ensamblador] Argumento faltante en SUB</html>";
                    }

                    // a binario
                    if (argToBinary(curr.substring(3), varAddressLookup) == null) {
                        return "<html>[Error ensamblador] Variable faltante.</html>";
                    }
                    rArr[i] += argToBinary(curr.substring(3), varAddressLookup);

                    break;
                case STA:
                    // OP Code
                    rArr[i] = "0100";

                    if (curr.length() == 3) {
                        return "<html>[Error ensamblador] Argumento faltante en STA</html>";
                    }

                    if (argToBinary(curr.substring(3), varAddressLookup) == null) {
                        return "<html>[Error ensamblador] Variable faltante.</html>";
                    }
                    rArr[i] += argToBinary(curr.substring(3), varAddressLookup);

                    break;
                case LDI:
                    // OP Code
                    rArr[i] = "0101";

                    // argumento 
                    if (curr.length() != 7) {
                        return "<html>[Error ensamblador] LDI debe tomar una cadena binaria de 4 bits</html>";
                    }

                    // Tomar el argumento
                    String argLDI = curr.substring(3);

                    // Asegúrese de que el argumento LDI sea una cadena binaria de 4 bits
                    if (!isValidBinaryString(argLDI)) {
                        return "<html>[Error ensamblador] LDI debe tomar una cadena binaria de 4 bits</html>";
                    }

                    // Agregar argumento al programa compilado
                    rArr[i] += padBinaryString4Bits(argLDI);

                    break;
                case JMP:
                    // existe argumento?
                    if (curr.length() == 3) {
                        return "<html>[Error ensamblador] Falta el argumento en JMP. </html>";
                    }

                    // OP Code
                    rArr[i] = "0110";

                    // Toma el argumento
                    String arg = curr.substring(3);

                    // Case 0: No hay argumento 
                    if (arg == null || arg.length() == 0) {
                        return "<html>[Error ensamblador] No se ha dado ningún argumento a la instrucción JMP.</html>";
                    }

                    // Case 1: El argumento no es una cadena binaria válida
                    if (!this.isValidBinaryString(arg)) {
                        return "<html>[Error ensamblador] El argumento de JMP debe ser una cadena binaria válida.</html>";
                    }

                    // Case 2: El argumento es binario.
                    rArr[i] += padBinaryString4Bits(arg);

                    break;
                case JC:
                    // argumento existe?
                    if (curr.length() == 2) {
                        return "<html>[Error ensamblador] Falta el argumento en JC. </html>";
                    }

                    // OP Code
                    rArr[i] = "0111";

                    // Toma el argumento
                    String argC = curr.substring(2);

                    // Case 0 : No hay argumento 
                    if (argC == null || argC.length() == 0) {
                        return "<html>[Error ensamblador] No se ha dado ningún argumento a la instrucción JC.</html>";
                    }

                    // Case 1: El argumento no es una cadena binaria válida
                    if (!this.isValidBinaryString(argC)) {
                        return "<html>[Error ensamblador] El argumento de JC debe ser una cadena binaria válida.</html>";
                    }

                    // Case 2: AEl argumento es binario.
                    rArr[i] += padBinaryString4Bits(argC);

                    break;
                case JZ:
                    // argumento existe?
                    if (curr.length() == 2) {
                        return "<html>[Error ensamblador] Falta el argumento en JZ. </html>";
                    }

                    // Establecer los 4 bits más significativos
                    rArr[i] = "1000";

                    // Analizar el argumento
                    String argZ = curr.substring(2);

                    // Case 1 : No hay argumento 
                    if (argZ == null || argZ.length() != 4) {
                        return "<html>[Error ensamblador] No se ha dado ningún argumento a la instrucción JZ.</html>";
                    }

                    // Case 2: El argumento no es una cadena binaria válida
                    if (!this.isValidBinaryString(argZ)) {
                        return "<html>[Error ensamblador] El argumento de JZ debe ser una cadena binaria válida.</html>";
                    }

                    // Case 3: Argumento es binario
                    rArr[i] += padBinaryString4Bits(argZ);

                    break;
                case OUT:
                    // Asegúrese de que no se proporcionó ningún argumento
                    if (curr.length() != 3) {
                        return "<html>[Error ensamblador] La instrucción OUT no tiene parámetros.</html>";
                    }

                    // Op Code
                    rArr[i] = "11100000";

                    break;
                case HLT:
                    // Asegúrese de que no se proporcionó ningún argumento
                    if (curr.length() != 3) {
                        return "<html>[Error ensamblador] La instrucción HLT no tiene parámetros.</html>";
                    }

                    // Op Code
                    rArr[i] = "11110000";

                    break;
                default:
                    return "<html>[Error ensamblador] No se puede analizar la instrucción: '" + curr + "'.</html>";
            }
        }

        rVal = "<html>";
        for (int i = 0; i < rArr.length; i++) {
            rVal += i + ": " + (rArr[i] == null ? "00000000" : rArr[i]) + "<br>";
        }
        return rVal + "</html>";
    }

    private String padBinaryString4Bits(String arg) {
        if (arg.length() == 0) {
            return "0000";
        } else if (arg.length() == 1) {
            return "000" + arg;
        } else if (arg.length() == 2) {
            return "00" + arg;
        } else if (arg.length() == 3) {
            return "0" + arg;
        } else {
            return arg;
        }
    }

    private SistemaSAP.TipoInstruccion parseInstruction(String curr) {
        if (curr.indexOf("ADD") != -1) {
            return SistemaSAP.TipoInstruccion.ADD;
        } else if (curr.indexOf("SUB") != -1) {
            return SistemaSAP.TipoInstruccion.SUB;
        } else if (curr.indexOf("NOP") != -1) {
            return SistemaSAP.TipoInstruccion.NOP;
        } else if (curr.indexOf("LDA") != -1) {
            return SistemaSAP.TipoInstruccion.LDA;
        } else if (curr.indexOf("STA") != -1) {
            return SistemaSAP.TipoInstruccion.STA;
        } else if (curr.indexOf("LDI") != -1) {
            return SistemaSAP.TipoInstruccion.LDI;
        } else if (curr.indexOf("JMP") != -1) {
            return SistemaSAP.TipoInstruccion.JMP;
        } else if (curr.indexOf("JZ") != -1) {
            return SistemaSAP.TipoInstruccion.JZ;
        } else if (curr.indexOf("JC") != -1) {
            return SistemaSAP.TipoInstruccion.JC;
        } else if (curr.indexOf("OUT") != -1) {
            return SistemaSAP.TipoInstruccion.OUT;
        } else if (curr.indexOf("HLT") != -1) {
            return SistemaSAP.TipoInstruccion.HLT;
        }
        return SistemaSAP.TipoInstruccion.INVALID;
    }

    private boolean isValidBinaryString(String value) {
        for (int i = 0; i < value.length(); i++) {
            int tempB = value.charAt(i);
            if (tempB != '0' && tempB != '1') {
                return false;
            }
        }
        return true;
    }

    private String argToBinary(String arg, Map<String, Integer> varAddressLookup) {
        // Validar entrada
        if (arg == null || arg.length() == 0) {
            return "";
        }

        // Comprobar el literal binario
        if (isValidBinaryString(arg)) {
            if (arg.length() == 0) {
                return "0000";
            } else if (arg.length() == 1) {
                return "000" + arg;
            } else if (arg.length() == 2) {
                return "00" + arg;
            } else if (arg.length() == 3) {
                return "0" + arg;
            } else {
                return arg;
            }
        }

        // Significa que tenemos una variable
        Integer val = varAddressLookup.get(arg);
        if (val == null) {
            return null;
        }

        String address = Integer.toBinaryString(val);
        if (address.length() == 0) {
            return "0000";
        } else if (address.length() == 1) {
            return "000" + address;
        } else if (address.length() == 2) {
            return "00" + address;
        } else if (address.length() == 3) {
            return "0" + address;
        } else {
            return address;
        }
    }

    private String argTo4BitString(byte arg) {
        String rVal = Integer.toBinaryString(0b1111 & arg) + "";

        if (rVal.length() == 4) {
            return rVal;
        } else if (rVal.length() == 3) {
            return "0" + rVal;
        } else if (rVal.length() == 2) {
            return "00" + rVal;
        } else if (rVal.length() == 1) {
            return "000" + rVal;
        } else {
            return "0000";
        }
    }
    

}
