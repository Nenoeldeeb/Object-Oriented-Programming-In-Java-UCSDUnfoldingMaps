package module6;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Implements a visual marker for cities on an earthquake map
 *
 * @author UC San Diego Intermediate Software Development MOOC team
 */
public class CityMarker extends CommonMarker {

	public static int TRI_SIZE = 5;  // The size of the triangle marker

	public CityMarker (Location location) {
		super (location);
	}


	public CityMarker (Feature city) {
		super (((PointFeature) city).getLocation (), city.getProperties ());
		// Cities have properties: "name" (city name), "country" (country name)
		// and "population" (population, in millions)
	}


	// pg is the graphics object on which you call the graphics
	// methods.  e.g. pg.fill(255, 0, 0) will set the color to red
	// x and y are the center of the object to draw. 
	// They will be used to calculate the coordinates to pass
	// into any shape drawing methods.  
	// e.g. pg.rect(x, y, 10, 10) will draw a 10x10 square
	// whose upper left corner is at position x, y

	/**
	 * Implementation of method to draw marker on the map.
	 */
	public void drawMarker (PGraphics pg, float x, float y) {
		//System.out.println("Drawing a city");
		// Save previous drawing style
		pg.pushStyle ();

		// IMPLEMENT: drawing triangle for each city
		pg.fill (115, 0, 230);
		pg.triangle (x - TRI_SIZE, y - TRI_SIZE, x, y, x + TRI_SIZE, y - TRI_SIZE);

		// Restore previous drawing style
		pg.popStyle ();
	}

	/**
	 * Show the title of the city if this marker is selected
	 */
	public void showTitle (PGraphics pg, float x, float y) {
		String title = getCity () + " " + getCountry () + "\nPop " + getPopulation () + " M";
		pg.pushStyle ();
		pg.fill (0);
		pg.rect (x, y - TRI_SIZE - 35, Math.max (pg.textWidth (title), pg.textWidth (title)) + 6, 35);
		pg.textSize (12);
		pg.textAlign (PConstants.LEFT, PConstants.TOP);
		pg.fill (255, 255, 255);
		pg.text (title, x + 3, y - TRI_SIZE - 33);
		pg.popStyle ();
	}


	private String getCity () {
		return getStringProperty ("name");
	}

	private String getCountry () {
		return getStringProperty ("country");
	}

	private float getPopulation () {
		return Float.parseFloat (getStringProperty ("population"));
	}
}
