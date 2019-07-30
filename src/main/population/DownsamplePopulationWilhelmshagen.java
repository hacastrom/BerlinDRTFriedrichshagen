package main.population;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.utils.io.IOUtils;
import org.opengis.feature.simple.SimpleFeature;

import java.io.BufferedWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;

/** DownsamplePopulationFrohnau analyses the plans of our population, and finds the agents who have at least one activity
 * (in selected plan) that occurs within the bounds of Frohnau, as defined by a .shp file. The more detailed selected
 * requirements are as follows:
 * + if the agent is a actually a person (not freight, etc.)
 * + if the coordinate of at least one activity falls within the bounds of the shape file
 * + BUT, the activity type cannot be "pt interaction", "car interaction" or "ride interaction"
 *
 * Those restrictions are used to define the first output population. The second output population is composed of the
 * same agents and plans as the first population. The only difference is that all instances of "access_walk" and
 * "egress_walk" are replaced with "non_network_walk", so as to be in compliance with the new standard practices of MATSim.
 *
 * 1pct
 * before : AGENTS --- inside:185 --- outside: 48838--- freight: 267 --- total: 49290
 * after  : AGENTS --- inside:169 --- outside: 48854--- freight: 267 --- total: 49290
 *
 * 10pct:
 * before : AGENTS --- inside:1955 --- outside: 489396--- freight: 2756 --- total: 494107
 * after  : AGENTS --- inside:1845 --- outside: 489506--- freight: 2756 --- total: 494107
 */

public class DownsamplePopulationWilhelmshagen {
        public static void main(String[] args) {

//            String username = "jakob";
//            String rootPath = null;
//            String percent = "1";
//
//            switch (username) {
//                case "jakob":
//                    rootPath = "C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/";
//                    break;
//                case "david":
//                    rootPath = "C:/Users/david/ENTER_PATH_HERE";
//                    break;
//                default:
//                    System.out.println("Incorrect Base Path");
//            }
//            String inputPopFilename = rootPath + "Input_global/plans/berlin-plans-" + percent + "pct-original.xml.gz";
//            String outputPopFilename = rootPath  + "Input_global/plans/berlin-plans-" + percent + "pct-frohnau.xml.gz";
//            String outputPopFilenameScrubbed = rootPath  + "Input_global/plans/berlin-plans-" + percent + "pct-frohnau-scrubbed.xml.gz";
//            String AgentsWithinFrohnauFilename = rootPath + "Input_global/agents-"+ percent +"pct-InsideFrohnau.txt";
//            String AgentsOutsideFrohnauFilename = rootPath + "Input_global/agents-"+ percent +"pct-OutsideFrohnau.txt";
//            String ActivitiesFilename = rootPath + "Input_global/activitiesInFrohnau-"+ percent +"pct.txt";
//            String filterShape = rootPath + "Frohnau/Frohnau-1.shp" ;
        	
            String inputPopFilename = "D:\\TUbit\\Shared\\MATSim HA2\\ModifyPopulation\\berlin-v5.3-1pct.0.plans.xml";
            String outputPopFilename = "D:\\TUbit\\Shared\\MATSim HA2\\ModifyPopulation\\berlin-Friedrichshagen-Plans.xml.gz";
            String outputPopFilenameScrubbed = "D:\\TUbit\\Shared\\MATSim HA2\\ModifyPopulation\\berlin-Friedrichshagen-Plans-Scrubbed.xml.gz" ;
            String AgentsWithinFriedrichshagenFilename =  "D:\\TUbit\\Shared\\MATSim HA2\\ModifyPopulation\\inside-Friedrichhagen.txt";
            String AgentsOutsideFriedrichshagenFilename = "D:\\TUbit\\Shared\\MATSim HA2\\ModifyPopulation\\outside-Friedrichhagen.txt";
            String ActivitiesFilename = "D:\\TUbit\\Shared\\MATSim HA2\\ModifyPopulation\\outside-Friedrichhagen.txt";
            String filterShape = "D:\\TUbit\\Shared\\MATSim HA2\\Shapes\\Shape Area of interest\\Wilhelmshagen.shp";  

            // Initialize Variables
            ArrayList<String> agentsWithinFrohnau = new ArrayList<>() ;
            ArrayList<String> agentsOutsideFrohnau = new ArrayList<>() ;
            ArrayList<String> activities = new ArrayList<>();
            int agentsTotal = 0;
            int freightTotal = 0;

            // Read Population
            Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
            new PopulationReader(sc).readFile(inputPopFilename);
            final Population pop = sc.getPopulation();

            // New Population
            Scenario sc2 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
            Population pop2 = sc2.getPopulation() ;


            // Read Shapefile
            final Collection<Geometry> geometries = new ArrayList<>();
            for (SimpleFeature feature : ShapeFileReader.getAllFeatures(filterShape) ){
                geometries.add((Geometry) feature.getDefaultGeometry());
            }

            // Find Agents with activities within Borders of Frohnau
            for ( Person person : pop.getPersons().values() ) {
                agentsTotal++;
                if (person.getId().toString().contains("freight")) {
                    freightTotal++;
                    continue;
                }
                Plan plan = person.getSelectedPlan() ;
                agentsOutsideFrohnau.add(person.getId().toString());
                for ( Activity activity : TripStructureUtils.getActivities(plan, null)) {
                    Coord coord = activity.getCoord();
                    String actType = activity.getType() ;
                    if ((geometries.stream().anyMatch(geom -> geom.contains(MGC.coord2Point(coord))))
                            && (!actType.contains("car"))
                            && (!actType.contains("pt"))
                            && (!actType.contains("ride"))) {
                        agentsOutsideFrohnau.remove(person.getId().toString());
                        agentsWithinFrohnau.add(person.getId().toString());
                        pop2.addPerson(person);
                        activities.add(actType);
                        break;
                    }

                }
            }


            // Print Frohnau Population (before scrub)
            new PopulationWriter(pop2).write(outputPopFilename);


            // Scub: Replace Access Walk and Egress Walk with Non-Network-Walk
            pop2 = RemoveAccessEgressWalkFromPlans(pop2);

            // Print Frohnau Population (after scrub)
            new PopulationWriter(pop2).write(outputPopFilenameScrubbed);

            // Print and Save Results
            System.out.println("AGENTS --- inside:" + agentsWithinFrohnau.size() + " --- outside: " + agentsOutsideFrohnau.size() + "--- freight: " + freightTotal + " --- total: " + agentsTotal);

            writeIdsToFile(agentsWithinFrohnau, AgentsWithinFriedrichshagenFilename);
            writeIdsToFile(agentsOutsideFrohnau, AgentsOutsideFriedrichshagenFilename);
            writeIdsToFile(activities, ActivitiesFilename);
        }

        static void writeIdsToFile(ArrayList<String> linkIds, String outputFile){
            BufferedWriter bw = IOUtils.getBufferedWriter(outputFile);
            try {
                for (int i = 0;i< linkIds.size();i++){
                    bw.write(linkIds.get(i));
                    bw.newLine();
                }
                bw.flush();
                bw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    public static Population RemoveAccessEgressWalkFromPlans(Population pop) {
            for ( Person person : pop.getPersons().values() ) {
            for (Plan plan : person.getPlans()){
                for (Leg leg : TripStructureUtils.getLegs(plan)) {
                    if ((leg.getMode().equals("access_walk")) || (leg.getMode().equals("egress_walk"))) {
                        leg.setMode(TransportMode.non_network_walk);
                    }
                }
            }
        }
        return pop ;
    }
}
