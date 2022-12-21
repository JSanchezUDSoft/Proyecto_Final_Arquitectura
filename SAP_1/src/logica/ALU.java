package logica;

import interfaces.IRegistro;

public class ALU {

    private IRegistro registroA;
    private IRegistro registroB;
    private RegistroEstados registroEstados;

    public ALU(IRegistro A, IRegistro B) {
        // La ALU mantiene referencias a los dos registros de operandos 
        // además de un registro para almacenar los estados (banderas)
        this.registroA = A;
        this.registroB = B;
        this.registroEstados = new RegistroEstados();
    }

    // Actualiza el registro de banderas en función del valor actual de ALU
    public void flagsIn(boolean sub) {
        boolean zF, cF; //zero y carry flags

        // Calcula es estado de Zero
        int resultado;
        if (sub) {
            resultado = (0b00000000000000000000000011111111 & this.registroA.getValor())
                    - (0b00000000000000000000000011111111 & this.registroB.getValor());
        } else {
            resultado = (0b00000000000000000000000011111111 & this.registroA.getValor())
                    + (0b00000000000000000000000011111111 & this.registroB.getValor());
        }

        if ((resultado & 0b11111111) == 0) {
            zF = true;
        } else {
            zF = false;
        }

        // Calcula el estado de Acarreo
        if ((resultado & 0b100000000) == 0) {
            cF = false;
        } else {
            cF = true;
        }

        // Actualiza los valores la las baderas de estado
        registroEstados.flagsIn(zF, cF);
    }

    // Devuelve el valor actual de la ALU sin el estado
    public byte ALUOut(boolean sub) {
        if (sub) {
            return (byte) (this.registroA.getValor() - this.registroB.getValor());
        } else {
            return (byte) (this.registroA.getValor() + this.registroB.getValor());
        }
    }

    // funciones Getter 
    public boolean getZeroFlag() {
        return this.registroEstados.getZF();
    }

    public boolean getCarryFlag() {
        return this.registroEstados.getCF();
    }

    public RegistroEstados getRegistroEstados() {
        return registroEstados;
    }

    
}
