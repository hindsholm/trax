package dk.hindsholm.trax;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import jakarta.enterprise.context.Dependent;

@Dependent
public class TrackExtractor {

    Path outDir;

    void setOutDir(Path outDir) {
        this.outDir = outDir;
    }

    void extractTracks(Path gpxFile) {
        try {
            GPX.read(gpxFile).tracks()
                .forEach(this::writeTrack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeTrack(Track track) {
        String fileName;
        try {
            fileName = getFileName(track);
        } catch (NoSuchElementException e) {
            System.out.printf("Could not extract filename from track '%s': %s\n", track, e);
            return;
        }
        Path outFile = outDir.resolve(fileName);
        System.out.printf("Writing '%s' to file '%s'\n", track, outFile.toAbsolutePath());
        GPX gpx = GPX.builder()
            .addTrack(sortTrack(track))
            .build();
        try {
            GPX.write(gpx, outFile);
        } catch (IOException e) {
            System.out.printf("Could not write file '%s': %s\n", outFile.toAbsolutePath(), e);
        }
    }

    private String getFileName(Track track) {
        LocalDateTime date = getTrackDateTime(track);
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".gpx";
    }

    private LocalDateTime getTrackDateTime(Track track) {
        Instant time = track.segments()
            .flatMap(TrackSegment::points)
            .map(WayPoint::getTime)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted()
            .findFirst()
            .orElseThrow();
        return LocalDateTime.ofInstant(time, ZoneId.systemDefault());
    }

    private Track sortTrack(Track track) {
        return track.toBuilder()
            .map(segment -> segment.toBuilder()
                .points(sortWayPoints(segment))
            .build())
        .build();
    }

    private List<WayPoint> sortWayPoints(TrackSegment segment) {
        return segment.points()
            .sorted((wpt1, wpt2) -> compareWaypoints(wpt1, wpt2))
            .toList();
    }

    private int compareWaypoints(WayPoint wpt1, WayPoint wpt2) {
        return wpt1.getTime().orElse(Instant.EPOCH)
            .compareTo(wpt2.getTime().orElse(Instant.EPOCH));
    }

}