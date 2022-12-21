package logica;

public class RegistroEstados {
    // Utiliza booleanos para almacenar el contenido del registro.

    private boolean zeroFlag;
    private boolean carryFlag;

    public RegistroEstados() {
        this.zeroFlag = false;
        this.carryFlag = false;
    }

    public void clear() {
        this.zeroFlag = false;
        this.carryFlag = false;
    }

    public void flagsIn(boolean zF, boolean cF) {
        this.zeroFlag = zF;
        this.carryFlag = cF;
    }

    public boolean getZF() {
        return this.zeroFlag;
    }

    public boolean getCF() {
        return this.carryFlag;
    }

    public void setZF(boolean zeroFlag) {
        this.zeroFlag = zeroFlag;
    }

    public void setCF(boolean carryFlag) {
        this.carryFlag = carryFlag;
    }
    
    
}
