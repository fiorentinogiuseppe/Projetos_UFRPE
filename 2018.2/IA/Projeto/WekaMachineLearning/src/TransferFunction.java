
public interface TransferFunction 
{
	/**
	 * Funzione di trasferimento 
	 * @param value Valore in input
	 * @return Valore funzione
	 */
	public double evalute(double value);
	
	
	/**
	 * Funzione derivata
	 * @param value Valore in input
	 * @return Valore funzione derivata
	 */
	public double evaluteDerivate(double value);
}
