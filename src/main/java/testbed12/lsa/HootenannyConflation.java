package testbed12.lsa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.commons.context.ExecutionContextFactory;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataWithGT;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataWithGTBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.io.datahandler.parser.GTBinZippedSHPParser;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ExceptionReport;

import net.opengis.wps.x100.OutputDefinitionType;

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
        
        IData input1 = input1List.get(0);
        
        if(!(input1 instanceof GenericFileDataBinding)){
            return result;
        }
        
        List<IData> input2List = inputData.get(INPUT2);
        
        result.put(CONFLATION_OUTPUT, input1List.get(0));
        
        String report = "";
        
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(HootenannyConflation.class.getResourceAsStream("stats.txt")));
        
        String line = "";
        
        String lineSeparator = System.getProperty("line.separator");
        
        try {
            while((line = bufferedReader.readLine()) != null){
                report = report.concat(line + lineSeparator);
            }
            
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        
        String mimeType = "";
        
        OutputDefinitionType outputDefinitionType = ExecutionContextFactory.getContext().getOutputs().get(0);
        
        if(outputDefinitionType.getIdentifier().getStringValue().equals(CONFLATION_OUTPUT)){            
            mimeType = outputDefinitionType.getMimeType();            
        }else{
            outputDefinitionType = ExecutionContextFactory.getContext().getOutputs().get(1);
            if(outputDefinitionType.getIdentifier().getStringValue().equals(CONFLATION_OUTPUT)){            
                mimeType = outputDefinitionType.getMimeType();            
            }
        }
        
        File resultFile = null;
        
        GenericFileDataWithGT genericFileDataWithGT = null;        
        
        if(mimeType.contains("xml")){
            //deliver osm
            resultFile = new File("/home/benjamin/aoi-output.osm");
            
            try {
                genericFileDataWithGT = new GenericFileDataWithGT(resultFile, mimeType);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }else if(mimeType.contains("shp")){
            //deliver zipped shape file
            
            GTBinZippedSHPParser parser = new GTBinZippedSHPParser();
            
//            resultFile = new File("D:/52n/Projekte/Laufend/Testbed 12/Conflation/aoi-output.zip");
            resultFile = new File("/home/benjamin/aoi-output.zip");
            
            try {
                GTVectorDataBinding gtVectorDataBinding = parser.parse(new FileInputStream(resultFile), "application/x-zipped-shp", null);
                
                genericFileDataWithGT = new GenericFileDataWithGT(gtVectorDataBinding.getPayload());
                
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        
        result.put(CONFLATION_OUTPUT, new GenericFileDataWithGTBinding(genericFileDataWithGT));
        result.put(CONFLATION_REPORT, new LiteralStringBinding(report));
        
        return result;
    }

    @Override
    public List<String> getErrors() {
        return null;
    }

    @Override
    public Class<?> getInputDataType(String id) {
        if(id.equals(INPUT1)){
            return GenericFileDataBinding.class;
        }else if(id.equals(INPUT2)){
            return GenericFileDataBinding.class;
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
            return GenericFileDataWithGTBinding.class;
        }else if(id.equals(CONFLATION_REPORT)){
            return LiteralStringBinding.class;
        }
        return null;
    }

}
