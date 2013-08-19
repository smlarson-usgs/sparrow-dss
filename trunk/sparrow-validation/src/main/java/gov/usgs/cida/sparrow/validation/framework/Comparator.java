/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.sparrow.validation.framework;

/**
 *
 * @author eeverman
 */
public interface Comparator {
	public boolean comp(double expected, double actual);
}
