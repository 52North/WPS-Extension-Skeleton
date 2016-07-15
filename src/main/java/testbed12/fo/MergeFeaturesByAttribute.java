package testbed12.fo;

import java.util.Iterator;
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
import org.n52.wps.algorithm.annotation.AlgorithmMetadata;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.quadtree.Quadtree;

@Algorithm(
        version = "0.0.1")
@AlgorithmMetadata(
        roles = { "http://www.opengis.net/spec/wps/2.0/def/process-profile/concept", "http://www.opengis.net/spec/wps/2.0/def/process/description/documentation" },
        hrefs = { "http://52north.github.io/wps-profileregistry/concept/generalization.html", "http://52north.github.io/wps-profileregistry/implementing/merge-features-by-attribute.html" })
public class MergeFeaturesByAttribute extends AbstractAnnotatedAlgorithm {

    private static Logger LOGGER = LoggerFactory.getLogger(MergeFeaturesByAttribute.class);

    private FeatureCollection result;

    private FeatureCollection data;

    private double distance;

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
            identifier = "distance", minOccurs = 1)
    @ParameterMetadata(roles={"http://www.opengis.net/spec/wps/2.0/def/process/description/documentation"}, hrefs={"http://52north.github.io/wps-profileregistry/implementing/merge-features-by-attribute.html#distance"})
    public void setDistance(double distance) {
        this.distance = distance;
    }

    @LiteralDataInput(
            identifier = "classfield", minOccurs = 1)
    @ParameterMetadata(roles={"http://www.opengis.net/spec/wps/2.0/def/process/description/documentation"}, hrefs={"http://52north.github.io/wps-profileregistry/implementing/merge-features-by-attribute.html#classfield"})
    public void setClassField(String classField) {
        this.classField = classField;
    }

    @Execute
    public void runMerge() {
        try {
            result = reclass(data, classField, distance);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private FeatureCollection<?, ?> reclass(FeatureCollection<?, ?> fc,
            String classfield,
            double distance) throws Exception {
        Quadtree qtree = new Quadtree();
        for (FeatureIterator<?> is = fc.features(); is.hasNext();) {
            SimpleFeature fs = (SimpleFeature) is.next();
            Geometry gs = (Geometry) fs.getDefaultGeometry();
            qtree.insert(gs.getEnvelope().getEnvelopeInternal(), fs);
        }

        int fcsize = fc.size();
        int i = 1;
        for (FeatureIterator<?> is = fc.features(); is.hasNext();) {
            LOGGER.info("processing feature " + i + " of " + fcsize);
            SimpleFeature fs = (SimpleFeature) is.next();
            String as = fs.getAttribute(classfield).toString().trim();
            Geometry gs = (Geometry) fs.getDefaultGeometry();
            Envelope genv = gs.getEnvelope().getEnvelopeInternal();
            if (qtree.remove(genv, fs)) {
                LOGGER.info("Envelope: " + genv);
                List<SimpleFeature> inEnvelope = qtree.query(genv);
                LOGGER.info("Checking features: " + inEnvelope.size());
                for (Iterator<SimpleFeature> iin = inEnvelope.iterator(); iin.hasNext();) {
                    SimpleFeature fiin = (SimpleFeature) iin.next();
                    Geometry giin = (Geometry) fiin.getDefaultGeometry();
                    if (as.equals(fiin.getAttribute(classfield).toString().trim()) && (gs.distance(giin) <= distance)) {
                        gs = gs.union(giin);
                        qtree.remove(((Geometry) fiin.getDefaultGeometry()).getEnvelope().getEnvelopeInternal(), fiin);
                    }
                }
                fs.setDefaultGeometry(gs);
                qtree.insert(gs.getEnvelope().getEnvelopeInternal(), fs);
            }
            i++;
        }

        FeatureCollection<?, ?> fcnew = new ListFeatureCollection((SimpleFeatureType) fc.getSchema(), qtree.queryAll());
        return fcnew;
    }

}
