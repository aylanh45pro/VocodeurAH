import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class PauvocoderGUI {
    public static String inputPath;
    public static boolean fileSelected = false;
    public static JFileChooser fileChooser = new JFileChooser();
    public static double[] inputWav;
    public static double freqScale = 1.0;
    public static double[] newPitchWav;
    public static double[] outputWav;
    public static boolean playWav = false;
    public static int currentSample = 0;

    public static void main(String[] args) {
        setup();

        while (true){
            if(StdDraw.isMousePressed()){
                if (StdDraw.mouseX() >= 50 && StdDraw.mouseX() <= 200 &&
                        StdDraw.mouseY() >= 345 && StdDraw.mouseY() <= 405 && !fileSelected){
                    selectFile();
                }

                if (fileSelected){
                    if (StdDraw.mouseX() >= 50 && StdDraw.mouseX() <= 110 &&
                            StdDraw.mouseY() >= 230 && StdDraw.mouseY() <= 260){
                        reduceFreq();
                        newPitchWav = Pauvocoder.resample(inputWav, freqScale);
                        outputWav = newPitchWav;
                    }
                    if (StdDraw.mouseX() >= 220 && StdDraw.mouseX() <= 280 &&
                            StdDraw.mouseY() >= 230 && StdDraw.mouseY() <= 260){
                        addFreq();
                        newPitchWav = Pauvocoder.resample(inputWav, freqScale);
                        outputWav = newPitchWav;
                    }

                    if (StdDraw.mouseX() >= 50 && StdDraw.mouseX() <= 200 &&
                            StdDraw.mouseY() >= 120 && StdDraw.mouseY() <= 180){
                        outputWav = Pauvocoder.vocodeSimple(newPitchWav, 1.0/freqScale);
                        selectOption(1);
                    }
                    if (StdDraw.mouseX() >= 275 && StdDraw.mouseX() <= 425 &&
                            StdDraw.mouseY() >= 120 && StdDraw.mouseY() <= 180){
                        outputWav = Pauvocoder.vocodeSimpleOver(newPitchWav, 1.0/freqScale);
                        selectOption(2);
                    }
                    if (StdDraw.mouseX() >= 500 && StdDraw.mouseX() <= 650 &&
                            StdDraw.mouseY() >= 120 && StdDraw.mouseY() <= 180){
                        outputWav = Pauvocoder.vocodeSimpleOverCross(newPitchWav, 1.0/freqScale);
                        selectOption(3);

                    }


                    if (StdDraw.mouseX() >= 740 && StdDraw.mouseX() <= 860 &&
                            StdDraw.mouseY() >= 30 && StdDraw.mouseY() <= 70){
                        reset();
                    }


                    StdDraw.rectangle(450, 260, 75, 30);
                    StdDraw.text(450, 260, "Play");
                    StdDraw.rectangle(650, 260, 75, 30);
                    if (StdDraw.mouseX() >= 375 && StdDraw.mouseX() <= 525 &&
                            StdDraw.mouseY() >= 230 && StdDraw.mouseY() <= 290){
                        playWav = true;
                    }

                }

                StdDraw.pause(300);
            }

            while (playWav){
                if (StdDraw.isMousePressed() && StdDraw.mouseX() >= 575 && StdDraw.mouseX() <= 725 &&
                        StdDraw.mouseY() >= 230 && StdDraw.mouseY() <= 290){
                    playWav = false;
                }

                if (currentSample < outputWav.length){
                    StdAudio.play(outputWav[currentSample]);
                    currentSample++;
                } else {
                    currentSample = 0;
                    playWav = false;
                }

            }
        }

    }

    /**
     * Augmente la valeur de fréquence
     */
    public static void addFreq(){
        if (freqScale < 2.0){
            freqScale = ((freqScale * 10) + 1) / 10;
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.filledRectangle(165, 260, 50, 25);
            StdDraw.setPenColor(Color.BLACK);
            StdDraw.text(165, 260, ""+freqScale);
            StdDraw.show();

        }

    }

    /**
     * Baisse la valeur de fréquence
     */
    public static void reduceFreq(){
        if (freqScale > 0.5){
            freqScale = ((freqScale * 10) - 1) / 10;
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.filledRectangle(165, 260, 50, 25);
            StdDraw.setPenColor(Color.BLACK);
            StdDraw.text(165, 260, ""+freqScale);
            StdDraw.show();
        }

    }

    /**
     * Affiche la fenettre avec les bons paramètres
     */
    public static void setup() {
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Wav file", "wav"));

        StdDraw.setCanvasSize(900, 500);
        StdDraw.setXscale(0, 900);
        StdDraw.setYscale(0, 500);
        StdDraw.enableDoubleBuffering();
        StdDraw.clear();

        Font font = new Font("Arial", Font.BOLD, 40);
        StdDraw.setFont(font);
        StdDraw.text(450, 460, "Pauvocodeur");

        font = new Font("Arial", Font.PLAIN, 20);
        StdDraw.setFont(font);
        StdDraw.rectangle(125, 375, 75, 30);
        StdDraw.text(125, 375, "Select a file");
        StdDraw.text(250, 375, "File : ");

        StdDraw.show();

    }

    /**
     * Affiche les options dès qu'un fichier est selectionné
     */
    public static void addedSetup() {

        StdDraw.setPenColor(StdDraw.LIGHT_GRAY);
        StdDraw.filledRectangle(125, 375, 75, 30);
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.rectangle(125, 375, 75, 30);
        StdDraw.text(125, 375, "Select a file");

        StdDraw.textLeft(50, 310, "Frequence Scale :");
        StdDraw.square(80, 260, 30);
        StdDraw.text(80, 260, "-");
        StdDraw.square(250, 260, 30);
        StdDraw.text(250, 260, "+");
        StdDraw.rectangle(165, 260, 55, 30);
        StdDraw.text(165, 260, ""+freqScale);

        StdDraw.rectangle(450, 260, 75, 30);
        StdDraw.text(450, 260, "Play");
        StdDraw.rectangle(650, 260, 75, 30);
        StdDraw.text(650, 260, "Pause");

        StdDraw.rectangle(125, 150, 75, 30);
        StdDraw.text(125, 150, "Simple");
        StdDraw.rectangle(350, 150, 75, 30);
        StdDraw.text(350, 150, "Simple Over");
        StdDraw.rectangle(575, 150, 75, 30);
        StdDraw.text(575, 150, "Over Cross");

        StdDraw.rectangle(800, 50, 60, 20);
        StdDraw.text(800, 50, "Reset");

        StdDraw.show();
    }


    public static void selectOption(int option){
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.filledRectangle(450, 200, 450, 100);

        StdDraw.setPenColor(StdDraw.LIGHT_GRAY);
        if (option == 1){
            StdDraw.filledRectangle(125, 150, 75, 30);
        } else if (option == 2){
            StdDraw.filledRectangle(350, 150, 75, 30);
        } else if (option == 3) {
            StdDraw.filledRectangle(575, 150, 75, 30);
        }

        addedSetup();
    }

    /**
     * Permet de sélectionner un fichier, n'accepte que les fichiers en .wad
     */
    public static void selectFile(){
        int response = fileChooser.showOpenDialog(null);

        if (response == JFileChooser.APPROVE_OPTION){
            inputPath = fileChooser.getSelectedFile().getAbsolutePath();
            inputWav = StdAudio.read(inputPath);
            outputWav = inputWav;
            fileSelected = true;

            StdDraw.textLeft(300, 375, fileChooser.getSelectedFile().getName());
            addedSetup();
            StdDraw.show();
        }

    }

    /**
     * Permet de réinitialiser le programme
     */
    public static void reset(){
        inputPath = "";
        fileSelected = false;
        fileChooser = new JFileChooser();
        inputWav = null;
        freqScale = 1.0;
        newPitchWav = null;
        outputWav = null;
        playWav = false;
        currentSample = 0;

        setup();
    }



}
