public class Neuron 
{
	public double		Valor;
	public double[]		Pesos;
	public double		Bias;
	public double		Delta;
	
	public Neuron(int prevLayerSize)
	{
		Pesos = new double[prevLayerSize];
		Bias = Math.random() / 10000000000000.0;
		Delta = Math.random() / 10000000000000.0;
		Valor = Math.random() / 10000000000000.0;
		
		for(int i = 0; i < Pesos.length; i++)
			Pesos[i] = Math.random() / 10000000000000.0;
	}
}
