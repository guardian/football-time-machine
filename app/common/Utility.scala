package common

/**
 * Created by Andrew Bulhak on 14/04/2014.
 *
 * Various methods useful in more than one place.
 */


trait Utility {
  def roundDown(n: Int, roundTo: Int) = n / roundTo * roundTo;

}
