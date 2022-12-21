package logica;

import interfaces.IRegistro;

public class Registro4Bit implements IRegistro {

    private byte valor;

    public Registro4Bit() {
        this.valor = 0;
    }

    @Override
    // Carga valor de 4 bits en el registro
    public void setValor(byte v) {
        // Asegúrese de que la entrada sea de 4 bits
        if (v > 0b1111) {
            throw new RuntimeException();
        }
        this.valor = v;
    }

    @Override
    // Getter
    public byte getValor() {
        return this.valor;
    }

    @Override
    // Reset del registro
    public void clear() {
        this.valor = 0;
    }

}
