package module6;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.OpenStreetMap;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 *
 * @author Your name here
 * Date: July 17, 2015
 */
public class EarthquakeCityMap extends PApplet {

	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.

	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;

	/**
	 * This is where to find the local tiles, for working without an Internet connection
	 */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	// The files containing city names and info and country names and info
	private final String cityFile = "city-data.json";
	private final String countryFile = "countries.geo.json";
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_week.atom";
	// The map
	private UnfoldingMap map;

	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;

	// NEW IN MODULE 5
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;

	public void setup () {
		// (1) Initializing canvas and map tiles
		size (900, 700, OPENGL);
		if (offline) {
			map = new UnfoldingMap (this, 200, 50, 650, 600, new MBTilesMapProvider (mbTilesString));
			earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		} else {
			map = new UnfoldingMap (this, 230, 50, 650, 600, new OpenStreetMap.OpenStreetMapProvider ());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			//earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher (this, map);

		// FOR TESTING: Set earthquakesURL to be one of the testing files by uncommenting
		// one of the lines below.  This will work whether you are online or offline
		//earthquakesURL = "test1.atom";
		//earthquakesURL = "test2.atom";

		// Uncomment this line to take the quiz
		earthquakesURL = "quiz2.atom";


		// (2) Reading in earthquake data and geometric properties
		//     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData (this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers (countries);

		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData (this, cityFile);
		cityMarkers = new ArrayList<Marker> ();
		for (Feature city : cities) {
			cityMarkers.add (new CityMarker (city));
		}

		//     STEP 3: read in earthquake RSS feed
		List<PointFeature> earthquakes = ParseFeed.parseEarthquake (this, earthquakesURL);
		quakeMarkers = new ArrayList<Marker> ();

		for (PointFeature feature : earthquakes) {
			//check if LandQuake
			if (isLand (feature)) {
				quakeMarkers.add (new LandQuakeMarker (feature));
			}
			// OceanQuakes
			else {
				quakeMarkers.add (new OceanQuakeMarker (feature));
			}
		}

		// could be used for debugging
		printQuakes ();
		sortAndPrintQuakes (20);

		// (3) Add markers to map
		//     NOTE: Country markers are not added to the map.  They are used
		//           for their geometric properties
		map.addMarkers (quakeMarkers);
		map.addMarkers (cityMarkers);


	}  // End setup


	public void draw () {
		background (0);
		map.draw ();
		addKey ();

	}


	// TODO: Add the method:
	//   private void sortAndPrint(int numToPrint)
	// and then call that method from setUp


	/**
	 * Event handler that gets called automatically when the
	 * mouse moves.
	 */


	@Override
	public void mouseMoved () {
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected (false);
			lastSelected = null;

		}
		selectMarkerIfHover (quakeMarkers);
		selectMarkerIfHover (cityMarkers);
		//loop();
	}

	// If there is a marker selected 
	private void selectMarkerIfHover (List<Marker> markers) {
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}

		for (Marker m : markers) {
			CommonMarker marker = (CommonMarker) m;
			if (marker.isInside (map, mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected (true);
				return;
			}
		}
	}

	/**
	 * The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes
	 * where the city is in the threat circle
	 */
	@Override
	public void mouseClicked () {
		if (lastClicked != null) {
			unhideMarkers ();
			lastClicked = null;
		} else if (lastClicked == null) {
			checkEarthquakesForClick ();
			if (lastClicked == null) {
				checkCitiesForClick ();
			}
		}
	}

	// Helper method that will check if a city marker was clicked on
	// and respond appropriately
	private void checkCitiesForClick () {
		if (lastClicked != null) return;
		// Loop over the earthquake markers to see if one of them is selected
		for (Marker marker : cityMarkers) {
			if (!marker.isHidden () && marker.isInside (map, mouseX, mouseY)) {
				lastClicked = (CommonMarker) marker;
				// Hide all the other earthquakes and hide
				for (Marker mhide : cityMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden (true);
					}
				}
				for (Marker mhide : quakeMarkers) {
					EarthquakeMarker quakeMarker = (EarthquakeMarker) mhide;
					if (quakeMarker.getDistanceTo (marker.getLocation ())
							> quakeMarker.threatCircle ()) {
						quakeMarker.setHidden (true);
					}
				}
				return;
			}
		}
	}

	// Helper method that will check if an earthquake marker was clicked on
	// and respond appropriately
	private void checkEarthquakesForClick () {
		if (lastClicked != null) return;
		// Loop over the earthquake markers to see if one of them is selected
		for (Marker m : quakeMarkers) {
			EarthquakeMarker marker = (EarthquakeMarker) m;
			if (!marker.isHidden () && marker.isInside (map, mouseX, mouseY)) {
				lastClicked = marker;
				// Hide all the other earthquakes and hide
				for (Marker mhide : quakeMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden (true);
					}
				}
				for (Marker mhide : cityMarkers) {
					if (mhide.getDistanceTo (marker.getLocation ())
							> marker.threatCircle ()) {
						mhide.setHidden (true);
					}
				}
				return;
			}
		}
	}

	// loop over and unhide all markers
	private void unhideMarkers () {
		for (Marker marker : quakeMarkers) {
			marker.setHidden (false);
		}

		for (Marker marker : cityMarkers) {
			marker.setHidden (false);
		}
	}

	// helper method to draw key in GUI
	private void addKey () {
		// Remember you can use Processing's graphics methods here
		fill (40, 40, 40);
		rect (20, 50, 190, 460, 5, 17, 17, 5);

		fill (255, 255, 255);
		textAlign (LEFT, CENTER);
		textSize (20);
		text ("Earthquake Key", 35, 80);
		textSize (18);
		text ("Earthquake depth", 35, 275);

		ellipse (50, 125, 15, 15);
		rect (40, 165, 15, 15);
		fill (115, 0, 230);
		triangle (40, 215, 50, 235, 60, 215);
		fill (0, 255, 0);
		ellipse (50, 325, 15, 15);
		fill (255, 255, 0);
		ellipse (50, 375, 15, 15);
		fill (255, 0, 0);
		ellipse (50, 425, 15, 15);
		stroke (255);
		line (45, 470, 55, 480);
		line (45, 480, 55, 470);

		fill (255, 255, 255);
		textSize (15);
		text ("Land quakes", 75, 125);
		text ("Ocean quakes", 75, 175);
		text ("City Markers", 75, 225);
		text ("Shallow", 75, 325);
		text ("Intermediate", 75, 375);
		text ("Deep", 75, 425);
		text ("Last day", 75, 475);
	}


	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.
	private boolean isLand (PointFeature earthquake) {

		// IMPLEMENT THIS: loop over all countries to check if location is in any of them
		// If it is, add 1 to the entry in countryQuakes corresponding to this country.
		for (Marker country : countryMarkers) {
			if (isInCountry (earthquake, country)) {
				return true;
			}
		}

		// not inside any country
		return false;
	}

	// prints countries with number of earthquakes
	// You will want to loop through the country markers or country features
	// (either will work) and then for each country, loop through
	// the quakes to count how many occurred in that country.
	// Recall that the country markers have a "name" property, 
	// And LandQuakeMarkers have a "country" property set.
	private void printQuakes () {
		int totalWaterQuakes = quakeMarkers.size ();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty ("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers) {
				EarthquakeMarker eqMarker = (EarthquakeMarker) marker;
				if (eqMarker.isOnLand ()) {
					if (countryName.equals (eqMarker.getStringProperty ("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println (countryName + ": " + numQuakes);
			}
		}
		System.out.println ("OCEAN QUAKES: " + totalWaterQuakes);
	}

	private void sortAndPrintQuakes (int numToPrint) {
		Object[] markers = quakeMarkers.toArray ();
		Arrays.sort (markers);
		if (markers.length < numToPrint) numToPrint = markers.length;
		for (int i = 0; i < numToPrint; i++) {
			System.out.println (markers[i]);
		}
	}


	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake feature if 
	// it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry (PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation ();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if (country.getClass () == MultiMarker.class) {

			// looping over markers making up MultiMarker
			for (Marker marker : ((MultiMarker) country).getMarkers ()) {

				// checking if inside
				if (((AbstractShapeMarker) marker).isInsideByLocation (checkLoc)) {
					earthquake.addProperty ("country", country.getProperty ("name"));

					// return if is inside one
					return true;
				}
			}
		}

		// check if inside country represented by SimplePolygonMarker
		else if (((AbstractShapeMarker) country).isInsideByLocation (checkLoc)) {
			earthquake.addProperty ("country", country.getProperty ("name"));

			return true;
		}
		return false;
	}

}
