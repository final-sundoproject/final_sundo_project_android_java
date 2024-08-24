package com.example.sundo_project_app.regulatedArea;

import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.locationtech.proj4j.CRSFactory;

public class CoordinateConverter {

    private static final CRSFactory CRS_FACTORY = new CRSFactory();
    private static final CoordinateReferenceSystem EPSG_5179 = CRS_FACTORY.createFromName("EPSG:5179");
    private static final CoordinateReferenceSystem WGS84 = CRS_FACTORY.createFromName("EPSG:4326");
    private static final CoordinateTransform TRANSFORM = new CoordinateTransformFactory().createTransform(EPSG_5179, WGS84);

    public static double[] convertEPSG5179ToWGS84(double minX, double minY, double maxX, double maxY) {
        ProjCoordinate src = new ProjCoordinate();
        ProjCoordinate dest = new ProjCoordinate();

        // Convert minX, minY
        src.x = minX;
        src.y = minY;
        TRANSFORM.transform(src, dest);
        double[] wgs84Min = {dest.x, dest.y};

        // Convert maxX, maxY
        src.x = maxX;
        src.y = maxY;
        TRANSFORM.transform(src, dest);
        double[] wgs84Max = {dest.x, dest.y};

        return new double[] {wgs84Min[0], wgs84Min[1], wgs84Max[0], wgs84Max[1]};
    }
}
