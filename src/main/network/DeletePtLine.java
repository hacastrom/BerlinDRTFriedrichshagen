package main.network;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

//This class is intended to delete an entire PTLine without compromising the functionality of the entire Schedule

public class DeletePtLine {
	
	public static void main(String[] args) {
	
	//Define Input and output folders
	Path transitScheduleInput = Paths.get("D:\\TUbit\\Shared\\MATSim HA2\\Line Testen\\berlin-v5.3-10pct.output_transitSchedule.xml.gz");
	Path transitScheduleOutput = Paths.get("D:\\TUbit\\Shared\\MATSim HA2\\Line Testen\\berlin-Transit.xml.gz");
	Path networkInput = Paths.get("D:\\TUbit\\Shared\\MATSim HA2\\Line Testen\\berlin-v5.3-10pct.output_network.xml.gz");
	String Stopsoutput = "D:\\TUbit\\Shared\\MATSim HA2\\Line Testen\\Stops.txt";
	
	Config config = ConfigUtils.createConfig();
	Scenario scenario = ScenarioUtils.createScenario(config);
	// Read in existing files
	new TransitScheduleReader(scenario).readFile(transitScheduleInput.toString());
	new MatsimNetworkReader(scenario.getNetwork()).readFile(networkInput.toString());
	ArrayList<String> stopsLine = new ArrayList<>();
	
	//Define lines that should be deleted 
	Set<String> linesToDelete = new HashSet<>(Arrays.asList("17326_700"));
	
	
	//In case multiple Lines should be deleted
	
	for (String lineID: linesToDelete) {
		
		DeletePtLine.deleteLine(scenario, lineID, stopsLine);
		
	}
	
//	write new Schedule
	new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(transitScheduleOutput.toString());
//	write list with the Tranport facilities
	LinkedHashSet<String> hashSet = new LinkedHashSet<>(stopsLine);
    ArrayList<String> listWithoutDuplicate = new ArrayList<>(hashSet);
	writeIdsToFile(listWithoutDuplicate, Stopsoutput);
	}
//Remove all the Routes from a line
	public static void deleteLine(Scenario scenario, String lineID, ArrayList<String> StopsLine) {
		
		TransitLine ptLine = scenario.getTransitSchedule().getTransitLines().get(Id.create(lineID, TransitLine.class));
		
		List<TransitRoute> toRemove = ptLine.getRoutes().values().stream().collect(Collectors.toList());
		for(TransitRoute route: toRemove) {
			System.out.print(route);
			List<TransitRouteStop> Stops = route.getStops(); 
			for (Object obj : Stops) {
				StopsLine.add(obj.toString());
			}
			ptLine.removeRoute(route);
//			System.out.print("the current routes are : ");
//			System.out.print(ptLine.getRoutes());
		
	}	
}

    static void writeIdsToFile(ArrayList<String> Ids, String outputFile){
        BufferedWriter bw = IOUtils.getBufferedWriter(outputFile);
        try {
            for (int i = 0;i< Ids.size();i++){
                bw.write(Ids.get(i));
                bw.newLine();
            }
            bw.flush();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }	
			

}
