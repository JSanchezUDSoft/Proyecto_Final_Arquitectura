package logica;

import java.util.ArrayList;
import java.util.List;

import interfaces.ILogObserver;

/* 
El registro de eventos es responsable de encapsular información sobre cambios 
significativos realizados el sistema. 
El registro de eventos se muestra al usuario. 
El registro de eventos se ha creado como único, lo que significa que solo una 
instancia del la clase puede existir (Singleton).
 */

public class EventLog {
    /*
    La implementación de la clase se basa en mantener una lista de cadenas 
    que representan todos los eventos que han ocurrido en el registro.
     */
    private List<String> eventos;

    // Usando el patrón de diseño observer, debemos mantener una lista de LogObservers
    private List<ILogObserver> observers;

    private static EventLog eventLog;

    private EventLog() {
        eventos = new ArrayList<String>();
        observers = new ArrayList<ILogObserver>();
    }

    /*
    Método estático que se usa para acceder a la instancia del Singleton. 
    Si aún no hay una instancia, creamos una y la devolvemos. Si ya existe, la
    devolvemos.
     */
    public static EventLog getEventLog() {
        if (eventLog == null) {
            eventLog = new EventLog();            
        } 
        return eventLog;      
    }

    // Agrega una entrada al registro y notifica a los observadores apropiados
    public void addEntrada(String entrada) {
        // Revisar la entrada antes de agregar al registro
        if (entrada == null) {
            throw new RuntimeException("Cadena nula pasada al registro addEntrada()");
        }
        eventos.add(entrada);

        // Ahora que hemos cambiado el registro, notifique a los observadores para que 
        // la interfaz de usuario pueda volver a dibujarse.
        notifyObservers(entrada);
    }

    // implementación del Observer
    public void addObserver(ILogObserver o) {
        observers.add(o);
    }

    public void removeObserver(ILogObserver o) {
        observers.remove(o);
    }

    public void notifyObservers(String s) {
        for (ILogObserver o : observers) {
            o.agregaEntradaLog(s);
        }
    }

}
