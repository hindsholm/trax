package dk.hindsholm.trax;

import java.nio.file.Path;

import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(name = "trax", description = "Extracts all tracks from a GPX file and save them to individual files " +
        "each named after the date and time of the track.")
public class Trax implements Runnable {

    @Option(names = { "-f", "--file" }, description = "Input GPX file", required = true)
    Path gpxFile;

    @Option(names = { "-o", "--outdir" }, description = "Output directory", required = true)
    Path outDir;

    @Spec
    CommandSpec spec;

    @Inject
    private TrackExtractor trackExtractor;

    @Override
    public void run() {
        validate();
        trackExtractor.setOutDir(outDir);
        trackExtractor.extractTracks(gpxFile);
    }

    private void validate() {
        if (!gpxFile.toFile().canRead() || !gpxFile.toFile().isFile()) {
            throw new ParameterException(spec.commandLine(), String.format("gpxFile '%s' not found", gpxFile.toAbsolutePath()));
        }
        if (!outDir.toFile().isDirectory()) {
            throw new ParameterException(spec.commandLine(), String.format("outdir '%s' is not a directory", outDir.toAbsolutePath()));
        }
    }

}
