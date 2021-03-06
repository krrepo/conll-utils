package de.tuebingen.uni.sfs.clarind.conllutils.cli;

import de.tuebingen.uni.sfs.clarind.conllutils.util.IOUtils;
import de.tuebingen.uni.sfs.clarind.conllutils.writers.PartitioningWriter;
import eu.danieldk.nlp.conllx.reader.CONLLReader;
import eu.danieldk.nlp.conllx.writer.CONLLWriter;
import eu.danieldk.nlp.conllx.writer.CorpusWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Program to create folds of CONLL data.
 */
public class Partition {
    public static void main(String[] args) throws IOException {
        if (args.length < 3)
            usage();

        final int nFolds = Integer.parseInt(args[0]);
        final String prefix = args[1];
        final String suffix = args[2];

        final List<CorpusWriter> writers = new ArrayList<>();
        for (int i = 0; i < nFolds; ++i) {
            try {
                writers.add(new CONLLWriter(new BufferedWriter(new FileWriter(String.format("%s%d%s", prefix, i, suffix)))));
            } catch (IOException e) {
                for (CorpusWriter writer : writers)
                    writer.close();

                System.err.println(e.getMessage());
                System.exit(1);
            }
        }

        try (CONLLReader reader = new CONLLReader(IOUtils.openArgOrStdin(args, 3));
             PartitioningWriter writer = new PartitioningWriter(writers)) {
            writer.write(reader);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void usage() {
        System.err.println("Usage: conll-partition N prefix suffix [CONLL]");
        System.exit(1);
    }
}
