package logica;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import interfaces.IRAMObserver;
import interfaces.IRegistro;

public class Memoria {
    
    private int capacidadMemoria; // capacidad de la memoria en bytes
    
    // Mantener los valores de RAM como un arreglo de bytes
    private byte[] data;

    // La memoria mantiene el direccionamiento como una referencia al registro de direcciones de memoria.
    private IRegistro MAR;

    // La memoria es observable, por lo que debe mantener una lista de observadores
    private List<IRAMObserver> observers;

    public Memoria(IRegistro MAR) {
        capacidadMemoria = 16;
        
        this.data = new byte[capacidadMemoria];
        this.MAR = MAR;
        this.observers = new ArrayList<IRAMObserver>();

        // Cargar valores basura en la memoria (para probar)
        for (int i = 0; i < 16; i++) {
            this.data[i] = (byte) ThreadLocalRandom.current().nextInt(0, 254);
        }

    }

    // Carga un valor en la dirección de memoria contenida en el registro de direcciones de memoria MAR
    public void memoryIn(byte val) {
        this.data[this.MAR.getValor()] = val;
        this.notifyObservers(this.MAR.getValor());
    }    

    // Devuelve los datos almacenados en la dirección contenida en el registro de direcciones de memoria
    public int memoryOut() {
        return this.data[(int) this.MAR.getValor()];
    }

    // Cambia manualmente una dirección de memoria (utilizada en WidgetRAM)
    public void cambiarValor(int address, byte newVal) {
        this.data[address] = newVal;
        this.notifyObservers(address);
    }
    // Devuleve el contenido de la memoria
    public byte[] getData() {
        return this.data;
    }

    // Métodos para implementar el patrón de diseño Observer
    public void addRAMObserver(IRAMObserver o) {
        if (o == null) {
            return;
        }
        this.observers.add(o);
    }

    public void removeRAMObserver(IRAMObserver o) {
        if (o == null) {
            return;
        }
        this.observers.remove(o);
    }

    private void notifyObservers(int address) {
        for (IRAMObserver o : observers) {
            o.cambiaValorRAM(address);
        }
    }

}
