package testbed12.lsa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.n52.wps.commons.WPSConfig;
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
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.grass.util.JavaProcessStreamReader;
import org.n52.wps.testbed12.DummyAlgorithmRepository;
import org.n52.wps.testbed12.module.Testbed12ProcessConfigModule;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opengis.wps.x100.OutputDefinitionType;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.data.binding.complex.GenericXMLDataBinding;
import testbed12.fo.util.DQUtils;
import testbed12.fo.util.DQ_AbsoluteExternalPositionalAccuracy;

public class HootenannyConflation extends AbstractAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(HootenannyConflation.class);

    private final static String INPUT1 = "INPUT1";
    private final static String INPUT1_TRANSLATION = "INPUT1_TRANSLATION";
    private final static String INPUT1_DATA_QUALITY = "INPUT1_DATA_QUALITY";
    private final static String INPUT2 = "INPUT2";
    private final static String INPUT2_DATA_QUALITY = "INPUT2_DATA_QUALITY";
    private final static String CONFLATION_TYPE = "CONFLATION_TYPE";
    private final static String MATCH_THRESHOLD = "MATCH_THRESHOLD";
    private final static String MISS_THRESHOLD = "MISS_THRESHOLD";
    private final static String REFERENCE_LAYER = "REFERENCE_LAYER";
    private final static String CONFLATION_OUTPUT = "CONFLATION_OUTPUT";
    private final static String CONFLATION_REPORT = "CONFLATION_REPORT";
    private final String lineSeparator = System.getProperty("line.separator");

    private double match_threshold = 0.161;
    private double miss_threshold = 0.999;

    private File statsFile;

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) throws ExceptionReport {

        ConfigurationModule configModule = WPSConfig.getInstance().getConfigurationModuleForClass(DummyAlgorithmRepository.class.getName(), ConfigurationCategory.REPOSITORY);

        List<? extends ConfigurationEntry<?>> propertyArray = configModule.getConfigurationEntries();

        String hootenannyHome = "";

        for (ConfigurationEntry<?> property : propertyArray) {
            if (property.getKey().equalsIgnoreCase(
                    Testbed12ProcessConfigModule.hootenannyHomeKey)) {
                hootenannyHome = property.getValue().toString();
            }
        }

        List<IData> input1List = inputData.get(INPUT1);

        List<IData> input2List = inputData.get(INPUT2);

        List<IData> input1_translationList = inputData.get(INPUT1_TRANSLATION);

        List<IData> input1_dataqualityList = inputData.get(INPUT1_DATA_QUALITY);
        
        List<IData> input2_dataqualityList = inputData.get(INPUT2_DATA_QUALITY);
        
        // get input1 data quality:
        IData input1_dataquality = input1_dataqualityList.get(0);
        DQUtils dqutils_input1 = new DQUtils(new DQ_AbsoluteExternalPositionalAccuracy((XmlObject) input1_dataquality.getPayload()));
        
        // get input2 data quality:
        IData input2_dataquality = input2_dataqualityList.get(0);
        DQUtils dqutils_input2 = new DQUtils(new DQ_AbsoluteExternalPositionalAccuracy((XmlObject) input2_dataquality.getPayload()));

        Map<String, IData> result = new HashMap<>();

        IData input1 = input1List.get(0);

        IData input2 = input2List.get(0);

        if (!(input1 instanceof GenericFileDataBinding) || !(input2 instanceof GenericFileDataBinding)) {
            return result;
        }

        File inputFile1 = ((GenericFileDataBinding) input1).getPayload().getBaseFile(true);

        File inputFile2 = ((GenericFileDataBinding) input2).getPayload().getBaseFile(true);

        String input1FilenameWithoutSuffix = getStringWithoutSuffix(inputFile1.getName());

        String input1FilenameOSM = inputFile1.getParent() + File.separatorChar + input1FilenameWithoutSuffix + ".osm";

        String input1FilenameSHP = inputFile1.getParent() + File.separatorChar + input1FilenameWithoutSuffix + ".shp";

        String currentTimeMilis = "" + System.currentTimeMillis();

        List<IData> matchThresholdList = inputData.get(MATCH_THRESHOLD);

        List<IData> missThresholdList = inputData.get(MISS_THRESHOLD);

        IData matchThresholdData = null;

        if (matchThresholdList != null && matchThresholdList.size() != 0) {
            matchThresholdData = matchThresholdList.get(0);
        }

        IData missThresholdData = null;

        if (missThresholdList != null && missThresholdList.size() != 0) {
            missThresholdData = missThresholdList.get(0);
        }

        if (matchThresholdData != null) {
            try {
                match_threshold = ((LiteralDoubleBinding) matchThresholdData).getPayload();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        if (missThresholdData != null) {
            try {
                miss_threshold = ((LiteralDoubleBinding) missThresholdData).getPayload();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        log.info("Starting conflation " + currentTimeMilis);

        //copy translation file to hootenanny directory if file exists
        if (input1_translationList.size() > 0) {
            IData input1_translation = input1_translationList.get(0);

            if (input1_translation instanceof GenericFileDataBinding) {

                File input1_translationFile = ((GenericFileDataBinding) input1_translation).getPayload().getBaseFile(false);

                File destFile = new File(hootenannyHome + "/translations/" + currentTimeMilis + "Input1.py");

                try {
                    FileUtils.copyFile(input1_translationFile, destFile);
                } catch (IOException e) {
                    log.error("Coul not copy translation file to hootenanny directory.", e);
                }

                String ogr2osmForInput1Command = "hoot ogr2osm " + getStringWithoutSuffix(destFile.getName()) + " " + input1FilenameOSM + " " + input1FilenameSHP;

                executeHootenannyCommand(ogr2osmForInput1Command);
            }
        }
        
        
        //TODO handle case if second input is not osm
        String statisticsFormat = "asciidoc";

        String statisticsMimeType = "text/plain";

        for (OutputDefinitionType outputDefinitionType : ExecutionContextFactory.getContext().getOutputs()) {
            if (outputDefinitionType.getIdentifier().getStringValue().equals(CONFLATION_REPORT)) {
                statisticsMimeType = outputDefinitionType.getMimeType();
                switch (outputDefinitionType.getMimeType()) {
                    case "application/pdf":
                        statisticsFormat = "pdf";
                        break;
                    case "text/html":
                        statisticsFormat = "html";
                        break;
                    default:
                        break;
                }
            }
        }

        //conflate
        try {
            statsFile = File.createTempFile("hootStats", "." + statisticsFormat);
        } catch (IOException e1) {
            log.error("Could not create temp file for storing conflation stats", e1);
        }

        String outputFilenameOSM = inputFile1.getParent() + File.separatorChar + currentTimeMilis + "conflationOutput.osm";

        String outputFilenameSHP = getStringWithoutSuffix(outputFilenameOSM) + ".shp";
        
        String conflationCommand = "";
        if (dqutils_input1.getMeanDisplacement() < dqutils_input2.getMeanDisplacement())
            conflationCommand = "hoot --conflate -D highway.match.threshold=" + match_threshold + " -D highway.miss.threshold=" + miss_threshold + " -D stats.output=" + getStringWithoutSuffix(statsFile.getAbsolutePath()) + " -D stats.format=" + statisticsFormat + " " + input1FilenameOSM + " " + inputFile2.getAbsolutePath() + " " + outputFilenameOSM + " --stats";
        else
            conflationCommand = "hoot --conflate -D highway.match.threshold=" + match_threshold + " -D highway.miss.threshold=" + miss_threshold + " -D stats.output=" + getStringWithoutSuffix(statsFile.getAbsolutePath()) + " -D stats.format=" + statisticsFormat + " " + inputFile2.getAbsolutePath() + " " + input1FilenameOSM + " " + outputFilenameOSM + " --stats";
        
        executeHootenannyCommand(conflationCommand, true);

        String osm2orgCommandForOutput = "hoot osm2shp " + outputFilenameOSM + " " + outputFilenameSHP;

        executeHootenannyCommand(osm2orgCommandForOutput);

        //Shapefile will be split in lines, points and polygons, we just return the lines
        File outputFileSHPLines = new File(getStringWithoutSuffix(outputFilenameSHP) + "Lines.shp");

        try {
            result.put(CONFLATION_OUTPUT, new GenericFileDataWithGT(outputFileSHPLines, "application/x-zipped-shp").getAsGTVectorDataBinding());
        } catch (IOException e1) {
            log.error("Could not create GenericFiledData for conflation output file.");
        }

        try {
            result.put(CONFLATION_REPORT, new GenericFileDataBinding(new GenericFileData(statsFile, statisticsMimeType)));
        } catch (IOException e) {
            log.error("Could not create GenericFiledData for conflation statistics file.");
        }

        return result;
    }

    //return a substring of the string until the first occurrence of a dot
    private String getStringWithoutSuffix(String inputString) {
        return inputString.substring(0, inputString.indexOf("."));
    }

    @Override
    public List<String> getErrors() {
        return null;
    }

    @Override
    public Class<?> getInputDataType(String id) {
        if (id.equals(INPUT1)) {
            return GenericFileDataBinding.class;
        } else if (id.equals(INPUT1_TRANSLATION)) {
            return GenericFileDataBinding.class;
        } else if (id.equals(INPUT1_DATA_QUALITY)) {
            return GenericXMLDataBinding.class;
        } else if (id.equals(INPUT2_DATA_QUALITY)) {
            return GenericXMLDataBinding.class;
        } else if (id.equals(INPUT2)) {
            return GenericFileDataBinding.class;
        } else if (id.equals(CONFLATION_TYPE)) {
            return LiteralStringBinding.class;
        } else if (id.equals(MATCH_THRESHOLD)) {
            return LiteralDoubleBinding.class;
        } else if (id.equals(MISS_THRESHOLD)) {
            return LiteralDoubleBinding.class;
        } else if (id.equals(REFERENCE_LAYER)) {
            return LiteralIntBinding.class;
        }
        return null;
    }

    @Override
    public Class<?> getOutputDataType(String id) {
        if (id.equals(CONFLATION_OUTPUT)) {
            return GTVectorDataBinding.class;
        } else if (id.equals(CONFLATION_REPORT)) {
            return GenericFileDataBinding.class;
        }
        return null;
    }

    private void executeHootenannyCommand(String command) {
        executeHootenannyCommand(command, false);
    }

    private void executeHootenannyCommand(String command, boolean writeToStatsFile) {

        try {

            log.info("Executing Hootenanny command " + command);

            Runtime rt = Runtime.getRuntime();

            Process proc = rt.exec(command);

            PipedOutputStream pipedOut = new PipedOutputStream();

            PipedInputStream pipedIn = new PipedInputStream(pipedOut);

            // attach error stream reader
            JavaProcessStreamReader errorStreamReader = new JavaProcessStreamReader(proc
                    .getErrorStream(), "ERROR", pipedOut);

            // attach output stream reader
            JavaProcessStreamReader outputStreamReader = new JavaProcessStreamReader(proc
                    .getInputStream(), "OUTPUT");

            // start them
            errorStreamReader.start();
            outputStreamReader.start();

            //fetch stats from console
            String errors = "";
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(pipedIn));) {
                String line = errorReader.readLine();

                while (line != null) {
                    errors = errors.concat(line + lineSeparator);
                    line = errorReader.readLine();
                }
            }

            try {
                proc.waitFor();
            } catch (InterruptedException e1) {
                log.error("Java process was interrupted.", e1);
            } finally {
                proc.destroy();
            }

            if (!errors.equals("")) {
                log.error("Errors detected: " + errors);
            }

        } catch (IOException e) {
            log.error("An error occured while executing the Hootenanny command " + command, e);
            throw new RuntimeException(e);
        }
    }

}
