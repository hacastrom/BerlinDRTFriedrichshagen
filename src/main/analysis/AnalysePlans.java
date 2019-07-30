package main.analysis;



import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.*;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AnalysePlans {
	

	public static void main ( String[] args ) throws IOException {
        String username = "hugo";
        String rootPath = null;
        String caseOfInterest = "Base";
        
        switch (username){
        case("hugo"):
        	rootPath = "D:/TUbit/Shared/MATSim HA2/Analysis/";
        break;
        default:
        	System.out.println("username is not valid");
        	break;
        }


        String popInput = rootPath + "Plans-" + caseOfInterest + "Case.xml.gz";
        String networkInput = rootPath + "Network-" + caseOfInterest + "Case.xml.gz";
//        String outputFile = rootPath + "analysis-" + caseOfInterest + "/plansAnalysis.txt";
        String aDRTlines = rootPath + "analysis-" + caseOfInterest + "drtLines.txt";
        String aResults = rootPath + "analysis-" + caseOfInterest + "Results.txt";
        ArrayList<String> aDRTlegs = new ArrayList<>(); 
        long legID = 0;

//       BufferedWriter bw = IOUtils.getBufferedWriter(outputFile);
//       String headers = "nPersons, nLegs, nCarLegs, nPtLegs, nZoomerLegs, totalZoomerDistance, totalCarDistance, totalPtDistance, ";
//     bw.write(headers);
//      bw.newLine();

            Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
            new PopulationReader(sc).readFile(popInput);
            Network network = NetworkUtils.createNetwork();
            new MatsimNetworkReader (network).readFile(networkInput);
      

            final Population pop = sc.getPopulation();

            long nCarLegs = 0 ;
            long nPtLegs = 0 ;
            long nBicycleLegs = 0;
            long nZoomerLegs = 0;
            long nWalklegs = 0;
            long nCarUsingPersons = 0;
            long nPtUsingPersons = 0;
            double totalCarDistance = 0. ;
            double totalPtDistance = 0. ;
            double totalBicycleDistance = 0.;
            double totalZoomerDistance = 0;
            double totalWalkDistance = 0;
            

            for ( Person person : pop.getPersons().values() ) {
                boolean carUser = false ;
                boolean ptUser = false ;
                Plan plan = person.getSelectedPlan() ;
                for ( Leg leg : TripStructureUtils.getLegs(plan) ) {
                    if ( TransportMode.car.equals( leg.getMode() ) ) {
                        nCarLegs++ ;
                        carUser = true ;
                        totalCarDistance += leg.getRoute().getDistance() ;
                    }
                    else if ( TransportMode.pt.equals(leg.getMode())) {
                        nPtLegs++ ;
                        totalPtDistance += leg.getRoute().getDistance() ;
                        ptUser = true;
                    } else if ("zoomer".equals(leg.getMode())) {
                        nZoomerLegs++ ;
                        totalZoomerDistance +=  leg.getRoute().getDistance();
                        legID = nZoomerLegs;
//                        Id<Link> linkBegin = leg.getRoute().getStartLinkId();
//                        Link linkB = network.getLinks().get(leg.getRoute().getStartLinkId());
//                        Node node = linkB.getFromNode();
                        String coordI = network.getLinks().get(leg.getRoute().getStartLinkId()).getToNode().getCoord().toString();
                        String coordF = network.getLinks().get(leg.getRoute().getEndLinkId()).getToNode().getCoord().toString();
                        String aux = new String(Objects.toString(legID)+ ";" + Objects.toString(leg.getMode()) + ";" + Objects.toString(leg.getDepartureTime()) +";"+ coordI + ";"+coordF );
                        aDRTlegs.add(aux);
                    } else if ("bicycle".equals(leg.getMode())) {
                    	nBicycleLegs++;
                    	totalBicycleDistance += leg.getRoute().getDistance() ;
                    }
                    else if ("Walk".equals(leg.getMode())) {
                    	nWalklegs++;
                    	totalWalkDistance += leg.getRoute().getDistance() ;
                    }
                        
                }
                if ( carUser ) nCarUsingPersons++ ;
                if ( ptUser ) nPtUsingPersons++ ;
            }
  //          bw.newLine();
   //         bw.write(pop.getPersons().size() + "," + nCarLegs + "," + 1.*nCarLegs/pop.getPersons().size()+ "," +
    //                nCarUsingPersons + "," + nPtLegs + "," + totalCarDistance/1000 + "," + totalPtDistance/1000 + "," +
    //                nZoomerLegs + "," + totalZoomerDistance/1000);

   //     bw.flush();
    //    bw.close();
        ArrayList<String> results = new ArrayList<>();
        results.add("Total legs by Car: " + nCarLegs);
        results.add("Total legs by PT: " + nPtLegs);
        results.add("Total legs by Bycicle: " + nBicycleLegs);
        results.add("Total legs by Walk: " + nWalklegs);
        results.add("Total legs by Zoomer: " + nZoomerLegs);
        results.add("Total distance by Car: " + totalCarDistance);
        results.add("Total distance by Pt: " + totalPtDistance);
        results.add("Total distance by Walk: " + totalWalkDistance);
        results.add("Total distance by Bicycle: " + totalBicycleDistance);
        results.add("Total distance by Zoomer: " + totalZoomerDistance);
        results.add("Total Car Users: " + nCarUsingPersons);
        results.add("Total PT Users: " + nPtUsingPersons);
        writeToFile(aDRTlegs, aDRTlines);
        writeToFile(results, aResults);
        }



    static void writePlansFile(String headers, String analysisBase, String analysisPolicy, String outputFile){
        BufferedWriter bw = IOUtils.getBufferedWriter(outputFile);
        try {
            bw.write(headers);
            bw.newLine();
            bw.write((analysisBase));
            bw.newLine();
            bw.write(analysisPolicy);
            bw.flush();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String convertToCSV(String[] data) {
        return Stream.of(data)
          .map(this::escapeSpecialCharacters)
          .collect(Collectors.joining(","));
    }
    
    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
    static void writeToFile(ArrayList<String> Ids, String outputFile){
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