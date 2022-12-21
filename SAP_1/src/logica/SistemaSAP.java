package logica; 

import interfaces.IClockObserver;
import interfaces.IRegistro;
import interfaces.SAPObserver;
import java.util.ArrayList;
import java.util.List;

public class SistemaSAP implements IClockObserver {
    
    // Asignamos valores enteros constantes a cada señal de línea de control
    public static final int FIN = 0;    //DETENCION
    public static final int MI = 1;     //REGISTRO DE ACCESO A MEMORIA MAR
    public static final int RI = 2;     //RAM
    public static final int RO = 3;     //RAM
    public static final int IO = 4;     //REGISTRO DE INSTRUCCION
    public static final int II = 5;     //REGISTRO DE INSTRUCCION
    public static final int AI = 6;     //REGISTRO A
    public static final int AO = 7;     //REGISTRO A
    public static final int SO = 8;     //ALU
    public static final int SU = 9;     //ALU
    public static final int BI = 10;    //REGISTRO B
    public static final int OI = 11;    //REGISTRO OUT
    public static final int CE = 12;    //PROGRAM COUNTER
    public static final int CO = 13;    //PROGRAM COUNTER
    public static final int J = 14;     //
    public static final int FI = 15;    //FLAGS
    
    // Enumera los tipos de registros válidos en SAP-1
    public enum TipoRegistro {
        A, B, ALU, IR, OUT, PC, MAR, BUS
    }

    //ISA
    
    //NIN: NINGUNA OPERACION
    //CAR: CARGAR EN LA DIRECCION X
    //SUM: SUMAR  
    //RES: REALMR   
    //ALM: ALMACENAR EN LA DIRECCION X
    //CAI: CARGAR INDIRECTAMENTE EN LA DIRECCION X
    //SAL: SALTO
    //SMI: SALTA SI ES MAYOR O IGUAL DESPUES DE COMPARAR
    //SIG: SALTA SI SON IGUALES DESPUES DE COMPARAR
    //OUT: SALIDA
    //FIN: DETENER EJECUCION
    //INVALID: INVALIDO
    public enum TipoInstruccion {
        NIN, CAR, SUM, RES, ALM, CAI, SAL, SMI, SIG, OUT, FIN, INVALID
    }

    // Contenido del SAP-1
    private IRegistro registroA;
    private IRegistro registroB;
    private IRegistro registroSalida;
    private IRegistro registroIR;
    private IRegistro registroMAR;
    private IRegistro bus;
    private ProgramCounter programCounter;
    private byte stepCount;
    private Memoria RAM;
    private EventLog log;
    private ALU alu;
    private boolean[] lineasControl;    
    
    // Dado que SAP es observable, debe mantener una lista de sus observadores (que es solo la Vista en esta implementación)
    private List<SAPObserver> observers;

    public SistemaSAP() {
        this.registroA = new Registro8Bit();
        this.registroB = new Registro8Bit();
        this.registroSalida = new Registro8Bit();
        this.registroIR = new Registro8Bit();
        this.registroMAR = new Registro4Bit();
        this.programCounter = new ProgramCounter();
        this.stepCount = 0;
        this.RAM = new Memoria(this.registroMAR);
        this.log = EventLog.getEventLog();
        this.alu = new ALU(this.registroA, this.registroB);
        this.lineasControl = new boolean[16];
        
        this.bus = new Registro8Bit();

        
        // Registrar el modelo como observador de reloj.
        Clock.getClock().addObserver(this);

        // Observadores del sistema
        this.observers = new ArrayList<SAPObserver>();
                
        this.cambioReloj();
    }

    public void reset() {
        // Informar al log
        this.log.addEntrada("Usuario ha solicitado hacer reset en el SAP...");

        // Reinicie el reloj si y solo si el reloj está alto
        if (Clock.getClock().getEstado()) {
            Clock.getClock().toggleClock();
        }

        // Borrar todos los registros y otros valores de datos (excepto la memoria)
        this.registroA.clear();
        this.registroB.clear();
        this.registroSalida.clear();
        this.programCounter.clear();
        this.registroIR.clear();
        this.registroMAR.clear();
        this.bus.clear();
        this.stepCount = 0;
        this.alu.getRegistroEstados().clear();
        this.resetTodasLineasControl();

        // Notificar a los observadores para que la vista pueda volver a pintarse.
        for (SAPObserver o : observers) {
            o.cambioRegistroA(this.registroA.getValor());
            o.cambioRegistroB(this.registroB.getValor());
            o.cambioOUT(this.registroSalida.getValor());
            o.cambioPC(this.programCounter.getValor());
            o.cambioIR(this.registroIR.getValor());
            o.cambioConteoPaso(this.stepCount);
            o.cambioMAR(this.registroMAR.getValor());
            o.cambioBUS(this.bus.getValor());
            o.cambioLineasControl();
            o.cambioFLAGS();
        }        
        this.cambioReloj();
        
    }

    private void resetTodasLineasControl() {
        // Establecer todas las líneas de control en falso
        for (int i = 0; i < 16; i++) {
            this.lineasControl[i] = false;
        }

        // Nada está poniendo su valor en el bus, así que se limpia
        this.bus.setValor((byte) 0);

        // Informamos a la vista que vuelva a pintar el bus
        this.notificarCambioBUS();

        // Si el reloj se detiene, eliminar esa restricción
        Clock.getClock().setActivar(false);
    }

    private TipoInstruccion decodificarIR() {
        // Obtener el valor almacenado en el registro de instrucciones
        byte instruccion = this.registroIR.getValor();

        // Descartar los cuatro bits menos significativos
        instruccion = (byte) (instruccion & 0b11110000);

        // Analizar el valor
        return decodificarInstruccion(instruccion);
    }

    // Método auxiliar que analiza un byte y encuentra su tipo de instrucción
    public TipoInstruccion decodificarInstruccion(byte i) {
        byte instruccion = (byte) (i>>4);
        instruccion = (byte) (instruccion & 0b00001111);
        //System.out.printf("INS: %x\n", instruccion);
        switch (instruccion) {
            case 0:
                return TipoInstruccion.NIN;
            case 1:
                return TipoInstruccion.CAR;
            case 2:
                return TipoInstruccion.SUM;
            case 3:
                return TipoInstruccion.RES;
            case 4:
                return TipoInstruccion.ALM;
            case 5:
                return TipoInstruccion.CAI;
            case 6:
                return TipoInstruccion.SAL;
            case 7:
                return TipoInstruccion.SMI;
            case 8:
                return TipoInstruccion.SIG;
            case 14:
                return TipoInstruccion.OUT;
            case 15:
                return TipoInstruccion.FIN;
            default:
                // Las instrucciones 0b1001, 0b1010, 0b1011, 0b1100, 0b1101 (9-13) no están implementadas
                return TipoInstruccion.INVALID;
        }
         
    }

    public void analizarInstruccion(byte address) {
        // Comience a construir una entrada para el registro
        String log = "[" + address + "]\t";

        // Primero, agregue la instrucción al Log
        byte instructionVal = (byte) (this.getRAM().getData()[address] & 0b11110000);
        TipoInstruccion t = decodificarInstruccion(instructionVal);

        // Manejar el resultado de la instrucción decodificada
        switch (t) {
            case NIN:
                log += "NIN";
                break;
            case CAR:
                log += "CAR";
                break;
            case SUM:
                log += "SUM";
                break;
            case RES:
                log += "RES";
                break;
            case ALM:
                log += "ALM";
                break;
            case CAI:
                log += "CAI";
                break;
            case SAL:
                log += "SAL";
                break;
            case SMI:
                log += "SMI";
                break;
            case SIG:
                log += "SIG";
                break;
            case OUT:
                log += "OUT";
                break;
            case FIN:
                log += "FIN";
                break;
            default:
                log += "N/A";
        }

        // Luego, agregue el argumento al Log
        log += " ";
        if (t != TipoInstruccion.NIN && t != TipoInstruccion.INVALID && t != TipoInstruccion.FIN
                && t != TipoInstruccion.OUT) {
            log += this.getRAM().getData()[address] & 0b00001111;
        }

        // Finalmente, agregue el valor decimal
        log += "\t" + this.getRAM().getData()[address];

        // Agregue la cadena analizada final al registro de eventos
        EventLog.getEventLog().addEntrada(log);
    }

    @Override
    public void cambioReloj() {
        // Si el reloj acaba de cambiar, incremente el conteo de pasos
        if (!Clock.getClock().getEstado()) {
            if (this.stepCount == 5) {
                this.stepCount = 1;
            } else {
                this.stepCount++;
            }
            this.notificarCambioStepCounter();
            EventLog.getEventLog().addEntrada("Contador de pasos actualizado a " + this.stepCount);

            switch (this.stepCount) {
                case 1:
                    // Si estamos en el ciclo 1, configure las líneas manualmente
                    this.resetTodasLineasControl();
                    this.lineasControl[CO] = true;
                    this.lineasControl[MI] = true;
                    notificarCambioLineasControl();
                    EventLog.getEventLog().addEntrada("[FETCH] Activando líneas de control: CO, MI");
                    break;
                case 2:
                    // Si estamos en el ciclo 2, configure las líneas manualmente
                    this.resetTodasLineasControl();
                    this.lineasControl[CE] = true;
                    this.lineasControl[RO] = true;
                    this.lineasControl[II] = true;
                    notificarCambioLineasControl();
                    EventLog.getEventLog().addEntrada("[FETCH] Activando líneas de control: CE, RO, II");
                    break;
                default:
                    // Averiguar qué instrucción estamos ejecutando
                    TipoInstruccion instruccionActual = this.decodificarIR();
                    if (instruccionActual == TipoInstruccion.NIN) {
                        if (this.stepCount == 3) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("NIN => No lineas de control");
                        }
                        if (this.stepCount == 4) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("NIN => No lineas de control");
                        }
                        if (this.stepCount == 5) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("NIN => No lineas de control");
                        }                        
                    }
                    if (instruccionActual == TipoInstruccion.CAR) {
                        if (this.stepCount == 3) {
                            this.resetTodasLineasControl();
                            this.lineasControl[IO] = true;
                            this.lineasControl[MI] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("CAR => IO, MI activadas");
                        }
                        if (this.stepCount == 4) {
                            this.resetTodasLineasControl();
                            this.lineasControl[RO] = true;
                            this.lineasControl[AI] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("CAR => RO, AI activadas");
                        }
                        if (this.stepCount == 5) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("CAR => No hace nada");
                        }
                    }
                    if (instruccionActual == TipoInstruccion.SUM) {
                        if (this.stepCount == 3) {
                            this.resetTodasLineasControl();
                            this.lineasControl[IO] = true;
                            this.lineasControl[MI] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("SUM => IO, MI activadas");
                        }
                        if (this.stepCount == 4) {
                            this.resetTodasLineasControl();
                            this.lineasControl[RO] = true;
                            this.lineasControl[BI] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("SUM => RO, BI activadas");
                        }
                        if (this.stepCount == 5) {
                            this.resetTodasLineasControl();
                            this.lineasControl[SO] = true;
                            this.lineasControl[FI] = true;
                            this.lineasControl[AI] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("SUM => ∑O, FI, AI activadas");
                        }
                    }
                    if (instruccionActual == TipoInstruccion.RES) {
                        if (this.stepCount == 3) {
                            this.resetTodasLineasControl();
                            this.lineasControl[IO] = true;
                            this.lineasControl[MI] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("RES => IO, MI activadas");
                        }
                        if (this.stepCount == 4) {
                            this.resetTodasLineasControl();
                            this.lineasControl[RO] = true;
                            this.lineasControl[BI] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("RES => RO, BI activadas");
                        }
                        if (this.stepCount == 5) {
                            this.resetTodasLineasControl();
                            this.lineasControl[SU] = true;
                            this.lineasControl[SO] = true;
                            this.lineasControl[FI] = true;
                            this.lineasControl[AI] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("RES => ∑O, SU, AI, FI activadas");
                        }
                    }
                    if (instruccionActual == TipoInstruccion.ALM) {
                        if (this.stepCount == 3) {
                            this.resetTodasLineasControl();
                            this.lineasControl[IO] = true;
                            this.lineasControl[MI] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("ALM => IO, MI activadas");
                        }
                        if (this.stepCount == 4) {
                            this.resetTodasLineasControl();
                            this.lineasControl[AO] = true;
                            this.lineasControl[RI] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("ALM => AO, RI activadas");

                        }
                        if (this.stepCount == 5) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("ALM => No hace nada");
                        }
                    }
                    if (instruccionActual == TipoInstruccion.CAI) {
                        if (this.stepCount == 3) {
                            this.resetTodasLineasControl();
                            this.lineasControl[IO] = true;
                            this.lineasControl[AI] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("CAI => IO, AI activadas");
                        }
                        if (this.stepCount == 4) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("CAI => No hace nada");
                        }
                        if (this.stepCount == 5) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("CAI => No hace nada");
                        }
                    }
                    if (instruccionActual == TipoInstruccion.SAL) {
                        if (this.stepCount == 3) {
                            this.resetTodasLineasControl();
                            this.lineasControl[IO] = true;
                            this.lineasControl[J] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("SAL => IO, J activadas");
                        }
                        if (this.stepCount == 4) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("SAL => No hace nada");
                        }
                        if (this.stepCount == 5) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("SAL => No hace nada");
                        }
                    }
                    if (instruccionActual == TipoInstruccion.SMI) {
                        if (this.stepCount == 3) {
                            this.resetTodasLineasControl();
                            if (this.getFlags().getCF()) {
                                this.lineasControl[IO] = true;
                                this.lineasControl[J] = true;
                                EventLog.getEventLog().addEntrada("SMI => IO, J activadas ya que CF=1");
                            } else {
                                EventLog.getEventLog().addEntrada("SMI => IO, No hace nada ya que CF=0");
                            }
                            notificarCambioLineasControl();
                        }

                        if (this.stepCount == 4) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("SMI => No hace nada");
                        }

                        if (this.stepCount == 5) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("SIG => No hace nada");
                        }
                    }
                    if (instruccionActual == TipoInstruccion.SIG) {
                        if (this.stepCount == 3) {
                            this.resetTodasLineasControl();
                            if (this.getFlags().getZF()) {
                                this.lineasControl[IO] = true;
                                this.lineasControl[J] = true;
                                EventLog.getEventLog().addEntrada("SIG => IO, J activadas ya que ZF=1");
                            } else {
                                EventLog.getEventLog().addEntrada("SIG => No hace nada ya que ZF=0");
                            }
                            notificarCambioLineasControl();
                        }
                        if (this.stepCount == 4) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("SIG => No hace nada");
                        }
                        if (this.stepCount == 5) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("SIG => No hace nada");
                        }
                    }
                    if (instruccionActual == TipoInstruccion.OUT) {
                        if (this.stepCount == 3) {
                            this.resetTodasLineasControl();
                            this.lineasControl[AO] = true;
                            this.lineasControl[OI] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("OUT => AO, OI activadas");
                        }
                        if (this.stepCount == 4) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("OUT => No hace nada");
                        }
                        if (this.stepCount == 5) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("OUT => No hace nada");
                        }
                    }
                    if (instruccionActual == TipoInstruccion.FIN) {
                        if (this.stepCount == 3) {
                            this.resetTodasLineasControl();
                            this.lineasControl[FIN] = true;
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("FIN => FIN activada");
                        }
                        // No es necesario manejar stepCount 4 y 5 ya que el 
                        // reloj ya no puede avanzar con FIN habilitado
                    }
                    if (instruccionActual == TipoInstruccion.INVALID) {
                        if (this.stepCount == 3) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("Instrucción Inválida  => No hace nada");
                        }
                        if (this.stepCount == 4) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("Instrucción Inválida  => No hace nada");

                        }
                        if (this.stepCount == 5) {
                            this.resetTodasLineasControl();
                            notificarCambioLineasControl();
                            EventLog.getEventLog().addEntrada("Instrucción Inválida  => No hace nada");
                        }
                    }
                    break;
            }

            // Ahora que hemos configurado todas las señales de flanco descendente, 
            // actualizamos el SAP en consecuencia ya que las instrucciones OUT 
            // no dependen del reloj.
            if (this.lineasControl[CO]) {
                this.bus.setValor(this.programCounter.getValor());
                EventLog.getEventLog().addEntrada("Valor del Program Counter en el bus (4 bits)");
                this.notificarCambioBUS();
            }
            if (this.lineasControl[RO]) {
                this.bus.setValor((byte) this.RAM.memoryOut());
                EventLog.getEventLog().addEntrada("Valor de RAM en el bus");
                this.notificarCambioBUS();
            }
            if (this.lineasControl[IO]) {
                // Coloque 4 bits menos significativos del registro de instrucciones en el bus
                this.bus.setValor((byte) (0b00001111 & this.registroIR.getValor()));
                this.notificarCambioBUS();
                EventLog.getEventLog().addEntrada("Valor del Instruction Register en el bus (4 Bits)");
            }
            if (this.lineasControl[AO]) {
                this.bus.setValor(this.registroA.getValor());
                this.notificarCambioBUS();
                EventLog.getEventLog().addEntrada("Valor del Registro A en el bus");
            }
            if (this.lineasControl[SU]) {
                this.notificarCambioRegistroA();
            }
            if (this.lineasControl[SO]) {
                this.bus.setValor(this.alu.ALUOut(this.lineasControl[SU]));
                this.notificarCambioBUS();
                EventLog.getEventLog().addEntrada("Valor de la operación de la ALU en el bus");
            }
            return;

        } else {
            // Significa que estamos en el flanco ascendente del reloj, manejamos 
            // todas las señales que dependen de un flanco ascendente del reloj.            
            if (this.lineasControl[FI]) {
                this.alu.flagsIn(this.lineasControl[SU]);
                this.notificarCambioRegistroEstados();
                EventLog.getEventLog().addEntrada("Registro de estado actualizado");
            }
            if (this.lineasControl[MI]) {
                this.registroMAR.setValor(this.bus.getValor());
                this.notificarCambioMAR();
                EventLog.getEventLog().addEntrada("MAR lee del bus");

            }
            if (this.lineasControl[CE]) {
                this.programCounter.activarConteo();
                this.notificarCambioPC();
                EventLog.getEventLog().addEntrada("Program Counter incrementa");

            }
            if (this.lineasControl[FIN]) {
                Clock.getClock().setActivar(true);
            }
            if (this.lineasControl[RI]) {
                this.RAM.memoryIn(this.bus.getValor());
                EventLog.getEventLog().addEntrada("RAM lee del bus");
            }

            if (this.lineasControl[II]) {
                this.registroIR.setValor(this.bus.getValor());
                this.notificarCambioIR();
                EventLog.getEventLog().addEntrada("Instruction Register lee del bus");
            }
            if (this.lineasControl[AI]) {
                this.registroA.setValor(this.bus.getValor());
                this.notificarCambioRegistroA();
                EventLog.getEventLog().addEntrada("Registro A lee del bus");
            }
            if (this.lineasControl[BI]) {
                this.registroB.setValor(this.bus.getValor());
                this.notificarCambioRegistroB();
                EventLog.getEventLog().addEntrada("Registro B lee del bus");

            }
            if (this.lineasControl[OI]) {
                this.registroSalida.setValor(this.bus.getValor());
                this.notificarCambioRegistroOUT();
                EventLog.getEventLog().addEntrada("Registro de salida lee del bus");

            }
            if (this.lineasControl[J]) {
                this.programCounter.setValor((byte) (this.bus.getValor() & 0b1111));
                this.notificarCambioPC();
                EventLog.getEventLog().addEntrada("Program Counter cambia por la bandera J");
            }
        }
    }

    // Metodos Getter 
    public Memoria getRAM() {
        return this.RAM;
    }

    public IRegistro getRegistroA() {
        return this.registroA;
    }

    public IRegistro getRegistroB() {
        return this.registroB;
    }

    public ALU getALU() {
        return this.alu;
    }

    public IRegistro getRegistroIR() {
        return this.registroIR;
    }

    public IRegistro getRegistroSalida() {
        return this.registroSalida;
    }

    public ProgramCounter getPC() {
        return this.programCounter;
    }

    public IRegistro getRegistroMAR() {
        return this.registroMAR;
    }

    public boolean[] getControlLines() {
        return this.lineasControl;
    }

    public byte getStepCount() {
        return this.stepCount;
    }

    public IRegistro getBus() {
        return this.bus;
    }

    public RegistroEstados getFlags() {
        return this.alu.getRegistroEstados();
    }

    // Método auxiliar para decodificar el contenido de un registro
    public String decodificarRegistro(TipoRegistro t, int bitPos) {
        byte val = 0;
        if (t != null) 
            switch (t) {
                case A:
                    val = getRegistroA().getValor();
                    break;
                case B:
                    val = getRegistroB().getValor();
                    break;
                case ALU:
                    val = getALU().ALUOut(getControlLines()[9]);
                    break;
                case IR:
                    val = getRegistroIR().getValor();
                    break;
                case OUT:
                    val = getRegistroSalida().getValor();
                    break;
                case PC:
                    val = getPC().getValor();
                    break;
                case MAR:
                    val = getRegistroMAR().getValor();
                    break;
                case BUS:
                    val = getBus().getValor();
                    break;
                default:
                    break;
            }
        return "" + (0b1 & (val >> bitPos));
    }
    
    // Implementación del Patrón Observer
    public void addObserver(SAPObserver o) {
        if (o == null) {
            return;
        }
        this.observers.add(o);
    }

    public void removeObserver(SAPObserver o) {
        if (o == null) {
            return;
        }
        this.observers.remove(o);
    }

    private void notificarCambioLineasControl() {
        for (SAPObserver o : observers) {
            o.cambioLineasControl();
        }
    }

    private void notificarCambioStepCounter() {
        for (SAPObserver o : observers) {
            o.cambioConteoPaso(this.stepCount);
        }
    }

    private void notificarCambioBUS() {
        for (SAPObserver o : observers) {
            o.cambioBUS(this.bus.getValor());
        }
    }

    private void notificarCambioMAR() {
        for (SAPObserver o : observers) {
            o.cambioMAR(this.registroMAR.getValor());
        }
    }

    private void notificarCambioPC() {
        for (SAPObserver o : observers) {
            o.cambioPC(this.programCounter.getValor());
        }
    }

    private void notificarCambioIR() {
        for (SAPObserver o : observers) {
            o.cambioIR(this.registroIR.getValor());
        }
    }

    private void notificarCambioRegistroA() {
        for (SAPObserver o : observers) {
            o.cambioRegistroA(this.registroA.getValor());
        }
    }

    private void notificarCambioRegistroB() {
        for (SAPObserver o : observers) {
            o.cambioRegistroB(this.registroB.getValor());
        }
    }

    private void notificarCambioRegistroOUT() {
        for (SAPObserver o : observers) {
            o.cambioOUT(this.registroSalida.getValor());
        }
    }

    private void notificarCambioRegistroEstados() {
        for (SAPObserver o : observers) {
            o.cambioFLAGS();
        }
    }
}
