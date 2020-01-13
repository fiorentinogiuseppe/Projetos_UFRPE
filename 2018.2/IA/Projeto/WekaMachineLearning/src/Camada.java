

public class Camada 
{
	public Neuronio Neuronios[];
	public int Tamanho;
	
	/**
	 * Camadas de Neuronios
	 * 
	 * @param t tamanho da camada
	 * @param ant Tamanho da camada anterior
	 */
	public Camada(int t, int prev)
	{
		Tamanho = t;
		Neuronios = new Neuronio[l];
		
		for(int j = 0; j < Tamanho; j++)
			Neuronios[j] = new Neuronio(ant);
	}
}
