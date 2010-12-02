package org.esa.beam.dataio.placemark;

import org.esa.beam.framework.datamodel.CrsGeoCoding;
import org.esa.beam.framework.datamodel.GcpDescriptor;
import org.esa.beam.framework.datamodel.GeoPos;
import org.esa.beam.framework.datamodel.PinDescriptor;
import org.esa.beam.framework.datamodel.PixelPos;
import org.esa.beam.framework.datamodel.Placemark;
import org.esa.beam.framework.datamodel.PlacemarkDescriptor;
import org.esa.beam.framework.datamodel.PlacemarkSymbol;
import org.esa.beam.util.io.CsvReader;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * User: Marco
 * Date: 03.09.2010
 */
public class PlacemarkIOTest {

    private static final int WRITER_INITIAL_SIZE = 200;
    private static final int NUM_PLACEMARKS = 5;
    private static final Rectangle DATA_BOUNDS = new Rectangle(100, 100);
    private static CrsGeoCoding GEO_CODING;

    @BeforeClass
    public static void beforeClass() throws TransformException, FactoryException {
        AffineTransform i2mTransform = new AffineTransform();
        final int northing = 60;
        final int easting = -5;
        i2mTransform.translate(easting, northing);
        final double scaleX = 0.3;
        final double scaleY = 0.3;
        i2mTransform.scale(scaleX, -scaleY);
        GEO_CODING = new CrsGeoCoding(DefaultGeographicCRS.WGS84, DATA_BOUNDS, i2mTransform);
    }

    @Test
    public void testReadWritePinXmlFile() throws Exception {
        testReadWritePlacemarkXmlFile(PinDescriptor.INSTANCE);
    }

    @Test
    public void testReadWriteGcpXmlFile() throws Exception {
        testReadWritePlacemarkXmlFile(GcpDescriptor.INSTANCE);
    }

    @Test
    public void testReadMinimalPlacemarkTextFile() throws Exception {
        testReadMinimalPlacemarkTextFile(GcpDescriptor.INSTANCE);
        testReadMinimalPlacemarkTextFile(PinDescriptor.INSTANCE);
    }

    @Test
    public void testReadWritePlacemarksTextFileWithAdditionalData() throws Exception {
        StringWriter writer = new StringWriter(WRITER_INITIAL_SIZE);
        PinDescriptor pinDescriptor = PinDescriptor.INSTANCE;
        List<Placemark> expectedPlacemarks = createPlacemarks(pinDescriptor, GEO_CODING, DATA_BOUNDS);
        String[] stdColumnName = {"X", "Y", "Lon", "Lat", "Label"};
        String[] addColumnName = {"A", "B", "C", "D"};
        List<Object[]> valuesList = new ArrayList<Object[]>();
        for (Placemark expectedPlacemark : expectedPlacemarks) {
            Object[] values = new Object[stdColumnName.length + addColumnName.length];
            values[0] = expectedPlacemark.getPixelPos().x;
            values[1] = expectedPlacemark.getPixelPos().y;
            values[2] = expectedPlacemark.getGeoPos().lon;
            values[3] = expectedPlacemark.getGeoPos().lat;
            values[4] = expectedPlacemark.getLabel();
            for (int j = stdColumnName.length; j < values.length; j++) {
                values[j] = Math.random();
            }
            valuesList.add(values);
        }
        PlacemarkIO.writePlacemarksWithAdditionalData(writer, pinDescriptor.getRoleLabel(), "ProductName",
                                                      expectedPlacemarks, valuesList, stdColumnName, addColumnName);
        String output = writer.toString();

        List<Placemark> actualPlacemarks = PlacemarkIO.readPlacemarks(new StringReader(output), GEO_CODING,
                                                                      pinDescriptor);

        testReadStandardResult(expectedPlacemarks, actualPlacemarks, pinDescriptor);
        CsvReader csvReader = new CsvReader(new StringReader(output), new char[]{'\t'}, true, "#");
        String[] header = csvReader.readRecord(); // header line
        assertEquals("Name", header[0]);
        String[] actualStdColNames = Arrays.copyOfRange(header, 1, stdColumnName.length + 1);
        assertArrayEquals(stdColumnName, actualStdColNames);
        assertEquals("Desc", header[stdColumnName.length + 1]);
        String[] actualAddColNames = Arrays.copyOfRange(header, stdColumnName.length + 2, header.length);
        assertArrayEquals(addColumnName, actualAddColNames);
        for (Object[] values : valuesList) {
            String[] record = csvReader.readRecord();
            String[] actualAddValues = Arrays.copyOfRange(record, stdColumnName.length + 2, header.length);
            Object[] expectedValues = Arrays.copyOfRange(values, stdColumnName.length, values.length);
            for (int i = 0; i < expectedValues.length; i++) {
                assertEquals(expectedValues[i].toString(), actualAddValues[i]);
            }
        }
        assertNull(csvReader.readRecord()); // assert all records read
    }

    @Test
    public void testReadPlacemarkTextFileWithDateTime() throws Exception {
        StringWriter writer = new StringWriter(WRITER_INITIAL_SIZE);
        PinDescriptor pinDescriptor = PinDescriptor.INSTANCE;
        List<Placemark> expectedPlacemarks = createPlacemarks(pinDescriptor, GEO_CODING, DATA_BOUNDS);
        String[] stdColumnName = {"X", "Y", "Lon", "Lat", "Label"};
        String[] addColumnName = {"DateTime"};
        List<Object[]> valuesList = new ArrayList<Object[]>();
        for (Placemark expectedPlacemark : expectedPlacemarks) {
            Object[] values = new Object[stdColumnName.length + addColumnName.length];
            values[0] = expectedPlacemark.getPixelPos().x;
            values[1] = expectedPlacemark.getPixelPos().y;
            values[2] = expectedPlacemark.getGeoPos().lon;
            values[3] = expectedPlacemark.getGeoPos().lat;
            values[4] = expectedPlacemark.getLabel();
            long dateInMillis = (long) (Math.random() * Long.MAX_VALUE);
            dateInMillis -= dateInMillis % 1000; // only second accuracy
            values[5] = new Date(dateInMillis);

            valuesList.add(values);
        }
        PlacemarkIO.writePlacemarksWithAdditionalData(writer, pinDescriptor.getRoleLabel(), "ProductName",
                                                      expectedPlacemarks, valuesList, stdColumnName, addColumnName);
        String output = writer.toString();

        List<Placemark> actualPlacemarks = PlacemarkIO.readPlacemarks(new StringReader(output), GEO_CODING,
                                                                      pinDescriptor);

        for (int i = 0; i < actualPlacemarks.size(); i++) {
            final Placemark actualPlacemark = actualPlacemarks.get(i);
            final Object expectedDateTimeAttribute = valuesList.get(i)[5];
            final Object actualDateTimeAttribute = actualPlacemark.getFeature().getAttribute("dateTime");
            assertEquals(expectedDateTimeAttribute, actualDateTimeAttribute);
        }
    }

    private void testReadWritePlacemarkXmlFile(PlacemarkDescriptor descriptorInstance) throws IOException {
        StringWriter writer = new StringWriter(WRITER_INITIAL_SIZE);
        List<Placemark> expectedPlacemarks = createPlacemarks(descriptorInstance, GEO_CODING, DATA_BOUNDS);
        PlacemarkIO.writePlacemarksFile(writer, expectedPlacemarks);
        String output = writer.toString();

        List<Placemark> actualPlacemarks = PlacemarkIO.readPlacemarks(new StringReader(output), GEO_CODING,
                                                                      descriptorInstance);
        testReadXmlResult(expectedPlacemarks, actualPlacemarks, descriptorInstance);
    }

    private void testReadXmlResult(List<Placemark> expectedPlacemarks, List<Placemark> actualPlacemarks,
                                   PlacemarkDescriptor descriptorInstance) {
        assertEquals(expectedPlacemarks.size(), actualPlacemarks.size());
        testReadStandardResult(expectedPlacemarks, actualPlacemarks, descriptorInstance);
        for (int i = 0; i < actualPlacemarks.size(); i++) {
            PlacemarkSymbol expectedSymbol = expectedPlacemarks.get(i).getSymbol();
            PlacemarkSymbol actualSymbol = actualPlacemarks.get(i).getSymbol();
            assertEquals(expectedSymbol.getFillPaint(), actualSymbol.getFillPaint());
            assertEquals(expectedSymbol.getOutlineColor(), actualSymbol.getOutlineColor());
        }
    }

    private void testReadStandardResult(List<Placemark> expectedPlacemarks, List<Placemark> actualPlacemarks,
                                        PlacemarkDescriptor descriptorInstance) {
        for (int i = 0; i < actualPlacemarks.size(); i++) {
            Placemark actualPlacemark = actualPlacemarks.get(i);
            Placemark expectedPlacemark = expectedPlacemarks.get(i);
            assertNotSame(expectedPlacemark, actualPlacemark);
            assertEquals(expectedPlacemark.getName(), actualPlacemark.getName());
            assertEquals(expectedPlacemark.getLabel(), actualPlacemark.getLabel());
            assertEquals(expectedPlacemark.getPixelPos(), actualPlacemark.getPixelPos());
            assertEquals(expectedPlacemark.getGeoPos(), actualPlacemark.getGeoPos());
            assertEquals(expectedPlacemark.getDescription(), actualPlacemark.getDescription());
            PlacemarkDescriptor descriptor = expectedPlacemark.getPlacemarkDescriptor();
            assertEquals(descriptor.getRoleLabel(), descriptorInstance.getRoleLabel());
        }
    }

    private List<Placemark> createPlacemarks(PlacemarkDescriptor descriptor, CrsGeoCoding geoCoding,
                                             Rectangle data_bounds) {
        ArrayList<Placemark> placemarkList = new ArrayList<Placemark>();
        for (int i = 0; i < NUM_PLACEMARKS; i++) {
            PixelPos pixelPos = new PixelPos((float) Math.random() * data_bounds.width,
                                             (float) Math.random() * data_bounds.height);
            GeoPos geoPos = geoCoding.getGeoPos(pixelPos, null);
            Placemark placemark = new Placemark("name_" + i, "label_" + i, "description_" + i,
                                                pixelPos, geoPos, descriptor, geoCoding);
            placemark.getSymbol().setFillPaint(new Color((int) (Math.random() * Integer.MAX_VALUE), true));
            placemark.getSymbol().setOutlineColor(new Color((int) (Math.random() * Integer.MAX_VALUE), true));
            placemarkList.add(placemark);
        }
        return placemarkList;
    }

    private void testReadMinimalPlacemarkTextFile(PlacemarkDescriptor descriptor) throws IOException {
        String output = "Name\tLat\tLon\n";
        output += "name_1\t10.2\t12.4\n";
        output += "name_2\t40.0\t-2.9\n";


        List<Placemark> actualPlacemarks = PlacemarkIO.readPlacemarks(new StringReader(output), GEO_CODING,
                                                                      descriptor);
        assertEquals(2, actualPlacemarks.size());
        assertEquals(actualPlacemarks.get(0).getName(), "name_1");
        assertEquals(actualPlacemarks.get(0).getGeoPos().lat, 10.2f, 1.0e-4);
        assertEquals(actualPlacemarks.get(0).getGeoPos().lon, 12.4f, 1.0e-4);
        assertEquals(actualPlacemarks.get(1).getName(), "name_2");
        assertEquals(actualPlacemarks.get(1).getGeoPos().lat, 40.0f, 1.0e-4);
        assertEquals(actualPlacemarks.get(1).getGeoPos().lon, -2.9f, 1.0e-4);
    }
}