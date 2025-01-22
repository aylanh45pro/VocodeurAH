/**
 * Pauvocoder - SAE 1&2
 * Mehdi LAFAY - Groupe S1.B2
 * Aylan HADDOUCHI - Groupe S1.B1
 * L'ensemble des méthodes sont exécutables depuis l'interface dans "PauvocoderInterface.java"
 * Compilation : $ javac Pauvocoder.java
 * Exécution : $ java Pauvocoder <fichier_audio.wav> <Coefficient_de_fréquence>
 * Code Vérifié et Corrigé avec SonarQube
 */
import java.util.logging.Logger;


public class Pauvocoder {

    private static final Logger logger = Logger.getLogger(Pauvocoder.class.getName());
    static final int SEQUENCE = StdAudio.SAMPLE_RATE/10;
    static final int OVERLAP = SEQUENCE/5 ;
    static final int SEEK_WINDOW = 3*OVERLAP/4;
    public static void main(String[] args) {
        if (args.length < 2) {
            logger.info("usage: pauvocoder <input.wav> <freqScale>\n");
            System.exit(1);
        }
        String wavInFile = args[0];
        double freqScale = Double.parseDouble(args[1]);
        String outPutFile = wavInFile.split("\\.")[0] + "_" + freqScale + "_";
        double[] inputWav = StdAudio.read(wavInFile);
        int sampleRate = 44100;
        double[] newPitchWav = resample(inputWav, freqScale);
        StdAudio.save(outPutFile + "Resampled.wav", newPitchWav);
        double[] outputWav = vocodeSimple(newPitchWav, 1.0 / freqScale);
        StdAudio.save(outPutFile + "Simple.wav", outputWav);
        outputWav = vocodeSimpleOver(newPitchWav, 1.0 / freqScale);
        StdAudio.save(outPutFile + "SimpleOver.wav", outputWav);
        outputWav = echo(outputWav, 100, 0.7, sampleRate);
        StdAudio.save(outPutFile + "SimpleOverCrossEcho.wav", outputWav);
        logger.info("\nSignal final affiché");
        joue(outputWav);
    }

    /**
     * Resample inputWav with freqScale
     * @param inputWav
     * @param freqScale
     * @return resampled wav
     */
    public static double[] resample(double[] inputWav, double freqScale) {
        double[] newWav;

        if (freqScale > 1) {
            double substractSample = (freqScale - 1) / freqScale;
            newWav = new double[(int)(inputWav.length - (inputWav.length * substractSample))]; // Nouveau tableau, de la taille de toutes les sequences sans les sauté

        } else if (freqScale < 1) {
            double addedSample = (1 - freqScale) / freqScale;
            newWav = new double[(int)(inputWav.length + (inputWav.length * addedSample))]; // Nouveau tableau, de la taille de toutes les sequences sans les sauté

        } else {
            newWav = new double[inputWav.length];
        }

        double inputCounter = 0;
        int newCounter = 0;

        while (inputCounter < inputWav.length && newCounter < newWav.length){
            newWav[newCounter] = inputWav[(int)(inputCounter)];

            newCounter++;
            inputCounter += freqScale;
        }

        return newWav;

    }

    /**
     * Simple dilatation, without any overlapping
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static double[] vocodeSimple(double[] inputWav, double dilatation) {
        int seqJump = (int) (dilatation * SEQUENCE); // Taille du saut (illustration représentative)
        double[] newWav = new double[(int) (inputWav.length / dilatation)]; // Nouveau tableau de la bonne taille

        if (dilatation < 1) {
            return traiterDilatationMoinsUn(inputWav, newWav, seqJump);
        } else if (dilatation > 1) {
            return traiterDilatationPlusUn(inputWav, newWav, seqJump);
        } else {
            return inputWav;
        }
    }

    /**
     * Traite dilatation,
     * @param inputWav
     * @param newWav
     * @param seqJump
     * @return newWaw
     */
    private static double[] traiterDilatationMoinsUn(double[] inputWav, double[] newWav, int seqJump) {
        int seqAjoute = SEQUENCE - seqJump; // Taille de sequence ajouté
        int compteurInput = 0;
        int compteurNew = 0;

        while (compteurInput < inputWav.length && compteurNew < newWav.length) { // Copie la sequence actuelle
            copierSequence(inputWav, newWav, compteurInput, compteurNew);
            compteurInput += SEQUENCE;
            compteurNew += SEQUENCE;
            compteurInput -= seqAjoute;
        }
        return newWav;
    }

    /**
     * Traite dilatation,
     * @param inputWav
     * @param newWav
     * @param seqJump
     * @return newWaw
     */
    private static double[] traiterDilatationPlusUn(double[] inputWav, double[] newWav, int seqJump) {
        int seqSupprime = seqJump - SEQUENCE; // Taille de sequence supprimé
        int compteurInput = 0;
        int compteurNew = 0;

        while (compteurInput < inputWav.length && compteurNew < newWav.length) { // Copie la sequence actuelle
            copierSequence(inputWav, newWav, compteurInput, compteurNew);
            compteurInput += seqSupprime + SEQUENCE;
            compteurNew += SEQUENCE;
        }
        return newWav;
    }

    /**
     * Copie Séquence,
     * @param inputWav
     * @param newWav
     * @param compteurInput
     * @param compteurNew
     */
    private static void copierSequence(double[] inputWav, double[] newWav, int compteurInput, int compteurNew) {
        for (int i = 0; i < SEQUENCE && compteurInput < inputWav.length && compteurNew < newWav.length; i++, compteurInput++, compteurNew++) {
            newWav[compteurNew] = inputWav[compteurInput];
        }
    }

    /**
     * Simple dilatation, with overlapping
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static double[] vocodeSimpleOver(double[] formeOndeEntree, double dilatation) {
        if (dilatation == 1.0) {
            return formeOndeEntree;
        }

        int sautSequence = (int)(dilatation * SEQUENCE);
        int sousSequence = SEQUENCE - OVERLAP;
        double attenuation = 1.0 / OVERLAP;
        double[] nouvelleFormeOnde = new double[(int)(formeOndeEntree.length / dilatation)];

        traiterFormeOnde(formeOndeEntree, nouvelleFormeOnde, sautSequence, sousSequence, attenuation, dilatation < 1);

        return nouvelleFormeOnde;
    }

    /**
     * traiterFormeOnde,
     * @param formeOndeEntree
     * @param nouvelleFormeOnde
     */
    private static void traiterFormeOnde(double[] formeOndeEntree, double[] nouvelleFormeOnde,
                                         int sautSequence, int sousSequence, double attenuation, boolean estCompression) {

        int compteurEntree = 0;
        int compteurSortie = 0;
        int sequenceActuelle = 0;
        boolean premiereSequence = true;
        int differenceSequence = estCompression ? SEQUENCE - sautSequence : sautSequence - SEQUENCE;

        while (compteurEntree < formeOndeEntree.length && compteurSortie < nouvelleFormeOnde.length) {
            // Traitement de la séquence principale
            while (sequenceActuelle < sousSequence && peutContinuerTraitement(formeOndeEntree, nouvelleFormeOnde,
                    compteurEntree, compteurSortie)) {
                traiterSequencePrincipale(formeOndeEntree, nouvelleFormeOnde, compteurEntree,
                        compteurSortie, sequenceActuelle, attenuation);
                sequenceActuelle++;
                compteurSortie++;
                compteurEntree++;
            }

            // Traitement de la zone de chevauchement
            traiterZoneChevauchement(formeOndeEntree, nouvelleFormeOnde, compteurEntree,
                    compteurSortie, attenuation);

            // Ajustement des compteurs
            if (premiereSequence) {
                premiereSequence = false;
                compteurSortie -= OVERLAP;
            }

            compteurEntree += estCompression ? -differenceSequence : differenceSequence;
            sequenceActuelle = 0;
        }
    }

    /**
     * traiterFormeOnde, continue le traitement
     * @param formeOndeEntree
     * @param nouvelleFormeOnde
     * @param compteurEntree
     * @param compteurSortie
     * @return compteurEntree < formeOndeEntree.length && compteurSortie < nouvelleFormeOnde.length
     */
    private static boolean peutContinuerTraitement(double[] formeOndeEntree, double[] nouvelleFormeOnde,
                                                   int compteurEntree, int compteurSortie) {
        return compteurEntree < formeOndeEntree.length && compteurSortie < nouvelleFormeOnde.length;
    }

    /**
     * traiterSéquencePrincipale,
     * @param formeOndeEntree
     * @param nouvelleFormeOnde
     * @param compteurEntree
     * @param compteurSortie
     * @param sequenceActuelle
     * @param attenuation
     */
    private static void traiterSequencePrincipale(double[] formeOndeEntree, double[] nouvelleFormeOnde,
                                                  int compteurEntree, int compteurSortie, int sequenceActuelle, double attenuation) {

        if (sequenceActuelle < OVERLAP) {
            if (nouvelleFormeOnde[compteurSortie] == 0) {
                nouvelleFormeOnde[compteurSortie] = formeOndeEntree[compteurEntree] *
                        (attenuation * sequenceActuelle);
            } else {
                nouvelleFormeOnde[compteurSortie] += formeOndeEntree[compteurEntree] *
                        (attenuation * sequenceActuelle);
            }
        } else {
            nouvelleFormeOnde[compteurSortie] = formeOndeEntree[compteurEntree];
        }
    }

    /**
     * traiterZoneChevauchement,
     * @param formeOndeEntree
     * @param nouvelleFormeOnde
     * @param compteurEntree
     * @param compteurSortie
     * @param sequenceActuelle
     * @param attenuation
     */
    private static void traiterZoneChevauchement(double[] formeOndeEntree, double[] nouvelleFormeOnde,
                                                 int compteurEntree, int compteurSortie, double attenuation) {

        for (int i = OVERLAP; i > 0 && peutContinuerTraitement(formeOndeEntree, nouvelleFormeOnde,
                compteurEntree, compteurSortie); i--, compteurSortie++, compteurEntree++) {
            nouvelleFormeOnde[compteurSortie] = formeOndeEntree[compteurEntree] * (attenuation * i);
        }
    }

    /**
     * Simple dilatation, with overlapping and maximum cross correlation search
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static double[] vocodeSimpleOverCross(double[] inputWav, double dilatation) {
        int sautSequence = (int)(dilatation * SEQUENCE);
        int sousSequence = SEQUENCE - OVERLAP;
        double attenuation = 1.0 / OVERLAP;
        double[] nouveauWav = new double[(int)(inputWav.length / dilatation)];

        if (dilatation < 1) {
            gererDilatationMoinsUnOverCross(inputWav, nouveauWav, sautSequence, sousSequence, attenuation);
        } else if (dilatation > 1) {
            gererDilatationPlusUnOverCross(inputWav, nouveauWav, sautSequence, sousSequence, attenuation);
        }
        return nouveauWav;
    }

    /**
     * gererDilatationMoinsUnOverCross,
     * @param inputWav
     * @param nouveauWav
     * @param attenuation
     * @param sautSequence
     * @param sousSequence
     */
    private static void gererDilatationMoinsUnOverCross(double[] inputWav, double[] nouveauWav, int sautSequence, int sousSequence, double attenuation) {
        int compteurInput = 0;
        int compteurNouveau = 0;
        int sequenceActuelle = 0;
        boolean premiereSequence = true;
        int sequenceAjoutee = SEQUENCE - sautSequence;

        while (compteurInput < inputWav.length && compteurNouveau < nouveauWav.length) {
            traiterSequenceOverCross(inputWav, nouveauWav, sousSequence, attenuation, compteurInput, compteurNouveau, sequenceActuelle);
            int meilleurDecalage = trouverMeilleurDecalageOverCross(inputWav, compteurInput);
            compteurInput += meilleurDecalage;
            appliquerAttenuationOverCross(inputWav, nouveauWav, compteurInput, compteurNouveau, attenuation, OVERLAP);

            if (premiereSequence) {
                premiereSequence = false;
                compteurNouveau -= OVERLAP;
            }
            compteurInput -= sequenceAjoutee;
            sequenceActuelle = 0;
        }
    }

    /**
     * gererDilatationPlusUnOverCross,
     * @param inputWav
     * @param nouveauWav
     * @param attenuation
     * @param sautSequence
     * @param sousSequence
     */
    private static void gererDilatationPlusUnOverCross(double[] inputWav, double[] nouveauWav, int sautSequence, int sousSequence, double attenuation) {
        int compteurInput = 0;
        int compteurNouveau = 0;
        int sequenceActuelle = 0;
        boolean premiereSequence = true;
        int sequenceEnlevee = sautSequence - SEQUENCE;

        while (compteurInput < inputWav.length && compteurNouveau < nouveauWav.length) {
            traiterSequenceOverCross(inputWav, nouveauWav, sousSequence, attenuation, compteurInput, compteurNouveau, sequenceActuelle);
            int meilleurDecalage = trouverMeilleurDecalageOverCross(inputWav, compteurInput);
            compteurInput += meilleurDecalage;
            appliquerAttenuationOverCross(inputWav, nouveauWav, compteurInput, compteurNouveau, attenuation, OVERLAP);

            if (premiereSequence) {
                premiereSequence = false;
                compteurNouveau -= OVERLAP;
            }
            compteurInput += sequenceEnlevee;
            sequenceActuelle = 0;
        }
    }

    /**
     * traiterSequenceOverCross,
     * @param inputWav
     * @param nouveauWav
     * @param sousSequence
     * @param attenuation
     * @param compteurInput
     * @param compteurNouveau
     * @param sequenceActuelle
     */
    private static void traiterSequenceOverCross(double[] inputWav, double[] nouveauWav, int sousSequence, double attenuation, int compteurInput, int compteurNouveau, int sequenceActuelle) {
        while (sequenceActuelle < sousSequence && compteurInput < inputWav.length && compteurNouveau < nouveauWav.length) {
            if (sequenceActuelle < OVERLAP) {
                nouveauWav[compteurNouveau] += inputWav[compteurInput] * (attenuation * sequenceActuelle);
            } else {
                nouveauWav[compteurNouveau] = inputWav[compteurInput];
            }
            sequenceActuelle++;
            compteurNouveau++;
            compteurInput++;
        }
    }

    /**
     * trouverMeilleurDecalageOverCross,
     * @param inputWav
     * @param compteurInput
     * @return  meilleurDecalage
     */
    private static int trouverMeilleurDecalageOverCross(double[] inputWav, int compteurInput) {
        double meilleureCorrelation = -1;
        int meilleurDecalage = 0;

        for (int decalage = -SEEK_WINDOW; decalage <= SEEK_WINDOW; decalage++) {
            double correlation = 0.0;
            for (int i = 0; i < SEQUENCE && compteurInput + i < inputWav.length && compteurInput + i + decalage < inputWav.length; i++) {
                correlation += inputWav[compteurInput + i] * inputWav[compteurInput + i + decalage];
            }
            if (correlation > meilleureCorrelation) {
                meilleureCorrelation = correlation;
                meilleurDecalage = decalage;
            }
        }
        return meilleurDecalage;
    }

    /**
     * appliquerAttenuationOverCross,
     * @param inputWav
     * @param nouveauWav
     * @param compteurNouveau
     * @param compteurInput
     * @param attenuation
     * @param overlap
     */
    private static void appliquerAttenuationOverCross(double[] inputWav, double[] nouveauWav, int compteurInput, int compteurNouveau, double attenuation, int overlap) {
        for (int i = overlap; i > 0 && compteurInput < inputWav.length && compteurNouveau < nouveauWav.length; i--, compteurNouveau++, compteurInput++) {
            nouveauWav[compteurNouveau] = inputWav[compteurInput] * (attenuation * i);
        }
    }

    /**
     * Play the input audio
     * @param input
     * @return input played
     */
    public static void joue(double[] input) {
        // Initialiser la fenêtre d'affichage
        StdDraw.setCanvasSize(800, 400);  // Taille de la fenêtre
        StdDraw.setXscale(0, 800);  // Place de l'axe X
        StdDraw.setYscale(-1.2, 1.2);  // Place de l'axe Y
        StdDraw.clear();  // Effacer l'écran avant de dessiner la nouvelle onde
        StdDraw.setPenColor(StdDraw.LIGHT_GRAY);  // Définir la couleur pour l'axe X
        StdDraw.setPenRadius(0.001);  // Définir l'épaisseur du stylo
        StdDraw.line(0, 0, 800, 0);  // Trace une ligne horizontale représentant l'axe X
        displayWaveform(input); //Affiche le fichier input
        StdAudio.play(input); //Joue le fichier input
    }

    /**
     * Add an echo to the wav
     * @param wav
     * @param delay in msec
     * @param gain
     * @param sampleRate --> évite que le son sature
     * @return wav with echo
     */
    public static double[] echo(double[] input, double delayMs, double gain, int sampleRate) {
        if (gain < 0 || gain > 1) {
            throw new IllegalArgumentException("Gain entre 0 et 1");
        }
        int delaySamples = (int) Math.round(delayMs * sampleRate / 1000.0);
        double[] echoedWav = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            if (i - delaySamples >= 0) {
                echoedWav[i] = input[i] + input[i - delaySamples] * gain;
            } else {
                echoedWav[i] = input[i];
            }
        }
        for (int i = 0; i < echoedWav.length; i++) {
            if (echoedWav[i] > 1) {
                echoedWav[i] = 1;
            } else if (echoedWav[i] < -1) {
                echoedWav[i] = -1;
            }
        }
        return echoedWav;
    }

    /**
     * Open a canvas that shows the wave of the input
     * @param wav
     * @return canvas with wav's wave
     */
    public static void displayWaveform(double[] wav) {
        // Préparation de la fenêtre d'affichage
        StdDraw.setCanvasSize(800, 400);
        StdDraw.setXscale(0, 800);
        StdDraw.setYscale(-1.2, 1.2);
        StdDraw.clear();
        StdDraw.setPenColor(StdDraw.LIGHT_GRAY);
        StdDraw.setPenRadius(0.001);
        StdDraw.line(0, 0, 800, 0);
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.setPenRadius(0.003);

        int step = Math.max(1, wav.length / 800); // Ajuste l'échelle pour la fenêtre
        double xScale = 800.0 / ((double) wav.length / step);

        for (int i = step; i < wav.length; i += step) {
            double x1 = (((double) i - step) / step) * xScale;
            double x2 = ((double) i / step) * xScale;
            StdDraw.line(x1, wav[i - step], x2, wav[i]);  // Trace la ligne entre les échantillons
        }
        StdDraw.show();
    }
}



