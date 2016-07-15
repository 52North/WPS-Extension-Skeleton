package testbed12.fo;

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.ParameterMetadata;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import testbed12.fo.util.GeometryHelper;

@Algorithm(version="0.0.1")
public class AreaScalingSimple extends AbstractAnnotatedAlgorithm {

    private static Logger LOGGER = LoggerFactory.getLogger(AreaScalingSimple.class);

    private FeatureCollection result;

    private FeatureCollection data;

    private double minSize;

    private String classField;
   

    @ComplexDataOutput(
            identifier = "result", binding = GTVectorDataBinding.class)
    @ParameterMetadata(roles={"http://www.opengis.net/spec/wps/2.0/def/process/description/documentation"}, hrefs={"http://52north.github.io/wps-profileregistry/implementing/merge-features-by-attribute.html#result"})
    public FeatureCollection getResult() {
        return result;
    }

    @ComplexDataInput(
            identifier = "data", binding = GTVectorDataBinding.class, minOccurs = 1)
    @ParameterMetadata(roles={"http://www.opengis.net/spec/wps/2.0/def/process/description/documentation"}, hrefs={"http://52north.github.io/wps-profileregistry/implementing/merge-features-by-attribute.html#data"})
    public void setData(FeatureCollection data) {
        this.data = data;
    }

    @LiteralDataInput(
            identifier = "minSize", minOccurs = 1)
    @ParameterMetadata(roles={"http://www.opengis.net/spec/wps/2.0/def/process/description/documentation"}, hrefs={"http://52north.github.io/wps-profileregistry/implementing/merge-features-by-attribute.html#distance"})
    public void setMinSize(double minSize) {
        this.minSize = minSize;
    }

    @Execute
    public void runScaling(){
        try {
            result = areaScale(data, minSize);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
    
    private FeatureCollection<?, ?> areaScale(FeatureCollection<?, ?> features,
            double minSize) throws Exception {

        List<SimpleFeature> newFeatures = new ArrayList<>();

        // --------------------------
        int count = 0;
        // --get single object in selection to analyse
        for (FeatureIterator<?> iter = features.features(); iter.hasNext();) {
            count++;
            SimpleFeature f = (SimpleFeature) iter.next();

            // create and initialize the builder
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(f.getFeatureType());
            builder.init(f);

            SimpleFeature fnew = builder.buildFeature(f.getID());
            Geometry geom = (Geometry) f.getDefaultGeometry(); // = erste
                                                               // Geometrie
            Polygon poly = null;
            if (geom instanceof Polygon) {
                poly = (Polygon) geom; // = erste Geometrie
                if (poly.getArea() < minSize) {
                    poly = (Polygon) GeometryHelper.skaleSize(poly, minSize);
                    fnew.setDefaultGeometry(poly);
                }
            }else if (geom instanceof MultiPolygon) {
                MultiPolygon multiPolygon = (MultiPolygon)geom;
                if(multiPolygon.getNumGeometries() == 1){
                    try {
                        poly = (Polygon) multiPolygon.getGeometryN(0); // = erste Geometrie 
                    } catch (Exception e) {
                        LOGGER.info("Multipolygon did contain one geometry, but it was not a polygon.");
                    }                 
                }
                if (poly != null && poly.getArea() < minSize) {
                    poly = (Polygon) GeometryHelper.skaleSize(poly, minSize);
                    fnew.setDefaultGeometry(poly);
                }
            } else {
                LOGGER.info("no polygon selected");
            }
            newFeatures.add(fnew);
        } // end loop over item selection
        return new ListFeatureCollection((SimpleFeatureType) features.getSchema(), newFeatures);
    }

}
