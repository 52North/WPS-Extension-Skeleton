package testbed12.fo.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryHelper {
	
	/** ********************************************************************* */

	/** ********************************************************************* */	
	public static Polygon translatePolygon(Polygon polygonOri, Coordinate trans) {
		//System.out.println("Polygon-Ori (bevor): " + polygonOri.toString());
		
		Polygon polygon = (Polygon)polygonOri.clone();
		
		// a) Skalierung des aeusseren Rings
		Coordinate[] extRingCoord = polygon.getExteriorRing().getCoordinates();
		for(int i=0; i<extRingCoord.length; i++)	{
			extRingCoord[i].x += trans.x;
			extRingCoord[i].y += trans.y;
		}
		
		// b) Skalierung der inneren Ringe
		//LinearRing[] intRingLineString = new LinearRing[polygon.getNumInteriorRing()];			
		for(int i=0; i<polygon.getNumInteriorRing(); i++)	{
			Coordinate[] intRingCoord = polygon.getInteriorRingN(i).getCoordinates();
			for(i=0; i<intRingCoord.length; i++)	{
				intRingCoord[i].x += trans.x;
				intRingCoord[i].y += trans.y;
			}
		}
		//System.out.println("Polygon-Ori (nach) : " + polygonOri.toString());
		//System.out.println("Polygon-Res (nach) : " + polygon.toString());
		return(polygon);
	}
	/** ********************************************************************* */
    /**	geklont von GNtranslatePolygon(), matthias							  */
	/** ********************************************************************* */	
	public static LineString translateLineString(LineString lineStringOri, Coordinate trans) {
		//System.out.println("LineString-Ori (bevor): " + polygonOri.toString());
		
		LineString lineString = (LineString)lineStringOri.clone();
		
		// a) Skalierung des aeusseren Rings
		Coordinate[] coords = lineString.getCoordinates();
		for(int i=0; i<coords.length; i++)	{
			coords[i].x += trans.x;
			coords[i].y += trans.y;
		}
		
		//System.out.println("LineString-Ori (nach) : " + lineStringOri.toString());
		//System.out.println("LineString-Res (nach) : " + lineString.toString());
		return(lineString);
	}
	/** ********************************************************************* */
    /**	geklont von GNtranslatePolygon(), matthias							  */
	/** ********************************************************************* */	
	public static Point translatePoint(Point pointOri, Coordinate trans) {
		//System.out.println("Point-Ori (bevor): " + pointOri.toString());
		
		Point point = (Point)pointOri.clone();
		
		// a) Skalierung des aeusseren Rings
		Coordinate[] coords = point.getCoordinates();
		for(int i=0; i<coords.length; i++)	{
			coords[i].x += trans.x;
			coords[i].y += trans.y;
		}
		
		//System.out.println("Point-Ori (nach) : " + pointOri.toString());
		//System.out.println("Point-Res (nach) : " + point.toString());
		return(point);
	}
	
/** ********************************************************************* */	
	
	/** ********************************************************************* */
	/**
	 * Funktion vergr�ssert Polygon auf Mindestgr�sse
	 * @param polygon	Inputgeometrie
	 * @param area_min	Mindestgroesse
	 */
	public static Polygon skaleMinSize(Polygon polygon, double area_min) {
		if(polygon.getArea() >= area_min)				// Abbruch falls Polygon schon groesser als area_min ist
			return(polygon);
		return(skaleSize(polygon, area_min));
	}
	/** ********************************************************************* */	
	
	/** ********************************************************************* */	
	public static Polygon skaleSize(Polygon polygon, double targetSize) {		
		double threshold = Math.abs(targetSize/100.0); 	// Schwellwert f�r Genauigkeit von 1%

		Coordinate sp_ori = new Coordinate();
		Coordinate sp_neu = new Coordinate();
		Coordinate trans  = new Coordinate();
		Polygon polygonCopy = (Polygon)polygon.clone();
		//Polygon polygonCopy = (Polygon)polygon;
		double area = 0.0;
		double scaleFactor = 0.0;
		double obererScaleFactor = 0.0;					// oberer Skalierungsfakrot (muss bestimmt werden)
		double untererScaleFactor = 1.0;				// unterer Skalierungsfaktor (ist automat. gegeben)
		boolean obererScaleFactorExist = false;
		int i; 

		/* 1. Schwerpunkts- und Fl�chenberechnung der Originalfl�che */
		sp_ori.x = polygon.getCentroid().getX();
		sp_ori.y = polygon.getCentroid().getY();
		area = polygon.getArea();

		/* 2. Skalierungsfaktor */
		scaleFactor = Math.sqrt(targetSize/area);
		
		/* 3. Iteration �ber Skalierung */
		while(Math.abs(targetSize - area) > threshold) {
			polygonCopy = (Polygon)polygon.clone();
			
			// a) Skalierung des aeusseren Rings
			Coordinate[] extRingCoord = polygonCopy.getExteriorRing().getCoordinates();
			for(i=0; i<extRingCoord.length; i++)	{
				extRingCoord[i].x *= scaleFactor;
				extRingCoord[i].y *= scaleFactor;
			}
			
			// b) Skalierung der inneren Ringe
			//LinearRing[] intRingLineString = new LinearRing[polygonCopy.getNumInteriorRing()];			
			for(i=0; i<polygonCopy.getNumInteriorRing(); i++)	{
				Coordinate[] intRingCoord = polygonCopy.getInteriorRingN(i).getCoordinates();
				for(i=0; i<intRingCoord.length; i++)	{
					intRingCoord[i].x *= scaleFactor;
					intRingCoord[i].y *= scaleFactor;
				}
			}
			
			// c) Berechnen des oberen Skalierungsfaktors falls n�tig
			area = polygonCopy.getArea();		
			if (obererScaleFactorExist == false) { // oberer Skalierungsfaktor muss noch bestimmt werden
				if(area > targetSize) {
					obererScaleFactor = scaleFactor;
					obererScaleFactorExist = true;
					scaleFactor += (obererScaleFactor  - untererScaleFactor)/2.0;
				} else {
					untererScaleFactor = scaleFactor;
					scaleFactor *= 2.0;
				}				
			} else {
				// d) Berechnen des neuen Skalierungsfaktors
				if(area < targetSize) {
					scaleFactor += (obererScaleFactor  - scaleFactor)/2.0;
				} else {
					scaleFactor = untererScaleFactor + (scaleFactor - untererScaleFactor)/2.0;
				}				
			}			
//			System.out.println(" | " + area + " | " + scaleFactor);
		}		

		/* 4. Skaliertes Polygon auf urspr�nglichen Schwerpunkt verschieben */    
		sp_neu.x = polygonCopy.getCentroid().getX();
		sp_neu.y = polygonCopy.getCentroid().getY();
		trans.x = sp_neu.x - sp_ori.x;
		trans.y = sp_neu.y - sp_ori.y;
		
		// a) Verschieben des aeusseren Rings
		Coordinate[] extRingCoord = polygonCopy.getExteriorRing().getCoordinates();
		for(i=0; i<extRingCoord.length; i++)	{
			extRingCoord[i].x -= trans.x;
			extRingCoord[i].y -= trans.y;
		}
		
		// b) Verschieben der inneren Ringe
		//LinearRing[] intRingLineString = new LinearRing[polygonCopy.getNumInteriorRing()];			
		for(i=0; i<polygonCopy.getNumInteriorRing(); i++)	{
			Coordinate[] intRingCoord = polygonCopy.getInteriorRingN(i).getCoordinates();
			for(i=0; i<intRingCoord.length; i++)	{
				intRingCoord[i].x -= trans.x;
				intRingCoord[i].y -= trans.y;
			}
		}
		
		return(polygonCopy);
	}  
	/** ********************************************************************* */

	/** ********************************************************************* */
	/**
	 *  Douglas-Peucker-Algorithmus
	 *  @param CoordinateList 
	 *  @param abweichung
	 *  @return number of deleted points  
	 */
	public static int dp_simplification(CoordinateList polyLine, double abweichung) {
		int dp[] = new int[polyLine.size()];
		int deletedPoints = 0;		
		//Coordinate p1  = new Coordinate(0.0, 0.0);
		//Coordinate p2  = new Coordinate(0.0, 0.0);
		//Coordinate pos  = new Coordinate(0.0, 0.0);
		
		int	dp_a = 0, 
			dp_e = 0;
		int	dp_r = 0, 
			dp_akt = 0;
			
		// 1. Anfangs- und Endpunkt sind Douglas-Peucker-Punkte (dp)
		dp_a = 0;
		dp_e = polyLine.size()-1; 

		dp_akt = dp_a;
		dp[dp_a] = 1; 

		// 2. Einfuegen weiterer dp's, solange Punkte ausserhalb eines
		//    vorgegebenen Mindestabstand zu dp-linien liegen */
		while(dp_a != dp_e) {
			  dp_r = dp_e;
			  while(dp_akt != dp_r) {
			    dp_akt = dp_r;
			    /*
				 * Suche Stuetzstelle der Linie, die am weitesten von den beiden
				 * Randpunkten entfernt ist. Funktion, gibt deren Index zurueck,
				 * solange diese ausserhalb von 'abweichung' liegt, ansonsten
				 * wird dp_akt zurueckgegeben -> Abbruch
				 */
			    int	i_a = dp_a;
			    int i_e = dp_akt;
			    double dist = abweichung;			    
			    double	max_dist = 0.0;
			    double	akt_dist = 0.0;  	  
			    
			    int i_akt = i_e;
			    for(int i = i_a+1; i<i_e; i++)	{
			    	LineSegment ls = new LineSegment(polyLine.getCoordinate(i_a), polyLine.getCoordinate(i_e));
			    	akt_dist = ls.distance(polyLine.getCoordinate(i));
			    	if(akt_dist > max_dist)	{
			    		max_dist  = akt_dist;
			    		if(max_dist > dist) i_akt = i;
			    	}	
			    }
			    dp_r = i_akt;
			  }    
			  dp_a = dp_r;      
			  dp[dp_a] = 1;  
		}	

		// 3. Uebergabe der Koordinaten
		deletedPoints = 0;
		for(int i=0; i<polyLine.size(); i++) {
			if(dp[i+deletedPoints] == 0) {
				polyLine.remove(i);
				deletedPoints++;
				i--;
			} 
		}
		
		return(deletedPoints);
	}
	
	
	public static Geometry expandPolygon(Polygon p, double width) {
		Coordinate[] coords = p.getCoordinates();
		Coordinate[] ncoords = new Coordinate[coords.length];
		double alpha, a1, a2, aext, abase;
		double r, dx, dy;
		double abaser, alphadiff;
		int il;
		// es wird immer am punkt i-1 gearbeitet
		for(int i=1; i<coords.length; i++) {
			il = i-2;
			if(i==1) il = coords.length-2;
			
			a1 = AngleFunctions.angle(coords[i-1], coords[il]);
			a2 = AngleFunctions.angle(coords[i-1], coords[i]);
			aext = a2 - a1;
			if(aext<0) aext += 2*Math.PI;
			aext = 2*Math.PI - aext;
			
			abase = AngleFunctions.angle(coords[i-1], coords[il]);
			
			//if(abase >= 0) alpha = abase + aext/2;
			//else alpha = abase - aext/2;
			alpha = abase - aext/2;
			if(alpha > Math.PI) alpha = alpha - 2*Math.PI;
			if(alpha < -Math.PI) alpha = alpha + 2*Math.PI;
			
		    //System.out.println("abase="+AngleFunctions.toDegrees(abase) + " --> aext="+AngleFunctions.toDegrees(aext) + " --> alpha=" + AngleFunctions.toDegrees(alpha) +  " ("+coords[il]+coords[i-1]+")");
		    
		    abaser = abase - Math.PI/2;
		    if(abaser < -Math.PI) abaser = abaser + 2*Math.PI;
		    alphadiff = Math.abs(alpha - abaser);
		    if(alphadiff > Math.PI) alphadiff = 2*Math.PI - alphadiff;
		    //System.out.println("abaser="+AngleFunctions.toDegrees(abaser));
		    //System.out.println("alphadiff="+AngleFunctions.toDegrees(alphadiff));
		    
		    r = width / Math.cos(alphadiff);
		    
		    dx = r * Math.cos(alpha);
		    dy = r * Math.sin(alpha);
		    ncoords[i-1] = new Coordinate(coords[i-1].x + dx, coords[i-1].y + dy);
		    //System.out.println("r="+r+" --> dx/dy " + dx + "/" + dy);
		}
		ncoords[coords.length-1] = new Coordinate(ncoords[0]);
		GeometryFactory geomfact = new GeometryFactory();
		Polygon pn =  geomfact.createPolygon(geomfact.createLinearRing(ncoords), null);
		return pn.buffer(0);
	}
}
