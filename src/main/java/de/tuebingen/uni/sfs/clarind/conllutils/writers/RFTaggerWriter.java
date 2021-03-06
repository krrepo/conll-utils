package de.tuebingen.uni.sfs.clarind.conllutils.writers;

import com.google.common.base.Preconditions;
import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.writer.AbstractCorpusWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * A writer for RFTagger training files.
 *
 * @author Daniël de Kok <me@danieldk.eu>
 */
public class RFTaggerWriter extends AbstractCorpusWriter {
    private final boolean cardinalityCheck;

    // Number of morphological features per tag.
    private final Map<String, Integer> featureCardinality;

    private boolean firstSentence;

    private final boolean tuebaFormat;

    private final BufferedWriter writer;

    public RFTaggerWriter(BufferedWriter writer, boolean tuebaFormat, boolean cardinalityCheck) {
        Preconditions.checkNotNull(writer);

        this.cardinalityCheck = cardinalityCheck;
        this.tuebaFormat = tuebaFormat;
        this.writer = writer;

        featureCardinality = new HashMap<>();
        firstSentence = true;
    }

    @Override
    public void write(Sentence sentence) throws IOException {
        Preconditions.checkNotNull(sentence);

        if (firstSentence) {
            firstSentence = false;
        } else {
            writer.write("\n");
        }

        for (Token token : sentence.getTokens()) {
            if (!token.getForm().isPresent()) {
                throw new IllegalArgumentException("Input should contain word form");
            }

            if (!token.getPosTag().isPresent()) {
                throw new IllegalArgumentException("Input should contain fine-grained part-of-speech tags");
            }

            if (!token.getFeatures().isPresent()) {
                throw new IllegalArgumentException("Input should contain (morphology) features");
            }

            List<String> morph = new ArrayList<>();
            morph.add(token.getPosTag().get());

            String morphFeatures = token.getFeatures().get();
            if (tuebaFormat) {
                // TüBA format: one character per morphological feature.

                if (token.getPosTag().get().equals("APPR") && morphFeatures.equals("--")) {
                    morph.add("*");
                }
                else if (!morphFeatures.equals("--")) {
                    for (int i = 0; i < morphFeatures.length(); ++i) {
                        morph.add(Character.toString(morphFeatures.charAt(i)));
                    }
                }
            } else {
                Collections.addAll(morph, StringUtils.split(morphFeatures, '|'));
            }

            if (cardinalityCheck) {
                checkCardinality(morph);
            }

            String rfMorph = StringUtils.join(morph, '.');

            writer.write(String.format("%s\t%s\n", token.getForm().get(), rfMorph));
        }
    }

    private void checkCardinality(List<String> morph) {
        String tag = morph.get(0);
        int thisCard = morph.size() - 1;
        Integer card = featureCardinality.get(tag);
        if (card == null) {
            featureCardinality.put(tag, thisCard);
        } else {
            if (!card.equals(thisCard)) {
                throw new IllegalArgumentException(
                        String.format("Cardinality of '%s' is %d, was %d before", tag, thisCard, card));
            }
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
