import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Timer;
import javax.sound.midi.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.font.TextAttribute; 
import java.awt.datatransfer.UnsupportedFlavorException;

public class pianoView {
    //Datastrukturer for oversikt i programmet:
    private ArrayList<tangent> oktav;    //Liste over tangenter. Den siste oktaven vil 
                                        //kun ha en tangent (C).
    private ArrayList<ArrayList<tangent>> oktav_liste;   //Liste over alle oktavene.
                                                        //Det er 6 oktaver totalt.
    private ArrayList<String> notene_navn = new ArrayList<>(Arrays. asList("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"));  //Liste med navn på notene fra C til B.
    private ArrayList<String> flats_og_sharps = new ArrayList<>(Arrays. asList("b5", "b6", "b7" ,"b9" ,"b1" ,"#5" ,"#6" ,"#7" ,"#9" ,"#1"));   //Liste av tall som brukes til å sjekke
                                                                                                                                                    //om en akkord har noter som enten er b
                                                                                                                                                    //eller # i metoden string_til_noter().
    private ArrayList<String> kvalitet_strenger = new ArrayList<>(Arrays. asList("min", "-", "aug", "+", "maj", "dim", "sus", "add", "6", "m"));   //Liste av strenger som brukes til å sjekke om
                                                                                                                                                        //en samling av tre chars utjgør en gyldig streng
                                                                                                                                                        //som betegner en akkords kvalitet i metoden string_til_noter().
    private Map<Integer, Integer> note_indekser = new HashMap<Integer, Integer>() {{put(5,7);put(7,11);put(9,14);put(11,17);put(13,21);put(2,2);put(4,5);put(6,9);}}; //Assosiserer en en note med en avstand.
    private Map<Character, Integer> flat_sharp_verdier =  new HashMap<Character, Integer>(){{put('b', - 1);put('#', 1);}};  //Brukes i string_til_noter() for å inkrementere et steg ned eller opp.
    private HashMap<String, generisk> connection;   //Kobler en streng med enten
                                                    //en/et knapp, panel eller label.
    private Map<String, String> tastatur = new HashMap<String, String>() {{put("A", "C"); put("W", "Db"); put("S", "D"); put("E", "Eb"); put("D", "E"); put("F", "F"); put("T", "Gb"); put("G", "G"); put("Y", "Ab"); put("H", "A"); put("U", "Bb"); put("J", "B");}};  //Gjør at en en tast på tastaturet (med og uten capslock)
                                                                                                                                                                                                                                                                                                                    //assosieres med en tangent i keyboardet. Brukes senere i
                                                                                                                                                                                                                                                                                                                    //KeyboardListener for å finne ut hvilken tast en bruker
                                                                                                                                                                                                                                                                                                                    //har trykket på.
    private ArrayList<tangent> C_noter, Db_noter, D_noter, Eb_noter, E_noter, F_noter, Gb_noter, G_noter, Ab_noter, A_noter, Bb_noter, B_noter;
    private Map<String, ArrayList<tangent>> note_og_hashmap;
    private option_panel currently_selected_settings_panel, general_options_panel, regex_options_panel, export_part_1, export_part_2;
    private Component[] general_settings_components, regex_settings_components, export_part_1_components, export_part_2_components; 
    private Integer oktav_tall; //Brukes gjennom hele programmet til å holde styr på
                                //hvilken oktav i lista oktav_liste vi vil ha tak i.
    private Pattern regex_pattern = Pattern.compile("[A-G]{1}[b|#]?{1}(([6|7|9]{1}|[11|13]{2}|[+|\\-|m]{1}([6|7|9]{1}|[11|13]{2})?{1}|[min|aug|dim]{3}([6|7|9]{1}|[11|13]{2})?{1}|([maj7|maj9]{4}|[maj11|maj13]{5})?{1})?{1}(sus[2|4])?{1}(add([2|4|5|6|7|9]{1}|[11|13]{2}))?{1}([b|#]{1}5)?{1}([b|#]{1}6)?{1}([b|#]{1}7)?{1}([b|#]{1}9)?{1}([b|#]{1}11)?{1}([b|#]{1}13)?{1})?(/{1}[A-G]{1}[b|#]?{1})?");
    private String midi_file_name, midi_file_path;
    private int current_sound; //Verdiene til settings componentene
    private int current_BPM; //Betegner nåværende BPM
    private int BPM_to_delay;
    private Color current_color;
    private boolean border_on;
    private boolean visual_on;
    private boolean shift_on;

    //Visuelt:
    private JButton avslutt, spill_harmoni, lag_harmoni, volume; //Alle knappene i programmet.
    private best_frame vinduet;
    private JLabel C1, C2, C3, C4, C5, C6, info_1, info_2, volume_tekst, oktav_tekst, import_text, export_text,
            connect_text, chord_overskrift, blank_tekst, SOUND_text, BPM_text, METRONOME_text, CONTROLLER_text;
    private JPanel test_1, test_2, hvit_panelet, sort_panelet, knappe_panelet, skjerm_panelet, info_panelet_1, info_panelet_2, harmoni_panel, volume_panel,
            oktav_panel, oktav_panel_2, various_buttons_panel, various_buttons_text,
            chord_panel_3, overskrift_panel, P1, P2, P3, P4, P5, P6, slider_panel, chord_panel_over, border_1, border_2,
            SOUND_panel, BPM_panel, METRONOME_panel, CONTROLLER_panel;
    private ArrayList<JPanel> sorte_tangenter;
    private SpringLayout spring; //Gir layoutet til de sorte tangentene.
    private JSlider slider; //Volume slideren
    private JTextField chord_tekst; //Der man skriver inn URL'en eller listen med akkorder.
    private PanelRound visual; //Et panel med avrundede kanter.
    private ButtonRound oktav_opp, oktav_ned, import_harmony, export_harmony, connect_synth, configure_synth, metronome_on_or_off, see_settings; //Avrundede knapper.
    private JLayeredPane overste_lag, nederste_lag, blaa_panelet;
    private pianoController controller; //Referanse til controller.
    private ArrayList<Thread> traad_liste_test; //FIKS FOR AVSPILLING AV AKKORDER I AKKORDPROGRESJONEN
    private int stop_check;
    private Timer akkord_timer;
    private Sequencer sequencer; //NYTT SYSTEM FOR AVSPILLING AV NOTER
    private Sequence sequence;
    Synthesizer synth_1, synth_2;
    Receiver PC_receiver, keyboard_receiver;
    midi_controller current_midi_controller;
    Thread config_thread, start_stop_thread, set_back_thread;
    AtomicBoolean paused;
    private int first_play;

    
    JPanel layer_test_tangent;
    JLayeredPane test_lag;

    public void GUI(pianoController kont) { //Opprettelsen av selve programmet.
        controller = kont;
        connection = new HashMap<String, generisk>();
        oktav_tall = 2;

        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.exit(9);
        }

        //------ Oppretter tangentene: ------//
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
        hvit_panelet.setSize(1281, 170);
        hvit_panelet.setLayout(new GridLayout(1, 36));

        nederste_lag = new JLayeredPane(); //Panelet som gjør at de hvite tangentene ligger "under" de sorte tangentene.
        nederste_lag.setVisible(true);
        nederste_lag.setBackground(Color.DARK_GRAY);
        nederste_lag.setBounds(0, 400, 1281, 170);
        nederste_lag.add(hvit_panelet, 0);

        sort_panelet = new JPanel(); //Panelet til de sorte tangentene.
        spring = new SpringLayout();
        sort_panelet.setLayout(spring);
        sort_panelet.setOpaque(false);
        sort_panelet.setSize(1176, 100);

        overste_lag = new JLayeredPane(); //Panelet som gjør at de sorte tangentene ligger "over" de hvite tangentene.
        overste_lag.setBackground(Color.DARK_GRAY);
        overste_lag.setVisible(true);
        overste_lag.setBounds(35, 400, 1176, 100);
        overste_lag.add(sort_panelet, 200);

        vinduet = new best_frame("Piano Ver.2.0");

        current_color = new Color(66,255,73); //Fargen til tangentene når de er avspilte. Gammel farge (grønn): 0,255,0

        ArrayList<Integer> amount_pr_octave = new ArrayList<>(Arrays. asList(12, 12, 12, 12, 12, 1));
        ArrayList<Integer> black_and_white_order = new ArrayList<>(Arrays. asList(0,1,0,1,0,0,1,0,1,0,1,0));
        int tangent_nummer = 1;
        int note_verdi = 36;  //Endre tilbake til 60 etter EXPORT-video
        oktav_liste = new ArrayList<ArrayList<tangent>>(); //Lista av oktaver
        C_noter = new ArrayList<tangent>(); //Hvert tangent_nummer til hver note, C_noter vil ha en note mer enn de andre listene.
        Db_noter = new ArrayList<tangent>();
        D_noter = new ArrayList<tangent>();
        Eb_noter = new ArrayList<tangent>(); 
        E_noter = new ArrayList<tangent>();
        F_noter = new ArrayList<tangent>();
        Gb_noter = new ArrayList<tangent>();
        G_noter = new ArrayList<tangent>(); 
        Ab_noter = new ArrayList<tangent>(); 
        A_noter = new ArrayList<tangent>();
        Bb_noter = new ArrayList<tangent>();
        B_noter = new ArrayList<tangent>();
        //Lager liste som alle disse er inne i - gjør det lettere å sette inn tangenter i for-løkken under
        note_og_hashmap = new HashMap<String, ArrayList<tangent>>() {{put("C",C_noter); put("Db", Db_noter); put("D", D_noter); put("Eb", Eb_noter); put("E", E_noter); put("F", F_noter); put("Gb", Gb_noter); put("G", G_noter); put("Ab", Ab_noter); put("A", A_noter); put("Bb", Bb_noter); put("B", B_noter);}};

        for (int note_amount = 0; note_amount < amount_pr_octave.size(); note_amount++){

            oktav = new ArrayList<tangent>();

            for (int current_note = 0; current_note < amount_pr_octave.get(note_amount); current_note++){

                tangent tangenten = new tangent(notene_navn.get(current_note), tangent_nummer, note_verdi, note_amount, black_and_white_order.get(current_note), 50);
                note_og_hashmap.get(tangenten.navn).add(tangenten); //Setter inn tangenten i ordboken som hører til noten.
                tangent_nummer ++;
                note_verdi ++;

                if (tangenten.original_color == Color.WHITE){
                    hvit_panelet.add(tangenten);
                }
                else {
                    sort_panelet.add(tangenten);
                }

                oktav.add(tangenten);

            }

            oktav_liste.add(oktav);

        }

        ArrayList<Integer> spaces = new ArrayList<>(Arrays. asList(50, 17, 50, 16, 17));
        Component myComps[] = sort_panelet.getComponents();
        for (int i = 1; i < myComps.length; i++){ //Lager korrekt visuell avstand mellom de sorte tangentene.
            spring.putConstraint(SpringLayout.WEST, myComps[i], spaces.get(i % 5), SpringLayout.EAST, myComps[i-1]);    
        }

        vinduet.add(border_1);
        vinduet.add(border_2);        
        vinduet.add(overste_lag);   //Legges til før nederste_lag så det ligger "øverst" av tangentpanelene.
        vinduet.add(nederste_lag);

        int x_coordinate = 12;
        for (int i = 1; i < 7; i++){
            JPanel C_panel = new JPanel();
            C_panel.setBackground(Color.DARK_GRAY);
            C_panel.setBounds(x_coordinate, 370, 30, 30);
            String navn = "C" + Integer.toString(i);
            JLabel C_label = new JLabel(navn);
            C_label.setFont(new FontUIResource(navn, 5, 18));
            C_label.setForeground(Color.LIGHT_GRAY);
            C_panel.add(C_label);
            vinduet.add(C_panel);
            x_coordinate += 245;
        }

        //------ Oppretter panelet til PLAY, CLEAR OG EXIT: ------//
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


        //------ Oppretter det blå panelet som brukes av SETTINGS: ------//
        current_sound = 144; //Hvilket instrument som brukes i avspilling av noter og progresjoner.
        current_BPM = 100; //BPM verdien som brukes i avspillingen av progresjonen og metronomen.
        BPM_to_delay = 2400; //Er lik 100bpm. En økning på 116 i BPM_to_delay, tillsier en økning på 5 i current_BPM.
        // 100 = 2400, 105 = 2284, 110 = 2180, 115 = 2088, 120 = 2000, etc. Formel for BPM: (60000/BPM) * 4
        border_on = true; //Om de grønne strekene som viser hvilken oktav du er i vises eller ikke.
        visual_on = true; //Om det vises om tangentene trykkes ned eller ikke når man spiller på keyboardet.
        shift_on = true; //Om programmet bytter rad under avspilling eller ikke.
        
        Color blue_panel_background_color = new Color(47, 73, 245);
        Color panel_text_color = new Color(175, 234, 255);

        general_options_panel = new option_panel(1, "GENERAL:", panel_text_color, blue_panel_background_color);

        JPanel general_grid = new JPanel(); //Panelet som utgjør main_panel i general_options_panel/ "GENERAL".
        general_grid.setBackground(blue_panel_background_color);
        GridLayout specifications = new GridLayout(3, 4);
        // specifications.setHgap(12);
        general_grid.setLayout(specifications);
        ArrayList<String> general_settings_labels = new ArrayList<>(Arrays. asList("SOUND:", "BORDER:", "BPM:",  "VISUALS:", "COLOR:", "NEW ROW:"));
        for (int i = 0; i < general_settings_labels.size(); i++){
            JLabel test = new JLabel(general_settings_labels.get(i));
            test.setFont(new FontUIResource(general_settings_labels.get(i), 5, 12));
            test.setForeground(panel_text_color);
            test.setHorizontalAlignment(JTextField.CENTER);
            general_grid.add(test, BorderLayout.CENTER);
            generisk<JLabel> nye = new generisk<>(test);
            connection.put(general_settings_labels.get(i), nye);
            if (i  % 2 == 0){ //Hvis partall
                JTextField writable = new JTextField("1");
                writable.setDisabledTextColor(panel_text_color);
                writable.setName(general_settings_labels.get(i));
                writable.setFont(new FontUIResource("1", 5, 12));
                writable.setBackground(blue_panel_background_color);
                writable.setForeground(panel_text_color);
                writable.setBorder(BorderFactory.createLineBorder(blue_panel_background_color));
                writable.setHorizontalAlignment(JTextField.CENTER);
                writable.setEnabled(false);
                writable.setEditable(false);
                writable.addMouseListener(vinduet);
                general_grid.add(writable, BorderLayout.CENTER);
                
                generisk<JTextField> ny = new generisk<>(writable);
                connection.put(general_settings_labels.get(i), ny);
            }
            else {
                JLabel on_or_off = new JLabel("ON");
                on_or_off.setName(general_settings_labels.get(i));
                on_or_off.setFont(new FontUIResource("ON", 5, 12));
                on_or_off.setHorizontalAlignment(JTextField.CENTER);
                on_or_off.setForeground(panel_text_color);
                on_or_off.setEnabled(false);
                on_or_off.addMouseListener(vinduet);
                general_grid.add(on_or_off, BorderLayout.CENTER);
                generisk<JLabel> ny = new generisk<>(on_or_off);
                connection.put(general_settings_labels.get(i), ny);
            }
        }

        general_options_panel.set_main_component(general_grid, 4, 6, 260, 40); //Setter general_grid til å være main_component i general_options_panel og lager en liste av komponentene i general_grid.
        JTextField fix_value_1 = (JTextField) general_options_panel.main_list_of_inner_components[5];
        fix_value_1.setText("100"); //Gjør visuel fiks av denne komponenten ved oppstart av programmet.
        JTextField fix_value_2 = (JTextField)general_options_panel.main_list_of_inner_components[9];
        fix_value_2.setText("66,255,73"); //Gjør visuel fiks av denne komponenten ved oppstart av programmet.
        general_settings_components = general_options_panel.main_list_of_inner_components; //Brukes i mouselistener til å gjøre endringer i komponentene til general_grid.
        general_options_panel.setVisible(false); //Settes kun visible og editable når "SETTINGS" trykkes på.
        vinduet.add(general_options_panel);
        currently_selected_settings_panel = general_options_panel;

        regex_options_panel = new option_panel(2, "CHORD RECOGNITION PATTERNS:", panel_text_color, blue_panel_background_color);

        JPanel regex_grid = new JPanel();
        regex_grid.setBackground(blue_panel_background_color);
        GridLayout specifications_2 = new GridLayout(8, 2);
        // specifications_2.setVgap(2);
        regex_grid.setLayout(specifications_2);
        ArrayList<String> regex_settings_labels = new ArrayList<>(Arrays. asList("MINOR LETTERS:", "MINOR SYMBOL:", "AUGMENTED LETTERS:", "AUGMENTED SYMBOL:", "MAJOR 7TH:", "DIMINISHED:", "SUSPENDED:", "ADD:"));
        for (int i = 0; i < regex_settings_labels.size(); i++){
            JLabel test = new JLabel(regex_settings_labels.get(i));
            test.setFont(new FontUIResource(regex_settings_labels.get(i), 5, 12));
            test.setForeground(panel_text_color);
            // test.setHorizontalAlignment(JTextField.CENTER);
            regex_grid.add(test, BorderLayout.CENTER);
            generisk<JLabel> nye = new generisk<>(test); 
            connection.put(regex_settings_labels.get(i), nye); //Disse legges også til ordboken.

            JTextField writable = new JTextField(kvalitet_strenger.get(i));
            // writable.setLayout(new BorderLayout());
            writable.setDisabledTextColor(panel_text_color);
            writable.setName(regex_settings_labels.get(i));
            writable.setSize(50, 15);
            writable.setFont(new FontUIResource(kvalitet_strenger.get(i), 5, 12));
            writable.setBackground(blue_panel_background_color);
            writable.setForeground(panel_text_color);
            writable.setBorder(BorderFactory.createLineBorder(blue_panel_background_color));
            writable.setHorizontalAlignment(JTextField.CENTER);
            writable.setEditable(false);
            writable.addMouseListener(vinduet);
            regex_grid.add(writable, BorderLayout.CENTER);
            generisk<JTextField> ny = new generisk<>(writable);
            connection.put(regex_settings_labels.get(i), ny);
        }
        regex_options_panel.make_scrollable(regex_grid, 2, 2, blue_panel_background_color, panel_text_color);
        regex_settings_components = regex_options_panel.main_list_of_inner_components;
        regex_options_panel.setVisible(false);
        vinduet.add(regex_options_panel);

        general_options_panel.set_neste(regex_options_panel);
        regex_options_panel.set_forrige(general_options_panel);

        export_part_1 = new option_panel(3, "CHOOSE EXPORT METHOD:", panel_text_color, blue_panel_background_color);
        JPanel export_part_1_main_panel = new JPanel();
        export_part_1_main_panel.setBackground(blue_panel_background_color);

        ArrayList<String> export_part_1_labels = new ArrayList<>(Arrays. asList("ADD TO CLIPBOARD", "DOWNLOAD MIDI-FILE", "CANCEL"));
        ArrayList<Component> export_part_1_finised = new ArrayList<Component>();
        JPanel double_panel = new JPanel();
        GridLayout specifications_3 = new GridLayout(1, 2, 60, 0);
        double_panel.setLayout(specifications_3);
        double_panel.setBackground(blue_panel_background_color);

        for (int i = 0; i < export_part_1_labels.size(); i++){
            JLabel export_part_1_label = new JLabel(export_part_1_labels.get(i));
            export_part_1_label.setName(export_part_1_labels.get(i));
            export_part_1_label.setFont(new FontUIResource(export_part_1_labels.get(i), 5, 12));
            export_part_1_label.setBorder(BorderFactory.createLineBorder(blue_panel_background_color));
            export_part_1_label.setForeground(panel_text_color); 
            export_part_1_label.setFocusable(false); 
            export_part_1_label.addMouseListener(vinduet);
            if (!export_part_1_labels.get(i).equals("CANCEL")){ //Skal ha underlines under alle untatt CANCEL.
                Font font = export_part_1_label.getFont();
                Map attributes = font.getAttributes();
                attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                export_part_1_label.setFont(font.deriveFont(attributes));
                double_panel.add(export_part_1_label);
            }
            else {
                export_part_1_finised.add(export_part_1_label);
            }
            generisk<JLabel> ny = new generisk<>(export_part_1_label);
            connection.put(export_part_1_labels.get(i), ny);
        }
        export_part_1_finised.add(double_panel);

        GridBagConstraints bag_part_1 = new GridBagConstraints();
        bag_part_1.anchor = GridBagConstraints.CENTER;
        bag_part_1.weighty = 1;
        bag_part_1.weightx = 1;
        bag_part_1.gridy = 0;
        bag_part_1.gridx = 0;
        bag_part_1.fill = GridBagConstraints.VERTICAL;
        bag_part_1.fill = GridBagConstraints.HORIZONTAL;
        export_part_1_main_panel.add(export_part_1_finised.get(1), bag_part_1);

        bag_part_1.anchor = GridBagConstraints.PAGE_END;
        bag_part_1.weighty = 0;
        bag_part_1.weightx = 0;
        bag_part_1.gridy = 1;
        bag_part_1.gridx = 0;
        bag_part_1.fill = GridBagConstraints.NONE;
        bag_part_1.fill = GridBagConstraints.NONE;
        export_part_1_main_panel.add(export_part_1_finised.get(0), bag_part_1);

        export_part_1.set_main_component(export_part_1_main_panel, 8, 2, 350, 40);
        export_part_1_components = export_part_1.main_list_of_inner_components;
        vinduet.add(export_part_1);
        export_part_1.setVisible(false);

        midi_file_name = "midi_file";
        midi_file_path = "midi_files\\";

        export_part_2 = new option_panel(4, "CHOOSE FILE NAME AND PATH:", panel_text_color, blue_panel_background_color);
        JPanel export_part_2_main_panel = new JPanel();
        export_part_2_main_panel.setBackground(blue_panel_background_color);
        GridLayout specifications_4 = new GridLayout(3, 1, 0, -2);
        export_part_2_main_panel.setLayout(specifications_4);
        ArrayList<String> export_part_2_labels = new ArrayList<>(Arrays. asList("PATH:", "NAME:", "FINISH", "CANCEL"));
        ArrayList<Component> export_part_2_finished = new ArrayList<Component>();

        for (int i = 0; i < export_part_2_labels.size(); i++){
            JLabel export_part_2_label = new JLabel(export_part_2_labels.get(i));
            export_part_2_label = vinduet.generate_blue_panel_label(export_part_2_label, export_part_2_labels.get(i), blue_panel_background_color, panel_text_color);
            export_part_2_finished.add(export_part_2_label);

            if (i == 1){
                JTextField export_part_2_field_1 = new JTextField();
                export_part_2_field_1 = vinduet.generate_blue_panel_field(export_part_2_field_1, "FILE PATH", midi_file_path + "        ", blue_panel_background_color, panel_text_color);
                export_part_2_finished.add(export_part_2_field_1);
 
                JTextField export_part_2_field_2 = new JTextField();
                export_part_2_field_2 = vinduet.generate_blue_panel_field(export_part_2_field_2, "FILE NAME" , "     " + midi_file_name, blue_panel_background_color, panel_text_color);
                export_part_2_finished.add(export_part_2_field_2);
            }
            
        }

        ArrayList<Integer> h_distances = new ArrayList<>(Arrays. asList(65, 30, 70));
        for (int i = 0; i < h_distances.size(); i++){
            JPanel part_2_panel = new JPanel();
            GridLayout specifications_5 = new GridLayout(1, 2, h_distances.get(i), 0);
            part_2_panel.setLayout(specifications_5);
            part_2_panel.add(export_part_2_finished.get(i + i)); part_2_panel.add(export_part_2_finished.get(i + i + 1));
            part_2_panel.setBackground(blue_panel_background_color);
            export_part_2_main_panel.add(part_2_panel);
        }

        export_part_2.set_main_component(export_part_2_main_panel, 0, 4, 470, 40);
        export_part_2_components = export_part_2.main_list_of_inner_components;
        vinduet.add(export_part_2);
        export_part_2.setVisible(false);


        //LAG FOR-LØKKE SOM OPPRETTER SOUND, BPM OG METRONOME
        SOUND_panel = new JPanel(new GridLayout());
        SOUND_panel.setOpaque(false);
        SOUND_panel.setBounds(385, 330, 80, 25);
        SOUND_text = new JLabel("SOUND: 1");
        SOUND_text.setFont(new FontUIResource("SOUND: 1", 5, 12));
        SOUND_text.setForeground(panel_text_color);
        SOUND_panel.add(SOUND_text, BorderLayout.CENTER);
        vinduet.add(SOUND_panel);

        BPM_panel = new JPanel(new GridLayout());
        BPM_panel.setOpaque(false);
        BPM_panel.setBounds(485, 330, 80, 25);
        BPM_text = new JLabel("BPM: " + Integer.toString(current_BPM));
        BPM_text.setFont(new FontUIResource("BPM: " + Integer.toString(current_BPM), 5, 12));
        BPM_text.setForeground(panel_text_color);
        BPM_panel.add(BPM_text, BorderLayout.CENTER);
        vinduet.add(BPM_panel);

        METRONOME_panel = new JPanel(new GridLayout());
        METRONOME_panel.setOpaque(false);
        METRONOME_panel.setBounds(580, 330, 120, 25);
        METRONOME_text = new JLabel("METRONOME: OFF");
        METRONOME_text.setFont(new FontUIResource("METRONOME: OFF", 5, 12));
        METRONOME_text.setForeground(panel_text_color);
        METRONOME_panel.add(METRONOME_text, BorderLayout.CENTER);
        vinduet.add(METRONOME_panel);

        CONTROLLER_panel = new JPanel();
        CONTROLLER_panel.setOpaque(false);
        CONTROLLER_panel.setBounds(385, 278, 307, 23); //y:310
        CONTROLLER_text = new JLabel("CONTROLLER: NONE");
        CONTROLLER_text.setFont(new FontUIResource("CONTROLLER: NONE", 5, 12));
        CONTROLLER_text.setForeground(panel_text_color);
        GridLayout test_grid = new GridLayout();
        CONTROLLER_text.setLayout(test_grid);
        CONTROLLER_panel.add(CONTROLLER_text, BorderLayout.CENTER);
        vinduet.add(CONTROLLER_panel);

        blaa_panelet = new JLayeredPane();
        blaa_panelet.setBounds(350, 280, 380, 75);
        harmoni_panel = new JPanel(); //Det blå panelet over tangentene.
        harmoni_panel.setBackground(blue_panel_background_color); harmoni_panel.addMouseListener(vinduet); harmoni_panel.setName("WRITE_HARMONY"); harmoni_panel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        harmoni_panel.setSize(380, 75);
        vinduet.add(blaa_panelet);
        blaa_panelet.add(harmoni_panel, 100);

        chord_tekst = new JTextField("");
        chord_tekst.setFont(new FontUIResource("", 5, 22));
        chord_tekst.setBounds(355, 303, 370, 25); //y:287
        chord_tekst.setBackground(blue_panel_background_color);
        chord_tekst.setForeground(panel_text_color);
        chord_tekst.setBorder(BorderFactory.createLineBorder(blue_panel_background_color));
        chord_tekst.setHorizontalAlignment(JTextField.CENTER);
        chord_tekst.addMouseListener(vinduet);
        chord_tekst.setName("WRITE_HARMONY");
        chord_tekst.setEditable(false);
        chord_tekst.addKeyListener(vinduet);
        vinduet.add(chord_tekst);

        //------ Oppretter volume-slideren: ------//
        slider_panel = new JPanel(new BorderLayout()); //Ugjør slideren til som justerer volumet.
        slider_panel.setBounds(53, 326, 200, 44);
        slider = new JSlider() {
            @Override
            public void updateUI() {
                setUI(new CustomSliderUI(this));
            }
        };
        slider.setFocusable(false); slider.setBackground(Color.DARK_GRAY); slider.addMouseListener(vinduet);
        slider.setName("VOLUME");
        slider_panel.add(slider);
        vinduet.add(slider_panel);

        int volume_visual_x = 89;
        int volume_visual_y = 313;
        int volume_visual_height = 6;
        for (int i = 0; i < 14; i++){
            PanelRound volume_visual = new PanelRound();
            volume_visual.setBackground(Color.GRAY);
            volume_visual.setBounds(volume_visual_x, volume_visual_y, 3, volume_visual_height);
            volume_visual.setRoundTopLeft(6);
            volume_visual.setRoundTopRight(6);
            volume_visual.setRoundBottomRight(6);
            volume_visual.setRoundBottomLeft(6);
            vinduet.add(volume_visual);
            volume_visual_x += 10;
            volume_visual_y -= 2;
            volume_visual_height += 2;
        }
        
        //------ Oppretter oktav knappene og navnet til programmet: ------//
        Color name_color = new Color(120, 193, 227);

        oktav_panel = new JPanel(); //Panelet og teksten der det står "Piano Ver.2"
        oktav_tekst = new JLabel("Piano Ver.2");
        oktav_panel_2 = new JPanel();
        oktav_panel.setBackground(Color.DARK_GRAY); oktav_panel.setBounds(1068, 273, 130, 30);
        oktav_tekst.setFont(new FontUIResource("Piano Ver.2", 5, 20)); oktav_tekst.setForeground(name_color);
        oktav_panel_2.setBackground(Color.DARK_GRAY); oktav_panel_2.setBounds(1068, 314, 130, 42); oktav_panel_2.setLayout(new GridLayout(1, 2, 11, 0));
        oktav_panel.add(oktav_tekst);
        vinduet.add(oktav_panel);
        vinduet.add(oktav_panel_2);

        oktav_ned = new ButtonRound(); //Venstre oktav-knapp.
        oktav_ned.setText("\u2BC7" + "OCTAVE"); oktav_ned.setFont(new FontUIResource("\u2BC7" + "OCTAVE", 5, 10)); oktav_ned.setRadius(8); oktav_ned.setColor(Color.LIGHT_GRAY); oktav_ned.setBorderColor(Color.DARK_GRAY); oktav_ned.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY)); oktav_ned.setFocusable(false); oktav_ned.addMouseListener(vinduet); oktav_ned.setName("OKTAV_NED");
        oktav_panel_2.add(oktav_ned);

        oktav_opp = new ButtonRound(); //Høyre oktav-knapp.
        oktav_opp.setText("OCTAVE" + "\u2BC8"); oktav_opp.setFont(new FontUIResource("OCTAVE" + "\u2BC8", 5, 10)); oktav_opp.setRadius(8); oktav_opp.setColor(Color.LIGHT_GRAY); oktav_opp.setBorderColor(Color.DARK_GRAY); oktav_opp.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY)); oktav_opp.setFocusable(false); oktav_opp.addMouseListener(vinduet); oktav_opp.setName("OKTAV_OPP");
        oktav_panel_2.add(oktav_opp);    

        //------ Oppretter panelet med IMPORT, CONNECT, METRO., etc. knappene: ------//
        ArrayList<String> various_names = new ArrayList<>(Arrays. asList("IMPORT   ", "CONNECT  ", "METRO.", "EXPORT   ", "CONFIG. ", "SETTINGS"));
        import_harmony = new ButtonRound();
        export_harmony = new ButtonRound();
        connect_synth = new ButtonRound();
        configure_synth = new ButtonRound();
        metronome_on_or_off = new ButtonRound();
        see_settings = new ButtonRound();
        ArrayList<ButtonRound> various_buttons = new ArrayList<>(Arrays. asList(import_harmony, connect_synth, metronome_on_or_off, export_harmony, configure_synth, see_settings));

        various_buttons_panel = new JPanel(); //Panelet hvor selve knappene IMPORT og EXPORT skal være.
        various_buttons_panel.setBounds(756, 298, 215, 48); various_buttons_panel.setLayout(new GridLayout(2, 3, 25, 24)); various_buttons_panel.setOpaque(false);

        various_buttons_text = new JPanel(); //Panelet der teksten IMPORT og EXPORT skal være.
        various_buttons_text.setBounds(759, 281, 215, 58); various_buttons_text.setLayout(new GridLayout(2, 3, 20, 15)); various_buttons_text.setOpaque(false);

        Color button_color = new Color(119, 122, 128);

        for (int i = 0; i < various_names.size(); i++){
            various_buttons.get(i).setRadius(5);
            various_buttons.get(i).setColor(Color.LIGHT_GRAY);
            various_buttons.get(i).setBorderColor(Color.DARK_GRAY);
            various_buttons.get(i).setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY)); 
            various_buttons.get(i).setFocusable(false);
            various_buttons.get(i).addMouseListener(vinduet);
            various_buttons.get(i).setName(various_names.get(i).replaceAll("\\s+",""));
            various_buttons_panel.add(various_buttons.get(i));
            generisk<ButtonRound> a = new generisk<>(various_buttons.get(i));
            connection.put(various_names.get(i).replaceAll("\\s+",""), a);

            JLabel ny_tekst = new JLabel(various_names.get(i));
            ny_tekst.setFont(new FontUIResource(various_names.get(i), 5, 10));
            ny_tekst.setHorizontalAlignment(JLabel.CENTER);
            ny_tekst.setForeground(Color.LIGHT_GRAY);

            various_buttons_text.add(ny_tekst, BorderLayout.CENTER);

        }
        vinduet.add(various_buttons_text);
        vinduet.add(various_buttons_panel);

        //------ Oppretter akkordpanel-generatoren, panelene, og knappene tilhørende dem: ------//
        JPanel basis_panel = new JPanel(new GridLayout());
        basis_panel.setBounds(58, 30, 1173, 200); //(old) height:134, (old)width:1173
        basis_panel.setBackground(Color.GRAY);
        
        JPanel connect_to_scrollable = new JPanel();
        GridLayout grid_specifications = new GridLayout(0, 8);
        connect_to_scrollable.setBackground(Color.GRAY);
        grid_specifications.setHgap(3);
        grid_specifications.setVgap(3);
        connect_to_scrollable.setLayout(grid_specifications);

        JScrollPane scrollable_panel = new JScrollPane(connect_to_scrollable);
        custom_scrollable new_UI = new custom_scrollable(scrollable_panel, Color.GRAY, Color.LIGHT_GRAY, 10, 10); 
        scrollable_panel.setBorder(BorderFactory.createEmptyBorder());
        scrollable_panel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollable_panel.getVerticalScrollBar().setUnitIncrement(2);
        scrollable_panel.getVerticalScrollBar().setOpaque(true);
        basis_panel.add(scrollable_panel, BorderLayout.CENTER);

        chord_panel_generator generator = new chord_panel_generator(vinduet, connect_to_scrollable); //Bytt ut vindu med scrollable_panel
        vinduet.set_generator(generator);

        vinduet.add(basis_panel);

        add_or_remove create_new_panel = new add_or_remove(1, generator, vinduet);
        create_new_panel.setName("CREATE_PANEL");
        create_new_panel.setBounds(34, 30, 22, 100); //x:34
        create_new_panel.setBackground(Color.LIGHT_GRAY);
        create_new_panel.setRoundTopLeft(15);
        create_new_panel.setFocusable(false);
        GridLayout grid_1 = new GridLayout();
        JLabel label_1 = new JLabel("+");
        label_1.setLayout(grid_1);
        label_1.setFont(new FontUIResource("+", 5, 18));
        label_1.setForeground(Color.DARK_GRAY);
        create_new_panel.add(label_1, BorderLayout.CENTER);
        vinduet.add(create_new_panel);

        add_or_remove remove_newest_panel = new add_or_remove(0, generator, vinduet);
        remove_newest_panel.setName("REMOVE_PANEL");
        remove_newest_panel.setBounds(34, 132, 22, 98);
        remove_newest_panel.setBackground(Color.LIGHT_GRAY);
        remove_newest_panel.setRoundBottomLeft(15); 
        remove_newest_panel.setFocusable(false);
        GridLayout grid_2 = new GridLayout();
        JLabel label_2 = new JLabel("-");
        label_2.setLayout(grid_2);
        label_2.setFont(new FontUIResource("-", 5, 18));
        label_2.setForeground(Color.DARK_GRAY);
        remove_newest_panel.add(label_2, BorderLayout.CENTER);
        vinduet.add(remove_newest_panel);

        vinduet.set_add_and_remove(create_new_panel, remove_newest_panel);

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

        //------ Oppretter generiske visuelle ting til programmet: ------//
        visual = new PanelRound(); //Lager de avrundede kantene til selve pianoet.
        visual.setBackground(Color.DARK_GRAY); visual.setBounds(0, 260, 1281, 160); visual.setRoundTopLeft(50); visual.setRoundTopRight(50);
        vinduet.add(visual);

        JPanel fjern_focus = new JPanel();
        fjern_focus.setName("REMOVE_FOCUS");
        fjern_focus.setOpaque(false);
        fjern_focus.setBounds(0, 0, 1281, 260);
        fjern_focus.addMouseListener(vinduet);
        vinduet.add(fjern_focus);

        skjerm_panelet = new JPanel(); //Bakgrunnspanel i øverstehalvdel av programmet.
        skjerm_panelet.setBackground(Color.GRAY); skjerm_panelet.setBounds(0, 0, 1281, 650);
        vinduet.add(skjerm_panelet);

        vinduet.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        vinduet.setLayout(null);
        vinduet.pack(); 
        vinduet.setSize(50, 50);
        vinduet.setLocationRelativeTo(null);
        vinduet.setVisible(true);

        //FOR FULLSCREEN FRA OPPSTART AV PROGRAMMET - GIR NOEN VISUELLE FEIL
        // GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
        // device.setFullScreenWindow(vinduet);

        //------ Oppretter innhold i orbok, finner synth og lager timer: ------//
        generisk<JButton> a = new generisk<>(spill_harmoni);
        connection.put("PLAY", a);
        generisk<JButton> b = new generisk<>(lag_harmoni);
        connection.put("CLEAR", b);
        generisk<JButton> c = new generisk<>(avslutt);
        connection.put("EXIT", c);
        generisk<ButtonRound> d = new generisk<>(oktav_ned);
        connection.put("OKTAV_NED", d);
        generisk<ButtonRound> e = new generisk<>(oktav_opp);
        connection.put("OKTAV_OPP", e);
        generisk<JPanel> f = new generisk<>(harmoni_panel);
        connection.put("WRITE_HARMONY", f);
        generisk<add_or_remove> i = new generisk<>(create_new_panel);
        connection.put("CREATE_PANEL", i);
        generisk<add_or_remove> j = new generisk<>(remove_newest_panel);
        connection.put("REMOVE_PANEL", j);
        generisk<JPanel> r = new generisk<>(chord_panel_over);
        connection.put("PANEL_1", r);
        generisk<JTextField> s = new generisk<>(chord_tekst);
        connection.put("WRITE_HARMONY", s);
        generisk<JSlider> t = new generisk<>(slider);
        connection.put("VOLUME", t);
        generisk<JPanel> u = new generisk<>(fjern_focus);
        connection.put("REMOVE_FOCUS", u);
        
        try { //Intitialliserer Synthesizer, Midichannel og Reciever som brukes til avspilling av noter.
            synth_1 = MidiSystem.getSynthesizer();
            synth_1.open();
            PC_receiver = synth_1.getReceiver();

            synth_2 = MidiSystem.getSynthesizer();
            synth_2.open();
            keyboard_receiver = synth_2.getReceiver();
        }

        catch (Exception ex){
            System.out.println(ex);
        }   

        try { //Initialliserer Sequencer og en Sequence som brukes i METRONOME.
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            //OPPSTÅR FEIL I METRONOMEN NÅR MAN SKRUR DEN PÅ IGJEN ETTER MAN HAR SKRUDD DEN PÅ EN GANG TIDLIGERE!
            sequence = new Sequence(Sequence.PPQ, 1);
            Track track = sequence.createTrack();
            ShortMessage metronome_first_and_last = new ShortMessage(ShortMessage.PROGRAM_CHANGE, 9, 1, 0);
            MidiEvent metronome_event_first = new MidiEvent(metronome_first_and_last, 0); track.add(metronome_event_first);
            for (int k = 0; k < 4; k++){
                ShortMessage metronome_message = new ShortMessage(ShortMessage.NOTE_ON, 9, 33, 50);
                MidiEvent metronome_event = new MidiEvent(metronome_message, k);
                track.add(metronome_event);
            } 
            MidiEvent metronome_event_last = new MidiEvent(metronome_first_and_last, 4); track.add(metronome_event_last);
        } 
        
        catch (InvalidMidiDataException | MidiUnavailableException imde) {

        }

        akkord_timer = new Timer(0, new ActionListener(){ //Må til så det er pauser mellom avspillingen av hver akkord i akkordprogresjonen (i tillegg til at det ikke oppstår problemer med GUI'en).
            private int counter = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    traad_liste_test.get(stop_check).run();
                    traad_liste_test.remove(stop_check);
                }
                catch (IndexOutOfBoundsException k) {
                    
                }  
            }
        });
        
        start_stop_thread = new Thread();
        paused = new AtomicBoolean(false);
        vinduet.set_spill_harmoni(spill_harmoni);
        vinduet.set_paused(paused);
        first_play = 0;
    }

    class test_midi implements Transferable {
        List seq;
        // DataFlavor flavor_test = new DataFlavor(java.util.List.class, "Sequencen som skal legges i clipboardet"); //Tilpass til objektet/klassen som du skal sende over
        DataFlavor flavor_test = DataFlavor.javaFileListFlavor;
        DataFlavor[] supported_flavors = {flavor_test};
        public test_midi(List seq){ //Setter verdien Sequence'en.
            this.seq = seq;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return supported_flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor.equals(flavor_test))
                 return true;
            return false;
        } 

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (flavor.equals(flavor_test)){
                return seq;
            }
            else throw new UnsupportedFlavorException(flavor);
            }
    }

    class tangent extends JPanel {
        String navn; //F.eks. "C", "Eb", etc.
        int tangent_nummer; //F.eks. 1, 2, 3 osv.
        int note_verdi; //F.eks. 60 for middle C, etc.
        int oktav;
        int volume;
        Color original_color;
        ShortMessage MIDI_message;
        public tangent(String navn, int tangent_nummer, int note_verdi, int oktav, int sort_eller_hvit, int volume){
            this.navn = navn; //Istedenfor  this.setName(navn); ????
            this.tangent_nummer = tangent_nummer;
            this.note_verdi = note_verdi;
            this.oktav = oktav;
            this.volume = volume;
            
            if (sort_eller_hvit == 0){ //Hvit tangent
                this.setBackground(Color.WHITE);
                this.setSize(40,170);
                original_color = Color.WHITE;    
                this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            }
            else {
                this.setBackground(Color.BLACK);
                this.setPreferredSize(new Dimension(19, 100));
                original_color = Color.BLACK;
                this.setBorder(BorderFactory.createEtchedBorder());
            }

            MIDI_message = new ShortMessage(); //Setter verdien til selve noten som skal spilles av tangenten.
            
        }

        public void start_spill(){
            try {
                MIDI_message.setMessage(current_sound, 4, note_verdi, volume); //60 = middle C
            }
            catch (InvalidMidiDataException imde){

            }
            this.setBackground(current_color);
            nederste_lag.moveToBack(hvit_panelet);
            PC_receiver.send(MIDI_message, -1);
            
        }

        public void stop_spill(){
            this.setBackground(original_color);
            nederste_lag.moveToBack(hvit_panelet);
        }

        public void connect_spill(ShortMessage the_message){
            if (this.getBackground() == original_color){ //Funket ikke å gjøre dette med switch-statements.
                this.setBackground(current_color);
                nederste_lag.moveToBack(hvit_panelet);
                keyboard_receiver.send(the_message, -1); //Noten spilles av kun her. Bruker en annen receiver en PC keyboardet.
            }
            else {
                this.setBackground(original_color);
                nederste_lag.moveToBack(hvit_panelet);
            }
        }

        @Override
        public String toString(){ //Nyttig for testing
            return ("navn: " + navn + ", tangent_nummer: " + tangent_nummer + ", oktav: " + oktav + "\n");
        }

    }

    class generisk<E> { //Brukes for å holde orden på buttons, labels og paneler i en hashmap.
        E variabel;
        public generisk(E variabel) {
            this.variabel = variabel;
        }
    }

    class custom_scrollable extends BasicScrollBarUI { //SÅ MAN KAN ENDRE UTSEENDE PÅ JScrollPane
    JScrollPane current_scroll;
    Color background, foreground;
    int arc_width, arc_height;
    public custom_scrollable(JScrollPane current_scroll, Color background, Color foreground, int arc_width, int arc_height){
        this.current_scroll = current_scroll;     
        this.background = background;
        this.foreground = foreground;
        this.arc_width = arc_width;
        this.arc_height = arc_height;

        current_scroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                private final Dimension d = new Dimension();
                @Override protected JButton createDecreaseButton(int orientation) {
                  return new JButton() {
                    @Override public Dimension getPreferredSize() {
                        return d;
                    }
                };
            }
                @Override protected JButton createIncreaseButton(int orientation) {
                  return new JButton() {
                    @Override public Dimension getPreferredSize() {
                        return d;
                    }
                };
            }  

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                int THUMB_SIZE = 10;
                int orientation = scrollbar.getOrientation();
                int x = trackBounds.x;
                int y = trackBounds.y;

                int width = orientation == JScrollBar.VERTICAL ? THUMB_SIZE : trackBounds.width;
                width = Math.max(width, THUMB_SIZE);

                int height = orientation == JScrollBar.VERTICAL ? trackBounds.height : THUMB_SIZE;
                height = Math.max(height, THUMB_SIZE);

                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(background);
                g2.fillRect(x, y - 1, width + 7, height + 2); // +2
                g2.dispose();
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(foreground);
                g2.fillRoundRect(r.x + 3, r.y + 3, r.width - 6, r.height - 6, arc_width, arc_height);
                g2.dispose();
            }
        });
    }
}

    class add_or_remove extends PanelRound{
        Integer set_function;
        chord_panel_generator generator;
        public add_or_remove(Integer set_function, chord_panel_generator generator, best_frame main_vinduet){
            this.set_function = set_function;
            this.generator = generator;
            this.addMouseListener(main_vinduet);
        }

        public void activate_button(){
            if (set_function == 1){
                ArrayList<String> blank_list = new ArrayList<String>();
                blank_list.add("");

                generator.generate_chord_panels(blank_list);
            }
            else {
                generator.delete_most_recent_panel();
            }
        }
    }

    class chord_panel_generator {
        best_frame main_vinduet;
        Integer amount_of_panels = 0;
        JPanel this_frame;
        Integer height_of_frame, scroll_sensitivity;
        chord_panel currently_active, start_panel, end_panel, first_available_panel;
        JScrollPane add_and_delete_visual_fix;
        public chord_panel_generator(best_frame main_vinduet, JPanel this_frame){
            this.main_vinduet = main_vinduet;
            this.this_frame = this_frame;
            height_of_frame = 200;
            add_and_delete_visual_fix = (JScrollPane) this_frame.getParent().getParent();
            scroll_sensitivity = 2;

            ArrayList<String> blank_list = new ArrayList<String>();
            blank_list.add("");
            this.generate_chord_panels(blank_list);

            start_panel = (chord_panel) this_frame.getComponent(0);
        }

        public void generate_chord_panels(ArrayList<String> list_with_chords) {
            for (int i = 0; i < list_with_chords.size(); i++){

                //----------------ha metode som finner første ledig akkordpanelet???--------------------//
                //Hvis det finnes et blankt panel fra før av, settes innholdet inn i det.
                //Hvis ikke lages et helt nytt panel i slutten av progresjonen.
                    chord_panel nytt_panel = new chord_panel(list_with_chords.get(i), Integer.toString(amount_of_panels + 1));
                    set_order(nytt_panel);
                    amount_of_panels ++;
                    // set_available_panel(nytt_panel); //Checks if newly added has lower number    
                
                if (amount_of_panels == 9){ //Skal bare gjøres en gang (Viewport skal endres hvis det enten blir mer enn 8 rader)
                    this_frame.getParent().getParent().getParent().setSize(1190, 200);
                    this_frame.getParent().getParent().getParent().revalidate();
                    this.increase_inner_box_size();
                }

                nytt_panel.finish_setting_chord();

                add_and_delete_visual_fix.getViewport().updateUI();
                
                this_frame.add(nytt_panel);
                
                nytt_panel.addMouseListener(main_vinduet);
                nytt_panel.main_name.addKeyListener(main_vinduet);
            }
        }

        public void set_order(chord_panel current_panel){ //Så vi vet hvilket panel som er det siste i rekken, aka. end_panel. 
            if (amount_of_panels > 0){
                current_panel.previous_panel = end_panel;
                end_panel.next_panel = current_panel;
            }
            end_panel = current_panel;
        }

        public void increase_inner_box_size(){
            height_of_frame += 200;
            this_frame.getComponent(0).setBounds(50, 30, 1190, height_of_frame);
            scroll_sensitivity = (int) (scroll_sensitivity + (Math.ceil((double)scroll_sensitivity / 1.5)));
            add_and_delete_visual_fix.getVerticalScrollBar().setUnitIncrement(scroll_sensitivity);
        }

        public void delete_most_recent_panel(){
            if (amount_of_panels > 1){ //Vi kan ikke ha mindre enn et panel.
                if (end_panel == currently_active){ //If end_panel is also that panel that is currently active (removes it from currently active).
                    currently_active = null; 
                }
                amount_of_panels --;
                chord_panel new_end_panel = end_panel.previous_panel;
                new_end_panel.next_panel = null;
                this_frame.remove(end_panel);
                end_panel = new_end_panel;

                if (amount_of_panels == 8){ //Skal bare gjøres en gang (Viewport skal endres hvis det enten blir mindre enn 9 rader)
                    this_frame.getParent().getParent().getParent().setSize(1173, 200);
                    height_of_frame -= 200;
                    this_frame.getComponent(0).setBounds(58, 30, 1173, height_of_frame);
                    
                    this_frame.getParent().getParent().getParent().revalidate();

                    scroll_sensitivity = (int) (scroll_sensitivity - (Math.ceil((double)scroll_sensitivity / 1.5)));
                    add_and_delete_visual_fix.getVerticalScrollBar().setUnitIncrement(scroll_sensitivity);
                }
                add_and_delete_visual_fix.getViewport().updateUI();
            }
        }

        public void clear_all_chord_panels(){
            while (amount_of_panels > 1){
                delete_most_recent_panel();
            }
            start_panel.main_name.setText("");
            start_panel.finish_setting_chord();
        }

        public void set_active_panel(chord_panel set_to_active){
            if (currently_active != null){
                currently_active.circle_panel.setVisible(false);
            }
            currently_active = set_to_active;
            currently_active.circle_panel.setVisible(true);
        }

        public void play_progression(chord_panel denne){  
            Runnable runnable = new Runnable(){
            @Override
            public void run(){  

                chord_panel midlertidig_panel = denne;
                
                while(midlertidig_panel != null){
                    if (paused.get()){
                        synchronized(start_stop_thread){
                            try { //Stopper avspillingen når spill_harmoni-knappen sin tekst er lik "STOPP" - teksten settes da til "PLAY".
                                start_stop_thread.wait();
                                midlertidig_panel = currently_active; //Skal "resette" tilbake til fokusert akkordpanel.
                                } 
                            catch (InterruptedException e) {
                                midlertidig_panel = currently_active; //Skal "resette" tilbake til fokusert akkordpanel.
                                }
                            }
                        }

                    midlertidig_panel.play_this_chord(main_vinduet); //Spiller av akkorden i det aktive panelet.
                    midlertidig_panel = midlertidig_panel.next_panel;           

                    }

                }
            };

            start_stop_thread = new Thread(runnable);
            start_stop_thread.start();
        }
        
        public void generator_start(){
            spill_harmoni.setText("PLAY");
            spill_harmoni.setName("PLAY");
            paused.set(true);

        }

        public void generator_stop(){
            spill_harmoni.setText("STOP");
            spill_harmoni.setName("STOP");
            paused.set(false);
            synchronized(start_stop_thread){
                start_stop_thread.notify();
            }

        }

    }


    class chord_panel extends JPanel {
        JPanel main_panel, number_panel, content_panel, circle_panel; //Main_panel contains name of chord, number_panel contains number up in left-hand corner.
        JLabel panel_name, contents, selected_circle;
        JTextField main_name;
        chord_panel previous_panel, next_panel;
        ArrayList<String> akkord_liste_av_noter; //Variabelen som brukes i string_til_noter()
        Thread panel_traad;
        public chord_panel(String main_name, String panel_name){ //Argument is position of where main_panel is going to be placed?
            this.setName(panel_name);
            this.setBackground(Color.LIGHT_GRAY);
            this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            GridBagLayout bag = new GridBagLayout();
            this.setLayout(bag);
            GridBagConstraints bag_constraints = new GridBagConstraints();
            this.setPreferredSize(new Dimension(144, 144));
            this.akkord_liste_av_noter = new ArrayList<String>();
            
            previous_panel = null;
            next_panel = null;
            
            number_panel = new JPanel();
            number_panel.setBackground(Color.LIGHT_GRAY);
            this.panel_name  = new JLabel(panel_name); 
            this.panel_name.setFont(new FontUIResource(panel_name, 5, 18)); 
            this.panel_name.setForeground(Color.DARK_GRAY);
            number_panel.add(this.panel_name);

            circle_panel = new JPanel();
            circle_panel.setOpaque(false);
            selected_circle = new JLabel("\u26AB");
            selected_circle.setFont(new FontUIResource("\u26AB", 5, 18));
            Color circle_color = new Color(255, 62, 3);
            selected_circle.setForeground(circle_color);
            circle_panel.add(selected_circle);

            main_panel = new JPanel(new GridLayout());
            main_panel.setOpaque(false);
            main_panel.setPreferredSize(new Dimension(142, 30));
            this.main_name  = new JTextField(main_name); 
            this.main_name.setFont(new FontUIResource(main_name, 5, 24));
            this.main_name.setHorizontalAlignment(JTextField.CENTER); 
            this.main_name.setForeground(Color.DARK_GRAY);
            this.main_name.setDisabledTextColor(Color.DARK_GRAY);
            this.main_name.setOpaque(false);
            this.main_name.setBorder(BorderFactory.createEmptyBorder());
            this.main_name.setEditable(false);
            main_panel.add(this.main_name, BorderLayout.CENTER);

            content_panel = new JPanel(new GridLayout());
            content_panel.setBackground(Color.LIGHT_GRAY);
            content_panel.setPreferredSize(new Dimension(90, 20));
            contents = new JLabel(" "); 
            contents.setFont(new FontUIResource(" ", 5, 16)); 
            contents.setForeground(Color.DARK_GRAY);
            contents.setHorizontalAlignment(JLabel.CENTER); 
            content_panel.add(contents, BorderLayout.CENTER);

            vinduet.adjust_text_size_in_panels(this); //Justerer størrelse på main_name og contents avhengig av hvor lange de er i forhold til panelene deres.

            bag_constraints.gridy = 0;
            bag_constraints.gridx = 0;
            bag_constraints.fill = GridBagConstraints.NONE;
            bag_constraints.insets = new Insets(0,0,0,0);
            bag_constraints.anchor = GridBagConstraints.PAGE_START;
            bag_constraints.weighty = 0.9;
            this.add(number_panel, bag_constraints);

            bag_constraints.gridx = 2;
            bag_constraints.insets = new Insets(0,0,0,-90);
            this.add(circle_panel, bag_constraints);
            circle_panel.setVisible(false);

            bag_constraints.gridy = 1;
            bag_constraints.gridx = 0;
            bag_constraints.fill = GridBagConstraints.BOTH;
            bag_constraints.insets = new Insets(0,0,0,0);
            bag_constraints.anchor = GridBagConstraints.CENTER;
            bag_constraints.weighty = 0.7;
            bag_constraints.gridwidth = 3;
            this.add(main_panel, bag_constraints);

            bag_constraints.gridy = 2;
            bag_constraints.gridx = 0;
            bag_constraints.fill = GridBagConstraints.BOTH;
            bag_constraints.gridwidth = 3;
            bag_constraints.insets = new Insets(-5,20,30,20);
            bag_constraints.anchor = GridBagConstraints.CENTER;
            bag_constraints.weighty = 0.9;
            this.add(content_panel, bag_constraints);
        }

        public void prepare_setting_chord(){ //Changes contents in main_panel
            change_colors(Color.CYAN, Color.CYAN, Color.LIGHT_GRAY);
            selected_circle.setForeground(Color.CYAN);
            main_name.setEditable(true);
            main_name.setEnabled(true);
            main_name.setFocusable(true);
            main_name.requestFocusInWindow();
            this.updateUI();
        }

        public void finish_setting_chord(){
            if (vinduet.is_valid_chord(main_name.getText())){ //Så main_name.getText() funker på blanke akkordpanler og de som passerer regex
                akkord_liste_av_noter = vinduet.finn_noter_i_akkord(main_name.getText()); //Lager listen med noter, setter ikke blankt innhold i contents.
                                     
                String content_string = vinduet.turn_list_into_string(this.akkord_liste_av_noter); //Setter innhold i contents
                contents.setText(content_string);
                
                Color occupied_panel_color = new Color(250, 188, 45); //Kan brukes istedenfor oransje

                change_colors(Color.DARK_GRAY, occupied_panel_color, occupied_panel_color); 
                selected_circle.setForeground(Color.RED);
            }

            else { //Tilfeller hvor innholdet i main_name.getText() er "", " " eller en streng som ikke passerer is_valid_chord()
                if (akkord_liste_av_noter.size() > 0){
                    akkord_liste_av_noter.clear(); //Fjerner alt tidligere innhold i lista hvis den settes som blankt.
                }
                // akkord_liste_av_noter.add(""); //Setter blankt innhold i contents.

                contents.setText(""); //Setter innhold i contents

                change_colors(Color.DARK_GRAY, Color.LIGHT_GRAY, Color.LIGHT_GRAY);
                selected_circle.setForeground(Color.RED);

                if (main_name.getText().equals("") || main_name.getText().equals(" ")){

                }
                else {
                    info_2.setText("Invalid chords cannot be added to the progression!");
                }
                main_name.setText(""); //Setter innholdet til å være blankt
            }
            vinduet.adjust_text_size_in_panels(this); //Justerer størrelse på main_name og contents avhengig av hvor lange de er i forhold til panelene deres.

            String new_text = main_name.getText();
            new_text = new_text.replaceAll("\\s", ""); //Removes all empty spaces within the text of main_name.
            main_name.setText(new_text);
            main_name.setEditable(false);
            main_name.setEnabled(false);
        }

        public void change_colors(Color text_color, Color border_color, Color body_color){

            this.setBorder(BorderFactory.createLineBorder(border_color));

            main_name.setForeground(text_color);
            panel_name.setForeground(text_color);
            contents.setForeground(text_color);

            number_panel.setBackground(body_color);
            content_panel.setBackground(body_color);
            this.setBackground(body_color);
        }

        public void change_contents(best_frame test, int oktav){
            Color active_color = new Color(93, 234, 252);
            ArrayList<tangent> change_these_tangents = vinduet.get_tangents(akkord_liste_av_noter);
            for (int i = 0; i < change_these_tangents.size(); i++){
                change_these_tangents.get(i).setBackground(active_color);
                nederste_lag.moveToBack(hvit_panelet);
            }
        }

        public void play_this_chord(best_frame main_vinduet){
            panel_traad = new Thread(new play_chord(main_vinduet, akkord_liste_av_noter, this));

            traad_liste_test.add(panel_traad);

            for (int k = 0; k < traad_liste_test.size(); k++){
                    akkord_timer.start();
                    // try {
                    //     traad_liste_test.get(k).join();
                    // }

                    // catch (InterruptedException ie){

                    // }
                    akkord_timer.setDelay(BPM_to_delay); //2000 funker stabilt - hvilken BPM tillsier det? Er satt til 100BPM by default.
            }
            
        }
    }

    class option_panel extends JPanel {
        int panel_nummer;
        JLabel overskrift, right_arrow, left_arrow;
        JPanel main_component;
        Component[] main_list_of_inner_components;
        JPanel right_arrow_panel, left_arrow_panel;
        option_panel forrige_panel, neste_panel;
        Color text_color;
        public option_panel(int panel_nummer, String name, Color text_color, Color background_color){
            this.setBounds(350, 280, 380, 75);
            this.setBackground(background_color);
            this.setBorder(BorderFactory.createLineBorder(Color.BLUE));
            this.panel_nummer = panel_nummer;
            this.text_color = text_color;
            
            GridBagLayout bag = new GridBagLayout();
            this.setLayout(bag);
            GridBagConstraints bag_constraints = new GridBagConstraints();
            bag_constraints.anchor = GridBagConstraints.PAGE_START;
            bag_constraints.gridy = 0;
            bag_constraints.gridx = 1;
            overskrift = new JLabel(name);
            overskrift.setFont(new FontUIResource(name, 5, 12));
            overskrift.setForeground(text_color);
            this.add(overskrift, bag_constraints);

            left_arrow = new JLabel("   ");
            left_arrow.setForeground(text_color);
            bag_constraints.anchor = GridBagConstraints.LINE_START;
            bag_constraints.gridy = 1;
            bag_constraints.gridx = 0;
            bag_constraints.insets = new Insets(-12,4,4,4);
            bag_constraints.fill = GridBagConstraints.VERTICAL;
            left_arrow_panel = new JPanel();
            left_arrow_panel.setBackground(background_color);
            left_arrow_panel.setPreferredSize(new Dimension(17, 50));
            GridBagLayout inner_bag = new GridBagLayout();
            left_arrow_panel.setLayout(inner_bag);
            GridBagConstraints inner_bag_constraints = new GridBagConstraints();
            inner_bag_constraints.insets = new Insets(-4,0,0,0);
            left_arrow_panel.add(left_arrow, inner_bag_constraints);
            this.add(left_arrow_panel, bag_constraints);

            right_arrow = new JLabel("   ");
            bag_constraints.anchor = GridBagConstraints.LINE_END;
            right_arrow.setForeground(text_color);
            bag_constraints.gridy = 1;
            bag_constraints.gridx = 2;
            right_arrow_panel = new JPanel();
            right_arrow_panel.setBackground(background_color);
            right_arrow_panel.setPreferredSize(new Dimension(17, 50));
            right_arrow_panel.setLayout(inner_bag);
            right_arrow_panel.add(right_arrow, inner_bag_constraints);
            this.add(right_arrow_panel, bag_constraints);
        }

        public void set_main_component(JPanel this_component, int top, int bottom, int preffered_width, int preffered_height){
            main_component = this_component;
            main_list_of_inner_components = main_component.getComponents();
            main_component.setPreferredSize(new Dimension(preffered_width, preffered_height));
            for (int i = 0; i < main_list_of_inner_components.length; i++){
                main_list_of_inner_components[i].setEnabled(false);
            }

            GridBagConstraints bag_constraints = new GridBagConstraints();
            bag_constraints.anchor = GridBagConstraints.CENTER;
            bag_constraints.weighty = 1;
            bag_constraints.weightx = 1;
            bag_constraints.gridy = 1;
            bag_constraints.gridx = 1;
            bag_constraints.fill = GridBagConstraints.HORIZONTAL;
            bag_constraints.fill = GridBagConstraints.VERTICAL;
            bag_constraints.insets = new Insets(top,0,bottom,0); //GENERAL: 4, 6. REGEX: 2, 4.
            this.add(this_component, bag_constraints);
        }

        public void set_forrige(option_panel panelet){
            forrige_panel = panelet;
            left_arrow.setText("\u2BC7");
            left_arrow_panel.setName("LEFT_ARROW");
            left_arrow_panel.addMouseListener(vinduet);
            generisk<JPanel> pil = new generisk<>(left_arrow_panel);
            connection.put("LEFT_ARROW", pil);
        }

        public void set_neste(option_panel panelet){
            neste_panel = panelet;
            right_arrow.setText("\u2BC8");
            right_arrow_panel.setName("RIGHT_ARROW");
            right_arrow_panel.addMouseListener(vinduet);
            generisk<JPanel> pil = new generisk<>(right_arrow_panel);
            connection.put("RIGHT_ARROW", pil);
        }

        public void make_scrollable(JPanel this_component, int top, int bottom, Color background, Color foreground){
            main_component = this_component;
            main_list_of_inner_components = main_component.getComponents();
            for (int i = 0; i < main_list_of_inner_components.length; i++){
                main_list_of_inner_components[i].setEnabled(false);
            }
            
            GridBagConstraints bag_constraints = new GridBagConstraints();
            bag_constraints.anchor = GridBagConstraints.CENTER;
            bag_constraints.weighty = 1;
            bag_constraints.weightx = 1;
            bag_constraints.gridy = 1;
            bag_constraints.gridx = 1;
            bag_constraints.fill = GridBagConstraints.HORIZONTAL;
            bag_constraints.fill = GridBagConstraints.VERTICAL;
            bag_constraints.insets = new Insets(top,0,bottom,0);

            JScrollPane scrollable_panel = new JScrollPane(main_component);
            custom_scrollable new_UI = new custom_scrollable(scrollable_panel, background, foreground, 0, 0); 
            scrollable_panel.setBorder(BorderFactory.createEmptyBorder());
            scrollable_panel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollable_panel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollable_panel.getVerticalScrollBar().setUnitIncrement(2);
            scrollable_panel.getVerticalScrollBar().setOpaque(true);
            scrollable_panel.setPreferredSize(new Dimension(260, 40));
            this.add(scrollable_panel, bag_constraints);
        }

    }

    public class best_frame extends JFrame implements MouseListener, KeyListener { //Subklasse av JFrame som lar oss bruke KeyListener 
                                                                                    //og Mouselistener for å ta input fra tastaturet 
                                                                                    //og datamusen.
        chord_panel_generator generator;
        add_or_remove add_button;
        add_or_remove remove_button;
        JButton spill_harmoni;
        AtomicBoolean paused;
        public best_frame(String navn) {
            this.setTitle(navn); //Setter navn på best_frame/vinduet.

            ArrayList<String> key_list = new ArrayList<>(Arrays.asList("A", "W", "S", "E", "D", "F", "T", "G", "Y", "H", "U", "J"));

            for (int i = 0; i < 6; i++){ //Lager et objekt pr.tangent som skal moderere actions til et JPanel via. et Timer objekt.
                for (int j = 0; j < 12; j++){
                    tangent_animation new_tangent = new tangent_animation(hvit_panelet, 10000);
                    new_tangent.addAction(key_list.get(j), j + 1);
                    if (i == 5){
                        break; //For final octave.
                    }
                }
            }
        
        }

        public void set_generator(chord_panel_generator generator){
            this.generator = generator;
        }

        public void set_spill_harmoni(JButton spill_harmoni){
            this.spill_harmoni = spill_harmoni;
        }

        public void set_paused(AtomicBoolean paused){
            this.paused = paused;
        }

        public void set_add_and_remove(add_or_remove add_button, add_or_remove remove_button){
            this.add_button = add_button;
            this.remove_button = remove_button;
        }

        public List<Object> import_chord_progression(String URL){
            String tune_name = "";
            Integer BPM = 0;
            String key = "";
            ArrayList<String> liste_av_akkorder = new ArrayList<String>();
            //ArrayList<String> liste_av_akkorder = new ArrayList<String>(); //SKAL NOK VÆRE DETTE

            //............... WEBSCRAPER BRUKES HER.....................//
            //EKSEMPEL PÅ RESULTAT - FREM TIL ANDRE VERSET:
            // tune_name = "While My Guitar Gently Weeps Chords by The Beatles";
            // BPM = 57;
            // key = "Am";
            // liste_av_akkorder = new ArrayList<>(Arrays.asList("Am", "Am/G", "F#m7b5", "Fmaj7", "Am", "G", "D", "E", "Am", "Am/G", "F#m7b5", "Fmaj7", "Am", "G", "D", "Dsus4", "D", "Dsus2", "D", "Esus4", "E", "Am", "Am/G", "F#m7b5", "Fmaj7", "Am", "G", "C", "E", "A", "C#m", "F#m", "C#m", "Bm", "E", "Esus4", "E", "A", "C#m", "F#m", "C#m", "Bm", "E", "Esus4", "E"));
            // String midlertig_streng = URL.replaceAll("\\[", "");
            String midlertig_streng = URL.replaceAll(",", "");
            String[] splitted = midlertig_streng.split("\\s+");
            for (int i = 0; i < splitted.length; i++){
                if (is_valid_chord(splitted[i])){
                    liste_av_akkorder.add(splitted[i]);
                }
            }

            
            return Arrays.asList(tune_name, BPM, key, liste_av_akkorder); 
        }

        @Override
        public void mouseClicked(MouseEvent e) { //Ikke tatt i bruk ennå (brukes muligens innen SHOW 
                                                    //sin funksjonalitet, hvis ikke brukes den nok i 
                                                    //mousePressed nedenfor).
        }

        //HVORFOR SJEKKE PÅ NAVN HER? ALLE UNATTT "CLEAR" SKAL KUN ENDRE BAKGRUNNSFARGE TIL CYAN - HVIS IKKE KAN HVER ENESTE KOMPONENT BLI CYAN NÅR DEN TRYKKES PÅ!
        @Override
        public void mousePressed(MouseEvent e) { //Håndeterer tilfeller hvor brukeren
                                                    //trykker på en knapp, panel, osv.
                                                    //via. datamusen.
            Color active_color = new Color(111, 200, 252);
            try {
                String navn = e.getComponent().getName(); //Finner navnet til komponenten (dvs. panelet, knappen, osv.).
                for (String i : connection.keySet()) { //Iterer gjennom hashmappen connection etter en streng/nøkkel som matcher navn.
                    switch (navn) {
                        case "PLAY":
                            spill_harmoni.setBackground(Color.CYAN);
                            break;
                        case "CLEAR":
                            generator.clear_all_chord_panels();
                            lag_harmoni.setBackground(Color.CYAN);
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
                        case "IMPORT":
                            import_harmony.setBackground(Color.CYAN);
                            break;
                        case "EXPORT":
                            export_harmony.setBackground(Color.CYAN);
                            break;
                        case "ADD TO CLIPBOARD":
                            JPanel clipboard_1 = (JPanel) export_part_1_components[0];
                            Component[] clipboard_liste = clipboard_1.getComponents();
                            clicked_on_blue_panel_label(clipboard_liste[0], active_color, true);
                            break;
                        case "DOWNLOAD MIDI-FILE":
                            JPanel midi_fil_1 = (JPanel) export_part_1_components[0];
                            Component[] midi_fil_liste = midi_fil_1.getComponents();
                            clicked_on_blue_panel_label(midi_fil_liste[1], active_color, true);
                            break;
                        case "FILE NAME":
                        case "FILE PATH":
                            JPanel file_path_panel = (JPanel) export_part_2_components[1];
                            Component[] file_path_component = file_path_panel.getComponents();
                            if (navn.equals("FILE NAME")){
                                clicked_on_blue_panel_textfield(file_path_component[1], active_color, true);
                            }
                            else {
                                clicked_on_blue_panel_textfield(file_path_component[0], active_color, true);
                            }
                            break;
                        case "FINISH":
                            JPanel finish_panel = (JPanel) export_part_2_components[2];
                            Component[] finish_component= finish_panel.getComponents();
                            clicked_on_blue_panel_label(finish_component[0], active_color, true);
                            break;
                        case "CANCEL":
                        if (export_part_1.isShowing()){
                            clicked_on_blue_panel_label(export_part_1_components[1], active_color, true);
                        }
                        else if (export_part_2.isShowing()){
                            JPanel file_2 = (JPanel) export_part_2_components[2];
                            Component[] file_2_liste = file_2.getComponents();
                            clicked_on_blue_panel_label(file_2_liste[1], active_color, true);
                        }       
                            break;
                        case "CONNECT":
                            connect_synth.setBackground(Color.CYAN);
                            break;
                        case "CONFIG.":
                            configure_synth.setBackground(Color.CYAN);
                            break;
                        case "METRO.":
                            metronome_on_or_off.setBackground(Color.CYAN);
                            break;
                        case "SETTINGS":
                            see_settings.setBackground(Color.CYAN);
                            break;
                        case "BORDER:":
                            clicked_on_blue_panel_label(general_settings_components[2], active_color, true);
                            clicked_on_blue_panel_label(general_settings_components[3], active_color, true);
                            break;
                        case "BPM:":
                            clicked_on_blue_panel_label(general_settings_components[4], active_color, true);
                            clicked_on_blue_panel_textfield(general_settings_components[5], active_color, true);
                            break;
                        case "MINOR LETTERS:":
                            clicked_on_blue_panel_label(regex_settings_components[0], active_color, true);
                            clicked_on_blue_panel_textfield(regex_settings_components[1], active_color, true);
                            break;
                        case "RIGHT_ARROW":
                            currently_selected_settings_panel.right_arrow_panel.setBackground(active_color);
                            break;
                        case "LEFT_ARROW":
                            currently_selected_settings_panel.left_arrow_panel.setBackground(active_color);
                            break;
                        case "CREATE_PANEL":
                            add_button.setBackground(Color.CYAN);
                            break;
                        case "REMOVE_PANEL":
                            remove_button.setBackground(Color.CYAN);
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
                Color active_color = new Color(47, 73, 245);
                String navn = e.getComponent().getName();
                for (String i : connection.keySet()) {
                    switch (navn) {
                        case "PLAY": //Skal endres til STOPP når en progresjon spilles av.
                        case "STOP":
                            spill_harmoni.setBackground(Color.LIGHT_GRAY);    

                            chord_panel midlertidig_panel; //Setter panelet progresjonen skal begynne å spilles av fra.
                            if (generator.currently_active == null){ //Hvis ingen akkord er fokusert på, spilles progresjonen fra begynnelsen av.
                                midlertidig_panel = generator.start_panel;
                                }   
                            else {
                                midlertidig_panel = generator.currently_active; //Ellers spilles den fra currently_active.
                                }
                            generator.set_active_panel(midlertidig_panel);

                            traad_liste_test = new ArrayList<Thread>();
                            akkord_timer.setDelay(0);
                            if (!paused.get()){ //Når teksten er lik PLAY.  
                                if (first_play == 0){ //Dårlig løsning, men funker. Gjør at aller første avspilling funker uten problemer.
                                    generator.generator_stop();
                                    first_play ++;
                                    }
                                else {
                                    generator.generator_start();
                                    }
                                generator.play_progression(midlertidig_panel);                                    
                                    }

                            else { //Når teksten er lik STOP.
                                generator.generator_stop();
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
                        case "IMPORT":
                            import_harmony.setBackground(Color.LIGHT_GRAY);

                            if (!chord_tekst.isEditable()){
                                disable_octave(); //Gjør slik at tangentene ikke kan trykkes ned mens man skriver inn i tekstfeltet.

                                chord_tekst.setText(""); //Setter innholdet i tekstfeltet til å være blankt hver gang IMPORT trykkes.
                                chord_tekst.setEnabled(true);
                                chord_tekst.setEditable(true);
                                chord_tekst.requestFocusInWindow();
                                Color import_border_color = new Color(111, 200, 252);
                                harmoni_panel.setBorder(BorderFactory.createLineBorder(import_border_color));
                                blaa_panelet.moveToBack(harmoni_panel);
                            }
                            else {
                                if (currently_selected_settings_panel.isShowing()){
                                    disable_option_components(currently_selected_settings_panel); //Skrur av settings panelet hvis det er på.
                                }
                                else if (export_part_1.isShowing()){
                                    disable_option_components(export_part_1);
                                }
                                else if (export_part_2.isShowing()){
                                    disable_option_components(export_part_2);
                                }

                                enable_octave();
                                chord_tekst.setEnabled(false);
                                chord_tekst.setEditable(false);
                                harmoni_panel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                                blaa_panelet.moveToBack(harmoni_panel);
                            }

                            break;
                        case "EXPORT":
                            export_harmony.setBackground(Color.LIGHT_GRAY);
                            if (export_part_1.isShowing()){ //Hvis panelet 
                                disable_option_components(export_part_1);
                            }
                            
                            else {
                                if (currently_selected_settings_panel.isShowing()){ //Hvis GENERAL SETTINGS eller CHORD RECOGNITION PATTERNS er synlige.
                                    disable_option_components(currently_selected_settings_panel);
                                }
                                else if (export_part_2.isShowing()){ //Hvis panelet hvor man velger filnavn og path er synlig.
                                    disable_option_components(export_part_2);
                                    break; //Så enable_option_components(export_part_1) ikke vises igjen.
                                }
                                else if (chord_tekst.isEditable()){ //Hvis man kan skrive inn en liste av akkorder.
                                    enable_octave();
                                    chord_tekst.setEnabled(false);
                                    chord_tekst.setEditable(false);
                                    harmoni_panel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                                    blaa_panelet.moveToBack(harmoni_panel);
                                }

                                enable_option_components(export_part_1);
                            }

                            break;
                        case "ADD TO CLIPBOARD":
                            JPanel clipboard_1 = (JPanel) export_part_1_components[0];
                            Component[] clipboard_liste = clipboard_1.getComponents();
                            clicked_on_blue_panel_label(clipboard_liste[0], active_color, false);

                            Sequence clipboard_sequence = get_midi_sequence();

                            if (clipboard_sequence != null){
                                try {
                                    //FINN UT OM DET GÅR AN Å LAGE ET FIL OBJEKT UTEN Å LAGE EN NY FIL PÅ HARDDISKEN!!!
                                    File test_fil = new File("clipboard_contents.mid");          
                                    
                                    PrintWriter prw= new PrintWriter(test_fil);
                                    prw.println(""); //Sletter det tidligere innholdet i filen, slik at man can kopiere flere progresjoner til clipboardet uten å restarte programmet.

                                    MidiSystem.write(clipboard_sequence, 0, test_fil); //Setter inn midi i midi-filen.
                        
                                    List list_of_file = new ArrayList();
                                    list_of_file.add(test_fil); //Ved å legge Sequence'en i en fil, kan den legges til maskinens clipboard.

                                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                    test_midi test_clipboard = new test_midi(list_of_file); //"Pakker" det vi skal feste til clipboardet i en flavor.
                                    clipboard.setContents(test_clipboard, null); //Fester midi-filen til clipboardet.
                                }

                                catch (IOException ioe) {
                                            
                                }
                            }

                            else {
                                info_2.setText("Cannot export an empty progression.");
                            }

                            disable_option_components(export_part_1); //Skrur av dette panelet og går tilbake til vanlig blått panel.

                            break;
                        case "DOWNLOAD MIDI-FILE":
                            JPanel midi_fil_1 = (JPanel) export_part_1_components[0];
                            Component[] midi_fil_liste = midi_fil_1.getComponents();
                            clicked_on_blue_panel_label(midi_fil_liste[1], active_color, false);
                            disable_option_components(export_part_1); //Turn off export_panel_1
                            enable_option_components(export_part_2); //Turn on export_panel_2

                            JPanel visual_fix_panel = (JPanel) export_part_2_components[1]; //Så man ikke trenger å trykke på tekstboksene to ganger for å aktivere dem (etter man har trykket DOWNLOAD MIDI-FILE).
                            Component[] visual_fix_component = visual_fix_panel.getComponents();
                            JTextField fix_0 = (JTextField) visual_fix_component[0];
                            JTextField fix_1 = (JTextField) visual_fix_component[1];
                            fix_0.setEditable(false);
                            fix_1.setEditable(false);

                            break;
                        case "FILE NAME":
                        case "FILE PATH":
                            JPanel file_path_panel = (JPanel) export_part_2_components[1];
                            Component[] file_path_component = file_path_panel.getComponents();
                            JTextField field_1 = (JTextField) file_path_component[0];
                            JTextField field_2 = (JTextField) file_path_component[1];

                            if (navn.equals("FILE NAME")){
                                clicked_on_blue_panel_textfield(field_2, active_color, false);
                                activate_or_deactivate_textfield(field_2,  "     " + "midi_file", "You cannot write an empty filename!");
                                String midlertig_2 = field_2.getText().replaceAll(" ", ""); //Fjerner spaces fra strengen.
                                midi_file_name = midlertig_2; 
                                field_2.setHorizontalAlignment(JTextField.CENTER);
                            }

                            else {
                                clicked_on_blue_panel_textfield(field_1, active_color, false);
                                activate_or_deactivate_textfield(field_1, "midi_files\\" + "        ", "You cannot write an empty path!");
                                String midlertig_1 = field_1.getText().replaceAll(" ", ""); //Fjerner spaces fra strengen.
                                midi_file_path = midlertig_1;
                                field_1.setHorizontalAlignment(JTextField.CENTER);
                            }
 
                            break;
                        case "FINISH":
                            JPanel finish_panel = (JPanel) export_part_2_components[2];
                            Component[] finish_component = finish_panel.getComponents();
                            clicked_on_blue_panel_label(finish_component[0], active_color, false);
                            
                            Sequence export_sequence = get_midi_sequence();

                            if (export_sequence != null){
                                try { 
                                    File ny_midi_fil = new File(midi_file_path + midi_file_name + ".mid"); //Lager den nye midi-filen som skal skrives til.
                                    
                                    if (ny_midi_fil.createNewFile()) {
                                        info_2.setText("Successfully created the file: " + ny_midi_fil.getName());
                                    } 
                                    else {
                                        info_2.setText("File already exists! The exported file must have a unique name.");
                                        return; //Avslutter metoden og går tilbake til det default blå panelet.
                                    }
                                                        
                                    MidiSystem.write(export_sequence, 0, ny_midi_fil); //Setter inn midi i midi-filen.
                                        
                                    } 
                                    catch (IOException ioe) {
                                        
                                    }
                            }
                            else {
                                info_2.setText("Cannot export an empty progression.");
                            }

                            disable_option_components(export_part_2); //Skrur av dette panelet og går tilbake til vanlig blått panel.
                            break;
                        case "CANCEL": 
                            if (export_part_1.isShowing()){
                                clicked_on_blue_panel_label(export_part_1_components[1], active_color, false);
                                disable_option_components(export_part_1);
                            }
                            else if (export_part_2.isShowing()){
                                JPanel file_2 = (JPanel) export_part_2_components[2];
                                Component[] file_2_liste = file_2.getComponents();
                                clicked_on_blue_panel_label(file_2_liste[1], active_color, true);
                                disable_option_components(export_part_2);
                            }
                            break;
                        case "CONNECT":
                            if (current_midi_controller == null){
                                current_midi_controller = new midi_controller();
                                CONTROLLER_text.setText(current_midi_controller.device_name);
                            }
                            connect_synth.setBackground(Color.LIGHT_GRAY);
                            break;
                        case "CONFIG.":
                            configure_synth.setBackground(Color.LIGHT_GRAY);
                            if (current_midi_controller != null){
                                Runnable main_runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        for(int i = 0; i < 3; i++){ //Ha en liste, når i = 0, finner vi lowest_note, når i = 1, finner vi highest_note  
                                            synchronized(config_thread){
                                                try {
                                                    if (i == 0){ //Lowest note
                                                        current_midi_controller.set_temporary_receiver("Play the lowest C note on your keyboard.", config_thread); 
                                                        config_thread.wait(); //Venter til brukeren trykker inn en tangent.
                                                        info_2.setText("Registering chosen tangent...");
                                                        }
                                                    else if (i == 1){ //Highest note
                                                        current_midi_controller.set_temporary_receiver("Play the highest C note on your keyboard.", config_thread);
                                                        config_thread.wait(); //Venter til brukeren trykker inn en tangent.
                                                        info_2.setText("Registering chosen tangent...");
                                                        }
                                                    else if (i == 2){ //Configrerer pianoet med lowest og highest verdiene.
                                                            current_midi_controller.hoved_receiver.configure(); //configure playable tangents out from new range
                                                            info_2.setText("Piano configuration finished!");
                                                        }
                                                        
                                                        try {
                                                            Thread.sleep(2000); //Så registreringen av en tangent ikke "overlapper" den neste registreringen av en tangent.
                                                        } 
                                                        catch (InterruptedException e) {
                                                            
                                                        }

                                                    } 
                                                catch (InterruptedException e) {
                                                        
                                                    }
                                                }                                                    
                                            }
                                        }
                                    };
                                config_thread = new Thread(main_runnable);
                                config_thread.start();                            
                            }
                            else {
                                info_2.setText("No external keyboard set!");
                            }
                            break;
                        case "METRO.":
                            try {
                                if (sequencer.isRunning()){
                                    metronome_on_or_off.setBackground(Color.LIGHT_GRAY);
                                    sequencer.stop();
                                    METRONOME_text.setText("METRONOME: OFF");
                                }
                                else {
                                    metronome_on_or_off.setBackground(Color.LIGHT_GRAY);
                                    sequencer.setSequence(sequence);
                                    sequencer.setTempoInBPM(current_BPM);
                                    sequencer.start();
                                    METRONOME_text.setText("METRONOME: ON");
                                }
                                
                            }
                            catch (InvalidMidiDataException imde) {

                            }
                            
                            break;
                        case "SETTINGS":
                            see_settings.setBackground(Color.LIGHT_GRAY);
                            if (currently_selected_settings_panel.isShowing()){ //Om komponenten er synlig
                                disable_option_components(currently_selected_settings_panel);
                            }
                            
                            else {
                                if (export_part_1.isShowing()){
                                    disable_option_components(export_part_1);
                                }
                                else if (export_part_2.isShowing()){
                                    disable_option_components(export_part_2);
                                }
                                else if (chord_tekst.isEditable()){ //Hvis man kan skrive inn en liste av akkorder.
                                    enable_octave();
                                    chord_tekst.setEnabled(false);
                                    chord_tekst.setEditable(false);
                                    harmoni_panel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                                    blaa_panelet.moveToBack(harmoni_panel);
                                }
                                enable_option_components(currently_selected_settings_panel); //trengs ikke? Trenger kun å gjøre dem enabeled en om gangen?
                            }
                            
                            break;
                        case "SOUND:":
                            //current_sound
                            break;
                        case "BORDER:": //border_on
                            clicked_on_blue_panel_label(general_settings_components[2], active_color, false);
                            clicked_on_blue_panel_label(general_settings_components[3], active_color, false);
                            JLabel BORDER_value = (JLabel) general_settings_components[3];
                            if (border_on){
                                border_1.setVisible(false);
                                border_2.setVisible(false);
                                border_on = false;
                                BORDER_value.setText("OFF");
                            }
                            else {
                                border_1.setVisible(true);
                                border_2.setVisible(true);
                                border_on = true;
                                BORDER_value.setText("ON");
                            }
                            break;
                        case "BPM:":
                            clicked_on_blue_panel_label(general_settings_components[4], active_color, false);
                            clicked_on_blue_panel_textfield(general_settings_components[5], active_color, false);
                            JLabel BPM_label = (JLabel) general_settings_components[4];
                            JTextField PBM_value = (JTextField) general_settings_components[5];
                            if (PBM_value.isEditable()){
                                enable_octave();
                                try {
                                    current_BPM =  Integer.valueOf(PBM_value.getText());
                                    BPM_to_delay = (60000/current_BPM) * 4;
                                    BPM_text.setText("BPM: " + Integer.toString(current_BPM)); 
                                    PBM_value.setEditable(false);
                                    PBM_value.setEnabled(false);   
                                }
                                catch (NumberFormatException nfe){ //Hvis brukeren skriver inn noe som ikke er et tall
                                    info_2.setText("You can only write numbers in this text field!");
                                    current_BPM =  100;
                                    BPM_to_delay = (60000/current_BPM) * 4;
                                    BPM_text.setText("BPM: " + Integer.toString(100));
                                    PBM_value.setText("100"); 
                                    PBM_value.setEditable(false);
                                    PBM_value.setEnabled(false);
                                }
                            }
                            else {
                                disable_octave(); //Gjør slik at tangentene ikke kan trykkes ned mens man skriver inn i tekstfeltet.

                                PBM_value.setEditable(true);
                                PBM_value.setEnabled(true);
                                PBM_value.requestFocusInWindow();
                            }   
                            break;  
                        case "VISUALS:":
                            //visual_on
                            break;
                        case "COLOR:":
                            //current_color
                            // disable_octave(); //Gjør slik at tangentene ikke kan trykkes ned mens man skriver inn i tekstfeltet.
                            break;
                        case "NEW ROW:":
                            //shift_on
                            break;
                        case "MINOR LETTERS:":
                            clicked_on_blue_panel_label(regex_settings_components[0], active_color, false);
                            clicked_on_blue_panel_textfield(regex_settings_components[1], active_color, false);
                            JLabel Minor_letters_label = (JLabel) regex_settings_components[0];
                            JTextField Minor_letters_value = (JTextField) regex_settings_components[1];
                            if (Minor_letters_value.isEditable()){
                                enable_octave();
                                if (Minor_letters_value.getText().equals("") || Minor_letters_value.getText().equals(" ") || Minor_letters_value.getText().length() > 19){
                                    info_2.setText("Empty string or string too long!");
                                    Minor_letters_value.setText("min");
                                    Minor_letters_value.setEditable(false);
                                    Minor_letters_value.setEnabled(false);
                                }
                                else{
                                    kvalitet_strenger.set(0, Minor_letters_value.getText());
                                    update_regex_pattern();
                                    Minor_letters_value.setEditable(false);
                                    Minor_letters_value.setEnabled(false);
                                }

                            }
                            else {
                                disable_octave(); //Gjør slik at tangentene ikke kan trykkes ned mens man skriver inn i tekstfeltet.
                                Minor_letters_value.setEditable(true);
                                Minor_letters_value.setEnabled(true);
                                Minor_letters_value.requestFocusInWindow();
                            }
                            
                            break;
                        case "RIGHT_ARROW":
                            currently_selected_settings_panel.right_arrow_panel.setBackground(active_color);
                            currently_selected_settings_panel.setVisible(false);
                            disable_option_components(currently_selected_settings_panel);

                            currently_selected_settings_panel = currently_selected_settings_panel.neste_panel;
                            currently_selected_settings_panel.setVisible(true);
                            enable_option_components(currently_selected_settings_panel);

                            break;
                        case "LEFT_ARROW":
                            currently_selected_settings_panel.left_arrow_panel.setBackground(active_color);
                            currently_selected_settings_panel.setVisible(false);
                            disable_option_components(currently_selected_settings_panel);

                            currently_selected_settings_panel = currently_selected_settings_panel.forrige_panel;
                            currently_selected_settings_panel.setVisible(true);
                            enable_option_components(currently_selected_settings_panel);

                            break;
                        case "VOLUME":
                            int new_volume = slider.getValue();
                            for (int liste_index = 0; liste_index < oktav_liste.size(); liste_index++){ //Litt dårlig måte å gjøre dette på, men funker.
                                for (int oktav_index = 0; oktav_index < oktav_liste.get(liste_index).size(); oktav_index++){
                                    oktav_liste.get(liste_index).get(oktav_index).volume = new_volume;
                                }
                            }
                            break;
                        case "CREATE_PANEL":
                            add_button.activate_button();
                            add_button.setBackground(Color.LIGHT_GRAY);
                            break;
                        case "REMOVE_PANEL":
                            if (generator.amount_of_panels == 1){
                                info_2.setText("Cannot remove the first chord panel! The first panel will always remain within the progression.");
                            } 
                            else {
                                remove_button.activate_button();
                            }
                            remove_button.setBackground(Color.LIGHT_GRAY);
                            break;   
                        case "REMOVE_FOCUS":
                        generator.currently_active.circle_panel.setVisible(false);
                        generator.currently_active = null;
                        break; 
                    }
                break;
                }

                if (e.getComponent() instanceof chord_panel){ //Hvis et akkordpanel har blitt klikket på.

                    Component current_component = e.getComponent(); //Finner panelet som ble trykket på.
                    chord_panel change = (chord_panel) current_component;

                    if (generator.currently_active != null){ //Setter innholdet i det nåværende fokuserte akkordpanelet før man setter den nye currently_active.
                        generator.currently_active.finish_setting_chord(); 
                    }
                    
                    if (change != generator.currently_active){ //Gjør akkordpanelet som ble trykket på til det fokuserte akkordpanelet.
                        generator.set_active_panel(change); //Setter change til å være currently_active
                        generator.add_and_delete_visual_fix.getVerticalScrollBar().setValue(change.getY()); //Moves scrollpane to position where entire panel is visible
                    }
                    else { //Akkordpanelet som ble trykket på er det fokuserte akkordpanelet og innholdet dens settes til å kunne bli endret.
                        disable_octave(); //Gjør slik at tangentene ikke kan trykkes ned mens man skriver inn i panelet.

                        generator.add_and_delete_visual_fix.getVerticalScrollBar().setValue(change.getY()); //Moves scrollpane to position where entire panel is visible
                        change.prepare_setting_chord(); //change settes til å kunne bli endret.

                        if (change.akkord_liste_av_noter.size() != 0){ //Hvis innholdet ikke er tomt, får man opp noter i akkordpanelet.
                            change.change_contents(this, oktav_tall);
                        }

                    }

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
                        case "STOP":
                            info_2.setText("Stops playing the current progression.");
                            break;
                        case "CLEAR":
                            info_2.setText("Deletes all chords within the chord progression.");
                            break;
                        case "EXIT":
                            info_2.setText("Closes the program.");
                            break;
                        case "OKTAV_NED":
                            info_2.setText("Sets the octave you are playing on to the one below it.");
                            break;
                        case "OKTAV_OPP":
                            info_2.setText("Sets the octave you are playing on to the one above it.");
                            break;
                        case "WRITE_HARMONY":
                            info_2.setText("If importing a list of chords, examples of valid chords are: G7, Cmaj7, C-/G, Edim7, etc. Press ENTER to finish importation.");
                            break;
                        case "IMPORT":
                            info_2.setText("Imports chords from the written text (eligible format is a list of chords).");
                            break;
                        case "EXPORT":
                            info_2.setText("Exports the current chord progression to the clipboard as MIDI.");
                            break;
                        case "CONNECT":
                            info_2.setText("Connects to external MIDI keyboards that are plugged into one of the USB ports. If multiple keyboards are connected, click to sort through them.");
                            break;
                        case "CONFIG.":
                            info_2.setText("Lets you configure the span of your plugged in keyboard by first playing the lowest C note followed by the highest C note.");
                            break;
                        case "METRO.":
                        info_2.setText("Turns the metronome on or off.");
                            break;
                        case "SETTINGS":
                            info_2.setText("Lets you adjust settings such as the current synth sound, BPM, etc.");
                            break;
                        case "SOUND:":
                            //current_sound
                            break;
                        case "BORDER:":
                            //border_on
                            info_2.setText("Sets wether the green lines around the playable octave are visible or not.");
                            break;
                        case "BPM:":
                            //current_BPM
                            info_2.setText("Lets you adjust the current BPM of the chord progression and metronome.");
                            break;  
                        case "VISUALS:":
                            break;
                        case "COLOR:":
                            //current_color
                            break;
                        case "NEW ROW:":
                            //shift_on
                            break;
                        case "RIGHT_ARROW":
                            info_2.setText("Go to the next page of options.");
                            break;
                        case "LEFT_ARROW":
                            info_2.setText("Go to the next page of options.");
                            break;
                        case "VOLUME":
                            info_2.setText("Lets you adjust the volume level.");
                            break;
                        case "CREATE_PANEL":
                            info_2.setText("Adds a new chord panel with no contents to the progression.");
                            break;
                        case "REMOVE_PANEL":
                            info_2.setText("Removes the currently selected chord panel. If no chord panel is selected, then the most recently added panel is removed.");
                            break;
                    }
                }

                if (e.getComponent() instanceof chord_panel){
                    info_2.setText("The current chord progression. Click on an empty panel to add a chord. Click on non-empty panels to change and display their content.");
                }

            } 
            catch (NullPointerException n) {

            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            info_2.setText("Hover the mouse over a button or panel to get more information about it.");
        }

        @Override
        public void keyTyped(KeyEvent e) {
            
        }

        @Override
        public void keyPressed(KeyEvent e) {
            
        }

        @Override
        public void keyReleased(KeyEvent e) { 
            //----------- Eneste metoden som skal ha in info fra chord_panel_test ------------//
            Integer pressed_key = e.getKeyCode();
            // System.out.println(pressed_key);
            switch (pressed_key){
                case 10: //ENTER
                    enable_octave(); //Tangentene i oktaven blir spillbare igjen.

                    //TEMPORARILY DISABLE PLAYING NOTES WHEN WRITING LETTERS WITH KEYBOARD IS ENABLED?
                    if (generator.currently_active != null){
                        generator.set_active_panel(generator.currently_active); //Sets current panel as currently_active
                        generator.currently_active.finish_setting_chord(); //Sets the text of the currently active panel to whatever is currently typed in the JTextfield.
                        color_tangents_back_to_original(); //Så alle tangentene settes tilbake til sin opprinnlige farge hvis man har kalt på change_content().
                        nederste_lag.moveToBack(hvit_panelet); //Setter de hvite tangente bak de sorte.
                    }

                    if (!chord_tekst.getText().equals("")){ //Hvis akkordpanelets liste ikke er tom, brukes for å hente innholdet i det blå panelet hvor man skriver inn lister av akkorder.

                        harmoni_panel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                        blaa_panelet.moveToBack(harmoni_panel);
                        chord_tekst.setEnabled(false); //Fjerner fokus fra panelet hvor man skriver inn URL og fjerner innholdet der.
                        chord_tekst.setEditable(false);

                        List<Object> import_results = import_chord_progression(chord_tekst.getText());

                        chord_tekst.setText(""); //TEST LINJE - FJERN SENERE
                        
                        ArrayList<String> tune_list = (ArrayList<String>) import_results.get(3);
                        
                        chord_panel from_start = generator.start_panel;
                        int list_index = 0;
                        while (from_start != null){ //Fills available chord panels before generating new ones.
                            if (from_start.main_name.getText().equals("") && (tune_list.size() > 0) ){ //if this current is blank/has no content
                                from_start.main_name.setText(tune_list.get(list_index));
                                from_start.finish_setting_chord();
                                tune_list.remove(list_index);//remove chord from tune_list 
                            }
                            from_start = from_start.next_panel;
                        }

                        generator.generate_chord_panels(tune_list); //Lager akkordpanelene
                       
                    }
                    break;

                case 32: //Når bruker trykker SPACE, blir panelet til høyre for currently_active satt til å være den nye currently_active.
                    try {
                        if (generator.currently_active.next_panel != null){ //Skal kun funke hvis det finnes et fokusert akkordpanel og hvis komponenten som har FocusOwner er et chord_panel.
                            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner().getParent().getParent() instanceof chord_panel){
                                generator.currently_active.finish_setting_chord();
                                generator.set_active_panel(generator.currently_active.next_panel);
                                generator.add_and_delete_visual_fix.getVerticalScrollBar().setValue(generator.currently_active.getY()); //Moves scrollpane to position where entire panel is visible
                                generator.currently_active.prepare_setting_chord(); //Turns on lights and activates textfield
                            }
                        }
                    }

                    catch (NullPointerException npe){

                    }
                    break;
                case 37: //Venstre piltast
                    JTextField test = (JTextField) KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(); 
                    if (!test.isEditable()){
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
                        }
                        break;
                    

                case 39: //Høyre piltast
                JTextField test_2 = (JTextField) KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(); 
                if (!test_2.isEditable()){
                switch (oktav_tall) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        oktav_tall ++;
                        this.flytt_oktav(oktav_tall);
                        break;
                    case 5:
                    default:
                        oktav_tall = 5;
                        info_2.setText("You can't set the octave any higher!");
                        this.flytt_oktav(4);
                        break;
                        }
                    }
                break;
            }
        }

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

                    try {
                        timer.stop();        
                        oktav_liste.get(oktav_tall).get(notene_navn.indexOf(tastatur.get(key))).stop_spill();     
                    }

                    catch (IndexOutOfBoundsException i) {

                    }
                }
	        }

            public void actionPerformed(ActionEvent e){ //Gjøres når tangenten er trykket nede.
                try {
                    oktav_liste.get(oktav_tall).get(notene_navn.indexOf(tastatur.get(e.getActionCommand()))).start_spill();
                }
                    
                catch (IndexOutOfBoundsException i) {

                }
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

        public String turn_list_into_string(ArrayList<String> liste_med_noter){
            String innhold_i_lista = "";
                    for (int j = 0; j < liste_med_noter.size(); j++){
                        innhold_i_lista += liste_med_noter.get(j) + " ";
                        }
            return innhold_i_lista;
        }

        public void adjust_text_size_in_panels(chord_panel panel_in_question){ //Minsker størrelsen til akkordnavnet og akkordinnhold hvis de er lenger enn den horisontale størrelsen til akkordpanelet.
            int main_text_length = panel_in_question.main_name.getText().length(); //Lengden av akkordnavn
            int minimize_by_this_amount = 0;
            if (main_text_length > 10){
                minimize_by_this_amount = (int) ((main_text_length % 10) * 1.6);
            }            
            panel_in_question.main_name.setFont(new FontUIResource(panel_in_question.main_name.getText(), 5, (24 - minimize_by_this_amount) )); //Akkordinnhold

            int contents_text_length = panel_in_question.contents.getText().length();
            minimize_by_this_amount = 0;
            if (contents_text_length > 12){
                minimize_by_this_amount = (int) ((contents_text_length % 12) * 1.05);
            }            
            panel_in_question.contents.setFont(new FontUIResource(panel_in_question.contents.getText(), 5, (16 - minimize_by_this_amount) )); //Akkordinnhold
        }

        public void flytt_oktav(int indeksPlassering){  //Kalles på hver gang OKTAV_OPP eller OKTAV_NED trykkes på av brukeren.
                                                        //Flytter de grønne strekene som viser hvilken oktav brukeren kan spille i.
        try {
            if (generator.currently_active == null || !(generator.currently_active.main_name.isEnabled()) || !(chord_tekst.isEnabled()) ){ //Så man ikke kan bytte oktav mens man skriver inn navnet på en akkord eller en liste med akkorder i tekstpanelene. Går inn i if-sjekken hvis et tekstpanel ikke er aktivt ELLER hvis det kun er et akkordpanel.
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
                else {
                    oktav_tall ++;
                }

            }

            catch (NullPointerException ne){

            }

        }

        public JLabel generate_blue_panel_label(JLabel component_in_question, String component_name, Color background, Color foreground){
            component_in_question.setName(component_name);
            component_in_question.setFont(new FontUIResource(component_name, 5, 12));
            component_in_question.setBackground(background); 
            component_in_question.setForeground(foreground); 
            component_in_question.setFocusable(false); 
            component_in_question.setHorizontalAlignment(JTextField.CENTER);
            component_in_question.addMouseListener(this);
            generisk<JLabel> ny = new generisk<>(component_in_question);
            connection.put(component_name, ny);
            return component_in_question;
        }

        public JTextField generate_blue_panel_field(JTextField component_in_question, String component_name, String component_text, Color background, Color foreground){
            component_in_question.setText(component_text);
            component_in_question.setName(component_name);
            component_in_question.setFont(new FontUIResource(component_name, 5, 12));
            component_in_question.setBackground(background); 
            component_in_question.setForeground(foreground); 
            component_in_question.setFocusable(false); 
            component_in_question.setHorizontalAlignment(JTextField.CENTER);
            component_in_question.setBorder(BorderFactory.createLineBorder(background));
            component_in_question.setDisabledTextColor(foreground);
            component_in_question.addMouseListener(this);
            generisk<JTextField> ny = new generisk<>(component_in_question);
            connection.put(component_name, ny);
            return component_in_question;
        }

        public void clicked_on_blue_panel_label(Component componenten, Color denne_fargen, boolean paa_eller_av){
            JLabel denne_componenten = (JLabel) componenten;
            denne_componenten.setOpaque(paa_eller_av);
            denne_componenten.setBackground(denne_fargen);
            denne_componenten.setBorder(BorderFactory.createLineBorder(denne_fargen));
        }

        public void clicked_on_blue_panel_textfield(Component componenten, Color denne_fargen, boolean paa_eller_av){
            JTextField denne_componenten = (JTextField) componenten;
            denne_componenten.setOpaque(paa_eller_av);
            denne_componenten.setBackground(denne_fargen);
            denne_componenten.setBorder(BorderFactory.createLineBorder(denne_fargen));
        }

        public void activate_or_deactivate_textfield(JTextField field, String default_name, String error_message){
            //FIKS PROBLEM MED JTEXTFIELD
            //SPILL INN EXPORT VIDEO 
            if (field.isEditable()){
                enable_octave();
                if (field.getText().equals("") || field.getText().equals(" ")){   
                    info_2.setText(error_message);
                    field.setText(default_name); 
                    field.setEditable(false);
                    field.setEnabled(false); 
                    field.setFocusable(false);
                }
                else {
                    field.setText(field.getText()); 
                    field.setEditable(false);
                    field.setEnabled(false); 
                    field.setFocusable(false);
                }
            }
            else {
                disable_octave(); //Gjør slik at tangentene ikke kan trykkes ned mens man skriver inn i tekstfeltet.
                field.setEditable(true);
                field.setEnabled(true);
                field.setFocusable(true);
                field.requestFocusInWindow();
            }   
        }

        public void enable_option_components(option_panel componenten){ //Skriv om til at den går gjennom en liste som sendes inn som argument.
            componenten.setVisible(true);
            for (int i = 0; i < componenten.main_list_of_inner_components.length; i++){
                componenten.main_list_of_inner_components[i].setEnabled(true);
            }
        }

        public void disable_option_components(option_panel componenten){
            componenten.setVisible(false);
            for (int i = 0; i < componenten.main_list_of_inner_components.length; i++){
                componenten.main_list_of_inner_components[i].setEnabled(false);
            }
        }

        public Sequence get_midi_sequence(){
            ArrayList<ArrayList<tangent>> convert_these_to_MIDI = new ArrayList<ArrayList<tangent>>();
            chord_panel midlertidig = generator.start_panel;
            while (midlertidig != null){
                convert_these_to_MIDI.add(get_tangents(midlertidig.akkord_liste_av_noter));
                midlertidig = midlertidig.next_panel;
            }
                            
            //Filtypen til filen skal være "MIDI Sequence"
            //Sequence --> Track --> MidiEvent --> ShortMessage

            try {
                Sequence midi_sequence = new Sequence(Sequence.PPQ, 1);
                Track midi_track = midi_sequence.createTrack();
                int tick = 0;

                for (int i = 0; i < convert_these_to_MIDI.size(); i++){
                    ArrayList<tangent> current_chord = convert_these_to_MIDI.get(i);
                    for (int j = 0; j < current_chord.size(); j++){ //Iterer gjennom hver akkord i lista.
                        ShortMessage midi_short_message = new ShortMessage(ShortMessage.NOTE_ON, 1, current_chord.get(j).note_verdi, 64);
                        MidiEvent midi_event = new MidiEvent(midi_short_message, tick); //Nå varer hver akkord fire taktslag.
                        midi_track.add(midi_event);

                        ShortMessage midi_short_message_2 = new ShortMessage(ShortMessage.NOTE_OFF, 1, current_chord.get(j).note_verdi, 0);
                        MidiEvent midi_event_2 = new MidiEvent(midi_short_message_2, tick + 4);
                        midi_track.add(midi_event_2);
                    }
                    tick += 4;

                }

                return midi_sequence; //Returnerer Sequence som skal settes inn i midi-filen.
            }

            catch (InvalidMidiDataException imde){
                    
            }         

            return null; //Skjer kun hvis det oppstår en feil.
        }

        //DENNE OG enable_octave HAR IKKE BLITT TESTET ORGENTLIG - F.EKS. OPPSTÅR FEIL HVIS MAN BYTTER OKTAV MENS MAN SKRIVER INN I ET PANEL.
        //FÅ DISSE TIL Å FUNKE FØR DU PUBLISERER VIDEO.NR 2!!!
        public void disable_octave(){
            sort_panelet.setEnabled(false);
            hvit_panelet.setEnabled(false);
            nederste_lag.moveToBack(hvit_panelet);
        }

        public void enable_octave(){
            sort_panelet.setEnabled(true);
            hvit_panelet.setEnabled(true);
            nederste_lag.moveToBack(hvit_panelet);
        }

        public void update_regex_pattern(){
            //FORELØPIG OPPDATERER KUN min
            regex_pattern = Pattern.compile("[A-G]{1}[b|#]?{1}(([6|7|9]{1}|[11|13]{2}|[+|\\-|m]{1}([6|7|9]{1}|[11|13]{2})?{1}|([" + kvalitet_strenger.get(0) + "]{" + Integer.toString(kvalitet_strenger.get(0).length()) + "}|[aug]{3}|[dim]{3})([6|7|9]{1}|[11|13]{2})?{1}|([maj7|maj9]{4}|[maj11|maj13]{5})?{1})?{1}(sus[2|4])?{1}(add([2|4|5|6|7|9]{1}|[11|13]{2}))?{1}([b|#]{1}5)?{1}([b|#]{1}6)?{1}([b|#]{1}7)?{1}([b|#]{1}9)?{1}([b|#]{1}11)?{1}([b|#]{1}13)?{1})?(/{1}[A-G]{1}[b|#]?{1})?");
        }

        //KAN INFOEN SOM FÅS FRA DENNE METODEN GJØRES BEDRE/MINDRE KOSTBART?
        //ALT DU KAN BRUKE I DENNE METODEN ER oktav_tall OG liste_med_noter !
        public ArrayList<tangent> get_tangents(ArrayList<String> liste_med_noter){ //Henter tangent objekter ut i fra en liste med strenger.
            ArrayList<tangent> resulting_list = new ArrayList<tangent>();
            if (liste_med_noter.size() == 0){
                return resulting_list;
            }
            resulting_list.add(oktav_liste.get(oktav_tall).get(notene_navn.indexOf(liste_med_noter.get(0))));  //Finner første noten i liste_med_noter.

            for (int i = 1; i < liste_med_noter.size(); i++){
                ArrayList<tangent> liste_for_noten = note_og_hashmap.get(liste_med_noter.get(i)); //Finner listen med alle tilfeller av en note.
                for (int j = 0; j < liste_for_noten.size(); j++){ 
                    if (liste_for_noten.get(j).tangent_nummer > resulting_list.get(i-1).tangent_nummer){ //Finner første tilelle i lista med høyere tangent_nummer enn siste element i resulting_list.
                        resulting_list.add(liste_for_noten.get(j));
                        break;
                    }
                }
            }
            return resulting_list;
        }

        //BURDE VÆRE BEST FOR GJENFARGING AV TANGENTER, MEN FUNKER IKKE PGA. INNHOLDET I resulting_list BLIR IKKE RIKTIG PGA. TRÅDENE
        // public void color_tangents_back_to_original(ArrayList<tangent> resulting_list){
        //     for (int i = oktav_tall; i < resulting_list.size(); i++) {//Ser ut som dette fikser de visuelle problemene.
        //         resulting_list.get(i).stop_spill();
        //     }
        // }

        //HVORDAN STOPPE DENNE TIDLIG ETTER SISTE NOTEN HAR BLITT FARGELAGT?
        public void color_tangents_back_to_original() { //Fargelegger alle avspilte tangenter tilbake til sin opprinnlige farge.
            for (int i = oktav_tall; i < oktav_liste.size(); i++) {//Ser ut som dette fikser de visuelle problemene.
                for (int j = 0; j < oktav_liste.get(i).size(); j++){
                    tangent denne_tangenten = oktav_liste.get(i).get(j);
                    if (denne_tangenten.getBackground() != denne_tangenten.original_color){
                        denne_tangenten.stop_spill();
                    }
                }
            }
        }

        public boolean is_valid_chord(String akkord) { //Bruker regex til å sjekke om en streng er en gyldig
                                                        //akkord. Returnerer true hvis dette er tilfellet.
            //Cmaj7add9#5 skal ikke være mulig. Skal egentlig være Cmaj7#5add9. 
            String akkord_uten_spaces = akkord.replaceAll("\\s", "");
            Matcher regex_matcher = regex_pattern.matcher(akkord_uten_spaces);
            boolean regex_match_found = regex_matcher.matches();
            return regex_match_found;
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

        public ArrayList<String> string_til_noter(String akkord, ArrayList<String> liste_av_noter, int root) {
            String char_pattern;    //Brukes for å sjekke om en rekke av chars utgjør en 
                                    //gjennkjennlig streng (f.eks. min, add, b9, osv.).
            if (liste_av_noter.size() == 0){ //Setter inn noter i lista. Er minimum tre noter i lista.
                liste_av_noter = sett_inn_extensions(akkord, liste_av_noter, root);
            }
            //GJØR SLIK AT DEN FUNKER PÅ NYE MØNSTRE SOM BRUKEREN SKRIVER SOM ER LENGRE ENN 3 TENGN!
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
            //------------------------------------------------
            //Ha sjekk for å se om 13th er riktig??
            //Setter 13th et halvt steg ned hvis 13th og b7th er like i liste_av_noter.
            //------------------------------------------------
            if (liste_av_noter.size() == 7 && liste_av_noter.get(3) == liste_av_noter.get(6)){
                liste_av_noter.set(6, notene_navn.get(notene_navn.indexOf(liste_av_noter.get(6)) - 1));
            }
            return liste_av_noter;
        }

        //Dårlig måte å sjekke dette på, men måtte bare finne en måte å gjøre det.
        public boolean sjekk_om_add(int current_index, String strengen){ //Slik at vi ikke legger til for mange noter i lista dersom vi har en akkord med add.
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
            if (kvalitet.equals(kvalitet_strenger.get(0)) || kvalitet.equals("-") || kvalitet.equals("m")){ //Setter 3rd et halvt steg ned.
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
            return liste_av_noter;
        }
        
        public String fjern_chars(String akkord, int lengde_paa_streng){
            //FINN UT HVORFOR: min6 funker og -6 funker ikke 
            StringBuilder string_builder = new StringBuilder(akkord);   //Lager et objekt som er en mutable string, gjør at man kan slette chars fra strengen.
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
            indeks_i_lista = indeks_i_lista % 12; //DETTE FIKSER ALLE PROBLEMENE (BARE TEST LITT MER MED DEN)
            return indeks_i_lista;
        }
    }

public class midi_controller{
    String device_name;
    MidiDevice main_device;
    List<Transmitter> transmitter_list;
    Transmitter trans;
    midi_input_receiver hoved_receiver;
    Thread config_thread;
    Thread threadObject;
    public midi_controller(){
        MidiDevice device;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < infos.length; i++) {
            try {
                device = MidiSystem.getMidiDevice(infos[i]);        
                List<Transmitter> transmitters = device.getTransmitters();
        
                for(int j = 0; j < transmitters.size(); j++) {
                    transmitters.get(j).setReceiver(new midi_input_receiver(device.getDeviceInfo().toString(), this));
                }

                trans = device.getTransmitter();
                trans.setReceiver(new midi_input_receiver(device.getDeviceInfo().toString(), this));
        
                device.open();
                System.out.println(device.getDeviceInfo()+" Was Opened");
                device_name = "CONTROLLER: " + device.getDeviceInfo();
                main_device = device;
                transmitter_list = transmitters;
                hoved_receiver = (midi_input_receiver) trans.getReceiver();
                } 
        
            catch (MidiUnavailableException e) {
        
            }
        }
    }

    public void set_temporary_receiver(String message, Thread traad){
        config_thread = traad;
        find_lowest_or_highest new_temporary = new find_lowest_or_highest(message);
        this.trans.setReceiver(new_temporary);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(true) {
                        if(new_temporary.onsket_tangent == null){ 
                            synchronized(threadObject){
                                try {
                                    threadObject.wait(); //Venter på notify
                                } 
                                catch (InterruptedException e) {
                                
                                }
                            }
                        }
                        hoved_receiver.set_lowest_or_highest(new_temporary.onsket_tangent.getData1());//Condition som skal gjøres når steget er ferdig

                        synchronized(config_thread){
                            config_thread.notify();
                        }

                        break; //Avslutter denne tråden.
                        }
                    }    
                };
            threadObject = new Thread(runnable);
            new_temporary.set_runnable(threadObject);
            threadObject.start();
    }

public class midi_input_receiver implements Receiver {
    String name;
    HashMap<Integer, tangent> data1_and_tangent; //Kobler et tall (som representerer en note) med en tangent i to oktaver.
    midi_controller controller_of_receiver;
    int lowest_note;
    int highest_note;
    public midi_input_receiver(String name, midi_controller controller_of_receiver) {
        this.name = name;
        this.controller_of_receiver = controller_of_receiver;
        lowest_note = 0;
        highest_note = 1;

        data1_and_tangent = new HashMap<Integer, tangent>();
        int tangent_index = 48;
        for (int i = 1; i < 4; i++){
            for (int j = 0; j < oktav_liste.get(i).size(); j++){
                data1_and_tangent.put(tangent_index, oktav_liste.get(i).get(j));
                tangent_index ++;
                }
            } 
        }
            
    public void send(MidiMessage msg, long timeStamp) {
        ShortMessage short_msg = (ShortMessage) msg;
        data1_and_tangent.get(short_msg.getData1()).connect_spill(short_msg); //Noten spilles av.
        }
        
    public void close() {
        
        }

    public void set_lowest_or_highest(int new_value){
        if (lowest_note == 0){
            lowest_note = new_value;
        }
        else {
            highest_note = new_value;
        }
    }

    public void configure(){
        data1_and_tangent = new HashMap<Integer, tangent>(); //Resetter orboken.

        controller_of_receiver.trans.setReceiver(this); //Setter receiver til midi_controlleren tilbake til opprinnelig receiver.

        for (int i = 0; i < oktav_liste.size(); i++){//Setter inn tall og tangenter i ordbok.
            for (int j = 0; j < oktav_liste.get(i).size(); j++){
                data1_and_tangent.put(lowest_note, oktav_liste.get(i).get(j));
                lowest_note ++;
                if (lowest_note == highest_note + 1){
                    return; //Avslutter metoden når antallet tangenter som er spesifisert av rekkevidden er satt inn.
                    }
                }
            }

        }

    }

    public class find_lowest_or_highest implements Receiver {
        ShortMessage onsket_tangent;
        Thread threadObject;
        public find_lowest_or_highest(String message){
            info_2.setText(message);
        }

        public void set_runnable(Thread traad){
            threadObject = traad;
        }

        public void send(MidiMessage msg, long timeStamp) {
            ShortMessage short_msg = (ShortMessage) msg;

            if(short_msg.getData1() != 0){
                onsket_tangent = short_msg;
                synchronized(threadObject){
                    threadObject.notify();
                    }
                }
            }
            
        public void close() {
            
            }

    }

}

class play_chord implements Runnable{ //Kalles på av hver akkord i akkordpanelet.
    best_frame frame;
    ArrayList<String> noter_i_akkord;
    chord_panel panelet;
    public play_chord(best_frame frame, ArrayList<String> noter_i_akkord, chord_panel panelet){
        this.frame = frame;
        this.noter_i_akkord = noter_i_akkord;
        this.panelet = panelet;
    }

    public void run() {
        ArrayList<tangent> liste_med_tangenter = frame.get_tangents(noter_i_akkord); 
        akkordMonitor monitoren = new akkordMonitor(noter_i_akkord, frame);
        Thread gronn = new Thread(new aktivAkkord(noter_i_akkord, frame, monitoren, panelet, liste_med_tangenter));
        gronn.start();  //Denne tråden skal spille av lyden til notenene i noter_i_akkord,
                        //spille av notene visuelt på keyboardet i grønt, og endre fargen
                        //til akkordpanelet med noter_i_akkord til å være CYAN.
        Thread hvit = new Thread(new inaktivAkkord(frame, monitoren, panelet, liste_med_tangenter));
        hvit.start();   //Denne tråden skal endre fargen på de avspilte notene tilbake
                        //til hvitt eller sort, og endre fargen på akkordpanelet med noter_i_akkord
                        //tilbake til oransje.
        if (((Integer.parseInt(panelet.panel_name.getText())) % 8) == 1 ){ //Så y-koordinatet i scrollbaren blir satt til akkordpanelet som blir avspilt.
            frame.generator.add_and_delete_visual_fix.getVerticalScrollBar().setValue(panelet.getY()); 
        }
        if (panelet == frame.generator.end_panel){ //Gjør om skriften til spill_harmoni tilbake til PLAY etter siste akkordpanelet har blitt spilt av. 
            // frame.generator.generator_start();
            // frame.paused.set(true);
            frame.spill_harmoni.setText("PLAY");
            frame.spill_harmoni.setName("PLAY");
            // frame.paused.set(true);
        }
    }
}

class aktivAkkord implements Runnable{ //Metoden som skal først skal fargelegge notene i akkorden grønne.
    best_frame rammen;
    ArrayList<String> lista;
    akkordMonitor monitoren;
    chord_panel panelet;
    ArrayList<tangent> liste_med_tangenter;
    Color active_color;
    public aktivAkkord(ArrayList<String> liste_av_noter, best_frame frame, akkordMonitor monitoren, chord_panel panelet, ArrayList<tangent> liste_med_tangenter){
        rammen = frame;
        lista = liste_av_noter;
        this.monitoren = monitoren;
        this.panelet = panelet;
        this.liste_med_tangenter = liste_med_tangenter;
        active_color = new Color(93, 234, 252);
    }

    public void run(){
        panelet.change_colors(Color.DARK_GRAY, active_color, active_color); //Farger akkordpanelet vi er i blått
        monitoren.fargelegg_gronn(lista, liste_med_tangenter);
    }
}

class inaktivAkkord implements Runnable{ //Metoden som til slutt skal farge de brukte notene til sin opprinnlige farge.
    best_frame rammen;
    akkordMonitor monitoren;
    chord_panel panelet;
    ArrayList<tangent> liste_med_tangenter;
    public inaktivAkkord(best_frame frame, akkordMonitor monitoren, chord_panel panelet, ArrayList<tangent> liste_med_tangenter){
        rammen = frame;
        this.monitoren = monitoren;
        this.panelet = panelet;
        this.liste_med_tangenter = liste_med_tangenter;
    }

    public void run(){
        try {
            Thread.sleep(1000);
            monitoren.fargelegg_hvit_eller_sort();
            panelet.finish_setting_chord();
        }

        catch(InterruptedException ie){

        }
    }
    
}

class akkordMonitor{ //Brukes til avspilling av akkordene i progresjonen.
    Lock laas;
    Condition alle_hvite;
    String[] alle_noter;
    best_frame rammen;
    int lengde;
    public akkordMonitor(ArrayList<String> liste_av_noter, best_frame rammen){
        laas = new ReentrantLock();
        alle_hvite = laas.newCondition();
        alle_noter = new String[liste_av_noter.size()];
        this.rammen = rammen;
        lengde = alle_noter.length;
    }

    public int klart_signal(){
        int storrelse = 0;
            for (int i = 0; i < alle_noter.length; i++){
                if (alle_noter[i] != null){
                    storrelse++;
                }
            }
            return storrelse;
    }

    // public void fargelegg_gronn(ArrayList<String> denne_listen, Color farge, int oktav, ArrayList<String> navn_paa_noter){
    public void fargelegg_gronn(ArrayList<String> denne_listen, ArrayList<tangent> liste_med_tangenter){
        laas.lock(); 
        try{
            for (int i = 0; i < liste_med_tangenter.size(); i++){
                liste_med_tangenter.get(i).start_spill();
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

    // public void fargelegg_hvit_eller_sort(int oktav){
    public void fargelegg_hvit_eller_sort(){
        laas.lock();
        try{
            if (klart_signal() != lengde){
                alle_hvite.await(); //Venter til notene er fargelagt grønne.
            }
            rammen.color_tangents_back_to_original();   //Fargelegger notene til opprinnlig farge igjen, går over alle oktavene
                                                        //siden vi kan spille av en lang akkord over flere oktaver.     
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
