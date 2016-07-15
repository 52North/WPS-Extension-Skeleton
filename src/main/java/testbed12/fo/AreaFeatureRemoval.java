package testbed12.fo;

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.ParameterMetadata;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

@Algorithm(
        version = "0.0.1")
public class AreaFeatureRemoval extends AbstractAnnotatedAlgorithm {

    private static Logger LOGGER = LoggerFactory.getLogger(AreaFeatureRemoval.class);

    private FeatureCollection result;

    private FeatureCollection data;

    private double minSize;

    @ComplexDataOutput(
            identifier = "result", binding = GTVectorDataBinding.class)
    @ParameterMetadata(
            roles = { "http://www.opengis.net/spec/wps/2.0/def/process/description/documentation" },
            hrefs = { "http://52north.github.io/wps-profileregistry/implementing/merge-features-by-attribute.html#result" })
    public FeatureCollection getResult() {
        return result;
    }

    @ComplexDataInput(
            identifier = "data", binding = GTVectorDataBinding.class, minOccurs = 1)
    @ParameterMetadata(
            roles = { "http://www.opengis.net/spec/wps/2.0/def/process/description/documentation" },
            hrefs = { "http://52north.github.io/wps-profileregistry/implementing/merge-features-by-attribute.html#data" })
    public void setData(FeatureCollection data) {
        this.data = data;
    }

    @LiteralDataInput(
            identifier = "minSize", minOccurs = 1)
    @ParameterMetadata(
            roles = { "http://www.opengis.net/spec/wps/2.0/def/process/description/documentation" },
            hrefs = { "http://52north.github.io/wps-profileregistry/implementing/merge-features-by-attribute.html#distance" })
    public void setMinSize(double minSize) {
        this.minSize = minSize;
    }

    @Execute
    public void runScaling() {
        try {
            result = areaRemove(data, minSize);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private FeatureCollection<?, ?> areaRemove(FeatureCollection<?, ?> features,
            double minSize) throws Exception {
        List<SimpleFeature> featuresToRemain = new ArrayList<SimpleFeature>();
        // --get single object in selection to analyse
        for (FeatureIterator<?> iter = features.features(); iter.hasNext();) {
            SimpleFeature f = (SimpleFeature) iter.next();
            Geometry geom = (Geometry) f.getDefaultGeometry();
            Polygon polygon = null;
            if (geom instanceof Polygon) {
                polygon = (Polygon)geom;
            }else if(geom instanceof MultiPolygon) {
                polygon = (Polygon) ((MultiPolygon)geom).getGeometryN(0);
            }
            if (polygon != null && polygon.getArea() >= minSize) {
                featuresToRemain.add(f);
            }
        }
        return new ListFeatureCollection(featuresToRemain.get(0).getFeatureType(), featuresToRemain);
    }

}
