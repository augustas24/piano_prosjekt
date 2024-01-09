import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jm.music.data.Note;
import jm.music.data.Phrase;
import jm.music.tools.Mod;
import jm.util.Play;
import jm.JMC;
import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.Timer;

public class pianoView {
    //Datastrukturer for oversikt i programmet:
    private JPanel tangent; //Den visuelle representasjonen av en tangent, oppbevares
                            //12 tangenter i en oktav. Har et navn (f.eks. "C") og
                            //en farge (hvit eller svart).
    private ArrayList<JPanel> oktav;    //Liste over tangenter. Den siste oktaven vil 
                                        //kun ha en tangent (C).
    private ArrayList<ArrayList<JPanel>> oktav_liste;   //Liste over alle oktavene.
                                                        //Det er 6 oktaver totalt.
    private String[] harmony_list = new String[8];  //Holder orden på hva som står
                                                    //i tekstfeltet til hver akkord/
                                                    //akkordpanel.
    private ArrayList<String>[] noter_til_hver_akkord = new ArrayList[8];   //Holder orden på notene til hver akkord i akkordpanelet.
    private ArrayList<String> notene_navn = new ArrayList<>(Arrays. asList("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"));  //Liste med navn på notene fra C til B.
    private ArrayList<String> flats_og_sharps = new ArrayList<>(Arrays. asList("b5", "b6", "b7" ,"b9" ,"b1" ,"#5" ,"#6" ,"#7" ,"#9" ,"#1"));   //Liste av tall som brukes til å sjekke
                                                                                                                                                    //om en akkord har noter som enten er b
                                                                                                                                                    //eller # i metoden string_til_noter().
    private ArrayList<String> kvalitet_strenger = new ArrayList<>(Arrays. asList("maj", "min", "dim", "aug", "sus", "add", "+", "-", "6"));//Liste av strenger som brukes til å sjekke om
                                                                                                                                                //en samling av tre chars utjgør en gyldig streng
                                                                                                                                                //som betegner en akkords kvalitet i metoden string_til_noter().
    private Map<Integer, Integer> note_indekser = new HashMap<Integer, Integer>() {{put(5,7);put(7,11);put(9,14);put(11,17);put(13,21);put(2,2);put(4,5);put(6,9);}}; //Assosiserer en en note med en avstand.
    private Map<Character, Integer> flat_sharp_verdier =  new HashMap<Character, Integer>(){{put('b', - 1);put('#', 1);}};  //Brukes i string_til_noter() for å inkrementere et steg ned eller opp.
    private HashMap<String, generisk> connection;   //Kobler en streng med enten
                                                    //en/et knapp, panel eller label.
    private Integer oktav_tall; //Brukes gjennom hele programmet til å holde styr på
                                //hvilken oktav i lista oktav_liste vi vil ha tak i.
    private String teksten; //Teksten til akkordene som står i hvert av de åtte
                            //akkordpanelene. Henter teksten sin fra chord_tekst
                            //(der brukeren skriver inn en akkord fra tastaturet).
    private int fokusert_akkord_panel;  //Indeksen til det akkordpanelet som nåværende blir fokusert på etter
                                        //brukeren har klikket på det.
    private HashMap<String, String> tastatur;    //Gjør at en en tast på tastaturet (med og uten capslock)
                                                    //assosieres med en tangent i keyboardet. Brukes senere i
                                                    //KeyboardListener for å finne ut hvilken tast en bruker
                                                    //har trykket på.
    private int volume_value; //Avgjør høyden på volumet.
    private HashMap<String, Integer> panel_indekser =  new HashMap<String, Integer>() {{put("PANEL_A",0);put("PANEL_B",1);put("PANEL_C",2);put("PANEL_D",3);put("PANEL_E",4);put("PANEL_F",5);put("PANEL_G",6);put("PANEL_H",7);}};//For å sette akkord_panel_liste til riktig tall når brukeren trykker på et panel.
    private HashMap<String, Color> note_farge = new HashMap<String, Color>() {{put("C", Color.WHITE);put("Db", Color.BLACK);put("D", Color.WHITE);put("Eb", Color.BLACK);put("E", Color.WHITE);put("F", Color.WHITE);put("Gb", Color.BLACK);put("G", Color.WHITE);put("Ab", Color.BLACK);put("A", Color.WHITE);put("Bb", Color.BLACK);put("B", Color.WHITE);}};
    private HashMap<JPanel, JPanel> venstre_naboer; //Oversikt over hvilke sorte tangenter som er til venstre for de hvite tangentene.
    private HashMap<JPanel, JPanel> hoyre_naboer; //Oversikt over hvilke sorte tangenter som er til venstre for de hvite tangentene.
    //Visuelt:
    private JButton avslutt, spill_harmoni, lag_harmoni, volume; //Alle knappene i programmet.
    private best_frame vinduet;
    private JLabel C1, C2, C3, C4, C5, C6, info_1, info_2, volume_tekst, oktav_tekst, add_tekst, show_tekst, chord_overskrift, 
            num1, num2, num3, num4, num5, num6, num7, num8, akkord_navn_1, akkord_navn_2, akkord_navn_3, akkord_navn_4, akkord_navn_5, akkord_navn_6,
            akkord_navn_7, akkord_navn_8, fill1, fill2, fill3, fill4, fill5, fill6, fill7, fill8, blank_tekst, 
            akkord_innhold_1, akkord_innhold_2, akkord_innhold_3, akkord_innhold_4,
            akkord_innhold_5, akkord_innhold_6, akkord_innhold_7, akkord_innhold_8; //Alt som vises med tekst i programmet.
    private JPanel test_1, test_2, hvit_panelet, sort_panelet, knappe_panelet, skjerm_panelet, info_panelet_1, info_panelet_2, harmoni_panel, volume_panel,
            oktav_panel, oktav_panel_2, add_panel, add_panel_2, akkord_panel_1, akkord_panel_2, akkord_panel_3,
            akkord_panel_4, akkord_panel_5, akkord_panel_6, akkord_panel_7, akkord_panel_8, plassering_1,
            plassering_2, plassering_3, plassering_4, plassering_5, plassering_6, plassering_7, plassering_8,
            chord_panel_3, overskrift_panel, P1, P2, P3, P4, P5, P6, slider_panel, chord_panel_over, border_1, border_2,
            tekstpanel_1, tekstpanel_2, tekstpanel_3, tekstpanel_4, tekstpanel_5, tekstpanel_6, tekstpanel_7, tekstpanel_8;
    private ArrayList<JLabel> num_liste; //Grupperer num objektene i liste for å skrive mindre kode senere.
    private ArrayList<JPanel> plassering_liste; //Grupperer disse objektene i liste for å skrive mindre kode senere.
    private ArrayList<JLabel> akkord_navn_liste; //Grupperer disse objektene i liste for å skrive mindre kode senere.
    private ArrayList<JLabel> akkord_innhold_liste; //Grupperer disse objektene i liste for å skrive mindre kode senere.
    private ArrayList<JPanel> akkord_panel_liste; //Grupperer disse objektene i liste for å skrive mindre kode senere.
    private ArrayList<JLabel> fill_liste; //Grupperer disse objektene i liste for å skrive mindre kode senere.
    private ArrayList<JTextField> tekstfelt_liste;
    private ArrayList<JPanel> tekstpanel_liste;
    private ArrayList<JPanel> sorte_tangenter; //Trengs denne?
    private HashMap<JPanel, JLayeredPane> panel_and_pane; //Oversikt over hvilken JLayeredPane en sort tangent er lagt oppå.
    private SpringLayout spring; //Bestemmer layoutet til de sorte tangentene.
    private JTextField chord_tekst, tekstfelt_1, tekstfelt_2, tekstfelt_3, tekstfelt_4, tekstfelt_5, tekstfelt_6, tekstfelt_7, tekstfelt_8; //Der man skriver inn navnet på akkordene.
    private PanelRound visual; //Et panel med avrundede kanter.
    private ButtonRound oktav_opp, oktav_ned, add_harmoni, show_harmoni; //Avrundede knapper.
    private JLayeredPane overste_lag;
    private pianoModel model; //Referanset til modellen.
    private pianoController controller; //Referanse til controller.
    

    public void GUI(pianoController kont) { //Opprettelsen av selve programmet.
        controller = kont;
        connection = new HashMap<String, generisk>();
        oktav_tall = 2;
        fokusert_akkord_panel = 0;

        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.exit(9);
        }

        border_1 = new JPanel();    //Denne og panelet under lager de to grønne linjene 
                                    //som signaliserer til brukeren hvilken oktav 
                                    //de spiller i.
        border_1.setBackground(Color.GREEN);
        border_1.setBounds(498, 400, 2, 170);

        border_2 = new JPanel();
        border_2.setBackground(Color.GREEN);
        border_2.setBounds(745, 400, 2, 170);

        hvit_panelet = new JPanel(); //Panelet til de hvite tangentene.
        hvit_panelet.setBackground(Color.DARK_GRAY);
        hvit_panelet.setBounds(0, 400, 1281, 170);
        hvit_panelet.setLayout(new GridLayout(1, 36));

        sort_panelet = new JPanel(); //Panelet til de sorte tangentene.
        spring = new SpringLayout();
        sort_panelet.setLayout(spring);
        sort_panelet.setOpaque(false);
        sort_panelet.setBackground(Color.RED);
        sort_panelet.setBounds(35, 400, 1176, 100);

        tastatur = new HashMap<String,String>(); //For å koble en tast til en tangent på keyboardet.
        tastatur.put("A", "C");
        tastatur.put("W", "Db");
        tastatur.put("S", "D");
        tastatur.put("E", "Eb");
        tastatur.put("D", "E");
        tastatur.put("F", "F");
        tastatur.put("T", "Gb");
        tastatur.put("G", "G");
        tastatur.put("Y", "Ab");
        tastatur.put("H", "A");
        tastatur.put("U", "Bb");
        tastatur.put("J", "B");

        vinduet = new best_frame("Piano Ver.1.0");

        oktav_liste = new ArrayList<ArrayList<JPanel>>(); //Lista av oktaver
        int antall = 0;
        int x = 35;
        sorte_tangenter = new ArrayList<JPanel>(); //Brukes for å beregne avstanden mellom hver tangent i sort_panelet.
        panel_and_pane = new HashMap<JPanel, JLayeredPane>();
        while (antall < 6) { 
            
            oktav = new ArrayList<JPanel>();    //Et oktav objekt. Hver oktav skal ha
                                                //12 noter (untatt siste oktaven).
            if (antall == 5) { //Setter kun inn en tangent i den siste oktaven.
                tangent = new JPanel(); // Gir riktig navn til noten.
                tangent.setName("C");
                tangent.setPreferredSize(new Dimension(40, 10));
                tangent.setBackground(Color.WHITE);
                tangent.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                oktav.add(tangent); //Setter inn tangent i oktaven.
                oktav_liste.add(oktav); //Setter oktaven inn i oktavLista på indeks.
                hvit_panelet.add(tangent);
                break; // Avslutter while-løkken.
            }
            for (int i = 0; i < 12; i++) { //Lager 12 tangenter pr. oktav, untatt den siste oktaven.
                if (note_farge.get(notene_navn.get(i)) == Color.WHITE) {
                    tangent = new JPanel(); // Gir riktig navn til noten.
                    tangent.setName(notene_navn.get(i));
                    tangent.setPreferredSize(new Dimension(40, 10));
                    tangent.setBackground(Color.WHITE);
                    tangent.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    oktav.add(tangent); //Setter inn tangent i oktaven.
                    hvit_panelet.add(tangent);
                } 
                else { //Oppretter de sorte tangentene.
                    tangent = new JPanel(); // Gir riktig navn til noten.
                    tangent.setName(notene_navn.get(i));
                    tangent.setBackground(Color.BLACK);
                    tangent.setBorder(BorderFactory.createEtchedBorder());
                    tangent.setSize(19, 100);
                    sorte_tangenter.add(tangent);
                    oktav.add(tangent); // setter inn tangent i oktaven.
                    overste_lag = new JLayeredPane();
                    overste_lag.setBackground(Color.RED);
                    vinduet.add(overste_lag);
                    overste_lag.setVisible(true);
                    overste_lag.setBounds(x, 400, 19, 100);
                    overste_lag.add(tangent, JLayeredPane.PALETTE_LAYER);
                    panel_and_pane.put(tangent, overste_lag);
                    if (notene_navn.get(i).equals("Db")) {
                        x += 36;
                    }
                    else if (notene_navn.get(i).equals("Eb")) {
                        x += 69;
                    } 
                    else if (notene_navn.get(i).equals("Gb")) {
                        x += 36;
                    } 
                    else if (notene_navn.get(i).equals("Ab")) {
                        x += 36;
                    } 
                    else if (notene_navn.get(i).equals("Bb")) {
                        x -= 1;
                    } 
                }
            }
            oktav_liste.add(oktav); //Setter oktaven inn i oktavLista på indeks.
            antall++;
            x += 70;
        }
        vinduet.add(border_1);
        vinduet.add(border_2);        
        vinduet.add(hvit_panelet);
        vinduet.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        venstre_naboer = new HashMap<JPanel, JPanel>();
        hoyre_naboer = new HashMap<JPanel, JPanel>();
        finn_hoyre_og_venstre_naboer(); //Finner alle høyre og venstre naboer for hver hvite akkord i alle oktaver.

        //Nå kommer masse kode som oppretter det visuelle i programmet:
        knappe_panelet = new JPanel();  //Panelet hvor knappene PLAY, CLEAR og EXIT plasseres.
        knappe_panelet.setBackground(Color.RED); knappe_panelet.setBounds(0, 570, 1281, 40); knappe_panelet.setLayout(new GridLayout(1, 3));
        spill_harmoni = new JButton("PLAY"); //Knappen PLAY. Alt som inngår i knappen står i linjen under.
        spill_harmoni.setFont(new FontUIResource("PLAY", 5, 18)); spill_harmoni.setBorder(BorderFactory.createLineBorder(Color.GRAY)); spill_harmoni.setBackground(Color.LIGHT_GRAY); spill_harmoni.setFocusable(false); spill_harmoni.addMouseListener(vinduet); spill_harmoni.setName("PLAY");
        lag_harmoni = new JButton("CLEAR"); //HUSK Å ENDRE TIL CLEAR
        lag_harmoni.setFont(new FontUIResource("CLEAR", 5, 18)); lag_harmoni.setBorder(BorderFactory.createLineBorder(Color.GRAY)); lag_harmoni.setBackground(Color.LIGHT_GRAY); lag_harmoni.setFocusable(false); lag_harmoni.addMouseListener(vinduet); lag_harmoni.setName("CLEAR");
        avslutt = new JButton("EXIT"); //Knappen EXIT. Alt som inngår i knappen står i linjen under.
        avslutt.setFont(new FontUIResource("EXIT", 5, 18)); avslutt.setBorder(BorderFactory.createLineBorder(Color.GRAY)); avslutt.addActionListener(new Avslutt()); avslutt.setBackground(Color.LIGHT_GRAY); avslutt.setFocusable(false); avslutt.addMouseListener(vinduet); avslutt.setName("EXIT");
        knappe_panelet.add(spill_harmoni);
        knappe_panelet.add(lag_harmoni);
        knappe_panelet.add(avslutt);
        vinduet.add(knappe_panelet);

        P1 = new JPanel(); //Panel og tekst som viser begynnelsen av hver oktav over selve keyboardet (de neste fem gjør det samme).
        P1.setBackground(Color.DARK_GRAY); P1.setBounds(12, 370, 30, 30);
        C1 = new JLabel("C1"); C1.setFont(new FontUIResource("C1", 5, 18)); C1.setForeground(Color.LIGHT_GRAY);
        P1.add(C1);
        vinduet.add(P1);

        P2 = new JPanel();
        P2.setBackground(Color.DARK_GRAY); P2.setBounds(257, 370, 30, 30);
        C2 = new JLabel("C2"); C2.setFont(new FontUIResource("C2", 5, 18)); C2.setForeground(Color.LIGHT_GRAY);
        P2.add(C2);
        vinduet.add(P2);

        P3 = new JPanel();
        P3.setBackground(Color.DARK_GRAY); P3.setBounds(502, 370, 30, 30);
        C3 = new JLabel("C3"); C3.setFont(new FontUIResource("C3", 5, 18)); C3.setForeground(Color.LIGHT_GRAY);
        P3.add(C3);
        vinduet.add(P3);

        P4 = new JPanel();
        P4.setBackground(Color.DARK_GRAY); P4.setBounds(747, 370, 30, 30);
        C4 = new JLabel("C4"); C4.setFont(new FontUIResource("C4", 5, 18)); C4.setForeground(Color.LIGHT_GRAY);
        P4.add(C4);
        vinduet.add(P4);

        P5 = new JPanel();
        P5.setBackground(Color.DARK_GRAY); P5.setBounds(991, 370, 30, 30);
        C5 = new JLabel("C5"); C5.setFont(new FontUIResource("C5", 5, 18)); C5.setForeground(Color.LIGHT_GRAY);
        P5.add(C5);
        vinduet.add(P5);

        P6 = new JPanel();
        P6.setBackground(Color.DARK_GRAY); P6.setBounds(1237, 370, 30, 30);
        C6 = new JLabel("C6"); C6.setFont(new FontUIResource("C6", 5, 18)); C6.setForeground(Color.LIGHT_GRAY);
        P6.add(C6);
        vinduet.add(P6);

        slider_panel = new JPanel(new BorderLayout()); //Ugjør slideren til som justerer volumet.
        slider_panel.setBounds(53, 314, 200, 44);
        JSlider slider = new JSlider() {
            @Override
            public void updateUI() {
                setUI(new CustomSliderUI(this));
            }
        };
        slider.setFocusable(false); slider_panel.setBackground(Color.DARK_GRAY); slider.setBackground(Color.DARK_GRAY); slider.addMouseListener(vinduet);
        slider_panel.add(slider);
        vinduet.add(slider_panel);

        volume_panel = new JPanel(); //Panelet og label hvor det står VOLUME over slideren.
        volume_tekst = new JLabel("VOLUME");
        volume_panel.setBackground(Color.DARK_GRAY); volume_panel.setBounds(93, 275, 120, 30); volume_tekst.setFont(new FontUIResource("VOLUME", 5, 18)); volume_tekst.setForeground(Color.LIGHT_GRAY);
        volume_panel.add(volume_tekst);
        vinduet.add(volume_panel);

        oktav_panel = new JPanel(); //Panelet og teksten der det står OCTAVE
        oktav_tekst = new JLabel("OCTAVE");
        oktav_panel_2 = new JPanel();
        oktav_panel.setBackground(Color.DARK_GRAY); oktav_panel.setBounds(1078, 275, 70, 30);
        oktav_tekst.setFont(new FontUIResource("OCTAVE", 5, 18)); oktav_tekst.setForeground(Color.LIGHT_GRAY);
        oktav_panel_2.setBackground(Color.DARK_GRAY); oktav_panel_2.setBounds(1048, 314, 130, 42); oktav_panel_2.setLayout(new GridLayout(1, 2, 11, 0));
        oktav_panel.add(oktav_tekst);
        vinduet.add(oktav_panel);
        vinduet.add(oktav_panel_2);

        oktav_ned = new ButtonRound(); //Venstre oktav-knapp.
        oktav_ned.setText("\u2BC7"); oktav_ned.setFont(new FontUIResource("\u2BC7", 5, 18)); oktav_ned.setRadius(30); oktav_ned.setColor(Color.LIGHT_GRAY); oktav_ned.setBorderColor(Color.DARK_GRAY); oktav_ned.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY)); oktav_ned.setFocusable(false); oktav_ned.addMouseListener(vinduet); oktav_ned.setName("OKTAV_NED");
        oktav_panel_2.add(oktav_ned);

        oktav_opp = new ButtonRound(); //Høyre oktav-knapp.
        oktav_opp.setText("\u2BC8"); oktav_opp.setFont(new FontUIResource("\u2BC8", 5, 18)); oktav_opp.setRadius(30); oktav_opp.setColor(Color.LIGHT_GRAY); oktav_opp.setBorderColor(Color.DARK_GRAY); oktav_opp.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY)); oktav_opp.setFocusable(false); oktav_opp.addMouseListener(vinduet); oktav_opp.setName("OKTAV_OPP");
        oktav_panel_2.add(oktav_opp);

        harmoni_panel = new JPanel(); //Det blå panelet over tantentene.
        harmoni_panel.setBackground(Color.BLUE); harmoni_panel.setBounds(350, 280, 380, 75); harmoni_panel.setLayout(new BorderLayout()); harmoni_panel.addMouseListener(vinduet); harmoni_panel.setName("WRITE_HARMONY");
        vinduet.add(harmoni_panel);

        //ENDRE DETTE, SKAL HA 8 SLIKE TextField I HVERT AKKORDPANEL.
        //BLIR DE NYE STEDENE HVOR MAN KAN SKRIVE INN AKKORDER.
        chord_tekst = new JTextField("");
        chord_tekst.setFont(new FontUIResource("", 5, 24));
        chord_tekst.setBackground(Color.BLUE);
        chord_tekst.setForeground(Color.WHITE);
        chord_tekst.setHorizontalAlignment(JTextField.CENTER);
        chord_tekst.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        chord_tekst.addMouseListener(vinduet);
        chord_tekst.setName("WRITE_HARMONY");
        // chord_tekst.setEnabled(false);
        chord_tekst.setEditable(false);
        harmoni_panel.add(chord_tekst, BorderLayout.CENTER);

        add_panel = new JPanel(); //Panelet hvor selve knappene ADD og SHOW skal være.
        add_panel.setBackground(Color.DARK_GRAY); add_panel.setBounds(755, 292, 65, 50); add_panel.setLayout(new GridLayout(2, 1, 0, 22));
        add_panel_2 = new JPanel(); //Panelet der teksten ADD og SHOW skal være.
        add_panel_2.setBackground(Color.DARK_GRAY); add_panel_2.setBounds(835, 280, 65, 75); add_panel_2.setLayout(new GridLayout(2, 1, 0, 0));
        add_harmoni = new ButtonRound(); //Knappen til ADD.
        add_harmoni.setRadius(10); add_harmoni.setColor(Color.LIGHT_GRAY); add_harmoni.setBorderColor(Color.DARK_GRAY); add_harmoni.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY)); add_harmoni.setFocusable(false); add_harmoni.addMouseListener(vinduet); add_harmoni.setName("ADD");
        show_harmoni = new ButtonRound(); //Knappen til SHOW.
        show_harmoni.setRadius(10); show_harmoni.setColor(Color.LIGHT_GRAY); show_harmoni.setBorderColor(Color.DARK_GRAY); show_harmoni.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY)); show_harmoni.setFocusable(false); show_harmoni.addMouseListener(vinduet); show_harmoni.setName("SHOW");
        add_panel.add(add_harmoni);
        add_panel.add(show_harmoni);

        add_tekst = new JLabel("ADD"); //Teksten til ADD.
        add_tekst.setFont(new FontUIResource("ADD", 5, 18)); add_tekst.setForeground(Color.LIGHT_GRAY);
        show_tekst = new JLabel("SHOW"); //Teksten til SHOW.
        show_tekst.setFont(new FontUIResource("SHOW", 5, 18)); show_tekst.setForeground(Color.LIGHT_GRAY);
        add_panel_2.add(add_tekst);
        add_panel_2.add(show_tekst);
        vinduet.add(add_panel);
        vinduet.add(add_panel_2);

        visual = new PanelRound(); //Lager de avrundede kantene til selve pianoet.
        visual.setBackground(Color.DARK_GRAY); visual.setBounds(0, 260, 1281, 160); visual.setRoundTopLeft(70); visual.setRoundTopRight(70);
        vinduet.add(visual);

        chord_panel_3 = new JPanel(); //Panelet som har nummerne til hvert akkordpanel oppe i venstre hjørnet.
        chord_panel_3.setOpaque(false); chord_panel_3.setBounds(25, 51, 1240, 30); chord_panel_3.setLayout(new GridLayout(1, 8, 7, 0));
        chord_panel_3.addMouseListener(vinduet);
        chord_panel_3.setName("PANEL_3");
        vinduet.add(chord_panel_3);
        num1 = new JLabel("1"); num2 = new JLabel("2"); num3 = new JLabel("3"); num4 = new JLabel("4"); num5 = new JLabel("5"); num6 = new JLabel("6"); num7 = new JLabel("7"); num8 = new JLabel("8");
        num_liste = new ArrayList<>(Arrays.asList(num1, num2, num3, num4, num5, num6, num7, num8)); //Utgjør hvert tall oppe i venstrehjørnet i hvert akkordpanel.
        for (int i = 0; i < 8; i++){
            num_liste.get(i).setFont(new FontUIResource(num_liste.get(i).getName(), 5, 18));
            num_liste.get(i).setBackground(Color.DARK_GRAY);
            chord_panel_3.add(num_liste.get(i));
        }

        plassering_1 = new JPanel(); plassering_2 = new JPanel(); plassering_3 = new JPanel(); plassering_4 = new JPanel(); plassering_5 = new JPanel(); plassering_6 = new JPanel(); plassering_7 = new JPanel(); plassering_8 = new JPanel();
        akkord_navn_1 = new JLabel(""); akkord_navn_2 = new JLabel(""); akkord_navn_3 = new JLabel(""); akkord_navn_4 = new JLabel(""); akkord_navn_5 = new JLabel(""); akkord_navn_6 = new JLabel(""); akkord_navn_7 = new JLabel(""); akkord_navn_8 = new JLabel("");
        akkord_innhold_1 = new JLabel(); akkord_innhold_2 = new JLabel(); akkord_innhold_3 = new JLabel(); akkord_innhold_4 = new JLabel(); akkord_innhold_5 = new JLabel(); akkord_innhold_6 = new JLabel(); akkord_innhold_7 = new JLabel(); akkord_innhold_8 = new JLabel();
        akkord_panel_1 = new JPanel(); akkord_panel_2 = new JPanel(); akkord_panel_3 = new JPanel(); akkord_panel_4 = new JPanel(); akkord_panel_5 = new JPanel(); akkord_panel_6 = new JPanel(); akkord_panel_7 = new JPanel(); akkord_panel_8 = new JPanel();
        fill1 = new JLabel(); fill2 = new JLabel(); fill3 = new JLabel(); fill4 = new JLabel(); fill5 = new JLabel(); fill6 = new JLabel(); fill7 = new JLabel(); fill8 = new JLabel();
        tekstfelt_1 = new JTextField(""); tekstfelt_2 = new JTextField(""); tekstfelt_3 = new JTextField(""); tekstfelt_4 = new JTextField(""); tekstfelt_5 = new JTextField(""); tekstfelt_6 = new JTextField(""); tekstfelt_7 = new JTextField(""); tekstfelt_8 = new JTextField("");
        tekstpanel_1 = new JPanel(); tekstpanel_2 = new JPanel(); tekstpanel_3 = new JPanel(); tekstpanel_4 = new JPanel(); tekstpanel_5 = new JPanel(); tekstpanel_6 = new JPanel(); tekstpanel_7 = new JPanel(); tekstpanel_8 = new JPanel();

        plassering_liste = new ArrayList<>(Arrays.asList(plassering_1, plassering_2, plassering_3, plassering_4, plassering_5, plassering_6, plassering_7, plassering_8));    //Der navnet på akkorden er i et av de åtte akkordpanelene.
        akkord_navn_liste = new ArrayList<>(Arrays.asList(akkord_navn_1, akkord_navn_2, akkord_navn_3, akkord_navn_4, akkord_navn_5, akkord_navn_6, akkord_navn_7, akkord_navn_8)); //Teksten til navnet på akkorden som vises i selve akkordpanelet.
        akkord_innhold_liste = new ArrayList<>(Arrays.asList(akkord_innhold_1, akkord_innhold_2, akkord_innhold_3, akkord_innhold_4, akkord_innhold_5, akkord_innhold_6, akkord_innhold_7, akkord_innhold_8));  //Teksten til notene i akkorden.
        akkord_panel_liste = new ArrayList<>(Arrays.asList(akkord_panel_1, akkord_panel_2, akkord_panel_3, akkord_panel_4, akkord_panel_5, akkord_panel_6, akkord_panel_7, akkord_panel_8));    //Selve akkordpanelene (dvs. de store grå/oransje firkantene)
        fill_liste = new ArrayList<>(Arrays.asList(fill1, fill2, fill3, fill4, fill5, fill6, fill7, fill8));    //Det grå mellomrommet mellom hvert akkordpanel.
        tekstfelt_liste =  new ArrayList<>(Arrays.asList(tekstfelt_1, tekstfelt_2, tekstfelt_3, tekstfelt_4, tekstfelt_5, tekstfelt_6, tekstfelt_7, tekstfelt_8)); //Tekstfeltetene hvor brukeren skriver inn en akkord i akkordpanelene.
        tekstpanel_liste = new ArrayList<>(Arrays.asList(tekstpanel_1, tekstpanel_2, tekstpanel_3, tekstpanel_4, tekstpanel_5, tekstpanel_6, tekstpanel_7, tekstpanel_8)); //Panelene hvor tekstfeltene blir satt.

        int x_index_plassering = 21;
        int x_index_panel = 20;
        int inkrementer_en_gang = 1;
        String bokstav_rekkefolge = "ABCDEFGH";
        for (int i = 0; i < 8; i++){
            plassering_liste.get(i).setOpaque(false);
            plassering_liste.get(i).setBounds(x_index_plassering, 115, 153 + inkrementer_en_gang, 60);
            plassering_liste.get(i).setLayout(new BorderLayout());

            akkord_navn_liste.get(i).setFont(new FontUIResource("", 5, 24));
            akkord_navn_liste.get(i).setHorizontalAlignment(JLabel.CENTER);

            akkord_innhold_liste.get(i).setFont(new FontUIResource("", 5, 16));
            akkord_innhold_liste.get(i).setHorizontalAlignment(JLabel.CENTER);

            vinduet.add(plassering_liste.get(i));
            plassering_liste.get(i).add(akkord_navn_liste.get(i), BorderLayout.PAGE_START);
            plassering_liste.get(i).add(akkord_innhold_liste.get(i), BorderLayout.PAGE_END);
            harmony_list[i] = akkord_navn_liste.get(i).getText();

            akkord_panel_liste.get(i).setBackground(Color.LIGHT_GRAY);
            akkord_panel_liste.get(i).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            akkord_panel_liste.get(i).setBounds(x_index_panel, 52, 153 + inkrementer_en_gang, 180);
            akkord_panel_liste.get(i).addMouseListener(vinduet); 
            akkord_panel_liste.get(i).setName("PANEL_" + bokstav_rekkefolge.charAt(i));
            x_index_panel += 155 + inkrementer_en_gang;
            vinduet.add(akkord_panel_liste.get(i));
        
            fill_liste.get(i).setBorder(BorderFactory.createLineBorder(Color.GRAY));
            akkord_panel_liste.get(i).add(fill_liste.get(i));

            tekstpanel_liste.get(i).setBackground(Color.LIGHT_GRAY);
            tekstpanel_liste.get(i).setBounds(x_index_plassering + 1, 115, 151, 60);
            x_index_plassering += 155;
            inkrementer_en_gang = 0;
            tekstpanel_liste.get(i).setLayout(new BorderLayout());
            tekstpanel_liste.get(i).addMouseListener(vinduet);
            tekstpanel_liste.get(i).setName("WRITE_HARMONY");
            vinduet.add(tekstpanel_liste.get(i));

            tekstfelt_liste.get(i).setFont(new FontUIResource("", 5, 24));
            tekstfelt_liste.get(i).setBackground(Color.LIGHT_GRAY);
            tekstfelt_liste.get(i).setForeground(Color.CYAN);
            tekstfelt_liste.get(i).setHorizontalAlignment(JTextField.CENTER);
            tekstfelt_liste.get(i).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            tekstfelt_liste.get(i).addMouseListener(vinduet);
            // tekstfelt_liste.get(i).setEnabled(false);
            tekstfelt_liste.get(i).setEditable(false);
            tekstpanel_liste.get(i).add(tekstfelt_liste.get(i), BorderLayout.CENTER);
        }

        chord_panel_over = new JPanel(); //Det oransje panelet som går over hvert akkordpanel når brukeren har skrevet inn en akkord på hver av de åtte akkordpanelene.
        chord_panel_over.setName("PANEL_1");
        chord_panel_over.setBackground(Color.ORANGE);
        chord_panel_over.setBounds(20, 51, 1240, 182);
        chord_panel_over.setLayout(new GridLayout(1, 8, 0, 0));
        chord_panel_over.addMouseListener(vinduet);
        vinduet.add(chord_panel_over);
        for (int i = 0; i < 8; i++){ //Kan ikke være i løkken overfor siden panelene vil bli lagt til i feil rekkefølge.
            chord_panel_over.add(fill_liste.get(i));
        }

        info_2 = new JLabel("Hover the mouse over a button or panel to get more information about it."); //Default teksten til INFO i bunnen av programmet.
        info_2.setFont(new FontUIResource("Hover the mouse over a button or panel to get more information about it.", 5, 18));
        info_panelet_2 = new JPanel(); //Panelet der teksten til info_2 blir plassert.
        info_panelet_2.setOpaque(false); info_panelet_2.setBounds(60, 598, 1281, 60); info_panelet_2.setLayout(new GridLayout(1, 1)); info_2.addMouseListener(vinduet);
        info_1 = new JLabel("INFO:"); //Selve teksten hvor det står "INFO".
        info_1.setFont(new FontUIResource("INFO: ", 5, 18));
        info_panelet_1 = new JPanel(); //Panelet der teksten til info_1 blir plassert.
        info_panelet_1.setBackground(Color.GRAY); info_panelet_1.setBounds(6, 598, 1335, 60); info_panelet_1.setLayout(new GridLayout(1, 1));
        info_panelet_2.add(info_2);
        vinduet.add(info_panelet_2);
        info_panelet_1.add(info_1);
        vinduet.add(info_panelet_1);

        overskrift_panel = new JPanel(); //Panel og tekst til "CHORD PROGRESSION".
        chord_overskrift = new JLabel("CHORD PROGRESSION");
        overskrift_panel.setBackground(Color.GRAY); overskrift_panel.setBounds(450, 10, 400, 30);
        chord_overskrift.setFont(new FontUIResource("CHORD PROGRESSION", 5, 18)); chord_overskrift.setBackground(Color.DARK_GRAY);
        vinduet.add(overskrift_panel);
        overskrift_panel.add(chord_overskrift);

        skjerm_panelet = new JPanel(); //Bakgrunnspanel i øverstehalvdel av programmet.
        skjerm_panelet.setBackground(Color.GRAY); skjerm_panelet.setBounds(0, 0, 1281, 650);
        vinduet.add(skjerm_panelet);

        vinduet.setLayout(null); //Generiske ting til programmet.
        vinduet.pack(); vinduet.setSize(50, 50); vinduet.setLocationRelativeTo(null); vinduet.setVisible(true);

        //Her kobler vi sammen navnene på knappene og de ulike GUI objektene:
        generisk<JButton> a = new generisk<>(spill_harmoni);
        connection.put("PLAY", a);
        generisk<JButton> b_2 = new generisk<>(lag_harmoni);
        connection.put("CLEAR", b_2);
        generisk<JButton> c = new generisk<>(avslutt);
        connection.put("EXIT", c);
        generisk<ButtonRound> d = new generisk<>(oktav_ned);
        connection.put("OKTAV_NED", d);
        generisk<ButtonRound> e = new generisk<>(oktav_opp);
        connection.put("OKTAV_OPP", e);
        generisk<JPanel> f = new generisk<>(harmoni_panel);
        connection.put("WRITE_HARMONY", f);
        generisk<ButtonRound> g = new generisk<>(add_harmoni);
        connection.put("ADD", g);
        generisk<ButtonRound> h = new generisk<>(show_harmoni);
        connection.put("SHOW", h);
        generisk<JPanel> i = new generisk<>(chord_panel_3);
        connection.put("PANEL_3", i);
        generisk<JPanel> j = new generisk<>(plassering_1);
        connection.put("PANEL_A", j);
        generisk<JPanel> k = new generisk<>(plassering_2);
        connection.put("PANEL_B", k);
        generisk<JPanel> l = new generisk<>(plassering_3);
        connection.put("PANEL_C", l);
        generisk<JPanel> m = new generisk<>(plassering_4);
        connection.put("PANEL_D", m);
        generisk<JPanel> n = new generisk<>(plassering_5);
        connection.put("PANEL_E", n);
        generisk<JPanel> o = new generisk<>(plassering_6);
        connection.put("PANEL_F", o);
        generisk<JPanel> p = new generisk<>(plassering_7);
        connection.put("PANEL_G", p);
        generisk<JPanel> q = new generisk<>(plassering_8);
        connection.put("PANEL_H", q);
        generisk<JPanel> r = new generisk<>(chord_panel_over);
        connection.put("PANEL_1", r);
        generisk<JTextField> s = new generisk<>(chord_tekst);
        connection.put("WRITE_HARMONY", s);
        generisk<JSlider> t = new generisk<>(slider);
        connection.put("VOLUME", t);
    }

    //Metoden kjøres veldig mange ganger, men det blir gjort orgentlig.
    //Evt. skriv om senere eller finn en bedre måte å finne naboer til hvite
    //tangenter.
    public void finn_hoyre_og_venstre_naboer(){
        int index = 0;
        while (index < 5){ 
            for (int i = 0; i < notene_navn.size(); i++){
                if (notene_navn.get(i).equals("C") || notene_navn.get(i).equals("F")){ //Finner sorte tangenter(JPanels) som er naboer til disse hvite tangentene i keyboardet.
                    hoyre_naboer.put(oktav_liste.get(index).get(i), oktav_liste.get(index).get(i + 1)); //Venstre nabo.
                    venstre_naboer.put(oktav_liste.get(index).get(i), oktav_liste.get(index).get(i + 1)); //Høyre nabo, settes til å være lik seg selv (så vi ikke får til en indeks som ikke finnes).
                }
                if (notene_navn.get(i).equals("E") ||notene_navn.get(i).equals("B")){
                    hoyre_naboer.put(oktav_liste.get(index).get(i), oktav_liste.get(index).get(i - 1)); //Venstre nabo , settes til å være lik seg selv (så vi ikke får til en indeks som ikke finnes).
                    venstre_naboer.put(oktav_liste.get(index).get(i), oktav_liste.get(index).get(i - 1)); //Høyre nabo.
                }
                if (notene_navn.get(i).equals("D") || notene_navn.get(i).equals("G") || notene_navn.get(i).equals("A")){
                    hoyre_naboer.put(oktav_liste.get(index).get(i), oktav_liste.get(index).get(i + 1)); //Venstre nabo.
                    venstre_naboer.put(oktav_liste.get(index).get(i), oktav_liste.get(index).get(i - 1)); //Venstre nabo.
                }
            }   
        index ++;
        }
    }

    class generisk<E> { //Brukes for å holde orden på buttons, labels og paneler i 
                        //en og samme hashmap.
        E variabel;
        public generisk(E variabel) {
            this.variabel = variabel;
        }
    }

    public class best_frame extends JFrame implements MouseListener { //Subklasse av JFrame som lar oss bruke KeyListener 
                                                                                    //og Mouselistener for å ta input fra tastaturet 
                                                                                    //og datamusen.
        public best_frame(String navn) {
            this.setTitle(navn); //Setter navn på best_frame/vinduet.
            tangent_animation C_tangent = new tangent_animation(hvit_panelet, 10000); //Lager et objekt pr.tangent som skal moderere actions til et JPanel via. et Timer objekt.
            tangent_animation Db_tangent = new tangent_animation(hvit_panelet, 10000);
            tangent_animation D_tangent = new tangent_animation(hvit_panelet, 10000);
            tangent_animation Eb_tangent = new tangent_animation(hvit_panelet, 10000);
            tangent_animation E_tangent = new tangent_animation(hvit_panelet, 10000);
            tangent_animation F_tangent = new tangent_animation(hvit_panelet, 10000);
            tangent_animation Gb_tangent = new tangent_animation(hvit_panelet, 10000);
            tangent_animation G_tangent = new tangent_animation(hvit_panelet, 10000);
            tangent_animation Ab_tangent = new tangent_animation(hvit_panelet, 10000);
            tangent_animation A_tangent = new tangent_animation(hvit_panelet, 10000);
            tangent_animation Bb_tangent = new tangent_animation(hvit_panelet, 10000);
            tangent_animation B_tangent = new tangent_animation(hvit_panelet, 10000);
            C_tangent.addAction("A", 1); //Legger til en action til hver tangent.
            Db_tangent.addAction("W", 2);
            D_tangent.addAction("S", 3);
            Eb_tangent.addAction("E", 4);
            E_tangent.addAction("D", 5);
            F_tangent.addAction("F", 6);
            Gb_tangent.addAction("T", 7);
            G_tangent.addAction("G", 8);
            Ab_tangent.addAction("Y", 9);
            A_tangent.addAction("H", 10);
            Bb_tangent.addAction("U", 11);
            B_tangent.addAction("J", 12);
        }

        @Override
        public void mouseClicked(MouseEvent e) { //Ikke tatt i bruk ennå (brukes muligens innen SHOW 
                                                    //sin funksjonalitet, hvis ikke brukes den nok i 
                                                    //mousePressed nedenfor).
        }

        @Override
        public void mousePressed(MouseEvent e) { //Håndeterer tilfeller hvor brukeren
                                                    //trykker på en knapp, panel, osv.
                                                    //via. datamusen.
            try {
                String navn = e.getComponent().getName(); //Finner navnet til komponenten (dvs. panelet, knappen, osv.).
                for (String i : connection.keySet()) { //Iterer gjennom hashmappen connection etter en streng/nøkkel som matcher navn.
                    switch (navn) {
                        case "PLAY":
                            spill_harmoni.setBackground(Color.CYAN);
                            break;
                        case "CLEAR":
                            for (int j = 0; j < 8; j++){
                                akkord_panel_liste.get(j).setVisible(true);
                                akkord_panel_liste.get(j).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                                akkord_navn_liste.get(j).setText("");
                                harmony_list[j] = akkord_navn_liste.get(j).getText();
                                akkord_innhold_liste.get(j).setText("");
                                tekstfelt_liste.get(j).setText("");
                                tekstpanel_liste.get(j).setVisible(true);
                                tekstfelt_liste.get(j).setVisible(true);
                                tekstpanel_liste.get(j).setBackground(Color.LIGHT_GRAY);
                                tekstpanel_liste.get(j).setForeground(Color.LIGHT_GRAY);
                                tekstfelt_liste.get(j).setBackground(Color.LIGHT_GRAY);
                                num_liste.get(j).setForeground(Color.DARK_GRAY);
                            }
                            // tekstfelt_liste.get(fokusert_akkord_panel).setEnabled(false);
                            tekstfelt_liste.get(fokusert_akkord_panel).setEditable(false);
                            break;
                        case "EXIT":
                            avslutt.setBackground(Color.CYAN);
                            break;
                        case "OKTAV_NED":
                            oktav_ned.setBackground(Color.CYAN);
                            break;
                        case "OKTAV_OPP":
                            oktav_opp.setBackground(Color.CYAN);
                            break;
                        case "ADD": //Samme tilfellet når ENTER trykkes 
                            teksten = tekstfelt_liste.get(fokusert_akkord_panel).getText(); //Henter teksten fra akkord_navn_liste.get(i) som er fokusert.
                            legg_til_akkord(teksten, fokusert_akkord_panel);
                            break;
                        case "SHOW":
                            show_harmoni.setBackground(Color.CYAN);
                            break;
                    }
                    break;
                }
            } 
            catch (NullPointerException n) {
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            try {
                String navn = e.getComponent().getName();
                for (String i : connection.keySet()) {
                    switch (navn) {
                        case "PLAY": //Skal endres til STOPP når en progresjon spilles av.
                            int counter = 0;
                            ArrayList<String> noter_i_akkord;
                            spill_harmoni.setBackground(Color.LIGHT_GRAY);
                            for (int l = 0; l < harmony_list.length; l++){ //Iterer gjennom akkordene i harmony_list.
                                if (harmony_list[l] != ""){ 
                                    noter_i_akkord = noter_til_hver_akkord[l];
                                    int nyIndex = oktav_tall;
                                    //BENDIK - BUG 1:
                                    akkordMonitor monitoren = new akkordMonitor(noter_i_akkord, this);
                                    Thread gronn = new Thread(new aktivAkkord(noter_i_akkord, this, nyIndex, Color.GREEN, monitoren, notene_navn));
                                    gronn.start();  //Denne tråden skal spille av lyden til notenene i noter_i_akkord,
                                                    //spille av notene visuelt på keyboardet i grønt, og endre fargen
                                                    //til akkordpanelet med noter_i_akkord til å være CYAN.
                                    Thread hvit = new Thread(new inaktivAkkord(noter_i_akkord, this, nyIndex, Color.WHITE, monitoren, notene_navn));
                                    hvit.start();   //Denne tråden skal endre fargen på de avspilte notene tilbake
                                                    //til hvitt, og endre fargen på akkordpanelet med noter_i_akkord
                                                    //tilbake til oransje.
                                    counter++;  
                                }
                            }
                            if (counter == 0){ //Hvis det er ingen akkorder skrevet inn i akkordpanelet.
                                spill_harmoni.setBackground(Color.LIGHT_GRAY);
                                info_2.setText("There are no chords in the progression!");
                            }
                            break;
                        case "CLEAR":
                            lag_harmoni.setBackground(Color.LIGHT_GRAY);
                            break;
                        case "EXIT":
                            avslutt.setBackground(Color.LIGHT_GRAY);
                            break;
                        case "OKTAV_NED":
                            oktav_ned.setBackground(Color.LIGHT_GRAY);
                            switch (oktav_tall) {
                                case 5:
                                case 4:
                                case 3:
                                case 2:
                                case 1:
                                    oktav_tall --;
                                    this.flytt_oktav(oktav_tall);
                                    break;
                                case 0:
                                default:
                                    oktav_tall = 0;
                                    info_2.setText("You can't set the octave any lower!");
                                    this.flytt_oktav(0);
                                    break;
                            }
                            break;
                        case "OKTAV_OPP":
                            oktav_opp.setBackground(Color.LIGHT_GRAY);
                            switch (oktav_tall) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                    oktav_tall = oktav_tall + 1;
                                    this.flytt_oktav(oktav_tall);
                                    break;
                                case 5:
                                default:
                                    oktav_tall = 5;
                                    info_2.setText("You can't set the octave any higher!");
                                    this.flytt_oktav(4);
                                    break;
                            }
                            break;
                        case "WRITE_HARMONY":
                            break;
                        case "ADD":
                            add_harmoni.setBackground(Color.LIGHT_GRAY);
                            break;
                        case "SHOW":
                            show_harmoni.setBackground(Color.LIGHT_GRAY);
                            break;
                        case "PANEL_A":
                        case "PANEL_B":
                        case "PANEL_C":
                        case "PANEL_D":
                        case "PANEL_E":
                        case "PANEL_F":
                        case "PANEL_G":
                        case "PANEL_H":
                        //plassering_liste - Der navnet på akkorden er i et av de åtte akkordpanelene.
                        //akkord_navn_liste - Teksten til navnet på akkorden som vises i selve akkordpanelet.
                        //akkord_innhold_liste - Teksten til notene i akkorden.
                        //akkord_panel_liste - Selve akkordpanelene (dvs. de store grå/oransje firkantene)
                        //fill_liste - Det grå mellomrommet mellom hvert akkordpanel.
                        //tekstfelt_liste - Tekstfeltetene hvor brukeren skriver inn en akkord i akkordpanelene.
                        //tekstpanel_liste - Panelene hvor tekstfeltene blir satt.
                            fokusert_akkord_panel = panel_indekser.get(navn);
                            LineBorder border;
                            for (int j = 0; j < 8; j++){
                                border = (LineBorder) akkord_panel_liste.get(j).getBorder();
                                if (border.getLineColor() == Color.CYAN){ //Finner det forrige fokuserte panelet og fjerner fokusest fra det.
                                    tekstfelt_liste.get(j).setEditable(false);   
                                    akkord_panel_liste.get(j).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                                    num_liste.get(fokusert_akkord_panel).setForeground(Color.DARK_GRAY);
                                }
                            }
                            tekstfelt_liste.get(fokusert_akkord_panel).setEditable(true);
                            akkord_panel_liste.get(fokusert_akkord_panel).setBorder(BorderFactory.createLineBorder(Color.CYAN));
                            akkord_panel_liste.get(fokusert_akkord_panel).setVisible(true);
                            num_liste.get(fokusert_akkord_panel).setForeground(Color.CYAN);
                            break;
                        // default: //Hvis man kilkker på noe som ikke er en knapp eller et interaktivt panel.

                    }
                break;
                }
            }
            catch (NullPointerException n) {
            }
            catch (StringIndexOutOfBoundsException s){
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            try {
                String navn = e.getComponent().getName();
                for (String i : connection.keySet()) {
                    switch (navn) {
                        case "PLAY":
                            info_2.setText("Plays the currently set chord progression from its beginning.");
                            break;
                        case "CLEAR":
                            info_2.setText("Deletes all chords within the chord progression.");
                            break;
                        case "EXIT":
                            info_2.setText("Closes the program.");
                            break;
                        case "OKTAV_NED":
                            info_2.setText(
                                    "Sets the octave you are playing on to the one below it (this button only works when in \"computer keyboard mode\").");
                            break;
                        case "OKTAV_OPP":
                            info_2.setText(
                                    "Sets the octave you are playing on to the one above it (this button only works when in \"computer keyboard mode\").");
                            break;
                        case "WRITE_HARMONY":
                            info_2.setText(
                                    "Write one chord at a time. Examples of valid chords are: G7, Cmaj7, C-/G, Edim7, etc. Press ENTER to move on to the next step.");
                            break;
                        case "ADD":
                            info_2.setText("Adds the chord to the panel above.");
                            break;
                        case "SHOW":
                            info_2.setText("First select a chord from the chord panel, then click on the notes you want to add to your chord or play them on your keyboard.");
                            break;
                        case "VOLUME":
                            info_2.setText("Adjust the volume level. (NOT IMPLEMENTED YET)");
                            break;
                        case "PANEL_3":
                        case "PANEL_A":
                        case "PANEL_B":
                        case "PANEL_C":
                        case "PANEL_D":
                        case "PANEL_E":
                        case "PANEL_F":
                        case "PANEL_G":
                        case "PANEL_H":
                        case "PANEL_1":
                            info_2.setText(
                                    "The current chord progression. Click on a panel to add a chord to it.");
                            break;
                    }
                }
            } catch (NullPointerException n) {
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            info_2.setText("Hover the mouse over a button or panel to get more information about it.");
        }

        //Basert på https://github.com/tips4java/tips4java/blob/main/source/KeyboardAnimation.java
        class tangent_animation extends AbstractAction {
            private final static String PRESSED = "pressed ";
	        private final static String RELEASED = "released ";
            private JComponent tangent_component;
	        private Timer timer;
	        private Map<String, Integer> pressedKeys = new HashMap<String, Integer>();
            public tangent_animation(JComponent tangent_component, int delay){
                this.tangent_component = tangent_component;
		        timer = new Timer(delay, this);
		        timer.setInitialDelay(0);
            }

            public void addAction(String keyStroke, int index){
                int offset = keyStroke.lastIndexOf(" ");
		        String key = offset == -1 ? keyStroke :  keyStroke.substring(offset + 1);
		        String modifiers = keyStroke.replace(key, "");
                
                InputMap inputMap = tangent_component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		        ActionMap actionMap = tangent_component.getActionMap();

                //pressed
                Action pressedAction = new tangent_action(key, index);
		        String pressedKey = modifiers + PRESSED + key;
                KeyStroke pressedKeyStroke = KeyStroke.getKeyStroke(pressedKey);
		        inputMap.put(pressedKeyStroke, pressedKey);
		        actionMap.put(pressedKey, pressedAction);

                //released
                Action releasedAction = new tangent_action(key, 0);
		        String releasedKey = modifiers + RELEASED + key;
		        KeyStroke releasedKeyStroke = KeyStroke.getKeyStroke(releasedKey);
		        inputMap.put(releasedKeyStroke, releasedKey);
		        actionMap.put(releasedKey, releasedAction);
            }

            public void handleKeyEvent(String key, int index){
                if (index == 0){
                    pressedKeys.remove(key);
                }  
                else{
                    pressedKeys.put(key, index);
                    timer.setActionCommand(key);
                }

                //Start the Timer when the first key is pressed
                if (pressedKeys.size() == 1){
                    timer.start();
                }

                if (pressedKeys.size() == 0){ //Gjøres når tangenten har blitt sluppet etter bruk.
                    timer.stop();
                    if (note_farge.get(tastatur.get(key)) == Color.WHITE){
                        oktav_liste.get(oktav_tall).get(notene_navn.indexOf(tastatur.get(key))).setBackground(Color.WHITE);
                        panel_and_pane.get(venstre_naboer.get(oktav_liste.get(oktav_tall).get(notene_navn.indexOf(tastatur.get(key))))).moveToFront(venstre_naboer.get(oktav_liste.get(oktav_tall).get(notene_navn.indexOf(tastatur.get(key)))));  
                        panel_and_pane.get(hoyre_naboer.get(oktav_liste.get(oktav_tall).get(notene_navn.indexOf(tastatur.get(key))))).moveToFront(hoyre_naboer.get(oktav_liste.get(oktav_tall).get(notene_navn.indexOf(tastatur.get(key)))));
                    }
                    else {
                        oktav_liste.get(oktav_tall).get(notene_navn.indexOf(tastatur.get(key))).setBackground(Color.BLACK);
                    }                
                }
	        }

            public void actionPerformed(ActionEvent e){ //Gjøres når tangenten er trykket nede.
                spill_noten(tastatur.get(e.getActionCommand()), Color.GREEN, oktav_tall, 1);
                //GJØR DISSE TO MER EFFEKTIVT!
                // panel_and_pane.get(venstre_naboer.get(oktav_liste.get(oktav_tall).get(notene_navn.indexOf(tastatur.get(e.getActionCommand()))))).moveToFront(venstre_naboer.get(oktav_liste.get(oktav_tall).get(notene_navn.indexOf(tastatur.get(e.getActionCommand())))));  
                // panel_and_pane.get(hoyre_naboer.get(oktav_liste.get(oktav_tall).get(notene_navn.indexOf(tastatur.get(e.getActionCommand()))))).moveToFront(hoyre_naboer.get(oktav_liste.get(oktav_tall).get(notene_navn.indexOf(tastatur.get(e.getActionCommand())))));  
            } 

            class tangent_action extends AbstractAction implements ActionListener{
                int index;
                public tangent_action(String navn, int index){
                    super(navn);
                    this.index = index;
                }

                public void actionPerformed(ActionEvent e){
			        handleKeyEvent((String)getValue(NAME), index);
		        }
            }
        }

        public void legg_til_akkord(String teksten, int riktig_index){
        //plassering_liste - Der navnet på akkorden er i et av de åtte akkordpanelene.
        //akkord_navn_liste - Teksten til navnet på akkorden som vises i selve akkordpanelet.
        //akkord_innhold_liste - Teksten til notene i akkorden.
        //akkord_panel_liste - Selve akkordpanelene (dvs. de store grå/oransje firkantene)
        //fill_liste - Det grå mellomrommet mellom hvert akkordpanel.
        //tekstfelt_liste - Tekstfeltetene hvor brukeren skriver inn en akkord i akkordpanelene.
        //tekstpanel_liste - Panelene hvor tekstfeltene blir satt.
            if (is_valid_chord(teksten) == true) {
                int tekstStoerrelse = 24;
                if (teksten.length() > 11){
                    tekstStoerrelse = 20;
                }
                else if (teksten.length() > 14){
                    tekstStoerrelse = 16;
                }
                else if (teksten.length() > 16){
                    tekstStoerrelse = 12;
                }
                if (riktig_index < 8){
                    num_liste.get(fokusert_akkord_panel).setForeground(Color.DARK_GRAY);
                    akkord_panel_liste.get(riktig_index).setVisible(false);
                    akkord_navn_liste.get(riktig_index).setText(teksten);
                    akkord_panel_liste.get(riktig_index).setFont(new FontUIResource(teksten, 5, tekstStoerrelse));
                    harmony_list[riktig_index] = akkord_navn_liste.get(riktig_index).getText();
                    noter_til_hver_akkord[riktig_index] = finn_noter_i_akkord(harmony_list[riktig_index]);
                    String innhold_i_lista = "";
                    for (int j = 0; j < noter_til_hver_akkord[riktig_index].size(); j++){
                        innhold_i_lista += noter_til_hver_akkord[riktig_index].get(j) + " ";
                        }
                    tekstpanel_liste.get(riktig_index).setVisible(false);
                    tekstfelt_liste.get(riktig_index).setVisible(false);
                    tekstpanel_liste.get(riktig_index).setOpaque(true);
                    akkord_innhold_liste.get(riktig_index).setText(innhold_i_lista);
                    num_liste.get(riktig_index).setText(num_liste.get(riktig_index).getText());
                    num_liste.get(riktig_index).setFont(new FontUIResource(num_liste.get(riktig_index).getName(), 5, 18));
                    akkord_panel_liste.get(riktig_index).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    // tekstfelt_liste.get(riktig_index).setEnabled(false);
                    tekstfelt_liste.get(riktig_index).setEditable(false);
                    num_liste.get(riktig_index).setFont(new FontUIResource(num_liste.get(riktig_index).getName(), 5, 18));
                }
                else {
                    info_2.setText("You have already added the maximum amount of chords!");
                }
            } 
            else {
                tekstfelt_liste.get(fokusert_akkord_panel).setText("");
                akkord_panel_liste.get(fokusert_akkord_panel).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                tekstfelt_liste.get(fokusert_akkord_panel).setEditable(false);
                info_2.setText("Invalid chords cannot be added to the progression!");
            }
            add_harmoni.setBackground(Color.CYAN);
        }

        public void flytt_oktav(int indeksPlassering){  //Kalles på hver gang OKTAV_OPP eller OKTAV_NED trykkes på av brukeren.
                                                        //Flytter de grønne strekene som viser hvilken oktav brukeren kan spille i.
            switch (indeksPlassering){
                case 0:
                    border_1.setBounds(8, 400, 2, 170);
                    border_2.setBounds(256, 400, 2, 170);
                    break;
                case 1:
                    border_1.setBounds(252, 400, 2, 170);
                    border_2.setBounds(500, 400, 2, 170);
                    break;
                case 2:
                    border_1.setBounds(498, 400, 2, 170);
                    border_2.setBounds(745, 400, 2, 170);
                    break;
                case 3:
                    border_1.setBounds(742, 400, 2, 170);
                    border_2.setBounds(991, 400, 2, 170);
                    break;
                case 4:
                    border_1.setBounds(988, 400, 2, 170);
                    border_2.setBounds(1235, 400, 2, 170);
                    break;
                case 5:
                    border_1.setBounds(1232, 400, 2, 170);
                    border_2.setBounds(1270, 400, 2, 170);
                    break;
                default:
                    System.out.println("Feil indeks!");
                    break;
            }
        }

        public void fargelegg_oktaver(int indeksPlassering) {
            try {
                for (int i = 0; i < oktav_liste.size(); i++) {
                    if (i == indeksPlassering) {
                        for (int j = 0; j < oktav_liste.get(i).size(); j++) {
                            String her = oktav_liste.get(i).get(j).getName();
                            if (her == "C" || her == "D" || her == "E" || her == "F"
                                    || her == "G" || her == "A" || her == "B") {
                                oktav_liste.get(i).get(j).setBackground(Color.WHITE);
                            }
                            else {
                                oktav_liste.get(i).get(j).setBackground(Color.BLACK);
                            }
                        }
                    }
                }
            } catch (NumberFormatException e) {

            }
        }

        //AUGUST - BUG 8:
        public boolean is_valid_chord(String akkord) { //Bruker regex til å sjekke om en streng er en gyldig
                                                        //akkord. Returnerer true hvis dette er tilfellet.
            //Cmaj7add9#5 skal ikke være mulig. Skal egentlig være Cmaj7#5add9. 
            Pattern main_pattern = Pattern.compile("[A-G]{1}[b|#]?{1}(([6|7|9]{1}|[11|13]{2}|[+|-]{1}([6|7|9]{1}|[11|13]{2})?{1}|[min|aug|dim]{3}([6|7|9]{1}|[11|13]{2})?{1}|([maj7|maj9]{4}|[maj11|maj13]{5})?{1})?{1}(sus[2|4])?{1}(add([2|4|5|6|7|9]{1}|[11|13]{2}))?{1}([b|#]{1}5)?{1}([b|#]{1}6)?{1}([b|#]{1}7)?{1}([b|#]{1}9)?{1}([b|#]{1}11)?{1}([b|#]{1}13)?{1})?(/{1}[A-G]{1}[b|#]?{1})?");
            Matcher main_matcher = main_pattern.matcher(akkord);
            boolean main_matchFound = main_matcher.matches();
            return main_matchFound;
            //--- Forklaring av regex-mønsteret ---
            //Finn EN bokstav fra A til G som KAN inneholde et tilfelle av b ELLER #: [A-G]{1}[b|#]?{1}
            //deretter KAN strengen inneholde ENTEN 6,7,9 ELLER 11,13 ELLER +, - : [6|7|9]{1}|[11|13]{2}|[+|-]{1} 
            //dette KAN følges av ENTEN 6, 7, 9 ELLER 11, 13: ([6|7|9]{1}|[11|13]{2})?{1}   //så vi kan f.eks. fange opp akkorder med -11 eller +7
            //dette KAN følges av ENTEN min, aug, dim: |[min|aug|dim]{3}
            //dette KAN følges av ENTEN 6, 7, 9 ELLER 11, 13: ([6|7|9]{1}|[11|13]{2})?{1}   //så vi kan f.eks. fange opp akkorder med min11 eller aug7
            //dette KAN følges av ENTEN maj7, maj9 ELLER maj11, maj13: ([maj7|maj9]{4}|[maj11|maj13]{5})?{1}
            //derreter KAN sus2, sus4: (sus[2|4])?{1} 
            //deretter KAN add2, add4, add5, add6, add7, add9, add11, add13: (add([2|4|5|6|7|9]{1}|[11|13]{2}))?{1}
            //deretter KAN b ELLER # 5, 6, 7, 9, 11, 13: ([b|#]{1}5)?{1}([b|#]{1}6)?{1}([b|#]{1}7)?{1}([b|#]{1}9)?{1}([b|#]{1}11)?{1}([b|#]{1}13)?{1})?
            //til slutt KAN det forekommer EN bokstav fra A til G som KAN inneholde et tilfellet av b ELLER # DERSOM det er en /: (/{1}[A-G]{1}[b|#]?{1})?"
        }

        public ArrayList<String> finn_noter_i_akkord(String akkord){
            String midlertidig_streng = "" + akkord.charAt(0);
            int root = notene_navn.indexOf(midlertidig_streng);
            if (akkord.length() > 1){
                if (akkord.charAt(1) == 'b'){
                    midlertidig_streng += akkord.charAt(1);
                    root = notene_navn.indexOf(midlertidig_streng);
                    }
                if (akkord.charAt(1) == '#'){
                    root = notene_navn.indexOf(midlertidig_streng) + 1;
                    midlertidig_streng += akkord.charAt(1);                    
                    }
                }
            akkord = fjern_chars(akkord, midlertidig_streng.length());
            ArrayList<String> noter_i_akkord = string_til_noter(akkord, new ArrayList<String>(), root);
            return noter_i_akkord;
        }

        //AUGUST - BUG 9
        public ArrayList<String> string_til_noter(String akkord, ArrayList<String> liste_av_noter, int root) {
            String char_pattern;    //Brukes for å sjekke om en rekke av chars utgjør en 
                                    //gjennkjennlig streng (f.eks. min, add, b9, osv.).
            if (liste_av_noter.size() == 0){ //Setter inn noter i lista. Er minimum tre noter i lista.
                liste_av_noter = sett_inn_extensions(akkord, liste_av_noter, root);
            }
            if (akkord.length() >= 3){  //Først sjekk tre bokstaver etter hverandre.
                char_pattern = "" + akkord.charAt(0) + akkord.charAt(1) + akkord.charAt(2);
                if (kvalitet_strenger.contains(char_pattern)){  //Ser om char_patter er i en liste med gyldige strenger
                                                                //slik at vi ikke bruker unødvendig tid på å kalle på endre_kvalitet().
                    liste_av_noter = endre_kvalitet(liste_av_noter, char_pattern, akkord);
                    akkord = fjern_chars(akkord, char_pattern.length());
                    if (char_pattern.equals("add")){
                        char_pattern = "" + akkord.charAt(0);
                        if (akkord.charAt(0) == '1'){
                            char_pattern += akkord.charAt(1);
                        }
                        akkord = fjern_chars(akkord, char_pattern.length());
                    }
                }
            }
            if (akkord.length() >= 2){ //Deretter sjekk to bokstaver etter hverandre.
                char_pattern = "" + akkord.charAt(0) + akkord.charAt(1);
                if (flats_og_sharps.contains(char_pattern)){
                    String neste_tallet = "" + akkord.charAt(1);
                    int neste_indeks = flat_sharp_verdier.get(akkord.charAt(0));
                    if (neste_tallet.equals("1")){
                        neste_tallet = "" + akkord.charAt(1) + akkord.charAt(2);
                    }
                    if (liste_av_noter.contains(notene_navn.get(hasher(root, note_indekser.get(Integer.parseInt(neste_tallet)))))){ //Sjekker om liste_av_noter har noten som skal gjøres b eller #.
                        liste_av_noter.set(liste_av_noter.indexOf(notene_navn.get(hasher(root, note_indekser.get(Integer.parseInt(neste_tallet))))), notene_navn.get(hasher(root, note_indekser.get(Integer.parseInt(neste_tallet)) + neste_indeks)));
                    }
                    else { //Setter inn den nye noten som enten er b eller #.
                        liste_av_noter.add(notene_navn.get(hasher(root, note_indekser.get(Integer.parseInt(neste_tallet)) + neste_indeks)));
                    }
                    akkord = fjern_chars(akkord, neste_tallet.length() + 1);
                }
            }
            if (akkord.length() >= 1){    //Til slutt sjekkes en bokstav.
                char_pattern = "" + akkord.charAt(0);
                if (kvalitet_strenger.contains(char_pattern)){  //Ser om char_pattern er i en liste med gyldige strenger.
                    liste_av_noter = endre_kvalitet(liste_av_noter, char_pattern, akkord);
                    akkord = fjern_chars(akkord, char_pattern.length());
                }
            }
            if (akkord.length() > 0){
                if (akkord.charAt(0) == '/'){ //Endrer rekkefølgen på elementene ELLER setter
                                                    //en note først i lista hvis strengen har en '/'.
                    liste_av_noter = inversion_sjekk(akkord, liste_av_noter);
                    akkord = ""; //Ikke noe mer å sjekke etter vi har lagt til inversion.
                    System.out.println(liste_av_noter);
                }
            }
            if (akkord.length() > 0){  //Fjerner alle unødvendige tegn som ikke er i flats_og_sharps eller kvalitet_strenger
                                        //for å komme frem til korrekte strenger i det neste rekursive kallet på string_til_noter.
                if (!flats_og_sharps.contains(akkord.charAt(0)) || !kvalitet_strenger.contains(akkord.charAt(0)) || !(akkord.charAt(0) == '/')){
                    akkord = fjern_chars(akkord, 1);
                }
                liste_av_noter = string_til_noter(akkord, liste_av_noter, root); //Rekursivt steg.
            }
            return liste_av_noter;
        }
        
        public ArrayList<String> sett_inn_extensions(String akkord, ArrayList<String> liste_av_noter, int root){    //Setter inn noter i liste_av_noter, hvor antallet noter 
                                                                                                                    //bestemmes av det største tallet i strengen akkord.
                int storste_tallet = 3; //Så det blir minimum tre noter i liste_av_noter. Iterer gjennom strengen
                                        //og leter etter tall. storste_tallet er lik det største tallet i strengen.
                for (int i = 0; i < akkord.length(); i++){
                    //Antallet elementer i lista bestemmes av det første tallet som finnes i strengen.
                    //Trenger vi egentlig å sjekke om storste_tallet er mindre enn noe annet da?
                    if ((akkord.charAt(i) == '6' || akkord.charAt(i) == '7') && storste_tallet < 4 && sjekk_om_add(i, akkord)){
                        storste_tallet  = 4;
                        break;
                    }
                    if (akkord.charAt(i) == '9' && storste_tallet < 5 && sjekk_om_add(i, akkord)){
                        storste_tallet = 5;
                        break;
                    }
                    if (i > 0){
                        if (akkord.charAt(i-1) == '1' && akkord.charAt(i) == '1' && sjekk_om_add(i, akkord)){
                            storste_tallet = 6;
                            break;
                        }
                        if (akkord.charAt(i-1) == '1' && akkord.charAt(i) == '3' && sjekk_om_add(i, akkord)){
                            storste_tallet = 7;
                            break;
                        }
                    }
                }
                String sekvens = "4334340"; //Må gjøres så notene hentes ut på riktig indeks fra notene_navn.
                String midlertidig_streng;
                int indeks_til_neste_note = 0;
                for (int i = 0; i < storste_tallet; i++){
                    midlertidig_streng = "" + sekvens.charAt(i);
                    liste_av_noter.add(notene_navn.get(hasher(root, indeks_til_neste_note))); //Mønsteret skal være: 4(3rd), 7(5th), 11(7th), 14(9th), 17(11th), 21(13th).
                    indeks_til_neste_note += Integer.parseInt(midlertidig_streng);
                }
            return liste_av_noter;
        }

        //Dårlig måte å sjekke dette på, men måtte bare finne en måte å gjøre det.
        public boolean sjekk_om_add(int current_index, String strengen){ //Slik at vi ikke forlenger lista dersom vi har en akkord med add.
            if (current_index > 0){
                if (strengen.charAt(current_index - 1) == 'd'){
                    return false;
                }
                if (current_index > 1){
                    if (strengen.charAt(current_index - 2) == 'd'){
                    return false;
                    }
                }
            }
            return true;
        }
        
        public ArrayList<String> endre_kvalitet(ArrayList<String> liste_av_noter, String kvalitet, String akkord){  //Endrer elementene i lista dersom akkorden er av en spesifisert
                                                                                                                    //kvalitet. Trenger ikke å endre elementer for maj-akkorder.
            if (kvalitet.equals("min") || kvalitet.equals("-")){ //Setter 3rd et halvt steg ned.
                liste_av_noter.set(1, notene_navn.get(hasher(notene_navn.indexOf(liste_av_noter.get(1)), - 1)));
            }
            if (kvalitet.equals("dim")){ //Setter 3rd et helt steg ned og 5th et halvt steg ned.
                liste_av_noter.set(1, notene_navn.get(hasher(notene_navn.indexOf(liste_av_noter.get(1)), - 1)));
                liste_av_noter.set(2, notene_navn.get(hasher(notene_navn.indexOf(liste_av_noter.get(2)), - 1)));
            } 
            if (akkord.length() >= 4){
                if (kvalitet.equals("sus") && akkord.charAt(3) == '2'){ //Setter 3rd et helt steg ned.
                    liste_av_noter.set(1, notene_navn.get(hasher(notene_navn.indexOf(liste_av_noter.get(1)), - 2)));
                }
                if (kvalitet.equals("sus") && akkord.charAt(3) == '4'){ //Setter 3rd et helt steg opp.
                    liste_av_noter.set(1, notene_navn.get(hasher(notene_navn.indexOf(liste_av_noter.get(1)), 1)));
                }
            }
            if (kvalitet.equals("aug") || kvalitet.equals("+")){//Setter 5th et halvt steg opp.
                liste_av_noter.set(2, notene_navn.get(hasher(notene_navn.indexOf(liste_av_noter.get(2)), 1))); 
            }
            if (kvalitet.equals("add")){
                int flat_eller_sharp = 0;
                int neste_indeks = 0;
                //Ikke mulig å lage akkorder som Cadd#9 ??? Fjern denne if-sjekken?
                if (akkord.charAt(3) == 'b' || akkord.charAt(3) == '#'){ //Hvis char på indeks 3 er b eller #, gå videre til char bak.
                    flat_eller_sharp = flat_sharp_verdier.get(akkord.charAt(3));
                    neste_indeks ++;
                }
                String midlertidig_streng = "" + akkord.charAt(3 + neste_indeks);
                int add_noten = Integer.parseInt(midlertidig_streng);
                if (akkord.length() > 4){
                    midlertidig_streng += akkord.charAt(4 + neste_indeks);
                    add_noten = Integer.parseInt(midlertidig_streng);
                }
                //Dårlig skrevet sjekk her - muligens skriver om senere.
                if ((notene_navn.indexOf(liste_av_noter.get(liste_av_noter.size() - 1)) > notene_navn.indexOf(notene_navn.get(hasher(notene_navn.indexOf(liste_av_noter.get(0)), note_indekser.get(add_noten) + flat_eller_sharp)))) && (add_noten == 2 || add_noten == 4 || add_noten == 6)){
                    for (int i = 0; i < liste_av_noter.size(); i++){    //Hvis siste tall i liste_av_noter har en høyere indeks enn tallet vi skal 
                                                                        //sette inn. Gjør at vi kan sette inn add2, add4, add6 på korrekte indekser.
                        if (notene_navn.indexOf(liste_av_noter.get(i)) > notene_navn.indexOf(notene_navn.get(hasher(notene_navn.indexOf(liste_av_noter.get(0)), note_indekser.get(add_noten) + flat_eller_sharp)))){
                            //Sett inn nye tall på plassen til i, flytt alle elementene til høyre for i et hakk utover i lista.
                            ArrayList<String> forste_halvdel = new ArrayList<>(liste_av_noter.subList(0, liste_av_noter.indexOf(liste_av_noter.get(i))));
                            ArrayList<String> andre_halvdel = new ArrayList<>(liste_av_noter.subList(liste_av_noter.indexOf(liste_av_noter.get(i)), liste_av_noter.size()));
                            forste_halvdel.add(notene_navn.get(hasher(notene_navn.indexOf(liste_av_noter.get(0)), note_indekser.get(add_noten) + flat_eller_sharp)));
                            forste_halvdel.addAll(andre_halvdel);
                            liste_av_noter = forste_halvdel;
                            break;
                        }
                    }
                }
                else if (!liste_av_noter.contains(notene_navn.get(hasher(notene_navn.indexOf(liste_av_noter.get(0)), note_indekser.get(add_noten) + flat_eller_sharp)))){  //Hvis noten ikke er i liste_av_noter.
                    //PROBLEM: add13 spiller samme note som add6. add13 funker som  
                    //den skal hvis lengden av liste_av_noter er 4 eller større.
                    liste_av_noter.add(notene_navn.get(hasher(notene_navn.indexOf(liste_av_noter.get(0)), note_indekser.get(add_noten) + flat_eller_sharp)));
                }
            }
            if (kvalitet.equals("6")){ //Legger inn en 6th i lista eller erstatter en 7th med en 6th.
                if (liste_av_noter.size() > 2){
                    liste_av_noter.set(3, notene_navn.get(hasher(notene_navn.indexOf(liste_av_noter.get(3)), - 1)));
                }
            }
            if (kvalitet.equals("maj")){ //Setter 7th til b7th hvis akkord ikke er maj.
                liste_av_noter.set(3, notene_navn.get(hasher(notene_navn.indexOf(liste_av_noter.get(3)), 1)));
            }
            System.out.println(liste_av_noter);
            return liste_av_noter;
        }
        
        public String fjern_chars(String akkord, int lengde_paa_streng){
            StringBuilder string_builder = new StringBuilder(akkord);   //Lager et objekt som er en mutable string,
                                                                        //gjør at vi kan slette chars fra strengen.
            while (lengde_paa_streng > 0){ //Hvis størrelse på liste_av_noter er lik 3 (vanlig triade), 4 eller 5 (7th eller 9th), 
                                            //6 eller 7 (11th eller 13th), så fjernes et visst antall chars i strengen akkord.
                string_builder.deleteCharAt(0);
                lengde_paa_streng --;
            }
            akkord = string_builder.toString(); //Oppdaterer strengen.
            return akkord;
        }
        
        public ArrayList<String> inversion_sjekk(String akkord, ArrayList<String> liste_av_noter){ //Stokker om på elementene dersom det er en inversion.
            String[] splitt_paa_tegnet = akkord.split("/");
            akkord = splitt_paa_tegnet[1];
            if (liste_av_noter.contains(akkord)){
                for (int i = 0; i < liste_av_noter.size(); i++){    //Iterer gjennom liste_av_noter, setter elementet først 
                                                                    //i lista bakerst helt til vi når elementet på akkord.
                    if (liste_av_noter.get(0).equals(akkord)){
                        System.out.println("her");
                        break;
                    }
                    liste_av_noter.add(liste_av_noter.get(0));
                    liste_av_noter.remove(0);
                }
            }
            else {
                Deque<String> midlertidig_deque = new ArrayDeque<>(liste_av_noter);
                midlertidig_deque.addFirst(akkord);
                liste_av_noter.clear();
                while (midlertidig_deque.size() != 0){
                    liste_av_noter.add(midlertidig_deque.pop());
                }
            }
            return liste_av_noter;
        }
                
        public int hasher(int root, int neste_note){ //Gjør det mulig å "loope" rundt i indeks_i_lista slik som ved linear probing.
            int indeks_i_lista = root + neste_note;
            if (indeks_i_lista == 11){
                return indeks_i_lista;
            }
            if (indeks_i_lista > 11){
                indeks_i_lista = (indeks_i_lista % 11) - 1;
                if (indeks_i_lista == -1){ //Denne sjekken må finnes slik at "Cb" er mulig å finne frem til.
                    indeks_i_lista = 11;
                }
            }
            else {
                indeks_i_lista = indeks_i_lista % 11;
                if (indeks_i_lista == -1){ //Denne sjekken må finnes slik at "Cb" er mulig å finne frem til.
                    indeks_i_lista = 11;
                }
            }
            return indeks_i_lista;
        }

        public int finn_oktav(ArrayList<String> lmn, int o, ArrayList<String> ln, int in){ //Avgjør hvliken oktav hver note havner i.
            ArrayList<String> liste_med_notene = lmn;
            int oktaven = o;
            int indexen = in;
            ArrayList<String> liste_med_navn = ln;
            for (int i = 0; i < liste_med_notene.size(); i++){
                if (indexen == 0){
                    return oktaven;
                }
                if ((liste_med_navn.indexOf(liste_med_notene.get(indexen)) < liste_med_navn.indexOf(liste_med_notene.get(indexen-1))) && (liste_med_notene.indexOf(liste_med_notene.get(indexen)) > liste_med_notene.indexOf(liste_med_notene.get(indexen-1)))){
                    oktaven++;      //Hvis elementet på indexen i liste_med_navn er mindre enn 
                                    //elementet foran seg i liste_med_navn OG elementet på indexen i 
                                    //liste_med_notene er større enn elementet foran seg i 
                                    //liste_med_notenem, så går vi opp en oktav og plasserer noter der.
                                    //NB! Dette kan sikkert gjøres enklere ved å bruke en
                                    //metode som er lik hasher() metoden ovenfor.
                    return oktaven;
                }
                else{
                    return oktaven;
                }
            }
            return oktaven;
        }

        public void spill_noten(String n, Color f, int o, int d){ //Spiller noten eller akkorden.
            String denne_noten = n;
            Color farge = f;
            int oktav = o;
            int duration = d;
            // volume_value = slider.getValue();
            // System.out.println(volume_value);
            int start_punkt = 24; //Hvilken oktav vi er i. Begynner på C3.
            int hvilken_note = 0; //Avgjør hvilken note som spilles.
            oktav_liste.get(oktav).get(notene_navn.indexOf(denne_noten)).setBackground(farge);
            hvilken_note += notene_navn.indexOf(denne_noten);
            if (note_farge.get(denne_noten) == Color.WHITE){
                panel_and_pane.get(venstre_naboer.get(oktav_liste.get(oktav_tall).get(notene_navn.indexOf(denne_noten)))).moveToFront(venstre_naboer.get(oktav_liste.get(oktav_tall).get(notene_navn.indexOf(denne_noten))));  
                panel_and_pane.get(hoyre_naboer.get(oktav_liste.get(oktav_tall).get(notene_navn.indexOf(denne_noten)))).moveToFront(hoyre_naboer.get(oktav_liste.get(oktav_tall).get(notene_navn.indexOf(denne_noten))));       
            }     
            if (farge == Color.GREEN){
                    Note noten = new Note();
                    switch(o){
                        case 0:
                            break;
                        case 1:
                            start_punkt += 12;
                            break;
                        case 2:
                            start_punkt += 24;
                            break;
                        case 3:
                            start_punkt += 36;
                            break;
                        case 4:
                            start_punkt += 48;
                            break;
                        case 5:
                            start_punkt += 60;
                            break;
                    }
                    Phrase p = new Phrase();
                    start_punkt += hvilken_note;
                    noten.setPitch(start_punkt);
                    noten.setDuration(duration/1);
                    p.add(noten);
                    Play.midi(p);   //Spiller av noten.
                }
            }
        }

class aktivAkkord implements Runnable{ //Metoden som skal først skal fargelegge notene i akkorden grønne.
    best_frame rammen;
    ArrayList<String> lista;
    int oktav;
    Color farge;
    akkordMonitor monitoren;
    ArrayList<String> navn_paa_noter;
    public aktivAkkord(ArrayList<String> liste_av_noter, best_frame frame, int o, Color farge, akkordMonitor monitoren, ArrayList<String> notene_navn){
        rammen = frame;
        lista = liste_av_noter;
        oktav = o;
        this.farge = farge;
        this.monitoren = monitoren;
        navn_paa_noter = notene_navn;
    }

    public void run(){
        monitoren.fargelegg_gronn(lista, farge, oktav, navn_paa_noter);
    }
}

class inaktivAkkord implements Runnable{ //Metoden som til slutt skal farge de brukte notene til sin opprinnlige farge.
    best_frame rammen;
    ArrayList<String> lista;
    int oktav;
    Color farge;
    akkordMonitor monitoren;
    public inaktivAkkord(ArrayList<String> liste_av_noter, best_frame frame, int o, Color farge, akkordMonitor monitoren, ArrayList<String> notene_navn){
        rammen = frame;
        lista = liste_av_noter;
        oktav = o;
        this.farge = farge;
        this.monitoren = monitoren;
    }

    public void run(){
        try {
            Thread.sleep(1000);
            monitoren.fargelegg_hvit_eller_sort(oktav);
        }

        catch(InterruptedException ie){

        }
    }
}

class akkordMonitor{ //Brukes til avspilling av akkordene i progresjonen.
    Lock laas;
    Condition alle_gronne, alle_hvite;
    String[] alle_noter;
    best_frame rammen;
    int lengde;
    public akkordMonitor(ArrayList<String> liste_av_noter, best_frame rammen){
        laas = new ReentrantLock();
        alle_gronne = laas.newCondition();
        alle_hvite = laas.newCondition();
        alle_noter = new String[liste_av_noter.size()];
        this.rammen = rammen;
        lengde = alle_noter.length;
    }

    public int klartSignal(){
        int storrelse = 0;
            for (int i = 0; i < alle_noter.length; i++){
                if (alle_noter[i] != null){
                    storrelse++;
                }
            }
            return storrelse;
    }

    public void fargelegg_gronn(ArrayList<String> denne_listen, Color farge, int oktav, ArrayList<String> navn_paa_noter){
        laas.lock(); 
        try{
            for (int i = 0; i < denne_listen.size(); i++){
                oktav = rammen.finn_oktav(denne_listen, oktav, navn_paa_noter, i);
                rammen.spill_noten(denne_listen.get(i), farge, oktav, 3);
                if (note_farge.get(denne_listen.get(i)) == Color.WHITE){
                    panel_and_pane.get(venstre_naboer.get(oktav_liste.get(oktav).get(notene_navn.indexOf(denne_listen.get(i))))).moveToFront(venstre_naboer.get(oktav_liste.get(oktav).get(notene_navn.indexOf(denne_listen.get(i)))));  
                    panel_and_pane.get(hoyre_naboer.get(oktav_liste.get(oktav).get(notene_navn.indexOf(denne_listen.get(i))))).moveToFront(hoyre_naboer.get(oktav_liste.get(oktav).get(notene_navn.indexOf(denne_listen.get(i)))));
                }
                alle_noter[i] = denne_listen.get(i);    
            }
            alle_hvite.signal(); //Kan nå fargelegge notene hvite.
        }
        
        catch(IndexOutOfBoundsException ioobe){

        }

        finally{
            laas.unlock();
        }
    }

    public void fargelegg_hvit_eller_sort(int oktav){
        laas.lock();
        try{
            if (klartSignal() != lengde){
                alle_hvite.await(); //Venter til notene er fargelagt grønne.
            }
            rammen.fargelegg_oktaver(oktav);    //Fargelegger notene til opprinnlig farge igjen, går over alle oktavene
                                                //siden vi kan spille av en lang akkord over flere oktaver.
            rammen.fargelegg_oktaver(oktav+1);
            rammen.fargelegg_oktaver(oktav+2);
            rammen.fargelegg_oktaver(oktav+3);
            rammen.fargelegg_oktaver(oktav+4);
            rammen.fargelegg_oktaver(oktav+5);
        }
        catch(InterruptedException a){
            System.out.println(a);
        }

        catch(IndexOutOfBoundsException ioob){

        }

        finally{
            laas.unlock();
        }
    }
}

class Avslutt implements ActionListener { //Avslutter programmet og lukker vinduet.
    @Override
    public void actionPerformed(ActionEvent e) {
        controller.avsluttSpill();
        }
    }
}
