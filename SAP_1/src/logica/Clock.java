package logica;

import java.util.ArrayList;
import java.util.List;

import interfaces.IClockObserver;

// Singleton  
public class Clock {
    // false -> reloj bajo; true -> reloj alto

    private boolean estado;

    // Si es verdadero, el estado del reloj no puede cambiar
    private boolean detenido;

    // Se utiliza para el espaciado en el registro de eventos
    private boolean firstToggleSpacing;

    // Dado que el reloj es observable, necesita mantener una lista de observadores (Patrón Observer)
    private List<IClockObserver> observers;

    // Singleton (Debe mantener una referencia static a sí mismo)
    private static Clock clock;

    private Clock() {
        this.estado = false;
        this.detenido = false;
        this.firstToggleSpacing = false;
        observers = new ArrayList<IClockObserver>();
    }

    public static Clock getClock() {
        // Si no tenemos referenciado en memoria el reloj, hacer uno nuevo; de lo contrario, devuelve el reloj actual
        if (clock == null) {
            clock = new Clock();
        }
        return clock;
    }

    // Devuelve false si el reloj está en estado bajo, true si el reloj está en estado alto
    public boolean getEstado() {
        return getClock().estado;
    }

    // Proporciona la capacidad de detener o reactivar el reloj.
    public void setActivar(boolean v) {
        this.detenido = v;
    }

    // Intercambia el estado del reloj
    public void toggleClock() {
        // Valida que el reloj exista
        getClock();

        // Si está detenido, no hacer nada
        if (this.detenido) {
            return;
        }

        // Invertir el estado
        this.estado = !this.estado;

        // Para gestionar el Log
        // Agregar espacio, excepto si es el primer cambio de reloj
        if (firstToggleSpacing) {
            EventLog.getEventLog().addEntrada("\n");
        } else {
            firstToggleSpacing = true;
        }

        // Agregar el evento al Log
        if (estado) {
            EventLog.getEventLog().addEntrada("Flanco ascendente del reloj");

        } else {
            EventLog.getEventLog().addEntrada("Flanco descendente del reloj");
        }

        // Le informa a los observadores que el reloj ha cambiado.
        notifyObservers();
        return;
    }

    // Implementación del Patrón Observer
    public void addObserver(IClockObserver o) {
        if (o == null) {
            return;
        }
        this.observers.add(o);
    }

    public void removeObserver(IClockObserver o) {
        if (o == null) {
            return;
        }
        this.observers.remove(o);
    }

    public void notifyObservers() {
        for (IClockObserver o : observers) {
            o.cambioReloj();
        }
    }
}
