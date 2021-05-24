package module6;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.List;

/**
 * A class to represent AirportMarkers on a world map.
 *
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 */
public class AirportMarker extends CommonMarker {
	public static List<SimpleLinesMarker> routes;

	public AirportMarker (Feature city) {
		super (((PointFeature) city).getLocation (), city.getProperties ());

	}

	@Override
	public void drawMarker (PGraphics pg, float x, float y) {
		pg.fill (255, 0, 0);
		pg.ellipse (x, y, 5, 5);


	}

	@Override
	public void showTitle (PGraphics pg, float x, float y) {
		// show rectangle with title
		String title = "Name " + getName () + "\nCity " + getCity () + "\nCountry " + getCountry () + "\nCode " + getCode ();
		pg.pushStyle ();
		pg.fill (0);
		pg.rect (x, y - 80, Math.max (pg.textWidth (title), pg.textWidth (title)) + 5, 80);
		pg.textSize (12);
		pg.textAlign (PConstants.LEFT, PConstants.TOP);
		pg.fill (255, 255, 255);
		pg.text (title, x + 3, y - 77);
		pg.popStyle ();
		// show routes


	}

	private String getName () {
		String name = getStringProperty ("name");
		return name.substring (1, name.length () - 1);
	}

	private String getCity () {
		String city = getStringProperty ("city");
		return city.substring (1, city.length () - 1);
	}

	private String getCountry () {
		String country = getStringProperty ("country");
		return country.substring (1, country.length () - 1);
	}

	private String getCode () {
		String code = getStringProperty ("code");
		return code.substring (1, code.length () - 1);
	}

}
