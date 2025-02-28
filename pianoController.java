import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;

public class pianoController {
pianoView utsende;
pianoModel keyboard;
Thread thread;
JButton denneKnappen;

public pianoController(){
    keyboard = new pianoModel();
    utsende = new pianoView();
    // System.out.println(keyboard); //testing
}

//Lager keyboardet
public void lagKnapper(){
    utsende.GUI(this);
    }

public void avsluttSpill(){//Avslutter programmet når brukeren trykker på avslutt
    System.exit(0);
    }
}

//Ha to tråd klasser? En for å spille noter, en annen for å spille av akkorder?

//Brukes når brukeren spiller på keyboardet
class noteTask implements Runnable{
    Thread[] traad;
    public noteTask(){
        traad = new Thread[12];
        // traad[i].run();
    }

    //Ha en if-sjekk som henter componenten som har blitt trykket?
    //Spiller progresjonen hvis "PLAY" trykkes, tar opp akkorden hvis "SHOW" trykkes.
    @Override
    public void run(){
         try { 
            for (int i = 0; i < traad.length; i++){
            traad[i] = new Thread(new Traad());
            }
        }
        catch (IndexOutOfBoundsException n) {

        }
    }
}

// //Brukes til å spille noter i en akkord på pianoet fritt
// class akkordTraad implements Runnable{
//     Thread[] traad;
//     ArrayList<String> listeAvNoter;
//     bestFrame frame;
//     public akkordTraad(ArrayList<String> listeAvNoter, bestFrame frame){
//         traad = new Thread[listeAvNoter.size()];
//         this.listeAvNoter = listeAvNoter;
//         this.frame = frame;
//     }

//     @Override
//     public void run(){//skal vente 1 sek, så spille noten i unison
//         for (int t = 0; t < traad.length; t++){
//             try {
//                 frame.spillNote(listeAvNoter.get(t), Color.GREEN);
//                 Thread.sleep(1000); //1sek
//                 frame.spillNote(listeAvNoter.get(t), Color.WHITE);
//             }
            
//             catch (IndexOutOfBoundsException index) {

//             }
//         }

//     }
// }

//Kan brukes i "SHOW" funksjonen - f.eks. samle inn alle tangentene som har blitt
//trykket ned innen 1 sekund etter en tangent blitt trykket ned
//(dette kan la deg registere akkorder).
class Monitor{
    ReentrantLock laas;
    ArrayList<String> noterAkkord;
    public Monitor(ArrayList<String> noterAkkord){
    laas = new ReentrantLock();   
    this.noterAkkord = noterAkkord;
    }

    //ArrayList<String>
    public void playAllNotes() throws InterruptedException{
        laas.lock();
        try {
            
        }
        finally {
        laas.unlock();
        }
    }
}
