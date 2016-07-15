package testbed12.lsa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.PlainStringBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ExceptionReport;

public class HootenannyConflation extends AbstractAlgorithm {

    private final static String INPUT1 = "INPUT1";
    private final static String INPUT2 = "INPUT2";
    private final static String CONFLATION_TYPE = "CONFLATION_TYPE";
    private final static String MATCH_THRESHOLD = "MATCH_THRESHOLD";
    private final static String MISS_THRESHOLD = "MISS_THRESHOLD";
    private final static String REFERENCE_LAYER = "REFERENCE_LAYER";
    private final static String CONFLATION_OUTPUT = "CONFLATION_OUTPUT";
    private final static String CONFLATION_REPORT = "CONFLATION_REPORT";
    
    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) throws ExceptionReport {
        
        List<IData> input1List = inputData.get(INPUT1);
        
        Map<String, IData> result = new HashMap<>();
        
        result.put(CONFLATION_OUTPUT, input1List.get(0));
        
        String report = "Example report, output is equal to input1.";
        
        result.put(CONFLATION_REPORT, new PlainStringBinding(report));
        
        return result;
    }

    @Override
    public List<String> getErrors() {
        return null;
    }

    @Override
    public Class<?> getInputDataType(String id) {
        if(id.equals(INPUT1)){
            return GTVectorDataBinding.class;
        }else if(id.equals(INPUT2)){
            return GTVectorDataBinding.class;
        }else if(id.equals(CONFLATION_TYPE)){
            return LiteralStringBinding.class;
        }else if(id.equals(MATCH_THRESHOLD)){
            return LiteralDoubleBinding.class;
        }else if(id.equals(MISS_THRESHOLD)){
            return LiteralDoubleBinding.class;
        }else if(id.equals(REFERENCE_LAYER)){
            return LiteralIntBinding.class;
        }
        return null;
    }

    @Override
    public Class<?> getOutputDataType(String id) {
        if(id.equals(CONFLATION_OUTPUT)){
            return GTVectorDataBinding.class;
        }else if(id.equals(CONFLATION_REPORT)){
            return PlainStringBinding.class;
        }
        return null;
    }

}
